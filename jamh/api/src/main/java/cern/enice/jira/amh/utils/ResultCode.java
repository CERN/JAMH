package cern.enice.jira.amh.utils;

public enum ResultCode {
	ISSUE_CREATED("Issue %s was created."),
	ISSUE_UPDATED("Issue %s was updated."),
	ISSUE_DELETED("Issue %s was deleted."),
	ISSUE_NOT_CREATED("Issue was not created."),
	ISSUE_NOT_UPDATED("Issue %s was not updated."),
	ISSUE_NOT_DELETED("Issue %s was not deleted."),
	ATTACHMENTS_UPLOADED("Attachments for issue %s were uploaded."),
	ATTACHMENTS_NOT_UPLOADED("Attachments for issue %s were not uploaded."),
	ISSUE_CREATED_BUT_ATTACHMENTS_NOT_UPLOADED(
			"Issue %s was created but attachments were not uploaded."),
	ISSUE_UPDATED_BUT_ATTACHMENTS_NOT_UPLOADED(
			"Issue %s was updated but attachments were not uploaded."),
	ISSUE_NOT_UPDATED_BUT_ATTACHMENTS_UPLOADED(
			"Issue %s was not updated but attachments were uploaded.");
	
	private String message;
	
	ResultCode(String message) {
		this.message = message;
	}
	
	public String getMessage(String issueKey) {
		return String.format(message, issueKey);
	}
}
