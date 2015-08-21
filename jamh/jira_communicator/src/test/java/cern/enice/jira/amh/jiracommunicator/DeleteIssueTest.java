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
public class DeleteIssueTest extends JiraRestCommunicatorBaseTest {
	
	@Parameters
	@Test
	public void testDeleteIssue(String issueKey, HttpResponse response, Result expectedResult) {
		issueDescriptor.setKey(issueKey);
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.deleteIssue(issueDescriptor);
		assertThat(result.getCode(), is(expectedResult.getCode()));
	}
	
	private Object parametersForTestDeleteIssue() {
		return $(
				$(null, null, new Result(ResultCode.ISSUE_NOT_DELETED)),
				$("", null, new Result(ResultCode.ISSUE_NOT_DELETED)),
				$(" ", null, new Result(ResultCode.ISSUE_NOT_DELETED)),
				$("PROJECT-123", null, new Result(ResultCode.ISSUE_NOT_DELETED)),
				$("PROJECT-123", new HttpResponse(400, null), new Result(ResultCode.ISSUE_NOT_DELETED)),
				$("PROJECT-123", new HttpResponse(100, null), new Result(ResultCode.ISSUE_NOT_DELETED)),
				$("PROJECT-123", new HttpResponse(403, null), new Result(ResultCode.ISSUE_NOT_DELETED_NO_PERMISSION)),
				$("PROJECT-123", new HttpResponse(404, null), new Result(ResultCode.ISSUE_NOT_DELETED_NO_ISSUE)),
				$("PROJECT-123", new HttpResponse(200, null), new Result(ResultCode.ISSUE_DELETED))
		);
	}
}
