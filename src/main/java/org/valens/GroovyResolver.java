/**
 * * Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen
 * (cnfree2000@hotmail.com) **
 */
package org.valens;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.build.SimpleLogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.task.AbstractBuildTask;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.variable.VariableDefinitionContextImpl;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class GroovyResolver extends AbstractBuildTask implements
        CustomPreBuildAction
{

    private static final Logger log = Logger
            .getLogger(GroovyResolver.class);
    GroovyProcessorBase gpb = null;
    
    public void init(@NotNull BuildContext buildContext)
    {
        this.buildContext = buildContext;
        gpb = new GroovyProcessorBase(buildContext);
    }

    public ErrorCollection validate(BuildConfiguration config)
    {
        gpb.setBuildConfiguration(config);
        return null;
    }

    @NotNull
    public BuildContext call() throws Exception
    {
        
        return gpb.call();
    }
    


}
