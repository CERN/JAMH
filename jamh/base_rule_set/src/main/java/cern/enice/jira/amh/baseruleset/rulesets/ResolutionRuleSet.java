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
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class ResolutionRuleSet implements RuleSet {

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
		logger.log(LogProvider.INFO, "ResolutionRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "ResolutionRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		if (issueDescriptor.getKey() == null) {
			return;
		}
		String tokenValue = null;
		if (tokens.containsKey(Tokens.RESOLUTION)) {
			tokenValue = tokens.get(Tokens.RESOLUTION);
		}
		if (tokenValue == null || tokenValue.isEmpty()) {
			return;
		}
		// Get resolutions list and find the one with a corresponding name
		List<String> resolutions = jiraCommunicator.getIssueResolutions();
		if (resolutions == null) return;
		for (String resolution : resolutions) {
			if (resolution.equalsIgnoreCase(tokenValue)) {
				issueDescriptor.setResolution(resolution);
				return;
			}
		}
	}

}
