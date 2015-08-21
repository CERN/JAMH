package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;

@RunWith(JUnitParamsRunner.class)
public class WorkflowStepsTest extends BaseRuleSetBaseTest {

	@Parameters
	@Test
	public void testTransitionIsSet(String issueKey, String token, String tokenValue, String expectedValue) {
		issueDescriptor.setKey(issueKey);
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		Map<String, Object> transitionsData = (Map<String, Object>)getMapFromJsonFile("src/test/resources/transitions-data.txt");
		doReturn(transitionsData).when(jiraCommunicator).getIssueTransitions(issueDescriptor.getKey());
		ruleSet.setJiraCommunicator(jiraCommunicator);
		tokens.put(token, tokenValue);
		ruleSet.validateTransition(tokens, issueDescriptor);
		assertThat(issueDescriptor.getTransition(), is(expectedValue));
	}

	private Object parametersForTestTransitionIsSet() {
		return $(
				//$("resolve", null, "Resolve Issue"), 
				$("PROJECT-123", "close", null, "701"),
				$("PROJECT-123", "reopen", null, "3"),
				$("PROJECT-123", "resolve", null, "1"),
				$("PROJECT-123", "transition", "close issue", "701"),
				$("PROJECT-123", "transition", "reopen issue", "3"),
				$("PROJECT-123", "transition", "resolve issue", "1"),
				$("PROJECT-123", "transition", "Not existing transition", null),
				$("PROJECT-123", "transition", "", null),
				$("PROJECT-123", "transition", null, null),
				$(null, "transition", null, null)
		);
	}

	@Parameters
	@Test
	public void testResolutionIsSet(String issueKey, String tokenValue, String expectedValue) {
		issueDescriptor.setKey(issueKey);
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		Map<String, Object> resolutionsData = (Map<String, Object>)getMapFromJsonFile("src/test/resources/resolutions-data.txt");
		doReturn(resolutionsData).when(jiraCommunicator).getIssueResolutions();
		ruleSet.setJiraCommunicator(jiraCommunicator);
		tokens.put(Tokens.RESOLUTION, tokenValue);
		ruleSet.validateResolution(tokens, issueDescriptor);
		assertThat(issueDescriptor.getResolution(), is(expectedValue));
	}
	
	private Object parametersForTestResolutionIsSet() {
		return $(
				$("PROJECT-123", "fixed", "Fixed"),
				$("PROJECT-123", "fIxEd", "Fixed"),
				$("PROJECT-123", "won't fix", "Won't Fix"),
				$("PROJECT-123", "won't FIX", "Won't Fix"),
				$("PROJECT-123", "not a bug", "Not a Bug"),
				$("PROJECT-123", "Not a Bug", "Not a Bug"),
				$(null, "Not a Bug", null),
				$("PROJECT-123", null, null),
				$(null, null, null),
				$("PROJECT-123", "", null),
				$("PROJECT-123", "Not Existing Resolution", null)
		);
	}
	
	@Test
	public void testResolutionsDataIsNotReceived() {
		issueDescriptor.setKey("PROJECT-123");
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		doReturn(null).when(jiraCommunicator).getIssueResolutions();
		ruleSet.setJiraCommunicator(jiraCommunicator);
		tokens.put(Tokens.RESOLUTION, "Any resolution");
		ruleSet.validateResolution(tokens, issueDescriptor);
		assertNull(issueDescriptor.getResolution());
	}
	
	@Parameters
	@Test
	public void testResolutionTokenIsNotSet(String issueKey) {
		issueDescriptor.setKey(issueKey);
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		Map<String, Object> resolutionsData = (Map<String, Object>)getMapFromJsonFile("src/test/resources/resolutions-data.txt");
		doReturn(resolutionsData).when(jiraCommunicator).getIssueResolutions();
		ruleSet.setJiraCommunicator(jiraCommunicator);
		ruleSet.validateResolution(tokens, issueDescriptor);
		assertNull(issueDescriptor.getResolution());
	}
	
	private Object parametersForTestResolutionTokenIsNotSet() {
		return $(
				$("PROJECT-123"),
				$((Object)null)
		);
	}
	
	@Test
	public void testIssueIsSetToBeDeletedWithToken() {
		tokens.put(Tokens.TRANSITION_DELETE, null);
		ruleSet.validateDeleteIssue(tokens, issueDescriptor);
		assertTrue(issueDescriptor.isDelete());
	}
	
	@Test
	public void testIssueIsNotSetToBeDeletedDueToMissingToken() {
		ruleSet.validateDeleteIssue(tokens, issueDescriptor);
		assertFalse(issueDescriptor.isDelete());
	}
	
}