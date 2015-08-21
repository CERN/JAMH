package cern.enice.jira.amh.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IssueDescriptor {
	private String key = null;
	private String summary = null;
	private String description = null;
	private String comment = null;
	private String commentAuthor = null;
	private String commentVisibleTo = null;
	private String project = null;
	private String issueType = null;
	private String priority = null;
	private Set<String> components = null;
	private Set<String> fixVersions = null;
	private Set<String> affectedVersions = null;
	private Set<String> labels = null;
	private String environment = null;
	private String assignee = null;
	private String reporter = null;
	private String transition = null;
	private String resolution = null;
	private String status = null;
	private boolean delete = false;
	private String dueDate = null;
	private String worklogStarted = null;
	private String worklogTimespent = null;
	private String originalEstimate = null;
	private String remainingEstimate = null;
	private Set<String> watchers = null;
	private List<String> attachments = null;
	private IssueDescriptor originalState = null;
	
	private Map<String, Object> customFields = null;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
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
	public String getCommentAuthor() {
		return commentAuthor;
	}
	public void setCommentAuthor(String commentAuthor) {
		this.commentAuthor = commentAuthor;
	}
	public String getCommentVisibleTo() {
		return commentVisibleTo;
	}
	public void setCommentVisibleTo(String commentVisibleTo) {
		this.commentVisibleTo = commentVisibleTo;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getIssueType() {
		return issueType;
	}
	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public Set<String> getComponents() {
		return components;
	}
	public void setComponents(Set<String> components) {
		this.components = components;
	}
	public Set<String> getFixVersions() {
		return fixVersions;
	}
	public void setFixVersions(Set<String> fixVersions) {
		this.fixVersions = fixVersions;
	}
	public Set<String> getAffectedVersions() {
		return affectedVersions;
	}
	public void setAffectedVersions(Set<String> affectedVersions) {
		this.affectedVersions = affectedVersions;
	}
	public Set<String> getLabels() {
		return labels;
	}
	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public String getReporter() {
		return reporter;
	}
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
	public String getTransition() {
		return transition;
	}
	public void setTransition(String transition) {
		this.transition = transition;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getWorklogStarted() {
		return worklogStarted;
	}
	public void setWorklogStarted(String worklogStarted) {
		this.worklogStarted = worklogStarted;
	}
	public String getWorklogTimespent() {
		return worklogTimespent;
	}
	public void setWorklogTimespent(String worklogTimespent) {
		this.worklogTimespent = worklogTimespent;
	}
	public String getOriginalEstimate() {
		return originalEstimate;
	}
	public void setOriginalEstimate(String estimate) {
		this.originalEstimate = estimate;
	}
	public String getRemainingEstimate() {
		return remainingEstimate;
	}
	public void setRemainingEstimate(String remaining) {
		this.remainingEstimate = remaining;
	}
	public Set<String> getWatchers() {
		return watchers;
	}
	public void setWatchers(Set<String> watchers) {
		this.watchers = watchers;
	}
	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	public Map<String, Object> getCustomFields() {
		return customFields;
	}
	public void setCustomFields(Map<String, Object> customFields) {
		this.customFields = customFields;
	}
	public IssueDescriptor getOriginalState() {
		return originalState;
	}
	public void setOriginalState(IssueDescriptor originalState) {
		this.originalState = originalState;
	}
}
