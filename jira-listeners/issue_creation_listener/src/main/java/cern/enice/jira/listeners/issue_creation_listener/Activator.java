package cern.enice.jira.listeners.issue_creation_listener;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	IssueCreationListener issueCreationListener = new IssueCreationListener();
    	
    	manager.add(createComponent()
    			.setInterface(Object.class.getName(), null)
    			.setImplementation(issueCreationListener)
    			.add(createServiceDependency()
						.setService(MailService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createConfigurationDependency()
						.setPid("cern.enice.jira.listeners.issue_creation_listener")
				)
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
