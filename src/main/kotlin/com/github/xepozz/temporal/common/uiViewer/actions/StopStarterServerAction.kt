package com.github.xepozz.temporal.common.uiViewer.actions

import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class StopStarterServerAction() : AnAction("Stop Server", "", AllIcons.Actions.Suspend) {

    override fun actionPerformed(event: AnActionEvent) {
        val starterServerService = event.project!!.getService(StarterServerService::class.java)
        event.presentation.isEnabled = false

        starterServerService.stop()
    }

    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val starterServerService = project.getService(StarterServerService::class.java)

        event.presentation.isEnabled = starterServerService.isActive
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}