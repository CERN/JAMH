package cern.enice.jira.listeners.issue_update_listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.User;
import cern.enice.jira.amh.utils.MimeType;
import cern.enice.jira.amh.utils.Utils;

@Path("/send-issue-updated-email-notification")
public class IssueUpdateListener implements ManagedService {
	
	private static final String USER_REFERENCE_PATTERN = "\\[~.*?\\]";
	private static final String BREAK_LINE = "\r\n";
	private static final String HTML_BREAK_LINE = "<br/>";
	
	// Service dependencies
	private volatile LogProvider logger;
	private volatile MailService mailService;
	private volatile JiraCommunicator jiraCommunicator;

	// Configurable fields
	private String jiraUserProfileUrl;
	private String emailSenderAddress;
	private String externalWatchersCustomFieldId;
	private String userReferencePattern;
	private String notificationMessageTemplate;
	private List<String> jiraMarkupPatterns = new ArrayList<String>();
	
	private ObjectMapper objectMapper = new ObjectMapper();

	public LogProvider getLogger() {
		return logger;
	}

	public void setLogger(LogProvider logger) {
		this.logger = logger;
	}

	public MailService getMailService() {
		return mailService;
	}

	void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void start() {
		logger.log(LogProvider.INFO, "Issue Update Listener is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Issue Update Listener is stopped.");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testSendEmailNotification() {
		logger.log(LogProvider.DEBUG, "Hello from Issue Update Listener!");
		return "Hello from Issue Update Listener!";
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String sendEmailNotification(String webhookJsonData) {
		try {
			process(webhookJsonData);
		} catch (Exception e) {
			logger.log(LogProvider.WARNING, e.getMessage());
		}
		return "";
	}

	public void process(String webhookJsonData) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> webhookData = objectMapper.readValue(webhookJsonData, Map.class);
		Map<String, Object> issue = (Map<String, Object>) webhookData.get("issue");
		Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
		String issueKey = (String) issue.get("key");
		String summary = (String) fields.get("summary");
		String externalWatchers = (String) fields.get("customfield_" + externalWatchersCustomFieldId);
		if (externalWatchers == null || externalWatchers.trim().isEmpty()) return;
		List<EMailAddress> externalWatchersList = 
				convertToEmailAddressList(externalWatchers.replace(";", ","));
		Map<String, Object> comment = (Map<String, Object>) webhookData.get("comment");
		Map<String, Object> changelog = (Map<String, Object>) webhookData.get("changelog");
		List<Field> updatedFields = getUpdatedFields(changelog);
		sendNotification(issueKey, summary, updatedFields, comment, externalWatchersList);
		logNotification(issueKey, summary, updatedFields, comment, externalWatchersList);
	}
	
	List<Field> getUpdatedFields(Map<String, Object> changelog) {
		ArrayList<Map<String, Object>> items = null;
		if (changelog != null)
			items = (ArrayList<Map<String, Object>>) changelog.get("items");
		if (items == null) return Collections.<Field>emptyList();
		List<Field> updatedFields = new ArrayList<Field>();
		for (Map<String, Object> item : items) {
			String oldValue = item.get("fromString") == null ? "Not set" : (String) item.get("fromString");
			String newValue = item.get("toString") == null ? "Not set" : (String) item.get("toString");
			updatedFields.add( new Field((String) item.get("field"), oldValue, newValue) );
		}
		return updatedFields;
	}
	
	List<EMailAddress> convertToEmailAddressList(String externalWatchers) {
		String[] externalWatchersArray = externalWatchers.split(",");
		List<EMailAddress> externalWatchersSet = new ArrayList<EMailAddress>();
		for (String externalWatcher : externalWatchersArray) {
			EMailAddress emailAddress = mailService.parseEmailAddress(externalWatcher.trim()); 
			if (emailAddress == null) continue;
			externalWatchersSet.add(emailAddress);
		}
		return externalWatchersSet;
	}
	
	void sendNotification(String issueKey, String summary, List<Field> updatedFields, 
			Map<String, Object> comment, List<EMailAddress> externalWatchers) {
		String emailSubject = issueKey + " " + summary;
		String emailBody = String.format(notificationMessageTemplate, 
				updatedFieldsAsHtml(updatedFields), 
				commentAsHtml(comment), emailSenderAddress, issueKey);
		EMail notificationEmail = new EMail();
		notificationEmail.setFrom(mailService.parseEmailAddress(emailSenderAddress));
		notificationEmail.setTo(externalWatchers);
		notificationEmail.setSubject(emailSubject);
		notificationEmail.setBody(emailBody);
		notificationEmail.setMime(MimeType.HTML);
		mailService.sendEmail(notificationEmail);
	}
	
	void logNotification(String issueKey, String summary, List<Field> updatedFields, 
			Map<String, Object> comment, List<EMailAddress> externalWatchers) {
		List<String> externalWatchersList = new ArrayList<String>();
		for (EMailAddress externalWatcher : externalWatchers) {
			externalWatchersList.add(externalWatcher.toString());
		}
		Map<String, Object> logObject = new HashMap<String, Object>();
		logObject.put("issueKey", issueKey);
		logObject.put("summary", summary);
		logObject.put("updatedFields", updatedFields);
		logObject.put("comment", comment == null ? "Not commented" : comment);
		logObject.put("externalWatchers", externalWatchersList);
		logger.log(LogProvider.INFO, "Issue followed by external watchers is updated:", logObject);
	}
	
	String commentAsHtml(Map<String, Object> comment) {
		if (comment == null || comment.isEmpty()) return "";
		Map<String, Object> author = (Map<String, Object>) comment.get("author");
		String commentBody = ((String) comment.get("body")).replace(BREAK_LINE, HTML_BREAK_LINE);
		commentBody = replaceJiraMarkup(commentBody);
		commentBody = replaceUserReferences(commentBody);
		return String.format("<strong>%s wrote:</strong><br/><br/>%s", 
				(String) author.get("displayName"), commentBody);
	}
	
	String replaceJiraMarkup(String text) {
		Pattern pattern;
		Matcher matcher;
		if (jiraMarkupPatterns == null || jiraMarkupPatterns.isEmpty()) return text;
		for (String jiraMarkupPattern : jiraMarkupPatterns) {
			pattern = Pattern.compile(jiraMarkupPattern);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				text = text.replace(matcher.group(), "");
			}
		}
		return text;
	}
	
