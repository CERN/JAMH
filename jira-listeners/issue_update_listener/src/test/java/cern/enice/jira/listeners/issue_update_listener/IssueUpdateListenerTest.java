package cern.enice.jira.listeners.issue_update_listener;

import static junitparams.JUnitParamsRunner.$;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.dto.EMail;

@RunWith(JUnitParamsRunner.class)
public class IssueUpdateListenerTest {
	
	@Parameters
	@Test
	public void testMailIsConfiguredCorrectly(String webhookJsonData) {
		IssueUpdateListener iul = new IssueUpdateListener();
		LogProvider logProvider = mock(LogProvider.class);
		MailService mailService = mock(MailService.class);
		iul.setLogger(logProvider);
		iul.setMailService(mailService);
		try {
			iul.process(webhookJsonData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		verify(mailService).sendEmail(any(EMail.class));
	}
	
	private Object parametersForTestMailIsConfiguredCorrectly() throws FileNotFoundException, IOException {
		String webhookJsonData = IOUtils.toString(new FileInputStream("src/test/resources/issue-update-webhook.txt"), "utf-8");
		return $(
				$(webhookJsonData)
		);
	}
}
