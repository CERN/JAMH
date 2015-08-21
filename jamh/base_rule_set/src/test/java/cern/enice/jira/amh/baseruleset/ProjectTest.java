package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.dto.EMailAddress;

@RunWith(JUnitParamsRunner.class)
public class ProjectTest extends BaseRuleSetBaseTest {

	@Parameters({ "NT", "nt", "Nt", "nT" })
	@Test
	public void testProjectTokenIsSet(String projectKey) {
		email.setFrom(new EMailAddress("user", "localPart", "some.domain"));
		tokens.put(Tokens.PROJECTKEY, projectKey);
		ruleSet.validateProject(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getProject(), is(projectKey.toUpperCase()));
	}

	@Test
	public void testDefaultProjectIsSet() {
		email.setFrom("user@some.domain");
		ruleSet.validateProject(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getProject(), is(ruleSet.getDefaultProjectKey().toUpperCase()));
	}

	@Parameters
	@Test
	public void testProjectIsPickedFromFromField(String emailAddress, String projectKey) {
		email.setFrom(emailAddress);
		ruleSet.validateProject(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getProject(), is(projectKey));
	}

	private Object parametersForTestProjectIsPickedFromFromField() {
		return $($("ABC <abc@gmail.com>", "ABC"), $("NT <abc@yandex.ru>", "NT"), $("ABC <abc@cern.ch>", "ABC"));
	}

	@Test
	public void testProjectTokenInvalidValueIsSet() {
		email.setFrom("user@some.domain");
		tokens.put(Tokens.PROJECTKEY, "AFJKLAERNBM");
		ruleSet.validateProject(tokens, issueDescriptor, email);
		assertNull(issueDescriptor.getIssueType());
	}
	
	@Test
	public void testProjectIsNotSetNoTokenNoDefaults() {
		email.setFrom("user@some.domain");
		ruleSet.setDefaultProjectKey("NotExistingProject");
		ruleSet.setDomainsToProjects(new HashMap<String, String>());
		ruleSet.validateProject(tokens, issueDescriptor, email);
		assertNull(issueDescriptor.getIssueType());
	}

}
