package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class EnvironmentTest extends BaseRuleSetBaseTest {
	@Parameters({"environment1", "OS: Windows 7 x64bit; JVM: 1.7.0.26 x64bit"})
	@Test
	public void testEnvironmentIsSet(String environment) {
		tokens.put(Tokens.ENVIRONMENT, environment);
		ruleSet.validateEnvironment(tokens, issueDescriptor);
		assertThat(issueDescriptor.getEnvironment(), is(environment));
	}
	
	@Test
	public void testEnvironmentIsNotSetDueToMissingToken() {
		ruleSet.validateEnvironment(tokens, issueDescriptor);
		assertThat(issueDescriptor.getEnvironment(), nullValue());
	}
}
