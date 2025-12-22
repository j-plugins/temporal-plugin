package com.github.xepozz.temporal.common.uiViewer.actions

import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class StartStarterServerAction() : AnAction("Start Server", "", AllIcons.Actions.Execute) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val starterServerService = project.getService(StarterServerService::class.java)

        event.presentation.isEnabled = false

        starterServerService.start()
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val starterServerService = project.getService(StarterServerService::class.java)

        event.presentation.isEnabled = !starterServerService.isActive
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

}