package com.enterpriseresourceplanning.api.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads environment-specific API configuration (QA / UAT / PROD).
 */
public final class ConfigManager {

	private static final Logger LOG = LogManager.getLogger(ConfigManager.class);
	private static Properties config;
	private static String activeEnvironment;

	private ConfigManager() {
	}

	public static synchronized void initialize() {
		if (config != null) {
			return;
		}
		config = loadClasspathProperties("config.properties");
		activeEnvironment = resolveEnvironment();
		String envFile = "config/" + activeEnvironment + ".properties";
		Properties envProps = loadClasspathProperties(envFile);
		config.putAll(envProps);
		applySystemOverrides();
		LOG.info("API Config initialized | env={} | baseUrl={}", activeEnvironment, getBaseUrl());
	}

	public static String getActiveEnvironment() {
		initialize();
		return activeEnvironment;
	}

	public static String getBaseUrl() {
		initialize();
		String url = config.getProperty("base.url");
		if (url == null || url.isBlank()) {
			url = config.getProperty("api.base.url");
		}
		if (url == null || url.isBlank()) {
			throw new IllegalStateException("base.url not configured for environment: " + activeEnvironment);
		}
		return url.trim();
	}

	public static String get(String key) {
		initialize();
		return config.getProperty(key);
	}

	public static String get(String key, String defaultValue) {
		initialize();
		return config.getProperty(key, defaultValue);
	}

	public static int getInt(String key, int defaultValue) {
		initialize();
		String value = config.getProperty(key);
		return value == null ? defaultValue : Integer.parseInt(value);
	}

	public static Properties getConfig() {
		initialize();
		return config;
	}

	private static String resolveEnvironment() {
		String sys = System.getProperty("api.environment");
		if (sys != null && !sys.isBlank()) {
			return sys.trim().toLowerCase();
		}
		String env = System.getenv("API_ENVIRONMENT");
		if (env != null && !env.isBlank()) {
			return env.trim().toLowerCase();
		}
		Properties base = loadClasspathProperties("config.properties");
		return base.getProperty("api.environment", "qa").trim().toLowerCase();
	}

	private static Properties loadClasspathProperties(String resource) {
		Properties properties = new Properties();
		try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
			if (stream == null) {
				if ("config.properties".equals(resource)) {
					throw new IllegalStateException("config.properties not found on classpath");
				}
				LOG.warn("Optional config file not found: {}", resource);
				return properties;
			}
			properties.load(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load " + resource, e);
		}
		return properties;
	}

	private static void applySystemOverrides() {
		override("base.url");
		override("api.auth.token");
		override("api.max.response.time.ms");
	}

	private static void override(String key) {
		String value = System.getProperty(key);
		if (value != null && !value.isBlank()) {
			config.setProperty(key, value);
		}
	}

}
