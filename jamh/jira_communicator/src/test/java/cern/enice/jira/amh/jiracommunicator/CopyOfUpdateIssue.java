package cern.enice.jira.amh.jiracommunicator;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.network_client.NetworkClientJerseyImpl;
import cern.enice.jira.amh.utils.ResultCode;
import cern.enice.jira.amh.utils.Utils;

@RunWith(JUnitParamsRunner.class)
public class CopyOfUpdateIssue extends JiraRestCommunicatorBaseTest {

	@Parameters
	@Test
	public void testIssueIsUpdated(IssueDescriptor issueDescriptor) {
		HttpResponse response = new HttpResponse(200, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.updateIssue(issueDescriptor);
		assertThat(result.getCode(), is(ResultCode.ISSUE_UPDATED));
		assertTrue(result.getErrors() == null);
		assertTrue(result.getFields() == null);
	}
	
	private Object parametersForTestIssueIsUpdated() {
		IssueDescriptor[] issueDescriptors = new IssueDescriptor[3];
		issueDescriptors[0] = new IssueDescriptor();
		issueDescriptors[0].setKey("NT-35");
		issueDescriptors[0].setAssignee("vvasilye");
		issueDescriptors[0].setDueDate("2014-12-05");
		issueDescriptors[0].setReporter("vvasilye");
		issueDescriptors[0].setEnvironment("OS: Windows 7 x64");
		
		issueDescriptors[1] = new IssueDescriptor();
		issueDescriptors[1].setKey("NT-35");
		issueDescriptors[1].setSummary("Test summary...");
		issueDescriptors[1].setAssignee("vvasilye");
		issueDescriptors[1].setDueDate("2014-12-05");
		issueDescriptors[1].setIssueType("Bug");
		issueDescriptors[1].setDescription("Test description...");
		issueDescriptors[1].setPriority("Minor");
		issueDescriptors[1].setComment("Bla bla bla...");
		
		issueDescriptors[2] = new IssueDescriptor();
		issueDescriptors[2].setKey("NT-35");
		issueDescriptors[2].setAssignee("vvasilye");
		issueDescriptors[2].setDueDate("2014-12-05");
		List<String> affectedVersions=  new ArrayList<String>();
		affectedVersions.add("5.0");
		List<String> components=  new ArrayList<String>();
		components.add("component1");
		components.add("component2");
		List<String> fixVersions=  new ArrayList<String>();
		fixVersions.add("5.0");
		fixVersions.add("5.1");
		issueDescriptors[2].setAffectedVersions(affectedVersions);
		issueDescriptors[2].setComponents(components);
		issueDescriptors[2].setFixVersions(fixVersions);
		issueDescriptors[2].setEnvironment("OS: Windows 7 x64");
		issueDescriptors[2].setTransition("5");
		issueDescriptors[2].setResolution("fixed");
		
		return $(
				$(issueDescriptors[0]),
				$(issueDescriptors[1]),
				$(issueDescriptors[2])
		);
	}
	
	@Test
	public void testFailedToUpdateIssueNoResponseContent() {
		issueDescriptor.setKey("NT-35");
		HttpResponse response = new HttpResponse(400, null);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.updateIssue(issueDescriptor);
		assertThat(result.getCode(), is(ResultCode.ISSUE_NOT_UPDATED));
		assertTrue(result.getErrors() == null);
		assertTrue(result.getFields() == null);
	}
	
//	@Test
//	public void testFailedToUpdateIssueWithResponseContent() {
//		IssueDescriptor issueDescriptor = new IssueDescriptor();
//		issueDescriptor.setKey("NT-35");
//		HttpResponse response = new HttpResponse(400, null);
//		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
//		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
//		Result result = jiraCommunicator.updateIssue(issueDescriptor);
//		assertThat(result.getCode(), is(ResultCode.ISSUE_NOT_UPDATED));
//		assertTrue(result.getErrors() == null);
//		assertTrue(result.getFields() == null);
//	}
}
