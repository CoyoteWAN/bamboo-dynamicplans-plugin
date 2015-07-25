/**
 * * Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen
 * (cnfree2000@hotmail.com) **
 */
package org.valens;

import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.util.concurrent.NotNull;
import org.apache.log4j.Logger;

public class DynamicTaskPreBuildAction extends BaseConfigurableBuildPlugin implements
        CustomPreBuildAction
{

    private static final Logger log = Logger
            .getLogger(DynamicTaskPreBuildAction.class);
    GroovyProcessorBase gpb = null;
    
    @Override
    public void init(@NotNull BuildContext buildContext)
    {
        this.buildContext = buildContext;
        gpb = new GroovyProcessorBase(buildContext);
    }

    @NotNull
    @Override
    public BuildContext call() throws Exception
    {        
        return gpb.call();
    }
    
}
