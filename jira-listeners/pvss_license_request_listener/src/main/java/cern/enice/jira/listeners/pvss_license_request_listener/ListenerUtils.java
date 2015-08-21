package cern.enice.jira.listeners.pvss_license_request_listener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ListenerUtils {
	static Map<String, Object> getIssueComponentUpdateData(String componentName) {
		Map<String, Object> add = new HashMap<String, Object>();
		add.put("name", componentName);
		Map<String, Object> component = new HashMap<String, Object>();
		component.put("add", add);
		List<Map<String, Object>> components = new ArrayList<Map<String, Object>>();
		components.add(component);
		Map<String, Object> update = new HashMap<String, Object>();
		update.put("components", components);
		Map<String, Object> issueData = new HashMap<String, Object>();
		issueData.put("update", update);
		return issueData;
	}
	
	static String getReporterDetails(Map<String, Object> fields) {
		@SuppressWarnings("unchecked")
		Map<String, Object> reporter = (Map<String, Object>) fields.get("reporter");
		if (reporter == null) return "Anonymous";
		return String.format("%s (%s)", 
				(String) reporter.get("displayName"), (String) reporter.get("emailAddress"));
	}
	
	static String formatCreationDate(String creationDate) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
		SimpleDateFormat jiraDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return simpleDateFormat.format(jiraDateFormat.parse( creationDate ));
	}
}
