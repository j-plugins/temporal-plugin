package com.github.xepozz.temporal.common.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.util.execution.ParametersListUtil

class TemporalRunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: TemporalRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val temporalExecutable = configuration.temporalExecutable.takeIf { !it.isNullOrBlank() } ?: "temporal"

        val commandLine = GeneralCommandLine(temporalExecutable)
        commandLine.withWorkDirectory(environment.project.basePath)
        commandLine.addParameters("server", "start-dev")
        commandLine.addParameters("--port", configuration.port.toString())
        commandLine.addParameters("--ui-port", configuration.uiPort.toString())
        commandLine.addParameters("--metrics-port", configuration.metricsPort.toString())

        configuration.logLevel?.takeIf { it.isNotBlank() }?.let {
            commandLine.addParameters("--log-level", it)
        }

        for ((key, value) in configuration.dynamicConfigValues) {
            commandLine.addParameters("--dynamic-config-value", "$key=$value")
        }

        for ((key, value) in configuration.searchAttributes) {
            commandLine.addParameters("--search-attribute", "$key=$value")
        }

        configuration.additionalArgs?.takeIf { it.isNotBlank() }?.let {
            commandLine.addParameters(ParametersListUtil.parse(it))
        }

        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
}
