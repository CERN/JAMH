package cern.enice.jira.amh.elasticsearch;

public interface ElasticSearchClientImplMBean {
	public String getElasticSearchHostName();
	public int getElasticSearchPort();
	public String getElasticSearchIndex();
	public String getElasticSearchType();
	public String getElasticSearchCluster();
}
