package com.github.xepozz.temporal.languages.php.inspections

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.testing.TemporalPhpTestCase

class PhpActivityMethodInspectionTest : TemporalPhpTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PhpActivityMethodInspection::class.java)
    }

    fun testWarnsOnActivityMethodWithoutAttribute() {
        myFixture.configureByFile("php/inspections/activityMethodMissingAttribute.php")

        myFixture.checkHighlighting(/* checkWarnings = */ true, false, false)
    }

    fun testNoWarningsWhenEveryMethodIsAnnotated() {
        myFixture.configureByFile("php/inspections/activityMethodAllAnnotated.php")

        myFixture.checkHighlighting(true, false, false)
    }

    fun testIgnoresClassesWithoutActivityInterfaceAttribute() {
        myFixture.configureByFile("php/inspections/nonActivityClass.php")

        myFixture.checkHighlighting(true, false, false)
    }

    fun testQuickFixAddsActivityMethodAttribute() {
        myFixture.configureByFile("php/inspections/activityMethodMissingAttributeFix.php")

        val expected = TemporalBundle.message("inspection.php.activity.method.attribute.missing.quick.fix")
        val fix = myFixture.getAllQuickFixes().firstOrNull { it.text == expected }
            ?: throw AssertionError(
                "No quick fix named '$expected'; available: " +
                    myFixture.getAllQuickFixes().joinToString { it.text }
            )
        myFixture.launchAction(fix)

        myFixture.checkResultByFile("php/inspections/activityMethodMissingAttributeFix.after.php")
    }
}
