package cern.enice.jira.amh.api;

import java.util.Map;

public interface ElasticSearchClient {

	/**
	 * Sends a documents of a default type to a default index in Elastic Search 
	 * @param object
	 */
	public void send(Map<String, Object> object);
	
	/**
	 * Sends a document of a given type to a default index in Elastic Search
	 * @param object
	 * @param index
	 */
	public void send(Map<String, Object> object, String type);
	
	/**
	 * Sends a document of a given type to a given index in Elastic Search
	 * @param object
	 * @param type
	 * @param index
	 */
	public void send(Map<String, Object> object, String type, String index);
}
