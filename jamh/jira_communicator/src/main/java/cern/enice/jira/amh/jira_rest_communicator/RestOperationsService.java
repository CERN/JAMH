package cern.enice.jira.amh.jira_rest_communicator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.Transition;
import cern.enice.jira.amh.dto.User;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

public class RestOperationsService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;

	public void start() {
		logger.log(LogProvider.INFO, "REST Operations Service is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "REST Operations Service is stopped.");
	}
	
	public List<Transition> getIssueTransitions(String issueKey) {
		try {
			String url = String.format("%s/rest/api/latest/issue/%s/transitions", 
					configuration.getJiraBaseUrl(), issueKey);
			HttpRequest request = new HttpRequest(HttpMethod.GET, url, 
					configuration.getJiraUsername(), configuration.getJiraPassword(), 
					null, ContentType.NONE, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			if (response == null) throw new Exception();
			int responseStatus = (Integer) response.getStatus();
			@SuppressWarnings("unchecked")
			Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
			if (responseStatus < 200 || responseStatus >= 300 || responseContent == null) 
				throw new Exception();
			List<Transition> transitionsList = new LinkedList<Transition>();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> transitions = 
					(List<Map<String, Object>>) responseContent.get("transitions");
			for (Map<String, Object> transition : transitions) {
				transitionsList.add(new Transition((String) transition.get("id"),
						(String) transition.get("name")));
			}
			return transitionsList;
		} catch (Exception e) {
			logger.log(LogProvider.DEBUG, "Couldn't fetch issue " + issueKey + " transitions.", e);
		}
		return null;
	}

	public List<String> getIssueResolutions() {
		try {
			HttpRequest request = new HttpRequest(HttpMethod.GET, 
					configuration.getJiraBaseUrl() + "/rest/api/latest/resolution",
					configuration.getJiraUsername(), configuration.getJiraPassword(), 
					null, ContentType.NONE, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			if (response == null) throw new Exception();
			int responseStatus = (Integer) response.getStatus();
			if (responseStatus < 200 || responseStatus >= 300) throw new Exception();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> responseContent = 
					(List<Map<String, Object>>) response.getContent();
			if (responseContent == null) throw new Exception();
			List<String> resolutions = new LinkedList<String>();
			for (Map<String, Object> resolution : responseContent) {
				resolutions.add((String) resolution.get("name"));
			}
			return resolutions;
		} catch (Exception e) {
			logger.log(LogProvider.DEBUG, "Couldn't fetch issue resolutions.", e);
		}
		return null;
		}
	
	User getUser(String username) {
		try {
			String url = getUserSearchUrl(username);
			HttpRequest request = new HttpRequest(HttpMethod.GET, url, 
					configuration.getJiraUsername(), configuration.getJiraPassword(),
					null, ContentType.NONE, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			if (response == null) throw new Exception();
			int responseStatus = response.getStatus();
			Object content = response.getContent();
			if (responseStatus == 404) return null;
			else if (responseStatus < 200 || responseStatus >= 300 || content == null)
				throw new Exception();
			Map<String, Object> foundUser = getUserIfOneIsFound(username, content);
			if (foundUser == null) return null;
			return new User((String)foundUser.get("name"), 
					(String)foundUser.get("displayName"),
					(String)foundUser.get("emailAddress"));
		} catch (Exception e) {
			logger.log(LogProvider.WARNING, 
					"Error occured while trying to find JIRA user " + username, e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> getUserIfOneIsFound(String username, Object content) {
		if (username.contains("@")) {
			List<Map<String, Object>> foundUsers = ((List<Map<String, Object>>) content);
			for (Map<String, Object> cUser: foundUsers) {
				if (((String)cUser.get("emailAddress")).equals(username))
					return cUser;
			}
			return null;
		} else {
			return (Map<String, Object>)content; 
		}
	}
	
	String getUserSearchUrl(String username) {
		if (username.contains("@")) 
			return String.format(
					"%s/rest/api/latest/user/search?maxResults=0&username=%s", 
					configuration.getJiraBaseUrl(), username);
		else 
			return String.format(
					"%s/rest/api/latest/user?username=%s", 
					configuration.getJiraBaseUrl(), username);
	}
}
