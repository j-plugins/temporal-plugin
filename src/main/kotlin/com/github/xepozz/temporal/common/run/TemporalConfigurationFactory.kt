package com.github.xepozz.temporal.common.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class TemporalConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): TemporalRunConfiguration {
        return TemporalRunConfiguration(project, this, "Temporal")
    }

    override fun getOptionsClass(): Class<out BaseState> = TemporalRunConfigurationOptions::class.java

    override fun getId(): String = "TemporalConfigurationFactory"
}