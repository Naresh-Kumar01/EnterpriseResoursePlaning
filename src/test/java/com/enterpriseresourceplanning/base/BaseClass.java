package com.enterpriseresourceplanning.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shared base for UI and API automation (logging helpers).
 */
public abstract class BaseClass {

	protected final Logger logger = LogManager.getLogger(getClass());

	protected void logInfo(String message) {
		logger.info(message);
	}

	protected void logError(String message, Throwable throwable) {
		logger.error(message, throwable);
	}

}
