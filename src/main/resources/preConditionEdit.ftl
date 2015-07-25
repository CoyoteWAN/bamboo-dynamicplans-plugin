[@ui.bambooSection title='Dynamic Tasks De-Activation' ]
    [@ww.textarea name='custom.bamboo.condition.list' 
            label='Condition' 
            description='Groovy that will return String True or False if job should be skipped or not' /]
    [@ww.textfield name='custom.bamboo.tasks.reg.list' 
            label='Task Matcher' 
            description='Regular expression to indicate which tasks to disable (based on description)' /]
[/@ui.bambooSection ]

[@ui.bambooSection title='Dynamic Tasks Switcher' ]

    [@ww.textarea name='custom.bamboo.task.list' 
            label='Groovy REGEX calculator' class='text-area long-field '
            description='Groovy that will return a regular expression. Any tasks with matching description will be toggled.' /]

    [@ww.checkbox label='Task Should be Enabled' name='custom.bamboo.task.action' toggle='true' description='Desired Matched Task State' /]

[/@ui.bambooSection ]

