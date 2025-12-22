package com.github.xepozz.temporal.common.uiViewer.actions

import com.github.xepozz.temporal.TemporalIcons
import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ClearConsoleViewAction() : AnAction("Clear Console", "", TemporalIcons.CLEAR_OUTPUTS) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val starterServerService = project.getService(StarterServerService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            starterServerService.clearConsoleView()
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

}