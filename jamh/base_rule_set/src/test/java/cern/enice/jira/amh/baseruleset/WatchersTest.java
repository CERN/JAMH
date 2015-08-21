package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.dto.IssueDescriptor;

@RunWith(JUnitParamsRunner.class)
public class WatchersTest extends BaseRuleSetBaseTest {
	
	@Parameters
	@Test
	public void testWatchersAreSetWithToken(String tokenValue, Set<String> expectedWatchers) {
		ruleSet.setCcAddressesAreWatchers(true);
		email.setCc(new String[0]);
		tokens.put(Tokens.WATCHERS, tokenValue);
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateWatchers(tokens, currentIssueStateDescriptor, issueDescriptor, email);
		assertThat(issueDescriptor.getWatchers(), is(expectedWatchers));
		Map<String, Object> customFields = issueDescriptor.getCustomFields();
		assertNull(customFields);
	}
	
	private Object parametersForTestWatchersAreSetWithToken() {
		Set<String> watchersList1 = new HashSet<String>();
		watchersList1.add("djsuprin");
		watchersList1.add("testuser");
		
		Set<String> watchersList2 = new HashSet<String>();
		watchersList2.add("testuser");
		watchersList2.add("djsuprin");
		
		Set<String> watchersList3 = new HashSet<String>();
		watchersList3.add("djsuprin");
		
		return $(
				$("djsuprin,testuser", watchersList1),
				$("testuser,djsuprin", watchersList2),
				$("", (Object)null),
				$(",,,", (Object)null),
				$("djsuprin", watchersList3)
		);
	}
	
	@Parameters
	@Test
	public void testExternalWatchersAreSetWithToken(String tokenValue, String expectedExternalWatchers) {
		ruleSet.setCcAddressesAreWatchers(true);
		email.setCc(new String[0]);
		tokens.put(Tokens.WATCHERS, tokenValue);
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateWatchers(tokens, currentIssueStateDescriptor, issueDescriptor, email);
		assertNull(issueDescriptor.getWatchers());
		Map<String, Object> customFields = issueDescriptor.getCustomFields();
		Iterator customFieldIterator = customFields.values().iterator();
		assertTrue(customFieldIterator.hasNext());
		String customFieldValue = (String)customFieldIterator.next();
		assertThat(customFieldValue, is(expectedExternalWatchers));
	}
	
	private Object parametersForTestExternalWatchersAreSetWithToken() {
		return $(
				$("djsuprin@gmail.com,some.user@external.domain", "djsuprin@gmail.com,some.user@external.domain"),
				$("some.user@external.domain", "some.user@external.domain")
		);
	}
	
	@Parameters
	@Test
	public void testBothLocalAndExternalWatchersAreSetWithToken(String tokenValue, Set<String> expectedWatchers, String expectedExternalWatchers) {
		ruleSet.setCcAddressesAreWatchers(true);
		email.setCc(new String[0]);
		tokens.put(Tokens.WATCHERS, tokenValue);
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateWatchers(tokens, currentIssueStateDescriptor, issueDescriptor, email);
		assertThat(issueDescriptor.getWatchers(), is(expectedWatchers));
		Map<String, Object> customFields = issueDescriptor.getCustomFields();
		Iterator customFieldIterator = customFields.values().iterator();
		assertTrue(customFieldIterator.hasNext());
		String customFieldValue = (String)customFieldIterator.next();
		assertThat(customFieldValue, is(expectedExternalWatchers));
	}

	private Object parametersForTestBothLocalAndExternalWatchersAreSetWithToken() {
		Set<String> watchersList1 = new HashSet<String>();
		watchersList1.add("djsuprin");
		watchersList1.add("testuser");
		
		Set<String> watchersList2 = new HashSet<String>();
		watchersList2.add("testuser");
		watchersList2.add("djsuprin");
		
		Set<String> watchersList3 = new HashSet<String>();
		watchersList3.add("djsuprin");
		watchersList3.add("testuser");
		
		return $(
				$("c1568705@trbvm.com,djsuprin,testuser", watchersList1, "c1568705@trbvm.com"),
				$("testuser,c1568705@trbvm.com,djsuprin", watchersList2, "c1568705@trbvm.com"),
				$("djsuprin,testuser,djsuprin@gmail.com", watchersList3, "djsuprin@gmail.com"),
				$("djsuprin,testuser,,djsuprin@gmail.com", watchersList3, "djsuprin@gmail.com"),
				$("c1568705@trbvm.com,djsuprin@gmail.com", (Object)null, "c1568705@trbvm.com,djsuprin@gmail.com")
		);
	}
	
	@Parameters({"true", "false"})
	@Test
	public void testNoWatchersTokenIsSet(boolean ccAddressesAreWatchers) {
		ruleSet.setCcAddressesAreWatchers(ccAddressesAreWatchers);
		email.setCc(new String[0]);
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateWatchers(tokens, currentIssueStateDescriptor, issueDescriptor, email);
		assertNull(issueDescriptor.getWatchers());
		assertNull(issueDescriptor.getCustomFields());
	}
	
	@Parameters
	@Test
	public void testWatchersAreSetWithCCField(String cc, String expectedWatchers) {
		ruleSet.setCcAddressesAreWatchers(true);
		email.setCc(cc.split(";"));
		ruleSet.setJiraCommunicator(getMockedJiraCommunicator());
		IssueDescriptor currentIssueStateDescriptor = new IssueDescriptor();
		ruleSet.validateWatchers(tokens, currentIssueStateDescriptor, issueDescriptor, email);
		Set<String> expectedWatchersSet = new HashSet<String>(Arrays.asList(expectedWatchers.split(";")));
		assertThat(issueDescriptor.getWatchers(), is(expectedWatchersSet));
	}
	
	private Object parametersForTestWatchersAreSetWithCCField() {
		return $(
				$("djsuprin@mail.ru;vladimir.vasilyev@cern.ch", "testuser;vvasilye")
				,$("vvasilye@cern.ch;testuser@some.domain", "vvasilye;testuser")
				,$("vladimir.vasilyev@cern.ch;testuser@some.domain", "vvasilye;testuser")
				,$("vladimir.vasilyev@cern.ch;unknownuser@some.domain", "vvasilye")
		);
	}
}
