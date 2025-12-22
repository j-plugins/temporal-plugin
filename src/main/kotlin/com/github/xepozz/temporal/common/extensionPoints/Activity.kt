package com.github.xepozz.temporal.common.extensionPoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.lazyDumbAwareExtensions
import com.github.xepozz.temporal.common.model.Activity as ActivityModel

interface Activity {
    fun getActivities(project: Project, module: Module? = null): List<ActivityModel>

    companion object {
        val ACTIVITY_EP = ExtensionPointName.create<Activity>("com.github.xepozz.temporal.activity")

        fun getActivities(project: Project, module: Module? = null): List<ActivityModel> {
            return ACTIVITY_EP.lazyDumbAwareExtensions(project).flatMap { it.getActivities(project, module) }.toList()
        }
    }
}
