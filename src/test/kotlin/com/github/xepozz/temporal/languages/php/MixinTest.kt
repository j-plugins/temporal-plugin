package com.github.xepozz.temporal.languages.php

import com.github.xepozz.temporal.testing.TemporalPhpTestCase
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

class MixinTest : TemporalPhpTestCase() {

    private lateinit var classes: Map<String, PhpClass>

    override fun setUp() {
        super.setUp()
        val file = myFixture.configureByFile("php/mixin/MixinFixtures.php") as PhpFile
        classes = PsiTreeUtil
            .findChildrenOfType(file, PhpClass::class.java)
            .associateBy { it.name }
    }

    fun testPhpClassIsActivityReflectsAttributePresence() {
        assertTrue(classes["MyActivityInterface"]!!.isActivity())
        assertTrue(classes["ConcreteActivityClass"]!!.isActivity())
        assertFalse(classes["MyWorkflowInterface"]!!.isActivity())
        assertFalse(classes["ConcreteWorkflowClass"]!!.isActivity())
        assertFalse(classes["PlainClass"]!!.isActivity())
    }

    fun testPhpClassIsWorkflowReflectsAttributePresence() {
        assertTrue(classes["MyWorkflowInterface"]!!.isWorkflow())
        assertTrue(classes["ConcreteWorkflowClass"]!!.isWorkflow())
        assertFalse(classes["MyActivityInterface"]!!.isWorkflow())
        assertFalse(classes["PlainClass"]!!.isWorkflow())
    }

    fun testHasAttributeMatchesTemporalClassFqns() {
        val activity = classes["ConcreteActivityClass"]!!
        assertTrue(activity.hasAttribute(TemporalClasses.ACTIVITY))
        assertFalse(activity.hasAttribute(TemporalClasses.WORKFLOW))

        val workflow = classes["ConcreteWorkflowClass"]!!
        assertTrue(workflow.hasAttribute(TemporalClasses.WORKFLOW))
        assertFalse(workflow.hasAttribute(TemporalClasses.ACTIVITY))

        val plain = classes["PlainClass"]!!
        assertFalse(plain.hasAttribute(TemporalClasses.ACTIVITY))
        assertFalse(plain.hasAttribute(TemporalClasses.WORKFLOW))
    }

    fun testMethodIsActivityIsTolerantForPublicInstanceMethodsInActivityClass() {
        val klass = classes["ConcreteActivityClass"]!!

        assertTrue("explicit #[ActivityMethod]", klass.methodByName("withAttr").isActivity())
        assertTrue("public non-static method tolerated", klass.methodByName("withoutAttr").isActivity())
    }

    fun testMethodIsActivityExcludesStaticMagicAndNonPublicMethods() {
        val klass = classes["ConcreteActivityClass"]!!

        assertFalse("static", klass.methodByName("staticMethod").isActivity())
        assertFalse("magic __construct", klass.methodByName("__construct").isActivity())
        assertFalse("non-public", klass.methodByName("protectedMethod").isActivity())
    }

    fun testMethodIsWorkflowIsTolerantForPublicInstanceMethodsInWorkflowClass() {
        val klass = classes["ConcreteWorkflowClass"]!!

        assertTrue("explicit #[WorkflowMethod]", klass.methodByName("run").isWorkflow())
        assertTrue("tolerant", klass.methodByName("helperWithoutAttribute").isWorkflow())
    }

    fun testMethodIsActivityReturnsFalseOutsideOfActivityClass() {
        val plainMethod = classes["PlainClass"]!!.methodByName("method")
        assertFalse(plainMethod.isActivity())
        assertFalse(plainMethod.isWorkflow())
    }

    private fun PhpClass.methodByName(name: String): Method =
        ownMethods.firstOrNull { it.name == name }
            ?: throw AssertionError("Method '$name' not found in ${this.name}; available: ${ownMethods.joinToString { it.name }}")
}
