package cern.enice.jira.listeners.second_line_support_request_listener;

public class SupportRequestData {
	String issueKey;
	String summary;
	String priorityName; 
	String creationDate;
	String reporterDetails;
	String description;
	public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getPriorityName() {
		return priorityName;
	}
	public void setPriorityName(String priorityName) {
		this.priorityName = priorityName;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getReporterDetails() {
		return reporterDetails;
	}
	public void setReporterDetails(String reporterDetails) {
		this.reporterDetails = reporterDetails;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
