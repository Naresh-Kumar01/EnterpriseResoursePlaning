package com.enterpriseresourceplanning.utilities;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Checks Selenium Grid availability before RemoteWebDriver connects.
 */
public final class GridConnectionHelper {

	private GridConnectionHelper() {
	}

	public static void ensureGridReady(String gridUrl) {
		String statusUrl = toStatusUrl(gridUrl);
		int maxAttempts = Integer.parseInt(System.getProperty("grid.wait.attempts", "12"));
		int sleepMs = Integer.parseInt(System.getProperty("grid.wait.sleep.ms", "5000"));

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			if (isGridReady(statusUrl)) {
				return;
			}
			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting for Selenium Grid", e);
			}
		}

		throw new IllegalStateException(buildGridNotRunningMessage(gridUrl, statusUrl));
	}

	private static boolean isGridReady(String statusUrl) {
		try {
			HttpURLConnection connection = (HttpURLConnection) URI.create(statusUrl).toURL().openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			int code = connection.getResponseCode();
			if (code != 200) {
				return false;
			}
			String body = new String(connection.getInputStream().readAllBytes());
			return body.contains("\"ready\":true") || body.contains("\"ready\": true");
		} catch (Exception e) {
			return false;
		}
	}

	private static String toStatusUrl(String gridUrl) {
		String base = gridUrl.endsWith("/") ? gridUrl.substring(0, gridUrl.length() - 1) : gridUrl;
		if (base.endsWith("/wd/hub")) {
			return base + "/status";
		}
		return base + "/status";
	}

	private static String buildGridNotRunningMessage(String gridUrl, String statusUrl) {
		return "Selenium Grid is not reachable at " + gridUrl + " (status: " + statusUrl + "). "
				+ "Start Grid first: docker compose up -d  OR  powershell -File scripts/start-selenium-grid.ps1. "
				+ "For local runs without Docker, use: mvn clean test  (without -Pci / -Pgrid).";
	}

}
