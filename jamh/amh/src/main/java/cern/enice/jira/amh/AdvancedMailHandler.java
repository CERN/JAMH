package cern.enice.jira.amh;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.api.ElasticSearchClient;
import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ResultCode;
import cern.enice.jira.amh.utils.EmailHandlingException;
import cern.enice.jira.amh.utils.Utils;

public class AdvancedMailHandler implements ManagedService, AdvancedMailHandlerMBean {

	private volatile ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService scheduledExecutorService;

	// Service dependencies
	volatile LogProvider logger;
	volatile MailService mailService;
	volatile JiraCommunicator jiraCommunicator;
	volatile ElasticSearchClient elasticSearch;

	volatile Map<Integer, RuleSet> ruleSets = new ConcurrentSkipListMap<Integer, RuleSet>();

	// Constants;
	private static final long MINIMAL_CHECK_MAILBOX_RATE = 10;
	private static final long DEFAULT_CHECK_MAILBOX_RATE = 30;
	private static final String RULE_SET_PRIORITY_PROPERTY_NAME = "jamh.priority";

	// Configurable fields
	private long checkMailboxRate = DEFAULT_CHECK_MAILBOX_RATE;
	private List<String> forwardFailureReportsTo = new ArrayList<String>();
	private List<String> failureReportsReceiversWhitelist = new ArrayList<String>();
	private String handlerEmailAddress;

	public long getCheckMailboxRate() {
		return checkMailboxRate;
	}
	public void setCheckMailboxRate(long checkMailboxRate) {
		if (checkMailboxRate < MINIMAL_CHECK_MAILBOX_RATE) {
			this.checkMailboxRate = DEFAULT_CHECK_MAILBOX_RATE;
			return;
		}
		this.checkMailboxRate = checkMailboxRate;
	}
	LogProvider getLogger() {
		return logger;
	}
	void setLogger(LogProvider logger) {
		this.logger = logger;
	}
	MailService getMailService() {
		return mailService;
	}
	void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	JiraCommunicator getJiraCommunicator() {
		return jiraCommunicator;
	}
	void setJiraCommunicator(JiraCommunicator jiraCommunicator) {
		this.jiraCommunicator = jiraCommunicator;
	}
	Map<Integer, RuleSet> getRuleSets() {
		return ruleSets;
	}
	void setRuleSets(Map<Integer, RuleSet> ruleSets) {
		this.ruleSets = ruleSets;
	}
	public List<String> getForwardFailureReportsTo() {
		return new ArrayList<String>(forwardFailureReportsTo);
	}
	void setForwardFailureReportsTo(List<String> forwardFailureReportsTo) {
		this.forwardFailureReportsTo = forwardFailureReportsTo;
	}
	public List<String> getFailureReportsReceiversWhitelist() {
		return new ArrayList<String>(failureReportsReceiversWhitelist);
	}
	void setFailureReportsReceiversWhitelist(List<String> failureReportsReceiversWhitelist) {
		this.failureReportsReceiversWhitelist = failureReportsReceiversWhitelist;
	}
	public void start() {
		startMailHandler();
	}

	public void stop() {
		stopMailHandler();
	}

