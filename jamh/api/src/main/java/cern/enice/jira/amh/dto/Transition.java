package cern.enice.jira.amh.dto;

public class Transition {
	private String id;
	private String name;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Transition(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
}
