package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.List;
import java.util.Map;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.baseruleset.Tokens;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class AssigneeRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
	private volatile JiraCommunicator jiraCommunicator;
	private volatile MailService mailService;
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "AssigneeRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "AssigneeRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String assignee = tokens.get(Tokens.ASSIGNEE);
		if (tokens.containsKey(Tokens.ASSIGNEE) && assignee != null) {
			assignee = assignee.trim();
			if (assignee.contains("@"))
				assignee = ruleSetUtils.getUsernameByEmailAddress(mailService.parseEmailAddress(assignee));
			if (!assignee.isEmpty()) {
				issueDescriptor.setAssignee(assignee);
				return;
			}
		}
		boolean firstCcIsAssignee = configuration.isFirstCcIsAssignee();
		// If feature is enabled use first CC email address as assignee
		if (!firstCcIsAssignee || issueDescriptor.getKey() != null)
			return;
		List<EMailAddress> cc = email.getCc();
		// If there are email addresses in CC then set the first one as assignee
		if (cc != null && !cc.isEmpty()) {
			String username = ruleSetUtils.getUsernameByEmailAddress(cc.get(0));
			if (!username.isEmpty())
				issueDescriptor.setAssignee(username);
		}
	}
}