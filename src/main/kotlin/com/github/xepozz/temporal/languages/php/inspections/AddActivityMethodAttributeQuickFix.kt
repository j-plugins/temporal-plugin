package com.github.xepozz.temporal.languages.php.inspections

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.languages.php.TemporalClasses
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.codeStyle.CodeStyleManager
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Method

class AddActivityMethodAttributeQuickFix(method: Method? = null) : LocalQuickFix {
    private val methodPointer: SmartPsiElementPointer<Method>? = method?.let {
        SmartPointerManager.getInstance(it.project).createSmartPsiElementPointer(it)
    }

    override fun getFamilyName(): String {
        return TemporalBundle.message("inspection.php.activity.method.attribute.missing.quick.fix")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        val method = methodPointer?.element
            ?: (element as? Method ?: element.parent) as? Method
            ?: return

        val newAttributesList = PhpPsiElementFactory.createAttributesList(project, TemporalClasses.ACTIVITY_METHOD)
        val anchor = method.firstChild
        method.addBefore(newAttributesList, anchor)

        CodeStyleManager.getInstance(project).reformat(method)
    }
}
