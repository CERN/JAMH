package cern.enice.jira.amh.network_client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

public class NetworkClientJerseyImpl implements NetworkClient {

	private volatile LogProvider logger;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public LogProvider getLogger() {
		return logger;
	}

	public void setLogger(LogProvider logger) {
		this.logger = logger;
	}
	
	public void start() {
		logger.log(LogProvider.INFO, "Network client is started.");
	}
	
	public void stop() {
		logger.log(LogProvider.INFO, "Network client is stopped.");
	}
	
	@Override
	public HttpResponse request(HttpRequest request) {
		if (request.getUrl() == null || request.getUrl().isEmpty() || !request.getUrl().startsWith("http"))
			return null;
		WebTarget webTarget = initialize(request); 
		Invocation.Builder invocationBuilder = setOutputContentType(webTarget, request);
		Invocation invocation = setInputData(request, invocationBuilder);
		Response response = invocation.invoke();
		if (response == null) return null;
		return asHttpResponse(response);
	}
	
	WebTarget initialize(HttpRequest request) {
		Client client = ClientBuilder.newClient();
		String username = request.getUsername();
		String password = request.getPassword();
		if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
			HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);
			client.register(feature);
		}
		if (request.getContentType() == ContentType.MULTIPART) {
			client.register(MultiPartFeature.class);
		}
		return client.target(request.getUrl());
	}
	
	Invocation.Builder setOutputContentType(WebTarget webTarget, HttpRequest request) {
		ContentType outputContentType = request.getOutputContentType();
		Invocation.Builder invocationBuilder = null;
		switch (outputContentType) {
		case NONE:
			invocationBuilder = webTarget.request();
			break;
		case JSON:
			invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			break;
		case TEXT:
			invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
			break;
		default:
			invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
			break;
		}
		return invocationBuilder;
	}
	
	Entity<?> getJsonDataEntity(Object data) {
		try {
			if (data instanceof String) {
				return Entity.json((String)data);
			}
			String dataString = objectMapper.writeValueAsString(data);
			return Entity.json(dataString);
		} catch (IOException e) {
			logger.log(LogProvider.DEBUG, "Couldn't generate JSON POST input data.", e);
		}
		return null;
	}
	
	Entity<?> getMultipartDataEntity(Object data) {
		FormDataMultiPart multipart = new FormDataMultiPart();
		if (data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> fileData = (Map<String, Object>)data;
			for (String key : fileData.keySet()) {
				final FileDataBodyPart filePart = new FileDataBodyPart("file", new File((String)fileData.get(key)));
				multipart.bodyPart(filePart);
			}
		}
		return Entity.entity(multipart, multipart.getMediaType());
	}
	
	Invocation setInputData(HttpRequest request, Invocation.Builder invocationBuilder) {
		ContentType inputContentType = request.getContentType();
		HttpMethod method = request.getMethod();
		Object data = request.getData();
		Entity<?> entity = null;
		switch (inputContentType) {
			case NONE:
				break;
			case JSON:
				invocationBuilder = invocationBuilder.accept(MediaType.APPLICATION_JSON);
				entity = getJsonDataEntity(data);
				break;
			case TEXT:
				invocationBuilder = invocationBuilder.accept(MediaType.TEXT_PLAIN);
				if (data instanceof String)
					entity = Entity.text(data);
				break;
			case MULTIPART:
				invocationBuilder = invocationBuilder.header("X-Atlassian-Token", "nocheck");
				invocationBuilder = invocationBuilder.accept(MediaType.MULTIPART_FORM_DATA);
				entity = getMultipartDataEntity(data);
				break;
			default:
				break;
		}
		if (entity != null)
			return invocationBuilder.build(method.toString(), entity);
		return invocationBuilder.build(method.toString());
	}
	
	HttpResponse asHttpResponse(Response response) {
		int responseStatus = response.getStatus();
		String responseContent = response.readEntity(String.class);
		Object responseContentObject = null;
		try {
			if (responseContent != null && !responseContent.isEmpty())
				responseContentObject = objectMapper.readValue(responseContent, Object.class);
		} catch (Exception e) {
			Map<String, Object> logObject = new HashMap<String, Object>();
			logObject.put("responseContent", responseContent);
			logger.log(LogProvider.DEBUG, "Couldn't cast response content to Java object.", logObject, e);
		}
		HttpResponse result = new HttpResponse(responseStatus, responseContentObject);
		return result;
	}
}