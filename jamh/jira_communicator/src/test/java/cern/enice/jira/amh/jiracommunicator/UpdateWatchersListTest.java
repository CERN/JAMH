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
import java.util.Arrays;
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
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.dto.HttpRequest;
import cern.enice.jira.amh.dto.HttpResponse;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.network_client.NetworkClientJerseyImpl;
import cern.enice.jira.amh.utils.ResultCode;
import cern.enice.jira.amh.utils.Utils;

@RunWith(JUnitParamsRunner.class)
public class UpdateWatchersListTest extends JiraRestCommunicatorBaseTest {

	@Parameters
	@Test
	public void testAddWatcher(String issueKey, String watcher, HttpResponse response, Result expectedResult) {
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		Result result = jiraCommunicator.addWatcher(issueKey, watcher);
		assertThat(result.getCode(), is(expectedResult.getCode()));
	}
	
	private Object parametersForTestAddWatcher() {
		return $(
				$(null, null, null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("", null, null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$(" ", null, null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", null, null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", "", null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", " ", null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", "username", null, new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", "username", new HttpResponse(400, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", "username", new HttpResponse(100, null), new Result(ResultCode.ISSUE_NOT_UPDATED)),
				$("PROJECT-123", "username", new HttpResponse(200, null), new Result(ResultCode.ISSUE_UPDATED))
		);
	}
	
	@Parameters
	@Test
	public void testUpdateWatchersList(String issueKey, String watchers, HttpResponse response) {
		issueDescriptor.setKey(issueKey);
		if (watchers != null) {
			if (watchers.isEmpty())
				issueDescriptor.setWatchers(Arrays.asList(new String[0]));
			else
				issueDescriptor.setWatchers(Arrays.asList(watchers.split(";")));
		}
		NetworkClient networkClient = jiraCommunicator.getNetworkClient();
		doReturn(response).when(networkClient).request((HttpRequest)anyObject());
		try {
			jiraCommunicator.updateWatchersList(issueDescriptor);
		} catch (Exception ex) {
			fail();
		}
	}
	
	private Object parametersForTestUpdateWatchersList() {
		return $(
				$(null, null, null),
				$("", null, null),
				$(" ", null, null),
				$("PROJECT-123", null, null),
				$("PROJECT-123", "", null),
				$("PROJECT-123", " ", null),
				$("PROJECT-123", "watcher1", null),
				$(null, "watcher1", null),
				$("", "watcher1", null),
				$(" ", "watcher1", null),
				$("PROJECT-123", "watcher1;watcher2", null),
				$("PROJECT-123", "watcher1", new HttpResponse(200, null)),
				$("PROJECT-123", "watcher1;watcher2", new HttpResponse(200, null)),
				$("PROJECT-123", "watcher1", new HttpResponse(400, null)),
				$("PROJECT-123", "watcher1;watcher2", new HttpResponse(400, null))
		);
	}
}
