---
name: kotlin-test
description: Write JUnit 4 + IntelliJ Platform Test Framework tests in Kotlin for this plugin (inspections, indexes, endpoint providers, EP implementations, PSI helpers, run configs). Use when the user asks to "add a test", "write a test", "cover X with tests", or "bootstrap the test suite".
---

# Kotlin Test Writer — temporal-plugin stack

Tests are **JUnit 4** (the project's declared test framework) run through the
**IntelliJ Platform Test Framework** (`testFramework(TestFrameworkType.Platform)`
in `build.gradle.kts`). Coverage is tracked by **Kover** (XML reported on
`check`). The project currently has no `src/test/` directory — if you're the
first person adding a test, lay the conventions below down deliberately.

## Layout (bootstrap on first test)

```
src/test/
├── kotlin/com/github/xepozz/temporal/...      # mirror of src/main/kotlin
└── resources/
    ├── php/fixtures/...                        # .php fixture files
    └── projectFixtures/...                     # multi-file project fixtures
```

- Mirror the production package exactly so a test sits next to the class it
  covers (e.g. `languages/php/inspections/PhpActivityMethodInspectionTest.kt`).
- Test data lives under `src/test/resources/` addressed from tests via
  `getTestDataPath()`.

## Base classes — pick the right one

| Scenario | Base class |
|---|---|
| Pure logic, no PSI / no project | `junit.framework.TestCase` (or plain JUnit 4 `class X { @Test fun ... }`) |
| Needs a light in-memory project + PSI | `com.intellij.testFramework.fixtures.BasePlatformTestCase` |
| Needs Java PSI too | `LightJavaCodeInsightFixtureTestCase` |
| PHP PSI / inspections / completion | `com.jetbrains.php.testFramework.PhpCodeInsightFixtureTestCase` |
| Multi-module / real filesystem project | `HeavyPlatformTestCase` (avoid unless you must) |

`BasePlatformTestCase` and the PHP variant expose `myFixture`
(`CodeInsightTestFixture`) — the main driver.

## Test data path convention

```kotlin
override fun getTestDataPath(): String =
    com.intellij.openapi.application.PathManager.getCommunityHomePath() // ⚠ don't use
```

Use the project-root-relative path instead:

```kotlin
override fun getTestDataPath(): String =
    java.io.File("src/test/resources").absolutePath
```

Fixture files live under that root, e.g.
`src/test/resources/php/inspections/activityMethodMissingAttribute.php`.

## Style rules

- **JUnit 4** — test methods named `testCamelCase`, no `fun should ...`
  backtick names (the platform framework expects `test...` prefixes via
  `UsefulTestCase`). Annotate with `@org.junit.Test` only when extending
  a plain JUnit class; `BasePlatformTestCase` discovers `test...` methods
  automatically, no annotation needed.
- **Arrange / Act / Assert** with blank lines between phases.
- **One behaviour per test.** Split data-driven cases into distinct `test...`
  methods (or drive them with a helper method, not a loop inside a single test).
- **No Mockito / MockK** unless unavoidable — prefer real platform fixtures.
  The IDE test framework gives you a real `Project`, `PsiManager`, indexes etc.
- **No `runBlocking`** in test bodies — use the fixture's synchronous API. If
  async is truly required, use `PlatformTestUtil.dispatchAllEventsInIdeEventQueue()`.
- **Dispose explicitly** of `Disposable` resources you create via
  `Disposer.register(testRootDisposable, ...)`.
- Use **`TemporalBundle.message(...)`** as the source of truth for expected
  strings, never hard-code translated text in assertions.

## Template — inspection test (with quick-fix)

File: `src/test/kotlin/com/github/xepozz/temporal/languages/php/inspections/PhpActivityMethodInspectionTest.kt`

```kotlin
package com.github.xepozz.temporal.languages.php.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class PhpActivityMethodInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String =
        File("src/test/resources/php/inspections").absolutePath

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PhpActivityMethodInspection::class.java)
    }

    fun testWarnsWhenActivityMethodMissesAttribute() {
        myFixture.configureByFile("activityMethodMissingAttribute.php")

        myFixture.checkHighlighting(/*checkWarnings = */ true, false, false)
    }

    fun testQuickFixAddsActivityMethodAttribute() {
        myFixture.configureByFile("activityMethodMissingAttribute.php")
        val fix = myFixture.findSingleIntention(
            com.github.xepozz.temporal.TemporalBundle.message(
                "inspection.php.activity.method.attribute.missing.quickfix.name"
            )
        )

        myFixture.launchAction(fix)

        myFixture.checkResultByFile("activityMethodMissingAttribute.after.php")
    }
}
```

Fixture `activityMethodMissingAttribute.php` uses `<warning descr="...">` markers:

```php
<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;

#[ActivityInterface]
interface OrderActivityInterface
{
    public function <warning descr="Missing #[ActivityMethod] attribute">reserve</warning>(string $orderId): void;
}
```

## Template — file-based index test

```kotlin
package com.github.xepozz.temporal.languages.php.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import java.io.File

class PhpActivityClassIndexTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String =
        File("src/test/resources/php/index").absolutePath

    fun testIndexesClassesMarkedWithActivityInterface() {
        myFixture.copyDirectoryToProject("activityFixture", "")

        val keys = FileBasedIndex.getInstance()
            .getAllKeys(PhpActivityClassIndex.KEY, project)
            .filter { fqn ->
                FileBasedIndex.getInstance()
                    .getContainingFiles(PhpActivityClassIndex.KEY, fqn, GlobalSearchScope.allScope(project))
                    .isNotEmpty()
            }

        assertContainsElements(keys, "\\App\\Activity\\OrderActivity")
    }
}
```

## Template — pure logic (no platform)

For small, PSI-free utilities in `common/` (e.g. option normalization, URL
builders), stay lightweight — no `BasePlatformTestCase`:

```kotlin
package com.github.xepozz.temporal.common.run

import org.junit.Assert.assertEquals
import org.junit.Test

class TemporalRunConfigurationOptionsTest {

    @Test
    fun `defaults produce the expected CLI args`() {
        val opts = TemporalRunConfigurationOptions().apply {
            port = 7233
            uiPort = 8233
        }

        assertEquals(listOf("--port", "7233", "--ui-port", "8233"), opts.toCliArgs())
    }
}
```

(Plain JUnit 4 classes *may* use backtick-quoted test names — the platform
discovery rule only applies when extending `UsefulTestCase`.)

## Running tests

```bash
./gradlew test                  # run the whole suite
./gradlew test --tests '*PhpActivityMethodInspectionTest*'
./gradlew koverXmlReport        # coverage report (build/reports/kover/report.xml)
```

The `.run/Run Tests.run.xml` configuration is pre-wired if you prefer the
IDE runner.

## What not to test

- Don't test IntelliJ Platform APIs — trust `FileBasedIndex`, `CachedValuesManager`,
  `PhpInspection`, etc. Test **your** glue code only.
- Don't assert against rendered HTML of inspection descriptions — check the
  inspection's `problem description` message key instead.
- Don't start a real Temporal server from tests; `StarterServerService` should
  be tested with a stub `ProcessHandler` or by extracting the pure-logic parts.

## Checklist before you commit a test

- [ ] Test file mirrors the path of the class under test.
- [ ] Fixture files live under `src/test/resources/`, not inlined as strings.
- [ ] Expected messages come from `TemporalBundle.message(...)`.
- [ ] Inspection tests call `enableInspections(...)` in `setUp()`.
- [ ] No hard-coded OS paths, no sleeps, no network.
- [ ] `./gradlew test` passes locally.
