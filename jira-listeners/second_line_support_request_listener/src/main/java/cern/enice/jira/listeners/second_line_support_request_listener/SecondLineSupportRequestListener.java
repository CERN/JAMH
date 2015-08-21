package cern.enice.jira.listeners.second_line_support_request_listener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
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

@Path("/send-second-line-support-request-email-notification")
public class SecondLineSupportRequestListener implements ManagedService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile MailService mailService;

	// Configurable fields
	private String jiraAddress;
	private String emailSenderAddress;
	private Map<String, List<String>> componentsToEmails = new HashMap<String, List<String>>();
	private String notificationMessageTemplate;

	public void start() {
		logger.log(LogProvider.INFO, "Second Line Support Request Listener is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Second Line Support Request Listener is stopped.");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testSendEmailNotification() {
		logger.log(LogProvider.INFO, "Hello from Second Line Support Request Listener!");
		return "Hello from Second Line Support Request Listener!";
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String sendEmailNotification(String webhookJsonData) {
		try {
			process(webhookJsonData);
		} catch (Exception e) {
			logger.log(LogProvider.WARNING, "Couldn't process second line support request listener.");
			Map<String, Object> logObject = new HashMap<String, Object>();
			logObject.put("webhookJsonData", webhookJsonData);
			logger.log(LogProvider.DEBUG, "Couldn't process second line support request listener:", logObject, e);
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public void process(String webhookJsonData) throws JsonParseException, 
			JsonMappingException, IOException, ParseException {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> webhookData = objectMapper.readValue(webhookJsonData, Map.class);
		Map<String, Object> issue = (Map<String, Object>) webhookData.get("issue");
		Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
		Map<String, Object> assignee = (Map<String, Object>) fields.get("assignee");
		ArrayList<Map<String, Object>> components = 
				(ArrayList<Map<String, Object>>) fields.get("components");
		SupportRequestData data = prepareSecondLineSupportRequestData(issue, fields);
		// If assignee is set or no components are set then stop
		if (assignee != null || components == null || components.isEmpty()) return;
		// Send email notifications to the email addresses associated with issue component
		for (Map<String, Object> component : components) {
			String componentName = (String) ((Map<String, Object>) component).get("name");
			componentName = componentName.toLowerCase();
			List<EMailAddress> emailAddressesToNotify = 
					getEmailAddressesAssociatedToComponent(componentName);
			// If there is nobody to notify then go to the next component 
			if (emailAddressesToNotify == null || emailAddressesToNotify.isEmpty())
				continue;
			sendNotification(data, componentName, emailAddressesToNotify);
			logSecondLineSupportRequest(data, componentName, emailAddressesToNotify);
		}
	}
	
	SupportRequestData prepareSecondLineSupportRequestData(
			Map<String, Object> issue, Map<String, Object> fields) throws ParseException {
		SupportRequestData data = new SupportRequestData();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
		SimpleDateFormat jiraDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		data.setIssueKey( (String) issue.get("key") );
		data.setSummary( (String) fields.get("summary") );
		data.setDescription( (String) fields.get("description") );
		String created = (String) fields.get("created");
		data.setCreationDate( simpleDateFormat.format(jiraDateFormat.parse(created)) );
		@SuppressWarnings("unchecked")
		Map<String, Object> priority = (Map<String, Object>) fields.get("priority");
		data.setPriorityName( (String) priority.get("name") );
		@SuppressWarnings("unchecked")
		Map<String, Object> reporter = (Map<String, Object>) fields.get("reporter");
		String reporterDetails = String.format("%s (%s)", 
				(String) reporter.get("displayName"), (String) reporter.get("emailAddress"));
		data.setReporterDetails(reporterDetails);
		return data;
	}
	
	List<EMailAddress> getEmailAddressesAssociatedToComponent(String componentName) {
		List<EMailAddress> emailAddressesToNotify = new ArrayList<EMailAddress>(); 
		if (!componentsToEmails.containsKey(componentName)) 
			return Collections.emptyList();
		for (String emailAddress : componentsToEmails.get(componentName)) {
			if (emailAddress == null || emailAddress.trim().isEmpty()) continue;
			emailAddressesToNotify.add(mailService.parseEmailAddress(emailAddress.trim()));
		}
		return emailAddressesToNotify;
	}
	
	void sendNotification(SupportRequestData data, String componentName, 
			List<EMailAddress> emailAddressesToNotify) {
		String issueKey = data.getIssueKey();
		String summary = data.getSummary();
		String emailSubject = issueKey + " " + summary;
		String emailBody = String.format(notificationMessageTemplate, 
				summary, issueKey, jiraAddress, issueKey, componentName, summary, 
				data.getPriorityName(), data.getCreationDate(), 
				data.getReporterDetails(), data.getDescription());
		EMail email = new EMail();
		email.setFrom(mailService.parseEmailAddress(emailSenderAddress));
		email.setTo(emailAddressesToNotify);
		email.setSubject(emailSubject);
		email.setBody(emailBody);
		email.setMime(MimeType.HTML);
		mailService.sendEmail(email);
	}
	
	void logSecondLineSupportRequest(SupportRequestData data, String componentName, 
			List<EMailAddress> emailAddressesToNotify) {
		Map<String, Object> logData = new HashMap<String, Object>();
		logData.put("issue", data.getIssueKey());
		logData.put("summary", data.getSummary());
		logData.put("component", componentName);
		logData.put("priority", data.getPriorityName());
		logData.put("creationDate", data.getCreationDate());
		logData.put("reporter", data.getReporterDetails());
		logData.put("description", data.getDescription());
		logData.put("emailAddressesToNotify", emailAddressesToNotify);
		logData.put("emailSenderAddress", emailSenderAddress);
		logger.log(LogProvider.INFO, 
				"Second line support request was triggered from " + data.getIssueKey(), 
				logData);
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties != null) {
			jiraAddress = Utils.updateStringProperty(properties, "jiraAddress");
			emailSenderAddress = Utils.updateStringProperty(properties, "emailSenderAddress");
			componentsToEmails.clear();
			componentsToEmails.putAll(Utils.updateConfigurableMapOfListsField(
					properties, "componentToEmail", "componentName::emailAddress"));
			notificationMessageTemplate = Utils.updateTemplateProperty(properties, "pathToNotificationMessageTemplate");
		}
	}

}