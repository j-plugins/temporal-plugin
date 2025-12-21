package com.github.xepozz.temporal.languages.php.endpoints

import com.github.xepozz.temporal.common.extensionPoints.Activity
import com.github.xepozz.temporal.languages.php.TemporalClasses
import com.github.xepozz.temporal.languages.php.hasAttribute
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.jetbrains.php.PhpIndex
import com.github.xepozz.temporal.common.model.Activity as ActivityModel

class PhpActivity : Activity {
    override fun getActivities(project: Project): List<ActivityModel> {
        val phpIndex = PhpIndex.getInstance(project)
        val smartPointerManager = SmartPointerManager.getInstance(project)

        return phpIndex.getAllClassNames(null).flatMap { name ->
            phpIndex.getClassesByName(name)
                .filter { it.hasAttribute(TemporalClasses.ACTIVITY) }
                .map {
                    ActivityModel(
                        id = it.name,
                        language = "PHP",
                        psiAnchor = smartPointerManager.createSmartPsiElementPointer(it),
                        parameters = emptyList()
                    )
                }
        }
    }
}
