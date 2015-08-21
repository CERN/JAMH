package cern.enice.jira.amh.jira_rest_communicator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

public class GetIssueService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;

	public void start() {
		logger.log(LogProvider.INFO, "Get Issue Service is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Get Issue Service is stopped.");
	}
	
	IssueDescriptor getIssue(String issueKey) {
		if (issueKey == null || issueKey.isEmpty())
			return null;
		String url = String.format("%s/rest/api/latest/issue/%s", 
				configuration.getJiraBaseUrl(), issueKey);
		HttpRequest request = new HttpRequest(HttpMethod.GET, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				null, ContentType.NONE, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		if (response == null) return null;
		int responseStatus = response.getStatus();
		@SuppressWarnings("unchecked") 
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		if (responseStatus >= 200 && responseStatus < 300 && responseContent != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> fields = (Map<String, Object>)responseContent.get("fields");
			return convertFieldsToIssueDescriptor(issueKey, fields);
		}
		return null;
	}
	
	IssueDescriptor convertFieldsToIssueDescriptor(String issueKey, Map<String, Object> fields) {
		IssueDescriptor issueDescriptor = new IssueDescriptor();
		issueDescriptor.setKey(issueKey);
		issueDescriptor.setSummary((String)fields.get("summary"));
		issueDescriptor.setDescription((String)fields.get("description"));
		issueDescriptor.setAssignee( getFieldProperty(fields, "assignee", "name") );
		issueDescriptor.setReporter( getFieldProperty(fields, "reporter", "name") );
		issueDescriptor.setIssueType( getFieldProperty(fields, "issuetype", "name") );
		issueDescriptor.setProject( getFieldProperty(fields, "project", "key") );
		issueDescriptor.setPriority( getFieldProperty(fields, "priority", "name") );
		issueDescriptor.setStatus( getFieldProperty(fields, "status", "name") );
		@SuppressWarnings("unchecked") 
		Set<String> components = getMultivalueFieldValuesList(
				(List<Map<String, Object>>)fields.get("components"));
		issueDescriptor.setComponents(components);
		@SuppressWarnings("unchecked") 
		Set<String> fixVersions = getMultivalueFieldValuesList(
				(List<Map<String, Object>>)fields.get("fixVersions"));
		issueDescriptor.setFixVersions(fixVersions);
		@SuppressWarnings("unchecked") 
		Set<String> affectedVersion = getMultivalueFieldValuesList(
				(List<Map<String, Object>>)fields.get("versions"));
		issueDescriptor.setAffectedVersions(affectedVersion);
		Map<String, Object> customFields = new HashMap<String, Object>();
		for (String fieldName : fields.keySet()) {
			if (fieldName.startsWith("customfield_")) {
				customFields.put(fieldName, fields.get(fieldName));
			}
		}
		issueDescriptor.setCustomFields(customFields);
		return issueDescriptor;
	}
	
	String getFieldProperty(Map<String, Object> fields, String fieldName, String propertyName) {
		@SuppressWarnings("unchecked")
		Map<String, Object> field = (Map<String, Object>)fields.get(fieldName);
		if (field == null) return null;
		return (String)field.get(propertyName);
	}
	
	Set<String> getMultivalueFieldValuesList(List<Map<String, Object>> multivalueField) {
		Set<String> valuesList = new HashSet<String>();
		if (multivalueField == null) return valuesList;
		for (Map<String, Object> fieldValue : multivalueField) {
			valuesList.add((String)fieldValue.get("name"));
		}
		return valuesList;
	}
}
