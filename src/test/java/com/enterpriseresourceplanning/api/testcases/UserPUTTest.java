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
 * PUT API test example for updating an existing user.
 */
@Listeners(TestListener.class)
public class UserPUTTest extends BaseAPI {

	@Test(description = "PUT /users/1 - update user")
	public void updateUser_shouldReturn200() {
		var node = JsonUtility.readTree("testdata/users.json").path("updateUser");
		User payload = UserPayload.updateUser(
				node.path("id").asInt(1),
				node.path("name").asText("Updated ERP User"),
				node.path("email").asText("updated@logixerp.com"));

		Response response = APIUtils.put(Routes.userById(payload.getId()), payload);

		ResponseValidator.validateStatusCode(response, 200);
		ResponseValidator.validateDefaultResponseTime(response);
		ResponseValidator.validateBodyField(response, "id", payload.getId());
		ResponseValidator.validateBodyField(response, "name", payload.getName());
	}

}
