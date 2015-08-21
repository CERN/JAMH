package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.dto.IssueDescriptor;

@RunWith(JUnitParamsRunner.class)
public class SummaryAndIssueKeyTest extends BaseRuleSetBaseTest {

	// Summary And Issue Key tests

	@Test
	@Parameters
	public void testSubjectContainsIssueKey(String issueKey, String tokensString, String expectedIssueKey) {
		ruleSet.validateIssueKey(issueDescriptor, issueKey + tokensString);
		assertThat(issueDescriptor.getKey(), is(expectedIssueKey.toUpperCase()));
	}

	private Object parametersForTestSubjectContainsIssueKey() {
		return $($("NT-5", "", "NT-5"), $("NT-5", " #BUG #MINOR", "NT-5"), $("nt-21", "", "NT-21"),
				$("nt-21", " #BUG #MINOR", "NT-21"), $("Abc-120", "", "ABC-120"),
				$("AbC-120", " #BUG #MINOR", "ABC-120"), $("Nt-12", "", "NT-12"), $("Nt-12", " #BUG #MINOR", "NT-12"),
				$("RE:NT-5", "", "NT-5"), $("Re: Re:NT-5", " #BUG #MINOR", "NT-5"),
				$("RE :re:re:  Re: nt-21", "", "NT-21"), $("rE:    nt-21", " #BUG #MINOR", "NT-21"));
	}
	
	@Parameters
	@Test
	public void testSubjectContainsWrongIssueKey(String subject) {
		email.setSubject(subject);
		ruleSet.validateIssueKey(issueDescriptor, subject);
		assertNull(issueDescriptor.getKey());
	}
	
	private Object parametersForTestSubjectContainsWrongIssueKey() {
		return $(
				$("NOSUCHPROJECT-123 #assignee=vvasilye"),
				$("NOSUCHPROJECT- #assignee=vvasilye"),
				$("-NOSUCHPROJECT- #assignee=vvasilye"),
				$("NOSUCHPROJECT-123")
		);
	}

	@Test
	@Parameters
	public void testSubjectContainsSummary(String subject, String expectedSummary) {
		String[] emailRecipients = new String[0];
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateSummary(tokens, issueDescriptor, currentIssueStateDescriptor, subject, emailRecipients);
		assertThat(issueDescriptor.getSummary(), is(ruleSet.getDefaultSummary()));
		assertNull(issueDescriptor.getKey());
	}

	private Object parametersForTestSubjectContainsSummary() {
		return $(
				$("Test issue summary", ""), 
				$("Test issue summary", " #BUG #MINOR"), 
				$("", ""),
				$("", " #BUG #MINOR"), 
				$(" ", " #BUG #MINOR"));
	}

	@Parameters
	@Test
	public void testSummaryTokenIsSet(String subject, String summaryTokenValue, String expectedSummary, String expectedIssueKey) {
		ruleSet.setDefaultSummary("Default Summary");
		String[] emailRecipients = new String[0];
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		tokens.put(Tokens.SUMMARY, summaryTokenValue);
		ruleSet.validateSummary(tokens, issueDescriptor, currentIssueStateDescriptor, subject, emailRecipients);
		assertThat(issueDescriptor.getSummary(), is(expectedSummary));
		assertThat(issueDescriptor.getKey(), is(expectedIssueKey));
	}
	
	private Object parametersForTestSummaryTokenIsSet() {
		return $(
				$("Summary In Subject", "Summary Token Value", "Summary Token Value", null),
				$("Summary In Subject", "", "Default Summary", null),
				$("Summary In Subject", null, "Default Summary", null),
				$("NT-123", null, "Default Summary", "NT-123"),
				$("NT-1a23", null, "Default Summary", null),
				$("", null, "Default Summary", null),
				$("    ", null, "Default Summary", null)
		);
	}
	
	@Parameters({"", "   ", "#abc #def", " #abc def"})
	@Test
	public void testDefaultSummaryIsSet(String subject) {
		String[] emailRecipients = new String[0];
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.setDefaultSummary("Default Summary");
		ruleSet.validateSummary(tokens, issueDescriptor, currentIssueStateDescriptor, subject, emailRecipients);
		assertThat(issueDescriptor.getSummary(), is(ruleSet.getDefaultSummary()));
	}
	
	@Parameters
	@Test
	public void testIgnoreTokensPatterns(String subject, String ignoreTokensPatternsAsString, String expectedSummary) {
		ruleSet.setIgnoreTokensPatterns(Arrays.asList(ignoreTokensPatternsAsString.split(";")));
		String[] emailRecipients = new String[0];
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateSummary(tokens, issueDescriptor, currentIssueStateDescriptor, subject, emailRecipients);
		assertThat(issueDescriptor.getSummary(), is(expectedSummary));
		assertNull(issueDescriptor.getKey());
	}
	
	private Object parametersForTestIgnoreTokensPatterns() {
		return $(
				$("Summary In Subject #ThirdPartyToken=1234567890 #assignee=vvasilye", 
						"#ThirdPartyToken=[0-9]+;#SR=[0-9]+", "Summary In Subject #ThirdPartyToken=1234567890"),
				$("Summary In Subject #SR=76543 #assignee=vvasilye", 
						"#ThirdPartyToken=[0-9]+;#SR=[0-9]+", "Summary In Subject #SR=76543"),
				$("Summary In Subject #SR=76543 #ThirdPartyToken=1234567890 #assignee=vvasilye", 
						"#ThirdPartyToken=[0-9]+;#SR=[0-9]+", "Summary In Subject #ThirdPartyToken=1234567890 #SR=76543")
		);
	}

}
