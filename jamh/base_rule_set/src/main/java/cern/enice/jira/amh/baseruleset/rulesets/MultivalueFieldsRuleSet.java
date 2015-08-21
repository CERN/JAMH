package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class MultivalueFieldsRuleSet implements RuleSet {
	
	public static final String DEFAULT_SEPARATOR = ",";

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
		logger.log(LogProvider.INFO, "MultivalueFieldsRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "MultivalueFieldsRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		IssueDescriptor issueDescriptorOriginalState = issueDescriptor.getOriginalState();
		String projectKey = issueDescriptorOriginalState.getProject() == null ? issueDescriptor.getProject()
				: issueDescriptorOriginalState.getProject();
		String issueType = issueDescriptorOriginalState.getIssueType() == null ? issueDescriptor.getIssueType()
				: issueDescriptorOriginalState.getIssueType();
		// Check if components should be set
		if (issueDescriptor.getComponents() == null) {
			Set<String> components = validateMultivalueField(tokens, Tokens.COMPONENTS, 
					projectKey, issueType);
			issueDescriptor.setComponents(components);
		}
		// Check if fix versions should be set
		issueDescriptor.setFixVersions(validateMultivalueField(tokens, Tokens.FIX_VERSIONS,
				projectKey, issueType));
		// Check if affected versions should be set
		issueDescriptor.setAffectedVersions(validateMultivalueField(tokens, Tokens.AFFECTED_VERSIONS,
				projectKey, issueType));
	}
	
	Set<String> validateMultivalueField(Map<String, String> tokens, String tokenName, String projectKey, String issueTypeName) {
		if (!tokens.containsKey(tokenName))
			return null;
		Set<String> resultSet = new HashSet<String>();
		Set<String> tokenValues = getMultivalueFieldValues(tokens, tokenName);
		String registeredValue = null;
		for (String tokenValue : tokenValues) {
			if (tokenName.equals(Tokens.COMPONENTS))
				registeredValue = jiraCommunicator.getComponentRegisteredName(projectKey, issueTypeName, tokenValue.trim());
			else 
				registeredValue = jiraCommunicator.getVersionRegisteredName(projectKey, issueTypeName, tokenValue.trim());
			if (registeredValue != null && !registeredValue.isEmpty())
				resultSet.add(registeredValue);
			logger.log(LogProvider.DEBUG, String.format("Multivalue field %s:", tokenName), resultSet);
		}
		if (resultSet.isEmpty())
			return null;
		return resultSet;
	}

	/**
	 * Splits multifield token's value using separator (default or one provided by separator special token)  
	 * @param tokens
	 * @param tokenName
	 * @return            Returns multivalue field's values as a set
	 */
	Set<String> getMultivalueFieldValues(Map<String, String> tokens, String tokenName) {
		// Comma is used as a default separator unless custom separator token is
		// not set
		String separator = DEFAULT_SEPARATOR;
		if (tokens.containsKey(Tokens.SEPARATOR)) {
			String separatorTokenValue = tokens.get(Tokens.SEPARATOR);
			if (separatorTokenValue != null && !separatorTokenValue.isEmpty()) {
				separator = separatorTokenValue;
			}
		}
		String tokenValue = tokens.get(tokenName);
		if (tokens.containsKey(tokenName) && tokenValue != null && !tokenValue.isEmpty())
			return new HashSet<String>(Arrays.asList(tokenValue.toLowerCase().split(separator)));
		return Collections.emptySet();
	}

	 void setJiraCommunicator(JiraCommunicator jiraCommunicator) {
		this.jiraCommunicator = jiraCommunicator;
	}

	 void setLogger(LogProvider logProvider) {
		this.logger=logProvider;
		
	}
}
