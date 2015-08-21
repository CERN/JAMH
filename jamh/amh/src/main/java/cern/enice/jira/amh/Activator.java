package cern.enice.jira.amh;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.ElasticSearchClient;
import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;

public class Activator extends DependencyActivatorBase {
	
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	String mBeanName = "cern.enice.jira.amh:type=AdvancedMailHandler";
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	AdvancedMailHandler advancedMailHandler = new AdvancedMailHandler();
    	
    	// Create main ExternalMailHandler service
		manager.add(createComponent()
				.setInterface(AdvancedMailHandler.class.getName(), null)
				.setImplementation(advancedMailHandler)
				.add(createServiceDependency()
						.setService(MailService.class)
						.setRequired(true)
				).add(createServiceDependency()
						.setService(JiraCommunicator.class)
						.setRequired(true)
				).add(createServiceDependency()
						.setService(ElasticSearchClient.class)
						.setRequired(true)
				).add(createServiceDependency()
						.setService(LogProvider.class)
						.setRequired(true)
				).add(createServiceDependency()
						.setService(RuleSet.class, "(jamh.priority=*)")
						.setRequired(true)
						.setCallbacks("ruleSetAdded", "ruleSetRemoved")
				).add(createConfigurationDependency()
						.setPid("cern.enice.jira.amh")
				)
		);
		
		try {
			mbs.registerMBean(advancedMailHandler, new ObjectName(mBeanName));
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
