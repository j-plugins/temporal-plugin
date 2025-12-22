package com.github.xepozz.temporal.common.uiViewer.configuration

import com.github.xepozz.temporal.common.uiViewer.services.StarterServerService
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "TemporalSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class TemporalSettings(val project: Project) :
    SimplePersistentStateComponent<TemporalSettingsState>(TemporalSettingsState()) {
    var runStarter
        get() = state.runStarter
        set(value) {
            val changed = state.runStarter != value
            state.runStarter = value
            if (changed) {
                restartStarterServerAsync(project)
            }
        }

    var address
        get() = state.address ?: ""
        set(value) {
            val changed = state.address != value
            state.address = value
            if (changed) {
                restartStarterServerAsync(project)
            }
        }

    var port
        get() = state.port
        set(value) {
            val changed = state.port != value
            state.port = value
            if (changed) {
                restartStarterServerAsync(project)
            }
        }

    var command
        get() = state.command ?: ""
        set(value) {
            val changed = state.command != value
            state.command = value
            if (changed) {
                restartStarterServerAsync(project)
            }
        }

    var binary
        get() = state.binary ?: ""
        set(value) {
            val changed = state.binary != value
            state.binary = value
            if (changed) {
                restartStarterServerAsync(project)
            }
        }

    companion object {
        fun getInstance(project: Project): TemporalSettings = project.getService(TemporalSettings::class.java)
    }
}

class TemporalSettingsState : BaseState() {
    var runCommand by property(true)
    var runStarter by property(true)
    var binary by string("%project%/temporal")
    var command by string("server start-dev --port 7233 --ui-port %port% --log-level error")
    var port by property(8234)

    var address by string("http://127.0.0.1:8234")
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