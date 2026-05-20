package com.enterpriseresourceplanning.api.testcases;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.enterpriseresourceplanning.api.base.BaseAPI;
import com.enterpriseresourceplanning.api.endpoints.Routes;
import com.enterpriseresourceplanning.api.utilities.APIUtils;
import com.enterpriseresourceplanning.api.validators.ResponseValidator;
import com.enterpriseresourceplanning.listeners.TestListener;

import io.restassured.response.Response;

/**
 * DELETE API test example.
 */
@Listeners(TestListener.class)
public class UserDELETETest extends BaseAPI {

	@Test(description = "DELETE /users/1 - delete user")
	public void deleteUser_shouldReturn200() {
		Response response = APIUtils.delete(Routes.userById(1));

		ResponseValidator.validateStatusCode(response, 200);
		ResponseValidator.validateDefaultResponseTime(response);
	}

}
