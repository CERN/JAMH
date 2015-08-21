package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DueDateTest extends BaseRuleSetBaseTest {
	
	@Test
	public void testDuedateTokenIsNotSet() {
		ruleSet.validateDueDate(tokens, issueDescriptor);
		assertNull(issueDescriptor.getDueDate());
	}
	
	@Parameters
	@Test
	public void testDuedateIsSet(String duedate, String expectedDuedate) {
		tokens.put(Tokens.DUEDATE, duedate);
		ruleSet.validateDueDate(tokens, issueDescriptor);
		assertThat(issueDescriptor.getDueDate(), is(expectedDuedate));
	}
	
	private Object parametersForTestDuedateIsSet() {
		return $(
				$("2014-12-05", "2014-12-05"),
				$("2014-05-13", "2014-05-13"),
				$("2014-16-45", "2015-05-15"),
				$("2014-9-12", "2014-09-12")
		);
	}
	
	@Parameters
	@Test
	public void testDuedateIsNotSetDueToInvalidFormat(String duedate) {
		tokens.put(Tokens.DUEDATE, duedate);
		ruleSet.validateDueDate(tokens, issueDescriptor);
		assertThat(issueDescriptor.getDueDate(), nullValue());
	}
	
	private Object parametersForTestDuedateIsNotSetDueToInvalidFormat() {
		return $(
				$("2015/05/13"),
				$("1024/512/256"),
				$((Object)null),
				$("")
		);
	}

}
