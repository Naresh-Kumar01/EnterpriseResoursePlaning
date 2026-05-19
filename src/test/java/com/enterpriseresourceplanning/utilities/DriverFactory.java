package com.enterpriseresourceplanning.utilities;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Creates WebDriver for local execution or Selenium Grid (Docker/Jenkins CI).
 */
public final class DriverFactory {

	private static final Logger LOG = Logger.getLogger(DriverFactory.class.getName());

	private DriverFactory() {
	}

	public static WebDriver create(Properties config) {
		ChromeOptions options = buildChromeOptions(config);

		if (isGridEnabled(config)) {
			return createRemoteDriver(config, options);
		}

		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver(options);
		LOG.info("Local ChromeDriver started");
		return driver;
	}

	private static WebDriver createRemoteDriver(Properties config, ChromeOptions options) {
		String gridUrl = config.getProperty("selenium.grid.url", "http://localhost:4444/wd/hub");
		GridConnectionHelper.ensureGridReady(gridUrl);
		try {
			URL hubUrl = URI.create(gridUrl).toURL();
			RemoteWebDriver driver = new RemoteWebDriver(hubUrl, options);
			driver.manage().timeouts().implicitlyWait(
					Duration.ofSeconds(Integer.parseInt(config.getProperty("implicit.wait", "10"))));
			LOG.info("RemoteWebDriver connected to Selenium Grid: " + gridUrl);
			return driver;
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to create RemoteWebDriver session at " + gridUrl
							+ ". Ensure Docker Selenium Grid is running (docker compose up -d).",
					e);
		}
	}

	private static ChromeOptions buildChromeOptions(Properties config) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--window-size=1920,1080");

		if (Boolean.parseBoolean(config.getProperty("headless", "false"))) {
			options.addArguments("--headless=new");
		}

		options.addArguments("--disable-autofill");
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("autofill.profile_enabled", false);
		options.setExperimentalOption("prefs", prefs);

		return options;
	}

	private static boolean isGridEnabled(Properties config) {
		return Boolean.parseBoolean(config.getProperty("selenium.grid.enabled", "false"));
	}

}
