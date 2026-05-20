package com.enterpriseresourceplanning.api.validators;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterpriseresourceplanning.api.utilities.ConfigManager;

import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Reusable response validations: status, body, headers, response time.
 */
public final class ResponseValidator {

	private static final Logger LOG = LogManager.getLogger(ResponseValidator.class);

	private ResponseValidator() {
	}

	public static void validateStatusCode(Response response, int expected) {
		int actual = response.getStatusCode();
		LOG.info("Status validation | expected={} | actual={}", expected, actual);
		assertThat("Unexpected status code", actual, equalTo(expected));
	}

	public static void validateBodyField(Response response, String jsonPath, Object expected) {
		Object actual = response.jsonPath().get(jsonPath);
		LOG.info("Body field validation | path={} | expected={} | actual={}", jsonPath, expected, actual);
		assertThat("Mismatch at " + jsonPath, actual, equalTo(expected));
	}

	public static void validateBodyContains(Response response, String jsonPath, String expectedSubstring) {
		String actual = response.jsonPath().getString(jsonPath);
		LOG.info("Body contains validation | path={}", jsonPath);
		assertThat(actual, containsString(expectedSubstring));
	}

	public static void validateBodyNotEmpty(Response response, String jsonPath) {
		Object actual = response.jsonPath().get(jsonPath);
		assertThat("Expected non-empty value at " + jsonPath, actual, notNullValue());
	}

	public static void validateHeader(Response response, String headerName, String expectedValue) {
		String actual = response.getHeader(headerName);
		LOG.info("Header validation | {} | expected={} | actual={}", headerName, expectedValue, actual);
		assertThat("Header mismatch: " + headerName, actual, equalTo(expectedValue));
	}

	public static void validateHeaderExists(Response response, String headerName) {
		String actual = response.getHeader(headerName);
		LOG.info("Header exists validation | {}", headerName);
		assertThat("Missing header: " + headerName, actual, notNullValue());
	}

	public static void validateResponseTime(Response response, long maxMs) {
		long actual = response.getTimeIn(TimeUnit.MILLISECONDS);
		LOG.info("Response time validation | max={}ms | actual={}ms", maxMs, actual);
		assertThat("Response time exceeded", actual, lessThan(maxMs));
	}

	public static void validateDefaultResponseTime(Response response) {
		validateResponseTime(response, ConfigManager.getInt("api.max.response.time.ms", 5000));
	}

}
