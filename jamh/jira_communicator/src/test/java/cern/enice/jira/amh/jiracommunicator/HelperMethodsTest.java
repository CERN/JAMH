package cern.enice.jira.amh.jiracommunicator;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.search.ReceivedDateTerm;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.network_client.NetworkClientJerseyImpl;
import cern.enice.jira.amh.utils.Utils;

@RunWith(JUnitParamsRunner.class)
public class HelperMethodsTest extends JiraRestCommunicatorBaseTest {

	@Parameters
	@Test
	public void testGetValidUser(String usernameOrEmail, String expectedUsername, Object user) {
		HttpResponse response = new HttpResponse(200, user);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> userData = jiraCommunicator.getUser(usernameOrEmail);
		assertNotNull(userData);
		String receivedUsername = (String)userData.get("name");
		assertThat(receivedUsername, is(expectedUsername));
	}

	private Object parametersForTestGetValidUser() {
		return $(
				$("vvasilye", "vvasilye", 
						getObjectFromJsonFile("src/test/resources/user-data-1.txt")), 
				$("djsuprin", "djsuprin", 
						getObjectFromJsonFile("src/test/resources/user-data-2.txt")), 
				$("vladimir.vasilyev@cern.ch", "vvasilye", 
						getObjectFromJsonFile("src/test/resources/user-data-3.txt"))
		);
	}

	@Parameters({"djsuprin23", "abcdef", "vladimir..vasilyev@cern.ch"})
	@Test
	public void testGetUserWithInvalidUsername(String usernameOrEmail) {
		HttpResponse response = new HttpResponse(404, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> userData = jiraCommunicator.getUser(usernameOrEmail);
		assertNull(userData);
	}
	
	@Parameters({"fixed", "won't fix", "unresolved", "duplicate", "incomplete", "cannot reproduce", "not a bug", "moved", "done"})
	@Test
	public void testGetResolutions(String resolutionNameLowercased) {
		List<Map<String, Object>> resolutionsFromFile = (List<Map<String, Object>>)getObjectFromJsonFile("src/test/resources/resolutions.txt");
		HttpResponse response = new HttpResponse(200, resolutionsFromFile);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> resolutions = jiraCommunicator.getIssueResolutions();
		assertTrue(resolutions.containsKey(resolutionNameLowercased));
	}
	
	@Test
	public void testFailedToGetResolutions() {
		HttpResponse response = new HttpResponse(404, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> resolutions = jiraCommunicator.getIssueResolutions();
		assertNull(resolutions);
	}
	
	@Parameters({"resolve issue", "close issue"})
	@Test
	public void testGetTransitions(String transitionNameLowercased) {
		Map<String, Object> transitionsFromFile = (Map<String, Object>)getObjectFromJsonFile("src/test/resources/transitions.txt");
		HttpResponse response = new HttpResponse(200, transitionsFromFile);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> transitions = jiraCommunicator.getIssueTransitions("NT-35");
		assertTrue(transitions.containsKey(transitionNameLowercased));
	}
	
	@Test
	public void testFailedToGetTransitions() {
		HttpResponse response = new HttpResponse(404, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> transitions = jiraCommunicator.getIssueTransitions("NT-35");
		assertNull(transitions);
	}
	
	@Test
	public void testCreateMetaIsFetched() {
		Map<String, Object> createMetaFromFile = (Map<String, Object>)getObjectFromJsonFile("src/test/resources/create-meta-raw.txt");
		HttpResponse response = new HttpResponse(200, createMetaFromFile);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> receivedCreateMeta = jiraCommunicator.fetchCreateMetaForProjects("nt");
		for (String projectKey : receivedCreateMeta.keySet()) {
			System.out.println("Create meta fetched for project key: " + projectKey);
		}
		assertTrue(receivedCreateMeta.containsKey("nt"));
		assertTrue(jiraCommunicator.getCreateMeta().containsKey("nt"));
	}
	
	@Test
	public void testFailedToFetchCreateMeta() {
		HttpResponse response = new HttpResponse(404, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Map<String, Object> receivedCreateMeta = jiraCommunicator.fetchCreateMetaForProjects("nt");
		assertNull(receivedCreateMeta);
	}
	
	
}
