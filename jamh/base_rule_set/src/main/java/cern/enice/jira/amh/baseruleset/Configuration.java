package cern.enice.jira.amh.baseruleset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.service.cm.ConfigurationException;

import cern.enice.jira.amh.utils.Utils;

public class Configuration implements ConfigurationMBean {
	
	// Constants
	public static final String REGEX_TIMETRACKING_FORMAT = "^[0-9]{1,}[wdh]{0,1}( [0-9]{1,}[wdh]{0,1}){0,3}$";
	public static final String JIRA_DATE_FORMAT = "yyyy-MM-dd";
	public static final int MAXIMUM_CC_WATCHERS = 5;
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";

	private String defaultProjectKey;
	private Map<String, String> handlerAddressesToDefaultProjectKeys;
	private String defaultIssueTypeName;
	private Map<String, String> handlerAddressesToDefaultIssueTypeNames;
	private String defaultSummary;
	private Map<String, String> handlerAddressesToDefaultSummaries;
	private boolean firstCcIsAssignee;
	private boolean ccAddressesAreWatchers;
	private boolean externalWatchersFeatureEnabled;
	private String externalWatchersCustomFieldId;
	private Map<String, String> domainsToProjects;
	private List<String> ignoreTokensPatterns;
	private List<String> externalWatchersWhitelist;
	private String allowedOperations;
	private int maximumCcWatchers;
	private Map<String, String> summariesToIssueTypes;
	private Map<String, String> summariesToComponents;
	
	static class CaseInsensitiveComparator implements Comparator<String> {
	    public static final CaseInsensitiveComparator INSTANCE = 
	           new CaseInsensitiveComparator();

	    public int compare(String first, String second) {
	         // some null checks
	         return first.compareToIgnoreCase(second);
	    }
	}
	public String getDefaultProjectKey() {
		return defaultProjectKey;
	}

	public void setDefaultProjectKey(String defaultProjectKey) {
		this.defaultProjectKey = defaultProjectKey;
	}

	public Map<String, String> getHandlerAddressesToDefaultProjectKeys() {
		Map<String, String> result = new TreeMap<String, String>(CaseInsensitiveComparator.INSTANCE);
		result.putAll(this.handlerAddressesToDefaultProjectKeys);
		return result;
	}

	public void setHandlerAddressesToDefaultProjectKeys(Map<String, String> handlerAddressesToDefaultProjectKeys) {
		this.handlerAddressesToDefaultProjectKeys = handlerAddressesToDefaultProjectKeys;
	}

	public String getDefaultIssueTypeName() {
		return defaultIssueTypeName;
	}

	public void setDefaultIssueTypeName(String defaultIssueTypeName) {
		this.defaultIssueTypeName = defaultIssueTypeName;
	}

	public Map<String, String> getHandlerAddressesToDefaultIssueTypeNames() {
		return new HashMap<String, String>(handlerAddressesToDefaultIssueTypeNames);
	}

	public void setHandlerAddressesToDefaultIssueTypeNames(Map<String, String> handlerAddressesToDefaultIssueTypeNames) {
		this.handlerAddressesToDefaultIssueTypeNames = handlerAddressesToDefaultIssueTypeNames;
	}

	public String getDefaultSummary() {
		return defaultSummary;
	}

	public void setDefaultSummary(String defaultSummary) {
		this.defaultSummary = defaultSummary;
	}

	public Map<String, String> getHandlerAddressesToDefaultSummaries() {
		return new HashMap<String, String>(handlerAddressesToDefaultSummaries);
	}

	public void setHandlerAddressesToDefaultSummaries(Map<String, String> handlerAddressesToDefaultSummaries) {
		this.handlerAddressesToDefaultSummaries = handlerAddressesToDefaultSummaries;
	}

	public boolean isFirstCcIsAssignee() {
		return firstCcIsAssignee;
	}

	public void setFirstCcIsAssignee(boolean firstCcIsAssignee) {
		this.firstCcIsAssignee = firstCcIsAssignee;
	}

	public boolean isCcAddressesAreWatchers() {
		return ccAddressesAreWatchers;
	}

