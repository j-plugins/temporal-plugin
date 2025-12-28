package com.github.xepozz.temporal.common.configuration

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@State(name = "TemporalExecutables", storages = [Storage("temporal.xml")])
@Service(Service.Level.PROJECT)
class TemporalExecutablesSettings : BaseState(), PersistentStateComponent<TemporalExecutablesSettings> {
    var executables by list<TemporalExecutable>()

    fun findByName(name: String?): TemporalExecutable? {
        if (name == null) return null
        return state.executables.find { it.name == name }
    }

    companion object {
        fun getInstance(project: Project) = project.service<TemporalExecutablesSettings>()
    }

    override fun getState() = this
    override fun loadState(state: TemporalExecutablesSettings) = copyFrom(state)
}
