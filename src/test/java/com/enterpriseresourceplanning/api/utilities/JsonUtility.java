package com.enterpriseresourceplanning.api.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON test data reader/writer utility for API data-driven tests.
 */
public final class JsonUtility {

	private static final Logger LOG = LogManager.getLogger(JsonUtility.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private JsonUtility() {
	}

	public static JsonNode readTree(String classpathResource) {
		try (InputStream stream = JsonUtility.class.getClassLoader().getResourceAsStream(classpathResource)) {
			if (stream == null) {
				throw new IllegalArgumentException("JSON not found: " + classpathResource);
			}
			return MAPPER.readTree(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read JSON: " + classpathResource, e);
		}
	}

	public static <T> T readAs(String classpathResource, Class<T> type) {
		try (InputStream stream = JsonUtility.class.getClassLoader().getResourceAsStream(classpathResource)) {
			if (stream == null) {
				throw new IllegalArgumentException("JSON not found: " + classpathResource);
			}
			T value = MAPPER.readValue(stream, type);
			LOG.debug("Loaded JSON as {} from {}", type.getSimpleName(), classpathResource);
			return value;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to deserialize JSON: " + classpathResource, e);
		}
	}

	public static String readRaw(String classpathResource) {
		try (InputStream stream = JsonUtility.class.getClassLoader().getResourceAsStream(classpathResource)) {
			if (stream == null) {
				throw new IllegalArgumentException("JSON not found: " + classpathResource);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read JSON: " + classpathResource, e);
		}
	}

	public static String toJson(Object object) {
		try {
			return MAPPER.writeValueAsString(object);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to serialize object to JSON", e);
		}
	}

}
