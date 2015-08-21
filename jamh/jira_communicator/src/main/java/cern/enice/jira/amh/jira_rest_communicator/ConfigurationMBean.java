package cern.enice.jira.amh.jira_rest_communicator;

public interface ConfigurationMBean {
	public String getJiraBaseUrl();
	public String getProjects();
	public String getJiraUsername();
	public int getRefreshRate(); 
}
