package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class SummaryRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "SummaryRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "SummaryRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		Map<String, String> handlerAddressesToDefaultSummaries = configuration.getHandlerAddressesToDefaultSummaries();
		// Obtain subject part without tokens 
		String subjectWithoutTokens = ruleSetUtils.getSubjectWithoutTokens(email.getSubject());
		if (subjectWithoutTokens == null || subjectWithoutTokens.trim().isEmpty()) {
			// Subject doesn't contain summary so set the default one
			issueDescriptor.setSummary( getDefaultSummaryForHandler(email.getTo(), handlerAddressesToDefaultSummaries) );
		} else {
			if (issueDescriptor.getKey() != null) {
				// Issue key exists so check for 3rd party tokens in subject to update summary
				addOrUpdateThirdPartyTokens(issueDescriptor, subjectWithoutTokens);
				return;
			} else {
				// Set email subject (without tokens) as issue summary 
				issueDescriptor.setSummary(subjectWithoutTokens.trim());
			}
		}

		if (!tokens.containsKey(Tokens.SUMMARY)) return;
		String summary = tokens.get(Tokens.SUMMARY);
		if (summary != null && !summary.trim().isEmpty()) {
			issueDescriptor.setSummary(summary.trim());
			return;
		}
	}
	
	void addOrUpdateThirdPartyTokens(IssueDescriptor issueDescriptor, String subject) {
		IssueDescriptor originalIssueDescriptor = issueDescriptor.getOriginalState();
		List<String> ignoreTokensPatterns = configuration.getIgnoreTokensPatterns();
		Pattern pattern;
		Matcher subjectMatcher;
		int i;
		boolean thirdPartyTokenIsFound = false; 
		for (i = 0; i < ignoreTokensPatterns.size(); i++) {
			pattern = Pattern.compile(ignoreTokensPatterns.get(i));
			subjectMatcher = pattern.matcher(subject);
			if (!subjectMatcher.find()) continue;
			thirdPartyTokenIsFound = true;
			break;
		}
		if (!thirdPartyTokenIsFound) return;
		String currentSummary = originalIssueDescriptor.getSummary();
		String summaryToUpdate = currentSummary;
		for (int j = i; j < ignoreTokensPatterns.size(); j++) {
			pattern = Pattern.compile(ignoreTokensPatterns.get(j));
			subjectMatcher = pattern.matcher(subject);
			String subjectCapturedToken = null;
			// Capture the latest pattern occurence in the subject
			while (subjectMatcher.find()) {
				subjectCapturedToken = subjectMatcher.group().trim();
			}
			if (subjectCapturedToken == null) continue;
			Matcher summaryMatcher = pattern.matcher(summaryToUpdate);
			String summaryCapturedToken = null;
			// Capture the latest pattern occurence in the summary
			while (summaryMatcher.find()) {
				summaryCapturedToken = summaryMatcher.group().trim();
			}
			if (summaryCapturedToken != null) {
				// Update third party token in the summary with a new one
				if (summaryCapturedToken.equalsIgnoreCase(subjectCapturedToken)) continue;
				summaryToUpdate = summaryMatcher.replaceFirst(subjectCapturedToken);
			} else {
				// Append captured third party token to the summary
				summaryToUpdate += " " + subjectCapturedToken;
			}
		}
		summaryToUpdate = summaryToUpdate.trim();
		if (!currentSummary.equals(summaryToUpdate))
			issueDescriptor.setSummary(summaryToUpdate);
	}
	
	String getDefaultSummaryForHandler(List<EMailAddress> emailRecipients, Map<String, String> handlerAddressesToDefaultValues) {
		String defaultValue = ruleSetUtils.getDefaultValueForHandler(emailRecipients, handlerAddressesToDefaultValues);
		if (defaultValue != null)
			return defaultValue;
		return configuration.getDefaultSummary();
	}
}
