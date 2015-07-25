package org.valens;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.v2.build.configuration.MiscellaneousBuildConfigurationPlugin;
import org.jetbrains.annotations.NotNull;

public class DynamicConfigurator extends BaseBuildConfigurationAwarePlugin implements MiscellaneousBuildConfigurationPlugin {
    @Override
    public boolean isApplicableTo(@NotNull Plan plan)
    {
        return plan instanceof Job;
    }

}