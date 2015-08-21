package cern.enice.jira.listeners.issue_creation_listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.utils.MimeType;
import cern.enice.jira.amh.utils.Utils;
 
@Path( "/send-issue-creation-email-notification" )
public class IssueCreationListener implements ManagedService {
	
	// Service Dependencies
	private volatile LogProvider logger;
	private volatile MailService mailService;
	
	// Configurable fields
	private String jiraAddress;
	private String defaultEmailSender;
	private Map<String, String> projectsToEmailSenders;
	private String externalWatchersCustomFieldId;
	private List<String> ignoreReporters;
	private String notificationMessageTemplate;

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

	public String getJiraAddress() {
		return jiraAddress;
	}

	public void setJiraAddress(String jiraAddress) {
		this.jiraAddress = jiraAddress;
	}

	public String getDefaultEmailSender() {
		return defaultEmailSender;
	}

	public void setDefaultEmailSender(String defaultEmailSender) {
		this.defaultEmailSender = defaultEmailSender;
	}

	public Map<String, String> getProjectsToEmailSenders() {
		return new HashMap<String, String>(projectsToEmailSenders);
	}

	public void setProjectsToEmailSenders(Map<String, String> projectsToEmailSenders) {
		this.projectsToEmailSenders = projectsToEmailSenders;
	}

	public String getExternalWatchersCustomFieldId() {
		return externalWatchersCustomFieldId;
	}

	public void setExternalWatchersCustomFieldId(String externalWatchersCustomFieldId) {
		this.externalWatchersCustomFieldId = externalWatchersCustomFieldId;
	}

	public List<String> getIgnoreReporters() {
		return new ArrayList<String>(ignoreReporters);
	}

	public void setIgnoreReporters(List<String> ignoreReporters) {
		this.ignoreReporters = ignoreReporters;
	}

	public String getNotificationMessageTemplate() {
		return notificationMessageTemplate;
	}

	public void setNotificationMessageTemplate(String notificationMessageTemplate) {
		this.notificationMessageTemplate = notificationMessageTemplate;
	}

