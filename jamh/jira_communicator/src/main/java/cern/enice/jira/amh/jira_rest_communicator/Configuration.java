package cern.enice.jira.amh.jira_rest_communicator;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.utils.Utils;

public class Configuration implements ManagedService, ConfigurationMBean {

	// Constants
	private static final int DEFAULT_CREATE_META_REFRESH_RATE = 30;
	
	private String projects;
	private String jiraBaseUrl;
	private String jiraUsername;
	private String jiraPassword;
	private int refreshRate;
	
	public String getProjects() {
		return projects;
	}

	void setProjects(String projects) {
		this.projects = projects;
	}

	public String getJiraBaseUrl() {
		return jiraBaseUrl;
	}

	void setJiraBaseUrl(String jiraBaseUrl) {
		this.jiraBaseUrl = jiraBaseUrl;
	}

	public String getJiraUsername() {
		return jiraUsername;
	}

	void setJiraUsername(String jiraUsername) {
		this.jiraUsername = jiraUsername;
	}

	public String getJiraPassword() {
		return jiraPassword;
	}

	public void setJiraPassword(String jiraPassword) {
		this.jiraPassword = jiraPassword;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties == null) return;
		jiraUsername = Utils.updateStringProperty(properties, "jiraUsername");
		jiraPassword = Utils.updateStringProperty(properties, "jiraPassword");
		jiraBaseUrl = Utils.updateStringProperty(properties, "jiraBaseUrl");
		String projectsProperty = (String)properties.get("projects");
		projects = null;
		if (projectsProperty != null && !projectsProperty.isEmpty())
			projects = projectsProperty.toUpperCase();
		refreshRate = DEFAULT_CREATE_META_REFRESH_RATE;
		String refreshRateProperty = (String)properties.get("createMetaRefreshRateInMinutes");
		if (refreshRateProperty == null || refreshRateProperty.isEmpty()) return;
		try {
			refreshRate = Integer.parseInt(refreshRateProperty);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("createMetaRefreshRateInMinutes", "value must be an integer");
		}
	}

}
