package cern.enice.jira.amh.jiracommunicator;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Dictionary;
import java.util.Hashtable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.cm.ConfigurationException;

@RunWith(JUnitParamsRunner.class)
public class UpdateConfigurationTest extends JiraRestCommunicatorBaseTest {

	// Updated method tests

	@Test
	public void testPropertiesObjectIsNull() {
		try {
			jiraCommunicator.updated(null);
		} catch (ConfigurationException ex) {
			fail("Failed with ConfigurationException. However, no exception was expected.");
		}
	}

	private Dictionary fillDictionary(String jiraUsername, String jiraPassword, String jiraBaseUrl) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		if (jiraUsername != null) properties.put("jiraUsername", jiraUsername);
		if (jiraPassword != null) properties.put("jiraPassword", jiraPassword);
		if (jiraBaseUrl != null) properties.put("jiraBaseUrl", jiraBaseUrl);
		return properties;
	}

	@Parameters
	@Test(expected = ConfigurationException.class)
	public void testConfigurationIsNotUpdated(Dictionary<String, String> properties) throws ConfigurationException {
		jiraCommunicator.updated(properties);
	}

	private Object parametersForTestConfigurationIsNotUpdated() {
		Dictionary<String, String> properties1 = fillDictionary(null, "val", "val");
		Dictionary<String, String> properties2 = fillDictionary("val", null, "val");
		Dictionary<String, String> properties3 = fillDictionary("val", "val", null);
		
		Dictionary<String, String> properties4 = fillDictionary("", "val", "val");
		Dictionary<String, String> properties5 = fillDictionary("val", "", "val");
		Dictionary<String, String> properties6 = fillDictionary("val", "val", "");
		
		Dictionary<String, String> properties7 = fillDictionary("  ", "val", "val");
		Dictionary<String, String> properties8 = fillDictionary("val", "   ", "val");
		Dictionary<String, String> properties9 = fillDictionary("val", "val", "     ");
		
		return $(
				$(properties1), $(properties2), $(properties3),
				$(properties4), $(properties5), $(properties6),
				$(properties7), $(properties8), $(properties9)
		);
	}

	@Test
	public void testConfigurationIsUpdatedWithMinimalInput() {
		String jiraUsername = "username";
		String jiraPassword = "password";
		String jiraBaseUrl = "baseurl";
		Dictionary<String, String> properties = fillDictionary(jiraUsername, "password", "baseurl");

		try {
			jiraCommunicator.updated(properties);
		} catch (ConfigurationException ex) {
			fail("Failed with ConfigurationException. However, no exception was expected.");
		}

		assertThat(jiraCommunicator.getJiraUsername(), is(jiraUsername));
		assertThat(jiraCommunicator.getJiraPassword(), is(jiraPassword));
		assertThat(jiraCommunicator.getJiraBaseUrl(), is(jiraBaseUrl));
	}

}
