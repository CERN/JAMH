package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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

public class WatchersRuleSet implements RuleSet {

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
		logger.log(LogProvider.INFO, "WatchersRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "WatchersRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		Set<String> externalWatchers = new HashSet<String>();
		Set<String> watchers = new HashSet<String>();
		if (tokens.containsKey(Tokens.WATCHERS)) {
			processWatchersTokenValue(tokens.get(Tokens.WATCHERS), watchers, externalWatchers);
		}
		processWatchersInEmailFields(email, tokens, issueDescriptor, watchers, externalWatchers);

		// Set watchers
		if (watchers.size() > 0) {
			issueDescriptor.setWatchers(watchers);
		}
		// Set external watchers
		if (!configuration.isExternalWatchersFeatureEnabled()) return;
		setExternalWatchersInDescriptor(issueDescriptor, externalWatchers);
	}
	
	/**
	 * Adds usernames and email addresses parsed from token's value to the watchers set or
	 * to the externalWatchers set
	 * @param tokenValue
	 * @param watchers
	 * @param externalWatchers
	 */
	void processWatchersTokenValue(String tokenValue, Set<String> watchers, Set<String> externalWatchers) {
		if (tokenValue == null || tokenValue.isEmpty()) return;
		String[] watchersInTokenValue = tokenValue.replace(";", ",").split(",");
		for (String watcherInTokenValue : watchersInTokenValue) {
			watcherInTokenValue = watcherInTokenValue.trim();
			if (watcherInTokenValue.isEmpty()) 
				continue;
			if (!watcherInTokenValue.contains("@")) {
				watchers.add(watcherInTokenValue);
				continue;
			}
			EMailAddress watcherEmailAddress = mailService.parseEmailAddress(watcherInTokenValue);
			if (watcherEmailAddress == null || 
					!isExternalWatcherAllowed(watcherEmailAddress.toString())) 
				continue;
			externalWatchers.add(watcherEmailAddress.toString());
		}
	}
	
	/**
	 * Uses email addresses in CC and FROM email fields as watchers (if JIRA user is found by email address)
	 * or as external watchers  
	 * @param email
	 * @param tokens
	 * @param issueDescriptor
	 * @param watchers
	 * @param externalWatchers
	 */
	void processWatchersInEmailFields(EMail email, Map<String, String> tokens, 
			IssueDescriptor issueDescriptor, Set<String> watchers, Set<String> externalWatchers) {
		// Try to use addresses from CC header as watchers and external watchers  
		boolean ccAddressesAreWatchers = configuration.isCcAddressesAreWatchers();
		int maximumCcWatchers = configuration.getMaximumCcWatchers();
		if (ccAddressesAreWatchers) {
			List<EMailAddress> cc = email.getCc();
			for (int i = 0; i < cc.size() && i < maximumCcWatchers; i++) {
				addToWatchersOrExternalWatchers(cc.get(i), watchers, externalWatchers);
			}
		}

		if (!tokens.containsKey(Tokens.REPORTER) && issueDescriptor.getReporter() == null
				&& issueDescriptor.getCommentAuthor() == null) {
			addToWatchersOrExternalWatchers(email.getFrom(), watchers, externalWatchers);
		}
	}

	/**
	 * Tries to get username for a given email address and adds it to watchers list 
	 * or tries to adds email address to external watchers list in case user couldn't be 
	 * looked up in JIRA
	 * @param emailAddress
	 * @param watchers
	 * @param externalWatchers
	 */
	void addToWatchersOrExternalWatchers(EMailAddress emailAddress, Set<String> watchers, Set<String> externalWatchers) {
		String username = ruleSetUtils.getUsernameByEmailAddress(emailAddress);
		if (!username.isEmpty())
			watchers.add(username);
		else if (isExternalWatcherAllowed(emailAddress.toString())) {
			externalWatchers.add(emailAddress.toString());
		}
	}
	
	/**
	 * Verifies that a given email address is a valid external watcher 
	 * @param emailAddress   Given email address
	 * @return               True if email address is valid external watcher or false otherwise 
	 */
	boolean isExternalWatcherAllowed(String emailAddress) {
		List<String> externalWatchersWhitelist = configuration.getExternalWatchersWhitelist();
		if (externalWatchersWhitelist.size() == 0) return true;
		for (String whitelistElement : externalWatchersWhitelist) {
			if (emailAddress.toLowerCase().contains(whitelistElement))
				return true;
		}
		return false;
	}

	/**
	 * Sets external watcher email addresses to the appropriate custom field
	 * 
	 * @param issueDescriptor
	 *            External watchers are set to this descriptor object
	 * @param newExternalWatchersSet
	 *            Set containing external watchers email addresses
	 */
	private void setExternalWatchersInDescriptor(IssueDescriptor issueDescriptor, Set<String> newExternalWatchersSet) {
		if (newExternalWatchersSet == null || newExternalWatchersSet.isEmpty())
			return;
		// Construct external watchers custom field name
		String customFieldId = configuration.getExternalWatchersCustomFieldId();
		String externalWatchersFieldName = "customfield_" + customFieldId;
		// Get original set of external watchers
		IssueDescriptor originalIssueStateDescriptor = issueDescriptor.getOriginalState();
		Set<String> originalExternalWatchersSet = getOriginalExternalWatchers(originalIssueStateDescriptor, 
				issueDescriptor.getKey(), externalWatchersFieldName);
		// Initialise map object for custom fields if it doesn't yet exist
		Map<String, Object> customFields = issueDescriptor.getCustomFields();
		if (customFields == null) {
			customFields = new HashMap<String, Object>();
			issueDescriptor.setCustomFields(customFields);
		}
		// Add new external watchers set to the original set
		newExternalWatchersSet.addAll(originalExternalWatchersSet);
		String externalWatchers = StringUtils.join(newExternalWatchersSet, ",");
		customFields.put(externalWatchersFieldName, externalWatchers);
	}
	
	/**
	 * Returns original external watchers set (at the time when email handing started)
	 * @param originalIssueStateDescriptor
	 * @param issueKey
	 * @param customFieldName
	 * @return
	 */
	private Set<String> getOriginalExternalWatchers(IssueDescriptor originalIssueStateDescriptor, 
			String issueKey, String customFieldName) {
		Map<String, Object> originalCustomFields = new HashMap<String, Object>();
		if (issueKey == null) return new HashSet<String>();
		originalCustomFields = originalIssueStateDescriptor.getCustomFields();
		if (originalCustomFields == null) return new HashSet<String>();
		String originalExternalWatchersString = "";
		if (originalCustomFields.containsKey(customFieldName)) {
			originalExternalWatchersString = (String)originalCustomFields.get(customFieldName);
		}
		return new HashSet<String>(Arrays.<String>asList(originalExternalWatchersString.split(",|;")));
	}
}