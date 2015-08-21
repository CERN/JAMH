package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class AutoTriageRuleSet implements RuleSet {

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
		logger.log(LogProvider.INFO, "AutoTriageRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "AutoTriageRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String summary = issueDescriptor.getSummary();
		if (issueDescriptor.getKey() != null || summary == null || summary.isEmpty())
			return;
		String autoTriagedIssueType = configuration.getSummariesToIssueTypes().get(summary.toLowerCase());
		if (autoTriagedIssueType != null)
			issueDescriptor.setIssueType(autoTriagedIssueType);
		String autoTriagedComponent = configuration.getSummariesToComponents().get(summary.toLowerCase());
		if (autoTriagedComponent == null) return;
		Set<String> components = new HashSet<String>();
		components.add(autoTriagedComponent);
		issueDescriptor.setComponents(components);
	}
}
