package com.enterpriseresourceplanning.api.utilities;

import io.restassured.filter.log.LogDetail;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.lessThan;

/**
 * Default response expectations for API tests (content type, max response time).
 */
public final class ResponseSpecBuilder {

	private ResponseSpecBuilder() {
	}

	public static ResponseSpecification build() {
		long maxMs = ConfigManager.getInt("api.max.response.time.ms", 5000);
		return new io.restassured.builder.ResponseSpecBuilder()
				.expectContentType(io.restassured.http.ContentType.JSON)
				.expectResponseTime(lessThan(maxMs))
				.log(LogDetail.STATUS)
				.build();
	}

}
