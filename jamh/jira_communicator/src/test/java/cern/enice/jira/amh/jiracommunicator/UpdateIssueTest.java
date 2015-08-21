package cern.enice.jira.amh.jiracommunicator;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.api.NetworkClient;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.utils.ResultCode;

@RunWith(JUnitParamsRunner.class)
public class UpdateIssueTest extends JiraRestCommunicatorBaseTest {
	
	@Parameters
	@Test
	public void testUpdateIssue(IssueDescriptor issueDescriptor, HttpResponse response, Result expectedResult) {
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.updateIssue(issueDescriptor);
		assertThat(result.getCode(), is(expectedResult.getCode()));
	}
	
	private Object parametersForTestUpdateIssue() {
		IssueDescriptor[] issueDescriptors = new IssueDescriptor[6];
		
		for (int i = 0; i < issueDescriptors.length; i++) {
			issueDescriptors[i] = new IssueDescriptor();
			issueDescriptors[i].setKey("PROJECT-123");
		}
		
		issueDescriptors[0].setAssignee("username");
		issueDescriptors[0].setDueDate("2014-12-05");
		issueDescriptors[0].setReporter("thereporter");
		issueDescriptors[0].setEnvironment("OS: Windows 7 x64");
		Map<String, Object> customFields = new HashMap<String, Object>();
		customFields.put("customfield_123", null);
		customFields.put("customfield_321", "Value");
		issueDescriptors[0].setCustomFields(customFields);
		
		issueDescriptors[1].setSummary("Test summary...");
		issueDescriptors[1].setAssignee("username");
		issueDescriptors[1].setIssueType("Bug");
		issueDescriptors[1].setDescription("Test description...");
		issueDescriptors[1].setPriority("Minor");
		issueDescriptors[1].setComment("Bla bla bla...");
		issueDescriptors[1].setWorklogTimespent("1w 2d 3h");
		
		issueDescriptors[2].setAssignee("username");
		issueDescriptors[2].setDueDate(null);
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
		issueDescriptors[2].setTransition("5");
		issueDescriptors[2].setResolution("fixed");
		issueDescriptors[2].setWorklogTimespent(null);
		
		issueDescriptors[4].setProject(null);
		components = new ArrayList<String>();
		issueDescriptors[4].setComponents(components);
		
		issueDescriptors[5].setProject("NT");
		issueDescriptors[5].setComponents(null);
		
		return $(
				$(issueDescriptors[0], null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[0], new HttpResponse(400, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[0], new HttpResponse(100, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[0], new HttpResponse(200, null), new Result(ResultCode.ISSUE_UPDATED)),
				
				$(issueDescriptors[1], null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[1], new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created.txt")), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[1], new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created-2.txt")), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[1], new HttpResponse(100, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[1], new HttpResponse(200, null), new Result(ResultCode.ISSUE_UPDATED)),
				
				$(issueDescriptors[2], null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[2], new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created-3.txt")), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[2], new HttpResponse(100, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(issueDescriptors[2], new HttpResponse(200, null), new Result(ResultCode.ISSUE_UPDATED))
		);
	}
}
