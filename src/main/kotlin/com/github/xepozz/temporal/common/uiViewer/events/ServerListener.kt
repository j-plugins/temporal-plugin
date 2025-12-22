package com.github.xepozz.temporal.common.uiViewer.events

import com.intellij.util.messages.Topic

interface ServerListener {
    companion object {
        @JvmField
        val TOPIC = Topic.create("com.github.xepozz.temporal.common.uiViewer.events.ServerListener", ServerListener::class.java)
    }

    fun onStarterStarted()

    fun onStarterTerminated(withError: Boolean)
}