	private synchronized void startMailHandler() {
		stopMailHandler();
		logger.log(LogProvider.INFO, "Advanced Mail Handler is started.");
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					handleNewMessages();
				} catch (Throwable e) {
					String message = "Unhandled exception: " + e.getLocalizedMessage();
					logger.log(LogProvider.ERROR, message, e);
					elasticSearch.send(HandlerUtils.constructLogObject(message), "log");
				}
			}
		}, 5, checkMailboxRate, TimeUnit.SECONDS);
	}

	private synchronized void stopMailHandler() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			scheduledFuture = null;
			logger.log(LogProvider.INFO, "Advanced Mail Handler is stopped.");
		}
	}

	void handleNewMessages() {
		if (ruleSets.isEmpty()) {
			String message = "No need to fetch email as no rule sets are registered.";
			logger.log(LogProvider.WARNING, message);
			return;
		}
		logger.log(LogProvider.DEBUG, "Collecting new email...");
		List<EMail> emails = mailService.collectEmail();
		if (emails == null || emails.isEmpty()) {
			logger.log(LogProvider.DEBUG, "No email to handle");
			return;
		}
		logger.log(LogProvider.INFO, String.format("%d message(s) collected.", emails.size()));
		for (EMail email : emails) {
			handleMessage(email);
		}
	}

	void handleMessage(EMail email) {
		String subject = email.getSubject();
		String body = email.getBody();
		// If email subject and body are empty, log it and proceed to the next email
		if ( (subject == null || subject.trim().isEmpty()) && 
				(body == null || body.trim().isEmpty()) ) {
			String message = "Email can't be processed as its subject and body are empty.";
			logger.log(LogProvider.INFO, message);
			elasticSearch.send(HandlerUtils.constructLogObject(
					message, email), "email-handling-log");
			return;
		}
		logger.log(LogProvider.INFO, 
				String.format("Handling email with subject \"%s\", sent by %s.", 
						email.getSubject(), email.getFrom().toString()));
		// Prepare issue descriptor by processing it with a set of registered rule sets
		IssueDescriptor issueDescriptor = new IssueDescriptor();
		issueDescriptor.setOriginalState(new IssueDescriptor());
		Map<String, String> tokens = new HashMap<String, String>();
		if (!applyRuleSets(email, tokens, issueDescriptor)) return;
		Result result = takeAction(issueDescriptor);
		logResult(issueDescriptor, email, result);
	}
	
	boolean applyRuleSets(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor) {
		for (int priority : ruleSets.keySet()) {
			RuleSet ruleSet = ruleSets.get(priority);
			try {
				ruleSet.process(email, tokens, issueDescriptor);
			} catch (EmailHandlingException e) {
				String message = "Email couldn't be processed: " + e.getLocalizedMessage();
				logger.log(LogProvider.ERROR, message, e);
				elasticSearch.send(HandlerUtils.constructLogObject(
						message, email), "email-handling-log");
				return false;	
			} catch (Exception e) {
				String message = HandlerUtils.constructRuleSetExceptionMessage(
						ruleSet.getClass().getSimpleName(), issueDescriptor, email);
				logger.log(LogProvider.WARNING, message, e);
				elasticSearch.send(HandlerUtils.constructLogObject(
						message, email), "email-handling-log");
			}
		}
		return true;
	}

	/**
	 * Analyses issue descriptor and takes an appropriate action (create/update/delete)
	 * @param issueDescriptor
	 * @return
	 */
	Result takeAction(IssueDescriptor issueDescriptor) {
		String issueKey = issueDescriptor.getKey();
		if (issueKey == null || issueKey.isEmpty()) {
			return jiraCommunicator.createIssue(issueDescriptor);
		}
		boolean toBeDeleted = issueDescriptor.isDelete();
		if (toBeDeleted) {
			return jiraCommunicator.deleteIssue(issueDescriptor);
		}
		return jiraCommunicator.updateIssue(issueDescriptor);
	}

	/**
	 * Logs operation result by sending log entry to the log providers, to Elastic Search 
	 * and by email to service responsible
	 * @param issueDescriptor
	 * @param email
	 * @param result
	 */
	void logResult(IssueDescriptor issueDescriptor, EMail email, Result result) {
		String issueKey = issueDescriptor.getKey();
		ResultCode code = result.getCode();
		String message = code.getMessage(issueKey);
		Map<String, Object> logObject = HandlerUtils.constructLogObject(
				message, email, issueDescriptor, result);
		if (code != ResultCode.ISSUE_CREATED && code != ResultCode.ISSUE_UPDATED 
				&& code != ResultCode.ISSUE_DELETED) {
			logger.log(LogProvider.WARNING, message, logObject);
			notifyOfFailedOperation(email, message, logObject);
		} else {
			logger.log(LogProvider.INFO, message);
		}
		elasticSearch.send(logObject, "email-handling-results");
	}

	/**
	 * Notifies service responsible and handled email's sender about a failed operation  
	 * @param handledEmail
	 * @param message
	 * @param logObject
	 */
	void notifyOfFailedOperation(EMail handledEmail, String message, Map<String, Object> logObject) {
		EMail notificationEmail = new EMail();
		EMailAddress handlerEmailSender = handledEmail.getFrom();
		// By default send failure reports only to the predefined set of users (service responsibles)
		List<EMailAddress> to = new ArrayList<EMailAddress>();
		for (String toAddress : forwardFailureReportsTo)
			to.add(mailService.parseEmailAddress(toAddress));
		// If the handled email sender is whitelisted, he will receive a failure report notification
		for (String failureReportReceiver : failureReportsReceiversWhitelist) {
			if (handlerEmailSender.toString().contains(failureReportReceiver)) {
				to.add(handlerEmailSender);
				break;
			}
		}
		if (to.isEmpty()) return;
		// Add failure reasons to the message
		@SuppressWarnings("unchecked")
		List<String> errors = (List<String>)logObject.get("errors");
		if (errors != null && !errors.isEmpty()) {
			message += "\nReasons:\n- " + StringUtils.join(errors, "\n- ");
		}
		notificationEmail.setTo(to);
		notificationEmail.setFrom(mailService.parseEmailAddress(handlerEmailAddress));
		notificationEmail.setSubject("JIRA Advanced Mail Handler: notification of a failed operation");
		notificationEmail.setBody(message);
		if (!mailService.sendEmail(notificationEmail)) {
			logger.log(LogProvider.WARNING, 
					"Could not send an email notification of a failed operation.", 
					notificationEmail);
		}
	}

	public void ruleSetAdded(ServiceReference<RuleSet> ref, RuleSet ruleSet) {
		String jamhPriority = (String) ref.getProperty(RULE_SET_PRIORITY_PROPERTY_NAME);
		if (jamhPriority == null) {
			logger.log(LogProvider.WARNING, String.format(
					"Couldn't remove rule set %s as its %s " + 
					"service property is missing.", 
					RULE_SET_PRIORITY_PROPERTY_NAME, ruleSet.getClass().getSimpleName()));
			return;
		}
		try {
			int priority = Integer.parseInt(jamhPriority);
			ruleSets.put(priority, ruleSet);
		} catch (NumberFormatException e) {
			logger.log(LogProvider.WARNING, String.format(
					"Couldn't remove rule set %s as its %s " + 
					"service property is not a valid number.", 
					RULE_SET_PRIORITY_PROPERTY_NAME, ruleSet.getClass().getSimpleName()));
		}
	}

	public void ruleSetRemoved(ServiceReference<RuleSet> ref, RuleSet ruleSet) {
		String jamhPriority = (String) ref.getProperty(RULE_SET_PRIORITY_PROPERTY_NAME);
		if (jamhPriority == null) {
			return;
		}
		try {
			int priority = Integer.parseInt(jamhPriority);
			ruleSets.remove(priority);
		} catch (NumberFormatException e) {
			logger.log(LogProvider.WARNING, String.format("Couldn't remove rule set %s", 
					ruleSet.getClass().getSimpleName()));
		}
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties == null) return;
		handlerEmailAddress = Utils.updateStringProperty(properties, "handlerEmailAddress");
		String checkMailboxRateProperty = Utils.updateStringProperty(properties, "checkMailboxRate");
		try {
			setCheckMailboxRate(Long.parseLong(checkMailboxRateProperty));
		} catch (NumberFormatException ex) {
			setCheckMailboxRate(DEFAULT_CHECK_MAILBOX_RATE);
		}
		failureReportsReceiversWhitelist.clear();
		failureReportsReceiversWhitelist.addAll(
				Utils.updateConfigurableListField(properties, "failureReportsReceiversWhitelist"));
		forwardFailureReportsTo.clear();
		forwardFailureReportsTo.addAll(
				Utils.updateConfigurableListField(properties, "forwardFailureReportsTo"));
	}
}