	public void setCcAddressesAreWatchers(boolean ccAddressesAreWatchers) {
		this.ccAddressesAreWatchers = ccAddressesAreWatchers;
	}

	public boolean isExternalWatchersFeatureEnabled() {
		return externalWatchersFeatureEnabled;
	}

	public void setExternalWatchersFeatureEnabled(boolean externalWatchersFeatureEnabled) {
		this.externalWatchersFeatureEnabled = externalWatchersFeatureEnabled;
	}

	public String getExternalWatchersCustomFieldId() {
		return externalWatchersCustomFieldId;
	}

	public void setExternalWatchersCustomFieldId(String externalWatchersCustomFieldId) {
		this.externalWatchersCustomFieldId = externalWatchersCustomFieldId;
	}

	public Map<String, String> getDomainsToProjects() {
		return new HashMap<String, String>(domainsToProjects);
	}

	public void setDomainsToProjects(Map<String, String> domainsToProjects) {
		this.domainsToProjects = domainsToProjects;
	}

	public List<String> getIgnoreTokensPatterns() {
		return new ArrayList<String>(ignoreTokensPatterns);
	}

	public void setIgnoreTokensPatterns(List<String> ignoreTokensPatterns) {
		this.ignoreTokensPatterns = ignoreTokensPatterns;
	}

	public List<String> getExternalWatchersWhitelist() {
		return new ArrayList<String>(externalWatchersWhitelist);
	}

	public void setExternalWatchersWhitelist(List<String> externalWatchersWhitelist) {
		this.externalWatchersWhitelist = externalWatchersWhitelist;
	}

	public String getAllowedOperations() {
		return allowedOperations;
	}

	public void setAllowedOperations(String allowedOperations) {
		this.allowedOperations = allowedOperations;
	}

	public int getMaximumCcWatchers() {
		return maximumCcWatchers;
	}

	public void setMaximumCcWatchers(int maximumCcWatchers) {
		this.maximumCcWatchers = maximumCcWatchers;
	}

	public Map<String, String> getSummariesToIssueTypes() {
		return new HashMap<String, String>(summariesToIssueTypes);
	}

	public void setSummariesToIssueTypes(Map<String, String> summariesToIssueTypes) {
		this.summariesToIssueTypes = summariesToIssueTypes;
	}

	public Map<String, String> getSummariesToComponents() {
		return new HashMap<String, String>(summariesToComponents);
	}

	public void setSummariesToComponents(Map<String, String> summariesToComponents) {
		this.summariesToComponents = summariesToComponents;
	}

	/**
	 * Updates BaseRuleSet configuration
	 */
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// Set configurable fields to hardcoded defaults
		handlerAddressesToDefaultProjectKeys = new HashMap<String, String>();
		handlerAddressesToDefaultIssueTypeNames = new HashMap<String, String>();
		handlerAddressesToDefaultSummaries = new HashMap<String, String>();
		summariesToIssueTypes = new HashMap<String, String>();
		summariesToComponents = new HashMap<String, String>();
		externalWatchersCustomFieldId = "";
		domainsToProjects = new HashMap<String, String>();
		ignoreTokensPatterns = new ArrayList<String>();
		externalWatchersWhitelist = new ArrayList<String>();
		maximumCcWatchers = MAXIMUM_CC_WATCHERS;

		// Override configurable fields with values from configuration file
		String defaultProjectKeyProperty = (String) properties.get("defaultProjectKey");
		String defaultIssueTypeNameProperty = (String) properties.get("defaultIssueTypeName");
		String defaultSummaryProperty = (String) properties.get("defaultSummary");
		String firstCcIsAssigneeProperty = (String) properties.get("firstCcIsAssignee");
		String ccAddressesAreWatchersProperty = (String) properties.get("ccAddressesAreWatchers");
		String externalWatchersFeatureEnabledProperty = (String) properties.get("externalWatchersFeatureEnabled");
		String externalWatchersCustomFieldIdProperty = (String) properties.get("externalWatchersCustomFieldId");
		String allowedOperations = (String) properties.get("allowedOperations");
		String maximumCcWatchersProperty = (String) properties.get("maximumCcWatchers");

