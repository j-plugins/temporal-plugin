package com.github.xepozz.temporal.languages.php

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpReference
import com.jetbrains.php.lang.psi.elements.PhpYield
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey

class TemporalTypeProvider : PhpTypeProvider4 {
    override fun getKey() = '\uBEB1'

    override fun getType(element: PsiElement) = when {
        DumbService.isDumb(element.project) -> null
        element is MethodReference -> getMethodUsageType(element)
        element is PhpYield -> getYieldType(element)
        else -> null
    }

    private fun getMethodUsageType(element: MethodReference): PhpType? {
        val targetMethods = CachedValuesManager.getCachedValue(element) {
            val methods = element.resolveGlobal(false)
                .mapNotNull { it as? Method }
                .mapNotNull {
                    return@mapNotNull when {
                        it.isActivity() -> it
                        it.isWorkflow() -> it
                        else -> null
                    }
                }

            CachedValueProvider.Result.create(methods, element, *methods.toTypedArray())
        }
        if (targetMethods.isEmpty()) return null

        val newType = PhpType.PhpTypeBuilder()

        targetMethods.map { method ->
            val signed = PhpTypeSignatureKey.getSignature(method)
//            println("signed: $signed")
            val parametrized = PhpType.createParametrized(
                PhpTypeSignatureKey.CLASS.sign(UtilClasses.REACT_PROMISE),
                signed
            )
//            println("parametrized $parametrized ${parametrized.typesWithParametrisedParts} ${parametrized.isComplete}")
            newType.add(parametrized)
        }
        return newType.build()
    }

    private fun getYieldType(element: PhpYield): PhpType? {
        val argument = element.argument as? PhpReference ?: return null

        val newType = PhpType.PhpTypeBuilder()
        val signatureTypes = PhpType().add(argument.signature.substring(2))
            .typesWithParametrisedParts
            .mapNotNull {
                val lBraceIndex = it.indexOf("<")
                val rBraceIndex = it.indexOf(">")

                when {
                    rBraceIndex > lBraceIndex -> it.substring(lBraceIndex + 1, rBraceIndex)
                    else -> null
                }
            }

        signatureTypes.forEach { newType.add(it) }

        return newType.build()
    }

    override fun complete(
        signature: String,
        project: Project,
    ): PhpType? {
        return null
    }

    override fun getBySignature(
        expression: String,
        visited: Set<String>,
        depth: Int,
        project: Project,
    ): Collection<PhpNamedElement>? {
        return null
    }
}