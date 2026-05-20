package com.enterpriseresourceplanning.api.endpoints;

/**
 * Centralized API route definitions (relative paths).
 */
public final class Routes {

	private Routes() {
	}

	public static final String USERS = "/users";

	public static String userById(int id) {
		return USERS + "/" + id;
	}

}
