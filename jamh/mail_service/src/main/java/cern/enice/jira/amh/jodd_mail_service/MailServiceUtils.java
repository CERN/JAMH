package cern.enice.jira.amh.jodd_mail_service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ImapSslServer;
import jodd.mail.MailException;
import jodd.mail.ReceiveMailSessionProvider;
import jodd.mail.ReceivedEmail;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.utils.MimeType;

class MailServiceUtils {
	
	// Service dependencies
	private volatile LogProvider logger;
		
	public LogProvider getLogger() {
		return logger;
	}

	public void setLogger(LogProvider logger) {
		this.logger = logger;
	}

	public void start() {
		logger.log(LogProvider.INFO, "MailServiceUtils service is started.");
	}

	public void stop() {
		logger.log(LogProvider.INFO, "MailServiceUtils service is stopped.");
	}

	Email prepareEmailForSending(EMail email) {
		Email emailToSend = new Email();
		String[] to = new String[email.getTo().size()];
		List<EMailAddress> emailAddresses = email.getTo();
		for (int i = 0; i < emailAddresses.size(); i++) {
			if (emailAddresses.get(i) == null) continue;
			to[i] = emailAddresses.get(i).toString();
		}
		emailToSend.to(to);
		emailToSend.from(email.getFrom().toString());
		emailToSend.subject(email.getSubject());
		if (email.getMime() == MimeType.HTML)
			emailToSend.addHtml(email.getBody());
		else
			emailToSend.addText(email.getBody());
		return emailToSend;
	}
	
	BodyAndMimeType findBodyOfGivenMimeType(List<EmailMessage> messages, 
			String mimeTypeString, MimeType mimeTypeToSet) {
		for (EmailMessage message : messages) {
			if (message.getMimeType().equals(mimeTypeString)) 
				return new BodyAndMimeType(message.getContent(), mimeTypeToSet);
		}
		return null;
	}
	
	/**
	 * Tries to use plain text email body part first, then HTML body part
	 * @param receivedEmail
	 * @return TODO
	 */
	BodyAndMimeType getBestBodyPart(ReceivedEmail receivedEmail){
		List<EmailMessage> messages = receivedEmail.getAllMessages();
		BodyAndMimeType plainText = findBodyOfGivenMimeType(messages, "text/plain", MimeType.PLAINTEXT);
		if (plainText == null)
			return findBodyOfGivenMimeType(messages, "text/html", MimeType.HTML);
		return plainText;
	}
	
	List<String> prepareAttachments(ReceivedEmail receivedEmail, String pathToAttachments) {
		List<EmailAttachment> attachments = receivedEmail.getAttachments();
		if (attachments == null || attachments.isEmpty()) return null;
		List<String> attachedFiles = new ArrayList<String>(attachments.size());
		for (EmailAttachment attachment : attachments) {
			String attachmentName = attachment.getName();
			if (attachmentName == null || attachmentName.isEmpty()) {
				attachmentName = "attachment"; 
			}
			File attachmentFile = new File(pathToAttachments, attachmentName);
			if (attachmentFile.exists() && !attachmentFile.isDirectory()) {
				attachmentName = String.format("%s-%s", 
						receivedEmail.getMessageNumber(), attachmentName);
				attachmentFile = new File(pathToAttachments, attachmentName);
			}
			attachment.writeToFile(attachmentFile);
			attachedFiles.add(attachmentFile.getAbsolutePath());
		}
		return attachedFiles;
	}
	
	/**
	 * Checks if an email contains an autoreply header
	 * @param receivedEmail
	 * @return
	 */
	boolean hasAutoReplyHeader(ReceivedEmail receivedEmail, Map<String, String> autoReplyHeaders) {
		for (String autoReplyHeader : autoReplyHeaders.keySet()) {
			String headerValue = receivedEmail.getHeader(autoReplyHeader);
			if (headerValue == null) continue;
			String autoReplyHeaderValue = autoReplyHeaders.get(autoReplyHeader);
			if (autoReplyHeaderValue == null || autoReplyHeaderValue.isEmpty() || 
					autoReplyHeaderValue.equalsIgnoreCase(headerValue)) {
				logger.log(LogProvider.WARNING, String.format(
						"Ignore email from %s with subject \"%s\" "
								+ "as it is an autoreply (header \"%s\" is found).", 
						receivedEmail.getFrom(), receivedEmail.getSubject(), 
						autoReplyHeader));
				return true;
			}
		}
		return false;
	}
	
	void useMailSession(MailAccount imapAccount) {
		if (imapAccount.session != null) return;
		try {
			ReceiveMailSessionProvider imapServer = new ImapSslServer(imapAccount.host, 
					imapAccount.port, imapAccount.username, imapAccount.password);
			imapAccount.session = imapServer.createSession();
			imapAccount.session.open();
			logger.log(LogProvider.INFO, 
					String.format("Mailbox session for account %s is opened.", 
							imapAccount.toString()));
		} catch (MailException e) {
			logger.log(LogProvider.WARNING, 
					String.format("Error occured while trying to open " + 
							"mailbox of account %s: %s", 
							imapAccount.toString(), e.getMessage()));
		}
	}
	
	void closeMailSession(MailAccount imapAccount) {
		try {
			imapAccount.session.close();
		} catch (MailException e) {
			if (logger == null) return;
			logger.log(LogProvider.WARNING, 
					String.format("Error occured while trying to close " + 
							"mailbox of account %s: %s", 
							imapAccount.toString(), e.getMessage()));
		} finally {
			imapAccount.session = null;
		}
	}
	
	void closeMailSessions(List<MailAccount> imapAccounts) {
		for (MailAccount imapAccount : imapAccounts) {
			closeMailSession(imapAccount);
		}
	}
	
}
