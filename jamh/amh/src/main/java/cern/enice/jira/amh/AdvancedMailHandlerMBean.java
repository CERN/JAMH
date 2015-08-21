package cern.enice.jira.amh;

import java.util.List;

import cern.enice.jira.amh.dto.EMailAddress;

public interface AdvancedMailHandlerMBean {
	public long getCheckMailboxRate();
	public List<String> getForwardFailureReportsTo();
	public List<String> getFailureReportsReceiversWhitelist();
}
