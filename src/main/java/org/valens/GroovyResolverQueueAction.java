
package org.valens;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.SimpleLogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.buildqueue.manager.CustomPreBuildQueuedAction;
import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.spring.container.ContainerManager;
import java.util.Map;
import org.apache.log4j.Logger;

public class GroovyResolverQueueAction implements
        CustomPreBuildQueuedAction
{

    private static final Logger log = Logger
            .getLogger(GroovyResolverQueueAction.class);
    
    GroovyProcessorBase gpb = null;
    
    private BuildContext buildContext;

    public void init(BuildContext bc)
    {
        this.buildContext = buildContext;
        gpb = new GroovyProcessorBase(buildContext);
    }

    public AdministrationConfiguration getAdministrationConfiguration()
    {
        return ((AdministrationConfiguration) ContainerManager
                .getComponent("administrationConfiguration"));
    }

    private boolean isLicenseValid()
    {
        return true;
    }

    private boolean isLicenseEnabled()
    {
        return false;
    }

    @Override
    public BuildContext call() throws InterruptedException, Exception
    {
        
        
        
        log.info("Queue Action called.");
        
        return gpb.call();
    }
}