package com.github.xepozz.temporal.common.uiViewer

import com.github.xepozz.temporal.common.uiViewer.actions.OpenSettingsAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationUtils {
    private val GROUP_ID = "Temporal"
    private val GROUP_WINDOW_ID = "TemporalWindow"

    fun notifyWindowError(project: Project, title: String, content: String) {
        Notification(GROUP_WINDOW_ID, title, content, NotificationType.ERROR)
            .addAction(OpenSettingsAction())
            .notify(project)
    }

    fun notifyWarning(project: Project, title: String, content: String) {
        Notification(GROUP_ID, title, content, NotificationType.WARNING)
            .addAction(OpenSettingsAction())
            .notify(project)
    }
}