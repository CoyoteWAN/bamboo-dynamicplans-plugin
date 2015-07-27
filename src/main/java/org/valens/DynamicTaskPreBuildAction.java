package org.valens;

import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.v2.build.BaseConfigurableBuildPlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.util.concurrent.NotNull;
import org.apache.log4j.Logger;

public class DynamicTaskPreBuildAction extends BaseConfigurableBuildPlugin implements
        CustomPreBuildAction
{

    private static final Logger log = Logger
            .getLogger(DynamicTaskPreBuildAction.class);
    CustomVariableContext customVariableContext = null;

    public DynamicTaskPreBuildAction(CustomVariableContext customVariableContext)
    {
        this.customVariableContext = customVariableContext;
    }

    GroovyProcessorBase gpb = null;

    public CustomVariableContext getCustomVariableContext()
    {
        return customVariableContext;
    }

    public void setCustomVariableContext(CustomVariableContext customVariableContext)
    {
        this.customVariableContext = customVariableContext;
    }

    @Override
    public void init(@NotNull BuildContext buildContext)
    {
        this.buildContext = buildContext;
        gpb = new GroovyProcessorBase(buildContext, customVariableContext);
    }

    @NotNull
    @Override
    public BuildContext call() throws Exception
    {
        return gpb.call();
    }

}
