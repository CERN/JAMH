package cern.enice.jira.amh.baseruleset;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.baseruleset.rulesets.BaseRuleSet;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;
import cern.enice.jira.amh.utils.Utils;

public class BaseRuleSetBaseTest {
	protected BaseRuleSet ruleSet;
	// private String pathToAttachments = "c:\\jira-listeners-home\\temp\\";
	protected IssueDescriptor issueDescriptor;
	protected IssueDescriptor originalIssueDescriptor;
	protected EMail email;
	protected Map<String, String> tokens;
	public static Map<String, Object> createMeta;
	
	@Before
	public void setUp() {
		ruleSet = spy(new BaseRuleSet());
		ruleSet.setDefaultProjectKey("nt");
		ruleSet.setDefaultIssueTypeName("bug");
		ruleSet.setDefaultSummary("(no summary)");
		Map<String, String> d2p = ruleSet.getDomainsToProjects();
		d2p.put("gmail", "abc");
		d2p.put("yandex", "nt");
		LogProvider logger = mock(LogProvider.class);
		ruleSet.setLogger(logger);
		issueDescriptor = new IssueDescriptor();
		originalIssueDescriptor = mock(IssueDescriptor.class);
		tokens = new HashMap<String, String>();
		email = new EMail();
	}
	
	public JiraRestCommunicator getMockedJiraCommunicator() {
		Map<String, Map<String, Object>> userDataMap = new HashMap<String, Map<String, Object>>();
		JiraRestCommunicator jiraCommunicator = mock(JiraRestCommunicator.class);
		userDataMap.put("vvasilye", (Map<String, Object>)Utils.getMapFromJsonFile("src/test/resources/user-data-1.txt"));
		userDataMap.put("testuser", (Map<String, Object>)Utils.getMapFromJsonFile("src/test/resources/user-data-2.txt"));
		userDataMap.put("cpodevin", (Map<String, Object>)Utils.getMapFromJsonFile("src/test/resources/user-data-3.txt"));
		doReturn(null).when(jiraCommunicator).getUser(anyString());
		doReturn(userDataMap.get("vvasilye")).when(jiraCommunicator).getUser("vladimir.vasilyev@cern.ch");
		doReturn(userDataMap.get("testuser")).when(jiraCommunicator).getUser("djsuprin@mail.ru");
		doReturn(userDataMap.get("cpodevin")).when(jiraCommunicator).getUser("djsuprin@yandex.ru");
		doReturn(userDataMap.get("vvasilye")).when(jiraCommunicator).getUser("vvasilye");
		doReturn(userDataMap.get("testuser")).when(jiraCommunicator).getUser("testuser");
		doReturn(userDataMap.get("cpodevin")).when(jiraCommunicator).getUser("cpodevin");
		//doReturn(null).when(jiraCommunicator).getUser("vvasilye@cern.ch");
		return jiraCommunicator;
	}
	
	@BeforeClass
	public static void fetchCreateMeta() {
		// Obtaining test create meta from local file
		createMeta = (Map<String, Object>)getMapFromJsonFile("src/test/resources/create-meta.txt");
	}
}
