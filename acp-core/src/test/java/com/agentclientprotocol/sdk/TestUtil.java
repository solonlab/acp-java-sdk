/*
 * Copyright 2025-2025 the original author or authors.
 */
package com.agentclientprotocol.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test utility for Java 8 compatible Map creation (replacing TestUtil.mapOf()).
 */
public final class TestUtil {

	private TestUtil() {
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf() {
		return Collections.emptyMap();
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1) {
		return Collections.singletonMap(k1, v1);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
		HashMap<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		return Collections.unmodifiableMap(map);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
		HashMap<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		return Collections.unmodifiableMap(map);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
		HashMap<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		return Collections.unmodifiableMap(map);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
		HashMap<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		return Collections.unmodifiableMap(map);
	}
}
