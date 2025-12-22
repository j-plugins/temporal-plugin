package com.github.xepozz.temporal.common.endpoints

import com.github.xepozz.temporal.TemporalIcons
import com.github.xepozz.temporal.common.extensionPoints.Workflow
import com.intellij.microservices.endpoints.EndpointType
import com.intellij.microservices.endpoints.EndpointsFilter
import com.intellij.microservices.endpoints.EndpointsProvider
import com.intellij.microservices.endpoints.FrameworkPresentation
import com.intellij.microservices.endpoints.ModuleEndpointsFilter
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import java.util.function.Supplier
import javax.swing.Icon
import com.github.xepozz.temporal.common.model.Workflow as WorkflowModel

class TemporalWorkflowEndpointsProvider : EndpointsProvider<WorkflowEndpointGroup, WorkflowEndpoint> {
    override val endpointType: EndpointType = EndpointType("Temporal Workflow", null, Supplier { "Temporal Workflow" })
    override val presentation: FrameworkPresentation = FrameworkPresentation("Temporal Workflow", "Temporal Workflow", TemporalIcons.TEMPORAL)

    override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<WorkflowEndpointGroup> {
        if (filter !is ModuleEndpointsFilter) return emptyList()
        return Workflow.getWorkflows(project, filter.module).map { WorkflowEndpointGroup(it) }
    }

    override fun getEndpoints(group: WorkflowEndpointGroup): Iterable<WorkflowEndpoint> {
        return listOf(WorkflowEndpoint(group.workflow))
    }

    override fun getDocumentationElement(group: WorkflowEndpointGroup, endpoint: WorkflowEndpoint): PsiElement? {
        return endpoint.workflow.psiAnchor?.dereference()
    }

    override fun getNavigationElement(group: WorkflowEndpointGroup, endpoint: WorkflowEndpoint): PsiElement? {
        return endpoint.workflow.psiAnchor?.dereference()
    }

    override fun getModificationTracker(project: Project): ModificationTracker = ModificationTracker.NEVER_CHANGED

    override fun getStatus(project: Project): EndpointsProvider.Status = EndpointsProvider.Status.AVAILABLE

    override fun isValidEndpoint(group: WorkflowEndpointGroup, endpoint: WorkflowEndpoint): Boolean = true

    override fun getEndpointPresentation(group: WorkflowEndpointGroup, endpoint: WorkflowEndpoint): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String = endpoint.workflow.id
            override fun getLocationString(): String? = endpoint.workflow.psiAnchor?.dereference()?.containingFile?.name
            override fun getIcon(unused: Boolean): Icon = TemporalIcons.WORKFLOW
        }
    }
}

data class WorkflowEndpointGroup(val workflow: WorkflowModel)
data class WorkflowEndpoint(val workflow: WorkflowModel)
