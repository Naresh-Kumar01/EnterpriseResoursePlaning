package com.enterpriseresourceplanning.api.testcases;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.enterpriseresourceplanning.api.base.BaseAPI;
import com.enterpriseresourceplanning.api.endpoints.Routes;
import com.enterpriseresourceplanning.api.utilities.APIUtils;
import com.enterpriseresourceplanning.api.utilities.ExcelUtility;
import com.enterpriseresourceplanning.api.validators.ResponseValidator;
import com.enterpriseresourceplanning.api.validators.SchemaValidator;
import com.enterpriseresourceplanning.listeners.TestListener;

import io.restassured.response.Response;

/**
 * GET API test examples with status, schema, header, and response-time validation.
 */
@Listeners(TestListener.class)
public class UserGETTest extends BaseAPI {

	@Test(description = "GET /users - list all users")
	public void getAllUsers_shouldReturn200() {
		Response response = APIUtils.get(Routes.USERS);

		ResponseValidator.validateStatusCode(response, 200);
		ResponseValidator.validateDefaultResponseTime(response);
		ResponseValidator.validateHeaderExists(response, "Content-Type");
		SchemaValidator.validate(response, "schemas/users-list-schema.json");
	}

	@Test(description = "GET /users/{id} - Excel-driven validation")
	public void getUserById_excelDriven() {
		var row = ExcelUtility.readSheet("testdata/users.xlsx", "GET").get(0);
		int userId = Integer.parseInt(row.get("userId"));
		int expectedStatus = Integer.parseInt(row.get("expectedStatus"));

		Response response = APIUtils.get(Routes.userById(userId));
		ResponseValidator.validateStatusCode(response, expectedStatus);
		ResponseValidator.validateBodyField(response, "id", userId);
		SchemaValidator.validate(response, "schemas/user-schema.json");
	}

	@Test(description = "GET /users/1 - validate response body fields")
	public void getUserById_shouldReturnExpectedFields() {
		Response response = APIUtils.get(Routes.userById(1));

		ResponseValidator.validateStatusCode(response, 200);
		ResponseValidator.validateBodyField(response, "id", 1);
		ResponseValidator.validateBodyNotEmpty(response, "name");
		ResponseValidator.validateBodyNotEmpty(response, "email");
	}

}
