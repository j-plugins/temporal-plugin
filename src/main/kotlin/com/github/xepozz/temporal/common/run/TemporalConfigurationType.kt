package com.github.xepozz.temporal.common.run

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.TemporalIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import javax.swing.Icon

class TemporalConfigurationType : ConfigurationType {
    companion object {
        fun getInstance(): TemporalConfigurationType {
            return ConfigurationTypeUtil.findConfigurationType(TemporalConfigurationType::class.java)
        }
    }

    override fun getDisplayName(): String = TemporalBundle.message("run.configuration.type.name")

    override fun getConfigurationTypeDescription(): String = TemporalBundle.message("run.configuration.type.name")

    override fun getIcon(): Icon = TemporalIcons.TEMPORAL

    override fun getId(): String = "TemporalConfigurationType"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(TemporalConfigurationFactory(this))
    }
}

