package com.github.xepozz.temporal.common.uiViewer.panel

import com.intellij.openapi.ui.SimpleToolWindowPanel
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent
import javax.swing.JPanel

class TemporalWebUIPanel(
    val webViewComponent: JComponent,
) : SimpleToolWindowPanel(false, false) {
    init {
        createContent()
    }

    private fun createContent() {
        val responsivePanel = JPanel(BorderLayout())
        responsivePanel.add(webViewComponent, BorderLayout.CENTER)
        responsivePanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                webViewComponent.revalidate()
                webViewComponent.repaint()
            }
        })

        setContent(responsivePanel)
    }
}