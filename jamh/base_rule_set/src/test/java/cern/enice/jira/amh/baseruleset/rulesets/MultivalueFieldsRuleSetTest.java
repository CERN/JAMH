package cern.enice.jira.amh.baseruleset.rulesets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mock;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.baseruleset.Tokens;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class MultivalueFieldsRuleSetTest {
	
	MultivalueFieldsRuleSet multivalueFieldsRuleSet = new MultivalueFieldsRuleSet();
	
	public JiraRestCommunicator getMockedJiraCommunicator() {
		Map<String, Map<String, Object>> userDataMap = new HashMap<String, Map<String, Object>>();
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		doReturn("support - labview").when(jiraCommunicator).getComponentRegisteredName(null, null, "support - labview");
		doReturn(true).when(jiraCommunicator).isValidProject("ens");
		
		return jiraCommunicator;
	}
	
	@Test
	public void test() throws EmailHandlingException {
		
		
		multivalueFieldsRuleSet.setLogger(new LogbackLogProvider());
		Map<String, String > token = new HashMap<String, String>();
		token.put(Tokens.COMPONENTS, "Support__-__LabVIEW");
		
		EMail email = new EMail();
		email.setFrom(new EMailAddress("Brice Copy", "brice.copy", "cern.ch"));
		email.setTo(Arrays.asList(new EMailAddress[]{new EMailAddress("ICE Control Support", "IceControls.support", "cern.ch")}));
		
		multivalueFieldsRuleSet.setJiraCommunicator(getMockedJiraCommunicator());
		IssueDescriptor issueDescriptor = new IssueDescriptor();
		
		issueDescriptor.setOriginalState(new IssueDescriptor());
		multivalueFieldsRuleSet.process(email, token, issueDescriptor);
		
		
		
		assertNotNull(issueDescriptor.getComponents());
		assertTrue(issueDescriptor.getComponents().contains("support - labview"));
	}
	
	

}
