package com.github.xepozz.temporal.common.uiViewer.services

import com.github.xepozz.temporal.common.uiViewer.NotificationUtils
import com.github.xepozz.temporal.common.uiViewer.configuration.TemporalSettings
import com.github.xepozz.temporal.common.uiViewer.events.EventService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter

@Service(Service.Level.PROJECT)
class StarterServerService(var project: Project) : Disposable {
    val isActive
        get() = starterProcess?.isAlive == true

    var starterProcess: Process? = null
    var browser: JBCefBrowser? = null
    var consoleView: ConsoleView? = null

    val eventService = EventService.getInstance(project)

    override fun dispose() {
        starterProcess?.destroy()
        consoleView?.dispose()
        browser?.dispose()
    }

    fun clearConsoleView() {
        consoleView?.clear()
    }

    fun loadBrowser() {
        val browser = browser ?: return
        val settings = TemporalSettings.getInstance(project)

        println("load browser url ${settings.address} $browser")

        browser.loadURL(settings.address)

        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadError(
                browser: CefBrowser,
                frame: CefFrame,
                errorCode: CefLoadHandler.ErrorCode,
                errorText: String,
                failedUrl: String
            ) {
                NotificationUtils.notifyWindowError(
                    project,
                    "Failed to load Starter Server",
                    """
                    URL: $failedUrl
                    Reason: $errorText
                    """.trimIndent()
                )
                println("on load error: (${errorCode.code}, ${errorCode.name}) $errorText, $failedUrl")
            }

        }, browser.cefBrowser)
    }

    fun reloadBrowser() {
//        println("reload ${browser?.cefBrowser?.url}")
        browser?.cefBrowser?.reloadIgnoreCache()
    }

    fun hardRestartBrowser() {
//        println("hard restart ${browser?.cefBrowser?.url}")
//        unloadBrowser()
        loadBrowser()
    }

    fun start(listeners: Collection<ProcessListener> = emptyList()) {
        if (isActive) return

        val settings = TemporalSettings.getInstance(project)

        hardRestartBrowser()

        if (settings.runStarter) {
            val listeners = listeners + object : ProcessListener {
                override fun startNotified(event: ProcessEvent) {
                    reloadBrowser()
                }
            }
            runStarterProcess(settings, listeners)
        }
    }

    private fun runStarterProcess(settings: TemporalSettings, listeners: Collection<ProcessListener>) {
        val projectBasePath = project.basePath!!

        val commandArgs = buildList {
            add(settings.binary.replace("%project%", projectBasePath))
            addAll(settings.command.trim().replace("%port%", settings.port.toString()).split(' '))
        }

        CoroutineScope(Dispatchers.IO).launch {
            executeCommand(commandArgs, listeners)
        }
    }

    fun stop() {
        if (!isActive) return

        starterProcess?.destroy()
    }

    private suspend fun executeCommand(commandArgs: List<String>, listeners: Collection<ProcessListener>) =
        withContext(Dispatchers.IO) {
            val command = GeneralCommandLine(commandArgs)

            val processHandler = KillableColoredProcessHandler.Silent(command)
            processHandler.setShouldKillProcessSoftly(false)
            processHandler.setShouldDestroyProcessRecursively(true)

            listeners.forEach { processHandler.addProcessListener(it) }

            val serverStartedChannel = Channel<Boolean>()

            processHandler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (event.text.contains("Server:")) {
                        serverStartedChannel.trySend(true)
                    }
                }

                override fun startNotified(event: ProcessEvent) {
                    eventService.fireStarted()
                }
                override fun processTerminated(event: ProcessEvent) {
                    serverStartedChannel.trySend(false)

                    println("process terminated: ${event.exitCode} $event")
                    eventService.fireTerminated(event.exitCode != 0)
                }
            })

            ProcessTerminatedListener.attach(processHandler, project, "Temporal server has been terminated.")

            consoleView?.attachToProcess(processHandler)
            consoleView?.requestScrollingToEnd()

            processHandler.startNotify()

            starterProcess = processHandler.process

            val result = withTimeoutOrNull(2000) {
                serverStartedChannel.receive()
            }

            if (result != true) {
                processHandler.killProcess()
                NotificationUtils.notifyWarning(
                    project,
                    "Temporal server startup took a long time",
                    """
                    It seems like the Temporal server has not started properly.
                    """.trimIndent()
                )
            }
        }
}