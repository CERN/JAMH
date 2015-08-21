package cern.enice.jira.amh.baseruleset.rulesets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class BasicFieldsRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
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
		logger.log(LogProvider.INFO, "BasicFieldsRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "BasicFieldsRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		validateDueDate(tokens, issueDescriptor);
		validateEnvironment(tokens, issueDescriptor);
		validateAttachments(email, issueDescriptor);
	}

	/**
	 * Sets issue duedate in issue descriptor if duedate token is specified and
	 * its value is properly formatted
	 * 
	 * @param tokens
	 *            Key-value pairs of token names and their values
	 * @param issueDescriptor
	 *            Issue descriptor object containing various issue fields
	 */
	void validateDueDate(Map<String, String> tokens, IssueDescriptor issueDescriptor) {
		if (!tokens.containsKey(Tokens.DUEDATE)) return;
		String tokenValue = tokens.get(Tokens.DUEDATE);
		if (tokenValue == null || tokenValue.isEmpty()) return;
		SimpleDateFormat dateFormat = new SimpleDateFormat(Configuration.JIRA_DATE_FORMAT);
		try {
			Date duedateAsDate = dateFormat.parse(tokenValue);
			issueDescriptor.setDueDate(dateFormat.format(duedateAsDate));
		} catch (ParseException e) {
			logger.log(LogProvider.WARNING, "Duedate value " + tokenValue + " has invalid format.");
		}
	}

	/**
	 * Sets issue environment in issue descriptor if environment token is
	 * specified
	 * 
	 * @param tokens
	 *            Key-value pairs of token names and their values
	 * @param issueDescriptor
	 *            Issue descriptor object containing various issue fields
	 */
	void validateEnvironment(Map<String, String> tokens, IssueDescriptor issueDescriptor) {
		if (!tokens.containsKey(Tokens.ENVIRONMENT)) return;
		String tokenValue = tokens.get(Tokens.ENVIRONMENT);
		issueDescriptor.setEnvironment(tokenValue);
	}

	/**
	 * Sets paths to attachment files to issue descriptor 
	 * @param email
	 * @param issueDescriptor
	 */
	void validateAttachments(EMail email, IssueDescriptor issueDescriptor) {
		List<String> attachments = email.getAttachments();
		if (attachments == null || attachments.isEmpty()) return;
		issueDescriptor.setAttachments(email.getAttachments());
	}

}
