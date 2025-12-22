package com.github.xepozz.temporal.common.uiViewer

import com.github.xepozz.temporal.TemporalIcons
import com.github.xepozz.temporal.common.uiViewer.events.ServerListener
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import javax.swing.Icon

class ChangeIconListener(val project: Project) : ServerListener {
    override fun onStarterStarted() {
        setIcon(BuggregatorIcons.BUGGREGATOR_SUCCESS)
    }

    override fun onStarterTerminated(withError: Boolean) {
        setIcon(
            when (withError) {
                true -> BuggregatorIcons.BUGGREGATOR_FAILED
                false -> TemporalIcons.TEMPORAL
            }
        )
    }

    private fun setIcon(icon: Icon) {
        val toolWindowManager = ToolWindowManagerEx.getInstanceEx(project)
        val temporalWindow = toolWindowManager.getToolWindow("Temporal") ?: return

        invokeLater {
            temporalWindow.setIcon(icon)
        }
    }
}