package com.github.xepozz.temporal.languages.php.index

import com.github.xepozz.temporal.testing.TemporalPhpTestCase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

class PhpActivityMethodIndexTest : TemporalPhpTestCase() {

    fun testIndexesEveryPublicConcreteMethodInsideAnActivityClass() {
        myFixture.copyFileToProject(
            "php/index/OrderActivityInterface.php",
            "src/Activity/OrderActivity.php",
        )
        myFixture.copyFileToProject(
            "php/index/PaymentActivityInterface.php",
            "src/Activity/PaymentActivity.php",
        )

        val keys = collectLiveKeys()

        // Method.isActivity() is tolerant — public non-static non-abstract methods
        // in a class carrying #[ActivityInterface] are indexed whether or not
        // they declare #[ActivityMethod] themselves.
        assertContainsElements(
            keys,
            "\\App\\Activity\\OrderActivity::reserve",
            "\\App\\Activity\\OrderActivity::cancel",
            "\\App\\Activity\\OrderActivity::track",
            "\\App\\Activity\\PaymentActivity::charge",
            "\\App\\Activity\\PaymentActivity::refund",
        )
    }

    fun testDoesNotIndexMethodsOfNonActivityClasses() {
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
        return idx.getAllKeys(PhpActivityMethodIndex.NAME, project)
            .filter { key ->
                idx.getContainingFiles(PhpActivityMethodIndex.NAME, key, scope).isNotEmpty()
            }
    }
}
