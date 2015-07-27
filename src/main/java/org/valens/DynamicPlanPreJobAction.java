/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.valens;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.v2.build.BuildContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author IHutuleac
 */
public class DynamicPlanPreJobAction implements PreJobAction {

    @Override
    public void execute(StageExecution se, BuildContext bc) {
        try {
            GroovyProcessorBase gpb = new GroovyProcessorBase(bc);
            gpb.call();
        } catch (Exception ex) {
            Logger.getLogger(DynamicPlanPreJobAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
