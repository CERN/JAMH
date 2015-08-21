package cern.enice.jira.amh.baseruleset.rulesets;

import java.util.Map;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.Configuration;
import cern.enice.jira.amh.baseruleset.RuleSetUtils;
import cern.enice.jira.amh.baseruleset.Tokens;
import cern.enice.jira.amh.dto.EMail;
import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.utils.EmailHandlingException;

public class CommentRuleSet implements RuleSet {

	// Service dependencies
	private volatile LogProvider logger;
	@SuppressWarnings("unused")
	private volatile JiraCommunicator jiraCommunicator;
	@SuppressWarnings("unused")
	private volatile MailService mailService;
	@SuppressWarnings("unused")
	private volatile Configuration configuration;
	private volatile RuleSetUtils ruleSetUtils;
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is started 
	 */
	public void start() {
		logger.log(LogProvider.INFO, "CommentRuleSet is started.");
	}
	
	/**
	 * OSGi lifecycle callback method which is called when BaseRuleSet service is stopped 
	 */
	public void stop() {
		logger.log(LogProvider.INFO, "CommentRuleSet is stopped.");
	}

	@Override
	public void process(EMail email, Map<String, String> tokens, IssueDescriptor issueDescriptor)
			throws EmailHandlingException {
		String bodyWithoutTokens = ruleSetUtils.getEmailBodyWithoutTokens(email.getBody());
		if (issueDescriptor.getKey() == null || tokens.containsKey(Tokens.DESCRIPTION) || 
				bodyWithoutTokens == null || bodyWithoutTokens.isEmpty()) return;
		String commentPrefix = getCommentPrefix(issueDescriptor.getCommentAuthor(), email);
		issueDescriptor.setComment(commentPrefix + bodyWithoutTokens);
		// Set comment visibility if the corresponding tag is specified
		if (tokens.containsKey(Tokens.VISIBLE_TO_ADMINISTRATORS))
			issueDescriptor.setCommentVisibleTo("Administrators");
		else if (tokens.containsKey(Tokens.VISIBLE_TO_DEVELOPERS))
			issueDescriptor.setCommentVisibleTo("Developers");
		else if (tokens.containsKey(Tokens.VISIBLE_TO_USERS))
			issueDescriptor.setCommentVisibleTo("Users");
	}

	/**
	 * JIRA REST API allows to post comments only on behalf of the user who
	 * triggers the HTTP request (in our case it will always be Icecontrols
	 * Support). This method constructs a header text for a comment explicitly
	 * saying that the comment was added by email and who is its actual author.
	 * 
	 * @param commentAuthor
	 *            Username or null if the email sender is not a known JIRA user
	 * @param email
	 *            Email descriptor object containing email headers and
	 *            attachments
	 * @return   Returns prefix message for a comment, saying who is a comment author
	 */
	public static String getCommentPrefix(String commentAuthor, EMail email) {
		String commentedByText;
		if (commentAuthor != null) {
			commentedByText = "Commented via email by " + commentAuthor + ":\n\n";
		} else {
			commentedByText = "{panel:bgColor=yellow}Commented via email by an unknown user (" + email.getFrom().toString()
					+ "):{panel}\n\n";
		}
		return commentedByText;
	}

}
