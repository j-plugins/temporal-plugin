package com.github.xepozz.temporal.languages.php.endpoints

import com.github.xepozz.temporal.common.extensionPoints.Activity
import com.github.xepozz.temporal.languages.php.index.PhpActivityClassIndex
import com.github.xepozz.temporal.languages.php.index.PhpActivityMethodIndex
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.github.xepozz.temporal.common.model.Activity as ActivityModel

class PhpActivity : Activity {
    override fun getActivities(project: Project): List<ActivityModel> {
        val phpIndex = PhpIndex.getInstance(project)
        val smartPointerManager = SmartPointerManager.getInstance(project)
        val results = mutableListOf<ActivityModel>()

        val classFqns = mutableSetOf<String>()
        FileBasedIndex.getInstance().processAllKeys(PhpActivityClassIndex.NAME, { key ->
            classFqns.add(key)
            true
        }, project)

        classFqns.forEach { fqn ->
            phpIndex.getClassesByFQN(fqn).forEach { phpClass ->
                results.add(
                    ActivityModel(
                        id = phpClass.name,
                        language = "PHP",
                        psiAnchor = smartPointerManager.createSmartPsiElementPointer(phpClass),
                        parameters = emptyList()
                    )
                )
            }
        }

        val methodKeys = mutableSetOf<String>()
        FileBasedIndex.getInstance().processAllKeys(PhpActivityMethodIndex.NAME, { key ->
            methodKeys.add(key)
            true
        }, project)

        methodKeys.forEach { key ->
            val parts = key.split("::")
            if (parts.size == 2) {
                val classFqn = parts[0]
                val methodName = parts[1]
                phpIndex.getClassesByFQN(classFqn).forEach { phpClass ->
                    phpClass.methods.find { it.name == methodName }?.let { method ->
                        results.add(
                            ActivityModel(
                                id = "$methodName (${phpClass.name})",
                                language = "PHP",
                                psiAnchor = smartPointerManager.createSmartPsiElementPointer(method),
                                parameters = emptyList()
                            )
                        )
                    }
                }
            }
        }

        return results
    }
}
