package cern.enice.jira.amh.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import cern.enice.jira.amh.api.ElasticSearchClient;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.utils.Utils;

public class ElasticSearchClientImpl implements ElasticSearchClient, ManagedService, ElasticSearchClientImplMBean {
	
	private static String DEFAULT_ELASTIC_SEARCH_CLUSTER = "elasticsearch";
	private static int DEFAULT_ELASTIC_SEARCH_PORT = 9300;
	
	// Service dependencies
	private volatile LogProvider logger;
	
	// Non-configurable fields
	private Client client;
	private ObjectMapper objectMapper = new ObjectMapper();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("-yyyy-MM");
	
	// Configurable fields
	private String elasticSearchHostName;
	private int elasticSearchPort;
	private String elasticSearchIndex;
	private String elasticSearchType;
	private String elasticSearchCluster;
	
	public String getElasticSearchHostName() {
		return elasticSearchHostName;
	}

	void setElasticSearchHostName(String elasticSearchHostName) {
		this.elasticSearchHostName = elasticSearchHostName;
	}

	public int getElasticSearchPort() {
		return elasticSearchPort;
	}

	void setElasticSearchPort(int elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getElasticSearchIndex() {
		return elasticSearchIndex;
	}

	void setElasticSearchIndex(String elasticSearchIndex) {
		this.elasticSearchIndex = elasticSearchIndex;
	}

	public String getElasticSearchCluster() {
		return elasticSearchCluster;
	}

	void setElasticSearchCluster(String elasticSearchCluster) {
		this.elasticSearchCluster = elasticSearchCluster;
	}

	public String getElasticSearchType() {
		return elasticSearchType;
	}

	void setElasticSearchType(String elasticSearchType) {
		this.elasticSearchType = elasticSearchType;
	}

	public void start() {
		//startClient();
		logger.log(LogProvider.INFO, "ElasticSearch Log Provider service is started.");
	}
	
	public void stop() {
		stopClient();
		logger.log(LogProvider.INFO, "ElasticSearch Log Provider service is stopped.");
	}
	
	private void startClient() {
		stopClient();
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", elasticSearchCluster).build();
		try {
			client = new TransportClient(settings).addTransportAddress(
					new InetSocketTransportAddress(
							elasticSearchHostName, elasticSearchPort));
		} catch (Exception e) {
			if (logger == null) return;
			logger.log(LogProvider.ERROR, "Couldn't start ElasticSearch client session.", e);
		}
	}
	
	private void stopClient() {
		if (client != null) {
			try {
				client.close();
			} catch (ElasticsearchException e) {
				if (logger == null) return;
				logger.log(LogProvider.ERROR, "Couldn't close ElasticSearch client session.");
			}
		}
	}
	
	private String getTimeBasedIndexName(String index) {
		return index + dateFormat.format(new Date(System.currentTimeMillis()));
	}
	
	@Override
	public void send(Map<String, Object> object) {
		send(object, elasticSearchType, getTimeBasedIndexName(elasticSearchIndex));
	}
	
	@Override
	public void send(Map<String, Object> object, String type) {
		send(object, type, getTimeBasedIndexName(elasticSearchIndex));
	}
	
	@Override
	public void send(Map<String, Object> object, String type, String index) {
		try {
			String esDocument = objectMapper.writeValueAsString(object);
			client.prepareIndex(index, type)
					.setSource(esDocument)
					.execute()
					.actionGet();
		} catch (NoNodeAvailableException e) {
			logger.log(LogProvider.WARNING, "No Elastic Search nodes are available to log object:", object);
		} 
		catch (Exception e) {
			logger.log(LogProvider.ERROR, "Couldn't send log entry to ElasticSearch:", object, e);
			startClient();
		}
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if (properties == null) return;
		elasticSearchHostName = Utils.updateStringProperty(properties, "elasticSearchHostName");		
		elasticSearchIndex = Utils.updateStringProperty(properties, "elasticSearchIndex");
		elasticSearchType = Utils.updateStringProperty(properties, "elasticSearchType");
		elasticSearchCluster = Utils.updateStringProperty(properties, "elasticSearchCluster",
				DEFAULT_ELASTIC_SEARCH_CLUSTER);
		String elasticSearchPortProperty = Utils.updateStringProperty(properties, "elasticSearchPort");
		try {
			elasticSearchPort = Integer.parseInt(elasticSearchPortProperty);
		} catch (NumberFormatException ex) {
			elasticSearchPort = DEFAULT_ELASTIC_SEARCH_PORT;
		}
		startClient();
	}

}