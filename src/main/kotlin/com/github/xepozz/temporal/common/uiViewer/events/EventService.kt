package com.github.xepozz.temporal.common.uiViewer.events

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class EventService(val project: Project) {
    val messageBus: ServerListener = project.messageBus.syncPublisher(ServerListener.TOPIC)

    fun fireStarted() {
//        println("EventService. fire started")
        messageBus.onStarterStarted()
    }

    fun fireTerminated(withError: Boolean) {
//        println("EventService. fire terminated")
        messageBus.onStarterTerminated(withError)
    }

    companion object {
        fun getInstance(project: Project): EventService = project.getService(EventService::class.java)
    }
}