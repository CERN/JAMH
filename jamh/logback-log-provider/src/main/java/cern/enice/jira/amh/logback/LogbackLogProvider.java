package cern.enice.jira.amh.logback;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.enice.jira.amh.api.LogProvider;

public class LogbackLogProvider implements LogProvider {
	
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private ObjectMapper objectMapper = new ObjectMapper();

	public void start() {
		log(LogProvider.INFO, "Logback Log Provider service is started.");
	}

	public void stop() {
		log(LogProvider.INFO, "Logback Log Provider service is stopped.");
	}

	@Override
	public void log(int level, String message) {
		log(level, message, null, null);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		log(level, message, null, exception);
	}

	@Override
	public void log(int level, String message, Object object) {
		log(level, message, object, null);
	}

	@Override
	public void log(int level, String message, Object object, Throwable exception) {
		if (level == LogProvider.ERROR) {
			logger.error(message, exception);
		} else if (level == LogProvider.WARNING) {
			logger.warn(message, exception);
		} else if (level == LogProvider.INFO) {
			logger.info(message, exception);
		} else if (level == LogProvider.DEBUG) {
			logger.debug(message, exception);
		}
		logObjectAsString(level, object);
	}
	
	private void logObjectAsString(int level, Object object) {
		if (object == null) return;
		try {
			String objectAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			if (objectAsString == null) return;
			switch (level) {
				case ERROR: logger.error(objectAsString); break;
				case WARNING: logger.warn(objectAsString); break;
				case INFO: logger.info(objectAsString); break;
				case DEBUG: logger.debug(objectAsString); break;
			}
		} catch (Exception e) {
			logger.debug("Failed to convert object to string.", e);
		}
	}
}