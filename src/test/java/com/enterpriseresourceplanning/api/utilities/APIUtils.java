package com.enterpriseresourceplanning.api.utilities;

import java.util.Map;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Reusable REST Assured HTTP client for CRUD operations.
 */
public final class APIUtils {

	private APIUtils() {
	}

	public static Response get(String path) {
		return given().spec(RequestSpecBuilder.build()).when().get(path);
	}

	public static Response get(String path, Map<String, ?> queryParams) {
		return given()
				.spec(RequestSpecBuilder.build())
				.queryParams(queryParams)
				.when()
				.get(path);
	}

	public static Response post(String path, Object body) {
		return given()
				.spec(RequestSpecBuilder.build())
				.body(body)
				.when()
				.post(path);
	}

	public static Response put(String path, Object body) {
		return given()
				.spec(RequestSpecBuilder.build())
				.body(body)
				.when()
				.put(path);
	}

	public static Response patch(String path, Object body) {
		return given()
				.spec(RequestSpecBuilder.build())
				.body(body)
				.when()
				.patch(path);
	}

	public static Response delete(String path) {
		return given()
				.spec(RequestSpecBuilder.build())
				.when()
				.delete(path);
	}

	public static Response execute(String method, String path, Object body, RequestSpecification customSpec) {
		RequestSpecification spec = customSpec != null ? customSpec : RequestSpecBuilder.build();
		var request = given().spec(spec);
		if (body != null) {
			request = request.body(body);
		}
		return switch (method.toUpperCase()) {
			case "GET" -> request.when().get(path);
			case "POST" -> request.when().post(path);
			case "PUT" -> request.when().put(path);
			case "PATCH" -> request.when().patch(path);
			case "DELETE" -> request.when().delete(path);
			default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		};
	}

}
