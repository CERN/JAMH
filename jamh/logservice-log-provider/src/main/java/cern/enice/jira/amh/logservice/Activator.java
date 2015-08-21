package cern.enice.jira.amh.logservice;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import cern.enice.jira.amh.api.LogProvider;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	LogProvider logServiceLogProvider = new LogServiceLogProvider();

    	manager.add(createAspectService(LogProvider.class, null, 50)
				.setImplementation(logServiceLogProvider)
				.add(createServiceDependency()
						.setService(LogService.class)
						.setRequired(true)
				)
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
