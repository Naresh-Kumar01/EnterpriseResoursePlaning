package com.enterpriseresourceplanning.api.utilities;

/**
 * Thread-safe storage for Bearer tokens and API keys reused across API tests.
 */
public final class TokenManager {

	private static final ThreadLocal<String> BEARER_TOKEN = new ThreadLocal<>();
	private static final ThreadLocal<String> API_KEY = new ThreadLocal<>();

	private TokenManager() {
	}

	public static void setBearerToken(String token) {
		BEARER_TOKEN.set(token);
	}

	public static String getBearerToken() {
		return BEARER_TOKEN.get();
	}

	public static void setApiKey(String apiKey) {
		API_KEY.set(apiKey);
	}

	public static String getApiKey() {
		return API_KEY.get();
	}

	public static void clear() {
		BEARER_TOKEN.remove();
		API_KEY.remove();
	}

}
