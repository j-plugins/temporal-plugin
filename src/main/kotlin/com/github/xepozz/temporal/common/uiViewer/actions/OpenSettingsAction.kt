package com.github.xepozz.temporal.common.uiViewer.actions

import com.github.xepozz.temporal.common.uiViewer.configuration.TemporalSettingsConfigurable
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class OpenSettingsAction() : AnAction("Open Settings", "", AllIcons.General.Settings) {
    override fun actionPerformed(event: AnActionEvent) {
        val settingsUtil = ShowSettingsUtil.getInstance()
        settingsUtil.showSettingsDialog(event.project, TemporalSettingsConfigurable::class.java)
    }
}