package com.github.xepozz.temporal.common.uiViewer.configuration

import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@State(name = "TemporalSettings", storages = [Storage("temporal.xml")])
@Service(Service.Level.PROJECT)
class TemporalSettings : BaseState(), PersistentStateComponent<TemporalSettings> {
    var runStarter by property(true)
//    var binary by string("%project%/temporal")
//    var command by string("server start-dev --port 7233 --ui-port %port% --log-level error")
    var uiViewerConfiguration by string()
//    var port by property(8234)

    var address by string("http://127.0.0.1:8234")

    override fun getState() = this
    override fun loadState(state: TemporalSettings) = copyFrom(state)

    companion object {
        fun getInstance(project: Project) = project.service<TemporalSettings>()
    }
}


fun restartStarterServerAsync(project: Project) {
    val starterServerService = project.getService(StarterServerService::class.java)
    if (starterServerService.isActive) {
        starterServerService.stop()
        starterServerService.start()
    } else {
        starterServerService.hardRestartBrowser()
    }
}