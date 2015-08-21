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

public class ReporterRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
	private volatile JiraCommunicator jiraCommunicator;
	private volatile MailService mailService;
	@SuppressWarnings("unused")
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "ReporterRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "ReporterRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String reporter = tokens.get(Tokens.REPORTER);
		if (tokens.containsKey(Tokens.REPORTER) && reporter != null) {
			reporter = reporter.trim();
			if (reporter.contains("@"))
				reporter = ruleSetUtils.getUsernameByEmailAddress(mailService.parseEmailAddress(reporter));
			if (!reporter.isEmpty()) {
				issueDescriptor.setReporter(reporter);
				return;
			}
		}
		if (issueDescriptor.getKey() != null) return;
		reporter = ruleSetUtils.getUsernameByEmailAddress(ruleSetUtils.getEmailSender(email));
		if (reporter.isEmpty()) return;
		issueDescriptor.setReporter(reporter);
	}
}