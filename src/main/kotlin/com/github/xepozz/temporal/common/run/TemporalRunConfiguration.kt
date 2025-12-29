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

    var temporalExecutableName: String? by options::temporalExecutableName

    var port: Int by options::port
    var uiPort: Int by options::uiPort
    var metricsPort: Int by options::metricsPort

    var logLevel: String? by options::logLevel

    var dynamicConfigValues: MutableMap<String, String> by options::dynamicConfigValues

    var searchAttributes: MutableMap<String, String> by options::searchAttributes
    var additionalArgs: String? by options::additionalArgs

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return TemporalSettingsEditor(project, this)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return TemporalRunProfileState(environment, this)
    }
}
