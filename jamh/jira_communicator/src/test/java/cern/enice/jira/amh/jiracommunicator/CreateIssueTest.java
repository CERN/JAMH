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
public class CreateIssueTest extends JiraRestCommunicatorBaseTest {
	
	@Parameters
	@Test
	public void testCreateIssue(IssueDescriptor issueDescriptor, HttpResponse response, Result expectedResult) {
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.createIssue(issueDescriptor);
		assertThat(result.getCode(), is(expectedResult.getCode()));
	}
	
	private Object parametersForTestCreateIssue() {
		return $(
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								null, null),
						new HttpResponse(100, null),
						new Result(ResultCode.ISSUE_NOT_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								null, null),
						new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created.txt")),
						new Result(ResultCode.ISSUE_NOT_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								null, null),
						new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created-2.txt")),
						new Result(ResultCode.ISSUE_NOT_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								null, null),
						new HttpResponse(400, getObjectFromJsonFile("src/test/resources/issue-not-created-3.txt")),
						new Result(ResultCode.ISSUE_NOT_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								null, null),
						new HttpResponse(200, null),
						new Result(ResultCode.ISSUE_NOT_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								"", null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor("NT", "Bug", "Summary", "assignee", "reporter", "priority", "duedate",
								"originalEstimate", "remainingEstimate", "Environment", "Description",
								"anAttachment", null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor(null, null, null, null, null, null, null, null, null, null, null, null, null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor(null, null, null, null, null, null, null, "originalEstimate", null, null, null, null, null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor(null, null, null, null, null, null, null, null, "remainingEstimate", null, null, null, null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor(null, null, null, null, null, null, null, " ", null, null, null, null, null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				),
				$(
						fillIssueDescriptor(null, null, null, null, null, null, null, null, " ", null, null, null, null),
						new HttpResponse(200, getObjectFromJsonFile("src/test/resources/issue-created.txt")),
						new Result(ResultCode.ISSUE_CREATED)
				)
		);
	}
}
