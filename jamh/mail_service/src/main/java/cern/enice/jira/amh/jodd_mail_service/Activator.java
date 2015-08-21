package cern.enice.jira.amh.jodd_mail_service;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;


public class Activator extends DependencyActivatorBase {
	
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	String mBeanName = "cern.enice.jira.amh:type=JoddMailService";
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	MailServiceUtils mailServiceUtils = new MailServiceUtils();
    	manager.add(createComponent()
				.setInterface(MailServiceUtils.class.getName(), null)
				.setImplementation(mailServiceUtils)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
		);
    	
    	Configuration configuration = new Configuration();
    	manager.add(createComponent()
				.setInterface(Configuration.class.getName(), null)
				.setImplementation(configuration)
				.add(createServiceDependency()
						.setService(MailServiceUtils.class).setRequired(true))
				.add(createConfigurationDependency()
						.setPid("cern.enice.jira.amh.jodd_mail_service")
				)
		);
    	
    	MailService joddMailService = new JoddMailService();
    	manager.add(createComponent()
				.setInterface(MailService.class.getName(), null)
				.setImplementation(joddMailService)
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
				.add(createServiceDependency()
						.setService(MailServiceUtils.class).setRequired(true))
		);
    	
    	try {
			mbs.registerMBean(joddMailService, new ObjectName(mBeanName));
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
