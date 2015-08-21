package cern.enice.jira.amh.baseruleset;

import java.util.List;
import java.util.Map;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.User;

public class RuleSetUtils {
	
	// Service dependencies
	private volatile JiraCommunicator jiraCommunicator;

	/**
	 * Tries to get a username corresponding to a given email address
	 * @param emailAddress   Given email address
	 * @return               Returns username in case given email address is found in JIRA or empty string otherwise
	 */
	public String getUsernameByEmailAddress(EMailAddress emailAddress) {
		String localPart = emailAddress.getLocalPart();
		String domain = emailAddress.getDomain();
		if (localPart == null || localPart.isEmpty() || domain == null || domain.isEmpty()) 
			return "";
		String emailAddressCleanedUp = localPart + "@" + domain;
		User userData = jiraCommunicator.getUser(emailAddressCleanedUp);
		if (userData != null) {
			String username = (String) userData.getName();
			return username == null ? "" : username;
		}
		userData = jiraCommunicator.getUser(localPart);
		if (userData != null) {
			String username = (String) userData.getName();
			return username == null ? "" : username;
		}
		return "";
	}
	
	/**
	 * Returns email address of the one who is supposed to be an email sender
	 * @param email
	 * @return        Returns REPLYTO header's value if it is specified, or FROM header's value otherwise
	 */
	public EMailAddress getEmailSender(EMail email) {
		List<EMailAddress> replyTo = email.getReplyTo();
		if (replyTo != null && !replyTo.isEmpty()) {
			return replyTo.get(0);
		} else {
			return email.getFrom();
		}
	}
	
	public String getDefaultValueForHandler(List<EMailAddress> emailRecipients, Map<String, String> handlerAddressesToDefaultValues) {
		for (EMailAddress recipient : emailRecipients) {
			String emailAddress = String.format("%s@%s", 
					recipient.getLocalPart(), recipient.getDomain());
			if (handlerAddressesToDefaultValues.containsKey(emailAddress))
				return handlerAddressesToDefaultValues.get(emailAddress);
		}
		return null;
	}
	
	public String getSubjectWithoutTokens(String subject) {
		int firstTokenPosition = subject.indexOf("#");
		String subjectWithoutTokens = subject;
		if (firstTokenPosition > -1)
			subjectWithoutTokens = subject.substring(0, firstTokenPosition);
		return subjectWithoutTokens;
	}
	
	public String getEmailBodyWithoutTokens(String body) {
		// Get email body ignoring the lines which begin with # character
		if (body == null) return null;
		String[] bodyStrings = body.replace("\r\n", "\n").split("\n");
		StringBuffer text = new StringBuffer();
		boolean isText = false;
		for (String bodyString : bodyStrings) {
			if (!isText && bodyString.startsWith("#"))
				continue;
			isText = true;
			text.append(bodyString + "\n");
		}
		return text.toString().trim();
	}
}
