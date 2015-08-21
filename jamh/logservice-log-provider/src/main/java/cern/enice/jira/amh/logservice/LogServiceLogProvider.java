package cern.enice.jira.amh.logservice;

import org.osgi.service.log.LogService;

import cern.enice.jira.amh.api.LogProvider;

public class LogServiceLogProvider implements LogProvider {
	
	private volatile LogService logService;
	private volatile LogProvider logProvider;
	
	public void start() {
		log(LogProvider.INFO, "LogService Log Provider service is started.");
	}
	
	public void stop() {
		log(LogProvider.INFO, "LogService Log Provider service is stopped.");
	}

	@Override
	public void log(int level, String message) {
		logService.log(level, message);
		logProvider.log(level, message);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		logService.log(level, message, exception);
		logProvider.log(level, message, exception);
	}

	@Override
	public void log(int level, String message, Object object) {
		logService.log(level, message);
		logService.log(level, "No need to write object to LogService log.");
		logProvider.log(level, message, object);
	}

	@Override
	public void log(int level, String message, Object object, Throwable exception) {
		logService.log(level, message, exception);
		logService.log(level, "No need to write object to LogService log.");
		logProvider.log(level, message, object, exception);
		
	}

}