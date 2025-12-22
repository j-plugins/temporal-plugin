package com.github.xepozz.temporal.common.uiViewer.panel

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandlerAdapter
import java.awt.datatransfer.StringSelection

object BrowserBuilder {
    private const val MENU_ID_REFRESH = 26501
    private const val MENU_ID_COPY_URL = 26502
    private const val MENU_ID_OPEN_EXTERNAL = 26503

    fun build(): JBCefBrowser {
        val browser = JBCefBrowser.createBuilder()
            .setEnableOpenDevToolsMenuItem(true)
            .build()

        browser.jbCefClient.addContextMenuHandler(MyContextMenuHandler, browser.cefBrowser)

        return browser
    }

    object MyContextMenuHandler : CefContextMenuHandlerAdapter() {
        override fun onBeforeContextMenu(
            browser: CefBrowser,
            frame: CefFrame,
            params: CefContextMenuParams,
            model: CefMenuModel
        ) {
            // model.clear()

            model.addSeparator()

            model.addItem(MENU_ID_REFRESH, "Refresh")
            model.addSeparator()
            model.addItem(MENU_ID_COPY_URL, "Copy URL")
            model.addItem(MENU_ID_OPEN_EXTERNAL, "Open in Browser")
        }

        override fun onContextMenuCommand(
            browser: CefBrowser,
            frame: CefFrame,
            params: CefContextMenuParams,
            commandId: Int,
            eventFlags: Int
        ): Boolean {
            return when (commandId) {
                MENU_ID_REFRESH -> {
                    browser.reload()
                    true
                }

                MENU_ID_COPY_URL -> {
                    CopyPasteManager.getInstance().setContents(
                        StringSelection(browser.url)
                    )
                    true
                }

                MENU_ID_OPEN_EXTERNAL -> {
                    BrowserUtil.browse(browser.url)
                    true
                }

                else -> false
            }
        }
    }
}