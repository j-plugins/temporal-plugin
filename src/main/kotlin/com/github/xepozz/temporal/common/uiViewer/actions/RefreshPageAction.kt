package com.github.xepozz.temporal.common.uiViewer.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RefreshPageAction(val browser: JBCefBrowser) : AnAction("Refresh Page", "", AllIcons.Actions.Refresh) {
    override fun actionPerformed(event: AnActionEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            browser.cefBrowser.reloadIgnoreCache()
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

}