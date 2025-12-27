package com.github.xepozz.temporal.common.configuration

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.Logger

object TemporalVersionChecker {
    private val LOG = Logger.getInstance(TemporalVersionChecker::class.java)

    fun checkVersion(path: String): String {
        if (path.isBlank()) return "Path is empty"
        return try {
            val commandLine = GeneralCommandLine(path, "--version")
            val output = ExecUtil.execAndGetOutput(commandLine)
            if (output.exitCode == 0) {
                output.stdout.trim()
            } else {
                output.stderr.trim().ifBlank { "Exit code ${output.exitCode}" }
            }
        } catch (e: Exception) {
            LOG.warn("Failed to check Temporal version for $path", e)
            e.message ?: "Unknown error"
        }
    }
}
