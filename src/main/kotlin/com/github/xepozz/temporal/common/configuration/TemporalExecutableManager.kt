package com.github.xepozz.temporal.common.configuration

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag

@Tag("executable")
data class TemporalExecutable(
    @Attribute("name")
    var name: String = "",
    @Attribute("path")
    var path: String = "",
) : BaseState()

@Service(Service.Level.PROJECT)
@State(name = "TemporalExecutables", storages = [Storage("temporal.xml")])
class TemporalExecutableManager(val project: Project) :
    SimplePersistentStateComponent<TemporalExecutableManagerState>(TemporalExecutableManagerState()) {
    val executables: MutableList<TemporalExecutable>
        get() = state.executables

    fun findByName(name: String?): TemporalExecutable? {
        if (name == null) return null
        return state.executables.find { it.name == name }
    }

    override fun loadState(state: TemporalExecutableManagerState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    companion object {
        fun getInstance(project: Project): TemporalExecutableManager = project.service()
    }
}

class TemporalExecutableManagerState : BaseState() {
    var executables by list<TemporalExecutable>()
}
