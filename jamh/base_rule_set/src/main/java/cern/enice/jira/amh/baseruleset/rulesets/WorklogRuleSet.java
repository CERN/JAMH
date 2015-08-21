package cern.enice.jira.amh.baseruleset.rulesets;

import java.text.SimpleDateFormat;
import java.util.Date;
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

public class WorklogRuleSet implements RuleSet {
	
	public static final String JIRA_TIME_POSTFIX = "T08:00:00.000+0000";

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
		logger.log(LogProvider.INFO, "WorklogRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "WorklogRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		if (!tokens.containsKey(Tokens.WORKLOG_TIMESPENT)) return;
		String worklogTimespent = tokens.get(Tokens.WORKLOG_TIMESPENT);
		if (worklogTimespent == null || worklogTimespent.isEmpty() || 
				!worklogTimespent.matches(Configuration.REGEX_TIMETRACKING_FORMAT)) return;
		issueDescriptor.setWorklogTimespent(worklogTimespent);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(Configuration.JIRA_DATE_FORMAT);
		String worklogStarted = dateFormat.format(new Date());
		if (tokens.containsKey(Tokens.WORKLOG_STARTED)) {
			String startedTokenValue = tokens.get(Tokens.WORKLOG_STARTED);
			if (startedTokenValue == null || startedTokenValue.isEmpty()) return;
			try {
				worklogStarted = dateFormat.format( dateFormat.parse(startedTokenValue) );
				issueDescriptor.setWorklogStarted(worklogStarted + JIRA_TIME_POSTFIX);
			} catch (Exception e) {
				logger.log(LogProvider.WARNING, String.format(
						"Worklog Started value %s has invalid date format. Correct one: %s", 
						startedTokenValue, Configuration.JIRA_DATE_FORMAT));
			}
		}
	}

}
