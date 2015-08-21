package cern.enice.jira.amh.jira_rest_communicator;

import java.util.List;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.dto.Transition;
import cern.enice.jira.amh.dto.User;

public class JiraRestCommunicator implements JiraCommunicator {
	
	// Service dependencies
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile Configuration configuration;
	private volatile IssueOperatingService issueOperationService;
	private volatile GetIssueService getIssueService;
	private volatile RestOperationsService restOperationsService;
	private volatile MetaDataService metaDataService;

	LogProvider getLogger() {
		return logger;
	}

	void setLogger(LogProvider logger) {
		this.logger = logger;
	}

	NetworkClient getNetworkClient() {
		return networkClient;
	}

	void setNetworkClient(NetworkClient networkClient) {
		this.networkClient = networkClient;
	}
	
	Configuration getConfiguration() {
		return configuration;
	}

	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	IssueOperatingService getIssueOperationService() {
		return issueOperationService;
	}

	void setIssueOperationService(IssueOperatingService issueOperationService) {
		this.issueOperationService = issueOperationService;
	}

	GetIssueService getGetIssueService() {
		return getIssueService;
	}

	void setGetIssueService(GetIssueService getIssueService) {
		this.getIssueService = getIssueService;
	}

	RestOperationsService getRestOperationsService() {
		return restOperationsService;
	}

	void setRestOperationsService(RestOperationsService restOperationsService) {
		this.restOperationsService = restOperationsService;
	}

	MetaDataService getMetaDataService() {
		return metaDataService;
	}

	void setMetaDataService(MetaDataService metaDataService) {
		this.metaDataService = metaDataService;
	}

	public void start() {
		logger.log(LogProvider.INFO, "REST JIRA Communicator is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "REST JIRA Communicator is stopped.");
	}

	@Override
	public Result createIssue(IssueDescriptor issueDescriptor) {
		return issueOperationService.createIssue(issueDescriptor);
	}

	@Override
	public Result updateIssue(IssueDescriptor issueDescriptor) {
		return issueOperationService.updateIssue(issueDescriptor);
	}

	@Override
	public Result deleteIssue(IssueDescriptor issueDescriptor) {
		return issueOperationService.deleteIssue(issueDescriptor);
	}

	@Override
	public IssueDescriptor getIssue(String issueKey) {
		return getIssueService.getIssue(issueKey);
	}

	@Override
	public User getUser(String username) {
		return restOperationsService.getUser(username);
	}

	@Override
	public List<Transition> getIssueTransitions(String issueKey) {
		return restOperationsService.getIssueTransitions(issueKey);
	}

	@Override
	public List<String> getIssueResolutions() {
		return restOperationsService.getIssueResolutions();
	}

	@Override
	public String getPriorityRegisteredName(String projectKey, String issueTypeName, String priorityName) {
		return metaDataService.getFieldRegisteredName(projectKey, issueTypeName, priorityName, "_priorities");
	}

	@Override
	public String getComponentRegisteredName(String projectKey, String issueTypeName, String componentName) {
		return metaDataService.getFieldRegisteredName(projectKey, issueTypeName, componentName, "_components");
	}

	@Override
	public String getVersionRegisteredName(String projectKey, String issueTypeName, String versionName) {
		return metaDataService.getFieldRegisteredName(projectKey, issueTypeName, versionName, "_versions");
	}

	@Override
	public boolean isValidProject(String projectKey) {
		return metaDataService.isValidProject(projectKey);
	}

	@Override
	public String getIssueTypeRegisteredName(String projectKey, String issueTypeName) {
		return metaDataService.getIssueTypeRegisteredName(projectKey, issueTypeName);
	}
}