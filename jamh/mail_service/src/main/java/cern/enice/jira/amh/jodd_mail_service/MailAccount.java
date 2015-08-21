package cern.enice.jira.amh.jodd_mail_service;

import java.util.Objects;

import jodd.mail.ReceiveMailSession;

class MailAccount {
	public String host;
	public int port;
	public String username;
	public String password;
	public ReceiveMailSession session = null;
	public MailAccount() { };
	public MailAccount(String host, int port, String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	@Override
	public String toString() {
		return String.format("(%s@%s:%d)", username, host, port);
	}
	@Override
	public int hashCode() {
		return Objects.hash(host, port, username, password);
	}
	@Override
	public boolean equals(Object object) {
		return Objects.equals(this, object);
	}
}
