/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.agentclientprotocol.sdk.spec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.agentclientprotocol.sdk.json.McpJsonMapper;
import com.agentclientprotocol.sdk.json.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Client Protocol (ACP) Schema based on
 * <a href="https://agentclientprotocol.com/">Agent Client Protocol specification</a>.
 *
 * This schema defines all request, response, and notification types used in ACP. ACP is a
 * protocol for communication between code editors (clients) and coding agents.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 */
public final class AcpSchema {

	private static final Logger logger = LoggerFactory.getLogger(AcpSchema.class);

	private static final TypeRef<HashMap<String, Object>> MAP_TYPE_REF = new TypeRef<HashMap<String, Object>>() {
	};

	private AcpSchema() {
	}

	public static final String JSONRPC_VERSION = "2.0";

	public static final int LATEST_PROTOCOL_VERSION = 1;

	/**
	 * Deserializes a JSON-RPC message from a JSON string into the appropriate message
	 * type (request, response, or notification).
	 * @param jsonMapper The JSON mapper to use for deserialization
	 * @param jsonText The JSON text to deserialize
	 * @return The deserialized JSON-RPC message
	 * @throws IOException If deserialization fails
	 * @throws IllegalArgumentException If the JSON structure doesn't match any known
	 * message type
	 */
	public static JSONRPCMessage deserializeJsonRpcMessage(McpJsonMapper jsonMapper, String jsonText)
			throws IOException {

		logger.debug("Received JSON message: {}", jsonText);

		HashMap<String, Object> map = jsonMapper.readValue(jsonText, MAP_TYPE_REF);

		// Determine message type based on specific JSON structure
		if (map.containsKey("method") && map.containsKey("id")) {
			return jsonMapper.convertValue(map, JSONRPCRequest.class);
		}
		else if (map.containsKey("method") && !map.containsKey("id")) {
			return jsonMapper.convertValue(map, JSONRPCNotification.class);
		}
		else if (map.containsKey("result") || map.containsKey("error")) {
			return jsonMapper.convertValue(map, JSONRPCResponse.class);
		}

		throw new IllegalArgumentException("Cannot deserialize JSONRPCMessage: " + jsonText);
	}

	// ---------------------------
	// Method Names (Agent Methods - client calls these)
	// ---------------------------

	public static final String METHOD_INITIALIZE = "initialize";

	public static final String METHOD_AUTHENTICATE = "authenticate";

	public static final String METHOD_SESSION_NEW = "session/new";

	public static final String METHOD_SESSION_LOAD = "session/load";

	public static final String METHOD_SESSION_PROMPT = "session/prompt";

	public static final String METHOD_SESSION_SET_MODE = "session/set_mode";

	public static final String METHOD_SESSION_SET_MODEL = "session/set_model";

	public static final String METHOD_SESSION_CANCEL = "session/cancel";

	// ---------------------------
	// Method Names (Client Methods - agent calls these)
	// ---------------------------

	public static final String METHOD_SESSION_REQUEST_PERMISSION = "session/request_permission";

	public static final String METHOD_SESSION_UPDATE = "session/update";

	public static final String METHOD_FS_READ_TEXT_FILE = "fs/read_text_file";

	public static final String METHOD_FS_WRITE_TEXT_FILE = "fs/write_text_file";

	public static final String METHOD_TERMINAL_CREATE = "terminal/create";

	public static final String METHOD_TERMINAL_OUTPUT = "terminal/output";

	public static final String METHOD_TERMINAL_RELEASE = "terminal/release";

	public static final String METHOD_TERMINAL_WAIT_FOR_EXIT = "terminal/wait_for_exit";

	public static final String METHOD_TERMINAL_KILL = "terminal/kill";

	// ---------------------------
	// JSON-RPC Message Types
	// ---------------------------

	/**
	 * A JSON-RPC request that expects a response.
	 *
	 * @param jsonrpc The JSON-RPC version (must be "2.0")
	 * @param id A unique identifier for the request
	 * @param method The name of the method to be invoked
	 * @param params Parameters for the method call
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class JSONRPCRequest implements JSONRPCMessage {

		private final String jsonrpc;
		private final Object id;
		private final String method;
		private final Object params;

		public JSONRPCRequest(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id, @JsonProperty("method") String method, @JsonProperty("params") Object params) {
			this.jsonrpc = jsonrpc;
			this.id = id;
			this.method = method;
			this.params = params;
		}

		@JsonProperty("jsonrpc")
		public String jsonrpc() {
			return this.jsonrpc;
		}

		@JsonProperty("id")
		public Object id() {
			return this.id;
		}

		@JsonProperty("method")
		public String method() {
			return this.method;
		}

		@JsonProperty("params")
		public Object params() {
			return this.params;
		}

public JSONRPCRequest(String method, Object id, Object params) {
			this(JSONRPC_VERSION, id, method, params);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			JSONRPCRequest that = (JSONRPCRequest) o;
			return java.util.Objects.equals(this.jsonrpc, that.jsonrpc) && java.util.Objects.equals(this.id, that.id) && java.util.Objects.equals(this.method, that.method) && java.util.Objects.equals(this.params, that.params);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(jsonrpc, id, method, params);
		}

		@Override
		public String toString() {
			return "JSONRPCRequest[jsonrpc=" + jsonrpc + ", id=" + id + ", method=" + method + ", params=" + params + "]";
		}

	}

	/**
	 * A JSON-RPC notification that does not expect a response.
	 *
	 * @param jsonrpc The JSON-RPC version (must be "2.0")
	 * @param method The name of the method to be invoked
	 * @param params Parameters for the method call
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class JSONRPCNotification implements JSONRPCMessage {

		private final String jsonrpc;
		private final String method;
		private final Object params;

		public JSONRPCNotification(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("method") String method, @JsonProperty("params") Object params) {
			this.jsonrpc = jsonrpc;
			this.method = method;
			this.params = params;
		}

		@JsonProperty("jsonrpc")
		public String jsonrpc() {
			return this.jsonrpc;
		}

		@JsonProperty("method")
		public String method() {
			return this.method;
		}

		@JsonProperty("params")
		public Object params() {
			return this.params;
		}

public JSONRPCNotification(String method, Object params) {
			this(JSONRPC_VERSION, method, params);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			JSONRPCNotification that = (JSONRPCNotification) o;
			return java.util.Objects.equals(this.jsonrpc, that.jsonrpc) && java.util.Objects.equals(this.method, that.method) && java.util.Objects.equals(this.params, that.params);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(jsonrpc, method, params);
		}

		@Override
		public String toString() {
			return "JSONRPCNotification[jsonrpc=" + jsonrpc + ", method=" + method + ", params=" + params + "]";
		}

	}

	/**
	 * A JSON-RPC response to a request.
	 *
	 * @param jsonrpc The JSON-RPC version (must be "2.0")
	 * @param id The request ID this response corresponds to
	 * @param result The result of the method call (null if error occurred)
	 * @param error The error information (null if successful)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class JSONRPCResponse implements JSONRPCMessage {

		private final String jsonrpc;
		private final Object id;
		private final Object result;
		private final JSONRPCError error;

		public JSONRPCResponse(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id, @JsonProperty("result") Object result, @JsonProperty("error") JSONRPCError error) {
			this.jsonrpc = jsonrpc;
			this.id = id;
			this.result = result;
			this.error = error;
		}

		@JsonProperty("jsonrpc")
		public String jsonrpc() {
			return this.jsonrpc;
		}

		@JsonProperty("id")
		public Object id() {
			return this.id;
		}

		@JsonProperty("result")
		public Object result() {
			return this.result;
		}

		@JsonProperty("error")
		public JSONRPCError error() {
			return this.error;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			JSONRPCResponse that = (JSONRPCResponse) o;
			return java.util.Objects.equals(this.jsonrpc, that.jsonrpc) && java.util.Objects.equals(this.id, that.id) && java.util.Objects.equals(this.result, that.result) && java.util.Objects.equals(this.error, that.error);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(jsonrpc, id, result, error);
		}

		@Override
		public String toString() {
			return "JSONRPCResponse[jsonrpc=" + jsonrpc + ", id=" + id + ", result=" + result + ", error=" + error + "]";
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class JSONRPCError {

		private final int code;
		private final String message;
		private final Object data;

		public JSONRPCError(@JsonProperty("code") int code, @JsonProperty("message") String message, @JsonProperty("data") Object data) {
			this.code = code;
			this.message = message;
			this.data = data;
		}

		@JsonProperty("code")
		public int code() {
			return this.code;
		}

		@JsonProperty("message")
		public String message() {
			return this.message;
		}

		@JsonProperty("data")
		public Object data() {
			return this.data;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			JSONRPCError that = (JSONRPCError) o;
			return this.code == that.code && java.util.Objects.equals(this.message, that.message) && java.util.Objects.equals(this.data, that.data);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(code, message, data);
		}

		@Override
		public String toString() {
			return "JSONRPCError[code=" + code + ", message=" + message + ", data=" + data + "]";
		}

	}

	/**
	 * Base type for all JSON-RPC messages.
	 */
	public interface JSONRPCMessage {

		String jsonrpc();

	}

	// ---------------------------
	// Agent Methods (Client → Agent)
	// ---------------------------

