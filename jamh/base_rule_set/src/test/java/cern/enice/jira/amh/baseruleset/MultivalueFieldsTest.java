package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Contains test cases to test the following fields: Components, Fix Versions, Affected Versions
 * @author vvasilye
 *
 */
@RunWith(JUnitParamsRunner.class)
public class MultivalueFieldsTest extends BaseRuleSetBaseTest {
	
	@Parameters(method="parametersForComponentsField")
	@Test
	public void testComponentsAreSet(String tokenValue, String expectedValuesAsString) {
		Set<String> expectedValues = new HashSet<String>(Arrays.asList(expectedValuesAsString.split(";")));
		tokens.put(Tokens.COMPONENTS, tokenValue);
		issueDescriptor.setComponents(ruleSet.validateMultivalueField(tokens, Tokens.COMPONENTS, "NT", "Bug"));
		assertThat(issueDescriptor.getComponents(), is(expectedValues));
	}
	
	private Object parametersForComponentsField() {
		return $(
				$("advanced mail handler, component2, jira listeners", "Advanced Mail Handler;Component2;JIRA listeners"),
				$("jira listeners, advanced mail handler", "JIRA listeners;Advanced Mail Handler")
		);
	}
	
	@Parameters(method="parametersForFixAndAffectedVersionsFields")
	@Test
	public void testAffectedVersionsAreSet(String tokenValue, String expectedValuesAsString) {
		Set<String> expectedValues = new HashSet<String>(Arrays.asList(expectedValuesAsString.split(";")));
		tokens.put(Tokens.AFFECTED_VERSIONS, tokenValue);
		issueDescriptor.setAffectedVersions(ruleSet.validateMultivalueField(tokens, Tokens.AFFECTED_VERSIONS, "NT", "Bug"));
		assertThat(issueDescriptor.getAffectedVersions(), is(expectedValues));
	}
	
	@Parameters(method="parametersForFixAndAffectedVersionsFields")
	@Test
	public void testFixVersionsAreSet(String tokenValue, String expectedValuesAsString) {
		Set<String> expectedValues = new HashSet<String>(Arrays.asList(expectedValuesAsString.split(";")));
		tokens.put(Tokens.FIX_VERSIONS, tokenValue);
		issueDescriptor.setFixVersions(ruleSet.validateMultivalueField(tokens, Tokens.FIX_VERSIONS, "NT", "Bug"));
		assertThat(issueDescriptor.getFixVersions(), is(expectedValues));
	}
	
	private Object parametersForFixAndAffectedVersionsFields() {
		return $(
				$("5.0, 5.1", "5.0;5.1"),
				$("5.1", "5.1")
		);
	}
	
	@Parameters
	@Test
	public void testMultivalueFieldIsNotSetDueToUnknownValues(String token, String tokenValue) {
		tokens.put(token, tokenValue);
		issueDescriptor.setComponents(ruleSet.validateMultivalueField(tokens, Tokens.COMPONENTS, "NT", "Bug"));
		issueDescriptor.setAffectedVersions(ruleSet.validateMultivalueField(tokens, Tokens.AFFECTED_VERSIONS, "NT", "Bug"));
		issueDescriptor.setFixVersions(ruleSet.validateMultivalueField(tokens, Tokens.FIX_VERSIONS, "NT", "Bug"));
		assertNull(issueDescriptor.getComponents());
		assertNull(issueDescriptor.getAffectedVersions());
		assertNull(issueDescriptor.getFixVersions());
	}
	
	private Object parametersForTestMultivalueFieldIsNotSetDueToUnknownValues() {
		return $(
				$(Tokens.COMPONENTS, "abc, bcd, cde"),
				$(Tokens.AFFECTED_VERSIONS, "abc, bcd, cde"),
				$(Tokens.FIX_VERSIONS, "abc, bcd, cde")
		);
	}
	
	@Test
	public void testMultivalueFieldIsNotSetDueToMissingToken() {
		issueDescriptor.setComponents(ruleSet.validateMultivalueField(tokens, Tokens.COMPONENTS, "NT", "Bug"));
		assertNull(issueDescriptor.getComponents());
	}
	
	@Parameters
	@Test
	public void testMultivalueFieldValuesAreSplitByDifferentSeparator(String tokenValue, String expectedValuesAsString, String separator) {
		Set<String> expectedValues = new HashSet<String>(Arrays.asList(expectedValuesAsString.split(";")));
		tokens.put(Tokens.SEPARATOR, separator);
		tokens.put(Tokens.FIX_VERSIONS, tokenValue);
		issueDescriptor.setFixVersions(ruleSet.validateMultivalueField(tokens, Tokens.FIX_VERSIONS, "NT", "Bug"));
		assertThat(issueDescriptor.getFixVersions(), is(expectedValues));
	}
	
	private Object parametersForTestMultivalueFieldValuesAreSplitByDifferentSeparator() {
		return $(
				$("5.0, 5.1", "5.0;5.1", null),
				$("5.0, 5.1", "5.0;5.1", ""),
				$("5.0; 5.1", "5.0;5.1", ";"),
				$("5.0 5.1", "5.0;5.1", " "),
				$("5.1", "5.1", null),
				$("5.1::5.0", "5.1;5.0", "::")
		);
	}
}