	public void start() {
		logger.log(LogProvider.INFO, "Issue Creation Listener is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Issue Creation Listener is stopped.");
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testSendEmailNotification() {
		logger.log(LogProvider.INFO, "Hello from Issue Creation Listener!");
		return "Hello from Issue Creation Listener!";
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String sendEmailNotification(String webhookJsonData) {
		try {
			process(webhookJsonData);
		} catch (Exception e) {
			logger.log(LogProvider.DEBUG, e.getMessage(), e);
		}
    	return "";
    }

	public void process(String webhookJsonData) 
			throws JsonParseException, JsonMappingException, IOException {
		// Prepare issue data to use in a notification email
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> webhookData = objectMapper.readValue(webhookJsonData, Map.class);
		Map<String, Object> issue = (Map<String, Object>) webhookData.get("issue");
		String issueKey = (String) issue.get("key");
		Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
		String summary = (String) fields.get("summary");
		String description = (String) fields.get("description");
		description = (description == null ? "(No description)" : description);
		Map<String, Object> reporter = (Map<String, Object>) fields.get("reporter");
		String reporterFullName = (String) reporter.get("displayName");
		String reporterEmail = (String) reporter.get("emailAddress");
		String externalWatchers = 
				(String) fields.get("customfield_" + externalWatchersCustomFieldId);
		List<EMailAddress> notificationRecipients = 
				getNotificationRecipients(externalWatchers, reporterEmail);
		// Stop if there is no one to inform
		if (notificationRecipients.isEmpty()) return;
		// Send issue creation notification and log the operation
		EMailAddress notificationSender = getNotificationSender(fields);
		sendNotification(issueKey, summary, description, reporterFullName, 
				notificationRecipients, notificationSender);
		logNotification(issueKey, summary, description, reporterFullName, 
				notificationRecipients, notificationSender);
	}
	
	EMailAddress getNotificationSender(Map<String, Object> fields) {
		Map<String, Object> project = (Map<String, Object>) fields.get("project");
		String projectKey = ((String)project.get("key")).toLowerCase();
		String emailSenderString = projectsToEmailSenders.containsKey(projectKey) ?
				projectsToEmailSenders.get(projectKey) : defaultEmailSender;
		return mailService.parseEmailAddress(emailSenderString);
	}
	
	List<EMailAddress> getNotificationRecipients(String externalWatchers, String reporterEmail) {
		if (externalWatchers != null && !externalWatchers.trim().isEmpty()) {
			externalWatchers = externalWatchers.replace(";", ",");
		} else {
			externalWatchers = "";
		}
		// Notify reporter only if its email address is not blacklisted
		String notificationRecipients = externalWatchers;
		if (!ignoreReporters.contains(reporterEmail.toLowerCase())) {
			notificationRecipients += "," + reporterEmail;
		}
		return convertToEMailAddressList(notificationRecipients);
	}
	
	private List<EMailAddress> convertToEMailAddressList(String notificationRecipients) {
		String[] recipientsArray = notificationRecipients.split(",");
		List<EMailAddress> recipientsList = new ArrayList<EMailAddress>();
		for (String recipient : recipientsArray) {
			EMailAddress emailAddress = mailService.parseEmailAddress(recipient.trim());
			if (emailAddress == null) continue;
			recipientsList.add(emailAddress);
		}
		return recipientsList;
	}
	
	void sendNotification(String issueKey, String summary, String description, 
			String reporterFullName, List<EMailAddress> notificationRecipients, 
			EMailAddress notificationSender) {
		String emailSubject = issueKey + " " + summary;
		String emailBody = String.format(notificationMessageTemplate,
				reporterFullName, jiraAddress, issueKey, issueKey, summary, 
				description, issueKey, notificationSender, issueKey,
				reporterFullName, jiraAddress, issueKey, issueKey, summary, 
				description, issueKey, notificationSender, issueKey);
		EMail notificationEmail = new EMail();
		notificationEmail.setFrom(notificationSender);
		notificationEmail.setTo(notificationRecipients);
		notificationEmail.setSubject(emailSubject);
		notificationEmail.setBody(emailBody);
		notificationEmail.setMime(MimeType.HTML);
		mailService.sendEmail(notificationEmail);
	}
	
	void logNotification(String issueKey, String summary, String description, 
			String reporterFullName, List<EMailAddress> notificationRecipients, 
			EMailAddress emailSender) {
		Map<String, Object> logObject = new LinkedHashMap<String, Object>();
		logObject.put("issue", issueKey);
		logObject.put("summary", summary);
		logObject.put("description", description);
		logObject.put("reporter", reporterFullName);
		logObject.put("notificationSender", emailSender);
		logObject.put("notificationRecipients", notificationRecipients);
		logObject.put("messageTemplate", notificationMessageTemplate);
		logger.log(LogProvider.INFO, String.format(
				"Issue %s is created. The following email addresses are notified: %s",
				issueKey, notificationRecipients), logObject);
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		if (properties == null) return;
		jiraAddress = Utils.updateStringProperty(properties, "jiraAddress");
		defaultEmailSender = Utils.updateStringProperty(properties, "defaultEmailSender");
		projectsToEmailSenders = Utils.updateConfigurableMapField(properties, 
				"projectToEmailSender", "<project>::<emailSenderEmailAddress>");
		externalWatchersCustomFieldId = Utils.updateStringProperty(properties, 
				"externalWatchersCustomFieldId");
		ignoreReporters = Utils.updateConfigurableListField(properties, "ignoreReporter");
		for (int i = 0; i < ignoreReporters.size(); i++)
			ignoreReporters.set(i, ignoreReporters.get(i).toLowerCase());
		notificationMessageTemplate = Utils.updateTemplateProperty(properties, 
				"pathToNotificationMessageTemplate");
	}
}
