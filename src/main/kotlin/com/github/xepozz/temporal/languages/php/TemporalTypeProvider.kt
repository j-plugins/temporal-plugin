package com.github.xepozz.temporal.languages.php

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4

class TemporalTypeProvider : PhpTypeProvider4 {
    override fun getKey() = '\uBEB1'
    override fun getType(p0: PsiElement?): PhpType? {
        TODO("Not yet implemented")
    }

    override fun complete(
        p0: String?,
        p1: Project?
    ): PhpType? {
        TODO("Not yet implemented")
    }

    override fun getBySignature(
        p0: String?,
        p1: Set<String?>?,
        p2: Int,
        p3: Project?
    ): Collection<PhpNamedElement?>? {
        TODO("Not yet implemented")
    }

    override fun interceptsNativeSignature() = true


}