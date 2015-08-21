package cern.enice.jira.amh.jiracommunicator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;

import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.jira_rest_communicator.JiraRestCommunicator;
import cern.enice.jira.amh.logback.LogbackLogProvider;
import cern.enice.jira.amh.network_client.NetworkClientJerseyImpl;

public class JiraRestCommunicatorBaseTest {
	protected static Map<String, Object> createMeta;
	protected IssueDescriptor issueDescriptor;
	protected JiraRestCommunicator jiraCommunicator;
	protected static ObjectMapper objectMapper = new ObjectMapper();
	
	public static Object getObjectFromJsonFile(String filename) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonFileContent = IOUtils.toString(new FileInputStream(filename), "utf-8");
			return mapper.readValue(jsonFileContent, Object.class);
		} catch (IOException e) {
			System.out.println(e);
		}
		return null;
	}

	@BeforeClass
	public static void fetchCreateMeta() {
		createMeta = (Map<String, Object>)getObjectFromJsonFile("src/test/resources/create-meta.txt");
	}

	@Before
	public void setUp() {
		issueDescriptor = new IssueDescriptor();
		jiraCommunicator = spy(new JiraRestCommunicator());
		jiraCommunicator.setCreateMeta(createMeta);
		jiraCommunicator.setJiraBaseUrl("");
		jiraCommunicator.setJiraUsername("");
		jiraCommunicator.setJiraPassword("");

		jiraCommunicator.setNetworkClient(mock(NetworkClientJerseyImpl.class));
		jiraCommunicator.setLogger(mock(LogbackLogProvider.class));
	}
	
	IssueDescriptor fillIssueDescriptor(String project, String issueType, String summary, 
			String assignee, String reporter, String priority, String duedate, 
			String originalEstimate, String remainingEstimate,
			String environment, String description, String attachments, String key) {
		IssueDescriptor issueDescriptor = new IssueDescriptor();
		issueDescriptor.setKey(key);
		issueDescriptor.setProject(project);
		issueDescriptor.setIssueType(issueType);
		issueDescriptor.setSummary(summary);
		issueDescriptor.setAssignee(assignee);
		issueDescriptor.setReporter(reporter);
		issueDescriptor.setPriority(priority);
		issueDescriptor.setDueDate(duedate);
		issueDescriptor.setOriginalEstimate(originalEstimate);
		issueDescriptor.setRemainingEstimate(remainingEstimate);
		issueDescriptor.setEnvironment(environment);
		issueDescriptor.setDescription(description);
		if (attachments != null) {
			if (attachments.isEmpty())
				issueDescriptor.setAttachments(new String[0]);
			else
				issueDescriptor.setAttachments(attachments.split(";"));
		}
		return issueDescriptor;
	}
}
