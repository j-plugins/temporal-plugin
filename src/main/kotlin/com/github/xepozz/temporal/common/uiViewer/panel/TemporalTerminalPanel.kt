package com.github.xepozz.temporal.common.uiViewer.panel

import com.github.xepozz.temporal.common.uiViewer.actions.ClearConsoleViewAction
import com.github.xepozz.temporal.common.uiViewer.actions.OpenSettingsAction
import com.github.xepozz.temporal.common.uiViewer.actions.StartStarterServerAction
import com.github.xepozz.temporal.common.uiViewer.actions.StopStarterServerAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent
import javax.swing.JPanel

class TemporalTerminalPanel(
    val terminalViewComponent: JComponent,
) : SimpleToolWindowPanel(false, false) {
    init {
        createToolBar()
        createContent()
    }

    private fun createToolBar() {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(StartStarterServerAction())
        actionGroup.add(ClearConsoleViewAction())
        actionGroup.add(StopStarterServerAction())
        actionGroup.addSeparator()
        actionGroup.add(OpenSettingsAction())

        val actionToolbar = ActionManager.getInstance().createActionToolbar("TemporalToolbar", actionGroup, false)
        actionToolbar.targetComponent = this

        val toolBarPanel = JPanel(GridLayout())
        toolBarPanel.add(actionToolbar.component)

        toolbar = toolBarPanel
    }

    private fun createContent() {
        val responsivePanel = JPanel(BorderLayout())
        responsivePanel.add(terminalViewComponent, BorderLayout.CENTER)
        responsivePanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                terminalViewComponent.revalidate()
                terminalViewComponent.repaint()
            }
        })

        setContent(responsivePanel)
    }
}