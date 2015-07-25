/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.valens;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.SimpleLogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.repository.RepositoryDefinition;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.variable.VariableDefinitionContextImpl;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;

import org.apache.log4j.Logger;

/**
 *
 * @author IHutuleac
 */
public class GroovyProcessorBase
{

    BuildContext buildContext = null;
    
    public BuildContext getBuildContext()
    {
        return buildContext;
    }

    private static final Logger log = Logger
            .getLogger(GroovyProcessorBase.class);

    GroovyProcessorBase(BuildContext buildContext)
    {
        this.buildContext = buildContext;
    }

    public BuildContext call() throws Exception
    {
        processContext(buildContext.getVariableContext().getDefinitions());

        return this.buildContext;
    }

    private void processContext(Map<String, VariableDefinitionContext> context)
    {
        try
        {
            
            
        
            Map<String, VariableDefinitionContext> nestedVariables = Maps.newHashMap();

            Iterator it = context.entrySet().iterator();

            String value;
            
            it = context.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry item = (Map.Entry) it.next();

                value = ((VariableDefinitionContext) item.getValue())
                        .getValue();
                String key = ((VariableDefinitionContext) item.getValue())
                        .getKey();
                if (value.startsWith(SCRIPT))
                {
                    value = calculateGroovy(key, nestedVariables, value.substring(SCRIPT.length()));
                }

                VariableDefinitionContextImpl variable = new VariableDefinitionContextImpl(
                        key, value,
                        ((VariableDefinitionContext) item.getValue())
                        .getVariableType());
                nestedVariables.put(key, variable);
            }

            try
            {
                context.clear();
                context.putAll(nestedVariables);
            } catch (UnsupportedOperationException e)
            {
                for (String key : nestedVariables.keySet())
                {
                    VariableDefinitionContext item = (VariableDefinitionContext) nestedVariables
                            .get(key);
                    if (item != null)
                    {
                        String value1 = item.getValue();
                        if (value1 != null)
                        {
                            this.buildContext.getVariableContext()
                                    .addResultVariable(key, value1);
                            
                        }
                    }
                }
            }
            BuildLogger buildLogger = getBuildLoggerManager()
                .getLogger(this.buildContext.getPlanResultKey());
            
            Map customConfiguration = buildContext.getBuildDefinition().getCustomConfiguration();
            if(customConfiguration == null || customConfiguration.get("custom.valens.condition") == null)
            {
                
                log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("conditions not set, skipping")));
            }
            else
            {
                String condition = customConfiguration.get("custom.valens.condition").toString();
                String expression = customConfiguration.get("custom.valens.tasks.reg").toString();
                if(expression == null || expression.trim().length() == 0)
                    expression = ".*";
                value = calculateGroovy("Condition", nestedVariables, condition);
                if(value.equalsIgnoreCase("true"))
                {
                    for(TaskDefinition td : this.buildContext.getTaskDefinitions())
                    {
                        if(td.getUserDescription().matches(expression))
                        {
                            td.setEnabled(false);
                            log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("Disabling task:" + td.getUserDescription())));
                        
                        }
                        
                    }
                }
            }
            
        } catch (Exception e)
        {
            log.error("Bamboo Nested Variables: an unexpected exception occurred."
                    + e.getMessage());
        }

    }
    private static final String SCRIPT = "script:";

    private String calculateGroovy(String name, Map nestedVariables, String s)
    {

        BuildLogger buildLogger = getBuildLoggerManager()
                .getLogger(this.buildContext.getPlanResultKey());
        SimpleLogEntry logEntry = new SimpleLogEntry(
                "Groovy Variable found in ${bamboo."
                + name);
        log.warn(buildLogger.addBuildLogEntry(logEntry));
        String value;

        Binding binding = new Binding();
        binding.setVariable("groovyVariableName", name);

        Iterator it = this.buildContext.getVariableContext()
                .getEffectiveVariables().entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry item = (Map.Entry) it.next();

            value = ((VariableDefinitionContext) item.getValue())
                    .getValue();
            String key = ((VariableDefinitionContext) item.getValue())
                    .getKey();
            key = key.replace("\\.", "_");
            binding.setVariable(key, value);
        }


        it = this.buildContext.getRepositoryDefinitions().iterator();
        while (it.hasNext())
        {
            RepositoryDefinition rdef = (RepositoryDefinition) it.next();
            Iterator it1 = rdef.getConfiguration().getKeys();
            while (it1.hasNext())
            {
                String key = (String) it1.next();

                value = rdef.getConfiguration().getString(key);
                binding.setVariable(key.replaceAll("\\.", "_")+"_"+rdef.getPosition(), value);
            }
        }
        log.info(buildLogger.addBuildLogEntry("GroovyShell starting"));
        Object result =  null;
        try{
        GroovyShell shell = new GroovyShell(binding);

        result = shell.evaluate(s);
        logEntry = new SimpleLogEntry(
                "Groovy Variable found in ${bamboo."
                + s + " replaced with " + result.toString());
        log.info(buildLogger.addBuildLogEntry(logEntry));
        }
        catch(Exception e)
        {
            buildLogger.addErrorLogEntry(e.getMessage(),e);
            
            e.printStackTrace();
        }
        return result.toString();

    }

    private BuildLoggerManager getBuildLoggerManager()
    {
        return ((BuildLoggerManager) ContainerManager
                .getComponent("buildLoggerManager"));
    }
}
