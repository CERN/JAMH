package cern.enice.jira.amh.jira_rest_communicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ResultCode;

class JiraCommunicatorUtils {

	/**
	 * Configure fields object which later will be transformed to JSON and sent within REST call
	 * @param fields
	 * @param issueDescriptor
	 */
	static void setFields(Map<String, Object> fields, IssueDescriptor issueDescriptor) {
		String summary = issueDescriptor.getSummary(); 
		String dueDate = issueDescriptor.getDueDate();
		String environment = issueDescriptor.getEnvironment();
		String description = issueDescriptor.getDescription();
		if (summary != null && !summary.isEmpty())
			fields.put("summary", summary);
		if (dueDate != null && !dueDate.isEmpty())
			fields.put("duedate", dueDate);
		if (environment != null && !environment.isEmpty())
			fields.put("environment", environment);
		if (description != null && !description.isEmpty())
			fields.put("description", description);
		fillSingleValueField(fields, "project", "key", issueDescriptor.getProject());
		fillSingleValueField(fields, "issuetype", "name", issueDescriptor.getIssueType());
		fillSingleValueField(fields, "assignee", "name", issueDescriptor.getAssignee());
		fillSingleValueField(fields, "reporter", "name", issueDescriptor.getReporter());
		fillSingleValueField(fields, "priority", "name", issueDescriptor.getPriority());
		fillSingleValueField(fields, "resolution", "name", issueDescriptor.getResolution());
		fillMultipleValuesField(fields, "components", "name", issueDescriptor.getComponents());
		fillMultipleValuesField(fields, "fixVersions", "name", issueDescriptor.getFixVersions());
		fillMultipleValuesField(fields, "versions", "name", issueDescriptor.getAffectedVersions());
		setTimeTracking(fields, issueDescriptor);
		setCustomFields(fields, issueDescriptor);
	}

	static void fillSingleValueField(Map<String, Object> fields, String fieldName, String valueTag, String value) {
		// Set given field with a single value
		if (value == null || value.isEmpty()) return;
		Map<String, Object> field = new HashMap<String, Object>();
		field.put(valueTag, value);
		fields.put(fieldName, field);
	}

	static void fillMultipleValuesField(Map<String, Object> fields, String fieldName, String valueTag, Set<String> values) {
		// Set given field with multiple values
		if (values == null || values.isEmpty()) return;
		List<Map<String, Object>> components = new ArrayList<Map<String, Object>>();
		for (String component : values) {
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put(valueTag, component);
			components.add(temp);
		}
		fields.put(fieldName, components);
	}
	
	static void setCustomFields(Map<String, Object> fields, IssueDescriptor issueDescriptor) {
		Map<String, Object> customFields = issueDescriptor.getCustomFields();
		if (customFields == null) return;
		for (String customFieldKey : customFields.keySet()) {
			Object customField = customFields.get(customFieldKey);
			fields.put(customFieldKey, customField);
//			if (customField instanceof String) {
//				fields.put(customFieldKey, (String)customField);
//			} else if (customField instanceof List) {
//				fields.put(customFieldKey, (List)customField);
//			}
		}
	}
	
	static void setTimeTracking(Map<String, Object> fields, IssueDescriptor issueDescriptor) {
		Map<String, Object> timeTracking = new HashMap<String, Object>();
		String originalEstimate = issueDescriptor.getOriginalEstimate();
		String remainingEstimate = issueDescriptor.getRemainingEstimate();
		if (originalEstimate != null && !originalEstimate.trim().isEmpty())
			timeTracking.put("originalEstimate", originalEstimate);
		if (remainingEstimate != null && !remainingEstimate.trim().isEmpty())
			timeTracking.put("remainingEstimate", remainingEstimate);
		if (!timeTracking.isEmpty())
			fields.put("timetracking", timeTracking);
	}

	@SuppressWarnings("unchecked")
	static void convertFieldValuesListToMap(String convertableField, String resultField, Map<String, Object> issuetype) {
		Map<String, Object> resultFieldValues = new HashMap<String, Object>();
		Map<String, Object> fields = (Map<String, Object>) issuetype.get("fields");
		if (fields.containsKey(convertableField)) {
			Map<String, Object> convertableFieldObject = (Map<String, Object>) fields.get(convertableField);
			List<Map<String, Object>> convertableFieldValuesList = (List<Map<String, Object>>) convertableFieldObject
					.get("allowedValues");
			for (Map<String, Object> convertableFieldValuesListElement : convertableFieldValuesList) {
				String convertableFieldValue = (String) convertableFieldValuesListElement.get("name");
				resultFieldValues.put(convertableFieldValue.toLowerCase(), convertableFieldValuesListElement);
			}
		}
		issuetype.put(resultField, resultFieldValues);
	}
	
	@SuppressWarnings("unchecked")
	static Result getResultWithErrorsForCode(Map<String, Object> responseContent, ResultCode code) {
		if (responseContent == null)
			return new Result(code);
		ArrayList<String> errorMessages = null;
		Map<String, String> errorFields = null;
		if (responseContent.containsKey("errorMessages")) {
			errorMessages = (ArrayList<String>) responseContent.get("errorMessages");
		}
		if (responseContent.containsKey("errors")) {
			errorFields = (Map<String, String>) responseContent.get("errors");
		}
		return new Result(code, errorMessages, errorFields);
	}

}
