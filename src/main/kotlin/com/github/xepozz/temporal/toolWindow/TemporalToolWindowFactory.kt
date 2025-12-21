package com.github.xepozz.temporal.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory


class TemporalToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
//        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
//        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

//        private val service = toolWindow.project.service<MyProjectService>()
//
//        fun getContent() = JBPanel<JBPanel<*>>().apply {
//            val label = JBLabel(TemporalBundle.message("randomLabel", "?"))
//
//            add(label)
//            add(JButton(TemporalBundle.message("shuffle")).apply {
//                addActionListener {
//                    label.text = TemporalBundle.message("randomLabel", service.getRandomNumber())
//                }
//            })
//        }
    }
}
