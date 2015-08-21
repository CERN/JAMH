package cern.enice.jira.amh.logback;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.LogProvider;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	LogProvider logbackLogProvider = new LogbackLogProvider();

    	manager.add(createComponent()
    			.setInterface(LogProvider.class.getName(), null)
				.setImplementation(logbackLogProvider)
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
