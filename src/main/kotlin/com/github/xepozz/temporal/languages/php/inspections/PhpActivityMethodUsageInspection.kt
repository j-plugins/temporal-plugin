package com.github.xepozz.temporal.languages.php.inspections

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.languages.php.TemporalClasses
import com.github.xepozz.temporal.languages.php.hasAttribute
import com.github.xepozz.temporal.languages.php.isActivity
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor

class PhpActivityMethodUsageInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpMethodReference(reference: MethodReference) {
                val methods = reference.resolveGlobal(false)
                for (element in methods) {
                    if (element is Method) {
                        if (element.isActivity() && !element.hasAttribute(TemporalClasses.ACTIVITY_METHOD)) {
                            holder.registerProblem(
                                reference.nameNode?.psi ?: reference,
                                TemporalBundle.message("inspection.php.activity.method.usage.attribute.missing.problem.description"),
                                AddActivityMethodAttributeQuickFix(element)
                            )
                        }
                    }
                }
            }
        }
    }
}
