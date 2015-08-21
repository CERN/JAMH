package cern.enice.jira.amh.dto;

import java.util.List;

import cern.enice.jira.amh.utils.MimeType;

public class EMail {
	private EMailAddress from = null;
	private List<EMailAddress> to = null;
	private List<EMailAddress> cc = null;
	private List<EMailAddress> bcc= null;
	private List<EMailAddress> replyTo = null;
	private String subject = null;
	private String body = null;
	private MimeType mime = MimeType.PLAINTEXT;
	private List<String> attachments = null;
	
	public EMailAddress getFrom() {
		return from;
	}
	public void setFrom(EMailAddress from) {
		this.from = from;
	}
	public List<EMailAddress> getTo() {
		return to;
	}
	public void setTo(List<EMailAddress> to) {
		this.to = to;
	}
	public List<EMailAddress> getCc() {
		return cc;
	}
	public void setCc(List<EMailAddress> cc) {
		this.cc = cc;
	}
	public List<EMailAddress> getBcc() {
		return bcc;
	}
	public void setBcc(List<EMailAddress> bcc) {
		this.bcc = bcc;
	}
	public List<EMailAddress> getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(List<EMailAddress> replyTo) {
		this.replyTo = replyTo;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public MimeType getMime() {
		return mime;
	}
	public void setMime(MimeType mime) {
		this.mime = mime;
	}
	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
}
