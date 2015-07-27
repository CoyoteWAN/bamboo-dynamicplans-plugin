
package org.valens;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.ErrorLogEntry;
import com.atlassian.bamboo.build.SimpleLogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.repository.RepositoryDefinition;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.variable.VariableDefinitionContextImpl;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.spring.container.ContainerManager;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    CustomVariableContext customVariableContext = null;
    private static final String CUSTOM_BAMBOO_TASK_ACTION = "custom.bamboo.task.action";
    private static final String CUSTOM_BAMBOO_TASKS_REG_LIST = "custom.bamboo.tasks.reg.list";
    private static final String CUSTOM_BAMBOO_CONDITION_LIST = "custom.bamboo.condition.list";
    private static final String CUSTOMBAMBOOTASKLIST = "custom.bamboo.task.list";
    private static final String SCRIPT = "script:";
    
    public BuildContext getBuildContext()
    {
        return buildContext;
    }

    private static final Logger log = Logger
            .getLogger(GroovyProcessorBase.class);

    GroovyProcessorBase(BuildContext buildContext, CustomVariableContext  customVariableContext )
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
        BuildLogger buildLogger = getBuildLoggerManager()
                    .getLogger(this.buildContext.getPlanResultKey());
        
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
            

            Map customConfiguration = buildContext.getBuildDefinition().getCustomConfiguration();
            if (customConfiguration == null || customConfiguration.get(CUSTOM_BAMBOO_CONDITION_LIST) == null
                    || customConfiguration.get(CUSTOM_BAMBOO_CONDITION_LIST).toString().trim().length() == 0)
            {
                log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("conditions not set, skipping")));
            } else
            {
                String condition = customConfiguration.get(CUSTOM_BAMBOO_CONDITION_LIST).toString();
                String expression = customConfiguration.get(CUSTOM_BAMBOO_TASKS_REG_LIST).toString();
                if (expression == null || expression.trim().length() == 0)
                {
                    expression = ".*";
                }
                value = calculateGroovy("Condition", nestedVariables, condition);

                for (TaskDefinition td : this.buildContext.getTaskDefinitions())
                {
                    if (td.getUserDescription().matches(expression))
                    {
                        td.setEnabled(false);
                        log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("Disabling task:" + td.getUserDescription())));
                    }
                }  
            }

            if (customConfiguration == null || customConfiguration.get(CUSTOMBAMBOOTASKLIST) == null
                    || customConfiguration.get(CUSTOMBAMBOOTASKLIST).toString().trim().length() == 0)
            {

                log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("tasks list not set, skipping")));
            } else
            {
                String condition = customConfiguration.get(CUSTOMBAMBOOTASKLIST).toString();

                value = calculateGroovy("Condition", nestedVariables, condition);

                for (TaskDefinition td : this.buildContext.getTaskDefinitions())
                {
                    
                    
                    if (td.getUserDescription().matches(value))
                    {           
                        boolean state = true;
                        if(customConfiguration.get(CUSTOM_BAMBOO_TASK_ACTION) == null || customConfiguration.get(CUSTOM_BAMBOO_TASK_ACTION).toString().equalsIgnoreCase("true"))
                            state = false;
                        
                        td.setEnabled(state);
                        
                        
                        log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry("Setting state :" 
                                + state
                                + " for task " 
                                + td.getUserDescription())));
                    }
                    else
                    {
                        log.warn(buildLogger.addBuildLogEntry(new SimpleLogEntry( 
                                td.getUserDescription() + " is not matching " + value)));
                    }

                }
                
            }

        } catch (Exception e)
        {
            log.error("Bamboo Groovy Variables: an unexpected exception occurred."
                    + e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            log.warn(buildLogger.addBuildLogEntry(new ErrorLogEntry(sw.toString())));

            e.printStackTrace();
        }

    }
    

    private String calculateGroovy(String name, Map nestedVariables, String groovy)
    {

        BuildLogger buildLogger = getBuildLoggerManager()
                .getLogger(this.buildContext.getPlanResultKey());
        SimpleLogEntry logEntry = new SimpleLogEntry(
                "Groovy found in "
                + name);
        String value;

        Binding binding = new Binding();
        binding.setVariable("groovyVariableName", name);

        
        
        Iterator it = this.buildContext.getVariableContext()
                .getResultVariables().entrySet().iterator();

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
                String key = ((String) it1.next());
                String varname = key.trim().replaceAll("\\.", "_") + "_" + rdef.getPosition();
                value = rdef.getConfiguration().getString(key);
                if(varname.trim().matches("[a-zA-Z0-9_]*"))
                    binding.setVariable(varname, value);
            }
        }
        log.info(buildLogger.addBuildLogEntry("GroovyShell bindings: "));
        for ( it = binding.getVariables().keySet().iterator(); it.hasNext();)
        {
            String s = (String) it.next();    
            if(!s.toLowerCase().contains("password"))
                 log.info(buildLogger.addBuildLogEntry("    " + s + "=" + binding.getVariable(s)));
        }
        
        log.info(buildLogger.addBuildLogEntry("Groovy Shell starting"));
        Object result = null;
        try
        {
            GroovyShell shell = new GroovyShell(binding);

            result = shell.evaluate("try{ " + groovy + " }catch(Exception e){ e.printStackTrace(); }");
            
            if(result == null)
                result = "null";
            
            logEntry = new SimpleLogEntry(
                    "Groovy Result: "
                    + groovy + " replaced with " + result.toString());
            
            log.info(buildLogger.addBuildLogEntry(logEntry));
        } catch (Throwable e)
        {
            buildLogger.addErrorLogEntry(e.getMessage(), e);

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
