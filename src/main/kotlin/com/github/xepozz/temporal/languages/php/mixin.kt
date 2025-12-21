package com.github.xepozz.temporal.languages.php

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiLanguageInjectionHost
import com.jetbrains.php.lang.psi.elements.PhpAttributesOwner
import com.jetbrains.php.lang.psi.elements.PhpClass

fun PhpClass.hasTrait(fqn: String): Boolean = traits.any { it.fqn == fqn }
fun PhpAttributesOwner.hasAttribute(fqn: String): Boolean = attributes.any { it.fqn == fqn }
fun PhpClass.hasInterface(fqn: String): Boolean = implementedInterfaces.any { it.fqn == fqn }
fun PhpClass.hasSuperClass(fqn: String): Boolean = superClasses.any { it.fqn == fqn }
val PsiLanguageInjectionHost.contentRange: TextRange
    get() = ElementManipulators.getValueTextRange(this).shiftRight(textRange.startOffset)

//fun PhpReference.getSignatures(): Collection<String> = signature.split('|')
//fun PhpReference.hasSignature(signatureToFind: String): Boolean = getSignatures().any { it==signatureToFind }
