package cern.enice.jira.amh.utils;

public class EmailHandlingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2287917237084239912L;
	
	private String errorCode = "FAILED_TO_PROCESS_RULE_SET";

	public EmailHandlingException(String message) {
		super(message);
	}
	
	public EmailHandlingException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
}
