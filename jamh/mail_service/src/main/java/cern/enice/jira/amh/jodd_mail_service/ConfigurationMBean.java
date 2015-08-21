package cern.enice.jira.amh.jodd_mail_service;

import java.util.List;
import java.util.Map;

public interface ConfigurationMBean {
	public String getSmtpAccountForJmx();
	public String getImapAccountsForJmx();
	public String getPathToAttachments();
	public List<String> getSendersBlacklist();
	public Map<String, String> getAutoReplyHeaders();
}
