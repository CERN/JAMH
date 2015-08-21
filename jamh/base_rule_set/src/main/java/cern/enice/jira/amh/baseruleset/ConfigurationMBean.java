package cern.enice.jira.amh.baseruleset;

import java.util.List;
import java.util.Map;

public interface ConfigurationMBean {
	public String getDefaultProjectKey();
	public String getDefaultIssueTypeName();
	public String getDefaultSummary();
	public boolean isFirstCcIsAssignee();
	public boolean isCcAddressesAreWatchers();
	public Map<String, String> getDomainsToProjects();
	public List<String> getIgnoreTokensPatterns();
	public String getExternalWatchersCustomFieldId();
	public List<String> getExternalWatchersWhitelist();
}
