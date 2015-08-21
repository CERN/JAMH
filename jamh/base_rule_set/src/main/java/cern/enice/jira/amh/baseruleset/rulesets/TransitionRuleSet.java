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
import cern.enice.jira.amh.dto.Transition;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class TransitionRuleSet implements RuleSet {

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
		logger.log(LogProvider.INFO, "TransitionRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "TransitionRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null)
			return;
		String tokenValue = null;
		if (tokens.containsKey(Tokens.TRANSITION)) {
			tokenValue = tokens.get(Tokens.TRANSITION);
			if (tokenValue == null) return;
			// If token value is a number then no need to query issue transitions list
			if (tokenValue.matches("\\d+")) {
				issueDescriptor.setTransition(tokenValue);
				return;
			}
		} else if (tokens.containsKey(Tokens.TRANSITION_RESOLVE)) {
			// TODO short token value should be a number to avoid an extra http request 
			tokenValue = Tokens.TRANSITION_RESOLVE_VALUE;
		} else if (tokens.containsKey(Tokens.TRANSITION_CLOSE)) {
			tokenValue = Tokens.TRANSITION_CLOSE_VALUE;
		} else if (tokens.containsKey(Tokens.TRANSITION_REOPEN)) {
			tokenValue = Tokens.TRANSITION_REOPEN_VALUE;
		}
		if (tokenValue == null || tokenValue.isEmpty())
			return;
		// Get transitions list and find the one with a corresponding name
		List<Transition> transitions = jiraCommunicator.getIssueTransitions(issueKey);
		if (transitions == null) return;
		for (Transition transition : transitions) {
			if (transition.getName().equalsIgnoreCase(tokenValue)) {
				issueDescriptor.setTransition(transition.getId());
				return;
			}
		}
	}

}
