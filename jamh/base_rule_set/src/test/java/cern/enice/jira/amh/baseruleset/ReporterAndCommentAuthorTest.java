package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;

@RunWith(JUnitParamsRunner.class)
public class ReporterAndCommentAuthorTest extends BaseRuleSetBaseTest {

	// Reporter and comment author tests
	
	private void reporterAndCommentAuthorSharedCode(String from, String reporterUsername) {
		email.setFrom(from);
		ruleSet.setJiraCommunicator(getMockedJiraCommunicator());
		ruleSet.validateReporter(tokens, issueDescriptor, email.getBody(), from);
		ruleSet.validateCommentAuthor(tokens, issueDescriptor, email.getBody(), from);
	}

	@Parameters(method="parametersForReporterFieldTests")
	@Test
	public void testReporterIsPickedFromFromField(String from, String reporterUsername) {
		reporterAndCommentAuthorSharedCode(from, reporterUsername);
		assertThat(issueDescriptor.getReporter(), is(reporterUsername));
	}
	
	@Parameters(method="parametersForReporterFieldTests")
	@Test
	public void testReporterIsPickedFromToken(String from, String reporterUsername) {
		tokens.put(Tokens.REPORTER, reporterUsername);
		reporterAndCommentAuthorSharedCode(from, reporterUsername);
		assertThat(issueDescriptor.getReporter(), is(reporterUsername));
	}
	
	@Parameters(method="parametersForReporterFieldTests")
	@Test
	public void testCommentAuthorIsPickedFromFromField(String from, String reporterUsername) {
		issueDescriptor.setKey("PROJECT-123");
		reporterAndCommentAuthorSharedCode(from, reporterUsername);
		assertThat(issueDescriptor.getCommentAuthor(), is(reporterUsername));
	}
	
	@Parameters(method="parametersForReporterFieldTests")
	@Test
	public void testCommentAuthorIsPickedFromFromFieldAndReporterIsPickedFromToken(String from, String reporterUsername) {
		tokens.put(Tokens.REPORTER, "someuser");
		issueDescriptor.setKey("PROJECT-123");
		reporterAndCommentAuthorSharedCode(from, reporterUsername);
		assertThat(issueDescriptor.getCommentAuthor(), is(reporterUsername));
		assertThat(issueDescriptor.getReporter(), is("someuser"));
	}

	private Object parametersForReporterFieldTests() {
		return $(
				$("vladimir.vasilyev@cern.ch", "vvasilye"),
				$("djsuprin@mail.ru", "testuser"),
				$("djsuprin@yandex.ru", "cpodevin")
		);
	}

}
