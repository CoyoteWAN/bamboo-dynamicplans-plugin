[#if plan?? && plan.buildDefinition?? && plan.buildDefinition.customConfiguration.get('custom.valens.condition')?has_content ]
	[@ui.bambooInfoDisplay titleKey='Conditions' float=false height='80px']
	
        [@ww.label label='Condition' ]
                [@ww.param name='value']${plan.buildDefinition.customConfiguration.get('custom.valens.condition')!}[/@ww.param]
        [/@ww.label]

        [@ww.label label='Tsks' ]
                [@ww.param name='value']${plan.buildDefinition.customConfiguration.get('custom.valens.tasks.reg')!}[/@ww.param]
        [/@ww.label]

	[/@ui.bambooInfoDisplay]
[/#if]