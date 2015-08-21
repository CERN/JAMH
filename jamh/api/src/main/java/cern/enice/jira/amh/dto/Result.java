package cern.enice.jira.amh.dto;

import java.util.List;
import java.util.Map;

import cern.enice.jira.amh.utils.ResultCode;

public class Result {
	private ResultCode code;
	private List<String> errors;
	private Map<String, String> fields;
	
	public Map<String, String> getFields() {
		return fields;
	}
	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
	public ResultCode getCode() {
		return code;
	}
	public void setCode(ResultCode code) {
		this.code = code;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	public Result(ResultCode code) {
		this.code = code;
		this.errors = null;
		this.fields = null;
	}
	public Result(ResultCode code, List<String> errors) {
		this.code = code;
		this.errors = errors;
		this.fields = null;
	}
	public Result(ResultCode code, List<String> errors, Map<String, String> fields) {
		this.code = code;
		this.errors = errors;
		this.fields = fields;
	}
}
