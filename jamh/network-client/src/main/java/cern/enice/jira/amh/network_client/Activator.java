package cern.enice.jira.amh.network_client;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.NetworkClient;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	NetworkClient networkClient = new NetworkClientJerseyImpl();

    	manager.add(createComponent()
				.setInterface(NetworkClient.class.getName(), null)
				.setImplementation(networkClient)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
