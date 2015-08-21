package cern.enice.jira.amh.api;

import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;

public interface NetworkClient {
	public HttpResponse request(HttpRequest request);
}
