package com.github.xepozz.temporal.testing

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * Base class for tests that need the Temporal PHP stubs available in the project.
 *
 * The stub file declares `\Temporal\Activity\ActivityInterface`,
 * `\Temporal\Activity\ActivityMethod`, `\Temporal\Workflow\WorkflowInterface`,
 * etc. — so that `#[ActivityInterface]` / `#[WorkflowInterface]` / `#[ActivityMethod]`
 * references in fixture files resolve to the same FQNs that
 * [com.github.xepozz.temporal.languages.php.TemporalClasses] declares.
 */
abstract class TemporalPhpTestCase : BasePlatformTestCase() {

    override fun getTestDataPath(): String = File(TEST_DATA_ROOT).absolutePath

    override fun setUp() {
        super.setUp()
        // VFS restricts file access to a whitelisted set of roots during tests.
        // Our test data lives outside the default allow-list, so we register it
        // for the lifetime of the test. The root is released when
        // `testRootDisposable` fires, keeping tests hermetic.
        VfsRootAccess.allowRootAccess(testRootDisposable, testDataPath)
        myFixture.copyFileToProject(STUBS_RELATIVE_PATH, STUBS_RELATIVE_PATH)
    }

    companion object {
        const val TEST_DATA_ROOT = "src/test/resources"
        const val STUBS_RELATIVE_PATH = "stubs/Temporal.php"
    }
}
