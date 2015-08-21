package cern.enice.jira.amh.baseruleset;

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
public class UpdateConfigurationTest extends BaseRuleSetBaseTest {

	// Updated method tests

	@Test
	public void testPropertiesObjectIsNull() {
		try {
			ruleSet.updated(null);
		} catch (ConfigurationException ex) {
			fail("Failed with ConfigurationException. However, no exception was expected.");
		}
	}

	private Dictionary fillDictionary(String defaultProjectKey, String defaultIssueTypeName,
			String defaultSummary, String ccAddressesAreWatchers, String firstCcIsAssignee,
			String externalWatchersCustomFieldId) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		if (defaultProjectKey != null) properties.put("defaultProjectKey", defaultProjectKey);
		if (defaultIssueTypeName != null) properties.put("defaultIssueTypeName", defaultIssueTypeName);
		if (defaultSummary != null) properties.put("defaultSummary", defaultSummary);
		if (ccAddressesAreWatchers != null) properties.put("ccAddressesAreWatchers", ccAddressesAreWatchers);
		if (firstCcIsAssignee != null) properties.put("firstCcIsAssignee", firstCcIsAssignee);
		if (externalWatchersCustomFieldId != null) properties.put("externalWatchersCustomFieldId", externalWatchersCustomFieldId);
		return properties;
	}

	@Parameters
	@Test
	public void testConfigurationIsNotUpdated(Dictionary<String, String> properties) {
		try {
			ruleSet.updated(properties);
		} catch (ConfigurationException ex) {
			return;
		}
		fail("Failed because expected ConfigurationException was never raised.");
	}

	private Object parametersForTestConfigurationIsNotUpdated() {
		Dictionary<String, String> properties1 = fillDictionary(null, "val", "val", "val", "val", "val");
		Dictionary<String, String> properties2 = fillDictionary("val", null, "val", "val", "val", "val");
		Dictionary<String, String> properties3 = fillDictionary("val", "val", null, "val", "val", "val");
		Dictionary<String, String> properties4 = fillDictionary("val", "val", "val", null, "val", "val");
		Dictionary<String, String> properties5 = fillDictionary("val", "val", "val", "val", null, "val");
		
		return $($(properties1), $(properties2), $(properties3), $(properties4), $(properties5));
	}

	@Parameters
	@Test
	public void testConfigurationIsUpdatedWithMinimalInput(String defaultProjectKey,
			String defaultIssueTypeName, String defaultSummary, String ccAddressesAreWatchers,
			String firstCcIsAssignee, String externalWatchersCustomFieldId) {
		Dictionary<String, String> properties = fillDictionary(defaultProjectKey,
				defaultIssueTypeName, defaultSummary, ccAddressesAreWatchers, firstCcIsAssignee,
				externalWatchersCustomFieldId);

		try {
			ruleSet.updated(properties);
		} catch (ConfigurationException ex) {
			fail("Failed with ConfigurationException. However, no exception was expected.");
		}

		assertThat(ruleSet.getDefaultProjectKey(), is(defaultProjectKey.toLowerCase()));
		assertThat(ruleSet.getDefaultIssueTypeName(), is(defaultIssueTypeName.toLowerCase()));
		assertThat(ruleSet.getDefaultSummary(), is(defaultSummary));
		assertThat(ruleSet.isCcAddressesAreWatchers(),
				is(Boolean.parseBoolean(ccAddressesAreWatchers)));
		assertThat(ruleSet.isFirstCcIsAssignee(), is(Boolean.parseBoolean(firstCcIsAssignee)));
		assertThat(ruleSet.getExternalWatchersCustomFieldId(), is(externalWatchersCustomFieldId));
	}

	private Object parametersForTestConfigurationIsUpdatedWithMinimalInput() {
		return $($("PROJECT", "BUG", "Summary", "true", "true", "12345"),
				$("project", "Bug", "Summary", "false", "false", "12345"),
				$("PROJECT", "BUG", "Summary", "yes", "no", "12345"),
				$("PROJECT", "BUG", "Summary", "", "   ", "12345"));
	}

}
