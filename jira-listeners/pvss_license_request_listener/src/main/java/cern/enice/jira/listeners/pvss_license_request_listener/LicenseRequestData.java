package cern.enice.jira.listeners.pvss_license_request_listener;

import java.util.List;
import java.util.Map;

public class LicenseRequestData {
	private String issueKey;
	private String messageId;
	private String assigneeEmail;
	private String reporterDetails;
	private String customFields; 
	private String creationDate;
	private String description;
	private String comment;
	private List<Map<String, Object>> components;
	public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getAssigneeEmail() {
		return assigneeEmail;
	}
	public void setAssigneeEmail(String assigneeEmail) {
		this.assigneeEmail = assigneeEmail;
	}
	public String getReporterDetails() {
		return reporterDetails;
	}
	public void setReporterDetails(String reporterDetails) {
		this.reporterDetails = reporterDetails;
	}
	public String getCustomFields() {
		return customFields;
	}
	public void setCustomFields(String customFields) {
		this.customFields = customFields;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<Map<String, Object>> getComponents() {
		return components;
	}
	public void setComponents(List<Map<String, Object>> components) {
		this.components = components;
	}
}
