package cern.enice.jira.amh.baseruleset;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DescriptionAndCommentTest extends BaseRuleSetBaseTest {

	@Parameters(method = "parametersForDescriptionAndCommentTests")
	@Test
	public void testDescriptionIsSetWhenIssueKeyIsNotSpecified(String bodyWithoutTokens, String bodyWithTokens, String commentAuthor) {
		email.setBody(bodyWithTokens);
		ruleSet.validateDescription(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getDescription(), is(bodyWithoutTokens));
	}

	@Parameters(method = "parametersForDescriptionAndCommentTests")
	@Test
	public void testDescriptionIsSetWhenIssueKeyIsSpecified(String bodyWithoutTokens, String bodyWithTokens, String commentAuthor) {
		issueDescriptor.setKey("nt-5");
		tokens.put(Tokens.DESCRIPTION, null);
		email.setBody(bodyWithTokens);
		ruleSet.validateDescription(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getDescription(), is(bodyWithoutTokens));
	}

	@Parameters(method = "parametersForDescriptionAndCommentTests")
	@Test
	public void testCommentIsSet(String bodyWithoutTokens, String bodyWithTokens, String commentAuthor) {
		// TODO these two properties should be parametrized
		issueDescriptor.setKey("nt-5");
		issueDescriptor.setCommentAuthor(commentAuthor);
		email.setBody(bodyWithTokens);
		//doReturn("comment prefix\n").when(ruleSet).getCommentPrefix(issueDescriptor.getCommentAuthor(), email);
		ruleSet.validateDescription(tokens, issueDescriptor, email);
		assertThat(issueDescriptor.getComment(), containsString(bodyWithoutTokens));
	}

	private Object parametersForDescriptionAndCommentTests() {
		return $(
				$("Test issue description or comment", "Test issue description or comment", "vvasilye"),
				$("Test issue description or comment", "#TOKEN1 #TOKEN2\nTest issue description or comment", null)
		);
	}
	
	@Parameters
	@Test
	public void testCommentVisibilityIsSet(String comment, String visibilityToken, String expectedVisibilityValue) {
		issueDescriptor.setKey("someproject-123");
		tokens.put(visibilityToken, null);
		email.setBody(comment);
		try {
		ruleSet.validateDescription(tokens, issueDescriptor, email);
		} catch (Exception e) {
			System.out.println(e);
		}
		assertThat(issueDescriptor.getCommentVisibleTo(), is(expectedVisibilityValue));
	}
	
	private Object parametersForTestCommentVisibilityIsSet() {
		return $(
				$("The Comment", "administrators", "Administrators"),
				$("The Comment", "developers", "Developers"),
				$("The Comment", "users", "Users")
		);
	}

}