		// Fail service configuration update in case of missing required
		// properties
		if (defaultProjectKeyProperty == null)
			throw new ConfigurationException("defaultProjectKey", "property is missing");
		if (defaultIssueTypeNameProperty == null)
			throw new ConfigurationException("defaultIssueTypeName", "property is missing");
		if (defaultSummaryProperty == null)
			throw new ConfigurationException("defaultSummary", "property is missing");
		if (firstCcIsAssigneeProperty == null)
			throw new ConfigurationException("firstCcIsAssignee", "property is missing");
		if (ccAddressesAreWatchersProperty == null)
			throw new ConfigurationException("ccAddressesAreWatchers", "property is missing");
		if (externalWatchersFeatureEnabledProperty == null)
			throw new ConfigurationException("externalWatchersFeatureEnabled", "property is missing");

		if (externalWatchersCustomFieldIdProperty != null && !externalWatchersCustomFieldIdProperty.trim().isEmpty()) {
			externalWatchersCustomFieldId = externalWatchersCustomFieldIdProperty.trim();
		}

		if (maximumCcWatchersProperty != null && !maximumCcWatchersProperty.trim().isEmpty())
			try {
				maximumCcWatchers = Integer.parseInt(maximumCcWatchersProperty);
			} catch (NumberFormatException ex) {
				// Maximum CC watchers is already set to the default maximum
			}

		defaultProjectKey = defaultProjectKeyProperty.trim().toLowerCase();
		defaultIssueTypeName = defaultIssueTypeNameProperty.trim().toLowerCase();
		defaultSummary = defaultSummaryProperty.trim();
		firstCcIsAssignee = Boolean.parseBoolean(firstCcIsAssigneeProperty.trim());
		ccAddressesAreWatchers = Boolean.parseBoolean(ccAddressesAreWatchersProperty.trim());
		externalWatchersFeatureEnabled = Boolean.parseBoolean(externalWatchersFeatureEnabledProperty.trim());

		if (allowedOperations != null && !allowedOperations.isEmpty()) {
			String allowedOperationsLowercased = allowedOperations.toLowerCase();
			if (!allowedOperationsLowercased.contains(CREATE) && !allowedOperationsLowercased.contains(UPDATE)
					&& !allowedOperationsLowercased.contains(DELETE)) {
				throw new ConfigurationException("allowedOperations",
						"property must contain whether create, update, delete or " + "any combination of these.");
			}
			this.allowedOperations = allowedOperationsLowercased;
		}

		domainsToProjects.clear();
		domainsToProjects.putAll(Utils.updateConfigurableMapField(
				properties, "domainToProject", "<domain>::<projectKey>"));

		handlerAddressesToDefaultProjectKeys.clear();
		handlerAddressesToDefaultProjectKeys.putAll(Utils.updateConfigurableMapField(
				properties, "handlerAddressToDefaultProjectKey", "<emailAddress>::<projectKey>"));

		handlerAddressesToDefaultIssueTypeNames.clear();
		handlerAddressesToDefaultIssueTypeNames.putAll(Utils.updateConfigurableMapField(
				properties, "handlerAddressToDefaultIssueTypeName", "<emailAddress>::<issueTypeName>"));

		handlerAddressesToDefaultSummaries.clear();
		handlerAddressesToDefaultSummaries.putAll(Utils.updateConfigurableMapField(
				properties, "handlerAddressToDefaultSummary", "<emailAddress>::<summary>"));
		
		summariesToIssueTypes.clear();
		summariesToIssueTypes.putAll(Utils.updateConfigurableMapField(
				properties, "summaryToIssueType", "<summary>::<issueType>"));
		
		summariesToComponents.clear();
		summariesToComponents.putAll(Utils.updateConfigurableMapField(
				properties, "summaryToComponent", "<summary>::<component>"));

		ignoreTokensPatterns.clear();
		ignoreTokensPatterns.addAll(Utils.updateConfigurableListField(properties, "ignoreTokensPattern"));

		externalWatchersWhitelist.clear();
		externalWatchersWhitelist.addAll(Utils.updateConfigurableListField(properties, "externalWatchersWhitelist"));
	}
}