	/**
	 * Initialize request - establishes connection and negotiates capabilities
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class InitializeRequest {

		private final Integer protocolVersion;
		private final ClientCapabilities clientCapabilities;
		private final Map<String, Object> meta;

		public InitializeRequest(@JsonProperty("protocolVersion") Integer protocolVersion, @JsonProperty("clientCapabilities") ClientCapabilities clientCapabilities, @JsonProperty("_meta") Map<String, Object> meta) {
			this.protocolVersion = protocolVersion;
			this.clientCapabilities = clientCapabilities;
			this.meta = meta;
		}

		@JsonProperty("protocolVersion")
		public Integer protocolVersion() {
			return this.protocolVersion;
		}

		@JsonProperty("clientCapabilities")
		public ClientCapabilities clientCapabilities() {
			return this.clientCapabilities;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public InitializeRequest(Integer protocolVersion, ClientCapabilities clientCapabilities) {
			this(protocolVersion, clientCapabilities, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			InitializeRequest that = (InitializeRequest) o;
			return java.util.Objects.equals(this.protocolVersion, that.protocolVersion) && java.util.Objects.equals(this.clientCapabilities, that.clientCapabilities) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(protocolVersion, clientCapabilities, meta);
		}

		@Override
		public String toString() {
			return "InitializeRequest[protocolVersion=" + protocolVersion + ", clientCapabilities=" + clientCapabilities + ", meta=" + meta + "]";
		}

	}

	/**
	 * Initialize response - returns agent capabilities and auth methods
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class InitializeResponse {

		private final Integer protocolVersion;
		private final AgentCapabilities agentCapabilities;
		private final List<AuthMethod> authMethods;
		private final Map<String, Object> meta;

		public InitializeResponse(@JsonProperty("protocolVersion") Integer protocolVersion, @JsonProperty("agentCapabilities") AgentCapabilities agentCapabilities, @JsonProperty("authMethods") List<AuthMethod> authMethods, @JsonProperty("_meta") Map<String, Object> meta) {
			this.protocolVersion = protocolVersion;
			this.agentCapabilities = agentCapabilities;
			this.authMethods = authMethods;
			this.meta = meta;
		}

		@JsonProperty("protocolVersion")
		public Integer protocolVersion() {
			return this.protocolVersion;
		}

		@JsonProperty("agentCapabilities")
		public AgentCapabilities agentCapabilities() {
			return this.agentCapabilities;
		}

		@JsonProperty("authMethods")
		public List<AuthMethod> authMethods() {
			return this.authMethods;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public InitializeResponse(Integer protocolVersion, AgentCapabilities agentCapabilities,
				List<AuthMethod> authMethods) {
			this(protocolVersion, agentCapabilities, authMethods, null);
		}

		/**
		 * Creates a default successful initialization response.
		 * Uses protocol version 1 and default agent capabilities.
		 * @return A default InitializeResponse
		 */
		public static InitializeResponse ok() {
			return new InitializeResponse(1, new AgentCapabilities(), null);
		}

		/**
		 * Creates a successful initialization response with the given capabilities.
		 * @param capabilities The agent capabilities to advertise
		 * @return An InitializeResponse with the specified capabilities
		 */
		public static InitializeResponse ok(AgentCapabilities capabilities) {
			return new InitializeResponse(1, capabilities, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			InitializeResponse that = (InitializeResponse) o;
			return java.util.Objects.equals(this.protocolVersion, that.protocolVersion) && java.util.Objects.equals(this.agentCapabilities, that.agentCapabilities) && java.util.Objects.equals(this.authMethods, that.authMethods) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(protocolVersion, agentCapabilities, authMethods, meta);
		}

		@Override
		public String toString() {
			return "InitializeResponse[protocolVersion=" + protocolVersion + ", agentCapabilities=" + agentCapabilities + ", authMethods=" + authMethods + ", meta=" + meta + "]";
		}

	}

	/**
	 * Authenticate request - authenticates using specified method
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AuthenticateRequest {

		private final String methodId;

		public AuthenticateRequest(@JsonProperty("methodId") String methodId) {
			this.methodId = methodId;
		}

		@JsonProperty("methodId")
		public String methodId() {
			return this.methodId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AuthenticateRequest that = (AuthenticateRequest) o;
			return java.util.Objects.equals(this.methodId, that.methodId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(methodId);
		}

		@Override
		public String toString() {
			return "AuthenticateRequest[methodId=" + methodId + "]";
		}

	}

	/**
	 * Authenticate response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AuthenticateResponse {

		public AuthenticateResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "AuthenticateResponse[]";
		}

	}

	/**
	 * Create new session request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class NewSessionRequest {

		private final String cwd;
		private final List<McpServer> mcpServers;
		private final Map<String, Object> meta;

		public NewSessionRequest(@JsonProperty("cwd") String cwd, @JsonProperty("mcpServers") List<McpServer> mcpServers, @JsonProperty("_meta") Map<String, Object> meta) {
			this.cwd = cwd;
			this.mcpServers = mcpServers;
			this.meta = meta;
		}

		@JsonProperty("cwd")
		public String cwd() {
			return this.cwd;
		}

		@JsonProperty("mcpServers")
		public List<McpServer> mcpServers() {
			return this.mcpServers;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public NewSessionRequest(String cwd, List<McpServer> mcpServers) {
			this(cwd, mcpServers, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			NewSessionRequest that = (NewSessionRequest) o;
			return java.util.Objects.equals(this.cwd, that.cwd) && java.util.Objects.equals(this.mcpServers, that.mcpServers) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(cwd, mcpServers, meta);
		}

		@Override
		public String toString() {
			return "NewSessionRequest[cwd=" + cwd + ", mcpServers=" + mcpServers + ", meta=" + meta + "]";
		}

	}

	/**
	 * Create new session response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class NewSessionResponse {

		private final String sessionId;
		private final SessionModeState modes;
		private final SessionModelState models;
		private final Map<String, Object> meta;

		public NewSessionResponse(@JsonProperty("sessionId") String sessionId, @JsonProperty("modes") SessionModeState modes, @JsonProperty("models") SessionModelState models, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionId = sessionId;
			this.modes = modes;
			this.models = models;
			this.meta = meta;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("modes")
		public SessionModeState modes() {
			return this.modes;
		}

		@JsonProperty("models")
		public SessionModelState models() {
			return this.models;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public NewSessionResponse(String sessionId, SessionModeState modes, SessionModelState models) {
			this(sessionId, modes, models, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			NewSessionResponse that = (NewSessionResponse) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.modes, that.modes) && java.util.Objects.equals(this.models, that.models) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, modes, models, meta);
		}

		@Override
		public String toString() {
			return "NewSessionResponse[sessionId=" + sessionId + ", modes=" + modes + ", models=" + models + ", meta=" + meta + "]";
		}

	}

	/**
	 * Load existing session request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class LoadSessionRequest {

		private final String sessionId;
		private final String cwd;
		private final List<McpServer> mcpServers;
		private final Map<String, Object> meta;

		public LoadSessionRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("cwd") String cwd, @JsonProperty("mcpServers") List<McpServer> mcpServers, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionId = sessionId;
			this.cwd = cwd;
			this.mcpServers = mcpServers;
			this.meta = meta;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("cwd")
		public String cwd() {
			return this.cwd;
		}

		@JsonProperty("mcpServers")
		public List<McpServer> mcpServers() {
			return this.mcpServers;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public LoadSessionRequest(String sessionId, String cwd, List<McpServer> mcpServers) {
			this(sessionId, cwd, mcpServers, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LoadSessionRequest that = (LoadSessionRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.cwd, that.cwd) && java.util.Objects.equals(this.mcpServers, that.mcpServers) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, cwd, mcpServers, meta);
		}

		@Override
		public String toString() {
			return "LoadSessionRequest[sessionId=" + sessionId + ", cwd=" + cwd + ", mcpServers=" + mcpServers + ", meta=" + meta + "]";
		}

	}

	/**
	 * Load session response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class LoadSessionResponse {

		private final SessionModeState modes;
		private final SessionModelState models;
		private final Map<String, Object> meta;

		public LoadSessionResponse(@JsonProperty("modes") SessionModeState modes, @JsonProperty("models") SessionModelState models, @JsonProperty("_meta") Map<String, Object> meta) {
			this.modes = modes;
			this.models = models;
			this.meta = meta;
		}

		@JsonProperty("modes")
		public SessionModeState modes() {
			return this.modes;
		}

		@JsonProperty("models")
		public SessionModelState models() {
			return this.models;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public LoadSessionResponse(SessionModeState modes, SessionModelState models) {
			this(modes, models, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LoadSessionResponse that = (LoadSessionResponse) o;
			return java.util.Objects.equals(this.modes, that.modes) && java.util.Objects.equals(this.models, that.models) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(modes, models, meta);
		}

		@Override
		public String toString() {
			return "LoadSessionResponse[modes=" + modes + ", models=" + models + ", meta=" + meta + "]";
		}

	}

	/**
	 * Prompt request - sends user message to agent
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PromptRequest {

		private final String sessionId;
		private final List<ContentBlock> prompt;
		private final Map<String, Object> meta;

		public PromptRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("prompt") List<ContentBlock> prompt, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionId = sessionId;
			this.prompt = prompt;
			this.meta = meta;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("prompt")
		public List<ContentBlock> prompt() {
			return this.prompt;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public PromptRequest(String sessionId, List<ContentBlock> prompt) {
			this(sessionId, prompt, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PromptRequest that = (PromptRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.prompt, that.prompt) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, prompt, meta);
		}

		@Override
		public String toString() {
			return "PromptRequest[sessionId=" + sessionId + ", prompt=" + prompt + ", meta=" + meta + "]";
		}

	}

	/**
	 * Prompt response - indicates why agent stopped
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PromptResponse {

		private final StopReason stopReason;
		private final Map<String, Object> meta;

		public PromptResponse(@JsonProperty("stopReason") StopReason stopReason, @JsonProperty("_meta") Map<String, Object> meta) {
			this.stopReason = stopReason;
			this.meta = meta;
		}

		@JsonProperty("stopReason")
		public StopReason stopReason() {
			return this.stopReason;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public PromptResponse(StopReason stopReason) {
			this(stopReason, null);
		}

		/**
		 * Creates a response indicating the agent has finished its turn.
		 * @return A PromptResponse with END_TURN stop reason
		 */
		public static PromptResponse endTurn() {
			return new PromptResponse(StopReason.END_TURN);
		}

		/**
		 * Creates a response indicating the agent has finished its turn with a text result.
		 * Note: The text content should be sent via the context before returning this response.
		 * @param text The text (for documentation purposes; actual content sent via context)
		 * @return A PromptResponse with END_TURN stop reason
		 */
		public static PromptResponse text(String text) {
			// Text content should be sent via context.sendMessage() before returning
			return new PromptResponse(StopReason.END_TURN);
		}

		/**
		 * Creates a response indicating the agent refused the request.
		 * @return A PromptResponse with REFUSAL stop reason
		 */
		public static PromptResponse refusal() {
			return new PromptResponse(StopReason.REFUSAL);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PromptResponse that = (PromptResponse) o;
			return java.util.Objects.equals(this.stopReason, that.stopReason) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(stopReason, meta);
		}

