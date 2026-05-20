package com.enterpriseresourceplanning.api.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * JSON Schema validation utility for API responses.
 */
public final class SchemaValidator {

	private static final Logger LOG = LogManager.getLogger(SchemaValidator.class);

	private SchemaValidator() {
	}

	public static void validate(Response response, String schemaClasspath) {
		LOG.info("JSON schema validation | schema={}", schemaClasspath);
		response.then().body(matchesJsonSchemaInClasspath(schemaClasspath));
	}

}
