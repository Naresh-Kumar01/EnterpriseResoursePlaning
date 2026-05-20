package com.enterpriseresourceplanning.api.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterpriseresourceplanning.listeners.ExtentReportManager;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Logs API request/response to Log4j2 and attaches to Extent Reports.
 */
public class ApiLogFilter implements Filter {

	private static final Logger LOG = LogManager.getLogger(ApiLogFilter.class);

	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		String requestLog = "Method: " + requestSpec.getMethod()
				+ "\nURI: " + requestSpec.getURI()
				+ "\nHeaders: " + requestSpec.getHeaders()
				+ "\nBody: " + requestSpec.getBody();
		LOG.info("API Request:\n{}", requestLog);
		ExtentReportManager.attachRequest(requestLog);

		Response response = ctx.next(requestSpec, responseSpec);

		String responseLog = "Status: " + response.getStatusCode()
				+ "\nTime(ms): " + response.getTime()
				+ "\nBody: " + response.getBody().asPrettyString();
		LOG.info("API Response:\n{}", responseLog);
		ExtentReportManager.attachResponse(responseLog);
		return response;
	}

}
