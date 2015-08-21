package cern.enice.jira.amh.dto;

public class User {
	private String name;
	private String displayName;
	private String email;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public User(String name, String displayName, String email) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.email = email;
	}
}
