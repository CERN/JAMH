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

public class IssueTypeRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;

	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service
	 * is started
	 */
	public void start() {
		logger.log(LogProvider.INFO, "IssueTypeRuleSet is started.");
	}

	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service
	 * is stopped
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "IssueTypeRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		if (issueDescriptor.getIssueType() != null && !issueDescriptor.getIssueType().isEmpty())
			return;
		IssueDescriptor originalIssueDescriptor = issueDescriptor.getOriginalState();
		String projectKey = originalIssueDescriptor.getProject() == null ? issueDescriptor.getProject()
				: originalIssueDescriptor.getProject();
		// Try to find issuetype in issuetype token's value
		String tokenValue = tokens.get(Tokens.ISSUETYPE);
		if (tokens.containsKey(Tokens.ISSUETYPE) && findIssueTypeByNameAndSet(issueDescriptor, tokenValue, projectKey))
			return;
		// Try to find issue type in token names
		for (String token : tokens.keySet())
			if (findIssueTypeByNameAndSet(issueDescriptor, token, projectKey))
				return;
		// If issue is to be updated then stop here
		if (originalIssueDescriptor.getIssueType() != null) return;
		// Try to set default issue type for specific handler
		Map<String, String> handlerAddressesToDefaultIssueTypeNames = configuration
				.getHandlerAddressesToDefaultIssueTypeNames();
		String defaultIssueTypeForHandler = ruleSetUtils.getDefaultValueForHandler(email.getTo(),
				handlerAddressesToDefaultIssueTypeNames);
		if (findIssueTypeByNameAndSet(issueDescriptor, defaultIssueTypeForHandler, projectKey))
			return;
		// Try to set default issue type
		String defaultIssueTypeName = configuration.getDefaultIssueTypeName();
		if (findIssueTypeByNameAndSet(issueDescriptor, defaultIssueTypeName, projectKey))
			return;
		throw new EmailHandlingException("Couldn't identify issue type for...");
	}

	/**
	 * Checks if issue type exists, sets issue type in issue descriptor
	 * 
	 * @param issueDescriptor
	 * @param name
	 * @param projectKey
	 * @return Returns true, if issue type exists and is set in issue
	 *         descriptor, otherwise returns false
	 */
	boolean findIssueTypeByNameAndSet(IssueDescriptor issueDescriptor, String name, String projectKey) {
		String issueType = jiraCommunicator.getIssueTypeRegisteredName(projectKey, name);
		if (issueType != null && !issueType.isEmpty()) {
			issueDescriptor.setIssueType(issueType);
			return true;
		}
		return false;
	}

}
