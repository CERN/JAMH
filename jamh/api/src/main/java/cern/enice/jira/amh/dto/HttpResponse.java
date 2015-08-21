package cern.enice.jira.amh.dto;

public class HttpResponse {
	private int status;
	private Object content;
	
	public HttpResponse(int status, Object content) {
		super();
		this.status = status;
		this.content = content;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
}
