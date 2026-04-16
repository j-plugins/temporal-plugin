# IntelliJ Platform Gradle Plugin (v2)

Official docs: <https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html>

## Minimum setup

```kotlin
plugins {
    java
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.changelog") version "2.5.0"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}
```

## Dependencies

```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        testFramework(TestFrameworkType.Platform)
    }
}
```

Backing `gradle.properties`:

```properties
platformVersion = 2025.1.1
platformBundledPlugins =
platformPlugins = com.jetbrains.php:251.23774.16
pluginSinceBuild = 251
kotlin.stdlib.default.dependency = false
```

## `intellijPlatform { ... }` block

```kotlin
intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey       = providers.environmentVariable("PRIVATE_KEY")
        password         = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token    = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion").map { listOf(/* stable / eap / ... */) }
    }

    pluginVerification {
        ides { recommended() }   // verify against the IDE versions JetBrains recommends
    }
}
```

## Common tasks

| Task | Purpose |
|---|---|
| `build` | Compile + package |
| `runIde` | Launch a sandbox IDE with the plugin loaded |
| `runIdeForUiTests` | Launch with `robot-server` for UI tests |
| `test` | Run JUnit tests (uses the Platform test framework) |
| `verifyPlugin` | Run IntelliJ Plugin Verifier against `recommended()` IDEs |
| `buildPlugin` | Produce `build/distributions/<name>-<version>.zip` |
| `publishPlugin` | Upload to JetBrains Marketplace |
| `patchChangelog` | Finalize the unreleased section in `CHANGELOG.md` |
| `koverXmlReport` | Generate coverage XML at `build/reports/kover/report.xml` |

Run the verifier before every release:

```bash
./gradlew verifyPlugin
```

It catches `NoSuchMethodError`-style API breaks across supported IDE versions.

## Rules

- Pin `platformVersion` and `pluginSinceBuild` — no wildcards.
- Keep `kotlin.stdlib.default.dependency = false` (the IDE bundles the stdlib;
  shipping another copy breaks classloading).
- Use `org.jetbrains.intellij.platform.module` instead of the main plugin in
  Gradle *submodules*, otherwise signing/publishing tasks pollute every
  module.
- Enable `org.gradle.configuration-cache = true` + `org.gradle.caching = true`
  for fast incremental builds (this repo already does).