	String replaceUserReferences(String text) {
		Pattern pattern = Pattern.compile(userReferencePattern);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			try {
				String userReference = matcher.group();
				String username = userReference.substring(2, userReference.length() - 1);
				User userObject = jiraCommunicator.getUser(username);
				if (userObject == null) continue;
				String displayName = userObject.getDisplayName();
				String userUrl = String.format(jiraUserProfileUrl, username);
				text = text.replace(userReference, 
						String.format("<a href=\"%s\">%s</a>", userUrl, displayName));
			} catch (Exception e) {
				// Do nothing
			}
		}
		return text;
	}

	String updatedFieldsAsHtml(List<Field> updatedFields) {
		if (updatedFields == null || updatedFields.isEmpty()) return "";
		StringBuffer updatedFieldsStringBuffer = new StringBuffer();
		updatedFieldsStringBuffer.append("<h2>Updated fields are:</h2><br/><br/>");
		// Construct a string with key-value pairs of updated fields
		for (Field updatedField : updatedFields) {
			updatedFieldsStringBuffer.append(
					String.format("<strong><u>%s:</u></strong><br/>" +
							"<i>From</i> %s <i>to</i> %s<br/><br/>", 
							updatedField.getField(),
							appendUpdatedFieldValue(updatedField.getOldValue()),
							appendUpdatedFieldValue(updatedField.getNewValue())
					)
			);
		}
		return updatedFieldsStringBuffer.toString();
	}
	
	String appendUpdatedFieldValue(String updatedFieldValue) {
		if (!updatedFieldValue.contains(BREAK_LINE))
			return updatedFieldValue;
		return String.format("<blockquote>%s</blockquote>", 
				updatedFieldValue.replace(BREAK_LINE, HTML_BREAK_LINE));
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		if (properties == null) return;
		jiraUserProfileUrl = Utils.updateStringProperty(properties, "jiraUserProfileUrl");
		userReferencePattern = Utils.updateStringProperty(properties, "userReferencePattern", USER_REFERENCE_PATTERN);
		emailSenderAddress = Utils.updateStringProperty(properties, "emailSenderAddress");
		externalWatchersCustomFieldId = Utils.updateStringProperty(properties, "externalWatchersCustomFieldId");
		jiraMarkupPatterns.clear();
		jiraMarkupPatterns.addAll(Utils.updateConfigurableListField(properties, "jiraMarkupPattern"));
		notificationMessageTemplate = Utils.updateTemplateProperty(properties, "pathToNotificationMessageTemplate");
	}
}
