package cern.enice.jira.amh.api;

import java.util.List;

import cern.enice.jira.amh.dto.IssueDescriptor;
import cern.enice.jira.amh.dto.Result;
import cern.enice.jira.amh.dto.Transition;
import cern.enice.jira.amh.dto.User;

public interface JiraCommunicator {
	
	/**
	 * Creates new issue in JIRA 
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info 
	 * @return                  Contains result code and list of errors in case of failed operation
	 */
	public Result createIssue(IssueDescriptor issueDescriptor);
	
	/**
	 * Updates an issue in JIRA 
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info 
	 * @return                  Contains result code and list of errors in case of failed operation
	 */
	public Result updateIssue(IssueDescriptor issueDescriptor);
	
	/**
	 * Deletes JIRA issue 
	 * @param issueDescriptor   Contains data to fill issue fields with along with the workflow transition info 
	 * @return                  Contains result code and list of errors in case of failed operation
	 */
	public Result deleteIssue(IssueDescriptor issueDescriptor);
	
	/**
	 * Obtains an issue's descriptor by its key
	 * @param issueKey   Issue key
	 * @return           IssueDescriptor object
	 */
	public IssueDescriptor getIssue(String issueKey);
	
	/**
	 * Gets info about a JIRA user by its username or an email address
	 * @param username   Username or email address of a sought-for user
	 * @return           User information represented as a Map
	 */
	public User getUser(String username);
	
	/**
	 * Gets the list of available transitions for a given issue 
	 * @param issueKey   Key of an issue for which the available transitions are requested
	 * @return           Returns the list of issue transitions represented as a 
	 *                   key-value pairs (Map) where key is a transition name (lowercased) and 
	 *                   value is the corresponding extended information about the transition
	 */
	public List<Transition> getIssueTransitions(String issueKey);
	
	/**
	 * Gets the list of available issue resolutions
	 * @return   Returns the list of available issue resolutions
	 */
	public List<String> getIssueResolutions();
	
	/**
	 * Returns true if project with specified key exists, false - otherwise
	 * @param projectKey   prject key (case-insensitive)
	 * @return             true if project with specified key exists, false - otherwise
	 */
	public boolean isValidProject(String projectKey);
	
	public String getIssueTypeRegisteredName(String projectKey, String issueTypeName);
	
	public String getPriorityRegisteredName(String projectKey, String issueTypeName, String priorityName);
	
	public String getComponentRegisteredName(String projectKey, String issueTypeName, String componentName);
	
	public String getVersionRegisteredName(String projectKey, String issueTypeName, String versionName);
}
