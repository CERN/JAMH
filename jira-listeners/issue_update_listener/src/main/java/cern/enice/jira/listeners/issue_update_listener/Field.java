package cern.enice.jira.listeners.issue_update_listener;

public class Field {
	private String field;
	private String oldValue;
	private String newValue;
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Field(String field, String oldValue, String newValue) {
		super();
		this.field = field;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
}
