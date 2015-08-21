package cern.enice.jira.listeners.second_line_support_request_listener;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;

public class Activator extends DependencyActivatorBase {
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	SecondLineSupportRequestListener secondLineSupportRequestListener = new SecondLineSupportRequestListener();
    	
    	manager.add(createComponent()
    			.setInterface(Object.class.getName(), null)
    			.setImplementation(secondLineSupportRequestListener)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createServiceDependency()
						.setService(MailService.class).setRequired(true))
				.add(createConfigurationDependency()
						.setPid("cern.enice.jira.listeners.second_line_support_request_listener")
				)
		);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
    }

}
