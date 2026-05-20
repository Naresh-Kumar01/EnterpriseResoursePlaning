package com.enterpriseresourceplanning.listeners;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.enterpriseresourceplanning.api.utilities.ConfigManager;

/**
 * Unified Extent Reports manager for API (and optional UI) test execution.
 */
public final class ExtentReportManager {

	private static ExtentReports extent;
	private static final ThreadLocal<ExtentTest> EXTENT_TEST = new ThreadLocal<>();

	private ExtentReportManager() {
	}

	public static synchronized ExtentReports getInstance() {
		if (extent == null) {
			ConfigManager.initialize();
			String path = ConfigManager.get("extent.report.path", "test-output/extent-reports");
			String name = ConfigManager.get("extent.report.name", "Automation_Report");
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

			ExtentSparkReporter spark = new ExtentSparkReporter(path + "/" + name + "_" + timestamp + ".html");
			spark.config().setTheme(Theme.STANDARD);
			spark.config().setDocumentTitle("Enterprise Automation Report");
			spark.config().setReportName("UI + API Automation");

			extent = new ExtentReports();
			extent.attachReporter(spark);
			extent.setSystemInfo("API Environment", ConfigManager.getActiveEnvironment());
			extent.setSystemInfo("API Base URL", ConfigManager.getBaseUrl());
		}
		return extent;
	}

	public static void startTest(String name) {
		EXTENT_TEST.set(getInstance().createTest(name));
	}

	public static ExtentTest getTest() {
		return EXTENT_TEST.get();
	}

	public static void attachRequest(String log) {
		ExtentTest test = getTest();
		if (test != null) {
			test.info("Request");
			test.info(MarkupHelper.createCodeBlock(log));
		}
	}

	public static void attachResponse(String log) {
		ExtentTest test = getTest();
		if (test != null) {
			test.info("Response");
			test.info(MarkupHelper.createCodeBlock(log));
		}
	}

	public static void pass(String message) {
		ExtentTest test = getTest();
		if (test != null) {
			test.pass(message);
		}
	}

	public static void fail(String message) {
		ExtentTest test = getTest();
		if (test != null) {
			test.fail(message);
		}
	}

	public static void endTest() {
		EXTENT_TEST.remove();
	}

	public static void flush() {
		if (extent != null) {
			extent.flush();
		}
	}

}
