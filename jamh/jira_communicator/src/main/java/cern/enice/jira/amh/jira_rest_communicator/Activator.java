package cern.enice.jira.amh.jira_rest_communicator;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;

public class Activator extends DependencyActivatorBase {
	
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	String mBeanName = "cern.enice.jira.amh:type=JiraRestCommunicator";
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	Configuration configuration = new Configuration();
    	manager.add(createComponent()
    			.setInterface(Configuration.class.getName(), null)
    			.setImplementation(configuration)
    			.add(createConfigurationDependency()
    					.setPid("cern.enice.jira.amh.jira_rest_communicator")));
    	
    	MetaDataService metaDataService = new MetaDataService();
    	manager.add(createComponent()
				.setInterface(MetaDataService.class.getName(), null)
				.setImplementation(metaDataService)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	GetIssueService getIssueService = new GetIssueService();
    	manager.add(createComponent()
				.setInterface(GetIssueService.class.getName(), null)
				.setImplementation(getIssueService)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	RestOperationsService restOperationsService = new RestOperationsService();
    	manager.add(createComponent()
				.setInterface(RestOperationsService.class.getName(), null)
				.setImplementation(restOperationsService)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	IssueOperatingHelperService issueOperatingHelperService = new IssueOperatingHelperService();
    	manager.add(createComponent()
				.setInterface(IssueOperatingHelperService.class.getName(), null)
				.setImplementation(issueOperatingHelperService)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	IssueOperatingService issueOperatingService = new IssueOperatingService();
    	manager.add(createComponent()
				.setInterface(IssueOperatingService.class.getName(), null)
				.setImplementation(issueOperatingService)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(IssueOperatingHelperService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	JiraCommunicator jiraRestCommunicator = new JiraRestCommunicator();
    	manager.add(createComponent()
				.setInterface(JiraCommunicator.class.getName(), null)
				.setImplementation(jiraRestCommunicator)
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createServiceDependency()
						.setService(IssueOperatingService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(GetIssueService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(RestOperationsService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(MetaDataService.class).setRequired(true))
		);
    	
    	try {
			mbs.registerMBean(configuration, new ObjectName(mBeanName));
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
