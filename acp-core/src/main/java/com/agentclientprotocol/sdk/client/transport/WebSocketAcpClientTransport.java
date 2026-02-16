/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.agentclientprotocol.sdk.client.transport;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import com.agentclientprotocol.sdk.json.McpJsonMapper;
import com.agentclientprotocol.sdk.json.TypeRef;
import com.agentclientprotocol.sdk.spec.AcpClientTransport;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.JSONRPCMessage;
import com.agentclientprotocol.sdk.util.Assert;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Implementation of the ACP WebSocket transport for clients that communicates with an
 * agent using WebSocket connections. Uses Jetty 9.x WebSocket client API for Java 8 compatibility.
 *
 * <p>
 * Messages are exchanged as JSON-RPC messages over WebSocket text frames.
 * </p>
 *
 * @author Mark Pollack
 */
public class WebSocketAcpClientTransport implements AcpClientTransport {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketAcpClientTransport.class);

	/** Default path for ACP WebSocket endpoints */
	public static final String DEFAULT_ACP_PATH = "/acp";

	private final URI serverUri;

	private final McpJsonMapper jsonMapper;

	private final WebSocketClient webSocketClient;

	private final Sinks.Many<JSONRPCMessage> inboundSink;

	private final Sinks.Many<JSONRPCMessage> outboundSink;

	private final Sinks.One<Void> connectionReady = Sinks.one();

	private volatile Session webSocketSession;

	private Scheduler outboundScheduler;

	private final AtomicBoolean isClosing = new AtomicBoolean(false);

	private final AtomicBoolean isConnected = new AtomicBoolean(false);

	private Consumer<Throwable> exceptionHandler = new Consumer<Throwable>() {
		@Override
		public void accept(Throwable t) {
			logger.error("Transport error", t);
		}
	};

	private Duration connectTimeout = Duration.ofSeconds(30);

	/**
	 * Creates a new WebSocketAcpClientTransport with the specified server URI and JsonMapper.
	 * @param serverUri The WebSocket URI to connect to (e.g., "ws://localhost:8080/acp")
	 * @param jsonMapper The JsonMapper to use for JSON serialization/deserialization
	 */
	public WebSocketAcpClientTransport(URI serverUri, McpJsonMapper jsonMapper) {
		this(serverUri, jsonMapper, new WebSocketClient());
	}

	/**
	 * Creates a new WebSocketAcpClientTransport with custom WebSocketClient.
	 * @param serverUri The WebSocket URI to connect to
	 * @param jsonMapper The JsonMapper to use for JSON serialization/deserialization
	 * @param webSocketClient The Jetty WebSocketClient to use for connections
	 */
	public WebSocketAcpClientTransport(URI serverUri, McpJsonMapper jsonMapper, WebSocketClient webSocketClient) {
		Assert.notNull(serverUri, "The serverUri can not be null");
		Assert.notNull(jsonMapper, "The JsonMapper can not be null");
		Assert.notNull(webSocketClient, "The WebSocketClient can not be null");

		this.serverUri = serverUri;
		this.jsonMapper = jsonMapper;
		this.webSocketClient = webSocketClient;

		this.inboundSink = Sinks.many().unicast().onBackpressureBuffer();
		this.outboundSink = Sinks.many().unicast().onBackpressureBuffer();
		// Use daemon thread so JVM can exit if closeGracefully() isn't called
		this.outboundScheduler = Schedulers.fromExecutorService(
			Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "acp-ws-client-outbound");
					t.setDaemon(true);
					return t;
				}
			}), "ws-client-outbound");
	}

	/**
	 * Sets the connection timeout for WebSocket establishment.
	 * @param timeout The connection timeout
	 * @return This transport for chaining
	 */
	public WebSocketAcpClientTransport connectTimeout(Duration timeout) {
		this.connectTimeout = timeout;
		return this;
	}

	@Override
	public Mono<Void> connect(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
		if (!isConnected.compareAndSet(false, true)) {
			return Mono.error(new IllegalStateException("Already connected"));
		}

		return Mono.<Void>fromCallable(new java.util.concurrent.Callable<Void>() {
			@Override
			public Void call() throws Exception {
				logger.info("Connecting to WebSocket server at {}", serverUri);

				// Set up inbound message handling
				handleIncomingMessages(handler);

				// Start the Jetty WebSocket client
				if (!webSocketClient.isStarted()) {
					webSocketClient.setConnectTimeout(connectTimeout.toMillis());
					webSocketClient.start();
				}

				// Connect with our listener
				Session session = webSocketClient.connect(new AcpWebSocketListener(), serverUri).get();
				webSocketSession = session;

				startOutboundProcessing();
				connectionReady.tryEmitValue(null);
				logger.info("Connected to WebSocket server at {}", serverUri);
				return null;
			}
		}).doOnError(new Consumer<Throwable>() {
			@Override
			public void accept(Throwable e) {
				logger.error("Failed to connect to WebSocket server at {}", serverUri, e);
				isConnected.set(false);
				exceptionHandler.accept(e);
			}
		}).doOnCancel(new Runnable() {
			@Override
			public void run() {
				logger.debug("WebSocket connection cancelled");
				isConnected.set(false);
			}
		}).then();
	}

	private void handleIncomingMessages(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
		this.inboundSink.asFlux()
			.flatMap(new Function<JSONRPCMessage, Mono<JSONRPCMessage>>() {
				@Override
				public Mono<JSONRPCMessage> apply(JSONRPCMessage message) {
					return Mono.just(message).transform(handler);
				}
			})
			.doOnNext(new Consumer<JSONRPCMessage>() {
				@Override
				public void accept(JSONRPCMessage response) {
					if (response != null) {
						outboundSink.tryEmitNext(response);
					}
				}
			})
			.doOnTerminate(new Runnable() {
				@Override
				public void run() {
					outboundSink.tryEmitComplete();
				}
			})
			.subscribe();
	}

	private void startOutboundProcessing() {
		this.outboundSink.asFlux()
			.publishOn(outboundScheduler)
			.subscribe(new Consumer<JSONRPCMessage>() {
				@Override
				public void accept(JSONRPCMessage message) {
					if (message != null && !isClosing.get() && webSocketSession != null && webSocketSession.isOpen()) {
						try {
							String jsonMessage = jsonMapper.writeValueAsString(message);
							logger.debug("Sending WebSocket message: {}", jsonMessage);
							webSocketSession.getRemote().sendString(jsonMessage);
						}
						catch (Exception e) {
							if (!isClosing.get()) {
								logger.error("Error sending WebSocket message", e);
								exceptionHandler.accept(e);
							}
						}
					}
				}
			});
	}

	@Override
	public Mono<Void> sendMessage(JSONRPCMessage message) {
		return connectionReady.asMono().then(Mono.defer(new java.util.function.Supplier<Mono<Void>>() {
			@Override
			public Mono<Void> get() {
				if (outboundSink.tryEmitNext(message).isSuccess()) {
					return Mono.empty();
				}
				else {
					return Mono.error(new RuntimeException("Failed to enqueue message"));
				}
			}
		}));
	}

	@Override
	public Mono<Void> closeGracefully() {
		return Mono.fromRunnable(new Runnable() {
			@Override
			public void run() {
				logger.debug("WebSocket transport closing gracefully");
				isClosing.set(true);
				inboundSink.tryEmitComplete();
				outboundSink.tryEmitComplete();
			}
		}).then(Mono.defer(new java.util.function.Supplier<Mono<Void>>() {
			@Override
			public Mono<Void> get() {
				if (webSocketSession != null && webSocketSession.isOpen()) {
					try {
						webSocketSession.close(1000, "Client closing");
					}
					catch (Exception e) {
						logger.warn("Error closing WebSocket session", e);
					}
				}
				return Mono.empty();
			}
		})).then(Mono.fromRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					outboundScheduler.dispose();
					webSocketClient.stop();
					logger.debug("WebSocket transport closed");
				}
				catch (Exception e) {
					logger.error("Error during graceful shutdown", e);
				}
			}
		}));
	}

	@Override
	public void setExceptionHandler(Consumer<Throwable> handler) {
		this.exceptionHandler = handler;
	}

	@Override
	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return jsonMapper.convertValue(data, typeRef);
	}

	/**
	 * Jetty WebSocket listener implementation for handling incoming messages.
	 */
	private class AcpWebSocketListener extends WebSocketAdapter {

		private final StringBuilder messageBuffer = new StringBuilder();

		@Override
		public void onWebSocketConnect(Session session) {
			super.onWebSocketConnect(session);
			logger.debug("WebSocket connection opened");
		}

		@Override
		public void onWebSocketText(String message) {
			logger.debug("Received WebSocket message: {}", message);

			try {
				JSONRPCMessage jsonRpcMessage = AcpSchema.deserializeJsonRpcMessage(jsonMapper, message);
				if (!inboundSink.tryEmitNext(jsonRpcMessage).isSuccess()) {
					if (!isClosing.get()) {
						logger.error("Failed to enqueue inbound message");
					}
				}
			}
			catch (Exception e) {
				if (!isClosing.get()) {
					logger.error("Error processing inbound message", e);
					exceptionHandler.accept(e);
				}
			}
		}

		@Override
		public void onWebSocketClose(int statusCode, String reason) {
			super.onWebSocketClose(statusCode, reason);
			logger.info("WebSocket connection closed: {} - {}", statusCode, reason);
			isClosing.set(true);
			inboundSink.tryEmitComplete();
		}

		@Override
		public void onWebSocketError(Throwable error) {
			super.onWebSocketError(error);
			if (!isClosing.get()) {
				logger.error("WebSocket error", error);
				exceptionHandler.accept(error);
			}
			isClosing.set(true);
			inboundSink.tryEmitComplete();
		}

	}

}