		@Override
		public String toString() {
			return "PromptResponse[stopReason=" + stopReason + ", meta=" + meta + "]";
		}

	}

	/**
	 * Set session mode request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SetSessionModeRequest {

		private final String sessionId;
		private final String modeId;

		public SetSessionModeRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("modeId") String modeId) {
			this.sessionId = sessionId;
			this.modeId = modeId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("modeId")
		public String modeId() {
			return this.modeId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SetSessionModeRequest that = (SetSessionModeRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.modeId, that.modeId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, modeId);
		}

		@Override
		public String toString() {
			return "SetSessionModeRequest[sessionId=" + sessionId + ", modeId=" + modeId + "]";
		}

	}

	/**
	 * Set session mode response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SetSessionModeResponse {

		public SetSessionModeResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "SetSessionModeResponse[]";
		}

	}

	/**
	 * Set session model request (UNSTABLE)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SetSessionModelRequest {

		private final String sessionId;
		private final String modelId;

		public SetSessionModelRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("modelId") String modelId) {
			this.sessionId = sessionId;
			this.modelId = modelId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("modelId")
		public String modelId() {
			return this.modelId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SetSessionModelRequest that = (SetSessionModelRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.modelId, that.modelId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, modelId);
		}

		@Override
		public String toString() {
			return "SetSessionModelRequest[sessionId=" + sessionId + ", modelId=" + modelId + "]";
		}

	}

	/**
	 * Set session model response (UNSTABLE)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SetSessionModelResponse {

		public SetSessionModelResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "SetSessionModelResponse[]";
		}

	}

	/**
	 * Cancel notification - cancels ongoing operations
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class CancelNotification {

		private final String sessionId;

		public CancelNotification(@JsonProperty("sessionId") String sessionId) {
			this.sessionId = sessionId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CancelNotification that = (CancelNotification) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId);
		}

		@Override
		public String toString() {
			return "CancelNotification[sessionId=" + sessionId + "]";
		}

	}

	// ---------------------------
	// Client Methods (Agent → Client)
	// ---------------------------

	/**
	 * Request permission from user
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class RequestPermissionRequest {

		private final String sessionId;
		private final ToolCallUpdate toolCall;
		private final List<PermissionOption> options;

		public RequestPermissionRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("toolCall") ToolCallUpdate toolCall, @JsonProperty("options") List<PermissionOption> options) {
			this.sessionId = sessionId;
			this.toolCall = toolCall;
			this.options = options;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("toolCall")
		public ToolCallUpdate toolCall() {
			return this.toolCall;
		}

		@JsonProperty("options")
		public List<PermissionOption> options() {
			return this.options;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RequestPermissionRequest that = (RequestPermissionRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.toolCall, that.toolCall) && java.util.Objects.equals(this.options, that.options);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, toolCall, options);
		}

		@Override
		public String toString() {
			return "RequestPermissionRequest[sessionId=" + sessionId + ", toolCall=" + toolCall + ", options=" + options + "]";
		}

	}

	/**
	 * Permission response from user
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class RequestPermissionResponse {

		private final RequestPermissionOutcome outcome;

		public RequestPermissionResponse(@JsonProperty("outcome") RequestPermissionOutcome outcome) {
			this.outcome = outcome;
		}

		@JsonProperty("outcome")
		public RequestPermissionOutcome outcome() {
			return this.outcome;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RequestPermissionResponse that = (RequestPermissionResponse) o;
			return java.util.Objects.equals(this.outcome, that.outcome);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(outcome);
		}

		@Override
		public String toString() {
			return "RequestPermissionResponse[outcome=" + outcome + "]";
		}

	}

	/**
	 * Session update notification - real-time progress
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SessionNotification {

		private final String sessionId;
		private final SessionUpdate update;
		private final Map<String, Object> meta;

		public SessionNotification(@JsonProperty("sessionId") String sessionId, @JsonProperty("update") SessionUpdate update, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionId = sessionId;
			this.update = update;
			this.meta = meta;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("update")
		public SessionUpdate update() {
			return this.update;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public SessionNotification(String sessionId, SessionUpdate update) {
			this(sessionId, update, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SessionNotification that = (SessionNotification) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.update, that.update) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, update, meta);
		}

		@Override
		public String toString() {
			return "SessionNotification[sessionId=" + sessionId + ", update=" + update + ", meta=" + meta + "]";
		}

	}

	/**
	 * Read text file request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ReadTextFileRequest {

		private final String sessionId;
		private final String path;
		private final Integer line;
		private final Integer limit;

		public ReadTextFileRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("path") String path, @JsonProperty("line") Integer line, @JsonProperty("limit") Integer limit) {
			this.sessionId = sessionId;
			this.path = path;
			this.line = line;
			this.limit = limit;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("path")
		public String path() {
			return this.path;
		}

		@JsonProperty("line")
		public Integer line() {
			return this.line;
		}

		@JsonProperty("limit")
		public Integer limit() {
			return this.limit;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ReadTextFileRequest that = (ReadTextFileRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.path, that.path) && java.util.Objects.equals(this.line, that.line) && java.util.Objects.equals(this.limit, that.limit);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, path, line, limit);
		}

		@Override
		public String toString() {
			return "ReadTextFileRequest[sessionId=" + sessionId + ", path=" + path + ", line=" + line + ", limit=" + limit + "]";
		}

	}

	/**
	 * Read text file response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ReadTextFileResponse {

		private final String content;

		public ReadTextFileResponse(@JsonProperty("content") String content) {
			this.content = content;
		}

		@JsonProperty("content")
		public String content() {
			return this.content;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ReadTextFileResponse that = (ReadTextFileResponse) o;
			return java.util.Objects.equals(this.content, that.content);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(content);
		}

		@Override
		public String toString() {
			return "ReadTextFileResponse[content=" + content + "]";
		}

	}

	/**
	 * Write text file request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class WriteTextFileRequest {

		private final String sessionId;
		private final String path;
		private final String content;

		public WriteTextFileRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("path") String path, @JsonProperty("content") String content) {
			this.sessionId = sessionId;
			this.path = path;
			this.content = content;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("path")
		public String path() {
			return this.path;
		}

		@JsonProperty("content")
		public String content() {
			return this.content;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			WriteTextFileRequest that = (WriteTextFileRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.path, that.path) && java.util.Objects.equals(this.content, that.content);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, path, content);
		}

		@Override
		public String toString() {
			return "WriteTextFileRequest[sessionId=" + sessionId + ", path=" + path + ", content=" + content + "]";
		}

	}

	/**
	 * Write text file response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class WriteTextFileResponse {

		public WriteTextFileResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "WriteTextFileResponse[]";
		}

	}

	/**
	 * Create terminal request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class CreateTerminalRequest {

		private final String sessionId;
		private final String command;
		private final List<String> args;
		private final String cwd;
		private final List<EnvVariable> env;
		private final Long outputByteLimit;

		public CreateTerminalRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("command") String command, @JsonProperty("args") List<String> args, @JsonProperty("cwd") String cwd, @JsonProperty("env") List<EnvVariable> env, @JsonProperty("outputByteLimit") Long outputByteLimit) {
			this.sessionId = sessionId;
			this.command = command;
			this.args = args;
			this.cwd = cwd;
			this.env = env;
			this.outputByteLimit = outputByteLimit;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("command")
		public String command() {
			return this.command;
		}

		@JsonProperty("args")
		public List<String> args() {
			return this.args;
		}

		@JsonProperty("cwd")
		public String cwd() {
			return this.cwd;
		}

		@JsonProperty("env")
		public List<EnvVariable> env() {
			return this.env;
		}

		@JsonProperty("outputByteLimit")
		public Long outputByteLimit() {
			return this.outputByteLimit;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CreateTerminalRequest that = (CreateTerminalRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.command, that.command) && java.util.Objects.equals(this.args, that.args) && java.util.Objects.equals(this.cwd, that.cwd) && java.util.Objects.equals(this.env, that.env) && java.util.Objects.equals(this.outputByteLimit, that.outputByteLimit);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, command, args, cwd, env, outputByteLimit);
		}

		@Override
		public String toString() {
			return "CreateTerminalRequest[sessionId=" + sessionId + ", command=" + command + ", args=" + args + ", cwd=" + cwd + ", env=" + env + ", outputByteLimit=" + outputByteLimit + "]";
		}

	}

	/**
	 * Create terminal response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class CreateTerminalResponse {

		private final String terminalId;

		public CreateTerminalResponse(@JsonProperty("terminalId") String terminalId) {
			this.terminalId = terminalId;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CreateTerminalResponse that = (CreateTerminalResponse) o;
			return java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(terminalId);
		}

		@Override
		public String toString() {
			return "CreateTerminalResponse[terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Terminal output request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class TerminalOutputRequest {

		private final String sessionId;
		private final String terminalId;

		public TerminalOutputRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("terminalId") String terminalId) {
			this.sessionId = sessionId;
			this.terminalId = terminalId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TerminalOutputRequest that = (TerminalOutputRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, terminalId);
		}

		@Override
		public String toString() {
			return "TerminalOutputRequest[sessionId=" + sessionId + ", terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Terminal output response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class TerminalOutputResponse {

		private final String output;
		private final boolean truncated;
		private final TerminalExitStatus exitStatus;

		public TerminalOutputResponse(@JsonProperty("output") String output, @JsonProperty("truncated") boolean truncated, @JsonProperty("exitStatus") TerminalExitStatus exitStatus) {
			this.output = output;
			this.truncated = truncated;
			this.exitStatus = exitStatus;
		}

		@JsonProperty("output")
		public String output() {
			return this.output;
		}

		@JsonProperty("truncated")
		public boolean truncated() {
			return this.truncated;
		}

		@JsonProperty("exitStatus")
		public TerminalExitStatus exitStatus() {
			return this.exitStatus;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TerminalOutputResponse that = (TerminalOutputResponse) o;
			return java.util.Objects.equals(this.output, that.output) && this.truncated == that.truncated && java.util.Objects.equals(this.exitStatus, that.exitStatus);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(output, truncated, exitStatus);
		}

		@Override
		public String toString() {
			return "TerminalOutputResponse[output=" + output + ", truncated=" + truncated + ", exitStatus=" + exitStatus + "]";
		}

	}

	/**
	 * Release terminal request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ReleaseTerminalRequest {

		private final String sessionId;
		private final String terminalId;

		public ReleaseTerminalRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("terminalId") String terminalId) {
			this.sessionId = sessionId;
			this.terminalId = terminalId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ReleaseTerminalRequest that = (ReleaseTerminalRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, terminalId);
		}

		@Override
		public String toString() {
			return "ReleaseTerminalRequest[sessionId=" + sessionId + ", terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Release terminal response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ReleaseTerminalResponse {

		public ReleaseTerminalResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "ReleaseTerminalResponse[]";
		}

	}

	/**
	 * Wait for terminal exit request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class WaitForTerminalExitRequest {

		private final String sessionId;
		private final String terminalId;

		public WaitForTerminalExitRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("terminalId") String terminalId) {
			this.sessionId = sessionId;
			this.terminalId = terminalId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			WaitForTerminalExitRequest that = (WaitForTerminalExitRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, terminalId);
		}

		@Override
		public String toString() {
			return "WaitForTerminalExitRequest[sessionId=" + sessionId + ", terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Wait for terminal exit response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class WaitForTerminalExitResponse {

		private final Integer exitCode;
		private final String signal;

		public WaitForTerminalExitResponse(@JsonProperty("exitCode") Integer exitCode, @JsonProperty("signal") String signal) {
			this.exitCode = exitCode;
			this.signal = signal;
		}

		@JsonProperty("exitCode")
		public Integer exitCode() {
			return this.exitCode;
		}

		@JsonProperty("signal")
		public String signal() {
			return this.signal;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			WaitForTerminalExitResponse that = (WaitForTerminalExitResponse) o;
			return java.util.Objects.equals(this.exitCode, that.exitCode) && java.util.Objects.equals(this.signal, that.signal);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(exitCode, signal);
		}

		@Override
		public String toString() {
			return "WaitForTerminalExitResponse[exitCode=" + exitCode + ", signal=" + signal + "]";
		}

	}

	/**
	 * Kill terminal request
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class KillTerminalCommandRequest {

		private final String sessionId;
		private final String terminalId;

		public KillTerminalCommandRequest(@JsonProperty("sessionId") String sessionId, @JsonProperty("terminalId") String terminalId) {
			this.sessionId = sessionId;
			this.terminalId = terminalId;
		}

		@JsonProperty("sessionId")
		public String sessionId() {
			return this.sessionId;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			KillTerminalCommandRequest that = (KillTerminalCommandRequest) o;
			return java.util.Objects.equals(this.sessionId, that.sessionId) && java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionId, terminalId);
		}

		@Override
		public String toString() {
			return "KillTerminalCommandRequest[sessionId=" + sessionId + ", terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Kill terminal response
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class KillTerminalCommandResponse {

		public KillTerminalCommandResponse() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "KillTerminalCommandResponse[]";
		}

	}

	// ---------------------------
	// Capabilities
	// ---------------------------

	/**
	 * Client capabilities
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ClientCapabilities {

		private final FileSystemCapability fs;
		private final Boolean terminal;
		private final Map<String, Object> meta;

		public ClientCapabilities(@JsonProperty("fs") FileSystemCapability fs, @JsonProperty("terminal") Boolean terminal, @JsonProperty("_meta") Map<String, Object> meta) {
			this.fs = fs;
			this.terminal = terminal;
			this.meta = meta;
		}

		@JsonProperty("fs")
		public FileSystemCapability fs() {
			return this.fs;
		}

		@JsonProperty("terminal")
		public Boolean terminal() {
			return this.terminal;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public ClientCapabilities() {
			this(new FileSystemCapability(), false, null);
		}

		public ClientCapabilities(FileSystemCapability fs, Boolean terminal) {
			this(fs, terminal, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ClientCapabilities that = (ClientCapabilities) o;
			return java.util.Objects.equals(this.fs, that.fs) && java.util.Objects.equals(this.terminal, that.terminal) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(fs, terminal, meta);
		}

		@Override
		public String toString() {
			return "ClientCapabilities[fs=" + fs + ", terminal=" + terminal + ", meta=" + meta + "]";
		}

	}

	/**
	 * File system capabilities
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class FileSystemCapability {

		private final Boolean readTextFile;
		private final Boolean writeTextFile;

		public FileSystemCapability(@JsonProperty("readTextFile") Boolean readTextFile, @JsonProperty("writeTextFile") Boolean writeTextFile) {
			this.readTextFile = readTextFile;
			this.writeTextFile = writeTextFile;
		}

		@JsonProperty("readTextFile")
		public Boolean readTextFile() {
			return this.readTextFile;
		}

		@JsonProperty("writeTextFile")
		public Boolean writeTextFile() {
			return this.writeTextFile;
		}

public FileSystemCapability() {
			this(false, false);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			FileSystemCapability that = (FileSystemCapability) o;
			return java.util.Objects.equals(this.readTextFile, that.readTextFile) && java.util.Objects.equals(this.writeTextFile, that.writeTextFile);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(readTextFile, writeTextFile);
		}

		@Override
		public String toString() {
			return "FileSystemCapability[readTextFile=" + readTextFile + ", writeTextFile=" + writeTextFile + "]";
		}

	}

	/**
	 * Agent capabilities
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AgentCapabilities {

		private final Boolean loadSession;
		private final McpCapabilities mcpCapabilities;
		private final PromptCapabilities promptCapabilities;
		private final Map<String, Object> meta;

		public AgentCapabilities(@JsonProperty("loadSession") Boolean loadSession, @JsonProperty("mcpCapabilities") McpCapabilities mcpCapabilities, @JsonProperty("promptCapabilities") PromptCapabilities promptCapabilities, @JsonProperty("_meta") Map<String, Object> meta) {
			this.loadSession = loadSession;
			this.mcpCapabilities = mcpCapabilities;
			this.promptCapabilities = promptCapabilities;
			this.meta = meta;
		}

		@JsonProperty("loadSession")
		public Boolean loadSession() {
			return this.loadSession;
		}

		@JsonProperty("mcpCapabilities")
		public McpCapabilities mcpCapabilities() {
			return this.mcpCapabilities;
		}

		@JsonProperty("promptCapabilities")
		public PromptCapabilities promptCapabilities() {
			return this.promptCapabilities;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public AgentCapabilities() {
			this(false, new McpCapabilities(), new PromptCapabilities(), null);
		}

		public AgentCapabilities(Boolean loadSession, McpCapabilities mcpCapabilities,
				PromptCapabilities promptCapabilities) {
			this(loadSession, mcpCapabilities, promptCapabilities, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AgentCapabilities that = (AgentCapabilities) o;
			return java.util.Objects.equals(this.loadSession, that.loadSession) && java.util.Objects.equals(this.mcpCapabilities, that.mcpCapabilities) && java.util.Objects.equals(this.promptCapabilities, that.promptCapabilities) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(loadSession, mcpCapabilities, promptCapabilities, meta);
		}

		@Override
		public String toString() {
			return "AgentCapabilities[loadSession=" + loadSession + ", mcpCapabilities=" + mcpCapabilities + ", promptCapabilities=" + promptCapabilities + ", meta=" + meta + "]";
		}

	}

	/**
	 * MCP capabilities supported by agent
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class McpCapabilities {

		private final Boolean http;
		private final Boolean sse;

		public McpCapabilities(@JsonProperty("http") Boolean http, @JsonProperty("sse") Boolean sse) {
			this.http = http;
			this.sse = sse;
		}

		@JsonProperty("http")
		public Boolean http() {
			return this.http;
		}

		@JsonProperty("sse")
		public Boolean sse() {
			return this.sse;
		}

public McpCapabilities() {
			this(false, false);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			McpCapabilities that = (McpCapabilities) o;
			return java.util.Objects.equals(this.http, that.http) && java.util.Objects.equals(this.sse, that.sse);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(http, sse);
		}

		@Override
		public String toString() {
			return "McpCapabilities[http=" + http + ", sse=" + sse + "]";
		}

	}

	/**
	 * Prompt capabilities
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PromptCapabilities {

		private final Boolean audio;
		private final Boolean embeddedContext;
		private final Boolean image;

		public PromptCapabilities(@JsonProperty("audio") Boolean audio, @JsonProperty("embeddedContext") Boolean embeddedContext, @JsonProperty("image") Boolean image) {
			this.audio = audio;
			this.embeddedContext = embeddedContext;
			this.image = image;
		}

		@JsonProperty("audio")
		public Boolean audio() {
			return this.audio;
		}

		@JsonProperty("embeddedContext")
		public Boolean embeddedContext() {
			return this.embeddedContext;
		}

		@JsonProperty("image")
		public Boolean image() {
			return this.image;
		}

public PromptCapabilities() {
			this(false, false, false);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PromptCapabilities that = (PromptCapabilities) o;
			return java.util.Objects.equals(this.audio, that.audio) && java.util.Objects.equals(this.embeddedContext, that.embeddedContext) && java.util.Objects.equals(this.image, that.image);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(audio, embeddedContext, image);
		}

		@Override
		public String toString() {
			return "PromptCapabilities[audio=" + audio + ", embeddedContext=" + embeddedContext + ", image=" + image + "]";
		}

	}

	// ---------------------------
	// Session Types
	// ---------------------------

	/**
	 * Session mode state
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SessionModeState {

		private final String currentModeId;
		private final List<SessionMode> availableModes;

		public SessionModeState(@JsonProperty("currentModeId") String currentModeId, @JsonProperty("availableModes") List<SessionMode> availableModes) {
			this.currentModeId = currentModeId;
			this.availableModes = availableModes;
		}

		@JsonProperty("currentModeId")
		public String currentModeId() {
			return this.currentModeId;
		}

		@JsonProperty("availableModes")
		public List<SessionMode> availableModes() {
			return this.availableModes;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SessionModeState that = (SessionModeState) o;
			return java.util.Objects.equals(this.currentModeId, that.currentModeId) && java.util.Objects.equals(this.availableModes, that.availableModes);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(currentModeId, availableModes);
		}

		@Override
		public String toString() {
			return "SessionModeState[currentModeId=" + currentModeId + ", availableModes=" + availableModes + "]";
		}

	}

	/**
	 * Session mode
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SessionMode {

		private final String id;
		private final String name;
		private final String description;

		public SessionMode(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("description") String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}

		@JsonProperty("id")
		public String id() {
			return this.id;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("description")
		public String description() {
			return this.description;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SessionMode that = (SessionMode) o;
			return java.util.Objects.equals(this.id, that.id) && java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.description, that.description);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(id, name, description);
		}

		@Override
		public String toString() {
			return "SessionMode[id=" + id + ", name=" + name + ", description=" + description + "]";
		}

	}

	/**
	 * Session model state (UNSTABLE)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class SessionModelState {

		private final String currentModelId;
		private final List<ModelInfo> availableModels;

		public SessionModelState(@JsonProperty("currentModelId") String currentModelId, @JsonProperty("availableModels") List<ModelInfo> availableModels) {
			this.currentModelId = currentModelId;
			this.availableModels = availableModels;
		}

		@JsonProperty("currentModelId")
		public String currentModelId() {
			return this.currentModelId;
		}

		@JsonProperty("availableModels")
		public List<ModelInfo> availableModels() {
			return this.availableModels;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SessionModelState that = (SessionModelState) o;
			return java.util.Objects.equals(this.currentModelId, that.currentModelId) && java.util.Objects.equals(this.availableModels, that.availableModels);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(currentModelId, availableModels);
		}

		@Override
		public String toString() {
			return "SessionModelState[currentModelId=" + currentModelId + ", availableModels=" + availableModels + "]";
		}

	}

	/**
	 * Model info (UNSTABLE)
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ModelInfo {

		private final String modelId;
		private final String name;
		private final String description;

		public ModelInfo(@JsonProperty("modelId") String modelId, @JsonProperty("name") String name, @JsonProperty("description") String description) {
			this.modelId = modelId;
			this.name = name;
			this.description = description;
		}

		@JsonProperty("modelId")
		public String modelId() {
			return this.modelId;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("description")
		public String description() {
			return this.description;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ModelInfo that = (ModelInfo) o;
			return java.util.Objects.equals(this.modelId, that.modelId) && java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.description, that.description);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(modelId, name, description);
		}

		@Override
		public String toString() {
			return "ModelInfo[modelId=" + modelId + ", name=" + name + ", description=" + description + "]";
		}

	}

	// ---------------------------
	// Content Types
	// ---------------------------

	/**
	 * Content block - base type for all content
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
	@JsonSubTypes({ @JsonSubTypes.Type(value = TextContent.class, name = "text"),
			@JsonSubTypes.Type(value = ImageContent.class, name = "image"),
			@JsonSubTypes.Type(value = AudioContent.class, name = "audio"),
			@JsonSubTypes.Type(value = ResourceLink.class, name = "resource_link"),
			@JsonSubTypes.Type(value = Resource.class, name = "resource") })
	public interface ContentBlock {

	}

	/**
	 * Text content
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class TextContent implements ContentBlock {

		private final String type;
		private final String text;
		private final Annotations annotations;
		private final Map<String, Object> meta;

		public TextContent(@JsonProperty("type") String type, @JsonProperty("text") String text, @JsonProperty("annotations") Annotations annotations, @JsonProperty("_meta") Map<String, Object> meta) {
			this.type = type;
			this.text = text;
			this.annotations = annotations;
			this.meta = meta;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("text")
		public String text() {
			return this.text;
		}

		@JsonProperty("annotations")
		public Annotations annotations() {
			return this.annotations;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public TextContent(String text) {
			this("text", text, null, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TextContent that = (TextContent) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.text, that.text) && java.util.Objects.equals(this.annotations, that.annotations) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, text, annotations, meta);
		}

		@Override
		public String toString() {
			return "TextContent[type=" + type + ", text=" + text + ", annotations=" + annotations + ", meta=" + meta + "]";
		}

	}

	/**
	 * Image content
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ImageContent implements ContentBlock {

		private final String type;
		private final String data;
		private final String mimeType;
		private final String uri;
		private final Annotations annotations;
		private final Map<String, Object> meta;

		public ImageContent(@JsonProperty("type") String type, @JsonProperty("data") String data, @JsonProperty("mimeType") String mimeType, @JsonProperty("uri") String uri, @JsonProperty("annotations") Annotations annotations, @JsonProperty("_meta") Map<String, Object> meta) {
			this.type = type;
			this.data = data;
			this.mimeType = mimeType;
			this.uri = uri;
			this.annotations = annotations;
			this.meta = meta;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("data")
		public String data() {
			return this.data;
		}

		@JsonProperty("mimeType")
		public String mimeType() {
			return this.mimeType;
		}

		@JsonProperty("uri")
		public String uri() {
			return this.uri;
		}

		@JsonProperty("annotations")
		public Annotations annotations() {
			return this.annotations;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ImageContent that = (ImageContent) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.data, that.data) && java.util.Objects.equals(this.mimeType, that.mimeType) && java.util.Objects.equals(this.uri, that.uri) && java.util.Objects.equals(this.annotations, that.annotations) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, data, mimeType, uri, annotations, meta);
		}

		@Override
		public String toString() {
			return "ImageContent[type=" + type + ", data=" + data + ", mimeType=" + mimeType + ", uri=" + uri + ", annotations=" + annotations + ", meta=" + meta + "]";
		}

	}

	/**
	 * Audio content
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AudioContent implements ContentBlock {

		private final String type;
		private final String data;
		private final String mimeType;
		private final Annotations annotations;
		private final Map<String, Object> meta;

		public AudioContent(@JsonProperty("type") String type, @JsonProperty("data") String data, @JsonProperty("mimeType") String mimeType, @JsonProperty("annotations") Annotations annotations, @JsonProperty("_meta") Map<String, Object> meta) {
			this.type = type;
			this.data = data;
			this.mimeType = mimeType;
			this.annotations = annotations;
			this.meta = meta;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("data")
		public String data() {
			return this.data;
		}

		@JsonProperty("mimeType")
		public String mimeType() {
			return this.mimeType;
		}

		@JsonProperty("annotations")
		public Annotations annotations() {
			return this.annotations;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AudioContent that = (AudioContent) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.data, that.data) && java.util.Objects.equals(this.mimeType, that.mimeType) && java.util.Objects.equals(this.annotations, that.annotations) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, data, mimeType, annotations, meta);
		}

		@Override
		public String toString() {
			return "AudioContent[type=" + type + ", data=" + data + ", mimeType=" + mimeType + ", annotations=" + annotations + ", meta=" + meta + "]";
		}

	}

	/**
	 * Resource link
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ResourceLink implements ContentBlock {

		private final String type;
		private final String name;
		private final String uri;
		private final String title;
		private final String description;
		private final String mimeType;
		private final Long size;
		private final Annotations annotations;
		private final Map<String, Object> meta;

		public ResourceLink(@JsonProperty("type") String type, @JsonProperty("name") String name, @JsonProperty("uri") String uri, @JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("mimeType") String mimeType, @JsonProperty("size") Long size, @JsonProperty("annotations") Annotations annotations, @JsonProperty("_meta") Map<String, Object> meta) {
			this.type = type;
			this.name = name;
			this.uri = uri;
			this.title = title;
			this.description = description;
			this.mimeType = mimeType;
			this.size = size;
			this.annotations = annotations;
			this.meta = meta;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("uri")
		public String uri() {
			return this.uri;
		}

		@JsonProperty("title")
		public String title() {
			return this.title;
		}

		@JsonProperty("description")
		public String description() {
			return this.description;
		}

		@JsonProperty("mimeType")
		public String mimeType() {
			return this.mimeType;
		}

		@JsonProperty("size")
		public Long size() {
			return this.size;
		}

		@JsonProperty("annotations")
		public Annotations annotations() {
			return this.annotations;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ResourceLink that = (ResourceLink) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.uri, that.uri) && java.util.Objects.equals(this.title, that.title) && java.util.Objects.equals(this.description, that.description) && java.util.Objects.equals(this.mimeType, that.mimeType) && java.util.Objects.equals(this.size, that.size) && java.util.Objects.equals(this.annotations, that.annotations) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, name, uri, title, description, mimeType, size, annotations, meta);
		}

		@Override
		public String toString() {
			return "ResourceLink[type=" + type + ", name=" + name + ", uri=" + uri + ", title=" + title + ", description=" + description + ", mimeType=" + mimeType + ", size=" + size + ", annotations=" + annotations + ", meta=" + meta + "]";
		}

	}

	/**
	 * Embedded resource
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class Resource implements ContentBlock {

		private final String type;
		private final EmbeddedResourceResource resource;
		private final Annotations annotations;
		private final Map<String, Object> meta;

		public Resource(@JsonProperty("type") String type, @JsonProperty("resource") EmbeddedResourceResource resource, @JsonProperty("annotations") Annotations annotations, @JsonProperty("_meta") Map<String, Object> meta) {
			this.type = type;
			this.resource = resource;
			this.annotations = annotations;
			this.meta = meta;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("resource")
		public EmbeddedResourceResource resource() {
			return this.resource;
		}

		@JsonProperty("annotations")
		public Annotations annotations() {
			return this.annotations;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Resource that = (Resource) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.resource, that.resource) && java.util.Objects.equals(this.annotations, that.annotations) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, resource, annotations, meta);
		}

		@Override
		public String toString() {
			return "Resource[type=" + type + ", resource=" + resource + ", annotations=" + annotations + ", meta=" + meta + "]";
		}

	}

	/**
	 * Embedded resource content
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
	@JsonSubTypes({ @JsonSubTypes.Type(value = TextResourceContents.class),
			@JsonSubTypes.Type(value = BlobResourceContents.class) })
	public interface EmbeddedResourceResource {

	}

	/**
	 * Text resource contents
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class TextResourceContents implements EmbeddedResourceResource {

		private final String text;
		private final String uri;
		private final String mimeType;

		public TextResourceContents(@JsonProperty("text") String text, @JsonProperty("uri") String uri, @JsonProperty("mimeType") String mimeType) {
			this.text = text;
			this.uri = uri;
			this.mimeType = mimeType;
		}

		@JsonProperty("text")
		public String text() {
			return this.text;
		}

		@JsonProperty("uri")
		public String uri() {
			return this.uri;
		}

		@JsonProperty("mimeType")
		public String mimeType() {
			return this.mimeType;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TextResourceContents that = (TextResourceContents) o;
			return java.util.Objects.equals(this.text, that.text) && java.util.Objects.equals(this.uri, that.uri) && java.util.Objects.equals(this.mimeType, that.mimeType);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(text, uri, mimeType);
		}

		@Override
		public String toString() {
			return "TextResourceContents[text=" + text + ", uri=" + uri + ", mimeType=" + mimeType + "]";
		}

	}

	/**
	 * Blob resource contents
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class BlobResourceContents implements EmbeddedResourceResource {

		private final String blob;
		private final String uri;
		private final String mimeType;

		public BlobResourceContents(@JsonProperty("blob") String blob, @JsonProperty("uri") String uri, @JsonProperty("mimeType") String mimeType) {
			this.blob = blob;
			this.uri = uri;
			this.mimeType = mimeType;
		}

		@JsonProperty("blob")
		public String blob() {
			return this.blob;
		}

		@JsonProperty("uri")
		public String uri() {
			return this.uri;
		}

		@JsonProperty("mimeType")
		public String mimeType() {
			return this.mimeType;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			BlobResourceContents that = (BlobResourceContents) o;
			return java.util.Objects.equals(this.blob, that.blob) && java.util.Objects.equals(this.uri, that.uri) && java.util.Objects.equals(this.mimeType, that.mimeType);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(blob, uri, mimeType);
		}

		@Override
		public String toString() {
			return "BlobResourceContents[blob=" + blob + ", uri=" + uri + ", mimeType=" + mimeType + "]";
		}

	}

	/**
	 * Annotations for content
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class Annotations {

		private final List<Role> audience;
		private final Double priority;
		private final String lastModified;

		public Annotations(@JsonProperty("audience") List<Role> audience, @JsonProperty("priority") Double priority, @JsonProperty("lastModified") String lastModified) {
			this.audience = audience;
			this.priority = priority;
			this.lastModified = lastModified;
		}

		@JsonProperty("audience")
		public List<Role> audience() {
			return this.audience;
		}

		@JsonProperty("priority")
		public Double priority() {
			return this.priority;
		}

		@JsonProperty("lastModified")
		public String lastModified() {
			return this.lastModified;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Annotations that = (Annotations) o;
			return java.util.Objects.equals(this.audience, that.audience) && java.util.Objects.equals(this.priority, that.priority) && java.util.Objects.equals(this.lastModified, that.lastModified);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(audience, priority, lastModified);
		}

		@Override
		public String toString() {
			return "Annotations[audience=" + audience + ", priority=" + priority + ", lastModified=" + lastModified + "]";
		}

	}

	// ---------------------------
	// Session Updates
	// ---------------------------

	/**
	 * Session update - different types of updates
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "sessionUpdate", visible = true)
	@JsonSubTypes({ @JsonSubTypes.Type(value = UserMessageChunk.class, name = "user_message_chunk"),
			@JsonSubTypes.Type(value = AgentMessageChunk.class, name = "agent_message_chunk"),
			@JsonSubTypes.Type(value = AgentThoughtChunk.class, name = "agent_thought_chunk"),
			@JsonSubTypes.Type(value = ToolCall.class, name = "tool_call"),
			@JsonSubTypes.Type(value = ToolCallUpdateNotification.class, name = "tool_call_update"),
			@JsonSubTypes.Type(value = Plan.class, name = "plan"),
			@JsonSubTypes.Type(value = AvailableCommandsUpdate.class, name = "available_commands_update"),
			@JsonSubTypes.Type(value = CurrentModeUpdate.class, name = "current_mode_update") })
	public interface SessionUpdate {

	}

	/**
	 * User message chunk
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class UserMessageChunk implements SessionUpdate {

		private final String sessionUpdate;
		private final ContentBlock content;
		private final Map<String, Object> meta;

		public UserMessageChunk(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("content") ContentBlock content, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.content = content;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("content")
		public ContentBlock content() {
			return this.content;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public UserMessageChunk(String sessionUpdate, ContentBlock content) {
			this(sessionUpdate, content, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UserMessageChunk that = (UserMessageChunk) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, content, meta);
		}

		@Override
		public String toString() {
			return "UserMessageChunk[sessionUpdate=" + sessionUpdate + ", content=" + content + ", meta=" + meta + "]";
		}

	}

	/**
	 * Agent message chunk
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AgentMessageChunk implements SessionUpdate {

		private final String sessionUpdate;
		private final ContentBlock content;
		private final Map<String, Object> meta;

		public AgentMessageChunk(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("content") ContentBlock content, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.content = content;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("content")
		public ContentBlock content() {
			return this.content;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public AgentMessageChunk(String sessionUpdate, ContentBlock content) {
			this(sessionUpdate, content, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AgentMessageChunk that = (AgentMessageChunk) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, content, meta);
		}

		@Override
		public String toString() {
			return "AgentMessageChunk[sessionUpdate=" + sessionUpdate + ", content=" + content + ", meta=" + meta + "]";
		}

	}

	/**
	 * Agent thought chunk
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AgentThoughtChunk implements SessionUpdate {

		private final String sessionUpdate;
		private final ContentBlock content;
		private final Map<String, Object> meta;

		public AgentThoughtChunk(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("content") ContentBlock content, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.content = content;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("content")
		public ContentBlock content() {
			return this.content;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public AgentThoughtChunk(String sessionUpdate, ContentBlock content) {
			this(sessionUpdate, content, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AgentThoughtChunk that = (AgentThoughtChunk) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, content, meta);
		}

		@Override
		public String toString() {
			return "AgentThoughtChunk[sessionUpdate=" + sessionUpdate + ", content=" + content + ", meta=" + meta + "]";
		}

	}

	/**
	 * Tool call
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCall implements SessionUpdate {

		private final String sessionUpdate;
		private final String toolCallId;
		private final String title;
		private final ToolKind kind;
		private final ToolCallStatus status;
		private final List<ToolCallContent> content;
		private final List<ToolCallLocation> locations;
		private final Object rawInput;
		private final Object rawOutput;
		private final Map<String, Object> meta;

		public ToolCall(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("toolCallId") String toolCallId, @JsonProperty("title") String title, @JsonProperty("kind") ToolKind kind, @JsonProperty("status") ToolCallStatus status, @JsonProperty("content") List<ToolCallContent> content, @JsonProperty("locations") List<ToolCallLocation> locations, @JsonProperty("rawInput") Object rawInput, @JsonProperty("rawOutput") Object rawOutput, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.toolCallId = toolCallId;
			this.title = title;
			this.kind = kind;
			this.status = status;
			this.content = content;
			this.locations = locations;
			this.rawInput = rawInput;
			this.rawOutput = rawOutput;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("toolCallId")
		public String toolCallId() {
			return this.toolCallId;
		}

		@JsonProperty("title")
		public String title() {
			return this.title;
		}

		@JsonProperty("kind")
		public ToolKind kind() {
			return this.kind;
		}

		@JsonProperty("status")
		public ToolCallStatus status() {
			return this.status;
		}

		@JsonProperty("content")
		public List<ToolCallContent> content() {
			return this.content;
		}

		@JsonProperty("locations")
		public List<ToolCallLocation> locations() {
			return this.locations;
		}

		@JsonProperty("rawInput")
		public Object rawInput() {
			return this.rawInput;
		}

		@JsonProperty("rawOutput")
		public Object rawOutput() {
			return this.rawOutput;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCall that = (ToolCall) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.toolCallId, that.toolCallId) && java.util.Objects.equals(this.title, that.title) && java.util.Objects.equals(this.kind, that.kind) && java.util.Objects.equals(this.status, that.status) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.locations, that.locations) && java.util.Objects.equals(this.rawInput, that.rawInput) && java.util.Objects.equals(this.rawOutput, that.rawOutput) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, toolCallId, title, kind, status, content, locations, rawInput, rawOutput, meta);
		}

		@Override
		public String toString() {
			return "ToolCall[sessionUpdate=" + sessionUpdate + ", toolCallId=" + toolCallId + ", title=" + title + ", kind=" + kind + ", status=" + status + ", content=" + content + ", locations=" + locations + ", rawInput=" + rawInput + ", rawOutput=" + rawOutput + ", meta=" + meta + "]";
		}

	}

	/**
	 * Tool call update
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallUpdate {

		private final String toolCallId;
		private final String title;
		private final ToolKind kind;
		private final ToolCallStatus status;
		private final List<ToolCallContent> content;
		private final List<ToolCallLocation> locations;
		private final Object rawInput;
		private final Object rawOutput;

		public ToolCallUpdate(@JsonProperty("toolCallId") String toolCallId, @JsonProperty("title") String title, @JsonProperty("kind") ToolKind kind, @JsonProperty("status") ToolCallStatus status, @JsonProperty("content") List<ToolCallContent> content, @JsonProperty("locations") List<ToolCallLocation> locations, @JsonProperty("rawInput") Object rawInput, @JsonProperty("rawOutput") Object rawOutput) {
			this.toolCallId = toolCallId;
			this.title = title;
			this.kind = kind;
			this.status = status;
			this.content = content;
			this.locations = locations;
			this.rawInput = rawInput;
			this.rawOutput = rawOutput;
		}

		@JsonProperty("toolCallId")
		public String toolCallId() {
			return this.toolCallId;
		}

		@JsonProperty("title")
		public String title() {
			return this.title;
		}

		@JsonProperty("kind")
		public ToolKind kind() {
			return this.kind;
		}

		@JsonProperty("status")
		public ToolCallStatus status() {
			return this.status;
		}

		@JsonProperty("content")
		public List<ToolCallContent> content() {
			return this.content;
		}

		@JsonProperty("locations")
		public List<ToolCallLocation> locations() {
			return this.locations;
		}

		@JsonProperty("rawInput")
		public Object rawInput() {
			return this.rawInput;
		}

		@JsonProperty("rawOutput")
		public Object rawOutput() {
			return this.rawOutput;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallUpdate that = (ToolCallUpdate) o;
			return java.util.Objects.equals(this.toolCallId, that.toolCallId) && java.util.Objects.equals(this.title, that.title) && java.util.Objects.equals(this.kind, that.kind) && java.util.Objects.equals(this.status, that.status) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.locations, that.locations) && java.util.Objects.equals(this.rawInput, that.rawInput) && java.util.Objects.equals(this.rawOutput, that.rawOutput);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(toolCallId, title, kind, status, content, locations, rawInput, rawOutput);
		}

		@Override
		public String toString() {
			return "ToolCallUpdate[toolCallId=" + toolCallId + ", title=" + title + ", kind=" + kind + ", status=" + status + ", content=" + content + ", locations=" + locations + ", rawInput=" + rawInput + ", rawOutput=" + rawOutput + "]";
		}

	}

	/**
	 * Tool call update notification
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallUpdateNotification implements SessionUpdate {

		private final String sessionUpdate;
		private final String toolCallId;
		private final String title;
		private final ToolKind kind;
		private final ToolCallStatus status;
		private final List<ToolCallContent> content;
		private final List<ToolCallLocation> locations;
		private final Object rawInput;
		private final Object rawOutput;
		private final Map<String, Object> meta;

		public ToolCallUpdateNotification(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("toolCallId") String toolCallId, @JsonProperty("title") String title, @JsonProperty("kind") ToolKind kind, @JsonProperty("status") ToolCallStatus status, @JsonProperty("content") List<ToolCallContent> content, @JsonProperty("locations") List<ToolCallLocation> locations, @JsonProperty("rawInput") Object rawInput, @JsonProperty("rawOutput") Object rawOutput, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.toolCallId = toolCallId;
			this.title = title;
			this.kind = kind;
			this.status = status;
			this.content = content;
			this.locations = locations;
			this.rawInput = rawInput;
			this.rawOutput = rawOutput;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("toolCallId")
		public String toolCallId() {
			return this.toolCallId;
		}

		@JsonProperty("title")
		public String title() {
			return this.title;
		}

		@JsonProperty("kind")
		public ToolKind kind() {
			return this.kind;
		}

		@JsonProperty("status")
		public ToolCallStatus status() {
			return this.status;
		}

		@JsonProperty("content")
		public List<ToolCallContent> content() {
			return this.content;
		}

		@JsonProperty("locations")
		public List<ToolCallLocation> locations() {
			return this.locations;
		}

		@JsonProperty("rawInput")
		public Object rawInput() {
			return this.rawInput;
		}

		@JsonProperty("rawOutput")
		public Object rawOutput() {
			return this.rawOutput;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallUpdateNotification that = (ToolCallUpdateNotification) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.toolCallId, that.toolCallId) && java.util.Objects.equals(this.title, that.title) && java.util.Objects.equals(this.kind, that.kind) && java.util.Objects.equals(this.status, that.status) && java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.locations, that.locations) && java.util.Objects.equals(this.rawInput, that.rawInput) && java.util.Objects.equals(this.rawOutput, that.rawOutput) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, toolCallId, title, kind, status, content, locations, rawInput, rawOutput, meta);
		}

		@Override
		public String toString() {
			return "ToolCallUpdateNotification[sessionUpdate=" + sessionUpdate + ", toolCallId=" + toolCallId + ", title=" + title + ", kind=" + kind + ", status=" + status + ", content=" + content + ", locations=" + locations + ", rawInput=" + rawInput + ", rawOutput=" + rawOutput + ", meta=" + meta + "]";
		}

	}

	/**
	 * Plan update
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class Plan implements SessionUpdate {

		private final String sessionUpdate;
		private final List<PlanEntry> entries;
		private final Map<String, Object> meta;

		public Plan(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("entries") List<PlanEntry> entries, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.entries = entries;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("entries")
		public List<PlanEntry> entries() {
			return this.entries;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public Plan(String sessionUpdate, List<PlanEntry> entries) {
			this(sessionUpdate, entries, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Plan that = (Plan) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.entries, that.entries) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, entries, meta);
		}

		@Override
		public String toString() {
			return "Plan[sessionUpdate=" + sessionUpdate + ", entries=" + entries + ", meta=" + meta + "]";
		}

	}

	/**
	 * Available commands update
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AvailableCommandsUpdate implements SessionUpdate {

		private final String sessionUpdate;
		private final List<AvailableCommand> availableCommands;
		private final Map<String, Object> meta;

		public AvailableCommandsUpdate(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("availableCommands") List<AvailableCommand> availableCommands, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.availableCommands = availableCommands;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("availableCommands")
		public List<AvailableCommand> availableCommands() {
			return this.availableCommands;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public AvailableCommandsUpdate(String sessionUpdate, List<AvailableCommand> availableCommands) {
			this(sessionUpdate, availableCommands, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AvailableCommandsUpdate that = (AvailableCommandsUpdate) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.availableCommands, that.availableCommands) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, availableCommands, meta);
		}

		@Override
		public String toString() {
			return "AvailableCommandsUpdate[sessionUpdate=" + sessionUpdate + ", availableCommands=" + availableCommands + ", meta=" + meta + "]";
		}

	}

	/**
	 * Current mode update
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class CurrentModeUpdate implements SessionUpdate {

		private final String sessionUpdate;
		private final String currentModeId;
		private final Map<String, Object> meta;

		public CurrentModeUpdate(@JsonProperty("sessionUpdate") String sessionUpdate, @JsonProperty("currentModeId") String currentModeId, @JsonProperty("_meta") Map<String, Object> meta) {
			this.sessionUpdate = sessionUpdate;
			this.currentModeId = currentModeId;
			this.meta = meta;
		}

		@JsonProperty("sessionUpdate")
		public String sessionUpdate() {
			return this.sessionUpdate;
		}

		@JsonProperty("currentModeId")
		public String currentModeId() {
			return this.currentModeId;
		}

		@JsonProperty("_meta")
		public Map<String, Object> meta() {
			return this.meta;
		}

public CurrentModeUpdate(String sessionUpdate, String currentModeId) {
			this(sessionUpdate, currentModeId, null);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CurrentModeUpdate that = (CurrentModeUpdate) o;
			return java.util.Objects.equals(this.sessionUpdate, that.sessionUpdate) && java.util.Objects.equals(this.currentModeId, that.currentModeId) && java.util.Objects.equals(this.meta, that.meta);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(sessionUpdate, currentModeId, meta);
		}

		@Override
		public String toString() {
			return "CurrentModeUpdate[sessionUpdate=" + sessionUpdate + ", currentModeId=" + currentModeId + ", meta=" + meta + "]";
		}

	}

	// ---------------------------
	// Tool Call Types
	// ---------------------------

	/**
	 * Tool call content
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
	@JsonSubTypes({ @JsonSubTypes.Type(value = ToolCallContentBlock.class, name = "content"),
			@JsonSubTypes.Type(value = ToolCallDiff.class, name = "diff"),
			@JsonSubTypes.Type(value = ToolCallTerminal.class, name = "terminal") })
	public interface ToolCallContent {

	}

	/**
	 * Tool call content block
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallContentBlock implements ToolCallContent {

		private final String type;
		private final ContentBlock content;

		public ToolCallContentBlock(@JsonProperty("type") String type, @JsonProperty("content") ContentBlock content) {
			this.type = type;
			this.content = content;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("content")
		public ContentBlock content() {
			return this.content;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallContentBlock that = (ToolCallContentBlock) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.content, that.content);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, content);
		}

		@Override
		public String toString() {
			return "ToolCallContentBlock[type=" + type + ", content=" + content + "]";
		}

	}

	/**
	 * Tool call diff
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallDiff implements ToolCallContent {

		private final String type;
		private final String path;
		private final String oldText;
		private final String newText;

		public ToolCallDiff(@JsonProperty("type") String type, @JsonProperty("path") String path, @JsonProperty("oldText") String oldText, @JsonProperty("newText") String newText) {
			this.type = type;
			this.path = path;
			this.oldText = oldText;
			this.newText = newText;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("path")
		public String path() {
			return this.path;
		}

		@JsonProperty("oldText")
		public String oldText() {
			return this.oldText;
		}

		@JsonProperty("newText")
		public String newText() {
			return this.newText;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallDiff that = (ToolCallDiff) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.path, that.path) && java.util.Objects.equals(this.oldText, that.oldText) && java.util.Objects.equals(this.newText, that.newText);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, path, oldText, newText);
		}

		@Override
		public String toString() {
			return "ToolCallDiff[type=" + type + ", path=" + path + ", oldText=" + oldText + ", newText=" + newText + "]";
		}

	}

	/**
	 * Tool call terminal
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallTerminal implements ToolCallContent {

		private final String type;
		private final String terminalId;

		public ToolCallTerminal(@JsonProperty("type") String type, @JsonProperty("terminalId") String terminalId) {
			this.type = type;
			this.terminalId = terminalId;
		}

		@JsonProperty("type")
		public String type() {
			return this.type;
		}

		@JsonProperty("terminalId")
		public String terminalId() {
			return this.terminalId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallTerminal that = (ToolCallTerminal) o;
			return java.util.Objects.equals(this.type, that.type) && java.util.Objects.equals(this.terminalId, that.terminalId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(type, terminalId);
		}

		@Override
		public String toString() {
			return "ToolCallTerminal[type=" + type + ", terminalId=" + terminalId + "]";
		}

	}

	/**
	 * Tool call location
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class ToolCallLocation {

		private final String path;
		private final Integer line;

		public ToolCallLocation(@JsonProperty("path") String path, @JsonProperty("line") Integer line) {
			this.path = path;
			this.line = line;
		}

		@JsonProperty("path")
		public String path() {
			return this.path;
		}

		@JsonProperty("line")
		public Integer line() {
			return this.line;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ToolCallLocation that = (ToolCallLocation) o;
			return java.util.Objects.equals(this.path, that.path) && java.util.Objects.equals(this.line, that.line);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(path, line);
		}

		@Override
		public String toString() {
			return "ToolCallLocation[path=" + path + ", line=" + line + "]";
		}

	}

	// ---------------------------
	// Enums
	// ---------------------------

	public enum StopReason {

		@JsonProperty("end_turn")
		END_TURN, @JsonProperty("max_tokens")
		MAX_TOKENS, @JsonProperty("max_turn_requests")
		MAX_TURN_REQUESTS, @JsonProperty("refusal")
		REFUSAL, @JsonProperty("cancelled")
		CANCELLED

	}

	public enum ToolCallStatus {

		@JsonProperty("pending")
		PENDING, @JsonProperty("in_progress")
		IN_PROGRESS, @JsonProperty("completed")
		COMPLETED, @JsonProperty("failed")
		FAILED

	}

	public enum ToolKind {

		@JsonProperty("read")
		READ, @JsonProperty("edit")
		EDIT, @JsonProperty("delete")
		DELETE, @JsonProperty("move")
		MOVE, @JsonProperty("search")
		SEARCH, @JsonProperty("execute")
		EXECUTE, @JsonProperty("think")
		THINK, @JsonProperty("fetch")
		FETCH, @JsonProperty("switch_mode")
		SWITCH_MODE, @JsonProperty("other")
		OTHER

	}

	public enum Role {

		@JsonProperty("assistant")
		ASSISTANT, @JsonProperty("user")
		USER

	}

	public enum PermissionOptionKind {

		@JsonProperty("allow_once")
		ALLOW_ONCE, @JsonProperty("allow_always")
		ALLOW_ALWAYS, @JsonProperty("reject_once")
		REJECT_ONCE, @JsonProperty("reject_always")
		REJECT_ALWAYS

	}

	public enum PlanEntryStatus {

		@JsonProperty("pending")
		PENDING, @JsonProperty("in_progress")
		IN_PROGRESS, @JsonProperty("completed")
		COMPLETED

	}

	public enum PlanEntryPriority {

		@JsonProperty("high")
		HIGH, @JsonProperty("medium")
		MEDIUM, @JsonProperty("low")
		LOW

	}

	// ---------------------------
	// Supporting Types
	// ---------------------------

	/**
	 * MCP server configuration.
	 * <p>
	 * Per the ACP spec:
	 * <ul>
	 * <li>Stdio transport: NO type field (default)</li>
	 * <li>HTTP transport: type="http"</li>
	 * <li>SSE transport: type="sse"</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Uses {@code EXISTING_PROPERTY} so that:
	 * <ul>
	 * <li>McpServerStdio (no type method) serializes WITHOUT type field</li>
	 * <li>McpServerHttp/Sse (with type method) serialize WITH type field</li>
	 * </ul>
	 * </p>
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY,
			defaultImpl = McpServerStdio.class)
	@JsonSubTypes({ @JsonSubTypes.Type(value = McpServerHttp.class, name = "http"),
			@JsonSubTypes.Type(value = McpServerSse.class, name = "sse") })
	public interface McpServer {

	}

	/**
	 * STDIO MCP server (default transport, no type field in JSON).
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class McpServerStdio implements McpServer {

		private final String name;
		private final String command;
		private final List<String> args;
		private final List<EnvVariable> env;

		public McpServerStdio(@JsonProperty("name") String name, @JsonProperty("command") String command, @JsonProperty("args") List<String> args, @JsonProperty("env") List<EnvVariable> env) {
			this.name = name;
			this.command = command;
			this.args = args;
			this.env = env;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("command")
		public String command() {
			return this.command;
		}

		@JsonProperty("args")
		public List<String> args() {
			return this.args;
		}

		@JsonProperty("env")
		public List<EnvVariable> env() {
			return this.env;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			McpServerStdio that = (McpServerStdio) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.command, that.command) && java.util.Objects.equals(this.args, that.args) && java.util.Objects.equals(this.env, that.env);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, command, args, env);
		}

		@Override
		public String toString() {
			return "McpServerStdio[name=" + name + ", command=" + command + ", args=" + args + ", env=" + env + "]";
		}

	}

	/**
	 * HTTP MCP server.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class McpServerHttp implements McpServer {

		private final String name;
		private final String url;
		private final List<HttpHeader> headers;

		public McpServerHttp(@JsonProperty("name") String name, @JsonProperty("url") String url, @JsonProperty("headers") List<HttpHeader> headers) {
			this.name = name;
			this.url = url;
			this.headers = headers;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("url")
		public String url() {
			return this.url;
		}

		@JsonProperty("headers")
		public List<HttpHeader> headers() {
			return this.headers;
		}

/**
		 * Returns the transport type identifier.
		 */
		@JsonProperty("type")
		public String type() {
			return "http";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			McpServerHttp that = (McpServerHttp) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.url, that.url) && java.util.Objects.equals(this.headers, that.headers);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, url, headers);
		}

		@Override
		public String toString() {
			return "McpServerHttp[name=" + name + ", url=" + url + ", headers=" + headers + "]";
		}

	}

	/**
	 * SSE MCP server.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class McpServerSse implements McpServer {

		private final String name;
		private final String url;
		private final List<HttpHeader> headers;

		public McpServerSse(@JsonProperty("name") String name, @JsonProperty("url") String url, @JsonProperty("headers") List<HttpHeader> headers) {
			this.name = name;
			this.url = url;
			this.headers = headers;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("url")
		public String url() {
			return this.url;
		}

		@JsonProperty("headers")
		public List<HttpHeader> headers() {
			return this.headers;
		}

/**
		 * Returns the transport type identifier.
		 */
		@JsonProperty("type")
		public String type() {
			return "sse";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			McpServerSse that = (McpServerSse) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.url, that.url) && java.util.Objects.equals(this.headers, that.headers);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, url, headers);
		}

		@Override
		public String toString() {
			return "McpServerSse[name=" + name + ", url=" + url + ", headers=" + headers + "]";
		}

	}

	/**
	 * Environment variable
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class EnvVariable {

		private final String name;
		private final String value;

		public EnvVariable(@JsonProperty("name") String name, @JsonProperty("value") String value) {
			this.name = name;
			this.value = value;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("value")
		public String value() {
			return this.value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			EnvVariable that = (EnvVariable) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.value, that.value);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, value);
		}

		@Override
		public String toString() {
			return "EnvVariable[name=" + name + ", value=" + value + "]";
		}

	}

	/**
	 * HTTP header
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class HttpHeader {

		private final String name;
		private final String value;

		public HttpHeader(@JsonProperty("name") String name, @JsonProperty("value") String value) {
			this.name = name;
			this.value = value;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("value")
		public String value() {
			return this.value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			HttpHeader that = (HttpHeader) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.value, that.value);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, value);
		}

		@Override
		public String toString() {
			return "HttpHeader[name=" + name + ", value=" + value + "]";
		}

	}

	/**
	 * Terminal exit status
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class TerminalExitStatus {

		private final Integer exitCode;
		private final String signal;

		public TerminalExitStatus(@JsonProperty("exitCode") Integer exitCode, @JsonProperty("signal") String signal) {
			this.exitCode = exitCode;
			this.signal = signal;
		}

		@JsonProperty("exitCode")
		public Integer exitCode() {
			return this.exitCode;
		}

		@JsonProperty("signal")
		public String signal() {
			return this.signal;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TerminalExitStatus that = (TerminalExitStatus) o;
			return java.util.Objects.equals(this.exitCode, that.exitCode) && java.util.Objects.equals(this.signal, that.signal);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(exitCode, signal);
		}

		@Override
		public String toString() {
			return "TerminalExitStatus[exitCode=" + exitCode + ", signal=" + signal + "]";
		}

	}

	/**
	 * Authentication method
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AuthMethod {

		private final String id;
		private final String name;
		private final String description;

		public AuthMethod(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("description") String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}

		@JsonProperty("id")
		public String id() {
			return this.id;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("description")
		public String description() {
			return this.description;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AuthMethod that = (AuthMethod) o;
			return java.util.Objects.equals(this.id, that.id) && java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.description, that.description);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(id, name, description);
		}

		@Override
		public String toString() {
			return "AuthMethod[id=" + id + ", name=" + name + ", description=" + description + "]";
		}

	}

	/**
	 * Permission option
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PermissionOption {

		private final String optionId;
		private final String name;
		private final PermissionOptionKind kind;

		public PermissionOption(@JsonProperty("optionId") String optionId, @JsonProperty("name") String name, @JsonProperty("kind") PermissionOptionKind kind) {
			this.optionId = optionId;
			this.name = name;
			this.kind = kind;
		}

		@JsonProperty("optionId")
		public String optionId() {
			return this.optionId;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("kind")
		public PermissionOptionKind kind() {
			return this.kind;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PermissionOption that = (PermissionOption) o;
			return java.util.Objects.equals(this.optionId, that.optionId) && java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.kind, that.kind);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(optionId, name, kind);
		}

		@Override
		public String toString() {
			return "PermissionOption[optionId=" + optionId + ", name=" + name + ", kind=" + kind + "]";
		}

	}

	/**
	 * Request permission outcome
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "outcome")
	@JsonSubTypes({ @JsonSubTypes.Type(value = PermissionCancelled.class, name = "cancelled"),
			@JsonSubTypes.Type(value = PermissionSelected.class, name = "selected") })
	public interface RequestPermissionOutcome {

	}

	/**
	 * Permission cancelled
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PermissionCancelled implements RequestPermissionOutcome {

		private final String outcome;

		public PermissionCancelled(@JsonProperty("outcome") String outcome) {
			this.outcome = outcome;
		}

		@JsonProperty("outcome")
		public String outcome() {
			return this.outcome;
		}

public PermissionCancelled() {
			this("cancelled");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PermissionCancelled that = (PermissionCancelled) o;
			return java.util.Objects.equals(this.outcome, that.outcome);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(outcome);
		}

		@Override
		public String toString() {
			return "PermissionCancelled[outcome=" + outcome + "]";
		}

	}

	/**
	 * Permission selected
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PermissionSelected implements RequestPermissionOutcome {

		private final String outcome;
		private final String optionId;

		public PermissionSelected(@JsonProperty("outcome") String outcome, @JsonProperty("optionId") String optionId) {
			this.outcome = outcome;
			this.optionId = optionId;
		}

		@JsonProperty("outcome")
		public String outcome() {
			return this.outcome;
		}

		@JsonProperty("optionId")
		public String optionId() {
			return this.optionId;
		}

public PermissionSelected(String optionId) {
			this("selected", optionId);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PermissionSelected that = (PermissionSelected) o;
			return java.util.Objects.equals(this.outcome, that.outcome) && java.util.Objects.equals(this.optionId, that.optionId);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(outcome, optionId);
		}

		@Override
		public String toString() {
			return "PermissionSelected[outcome=" + outcome + ", optionId=" + optionId + "]";
		}

	}

	/**
	 * Plan entry
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PlanEntry {

		private final String content;
		private final PlanEntryPriority priority;
		private final PlanEntryStatus status;

		public PlanEntry(@JsonProperty("content") String content, @JsonProperty("priority") PlanEntryPriority priority, @JsonProperty("status") PlanEntryStatus status) {
			this.content = content;
			this.priority = priority;
			this.status = status;
		}

		@JsonProperty("content")
		public String content() {
			return this.content;
		}

		@JsonProperty("priority")
		public PlanEntryPriority priority() {
			return this.priority;
		}

		@JsonProperty("status")
		public PlanEntryStatus status() {
			return this.status;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PlanEntry that = (PlanEntry) o;
			return java.util.Objects.equals(this.content, that.content) && java.util.Objects.equals(this.priority, that.priority) && java.util.Objects.equals(this.status, that.status);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(content, priority, status);
		}

		@Override
		public String toString() {
			return "PlanEntry[content=" + content + ", priority=" + priority + ", status=" + status + "]";
		}

	}

	/**
	 * Available command
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AvailableCommand {

		private final String name;
		private final String description;
		private final AvailableCommandInput input;

		public AvailableCommand(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("input") AvailableCommandInput input) {
			this.name = name;
			this.description = description;
			this.input = input;
		}

		@JsonProperty("name")
		public String name() {
			return this.name;
		}

		@JsonProperty("description")
		public String description() {
			return this.description;
		}

		@JsonProperty("input")
		public AvailableCommandInput input() {
			return this.input;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AvailableCommand that = (AvailableCommand) o;
			return java.util.Objects.equals(this.name, that.name) && java.util.Objects.equals(this.description, that.description) && java.util.Objects.equals(this.input, that.input);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(name, description, input);
		}

		@Override
		public String toString() {
			return "AvailableCommand[name=" + name + ", description=" + description + ", input=" + input + "]";
		}

	}

	/**
	 * Available command input
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class AvailableCommandInput {

		private final String hint;

		public AvailableCommandInput(@JsonProperty("hint") String hint) {
			this.hint = hint;
		}

		@JsonProperty("hint")
		public String hint() {
			return this.hint;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AvailableCommandInput that = (AvailableCommandInput) o;
			return java.util.Objects.equals(this.hint, that.hint);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(hint);
		}

		@Override
		public String toString() {
			return "AvailableCommandInput[hint=" + hint + "]";
		}

	}

}
