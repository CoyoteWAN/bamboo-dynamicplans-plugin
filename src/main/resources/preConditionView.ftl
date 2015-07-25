[#if build.buildDefinition.customConfiguration.get('custom.bamboo.condition.list')?has_content ]
    [@ui.bambooInfoDisplay titleKey='Dynamic plan configuration' float=false height='160px']
        [@ww.label label='Condition' ]
            [@ww.param name='value']${build.buildDefinition.customConfiguration.get('custom.bamboo.condition.list')?if_exists}[/@ww.param]
        [/@ww.label]

        [@ww.label label='Tasks to disabled based on the condition' ]
            [@ww.param name='value']${build.buildDefinition.customConfiguration.get('custom.bamboo.tasks.reg.list')?if_exists}[/@ww.param]
        [/@ww.label]

        [@ww.label label='Direct Tasks Regex' ]
            [@ww.param name='value']${build.buildDefinition.customConfiguration.get('custom.bamboo.task.list')?if_exists}[/@ww.param]
        [/@ww.label]

        [@ww.label label='Direct Tasks Desired State' ]
            [@ww.param name='value']${build.buildDefinition.customConfiguration.get('custom.bamboo.task.action')?if_exists}[/@ww.param]
        [/@ww.label]
    [/@ui.bambooInfoDisplay]
[/#if]