package cern.enice.jira.amh.api;

import java.util.List;

import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;

public interface MailService {
	/**
	 * Collects email messages from the pre-configured mailbox using IMAP protocol
	 * @return List of email messages, each wrapped by JAMH API email wrapper
	 */
	public List<EMail> collectEmail();
	
	/**
	 * Sends an email using SMTP protocol 
	 * @param email Email message, wrapped by JAMH API email wrapper
	 * @return Returns true if email is sent, otherwise - false 
	 */
	public boolean sendEmail(EMail email);
	
	/**
	 * 
	 * @param emailAddress   email address to be parsed
	 * @return               parsed email address, wrapped by JAMH API email address wrapper
	 */
	public EMailAddress parseEmailAddress(String emailAddress);
}
