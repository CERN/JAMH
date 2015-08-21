package cern.enice.jira.amh.jodd_mail_service;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags.Flag;

import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailFilter;
import jodd.mail.MailException;
import jodd.mail.ReceivedEmail;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;

public class JoddMailService implements MailService {

	// Service dependencies
	private volatile LogProvider logger;
	private volatile Configuration configuration;
	private volatile MailServiceUtils mailServiceUtils;
	
	LogProvider getLogger() {
		return logger;
	}

	void setLogger(LogProvider logger) {
		this.logger = logger;
	}
	
	Configuration getConfiguration() {
		return configuration;
	}

	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	MailServiceUtils getMailServiceUtils() {
		return mailServiceUtils;
	}

	void setMailServiceUtils(MailServiceUtils mailServiceUtils) {
		this.mailServiceUtils = mailServiceUtils;
	}

	public void start() {
		logger.log(LogProvider.INFO, "Mail service is started.");
	}

	public void stop() {
		mailServiceUtils.closeMailSessions(
				configuration.getImapAccounts());
		logger.log(LogProvider.INFO, "Mail service is stopped.");
	}

	@Override
	public List<EMail> collectEmail() {
		ReceivedEmail[] receivedEmails = null;
		List<EMail> emails = new ArrayList<EMail>();
		for (MailAccount imapAccount : configuration.getImapAccounts()) {
			mailServiceUtils.useMailSession(imapAccount);
			try {
				imapAccount.session.getNewMessageCount();
				EmailFilter filter = new EmailFilter();
				filter.flag(Flag.SEEN, false);
				receivedEmails = imapAccount.session.receiveEmailAndMarkSeen(filter);
			} catch (MailException e) {
				logger.log(LogProvider.WARNING, 
						String.format("Error occured while trying to fetch email " + 
								"from mailbox of account %s: %s. " + 
								"Trying to restart mailbox connection...", 
								imapAccount.toString(), e.getMessage()));
				mailServiceUtils.closeMailSession(imapAccount);
			}
			if (receivedEmails == null || receivedEmails.length == 0)
				continue;
			for (ReceivedEmail receivedEmail : receivedEmails) {
				if (!isEmailAllowed(receivedEmail)) continue;
				emails.add(convertToEMail(receivedEmail));
			}
		}
		return emails;
	}
	
	@Override
	public EMailAddress parseEmailAddress(String emailAddress) {
		EmailAddress parsedEmailAddress = new EmailAddress(emailAddress);
		if (!parsedEmailAddress.isValid()) return null;
		return new EMailAddress(parsedEmailAddress.getPersonalName(), 
				parsedEmailAddress.getLocalPart(), parsedEmailAddress.getDomain());
	}
	
	List<EMailAddress> parseEmailAddresses(String[] emailAddresses) {
		List<EMailAddress> emailAddressesList = new ArrayList<EMailAddress>();
		for (String emailAddress : emailAddresses) {
			emailAddressesList.add(parseEmailAddress(emailAddress));
		}
		return emailAddressesList;
	}

	/**
	 * Converts Jodd's ReceivedEmail object to the internal JAMH email wrapper
	 * @param receivedEmail Jodd email wrapper object
	 * @return JAMH email wrapper object
	 */
	EMail convertToEMail(ReceivedEmail receivedEmail) {
		String from = receivedEmail.getFrom();
		String subject = receivedEmail.getSubject() != null ? receivedEmail.getSubject() : "";
		EMail email = new EMail();
		email.setFrom(parseEmailAddress(from));
		email.setTo(parseEmailAddresses(receivedEmail.getTo()));
		email.setCc(parseEmailAddresses(receivedEmail.getCc()));
		email.setBcc(parseEmailAddresses(receivedEmail.getBcc()));
		email.setReplyTo(parseEmailAddresses(receivedEmail.getReplyTo()));
		email.setSubject(subject);
		BodyAndMimeType bamt = mailServiceUtils.getBestBodyPart(receivedEmail);
		email.setBody(bamt.body);
		email.setMime(bamt.mimeType);
		// Log a warning that email contains an empty body
		if (email.getBody() == null || email.getBody().isEmpty()) {
			String unrecognizedBodyWarning = String.format(
					"Email from %s with subject \"%s\" doesn't contain plain text " + 
							"or HTML body part. Proceed with empty body.", 
					from, subject);
			logger.log(LogProvider.WARNING, unrecognizedBodyWarning);
		}
		email.setAttachments(
				mailServiceUtils.prepareAttachments(receivedEmail, 
						configuration.getPathToAttachments()));
		return email;
	}
	
	/**
	 * Checks if an email is not auto-generated or sent by a blacklisted sender  
	 * @param receivedEmail Jodd email wrapper object
	 * @return in case email is auto-generated returns true, otherwise - false
	 */
	boolean isEmailAllowed(ReceivedEmail receivedEmail) {
		// Check if sender is blacklisted
		String from = receivedEmail.getFrom();
		String subject = receivedEmail.getSubject();
		for (String blacklistedSender : configuration.getSendersBlacklist()) {
			if (!from.toLowerCase().contains(blacklistedSender)) continue;
			logger.log(LogProvider.WARNING, String.format(
					"Ignore email from %s with subject \"%s\" as " + 
							"its sender is blacklisted.", 
					from, subject));
			return false;
		}
		if (mailServiceUtils.hasAutoReplyHeader(receivedEmail, 
				configuration.getAutoReplyHeaders())) 
			return false;
		// Check if message contains multipart/report content type
		String emailContentType = receivedEmail.getHeader("Content-Type");
		if (emailContentType.contains("multipart/report")) {
			logger.log(LogProvider.WARNING, String.format(
					"Ignore email from %s with subject \"%s\" as " + 
							"it is a bounced message (multipart/report).", 
					from, subject));
			return false;
		}
		return true;
	}

	@Override
	public boolean sendEmail(EMail email) {
		boolean emailIsSent = false;
		Email emailToSend = mailServiceUtils.prepareEmailForSending(email);
		SendMailSession session = null;
		try {
			MailAccount smtpAccount = configuration.getSmtpAccount();
			SmtpServer smtpServer = new SmtpServer(smtpAccount.host, smtpAccount.port, 
					smtpAccount.username, smtpAccount.password);
			session = smtpServer.createSession();
			session.open();
			session.sendMail(emailToSend);
			emailIsSent = true;
		} catch (MailException ex) {
			logger.log(LogProvider.WARNING, "Failed to send email.", email, ex);
			emailIsSent = false;
		} finally {
			if (session != null) session.close();
		}
		return emailIsSent;
	}
}