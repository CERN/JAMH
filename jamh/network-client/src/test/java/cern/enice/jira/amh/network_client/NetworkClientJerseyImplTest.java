package cern.enice.jira.amh.network_client;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.utils.ContentType;
import cern.enice.jira.amh.utils.HttpMethod;

@Ignore
@RunWith(JUnitParamsRunner.class)
public class NetworkClientJerseyImplTest {
	protected NetworkClientJerseyImpl networkClient;

	protected static String username = "";
	protected static String password = "";
	protected static String jiraBaseRestUrl = "";
	protected static String issueKey = "";
	
	public static String PROPERTIES_FILE = "C:\\Users\\vvasilye\\jamh-test.properties";

	@BeforeClass
	public static void readProperties() throws IOException {
		Properties prop = new Properties();
		InputStream inputStream = new FileInputStream(PROPERTIES_FILE);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("Property file '" + PROPERTIES_FILE + "' does not exist.");
		}

		// get the property value and print it out
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		jiraBaseRestUrl = prop.getProperty("jiraBaseRestUrl");
		issueKey = prop.getProperty("issueKey");
	}

	@Before
	public void setUp() throws IOException {
		networkClient = spy(new NetworkClientJerseyImpl());
		LogProvider logger = mock(LogProvider.class);
		networkClient.setLogger(logger);
	}

	@Parameters
	@Test
	public void testHttpRequestsAreSuccessful(HttpRequest request) {
		System.out.println("Request method: " + request.getMethod());
		System.out.println("Request url: " + request.getUrl());
		HttpResponse response = networkClient.request(request);
		int responseStatus = response.getStatus();
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		
		System.out.println("Response Status: " + responseStatus);
		System.out.println("Response Content: " + responseContent);
		assertTrue(responseStatus >= 200 && responseStatus < 300);

		if (request.getMethod().equals(HttpMethod.POST)) {
			IssueToDelete.issueToDelete = (String) responseContent.get("key");
		}
	}

	private Object parametersForTestHttpRequestsAreSuccessful() throws IOException {
		readProperties();
		// Configure update data
		Map<String, Object> updateData = getTestUpdateJsonData();
		// Configure project data
		Map<String, Object> createData = getTestCreateJsonData();

		HttpRequest requests[] = new HttpRequest[3];
		requests[0] = new HttpRequest(HttpMethod.GET, jiraBaseRestUrl + "/issue/" + issueKey, username, password, null,
				ContentType.NONE, ContentType.JSON);
		requests[1] = new HttpRequest(HttpMethod.PUT, jiraBaseRestUrl + "/issue/" + issueKey, username, password,
				updateData, ContentType.JSON, ContentType.JSON);
		requests[2] = new HttpRequest(HttpMethod.POST, jiraBaseRestUrl + "/issue", username, password, createData,
				ContentType.JSON, ContentType.JSON);
		return $($(requests[0]), $(requests[1]), $(requests[2]));
	}

	private Map<String, Object> getTestUpdateJsonData() {
		Map<String, Object> priority = new HashMap<String, Object>();
		priority.put("name", "Minor");
		Map<String, Object> issuetype = new HashMap<String, Object>();
		issuetype.put("name", "Bug");
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("summary", "Test summary");
		fields.put("priority", priority);
		fields.put("issuetype", issuetype);
		Map<String, Object> testJsonData = new HashMap<String, Object>();
		testJsonData.put("fields", fields);
		return testJsonData;
	}

	private Map<String, Object> getTestCreateJsonData() {
		Map<String, Object> project = new HashMap<String, Object>();
		project.put("key", "NT");
		Map<String, Object> testJsonData = getTestUpdateJsonData();
		Map<String, Object> fields = (Map<String, Object>) testJsonData.get("fields");
		fields.put("project", project);
		return testJsonData;
	}

	@Test
	public void testDeleteMethod() {
		String url = jiraBaseRestUrl + "/issue/" + IssueToDelete.issueToDelete;
		System.out.println("URL: " + url);
		HttpRequest request = new HttpRequest(HttpMethod.DELETE, url, username, password, null, ContentType.NONE,
				ContentType.NONE);
		HttpResponse response = networkClient.request(request);
		int responseStatus = response.getStatus();
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		System.out.println("Response Status: " + responseStatus);
		System.out.println("Response Content: " + responseContent);
		assertTrue(responseStatus >= 200 && responseStatus < 300);
	}
	
	@Ignore
	@Test
	public void testMultipartRequest() {
		Map<String, Object> issueAttachments = new HashMap<String, Object>();
		File file = new File("src/test/resources/issue-update-webhook.txt");
		System.out.println("Absolute path: " + file.getAbsolutePath());
		issueAttachments.put("file", file.getAbsolutePath());
		String url = jiraBaseRestUrl + "/issue/" + issueKey + "/attachments";
		HttpRequest request = new HttpRequest(HttpMethod.POST, url, username, password, issueAttachments,
				ContentType.MULTIPART, ContentType.JSON);
		HttpResponse response = networkClient.request(request);
		int responseStatus = response.getStatus();
		Map<String, Object> responseContent = (Map<String, Object>) response.getContent();
		System.out.println("Response Status: " + responseStatus);
		System.out.println("Response Content: " + responseContent);
		assertTrue(responseStatus >= 200 && responseStatus < 300);
		assertTrue(responseContent != null);
	}
	
	@Parameters
	@Test
	public void testRequestFailedDueToInvalidUrl(String url) {
		HttpRequest request = new HttpRequest(HttpMethod.GET, url, username, password, null, ContentType.NONE,
				ContentType.NONE);
		HttpResponse response = networkClient.request(request);
		assertNull(response);
	}
	
	private Object parametersForTestRequestFailedDueToInvalidUrl() {
		return $(
				$(""), $((String)null)
		);
	}
	
	@Parameters
	@Test
	public void testUnauthorizedRequestFailed(String restUsername, String restPassword) {
		HttpRequest request = new HttpRequest(HttpMethod.GET, jiraBaseRestUrl + "/issue", restUsername, restPassword, null, ContentType.NONE,
				ContentType.NONE);
		HttpResponse response = networkClient.request(request);
		int responseStatus = response.getStatus();
		assertTrue(responseStatus >= 400 || responseStatus < 500);
	}
	
	private Object parametersForTestUnauthorizedRequestFailed() {
		return $(
				$((String)null, (String)null),
				$("", (String)null),
				$((String)null, ""),
				$("", ""),
				$(username, ""),
				$("", password),
				$(username, (String)null),
				$((String)null, password)
		);
	}
}

class IssueToDelete {
	public static String issueToDelete;
}