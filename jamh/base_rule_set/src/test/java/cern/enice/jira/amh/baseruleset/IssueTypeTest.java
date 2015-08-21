package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class IssueTypeTest extends BaseRuleSetBaseTest {

	@Parameters({ "BUG", "Bug", "bug", "bUg", "task", "information request", "Information Request" })
	@Test
	public void testIssueTypeTokenIsSet(String issueType) {
		tokens.put(Tokens.ISSUETYPE, issueType);
		// TODO mock email and originalIssueDescriptor
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertThat(issueDescriptor.getIssueType().toLowerCase(), is(issueType.toLowerCase()));
	}
	
	@Parameters({ "BUG", "Bug", "bug", "bUg", "task", "TAsk", "Information Request" })
	@Test
	public void testIssueTypeShortTokenIsSet(String issueType) {
		tokens.put(issueType, null);
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertThat(issueDescriptor.getIssueType().toLowerCase(), is(issueType.toLowerCase()));
	}

	@Test
	public void testIssueTypeTokenIsNotSet() {
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertThat(issueDescriptor.getIssueType().toLowerCase(), is(ruleSet.getDefaultIssueTypeName().toLowerCase()));
	}

	@Parameters
	@Test
	public void testIssueTypeTokenInvalidValueIsSet(String shortIssueTypeToken, String issueTypeTokenValue, String expectedIssueType) {
		if (shortIssueTypeToken != null) {
			tokens.put(shortIssueTypeToken, null);
		}
		tokens.put(Tokens.ISSUETYPE, issueTypeTokenValue);
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertThat(issueDescriptor.getIssueType(), is(expectedIssueType));
	}
	
	private Object parametersForTestIssueTypeTokenInvalidValueIsSet() {
		return $(
				$(null, "AFJKLAERNBM", null),
				$(null, null, null),
				$("Bug", null, "Bug"),
				$("bug", "AFJKLAERNBM", "Bug")
		);
	}
	
	@Test
	public void testIssueTypesAreNotReceived() {
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertNull(issueDescriptor.getIssueType());
	}
	
	@Test
	public void testIssueTypeIsNotSetNoTokenNoDefaultIssueType() {
		ruleSet.setDefaultIssueTypeName("NotExistingIssueType");
		ruleSet.validateIssueType(email, tokens, issueDescriptor, originalIssueDescriptor, "nt");
		assertNull(issueDescriptor.getIssueType());
	}

}
