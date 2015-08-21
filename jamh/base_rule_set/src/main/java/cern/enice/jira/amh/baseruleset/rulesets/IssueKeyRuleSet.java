package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class IssueKeyRuleSet implements RuleSet {
	
	public static final String REGEX_ISSUEKEY = "\\b[A-Za-z]{2,}-[0-9]+\\b";

	// Service dependencies
	private volatile LogProvider logger;
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "IssueKeyRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "IssueKeyRuleSet is stopped.");
	}

	/**
	 * Checks if issue is to be created or updated: sets issue key and 
	 * queries its original state in the latter case.
	 * Throws RuleSetProcessingException if create or update operation is not allowed. 
	 */
	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String subjectWithoutTokens = ruleSetUtils.getSubjectWithoutTokens(email.getSubject());
		if (subjectWithoutTokens == null || subjectWithoutTokens.trim().isEmpty())
			return;
		Pattern issueKeyPattern = Pattern.compile(REGEX_ISSUEKEY);
		Matcher matcher = issueKeyPattern.matcher(subjectWithoutTokens);
		if (matcher.find()) {
			String issueKey = matcher.group().trim().toUpperCase();
			String[] issueKeyParts = issueKey.split("-");
			if (jiraCommunicator.isValidProject(issueKeyParts[0])) {
				issueDescriptor.setKey(issueKey);
			}
		}
		String allowedOperations = configuration.getAllowedOperations();
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null && !allowedOperations.contains(Configuration.CREATE))
			throw new EmailHandlingException("Creating issue is not allowed.");
		if (issueKey != null && !allowedOperations.contains(Configuration.UPDATE))
			throw new EmailHandlingException("Updating issue is not allowed.");
		IssueDescriptor originalIssueStateDescriptor = issueDescriptor.getOriginalState();
		if (issueKey == null) {
			issueDescriptor.setOriginalState(new IssueDescriptor());
		} else if (originalIssueStateDescriptor.getKey() == null) {
			originalIssueStateDescriptor = jiraCommunicator.getIssue(issueKey);
			issueDescriptor.setOriginalState(originalIssueStateDescriptor);
		}
	}

}
