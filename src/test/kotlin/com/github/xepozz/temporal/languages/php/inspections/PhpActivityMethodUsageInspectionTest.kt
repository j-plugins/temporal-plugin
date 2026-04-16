package com.github.xepozz.temporal.languages.php.inspections

import com.github.xepozz.temporal.testing.TemporalPhpTestCase

class PhpActivityMethodUsageInspectionTest : TemporalPhpTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PhpActivityMethodUsageInspection::class.java)
    }

    fun testWarnsAtCallSiteWhenTargetMethodLacksActivityMethodAttribute() {
        myFixture.configureByFile("php/inspections/activityMethodUsageMissing.php")

        myFixture.checkHighlighting(/* checkWarnings = */ true, false, false)
    }

    fun testNoWarningsWhenEveryTargetMethodIsAnnotated() {
        myFixture.configureByFile("php/inspections/activityMethodUsageOk.php")

        myFixture.checkHighlighting(true, false, false)
    }

    fun testIgnoresCallsToNonActivityClasses() {
        myFixture.configureByFile("php/inspections/nonActivityUsage.php")

        myFixture.checkHighlighting(true, false, false)
    }
}
