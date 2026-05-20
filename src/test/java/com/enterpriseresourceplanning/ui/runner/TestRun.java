package com.enterpriseresourceplanning.ui.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Cucumber + JUnit runner for UI BDD tests.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",
		glue = "com.enterpriseresourceplanning.ui.testCases",
		plugin = {
				"pretty",
				"html:target/cucumber-reports/cucumber.html",
				"json:target/cucumber-reports/cucumber.json"
		},
		monochrome = true
)
public class TestRun {

}
