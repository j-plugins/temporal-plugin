package com.github.xepozz.temporal.languages.php.index

import com.github.xepozz.temporal.testing.TemporalPhpTestCase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class PhpWorkflowClassIndexTest : TemporalPhpTestCase() {

    fun testIndexesOnlyClassesWithWorkflowInterfaceAttribute() {
        myFixture.copyFileToProject(
            "php/index/OrderWorkflowInterface.php",
            "src/Workflow/OrderWorkflow.php",
        )
        myFixture.copyFileToProject(
            "php/index/ReportWorkflowInterface.php",
            "src/Workflow/ReportWorkflow.php",
        )
        myFixture.copyFileToProject(
            "php/index/NotAnActivity.php",
            "src/Service/NotAnActivity.php",
        )

        val keys = collectLiveKeys()

        assertContainsElements(
            keys,
            "\\App\\Workflow\\OrderWorkflow",
            "\\App\\Workflow\\ReportWorkflow",
        )
        assertDoesntContain(keys, "\\App\\Service\\Foo", "\\App\\Service\\BarInterface")
    }

    private fun collectLiveKeys(): List<String> {
        val idx = FileBasedIndex.getInstance()
        val scope = GlobalSearchScope.projectScope(project)
        return idx.getAllKeys(PhpWorkflowClassIndex.NAME, project)
            .filter { key ->
                idx.getContainingFiles(PhpWorkflowClassIndex.NAME, key, scope).isNotEmpty()
            }
    }
}
