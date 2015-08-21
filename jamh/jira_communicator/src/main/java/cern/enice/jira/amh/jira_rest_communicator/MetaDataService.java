package cern.enice.jira.amh.jira_rest_communicator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

public class MetaDataService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;
	
	// Non-configurable fields
	private Map<String, Object> createMeta = new ConcurrentHashMap<String, Object>();
	private volatile ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService scheduledExecutorService;
	
	// Constants
	private static final String FAILED_TO_FETCH_CREATE_META = 
			"Couldn't fetch issue create meta data due to invalid HTTP response.";

	public void start() {
		startFetchingMetaData();
		logger.log(LogProvider.INFO, "REST Operations Service is started.");
	}

	public void stop() {
		stopFetchingMetaData();
		logger.log(LogProvider.INFO, "REST Operations Service is stopped.");
	}
	
	private void startFetchingMetaData() {
		stopFetchingMetaData();
		fetchCreateMeta();
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					fetchCreateMeta();
				} catch (Throwable ex) {
					logger.log(LogProvider.DEBUG, "Unhandled exception while fetching create meta:", ex);
				}
			}
		}, configuration.getRefreshRate(), configuration.getRefreshRate(), TimeUnit.MINUTES);
	}

	private void stopFetchingMetaData() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			scheduledFuture = null;
		}
	}

	/**
	 * Downloads issue create meta data (issue fields, their types and available values) for the specified set of projects
	 * @param projectKeys   Comma-separated list of keys of the projects for which issue create meta data will be downloaded
	 * @return              Issue create meta data represented as an hierarchical Map
	 */
	synchronized void fetchCreateMeta() throws IllegalStateException {
		String projectKeysUrlParameter = "";
		if (configuration.getProjects() != null)
			projectKeysUrlParameter = "&projectKeys=" + configuration.getProjects();
		String url = String.format(
				"%s/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields%s",  
				configuration.getJiraBaseUrl(), projectKeysUrlParameter);
		HttpRequest request = new HttpRequest(HttpMethod.GET, url, 
				configuration.getJiraUsername(), configuration.getJiraPassword(), 
				null, ContentType.NONE, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		if (response == null) 
			throw new IllegalStateException(FAILED_TO_FETCH_CREATE_META);
		int responseStatus = response.getStatus();
		@SuppressWarnings("unchecked")
		Map<String, Object> createMetaData = (Map<String, Object>) response.getContent();
		if (responseStatus < 200 || responseStatus >= 300 || createMetaData == null) 
			throw new IllegalStateException(FAILED_TO_FETCH_CREATE_META);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> projects = 
				(List<Map<String, Object>>) createMetaData.get("projects");
		for (Map<String, Object> project : projects) {
			String projectKey = (String) project.get("key");
			createMeta.put(projectKey.toLowerCase(), updateProjectMetaWithShortCuts(project));
		}
		logger.log(LogProvider.INFO, "Issue create meta data is fetched.");
		return;
	}
	
	Map<String, Object> updateProjectMetaWithShortCuts(Map<String, Object> project) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> issuetypes = 
				(List<Map<String, Object>>) project.get("issuetypes");
		for (Map<String, Object> issuetype : issuetypes) {
			JiraCommunicatorUtils.convertFieldValuesListToMap("priority", "_priorities", issuetype);
			JiraCommunicatorUtils.convertFieldValuesListToMap("components", "_components", issuetype);
			JiraCommunicatorUtils.convertFieldValuesListToMap("versions", "_versions", issuetype);
			JiraCommunicatorUtils.convertFieldValuesListToMap("fixVersions", "_fixversions", issuetype);
			String issuetypeName = (String) issuetype.get("name");
			project.put(issuetypeName.toLowerCase(), issuetype);
		}
		return project;
	}
	
	@SuppressWarnings("unchecked")
	synchronized Map<String, Object> getIssueType(String projectKey, String issueTypeName) {
		if (!isValidProject(projectKey) || issueTypeName == null || issueTypeName.isEmpty())
			return null;
		Map<String, Object> project = (Map<String, Object>)createMeta.get(projectKey.toLowerCase());
		if (project == null)
			return null;
		return (Map<String, Object>)project.get(issueTypeName.toLowerCase());
	}
	
	String getFieldRegisteredName(String projectKey, String issueTypeName, 
			String fieldValueName, String fieldName) {
		if (fieldValueName == null || fieldValueName.isEmpty())
			return null;
		Map<String, Object> issueType = getIssueType(projectKey, issueTypeName);
		if (issueType == null) return null;
		@SuppressWarnings("unchecked")
		Map<String, Object> fieldValues = (Map<String, Object>)issueType.get(fieldName);
		if (fieldValues == null) return null;
		@SuppressWarnings("unchecked")
		Map<String, Object> field = 
				(Map<String, Object>)fieldValues.get(fieldValueName.toLowerCase());
		if (field == null) return null;
		return (String)field.get("name");
	}

	public synchronized boolean isValidProject(String projectKey) {
		if (createMeta == null || projectKey == null || projectKey.isEmpty())
			return false;
		return createMeta.containsKey(projectKey.toLowerCase());
	}

	public String getIssueTypeRegisteredName(String projectKey, String issueTypeName) {
		Map<String, Object> issueType = getIssueType(projectKey, issueTypeName);
		if (issueType == null)
			return null;
		return (String)issueType.get("name");
	}
}
