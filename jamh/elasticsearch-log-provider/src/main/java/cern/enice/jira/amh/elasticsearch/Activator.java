package cern.enice.jira.amh.elasticsearch;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import cern.enice.jira.amh.api.ElasticSearchClient;
import cern.enice.jira.amh.api.LogProvider;

public class Activator extends DependencyActivatorBase {
	
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	String mBeanName = "cern.enice.jira.amh:type=ElasticSearchClientImpl";
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	ElasticSearchClient elasticSearchLogProvider = new ElasticSearchClientImpl();

    	manager.add(createComponent()
    			.setInterface(ElasticSearchClient.class.getName(), null)
				.setImplementation(elasticSearchLogProvider)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createConfigurationDependency()
						.setPid("cern.enice.jira.amh.elasticsearch")
				)
		);
    	
    	try {
			mbs.registerMBean(elasticSearchLogProvider, new ObjectName(mBeanName));
		} catch (Exception ex) {
			
		}
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    	try {
    		mbs.unregisterMBean( new ObjectName(mBeanName) );
    	} catch (Exception ex) {
    		
    	}
    }

}
