package cern.enice.jira.amh.baseruleset;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.Tokens;
import cern.enice.jira.amh.baseruleset.rulesets.BaseRuleSet;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.EMailAddress;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class BaseRuleSetTest {

	@Test
	public void test() throws EmailHandlingException {
		BaseRuleSet brs = new BaseRuleSet();
		EMail email = new EMail();
		email.setFrom(new EMailAddress("Brice Copy", "brice.copy", "cern.ch"));
		email.setTo(Arrays.asList(new EMailAddress[]{new EMailAddress("ICE Control Support", "IceControls.support", "cern.ch")}));
		email.setSubject("my problem #RESOLVE #PROJECT=ENS #COMPONENTS=Support - LabVIEW");
        IssueDescriptor issueDescriptor = new IssueDescriptor();
		issueDescriptor.setOriginalState(new IssueDescriptor());
		
		Configuration cfg = new Configuration();
		cfg.setIgnoreTokensPatterns(new ArrayList<String>());
		brs.setConfiguration(cfg);
		brs.setLogger(new LogbackLogProvider());
		Map<String, String> tokens = new HashMap<String, String>();
		brs.process(email, tokens, issueDescriptor);
		
		assertTrue(tokens.containsKey(Tokens.COMPONENTS));
		assertEquals("Support - LabVIEW", tokens.get(Tokens.COMPONENTS));
	}

}
