package com.enterpriseresourceplanning.api.utilities;

import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds reusable REST Assured request specifications (headers, auth, logging).
 */
public final class RequestSpecBuilder {

	private RequestSpecBuilder() {
	}

	public static RequestSpecification build() {
		io.restassured.builder.RequestSpecBuilder builder = new io.restassured.builder.RequestSpecBuilder()
				.setBaseUri(ConfigManager.getBaseUrl())
				.setContentType(ContentType.JSON)
				.setAccept(ContentType.JSON)
				.addFilter(new ApiLogFilter())
				.log(LogDetail.URI);

		String token = TokenManager.getBearerToken();
		if (token != null && !token.isBlank()) {
			builder.addHeader("Authorization", "Bearer " + token);
		}

		String apiKey = TokenManager.getApiKey();
		if (apiKey != null && !apiKey.isBlank()) {
			builder.addHeader("x-api-key", apiKey);
		}

		String configuredToken = ConfigManager.get("api.auth.token");
		if ((token == null || token.isBlank()) && configuredToken != null && !configuredToken.isBlank()) {
			builder.addHeader("Authorization", "Bearer " + configuredToken);
		}

		return builder.build();
	}

}
