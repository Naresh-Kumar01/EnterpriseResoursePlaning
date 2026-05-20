package com.enterpriseresourceplanning.api.testcases;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.enterpriseresourceplanning.api.base.BaseAPI;
import com.enterpriseresourceplanning.api.endpoints.Routes;
import com.enterpriseresourceplanning.api.models.User;
import com.enterpriseresourceplanning.api.payloads.UserPayload;
import com.enterpriseresourceplanning.api.utilities.APIUtils;
import com.enterpriseresourceplanning.api.utilities.JsonUtility;
import com.enterpriseresourceplanning.api.validators.ResponseValidator;
import com.enterpriseresourceplanning.listeners.TestListener;

import io.restassured.response.Response;

/**
 * POST API test examples with dynamic and JSON-driven payloads.
 */
@Listeners(TestListener.class)
public class UserPOSTTest extends BaseAPI {

	@Test(description = "POST /users - create user from JSON template")
	public void createUser_fromJsonTemplate_shouldReturn201() {
		User payload = UserPayload.fromJsonTemplate();
		Response response = APIUtils.post(Routes.USERS, payload);

		ResponseValidator.validateStatusCode(response, 201);
		ResponseValidator.validateDefaultResponseTime(response);
		ResponseValidator.validateBodyField(response, "name", payload.getName());
		ResponseValidator.validateBodyField(response, "email", payload.getEmail());
	}

	@Test(description = "POST /users - create user with dynamic payload")
	public void createUser_randomPayload_shouldReturn201() {
		User payload = UserPayload.randomUser();
		Response response = APIUtils.post(Routes.USERS, payload);

		ResponseValidator.validateStatusCode(response, 201);
		ResponseValidator.validateBodyContains(response, "username", payload.getUsername());
	}

	@Test(description = "POST /users - JSON test data driven")
	public void createUser_fromUsersJson() {
		var node = JsonUtility.readTree("testdata/users.json").path("createUser");
		User user = UserPayload.createUser(
				node.path("name").asText(),
				node.path("username").asText(),
				node.path("email").asText());

		Response response = APIUtils.post(Routes.USERS, user);
		ResponseValidator.validateStatusCode(response, 201);
	}

}
