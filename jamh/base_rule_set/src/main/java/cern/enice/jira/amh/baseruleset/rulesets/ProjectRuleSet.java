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

public class ProjectRuleSet implements RuleSet {

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
		logger.log(LogProvider.INFO, "ProjectRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "ProjectRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String issueKey = issueDescriptor.getKey();
		if (issueKey != null) return;
		String projectKey = null;
		// Try to set project token's value if it exists
		if (tokens.containsKey(Tokens.PROJECTKEY)) {
			String tokenValue = tokens.get(Tokens.PROJECTKEY);
			if (jiraCommunicator.isValidProject(tokenValue))
				projectKey = tokenValue;
		} else {
			projectKey = findProjectKeyInFromAddress(email.getFrom());
		}
		if (projectKey == null || projectKey.trim().isEmpty()) {
			projectKey = getDefaultProjectKey(email.getTo());
		}
		if (projectKey == null || projectKey.trim().isEmpty())
			throw new EmailHandlingException("Couldn't identify project key for...");
		issueDescriptor.setProject(projectKey.toUpperCase());
	}
	
	/**
	 * Tries to obtain project key associated with one of the email recipients or a default project key 
	 * @param recipients   list of email addresses of email recipients
	 * @return             project key or null if no valid default project key was found
	 */
	String getDefaultProjectKey(List<EMailAddress> recipients) {
		String defaultProjectKey = configuration.getDefaultProjectKey();
		Map<String, String> handlerAddressesToDefaultProjectKeys = configuration
				.getHandlerAddressesToDefaultProjectKeys();
		String defaultProjectKeyForHandler = ruleSetUtils.getDefaultValueForHandler(
				recipients, handlerAddressesToDefaultProjectKeys);
		if (jiraCommunicator.isValidProject(defaultProjectKeyForHandler))
			return defaultProjectKeyForHandler;
		else if (jiraCommunicator.isValidProject(defaultProjectKey))
			return defaultProjectKey;
		return null;
	}
	
	/**
	 * Tries to obtain project key in sender's email address. First it tries to get
	 * a project key associated with email domain name. Second it tries to find 
	 * project key in personal name part of email address, e.g. PROJECTKEY <email@address>.   
	 * @param fromAddress   sender's email address
	 * @return              project key or null if no valid project key was found
	 */
	String findProjectKeyInFromAddress(EMailAddress fromAddress) {
		Map<String, String> domainsToProjects = configuration.getDomainsToProjects();
		String personalName = fromAddress.getPersonalName();
		String domain = fromAddress.getDomain().toLowerCase();
		String projectForDomain = null;
		if (domainsToProjects.containsKey(domain)) {
			projectForDomain = domainsToProjects.get(domain);
			if (jiraCommunicator.isValidProject(projectForDomain)) {
				return projectForDomain;
			}
		} else if (personalName != null) {
			personalName = personalName.trim();
			if (jiraCommunicator.isValidProject(personalName)) {
				return personalName;
			}
		}
		return null;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setJiraCommunicator(JiraCommunicator jiraCommunicator) {
		this.jiraCommunicator = jiraCommunicator;
	}
	public void setRuleSetUtils(RuleSetUtils ruleSetUtils) {
		this.ruleSetUtils = ruleSetUtils;
	}
}
