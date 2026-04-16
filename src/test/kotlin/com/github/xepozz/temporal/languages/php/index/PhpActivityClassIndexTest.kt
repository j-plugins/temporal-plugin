package com.github.xepozz.temporal.languages.php.index

import com.github.xepozz.temporal.testing.TemporalPhpTestCase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class PhpActivityClassIndexTest : TemporalPhpTestCase() {

    fun testIndexesOnlyClassesWithActivityInterfaceAttribute() {
        myFixture.copyFileToProject(
            "php/index/OrderActivityInterface.php",
            "src/Activity/OrderActivity.php",
        )
        myFixture.copyFileToProject(
            "php/index/PaymentActivityInterface.php",
            "src/Activity/PaymentActivity.php",
        )
        myFixture.copyFileToProject(
            "php/index/NotAnActivity.php",
            "src/Service/NotAnActivity.php",
        )

        val keys = collectLiveKeys()

        assertContainsElements(
            keys,
            "\\App\\Activity\\OrderActivity",
            "\\App\\Activity\\PaymentActivity",
        )
        assertDoesntContain(keys, "\\App\\Service\\Foo", "\\App\\Service\\BarInterface")
    }

    fun testEmptyProjectHasNoActivityClasses() {
        val keys = collectLiveKeys()

        assertEmpty(keys)
    }

    private fun collectLiveKeys(): List<String> {
        val idx = FileBasedIndex.getInstance()
        val scope = GlobalSearchScope.projectScope(project)
        return idx.getAllKeys(PhpActivityClassIndex.NAME, project)
            .filter { key ->
                idx.getContainingFiles(PhpActivityClassIndex.NAME, key, scope).isNotEmpty()
            }
    }
}
