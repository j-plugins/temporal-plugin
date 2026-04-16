package com.github.xepozz.temporal.languages.php.index

import com.github.xepozz.temporal.testing.TemporalPhpTestCase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class PhpWorkflowMethodIndexTest : TemporalPhpTestCase() {

    fun testIndexesMethodsOfWorkflowClasses() {
        myFixture.copyFileToProject(
            "php/index/OrderWorkflowInterface.php",
            "src/Workflow/OrderWorkflow.php",
        )
        myFixture.copyFileToProject(
            "php/index/ReportWorkflowInterface.php",
            "src/Workflow/ReportWorkflow.php",
        )

        val keys = collectLiveKeys()

        assertContainsElements(
            keys,
            "\\App\\Workflow\\OrderWorkflow::run",
            "\\App\\Workflow\\OrderWorkflow::cancel",
            "\\App\\Workflow\\ReportWorkflow::generate",
        )
    }

    fun testDoesNotIndexMethodsOfNonWorkflowClasses() {
        myFixture.copyFileToProject(
            "php/index/NotAnActivity.php",
            "src/Service/NotAnActivity.php",
        )

        val keys = collectLiveKeys()

        assertDoesntContain(
            keys,
            "\\App\\Service\\Foo::bar",
            "\\App\\Service\\BarInterface::baz",
        )
    }

    private fun collectLiveKeys(): List<String> {
        val idx = FileBasedIndex.getInstance()
        val scope = GlobalSearchScope.projectScope(project)
        return idx.getAllKeys(PhpWorkflowMethodIndex.NAME, project)
            .filter { key ->
                idx.getContainingFiles(PhpWorkflowMethodIndex.NAME, key, scope).isNotEmpty()
            }
    }
}
