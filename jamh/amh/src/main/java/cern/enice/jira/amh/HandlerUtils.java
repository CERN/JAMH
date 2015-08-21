package cern.enice.jira.amh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;

class HandlerUtils {
	
	static Map<String, Object> constructLogObject(String message) {
		return constructLogObject(message, null, null, null);
	}
	
	static Map<String, Object> constructLogObject(String message, EMail email) {
		return constructLogObject(message, email, null, null);
	}
	
	static Map<String, Object> constructLogObject(String message, 
			EMail email, IssueDescriptor issueDescriptor, Result result) {
		Map<String, Object> logObject = new HashMap<String, Object>();
		logObject.put("eventTimestamp", System.currentTimeMillis());
		logObject.put("message", message);
		if (email == null) return logObject;
		logObject.put("emailSubject", email.getSubject());
		logObject.put("emailBody", email.getBody());
		logObject.put("emailFrom", email.getFrom().toString());
		logObject.put("emailReplyTo", 
				emailAddressesToString(email.getReplyTo()));
		logObject.put("emailTo", 
				emailAddressesToString(email.getTo()));
		logObject.put("emailCc", 
				emailAddressesToString(email.getCc()));
		logObject.put("emailBcc", 
				emailAddressesToString(email.getBcc()));
		logObject.put("emailAttachments", 
				StringUtils.join(email.getAttachments(), ", "));
		if (result == null) return logObject;
		logObject.put("code", result.getCode().toString());
		logObject.put("issueKey", issueDescriptor.getKey());
		logObject.put("issueDescriptor", issueDescriptor);
		logObject.put("errors", getErrorList(result));
		return logObject;
	}
	
	static String emailAddressesToString(List<EMailAddress> emailAddresses) {
		if (emailAddresses == null) return null;
		List<String> emailAddressesList = new ArrayList<String>();
		for (EMailAddress emailAddress : emailAddresses) {
			emailAddressesList.add(emailAddress.toString());
		}
		return StringUtils.join(emailAddressesList, ", ");
	}
	
	private static List<String> getErrorList(Result result) {
		if (result == null) return null;
		List<String> errors = new ArrayList<String>();
		if (result.getErrors() != null) errors.addAll(result.getErrors());
		Map<String, String> errorFields = result.getFields();
		if (errorFields != null) {
			for (String field: errorFields.keySet()) {
				errors.add(field + ": " + errorFields.get(field));
			}
		}
		return errors;
	}
	
	static String constructRuleSetExceptionMessage(String ruleSetName, 
			IssueDescriptor issueDescriptor, EMail email) {
		String message;
		if (issueDescriptor.getKey() != null)
			message = String.format("Couldn't process rule set %s for issue %s", 
					ruleSetName, issueDescriptor.getKey());
		else if (issueDescriptor.getSummary() != null) {
			message = String.format(
					"Couldn't process rule set %s for new issue with summary %s", 
					ruleSetName, issueDescriptor.getSummary());
		} else {
			message = String.format(
					"Couldn't process rule set %s for email with subject %s", 
					ruleSetName, email.getSubject());
		}
		return message;
	}
}
