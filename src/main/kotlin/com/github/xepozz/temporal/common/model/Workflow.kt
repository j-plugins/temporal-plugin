package com.github.xepozz.temporal.common.model

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

data class Workflow(
    val id: String,
    val language: String,
    val psiAnchor: SmartPsiElementPointer<out PsiElement>,
    val parameters: Collection<String>
)
