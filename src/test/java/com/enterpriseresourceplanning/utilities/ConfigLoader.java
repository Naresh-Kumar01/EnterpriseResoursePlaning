package com.enterpriseresourceplanning.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config.properties and applies CI/CD overrides from system properties and environment variables.
 */
public final class ConfigLoader {

	private ConfigLoader() {
	}

	public static Properties load() {
		Properties config = new Properties();
		try (InputStream stream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (stream == null) {
				throw new IllegalStateException("config.properties not found on classpath");
			}
			config.load(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load config.properties", e);
		}

		applyOverride(config, "environment");
		applyOverride(config, "browser");
		applyOverride(config, "headless");
		applyOverride(config, "selenium.grid.enabled");
		applyOverride(config, "selenium.grid.url");
		applyOverride(config, "valid.username");
		applyOverride(config, "valid.password");
		applyOverride(config, "dev.base.url");

		applyEnvOverride(config, "SELENIUM_GRID_ENABLED", "selenium.grid.enabled");
		applyEnvOverride(config, "SELENIUM_GRID_URL", "selenium.grid.url");
		applyEnvOverride(config, "HEADLESS", "headless");

		return config;
	}

	private static void applyOverride(Properties config, String key) {
		String value = System.getProperty(key);
		if (value != null && !value.isBlank()) {
			config.setProperty(key, value);
		}
	}

	private static void applyEnvOverride(Properties config, String envKey, String configKey) {
		String value = System.getenv(envKey);
		if (value != null && !value.isBlank()) {
			config.setProperty(configKey, value);
		}
	}

}
