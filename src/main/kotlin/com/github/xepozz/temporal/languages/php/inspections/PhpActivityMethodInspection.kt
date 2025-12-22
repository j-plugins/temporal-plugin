package com.github.xepozz.temporal.languages.php.inspections

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.languages.php.TemporalClasses
import com.github.xepozz.temporal.languages.php.hasAttribute
import com.github.xepozz.temporal.languages.php.isActivity
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor

class PhpActivityMethodInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpMethod(method: Method) {
                if (!method.isActivity()) return
                if (method.hasAttribute(TemporalClasses.ACTIVITY_METHOD)) return

                holder.registerProblem(
                    method.nameIdentifier ?: method,
                    TemporalBundle.message("inspection.php.activity.method.attribute.missing.problem.description"),
                    AddActivityMethodAttributeQuickFix()
                )
            }
        }
    }
}
