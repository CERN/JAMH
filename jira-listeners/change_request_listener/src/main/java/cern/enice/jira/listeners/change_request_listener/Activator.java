package cern.enice.jira.listeners.change_request_listener;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	ChangeRequestListener changeRequestListener = new ChangeRequestListener();
    	
    	manager.add(createComponent()
    			.setInterface(Object.class.getName(), null)
    			.setImplementation(changeRequestListener)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createServiceDependency()
						.setService(NetworkClient.class).setRequired(true))
				.add(createServiceDependency()
						.setService(JiraCommunicator.class).setRequired(true))
				.add(createConfigurationDependency()
						.setPid("cern.enice.jira.listeners.change_request_listener")
				)
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
