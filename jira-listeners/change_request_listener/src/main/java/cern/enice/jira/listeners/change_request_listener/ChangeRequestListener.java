package cern.enice.jira.listeners.change_request_listener;

import java.io.IOException;
import java.util.ArrayList;
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

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;
import cern.enice.jira.amh.utils.ResultCode;
import cern.enice.jira.amh.utils.Utils;

@Path("/create-change-request")
public class ChangeRequestListener implements ManagedService {

	// Service dependency
	private volatile LogProvider logger;
	private volatile NetworkClient networkClient;
	private volatile JiraCommunicator jiraCommunicator;

	// Configurable fields
	private String jiraAddress;
	//private String destinationProjectCustomFieldId;
	private Map<String, String> projectsToDestinationProjectCustomFieldIds;
	private String defaultDestinationProject;
	private String forceIssueType;
	private String restUsername;
	private String restPassword;
	private Map<String, List<String>> componentsToProjects = new HashMap<String, List<String>>();

	public void start() {
		logger.log(LogProvider.INFO, "Change Request Listener is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "Change Request Listener is stopped.");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testCreateChangeRequest() {
		logger.log(LogProvider.INFO, "Hello from Change Request Listener!");
		return "Hello from Change Request Listener!";
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createChangeRequest(String webhookJsonData) {
		try {
			process(webhookJsonData);
		} catch (Exception e) {
			logger.log(LogProvider.WARNING, "Couldn't process change request listener.");
			Map<String, Object> logObject = new HashMap<String, Object>();
			logObject.put("webhookJsonData", webhookJsonData);
			logger.log(LogProvider.DEBUG, "Couldn't process change request listener:", logObject, e);
		}
		return "";
	}

	public void process(String webhookJsonData) throws JsonParseException, JsonMappingException,
			IOException {
		// Prepare the data required to perform change request 
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> webhookData = (Map<String, Object>) objectMapper.readValue(
				webhookJsonData, Map.class);
		Map<String, Object> issue = (Map<String, Object>) webhookData.get("issue");
		String issueKey = (String) issue.get("key");
		Map<String, Object> issueFields = (Map<String, Object>) issue.get("fields");
		List<Map<String, Object>> components = (List<Map<String, Object>>) issueFields
				.get("components");
		// Get destination projects from custom field mapped to the current project key
		Map<String, Object> project = (Map<String, Object>)issueFields.get("project");
		String customFieldId = projectsToDestinationProjectCustomFieldIds.get(
				((String)project.get("key")).toLowerCase());
		List<Map<String, String>> overridenDestinationProjects = null;
		if (customFieldId != null && !customFieldId.isEmpty()) 
			overridenDestinationProjects = (List<Map<String, String>>) issueFields
					.get("customfield_" + customFieldId);
		// Get list of projects in which the given issue should be duplicated
		List<String> destinationProjects = getDestinationProjects(components, overridenDestinationProjects);
		// Duplicate issue in each destination project and create the link between the two 
		for (String destinationProject : destinationProjects) {
			String newIssueKey = duplicateIssue(issueKey, issueFields, destinationProject.toUpperCase());
			if (newIssueKey == null || newIssueKey.isEmpty())
				continue;
			createIssueLink(issueKey, newIssueKey);
		}
	}

	List<String> getDestinationProjects(List<Map<String, Object>> components,
			List<Map<String, String>> overridenDestinationProjects) {
		List<String> destinationProjects = new ArrayList<String>();
		if (components.isEmpty() && 
				(overridenDestinationProjects == null || overridenDestinationProjects.isEmpty())) {
			// No components or destination projects are set so use default destination project
			destinationProjects.add(defaultDestinationProject);
		} else if (overridenDestinationProjects != null && !overridenDestinationProjects.isEmpty()) {
			// Ignore components as destination projects are set explicitly
			for (Map<String, String> overridenDestinationProject : overridenDestinationProjects) {
				String[] parsedValue = overridenDestinationProject.get("value").split("-");
				String overridenProjectKey = parsedValue[0].trim();
				destinationProjects.add(overridenProjectKey);
			}
		} else {
			// Get destination projects associated with the issue components
			for (Map<String, Object> component : components) {
				String componentName = (String) component.get("name");
				List<String> projectsForComponent = componentsToProjects.get(componentName.toLowerCase());
				if (projectsForComponent != null && !projectsForComponent.isEmpty())
					destinationProjects.addAll(projectsForComponent);
			}
		}
		return destinationProjects;
	}
	
	private String getFieldProperty(Map<String, Object> fields, String fieldName, String propertyName) {
		Map<String, Object> field = (Map<String, Object>)fields.get(fieldName);
		if (field == null) return null;
		return (String)field.get(propertyName);
	}

	private String duplicateIssue(String issueKey, Map<String, Object> issueFields, String destinationProject) {
		try {
			// Prepare issue desciptor object to create new issue
			IssueDescriptor issueDescriptor = new IssueDescriptor();
			issueDescriptor.setSummary( (String)issueFields.get("summary") );
			issueDescriptor.setProject(destinationProject);
			// TEST If forceIssueType parameter is specified then set its value otherwise use original issue type
			String issueType = forceIssueType.isEmpty() ? 
					getFieldProperty(issueFields, "issuetype", "name") : forceIssueType;
			issueDescriptor.setIssueType(issueType);
			issueDescriptor.setReporter(getFieldProperty(issueFields, "assignee", "name"));
			issueDescriptor.setPriority(getFieldProperty(issueFields, "priority", "name"));
			issueDescriptor.setDescription( (String)issueFields.get("description") );
			// Create new issue
			Result result = jiraCommunicator.createIssue(issueDescriptor);
			if (result.getCode() == ResultCode.ISSUE_CREATED) {
				String newIssueKey = issueDescriptor.getKey();
				logger.log(LogProvider.INFO, 
						String.format("Issue %s was duplicated in %s project (new issue key: %s).", 
								issueKey, destinationProject, newIssueKey));
				return newIssueKey;
			} else {
				logger.log(LogProvider.INFO, 
						String.format("Failed to duplicate issue %s in %s project.", issueKey, destinationProject));
			}
		} catch (Exception e) {
			logger.log(LogProvider.INFO, 
					String.format("Failed to duplicate issue %s in %s project.", issueKey, destinationProject), e);
		}
		return null;
	}

	void createIssueLink(String originalIssueKey, String newIssueKey) {
		Map<String, Object> linkObject = new HashMap<String, Object>();
		Map<String, String> typeObject = new HashMap<String, String>();
		Map<String, String> inwardIssueObject = new HashMap<String, String>();
		Map<String, String> outwardIssueObject = new HashMap<String, String>();
		typeObject.put("name", "Change Request");
		inwardIssueObject.put("key", originalIssueKey);
		outwardIssueObject.put("key", newIssueKey);
		linkObject.put("type", typeObject);
		linkObject.put("inwardIssue", inwardIssueObject);
		linkObject.put("outwardIssue", outwardIssueObject);
		try {
			HttpRequest request = new HttpRequest(HttpMethod.POST, jiraAddress
					+ "/rest/api/latest/issueLink", restUsername, restPassword, linkObject,
					ContentType.JSON, ContentType.JSON);
			HttpResponse response = networkClient.request(request);
			if (response == null) return;
			int responseStatus = response.getStatus();
			if (responseStatus >= 200 && responseStatus < 300) {
				logger.log(LogProvider.INFO, String.format("Issue link was created between %s and %s", 
						originalIssueKey, newIssueKey));
				return;
			}
		} catch (Exception e) {
			logger.log(LogProvider.DEBUG, String.format("Couldn't create issue link between %s and %s.", 
					originalIssueKey, newIssueKey), e);
		}
		logger.log(LogProvider.INFO, String.format("Couldn't create issue link between %s and %s.", 
				originalIssueKey, newIssueKey));
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties != null) {
			jiraAddress = Utils.updateStringProperty(properties, "jiraAddress");
			projectsToDestinationProjectCustomFieldIds = Utils.updateConfigurableMapField(properties, 
					"projectToDestinationProjectCustomFieldId", "projectKey::customFieldId");
			defaultDestinationProject = Utils.updateStringProperty(
					properties, "defaultDestinationProject", "");
			forceIssueType = Utils.updateStringProperty(
					properties, "forceIssueType", "");
			restUsername = Utils.updateStringProperty(properties, "restUsername");
			restPassword = Utils.updateStringProperty(properties, "restPassword");
			componentsToProjects.clear();
			componentsToProjects.putAll(Utils.updateConfigurableMapOfListsField(
					properties, "componentToProject", "componentName::projectKey"));
		}
	}
}
