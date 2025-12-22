package com.github.xepozz.temporal.common.endpoints

import com.github.xepozz.temporal.TemporalIcons
import com.github.xepozz.temporal.common.extensionPoints.Activity
import com.intellij.microservices.endpoints.EndpointType
import com.intellij.microservices.endpoints.EndpointsFilter
import com.intellij.microservices.endpoints.EndpointsProvider
import com.intellij.microservices.endpoints.FrameworkPresentation
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import java.util.function.Supplier
import javax.swing.Icon
import com.github.xepozz.temporal.common.model.Activity as ActivityModel

class TemporalActivityEndpointsProvider : EndpointsProvider<ActivityEndpointGroup, ActivityEndpoint> {
    override val endpointType: EndpointType = EndpointType("Temporal Activity", null, Supplier { "Temporal Activity" })
    override val presentation: FrameworkPresentation = FrameworkPresentation("Temporal Activity", "Temporal Activity", TemporalIcons.TEMPORAL)

    override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<ActivityEndpointGroup> {
        return Activity.getActivities(project).map { ActivityEndpointGroup(it) }
    }

    override fun getEndpoints(group: ActivityEndpointGroup): Iterable<ActivityEndpoint> {
        return listOf(ActivityEndpoint(group.activity))
    }

    override fun getDocumentationElement(group: ActivityEndpointGroup, endpoint: ActivityEndpoint): PsiElement? {
        return endpoint.activity.psiAnchor?.dereference()
    }

    override fun getNavigationElement(group: ActivityEndpointGroup, endpoint: ActivityEndpoint): PsiElement? {
        return endpoint.activity.psiAnchor?.dereference()
    }

    override fun getModificationTracker(project: Project): ModificationTracker = ModificationTracker.NEVER_CHANGED

    override fun getStatus(project: Project): EndpointsProvider.Status = EndpointsProvider.Status.AVAILABLE

    override fun isValidEndpoint(group: ActivityEndpointGroup, endpoint: ActivityEndpoint): Boolean = true

    override fun getEndpointPresentation(group: ActivityEndpointGroup, endpoint: ActivityEndpoint): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String = endpoint.activity.id
            override fun getLocationString(): String? = endpoint.activity.psiAnchor?.dereference()?.containingFile?.name
            override fun getIcon(unused: Boolean): Icon = TemporalIcons.TEMPORAL
        }
    }
}

data class ActivityEndpointGroup(val activity: ActivityModel)
data class ActivityEndpoint(val activity: ActivityModel)
