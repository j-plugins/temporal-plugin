package com.github.xepozz.temporal.common.uiViewer

import com.github.xepozz.temporal.common.uiViewer.actions.OpenInSystemBrowserAction
import com.github.xepozz.temporal.common.uiViewer.actions.OpenSettingsAction
import com.github.xepozz.temporal.common.uiViewer.actions.RefreshPageAction
import com.github.xepozz.temporal.common.uiViewer.panel.BrowserBuilder
import com.github.xepozz.temporal.common.uiViewer.panel.TemporalTerminalPanel
import com.github.xepozz.temporal.common.uiViewer.panel.TemporalWebUIPanel
import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.filters.UrlFilter
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowContentUiType
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.content.ContentFactory

open class TemporalWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = BrowserBuilder.build()

        val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        consoleView.addMessageFilter(UrlFilter())

        val contentFactory = ContentFactory.getInstance()
        val contentManager = toolWindow.contentManager

        val starterServerService = toolWindow.project.getService(StarterServerService::class.java)
        starterServerService.browser = browser
        starterServerService.consoleView = consoleView

        val webLayout = TemporalWebUIPanel(browser.component)

        val terminalLayout = TemporalTerminalPanel(consoleView.component)

        val actionGroup = DefaultActionGroup().apply {
            add(RefreshPageAction(browser))
            addSeparator()
            add(OpenSettingsAction())
            add(OpenInSystemBrowserAction(browser))
        }

        val toolWindow = toolWindow as ToolWindowEx
        toolWindow.setAdditionalGearActions(actionGroup)
        toolWindow.setDefaultContentUiType(ToolWindowContentUiType.COMBO)
        toolWindow.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true")
        toolWindow.setTitleActions(
            listOf(
                RefreshPageAction(browser),
                OpenInSystemBrowserAction(browser),
            )
        )

        contentFactory.apply {
            createContent(webLayout, "Web UI", false).apply {
                this.isPinnable = true
                this.isCloseable = false
                contentManager.addContent(this)
            }

            createContent(terminalLayout, "Terminal", false).apply {
                this.isPinnable = true
                this.isCloseable = false
                contentManager.addContent(this)
            }
        }

        starterServerService.start()
    }
}