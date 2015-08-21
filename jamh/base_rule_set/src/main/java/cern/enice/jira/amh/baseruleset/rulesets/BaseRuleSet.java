package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.HashMap;
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
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

/**
 * Performs email tokens validation to set the main issue fields in issue
 * descriptor
 * 
 * @author vvasilye
 * @see RuleSet
 */
public class BaseRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	private volatile Configuration configuration;
	@SuppressWarnings("unused")
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "BaseRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "BaseRuleSet is stopped.");
	}
	
	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		logger.log(LogProvider.DEBUG, "Applying base rule set...");
		// Move all 3rd party tokens in subject before handler tokens 
		String subject = moveCapturedSubjectPatternsBeforeTokens(
				email.getSubject(), configuration.getIgnoreTokensPatterns());
		// Parse tokens out of subject and body
		email.setSubject(subject);
		String body = email.getBody();
		tokens.putAll(getTokens(subject, body));
	}
	
	String moveCapturedSubjectPatternsBeforeTokens(String subject, List<String> ignoreTokensPatterns) {
		if (subject == null) return null;
		int firstTokenPosition = subject.indexOf("#");
		String tokensPart = "", subjectWithoutTokens = "";
		if (firstTokenPosition > -1) {
			tokensPart = subject.substring(firstTokenPosition);
			subjectWithoutTokens = subject.substring(0, firstTokenPosition);
			Pattern pattern;
			Matcher matcher;
			for (String ignoreSubjectPattern : ignoreTokensPatterns) {
				pattern = Pattern.compile(ignoreSubjectPattern);
				matcher = pattern.matcher(tokensPart);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					String foundSubstring = matcher.group();
					matcher.appendReplacement(sb, "");
					subjectWithoutTokens += foundSubstring + " ";
				}
				matcher.appendTail(sb);
				if (!sb.toString().isEmpty())
					tokensPart = sb.toString();
			}
			return subjectWithoutTokens + " " + tokensPart;
		}
		return subject;
	}
	
	Map<String, String> getTokens(String subject, String body) {
		Map<String, String> tokens = new HashMap<String, String>();
		if (subject != null) {
			tokens.putAll(parseTokensString(subject));
		}

		// Parse tokens out of body and unite them with the subject tokens
		if (body != null) {
			String[] bodyStrings = body.split("\\n");
			for (String bodyString : bodyStrings) {
				if (!bodyString.startsWith("#"))
					break;
				tokens.putAll(parseTokensString(bodyString.trim()));
			}
		}
		return tokens;
	}

	Map<String, String> parseTokensString(String stringWithTokens) {
		Map<String, String> parsedTokens = new HashMap<String, String>();
		String[] tokens = stringWithTokens.split("#");
		for (int i = 1; i < tokens.length; i++) {
			String[] tokenArray = tokens[i].split("=", 2);
			if (tokenArray.length > 1) {
				parsedTokens.put(tokenArray[0].trim().toLowerCase(), tokenArray[1].trim().replace("__", " "));
			} else {
				parsedTokens.put(tokenArray[0].trim().toLowerCase(), null);
			}
		}
		return parsedTokens;
	}

	void setLogger(LogProvider logger) {
		this.logger = logger;
	}
	
	void setJiraCommunicator(JiraCommunicator jiraCommunicator) {
		this.jiraCommunicator = jiraCommunicator;
	}
	
	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
}
