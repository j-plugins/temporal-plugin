package com.github.xepozz.temporal.common.uiViewer.actions

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

class OpenInSystemBrowserAction(val browser: JBCefBrowser) :
    AnAction("Open In System Browser", "", AllIcons.Actions.InlayGlobe) {
    override fun actionPerformed(event: AnActionEvent) {
        BrowserUtil.browse(browser.cefBrowser.url)
    }
}