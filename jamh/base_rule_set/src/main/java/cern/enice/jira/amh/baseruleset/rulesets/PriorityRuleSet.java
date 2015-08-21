package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.Map;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.baseruleset.Tokens;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class PriorityRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	@SuppressWarnings("unused")
	private volatile Configuration configuration;
	@SuppressWarnings("unused")
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "PriorityRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "PriorityRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		IssueDescriptor issueDescriptorOriginalState = issueDescriptor.getOriginalState();
		String projectKey = issueDescriptorOriginalState.getProject() == null ? 
				issueDescriptor.getProject() : issueDescriptorOriginalState.getProject();
		String issueType = issueDescriptorOriginalState.getIssueType() == null ? 
				issueDescriptor.getIssueType() : issueDescriptorOriginalState.getIssueType();
		for (String token : tokens.keySet()) {
			String priority = jiraCommunicator.getPriorityRegisteredName(projectKey, issueType, token);
			if (priority != null && !priority.isEmpty()) {
				issueDescriptor.setPriority(priority);
				return;
			}
		}
		if (!tokens.containsKey(Tokens.PRIORITY))
			return;
		String tokenValue = tokens.get(Tokens.PRIORITY);
		String priority = jiraCommunicator.getPriorityRegisteredName(projectKey, issueType, tokenValue);
		if (priority != null && !priority.isEmpty()) {
			issueDescriptor.setPriority(priority);
		}
	}
}
