package cern.enice.jira.amh.jodd_mail_service;

import cern.enice.jira.amh.utils.MimeType;

class BodyAndMimeType {
	public String body;
	public MimeType mimeType;
	public BodyAndMimeType(String body, MimeType mimeType) {
		super();
		this.body = body;
		this.mimeType = mimeType;
	}
}
