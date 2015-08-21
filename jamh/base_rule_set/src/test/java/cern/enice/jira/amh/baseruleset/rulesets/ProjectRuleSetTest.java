package cern.enice.jira.amh.baseruleset.rulesets;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;

public class ProjectRuleSetTest {

	ProjectRuleSet ruleset;
	IssueDescriptor output;
	
	@Before
	public void setup(){
		ruleset =  new ProjectRuleSet();
		ruleset.setJiraCommunicator(getMockedJiraCommunicator());
		output = new IssueDescriptor();
		ruleset.setRuleSetUtils(new RuleSetUtils());
		
	}
	
//	@Test
//	public void testPersonalNameIsAProject() throws Exception {
//		Configuration cfg = new Configuration();
//		Map<String, String> domainToProjectsMap = new HashMap<String, String>();
//		domainToProjectsMap.put("icecontrols.support@cern.ch", "ens");
//		cfg.setHandlerAddressesToDefaultProjectKeys(new HashMap<String, String>());
//		cfg.setDomainsToProjects(domainToProjectsMap);
//		ruleset.setConfiguration(cfg);
//		EMail from = new EMail();
//		from.setFrom(new EMailAddress("ENS", "icecosp", "cern.ch"));
//		ruleset.process(from, new HashMap<String,String>(), output);
//	    assertEquals("ENS", output.getProject());
//	
//	}
	
	@Test
	public void testToAddressMappedToAProjectCorrectly() throws Exception{
		
		Configuration cfg = new Configuration();
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("mtb.support@cern.ch", "MTB");
		cfg.setHandlerAddressesToDefaultProjectKeys(map);
		cfg.setDomainsToProjects(new HashMap<String, String>());
		ruleset.setConfiguration(cfg);
		EMail email = new EMail();
		email.setFrom(new EMailAddress("Brice Copy", "brice.copy", "cern.ch"));
		email.setTo(Arrays.asList(new EMailAddress[]{new EMailAddress("MTB Support", "MTB.Support", "cern.ch")}));
		
	    ruleset.process(email, new HashMap<String,String>(), output);
	    
	    assertEquals("MTB", output.getProject());
		
	}
	
	
	public JiraRestCommunicator getMockedJiraCommunicator() {
		Map<String, Map<String, Object>> userDataMap = new HashMap<String, Map<String, Object>>();
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		doReturn(true).when(jiraCommunicator).isValidProject("MTB");
		doReturn(true).when(jiraCommunicator).isValidProject("ens");
		
		return jiraCommunicator;
	}
}
