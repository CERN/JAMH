package cern.enice.jira.amh.jira_rest_communicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;
import cern.enice.jira.amh.utils.ResultCode;

public class IssueOperatingService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;
	private volatile IssueOperatingHelperService helperService;

	public void start() {
		logger.log(LogProvider.INFO, "Issue Operating Service is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Issue Operating Service is stopped.");
	}
	
	Result createIssue(IssueDescriptor issueDescriptor) {
		Map<String, Object> issueData = new HashMap<String, Object>();
		Map<String, Object> fields = new HashMap<String, Object>();
		JiraCommunicatorUtils.setFields(fields, issueDescriptor);
		issueData.put("fields", fields);
		HttpRequest request = new HttpRequest(HttpMethod.POST, 
				configuration.getJiraBaseUrl() + "/rest/api/latest/issue", 
				configuration.getJiraUsername(), configuration.getJiraPassword(), issueData, 
				ContentType.JSON, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		int responseStatus = response.getStatus();
		@SuppressWarnings("unchecked")
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		if (responseContent == null)
			return new Result(ResultCode.ISSUE_NOT_CREATED);
		if (responseStatus < 200 || responseStatus >= 300) {
			return JiraCommunicatorUtils.getResultWithErrorsForCode(
					responseContent, ResultCode.ISSUE_NOT_CREATED);
		}
		String issueKey = (String) responseContent.get("key");
		issueDescriptor.setKey(issueKey);
		helperService.updateWatchersList(issueDescriptor);
		if (issueDescriptor.getWorklogTimespent() != null) 
			helperService.logWork(issueDescriptor);
		List<String> attachments = issueDescriptor.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			Result attachmentResult = helperService.attachFiles(issueDescriptor);
			if (attachmentResult.getCode() != ResultCode.ATTACHMENTS_UPLOADED) {
				return new Result(ResultCode.ISSUE_CREATED_BUT_ATTACHMENTS_NOT_UPLOADED);
			}
		}
		return new Result(ResultCode.ISSUE_CREATED);
	}

	Result updateIssue(IssueDescriptor issueDescriptor) {
		Result result = new Result(ResultCode.ISSUE_NOT_UPDATED);
		Map<String, Object> issueData = new HashMap<String, Object>();
		Map<String, Object> fields = new HashMap<String, Object>();
		JiraCommunicatorUtils.setFields(fields, issueDescriptor);
		issueData.put("fields", fields);
		if (issueDescriptor.getComment() != null) 
			issueData.put("update", getCommentObject(issueDescriptor));
		result = makeUpdateIssueRequest(issueDescriptor, issueData);
		helperService.updateWatchersList(issueDescriptor);
		if (issueDescriptor.getWorklogTimespent() != null) 
			helperService.logWork(issueDescriptor);
		List<String> attachments = issueDescriptor.getAttachments();
		if (attachments == null || attachments.isEmpty())
			return result;
		Result attachmentResult = helperService.attachFiles(issueDescriptor);
		if (attachmentResult.getCode() != ResultCode.ATTACHMENTS_UPLOADED
				&& result.getCode() == ResultCode.ISSUE_UPDATED) {
			result.setCode(ResultCode.ISSUE_UPDATED_BUT_ATTACHMENTS_NOT_UPLOADED);
		}
		return result;
	}
	
	public Result deleteIssue(IssueDescriptor issueDescriptor) {
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null) 
			return new Result(ResultCode.ISSUE_NOT_DELETED);
		String url = String.format("%s/rest/api/latest/issue/%s", 
				configuration.getJiraBaseUrl(), issueKey);
		HttpRequest request = new HttpRequest(HttpMethod.DELETE, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				null, ContentType.NONE, ContentType.NONE);
		HttpResponse response = networkClient.request(request);
		if (response == null) 
			return new Result(ResultCode.ISSUE_NOT_DELETED);
		int responseStatus = response.getStatus();
		if (responseStatus >= 200 && responseStatus < 300) {
			return new Result(ResultCode.ISSUE_DELETED);
		} else if (responseStatus == 403) {
			return new Result(ResultCode.ISSUE_NOT_DELETED, Arrays.asList(
					String.format("Issue %s couldn't be deleted: no permissions.", issueKey)));
		} else if (responseStatus == 404) {
			return new Result(ResultCode.ISSUE_NOT_DELETED, Arrays.asList(
					String.format("Issue %s couldn't be deleted as it doesn't exist.", issueKey)));
		}
		return new Result(ResultCode.ISSUE_NOT_DELETED);
	}
	
	/**
	 * Performs transition if transition id is set in issue descriptor, or update otherwise 
	 * @param issueDescriptor
	 * @param issueData
	 * @return
	 */
	Result makeUpdateIssueRequest(IssueDescriptor issueDescriptor, Map<String, Object> issueData) {
		HttpRequest request;
		String issueKey = issueDescriptor.getKey();
		String transitionId = issueDescriptor.getTransition();
		String url = String.format("%s/rest/api/latest/issue/%s", 
				configuration.getJiraBaseUrl(), issueKey);
		if (transitionId == null) {
			request = new HttpRequest(HttpMethod.PUT, url, 
					configuration.getJiraUsername(), configuration.getJiraPassword(), 
					issueData, ContentType.JSON, ContentType.JSON);
		} else {
			Map<String, Object> transitionObject = new HashMap<String, Object>();
			transitionObject.put("id", transitionId);
			issueData.put("transition", transitionObject);
			request = new HttpRequest(HttpMethod.POST, url + "/transitions", 
					configuration.getJiraUsername(), configuration.getJiraPassword(), 
					issueData, ContentType.JSON, ContentType.JSON);
		}
		HttpResponse response = networkClient.request(request);
		if (response == null) 
			return new Result(ResultCode.ISSUE_NOT_UPDATED);
		int responseStatus = response.getStatus();
		@SuppressWarnings("unchecked")
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		if (responseStatus >= 200 && responseStatus < 300) 
			return new Result(ResultCode.ISSUE_UPDATED);
		return JiraCommunicatorUtils.getResultWithErrorsForCode(
				responseContent, ResultCode.ISSUE_NOT_UPDATED);
	}
	
	Map<String, Object> getCommentObject(IssueDescriptor issueDescriptor) {
		String comment = issueDescriptor.getComment();
		Map<String, Object> addObject = new HashMap<String, Object>();
		Map<String, Object> bodyObject = new HashMap<String, Object>();
		bodyObject.put("body", comment);
		setCommentVisibility(issueDescriptor, bodyObject);
		addObject.put("add", bodyObject);
		ArrayList<Map<String, Object>> commentsArray = new ArrayList<Map<String, Object>>();
		commentsArray.add(addObject);
		Map<String, Object> commentObject = new HashMap<String, Object>();
		commentObject.put("comment", commentsArray);
		return commentObject;
	}
	
	void setCommentVisibility(IssueDescriptor issueDescriptor, Map<String, Object> commentObject) {
		String visibleTo = issueDescriptor.getCommentVisibleTo();
		if (visibleTo == null || visibleTo.trim().isEmpty()) return;
		Map<String, Object> visibilityObject = new HashMap<String, Object>();
		visibilityObject.put("type", "role");
		visibilityObject.put("value", visibleTo);
		commentObject.put("visibility", visibilityObject);
	}
}
