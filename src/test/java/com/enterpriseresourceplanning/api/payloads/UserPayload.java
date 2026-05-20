package com.enterpriseresourceplanning.api.payloads;

import java.util.UUID;

import com.enterpriseresourceplanning.api.models.User;
import com.enterpriseresourceplanning.api.utilities.JsonUtility;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Dynamic payload generator for User API requests.
 */
public final class UserPayload {

	private UserPayload() {
	}

	public static User createUser(String name, String username, String email) {
		User user = new User();
		user.setName(name);
		user.setUsername(username);
		user.setEmail(email);
		user.setPhone("555-0100");
		user.setWebsite("logixerp.com");
		return user;
	}

	public static User fromJsonTemplate() {
		JsonNode node = JsonUtility.readTree("testdata/users.json").path("createUser");
		return createUser(
				node.path("name").asText("User " + UUID.randomUUID()),
				node.path("username").asText("user_" + System.currentTimeMillis()),
				node.path("email").asText("automation+" + System.currentTimeMillis() + "@logixerp.com"));
	}

	public static User randomUser() {
		long suffix = System.currentTimeMillis();
		return createUser(
				"Automation User " + suffix,
				"auto_user_" + suffix,
				"auto" + suffix + "@enterprise.test");
	}

	public static User updateUser(int id, String name, String email) {
		User user = createUser(name, "updated_user", email);
		user.setId(id);
		return user;
	}

}
