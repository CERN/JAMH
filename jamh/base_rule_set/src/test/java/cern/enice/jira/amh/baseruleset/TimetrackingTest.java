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
public class TimetrackingTest extends BaseRuleSetBaseTest {
	
	@Test
	public void testBothEstimateParamsAreNotSet() {
		ruleSet.validateTimeTracking(tokens, issueDescriptor);
		assertNull(issueDescriptor.getRemainingEstimate());
		assertNull(issueDescriptor.getOriginalEstimate());
	}
	
	@Parameters(method = "estimateInvalidParameters")
	@Test
	public void testOriginalEstimateIsInvalidAndNotSet(String estimate) {
		tokens.put(Tokens.ORIGINAL_ESTIMATE, estimate);
		ruleSet.validateTimeTracking(tokens, issueDescriptor);
		assertNull(issueDescriptor.getOriginalEstimate());
	}
	
	@Parameters(method = "estimateInvalidParameters")
	@Test
	public void testRemainingEstimateIsInvalidAndNotSet(String estimate) {
		tokens.put(Tokens.REMAINING_ESTIMATE, estimate);
		ruleSet.validateTimeTracking(tokens, issueDescriptor);
		assertNull(issueDescriptor.getRemainingEstimate());
	}
	
	@Parameters(method = "estimateCorrectParameters")
	@Test
	public void testOnlyOriginalEstimateIsSet(String estimate) {
		tokens.put(Tokens.ORIGINAL_ESTIMATE, estimate);
		ruleSet.validateTimeTracking(tokens, issueDescriptor);
		assertThat(issueDescriptor.getOriginalEstimate(), is(estimate));
		assertNull(issueDescriptor.getRemainingEstimate());
	}
	
	@Parameters(method = "estimateCorrectParameters") 
	@Test
	public void testOnlyRemainingEstimateIsSet(String estimate) {
		tokens.put(Tokens.REMAINING_ESTIMATE, estimate);
		ruleSet.validateTimeTracking(tokens, issueDescriptor);
		assertThat(issueDescriptor.getRemainingEstimate(), is(estimate));
		assertNull(issueDescriptor.getOriginalEstimate());
	}
	
	private Object estimateCorrectParameters() {
		return $(
				$("4w 5d 6h"),
				$("1w 10d 20h")
		);
	}
	
	private Object estimateInvalidParameters() {
		return $(
				$("4a 5b 6c"),
				$("x1 y10"),
				$("123z"),
				$(""),
				$(" "),
				$((Object)null)
		);
	}

}
