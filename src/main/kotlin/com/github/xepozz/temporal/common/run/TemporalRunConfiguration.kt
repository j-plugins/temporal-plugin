package com.github.xepozz.temporal.common.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

open class TemporalRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<TemporalRunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): TemporalRunConfigurationOptions {
        return super.getOptions() as TemporalRunConfigurationOptions
    }

    var temporalExecutable: String?
        get() = options.temporalExecutable
        set(value) {
            options.temporalExecutable = value
        }

    var port: Int
        get() = options.port
        set(value) {
            options.port = value
        }

    var logLevel: String?
        get() = options.logLevel
        set(value) {
            options.logLevel = value
        }

    var dynamicConfigValues: MutableMap<String, String>
        get() = options.dynamicConfigValues
        set(value) {
            options.dynamicConfigValues = value
        }

    var searchAttributes: MutableMap<String, String>
        get() = options.searchAttributes
        set(value) {
            options.searchAttributes = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return TemporalSettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return TemporalRunProfileState(environment, this)
    }
}
