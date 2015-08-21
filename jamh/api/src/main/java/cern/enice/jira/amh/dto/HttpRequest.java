package cern.enice.jira.amh.dto;

import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

public class HttpRequest {
	private HttpMethod method = null;
	private String url = null;
	private String username = null;
	private String password = null;
	private Object data = null;
	private ContentType contentType = null;
	private ContentType outputContentType = null;

	public HttpRequest(HttpMethod method, String url, String username, String password, Object data,
			ContentType contentType, ContentType outputContentType) {
		super();
		this.method = method;
		this.url = url;
		this.username = username;
		this.password = password;
		this.data = data;
		this.contentType = contentType;
		this.outputContentType = outputContentType;
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public ContentType getContentType() {
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public ContentType getOutputContentType() {
		return outputContentType;
	}
	public void setOutputContentType(ContentType outputContentType) {
		this.outputContentType = outputContentType;
	}
}
