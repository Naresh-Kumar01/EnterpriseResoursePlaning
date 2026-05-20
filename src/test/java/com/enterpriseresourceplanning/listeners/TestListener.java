package com.enterpriseresourceplanning.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.enterpriseresourceplanning.api.utilities.ConfigManager;
import com.enterpriseresourceplanning.api.utilities.TokenManager;

/**
 * TestNG listener for API tests: Extent reporting and token cleanup.
 */
public class TestListener implements ITestListener {

	@Override
	public void onTestStart(ITestResult result) {
		ConfigManager.initialize();
		ExtentReportManager.startTest(result.getMethod().getMethodName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ExtentReportManager.pass("Passed: " + result.getName());
		cleanup();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String message = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error";
		ExtentReportManager.fail("Failed: " + result.getName() + " | " + message);
		cleanup();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ExtentReportManager.fail("Skipped: " + result.getName());
		cleanup();
	}

	@Override
	public void onFinish(ITestContext context) {
		ExtentReportManager.flush();
	}

	private void cleanup() {
		TokenManager.clear();
		ExtentReportManager.endTest();
	}

}
