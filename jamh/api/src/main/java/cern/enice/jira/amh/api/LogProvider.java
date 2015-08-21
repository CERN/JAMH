package cern.enice.jira.amh.api;

public interface LogProvider {
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	public static final int DEBUG = 4;
	
	public void log(int level, String message);
	public void log(int level, String message, Throwable exception);
	public void log(int level, String message, Object object);
	public void log(int level, String message, Object object, Throwable exception);
}
