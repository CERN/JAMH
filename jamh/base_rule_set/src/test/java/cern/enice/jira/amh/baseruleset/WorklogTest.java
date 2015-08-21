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
public class WorklogTest extends BaseRuleSetBaseTest {
	
	@Test
	public void testNoneWorklogParamsAreSetDueToMissingTimespentToken() {
		ruleSet.validateWorklog(tokens, issueDescriptor);
		assertNull(issueDescriptor.getWorklogTimespent());
		assertNull(issueDescriptor.getWorklogStarted());
	}
	
	@Parameters(method = "worklogInvalidParameters")
	@Test
	public void testNoneWorklogParamsAreSetDueToInvalidTimespentToken(String timespent, String started) {
		tokens.put(Tokens.WORKLOG_TIMESPENT, timespent);
		tokens.put(Tokens.WORKLOG_STARTED, started);
		ruleSet.validateWorklog(tokens, issueDescriptor);
		assertNull(issueDescriptor.getWorklogTimespent());
		assertNull(issueDescriptor.getWorklogStarted());
	}
	
	private Object worklogInvalidParameters() {
		return $(
				$("4a 5b 6c", "2015-01-20"),
				$("x1 y10", "2015-01-20"),
				$("123z", "2015-01-20"),
				$("", "2015-01-20"),
				$(" ", "2015-01-20"),
				$(null, "2015-01-20")
		);
	}
	
	@Parameters(method = "worklogCorrectParameters")
	@Test
	public void testOnlyTimespentIsSet(String timespent, String started) {
		tokens.put(Tokens.WORKLOG_TIMESPENT, timespent);
		ruleSet.validateWorklog(tokens, issueDescriptor);
		assertThat(issueDescriptor.getWorklogTimespent(), is(timespent));
		assertNull(issueDescriptor.getWorklogStarted());
	}
	
	@Parameters(method = "worklogCorrectParameters")
	@Test
	public void testBothTimespentAndStartedAreSet(String timespent, String started) {
		tokens.put(Tokens.WORKLOG_TIMESPENT, timespent);
		tokens.put(Tokens.WORKLOG_STARTED, started);
		ruleSet.validateWorklog(tokens, issueDescriptor);
		assertThat(issueDescriptor.getWorklogTimespent(), is(timespent));
		assertThat(issueDescriptor.getWorklogStarted(), is(started + ruleSet.JIRA_TIME_POSTFIX));
	}
	
	private Object worklogCorrectParameters() {
		return $(
				$("4w 5d 6h", "2015-01-19"),
				$("1w 10d 20h", "2015-01-19")
		);
	}
	
	@Parameters
	@Test
	public void testTimespentIsSetButStartedIsNotDueToInvalidParameterValue(String timespent, String started) {
		tokens.put(Tokens.WORKLOG_TIMESPENT, timespent);
		tokens.put(Tokens.WORKLOG_STARTED, started);
		ruleSet.validateWorklog(tokens, issueDescriptor);
		assertThat(issueDescriptor.getWorklogTimespent(), is(timespent));
		assertNull(issueDescriptor.getWorklogStarted());
	}
	
	private Object parametersForTestTimespentIsSetButStartedIsNotDueToInvalidParameterValue() {
		return $(
				$("4w 5d 6h", "2015/01/19"),
				$("1w 10d 20h", "2015.01.19"),
				$("1w 10d 20h", "20150119"),
				$("1w 10d 20h", ""),
				$("1w 10d 20h", " "),
				$("1w 10d 20h", null)
		);
	}

}