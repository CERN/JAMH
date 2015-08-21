package cern.enice.jira.amh.jira_rest_communicator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;
import cern.enice.jira.amh.utils.ResultCode;

public class IssueOperatingHelperService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;

	public void start() {
		logger.log(LogProvider.INFO, "Issue Operating Helper Service is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Issue Operating Helper Service is stopped.");
	}
	
	/**
	 * Adds watchers to JIRA issue watchers list
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info
	 */
	void updateWatchersList(IssueDescriptor issueDescriptor) {
		Set<String> watchers = issueDescriptor.getWatchers();
		if (watchers == null || watchers.isEmpty()) return;
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null || issueKey.isEmpty()) {
			Map<String, Object> logObject = new HashMap<String, Object>();
			logObject.put("watchers", watchers);
			logger.log(LogProvider.WARNING, "Couldn't add watchers to the list as issue key is NULL or empty.", logObject);
			return;
		}
		for (String watcher : watchers) {
			addWatcher(issueKey, watcher);
		}
	}
	
	/**
	 * Adds given username to a given issue as a watcher
	 * @param issueKey   given issue key
	 * @param username   given username
	 * @return           returns result code ISSUE_UPDATED if watcher was added, 
	 *                   or ISSUE_NOT_UPDATE otherwise 
	 */
	Result addWatcher(String issueKey, String username) {
		if (issueKey == null || issueKey.trim().isEmpty()) {
			logger.log(LogProvider.WARNING, 
					"Failed to add a watcher to the list as the issue key is NULL or empty.");
			return new Result(ResultCode.ISSUE_NOT_UPDATED);
		}
		if (username == null || username.trim().isEmpty()) {
			logger.log(LogProvider.INFO, String.format(
					"Failed to add watcher for issue %s as its name is NULL or empty.", issueKey));
			return new Result(ResultCode.ISSUE_NOT_UPDATED);
		}
		String url = String.format("%s/rest/api/latest/issue/%s/watchers", 
				configuration.getJiraBaseUrl(), issueKey);
		HttpRequest request = new HttpRequest(HttpMethod.POST, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				"\"" + username + "\"", ContentType.JSON, ContentType.NONE);
		HttpResponse response = networkClient.request(request);
		if (response == null) return new Result(ResultCode.ISSUE_NOT_UPDATED);
		if (response.getStatus() < 200 || response.getStatus() >= 300) {
			logger.log(LogProvider.INFO, String.format(
					"Couldn't add %s as watcher for ticket %s.", username, issueKey));
			return new Result(ResultCode.ISSUE_NOT_UPDATED);
		}
		logger.log(LogProvider.INFO, String.format(
				"Added %s as a watcher for ticket %s.", username, issueKey));
		return new Result(ResultCode.ISSUE_UPDATED);
	}
	
	/**
	 * Adds new worklog entry to JIRA issue
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info
	 * @return                  Contains result code and list of errors in case of failed operation
	 */
	Result logWork(IssueDescriptor issueDescriptor) {
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null || issueKey.trim().isEmpty()) {
			logger.log(LogProvider.WARNING, 
					"Failed to add a worklog entry as then issue's key is NULL or empty.");
			return new Result(ResultCode.ISSUE_NOT_UPDATED); 
		}
		Map<String, Object> issueData = new HashMap<String, Object>();
		String worklogStarted = issueDescriptor.getWorklogStarted();
		String worklogTimespent = issueDescriptor.getWorklogTimespent();
		if (worklogTimespent == null || worklogTimespent.trim().isEmpty()) 
			return new Result(ResultCode.ISSUE_NOT_UPDATED); 
		issueData.put("timeSpent", worklogTimespent);
		if (worklogStarted != null && !worklogStarted.trim().isEmpty()) 
			issueData.put("started", worklogStarted);
		String url = String.format("%s/rest/api/latest/issue/%s/worklog", 
				configuration.getJiraBaseUrl(), issueKey);
		HttpRequest request = new HttpRequest(HttpMethod.POST, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				issueData, ContentType.JSON, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		if (response == null) return new Result(ResultCode.ISSUE_NOT_UPDATED);
		int responseStatus = response.getStatus();
		if (responseStatus >= 200 && responseStatus < 300) 
			return new Result(ResultCode.ISSUE_UPDATED);
		logger.log(LogProvider.DEBUG, 
				String.format("Log work entry was not added for issue %s.", issueKey));
		return new Result(ResultCode.ISSUE_NOT_UPDATED);
	}

	/**
	 * Attaches files to JIRA issue   
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info 
	 * @return                  Contains result code and list of errors in case of failed operation
	 */
	Result attachFiles(IssueDescriptor issueDescriptor) {
		String issueKey = issueDescriptor.getKey();
		List<String> attachments = issueDescriptor.getAttachments();
		Map<String, Object> issueAttachments = new HashMap<String, Object>();
		for (int i = 0; i < attachments.size(); i++) 
			issueAttachments.put("file" + String.valueOf(i), attachments.get(i));
		String url = String.format("%s/rest/api/latest/issue/%s/attachments", 
				configuration.getJiraBaseUrl(), issueKey); 
		HttpRequest request = new HttpRequest(HttpMethod.POST, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				issueAttachments, ContentType.MULTIPART, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		// Delete all attachment files from temp directory
		for (String attachment : attachments) {
			File file = new File(attachment);
			file.delete();
		}
		if (response == null) 
			return new Result(ResultCode.ATTACHMENTS_NOT_UPLOADED);
		int responseStatus = response.getStatus();
		if (responseStatus >= 200 && responseStatus < 300) 
			return new Result(ResultCode.ATTACHMENTS_UPLOADED);
		return new Result(ResultCode.ATTACHMENTS_NOT_UPLOADED);
	}
}
