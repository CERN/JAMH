package cern.enice.jira.amh.api;

import java.util.Map;

import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public interface RuleSet {
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor) 
			throws EmailHandlingException;
}
