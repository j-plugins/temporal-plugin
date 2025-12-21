package com.github.xepozz.temporal.common.extensionPoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.lazyDumbAwareExtensions
import com.github.xepozz.temporal.common.model.Workflow as WorkflowModel

interface Workflow {
    fun getWorkflows(project: Project): List<WorkflowModel>

    companion object {
        val WORKFLOW_EP = ExtensionPointName.create<Workflow>("com.github.xepozz.temporal.workflow")

        fun getWorkflows(project: Project): List<WorkflowModel> {
            return WORKFLOW_EP.lazyDumbAwareExtensions(project).flatMap { it.getWorkflows(project) }.toList()
        }
    }
}
