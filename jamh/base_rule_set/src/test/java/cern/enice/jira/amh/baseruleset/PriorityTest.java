package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class PriorityTest extends BaseRuleSetBaseTest {

	// Priority tests

	@Test
	public void testPriorityTokenIncorrectValueIsSet() {
		String priority = "abcde";
		tokens.put(Tokens.PRIORITY, priority);
		ruleSet.validatePriority(tokens, issueDescriptor, "nt", "bug");
		assertNull(issueDescriptor.getPriority());
	}

	@Parameters({ "MAJOR", "major", "MaJOr", "minor" })
	@Test
	public void testPriorityTokenIsSet(String priority) {
		tokens.put(Tokens.PRIORITY, priority);
		ruleSet.validatePriority(tokens, issueDescriptor, "nt", "bug");
		assertThat(issueDescriptor.getPriority().toLowerCase(), is(priority.toLowerCase()));
	}

	@Parameters
	@Test
	public void testPriorityShortTokenIsSet(String priorityToken, String expectedPriority) {
		tokens.put(priorityToken, null);
		ruleSet.validatePriority(tokens, issueDescriptor, "nt", "bug");
		String resultPriority = issueDescriptor.getPriority();
		resultPriority = resultPriority == null ? null : resultPriority.toLowerCase(); 
		assertThat(resultPriority, is(expectedPriority));
	}
	
	private Object parametersForTestPriorityShortTokenIsSet() {
		return $(
				$("MAJOR", "major"),
				$("major", "major"),
				$("MaJOr", "major"),
				$("minor", "minor"),
				$("trivial", "trivial"),
				$("critical", "critical"),
				$("blocker", "blocker"),
				$("NotExistingPriority", null)
		);
	}

}
