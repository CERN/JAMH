package cern.enice.jira.amh.baseruleset;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cern.enice.jira.amh.api.JiraCommunicator;
import cern.enice.jira.amh.api.LogProvider;
import cern.enice.jira.amh.api.MailService;
import cern.enice.jira.amh.api.RuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.AssigneeRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.AutoTriageRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.BaseRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.BasicFieldsRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.CommentAuthorRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.CommentRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.DeleteIssueRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.DescriptionRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.IssueKeyRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.IssueTypeRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.MultivalueFieldsRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.PriorityRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.ProjectRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.ReporterRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.ResolutionRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.SummaryRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.TimeTrackingRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.TransitionRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.WatchersRuleSet;
import cern.enice.jira.amh.baseruleset.rulesets.WorklogRuleSet;

public class Activator extends DependencyActivatorBase {
	
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	String mBeanName = "cern.enice.jira.amh.baseruleset:type=BaseRuleSet";
	
    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
    	
    	RuleSetUtils ruleSetUtils = new RuleSetUtils();
    	manager.add(createComponent()
    			.setInterface(RuleSetUtils.class.getName(), null)
    			.setImplementation(ruleSetUtils)
				.add(createServiceDependency()
						.setService(JiraCommunicator.class).setRequired(true)));
    	
    	Configuration configuration = new Configuration();
    	manager.add(createComponent()
    			.setInterface(Configuration.class.getName(), null)
    			.setImplementation(configuration)
    			.add(createConfigurationDependency()
    					.setPid("cern.enice.jira.amh.base_rule_set")));
    	
    	addRuleSet(manager, new BaseRuleSet(), 1000);
    	addRuleSet(manager, new IssueKeyRuleSet(), 1100);
    	addRuleSet(manager, new DeleteIssueRuleSet(), 1200);
    	addRuleSet(manager, new SummaryRuleSet(), 1300);
    	addRuleSet(manager, new AutoTriageRuleSet(), 1305);
    	addRuleSet(manager, new ProjectRuleSet(), 1400);
    	addRuleSet(manager, new IssueTypeRuleSet(), 1500);
    	addRuleSet(manager, new PriorityRuleSet(), 1600);
    	addRuleSet(manager, new MultivalueFieldsRuleSet(), 1700);
    	addRuleSet(manager, new TimeTrackingRuleSet(), 1800);
    	addRuleSet(manager, new WorklogRuleSet(), 1900);
    	addRuleSet(manager, new TransitionRuleSet(), 2000);
    	addRuleSet(manager, new ResolutionRuleSet(), 2100);
    	addRuleSet(manager, new AssigneeRuleSet(), 2200);
    	addRuleSet(manager, new ReporterRuleSet(), 2300);
    	addRuleSet(manager, new CommentAuthorRuleSet(), 2400);
    	addRuleSet(manager, new WatchersRuleSet(), 2500);
    	addRuleSet(manager, new DescriptionRuleSet(), 2600);
    	addRuleSet(manager, new CommentRuleSet(), 2700);
    	addRuleSet(manager, new BasicFieldsRuleSet(), 2800);
    	
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
    
    public void addRuleSet(DependencyManager manager, RuleSet ruleSet, int priority) {
    	Properties issueKeyRuleSetProperties = new Properties();
    	issueKeyRuleSetProperties.setProperty("jamh.priority", String.valueOf(priority));

    	manager.add(createComponent()
    			.setInterface(RuleSet.class.getName(), issueKeyRuleSetProperties)
    			.setImplementation(ruleSet)
    			.add(createServiceDependency()
						.setService(JiraCommunicator.class).setRequired(true))
    			.add(createServiceDependency()
						.setService(MailService.class).setRequired(true))
				.add(createServiceDependency()
						.setService(LogProvider.class).setRequired(true))
				.add(createServiceDependency()
						.setService(RuleSetUtils.class).setRequired(true))
				.add(createServiceDependency()
						.setService(Configuration.class).setRequired(true))
		);
    }

}
