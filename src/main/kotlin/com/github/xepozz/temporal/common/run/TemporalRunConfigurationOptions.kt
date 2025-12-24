package com.github.xepozz.temporal.common.run

import com.intellij.execution.configurations.RunConfigurationOptions

open class TemporalRunConfigurationOptions : RunConfigurationOptions() {
    var temporalExecutable by string("")
    var port by property(7233)
    var uiPort by property(8233)
    var metricsPort by property(57271)
    var logLevel by string("info")
    var dynamicConfigValues by map<String, String>()
    var searchAttributes by map<String, String>()
    var additionalArgs by string("")
}
