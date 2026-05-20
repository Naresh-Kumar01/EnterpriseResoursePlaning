package com.enterpriseresourceplanning.api.base;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.enterpriseresourceplanning.api.utilities.ConfigManager;
import com.enterpriseresourceplanning.api.utilities.RequestSpecBuilder;
import com.enterpriseresourceplanning.api.utilities.ResponseSpecBuilder;
import com.enterpriseresourceplanning.api.utilities.TokenManager;
import com.enterpriseresourceplanning.base.BaseClass;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Base setup for all API test classes: environment, specs, logging, authentication.
 */
public abstract class BaseAPI extends BaseClass {

	protected RequestSpecification requestSpec;
	protected ResponseSpecification responseSpec;

	@BeforeClass(alwaysRun = true)
	public void setUpApi() {
		ConfigManager.initialize();
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.baseURI = ConfigManager.getBaseUrl();
		requestSpec = RequestSpecBuilder.build();
		responseSpec = ResponseSpecBuilder.build();
		RestAssured.requestSpecification = requestSpec;
		logInfo("API setup complete | env=" + ConfigManager.getActiveEnvironment()
				+ " | baseUri=" + ConfigManager.getBaseUrl());
	}

	@AfterClass(alwaysRun = true)
	public void tearDownApi() {
		TokenManager.clear();
		RestAssured.reset();
		logInfo("API teardown complete");
	}

}
