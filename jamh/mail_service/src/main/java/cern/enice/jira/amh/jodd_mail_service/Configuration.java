package cern.enice.jira.amh.jodd_mail_service;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.utils.Utils;

class Configuration implements ManagedService, ConfigurationMBean {
	
	// Service dependencies
	private volatile MailServiceUtils mailServiceUtils;
	
	// Configurable fields
	private List<MailAccount> imapAccounts = new ArrayList<MailAccount>();
	private MailAccount smtpAccount = new MailAccount();
	private String pathToAttachments;
	private List<String> sendersBlacklist = new ArrayList<String>();
	private Map<String, String> autoReplyHeaders = new HashMap<String, String>();

	MailServiceUtils getMailServiceUtils() {
		return mailServiceUtils;
	}

	void setMailServiceUtils(MailServiceUtils mailServiceUtils) {
		this.mailServiceUtils = mailServiceUtils;
	}
	
	public List<MailAccount> getImapAccounts() {
		return imapAccounts;
	}

	public void setImapAccounts(List<MailAccount> imapAccounts) {
		this.imapAccounts = imapAccounts;
	}

	public MailAccount getSmtpAccount() {
		return smtpAccount;
	}

	public void setSmtpAccount(MailAccount smtpAccount) {
		this.smtpAccount = smtpAccount;
	}

	public String getPathToAttachments() {
		return pathToAttachments;
	}

	public void setPathToAttachments(String pathToAttachments) {
		this.pathToAttachments = pathToAttachments;
	}

	public List<String> getSendersBlacklist() {
		return sendersBlacklist;
	}

	public void setSendersBlacklist(List<String> sendersBlacklist) {
		this.sendersBlacklist = sendersBlacklist;
	}

	public Map<String, String> getAutoReplyHeaders() {
		return autoReplyHeaders;
	}

	public void setAutoReplyHeaders(Map<String, String> autoReplyHeaders) {
		this.autoReplyHeaders = autoReplyHeaders;
	}

	@Override
	public String getSmtpAccountForJmx() {
		return String.format("Host: %s, port: %d, username: %s",
				smtpAccount.host, smtpAccount.port, smtpAccount.username);
	}

	@Override
	public String getImapAccountsForJmx() {
		StringBuffer imapAccountsString = new StringBuffer();
		for (MailAccount account: imapAccounts) {
			imapAccountsString.append(String.format("Host: %s, port: %d, username: %s\n",
					account.host, account.port, account.username));
		}
		return imapAccountsString.toString();
	}

	Map<String, String> updateAutoReplyHeaders(Dictionary<String, ?> properties) {
		int index = 0;
		Map<String, String> autoReplyHeaders = new HashMap<String, String>();
		while (properties.get("autoReplyHeader" + index) != null) {
			String autoReplyHeadersElement = (String) properties.get("autoReplyHeader" + index++);
			if (autoReplyHeadersElement != null && !autoReplyHeadersElement.isEmpty()) {
				String[] headerAndValue = autoReplyHeadersElement.split(":");
				if (headerAndValue.length > 1)
					autoReplyHeaders.put(headerAndValue[0], headerAndValue[1]);
				else
					autoReplyHeaders.put(headerAndValue[0], null);
			}
		}
		return autoReplyHeaders;
	}
	
	List<MailAccount> updateImapAccounts(Dictionary<String, ?> properties) 
			throws ConfigurationException {
		int index = 0;
		List<MailAccount> imapAccounts = new ArrayList<MailAccount>();
		while (properties.get("imapUsername" + index) != null) {
			imapAccounts.add(
					buildMailAccount(properties,
							"imapHost" + index, "imapPort" + index, 
							"imapUsername" + index, "imapPassword" + index));
			index++;
		}
		if (imapAccounts.isEmpty())
			throw new ConfigurationException("", "At least one imap account should be specified"); 
		return imapAccounts;
	}
	
	MailAccount buildMailAccount(Dictionary<String, ?> properties,
			String hostPropertyName, String portPropertyName, 
			String usernamePropertyName, String passwordPropertyName) 
					throws ConfigurationException {
		String host = Utils.updateStringProperty(properties, hostPropertyName);
		String port = Utils.updateStringProperty(properties, portPropertyName);
		String username = Utils.updateStringProperty(properties, usernamePropertyName);
		String password = Utils.updateStringProperty(properties, passwordPropertyName);
		int portNumber;
		try {
			portNumber = Integer.parseInt(port);
		} catch (Exception e) {
			throw new ConfigurationException(portPropertyName, "property is not a valid number");
		}
		return new MailAccount(host, portNumber, username, password);
	}
	
	List<String> updateSendersBlacklist(Dictionary<String, ?> properties) {
		List<String> sendersBlacklist = Utils.updateConfigurableListField(
				properties, "sendersBlacklist");
		for (int i = 0; i < sendersBlacklist.size(); i++) 
			sendersBlacklist.set(i, sendersBlacklist.get(i).toLowerCase());
		return sendersBlacklist;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties == null) return;
		if (mailServiceUtils != null)
			mailServiceUtils.closeMailSessions(imapAccounts);
		smtpAccount = buildMailAccount(properties,
				"smtpHost", "smtpPort", "smtpUsername", "smtpPassword");
		pathToAttachments = Utils.updateStringProperty(properties, "pathToAttachments");
		sendersBlacklist.clear();
		sendersBlacklist.addAll(
				updateSendersBlacklist(properties));
		autoReplyHeaders.clear();
		autoReplyHeaders.putAll(
				updateAutoReplyHeaders(properties));
		imapAccounts.clear();
		imapAccounts.addAll(
				updateImapAccounts(properties));
	}

}
