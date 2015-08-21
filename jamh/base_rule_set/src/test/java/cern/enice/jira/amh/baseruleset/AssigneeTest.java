package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;

@RunWith(JUnitParamsRunner.class)
public class AssigneeTest extends BaseRuleSetBaseTest {

	// Assignee tests

	@Test
	public void testAssigneeIsSetWithToken() {
		String username = "username1";
		tokens.put(Tokens.ASSIGNEE, username);
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getAssignee(), is(username));
	}

	@Parameters
	@Test
	public void testAssigneeIsSetWithCC(String cc, String expectedAssignee) {
		email.setCc(cc.split(";"));
		ruleSet.setFirstCcIsAssignee(true);
		ruleSet.setJiraCommunicator(getMockedJiraCommunicator());
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getAssignee(), is(expectedAssignee));
	}
	
	private Object parametersForTestAssigneeIsSetWithCC() {
		return $(
				$("vladimir.vasilyev@cern.ch", "vvasilye"),
				$("djsuprin@mail.ru", "testuser"),
				$("djsuprin@yandex.ru", "cpodevin"),
				$("vladimir.vasilyev@cern.ch;djsuprin@mail.ru;djsuprin@yandex.ru", "vvasilye"),
				$("djsuprin@mail.ru;vladimir.vasilyev@cern.ch", "testuser"),
				$("djsuprin@yandex.ru;djsuprin@mail.ru", "cpodevin")
		);
	}

	@Parameters
	@Test
	public void testAssigneeIsSetWithBothTokenAndCC(String tokenAssigneeValue, String cc, String expectedAssignee) {
		tokens.put(Tokens.ASSIGNEE, tokenAssigneeValue);
		ruleSet.setFirstCcIsAssignee(true);
		email.setCc(cc.split(";"));
		ruleSet.setJiraCommunicator(getMockedJiraCommunicator());
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getAssignee(), is(expectedAssignee));
	}

	private Object parametersForTestAssigneeIsSetWithBothTokenAndCC() {
		return $(
				$("vvasilye", "djsuprin@mail.ru;vladimir.vasilyev@cern.ch", "vvasilye"),
				$("testuser", "vladimir.vasilyev@cern.ch';djsuprin@mail.ru", "testuser"),
				$("cpodevin", "djsuprin@mail.ru;vladimir.vasilyev@cern.ch", "cpodevin"),
				$("", "djsuprin@mail.ru;vladimir.vasilyev@cern.ch", "testuser"),
				$("", "", null)
		);
	}

	@Parameters({"true", "false"})
	@Test
	public void testAssigneeIsNullWhenNoTokenOrCcIsSet(boolean firstIsCc) {
		ruleSet.setFirstCcIsAssignee(firstIsCc);
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertNull(issueDescriptor.getAssignee());
	}
	
	@Parameters
	@Test
	public void testAssigneeIsNotSetDueToInvalidToken(String tokenValue, boolean firstIsCc) {
		ruleSet.setFirstCcIsAssignee(firstIsCc);
		tokens.put(Tokens.ASSIGNEE, tokenValue);
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertNull(issueDescriptor.getAssignee());
	}
	
	private Object parametersForTestAssigneeIsNotSetDueToInvalidToken() {
		return $(
				$("", true),
				$("", false),
				$(null, true),
				$(null, false)
		);
	}
	
	// This test case covers the case when CC field is an empty array
	@Test
	public void testAssigneeIsNotSetDueToEmptyCcField() {
		ruleSet.setFirstCcIsAssignee(true);
		email.setCc(new String[0]);
		ruleSet.validateAssignee(tokens, issueDescriptor, email);
		assertNull(issueDescriptor.getAssignee());
	}

}
