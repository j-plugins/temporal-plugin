package com.github.xepozz.temporal.languages.php.endpoints

import com.github.xepozz.temporal.common.extensionPoints.Workflow
import com.github.xepozz.temporal.languages.php.index.PhpWorkflowClassIndex
import com.github.xepozz.temporal.languages.php.index.PhpWorkflowMethodIndex
import com.github.xepozz.temporal.languages.php.isWorkflow
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndex
import com.github.xepozz.temporal.common.model.Workflow as WorkflowModel

class PhpWorkflow : Workflow {
    override fun getWorkflows(project: Project, module: Module?): List<WorkflowModel> {
        val phpIndex = PhpIndex.getInstance(project)
        val smartPointerManager = SmartPointerManager.getInstance(project)
        val results = mutableListOf<WorkflowModel>()
        val scope = module?.moduleContentScope ?: GlobalSearchScope.allScope(project)

        val classFqns = mutableSetOf<String>()
        FileBasedIndex.getInstance().processAllKeys(PhpWorkflowClassIndex.NAME, { key ->
            classFqns.add(key)
            true
        }, project)

        classFqns.forEach { fqn ->
            phpIndex.getClassesByFQN(fqn).filter { it.isWorkflow() && scope.contains(it.containingFile.virtualFile) }.forEach { phpClass ->
                results.add(
                    WorkflowModel(
                        id = phpClass.name,
                        language = "PHP",
                        psiAnchor = smartPointerManager.createSmartPsiElementPointer(phpClass),
                        parameters = emptyList()
                    )
                )
            }
        }

        val methodKeys = mutableSetOf<String>()
        FileBasedIndex.getInstance().processAllKeys(PhpWorkflowMethodIndex.NAME, { key ->
            methodKeys.add(key)
            true
        }, project)

        methodKeys.forEach { key ->
            val parts = key.split("::")
            if (parts.size == 2) {
                val classFqn = parts[0]
                val methodName = parts[1]
                phpIndex.getClassesByFQN(classFqn).forEach { phpClass ->
                    phpClass.ownMethods.find { it.name == methodName && it.isWorkflow() && scope.contains(it.containingFile.virtualFile) }?.let { method ->
                        results.add(
                            WorkflowModel(
                                id = "${phpClass.name}::$methodName",
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
