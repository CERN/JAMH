package cern.enice.jira.listeners.pvss_license_request_listener;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;
import cern.enice.jira.amh.utils.MimeType;
import cern.enice.jira.amh.utils.Utils;

@Path("/send-pvss-license-request")
public class PvssLicenseRequestListener implements ManagedService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile MailService mailService;
	private volatile NetworkClient networkClient;

	// Configurable fields
	private String jiraAddress;
	private String defaultComponent;
	private String emailSenderAddress;
	private String licenseGeneratorEmailAddress;
	private String restUsername;
	private String restPassword;
	private String pvssLicenseRequestTemplate;
	
	private static final List<String> CUSTOM_FIELDS_TO_IGNORE = Arrays.asList(new String[]{"Rank", "Global Rank","Watchers","Time in Status","Rank (Obsolete)"});

	public void start() {
		logger.log(LogProvider.INFO, "PVSS License Request Listener is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "PVSS License Request Listener is stopped.");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testSendEmailNotification() {
		logger.log(LogProvider.INFO, "Hello from PVSS License Request Listener!");
		return "Hello from PVSS License Request Listener!";
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

	@SuppressWarnings("unchecked")
	public void process(String webhookJsonData) throws JsonParseException, JsonMappingException,
			IOException, ParseException {
		ObjectMapper objectMapper = new ObjectMapper(); 
		Map<String, Object> webhookData = objectMapper.readValue(webhookJsonData, Map.class);
		Map<String, Object> issue = (Map<String, Object>) webhookData.get("issue");
		Map<String, Object> issueExtendedData = getExtendedIssueData(issue);
		if (issueExtendedData == null) {
			logger.log(LogProvider.WARNING, 
					"Failed to obtain issue custom field names, " + 
					"therefore it becomes impossible to construct " + 
					"license request properly.");
			return;
		}
		LicenseRequestData licenseRequestData = PvssLicenseRequestListener.prepareLicenseRequestData(
				webhookData, issue, issueExtendedData);
		sendLicenseRequest(licenseRequestData);
		logLicenseRequest(licenseRequestData);
		addDefaultComponentToIssue(licenseRequestData);
	}

	Map<String, Object> getExtendedIssueData(Map<String, Object> issue) {
		try {
			String issueKey = (String) issue.get("key");
			HttpRequest request = new HttpRequest(HttpMethod.GET, 
					String.format("%s/rest/api/latest/issue/%s?expand=names", jiraAddress, issueKey), 
					restUsername, restPassword, null, ContentType.NONE, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			int responseStatus = response.getStatus();
			@SuppressWarnings("unchecked")
			Map<String, Object> issueExtendedData = (Map<String, Object>) response.getContent();
			if (responseStatus < 200 || responseStatus >= 300) {
				Map<String, Object> objectToLog = new HashMap<String, Object>();
				objectToLog.put("responseContent", issueExtendedData);
				logger.log(LogProvider.DEBUG, "Couldn't get custom fields names.", objectToLog);
				return null;
			}
			return issueExtendedData;
		} catch (Exception e) {
			logger.log(LogProvider.DEBUG, "Couldn't get custom fields names.", e);
		}
		return null;
	}
	
	void sendLicenseRequest(LicenseRequestData licenseRequestData) {
		String emailSubject = "[ICE Support] PVSS License Request " + 
				licenseRequestData.getIssueKey();
		String emailBody = String.format(pvssLicenseRequestTemplate, 
				licenseRequestData.getIssueKey(), licenseRequestData.getMessageId(), 
				licenseRequestData.getAssigneeEmail(), licenseRequestData.getCustomFields(), 
				licenseRequestData.getCreationDate(), licenseRequestData.getReporterDetails(), 
				licenseRequestData.getDescription(), licenseRequestData.getComment());
		List<EMailAddress> licenseGeneratorEmailAddressAsList = new ArrayList<EMailAddress>();
		licenseGeneratorEmailAddressAsList.add( mailService.parseEmailAddress(licenseGeneratorEmailAddress) );
		// Send email notification
		EMail email = new EMail();
		email.setFrom(mailService.parseEmailAddress(emailSenderAddress));
		email.setTo(licenseGeneratorEmailAddressAsList);
		email.setSubject(emailSubject);
		email.setMime(MimeType.PLAINTEXT);
		email.setBody(emailBody);
		mailService.sendEmail(email);
	}
	
	void logLicenseRequest(LicenseRequestData licenseRequestData) {
		// Additional data to pass to logger
		Map<String, Object> logData = new LinkedHashMap<String, Object>();
		logData.put("licenseGeneratorEmailAddress", licenseGeneratorEmailAddress);
		logData.put("issue", licenseRequestData.getIssueKey());
		logData.put("messageId", licenseRequestData.getMessageId());
		logData.put("description", licenseRequestData.getDescription());
		logData.put("comment", licenseRequestData.getComment());
		logData.put("customFields", licenseRequestData.getCustomFields());
		logData.put("assigneeEmail", licenseRequestData.getAssigneeEmail());
		logData.put("reporter", licenseRequestData.getReporterDetails());
		logData.put("created", licenseRequestData.getCreationDate());
		logData.put("emailSenderAddress", emailSenderAddress);
		// Write to log file
		logger.log(LogProvider.INFO,
				"License request initiated in issue " + licenseRequestData.getIssueKey() + " was sent", logData);
	}

	void addDefaultComponentToIssue(LicenseRequestData licenseRequestData) {
		for (Object component : licenseRequestData.getComponents()) {
			@SuppressWarnings("unchecked")
			String componentName = (String) ((Map<String, Object>) component).get("name");
			if (componentName.equalsIgnoreCase(defaultComponent)) 
				return;
		}
		String issueKey = licenseRequestData.getIssueKey();
		try {
			Map<String, Object> updateIssueComponentJsonData = 
					ListenerUtils.getIssueComponentUpdateData(defaultComponent);
			HttpRequest request = new HttpRequest(HttpMethod.PUT, 
					String.format("%s/rest/api/latest/issue/%s", jiraAddress, issueKey), 
					restUsername, restPassword, updateIssueComponentJsonData, 
					ContentType.JSON, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			if (response.getStatus() >= 200 && response.getStatus() < 300) return;
			logger.log(LogProvider.INFO, String.format(
					"Couldn't set component %s for issue %s.", defaultComponent, issueKey));
		} catch (Exception e) {
			logger.log(LogProvider.INFO, String.format(
					"Couldn't set component %s for issue %s.", defaultComponent, issueKey), e);
		}
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties != null) {
			jiraAddress = Utils.updateStringProperty(properties, "jiraAddress");
			defaultComponent = Utils.updateStringProperty(properties, "defaultComponent");
			emailSenderAddress = Utils.updateStringProperty(properties, "emailSenderAddress");
			licenseGeneratorEmailAddress = Utils.updateStringProperty(properties, "licenseGeneratorEmailAddress");
			restUsername = Utils.updateStringProperty(properties, "restUsername");
			restPassword = Utils.updateStringProperty(properties, "restPassword");
			pvssLicenseRequestTemplate = Utils.updateTemplateProperty(properties, "pathToNotificationMessageTemplate");
		}
	}
	
	void setJiraAddress(String jiraAddress) {
		this.jiraAddress = jiraAddress;
	}
	
	void setRestUsername(String restUsername) {
		this.restUsername = restUsername;
	}
	void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}
	void setNetworkClient(NetworkClient networkClient) {
		this.networkClient = networkClient;
	}

	@SuppressWarnings("unchecked")
	static final String getCustomFields(Map<String, Object> issueExtendedData) {
		StringBuffer customFieldsBuffer = new StringBuffer();
		Map<String, Object> issueFields = (Map<String, Object>) issueExtendedData.get("fields");
		Map<String, Object> fieldNames = (Map<String, Object>) issueExtendedData.get("names");
		for (Entry<String, Object> fieldName : fieldNames.entrySet()) {
			String fieldNameKey = fieldName.getKey();
			String customFieldValue = "";
			if (!fieldNameKey.startsWith("customfield")) continue;
			
			// Filter out any irrelevant fields
			if(CUSTOM_FIELDS_TO_IGNORE.contains(fieldName.getValue())) continue;
			
			Object customField = issueFields.get(fieldNameKey);
			if (customField == null) continue;
			if (customField instanceof List) {
				if(fieldName.getValue().equals("PVSS License Force Generation")){
					List<Map<String,String>> m = (List<Map<String,String>>) customField;
					if(m.size()>0){
						customFieldValue = m.get(0).get("value");
					}
				}else{
				  customFieldValue = StringUtils.join((ArrayList<String>) customField, ", ");
				}
			} else if (customField instanceof Map) {
				customFieldValue = (String) ((Map<String, Object>) customField).get("value");
			} else {
				customFieldValue = String.valueOf(customField);
			}
			customFieldsBuffer.append(String.format("%s : %s\n", fieldName.getValue(), customFieldValue));
		}
		return customFieldsBuffer.toString();
	}

	@SuppressWarnings("unchecked")
	static LicenseRequestData prepareLicenseRequestData(Map<String, Object> webhookData, 
			Map<String, Object> issue, Map<String, Object> issueExtendedData) throws ParseException {
		LicenseRequestData licenseRequestData = new LicenseRequestData();
		Map<String, Object> fields = new HashMap<String, Object>();
		if(issue.containsKey("fields")){
			fields.putAll((Map<String, Object>) issue.get("fields"));
		}
		String issueKey = (String) issue.get("key");
		licenseRequestData.setIssueKey(issueKey);
		licenseRequestData.setCustomFields( PvssLicenseRequestListener.getCustomFields(issueExtendedData) );
		if(fields.containsKey("created")){
			licenseRequestData.setCreationDate( ListenerUtils.formatCreationDate((String) fields.get("created")) );
		}
		licenseRequestData.setMessageId( DigestUtils.md5Hex(issueKey + "PVSSLic") );
		Map<String, Object> assignee = (Map<String, Object>) fields.get("assignee");
		licenseRequestData.setAssigneeEmail( assignee == null ? "" : (String) assignee.get("emailAddress") );
		licenseRequestData.setReporterDetails( ListenerUtils.getReporterDetails(fields) );
		String comment = (String) webhookData.get("comment");
		licenseRequestData.setComment( comment == null ? "" : comment );
		String description = (String) fields.get("description");
		licenseRequestData.setDescription( description == null ? "" : description );
		List<Map<String, Object>> components = (List<Map<String, Object>>) fields.get("components");
		licenseRequestData.setComponents(components);
		return licenseRequestData;
	}
}
