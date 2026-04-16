# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the code in this repository.

## Project Overview

**temporal-plugin** is an IntelliJ Platform plugin that improves the Developer Experience (DX) for [Temporal.io](https://temporal.io/) users across all IDEs based on the IntelliJ Platform (IDEA Ultimate, PhpStorm, etc.).

- **Plugin ID**: `com.github.xepozz.temporal`
- **Vendor**: Dmitrii Derepko (@xepozz)
- **Platform**: IntelliJ Platform 2025.1.1+ (since build 251)
- **Currently supported languages**: PHP (extensible to Go, Java, TypeScript, etc.)

Temporal is an open-source durable execution solution for building scalable, distributed systems.

## Build & Development

This project uses **Gradle (Kotlin DSL)** with the IntelliJ Platform Gradle Plugin.

- **JVM**: Java 21
- **Kotlin**: 2.3.0
- **Gradle**: 9.2.1

Common commands:

```bash
./gradlew build                 # Build the plugin
./gradlew runIde                # Run IDE with the plugin loaded (sandbox)
./gradlew test                  # Run tests
./gradlew verifyPlugin          # Verify against supported IDE versions
./gradlew buildPlugin           # Produce a distributable .zip
./gradlew runIdeForUiTests      # Run IDE for UI/robot tests
./gradlew koverXmlReport        # Generate code coverage report
```

Dependencies are declared through the Gradle Version Catalog (`gradle/libs.versions.toml`) and via Gradle properties in `gradle.properties` (`platformPlugins`, `platformBundledPlugins`, `platformBundledModules`).

Key platform plugin dependencies (from `gradle.properties`):
- `com.jetbrains.php` (optional, config file `language-php.xml`)
- `com.github.xepozz.metastorm` (required)
- `com.jetbrains.hackathon.indices.viewer`

## Architecture

The base package is `com.github.xepozz.temporal`. The project is split into two top-level packages:

### `common/` — language-agnostic
- `extensionPoints/` — EP interfaces (`Workflow`, `Activity`)
- `model/` — common data classes (`Workflow`, `Activity`)
- `index/` — base indexing classes
- `endpoints/` — `microservices.endpointsProvider` implementations
- `run/` — Run configuration type for the Temporal dev server
- `uiViewer/` — Tool window (Web UI tab + Terminal tab), `StarterServerService`, actions
- `configuration/` — Settings (`TemporalExecutable`, settings UI, version checker)

### `languages/<lang>/` — language adapters
- `languages/php/` — the only adapter today
  - `TemporalClasses.kt` — constants for Temporal PHP FQCNs/attributes
  - `mixin.kt` — extension fns (`isActivity`, `isWorkflow`, `hasAttribute`, ...)
  - `endpoints/PhpActivity.kt`, `endpoints/PhpWorkflow.kt` — EP impls
  - `index/` — `PhpWorkflowClassIndex`, `PhpWorkflowMethodIndex`, `PhpActivityClassIndex`, `PhpActivityMethodIndex`
  - `inspections/` — `PhpActivityMethodInspection`, `PhpActivityMethodUsageInspection`, quick fixes
  - `TemporalTypeProvider.kt` — PHP type provider (resolves workflow/activity return types, React Promise, generator `yield`)

### Plugin descriptors
- `src/main/resources/META-INF/plugin.xml` — core descriptor (EP definitions, common extensions, tool window, run config, endpoints, settings)
- `src/main/resources/META-INF/language-php.xml` — loaded only when `com.jetbrains.php` is available; wires PHP adapters, indexes, inspections, type provider

## Extension Point Pattern

When adding a feature that should work across languages, follow these five steps (see `CONTRIBUTING.md` for more detail):

1. **Define the EP interface** in `common/extensionPoints/` (e.g., `Workflow`).
2. **Register the EP** in `plugin.xml` under `<extensionPoints>` with `dynamic="true"` and a name starting with `com.github.xepozz.temporal`.
3. **Implement a shared provider** (e.g., `CompletionProvider`, inspection, etc.) in `common/`, iterating over the EP via `ACTIVITY_EP.lazyDumbAwareExtensions(project)`.
4. **Implement the language adapter** in `languages/<lang>/` (prefix the class with the language name, e.g., `PhpActivity`).
5. **Register the adapter** in the language-specific XML (e.g., `language-php.xml`) under `defaultExtensionNs="com.github.xepozz.temporal"`.

## Temporal PHP SDK References

The plugin recognizes these PHP FQCNs/attributes (see `languages/php/TemporalClasses.kt`):

| Constant | FQCN |
|---|---|
| `ACTIVITY` | `\Temporal\Activity\ActivityInterface` |
| `ACTIVITY_METHOD` | `\Temporal\Activity\ActivityMethod` |
| `WORKFLOW` | `\Temporal\Workflow\WorkflowInterface` |
| `WORKFLOW_METHOD` | `\Temporal\Workflow\WorkflowMethod` |
| `WORKFLOW_INIT` | `\Temporal\Workflow\WorkflowInit` |
| `SIGNAL_METHOD` | `\Temporal\Workflow\SignalMethod` |
| `QUERY_METHOD` | `\Temporal\Workflow\QueryMethod` |
| `UPDATE_METHOD` | `\Temporal\Workflow\UpdateMethod` |
| `UPDATE_VALIDATOR_METHOD` | `\Temporal\Workflow\UpdateValidatorMethod` |
| `CHILD_WORKFLOW_STUB` | `\Temporal\Workflow\ChildWorkflowStubInterface` |

## Coding Standards

- **Kotlin first** — all new code in Kotlin; prefer `data class`/idiomatic Kotlin.
- **Performance** — use `CachedValue`, `DumbService.isDumb()`, and `lazyDumbAwareExtensions(project)` (not `extensionList`) when iterating EPs.
- **PHP PSI** — prefer the helpers in `languages/php/mixin.kt` (`PhpClass.isActivity()`, `Method.isWorkflow()`, `hasAttribute(fqn)`, ...) over manual PSI traversal. Note that `Method.isActivity/isWorkflow` are tolerant: they return `true` for a public, non-static, non-abstract method inside an Activity/Workflow class even without the explicit `#[ActivityMethod]`/`#[WorkflowMethod]` attribute.
- **Consistency** — mirror the PHP sub-package layout when adding new languages (e.g., `languages/go/navigation` mirrors `languages/php/navigation`).
- **Naming** — EP interfaces are named after the entity (`Workflow`, `Activity`), not suffixed with `EP`. Adapter classes are prefixed with the language (`Php...`).
- **Namespaces** — all EP names and IDs start with `com.github.xepozz.temporal`.
- **Minimal changes** — implement the smallest change that satisfies the requirement without breaking the architecture.

## i18n

All user-visible strings live in `src/main/resources/messages/TemporalBundle.properties` and are accessed via `TemporalBundle`. Inspection descriptions live in `src/main/resources/inspectionDescriptions/*.html`.

## Skills

Project-specific Claude Code skills are available under `.claude/skills/`. They scaffold official Temporal PHP SDK patterns (workflow, activity, signal, query, update, worker, saga, child workflow). Invoke via `/<skill-name>`.

## Useful Paths

| Topic | Path |
|---|---|
| Plugin descriptor | `src/main/resources/META-INF/plugin.xml` |
| PHP descriptor | `src/main/resources/META-INF/language-php.xml` |
| PHP FQCN constants | `src/main/kotlin/com/github/xepozz/temporal/languages/php/TemporalClasses.kt` |
| PHP PSI helpers | `src/main/kotlin/com/github/xepozz/temporal/languages/php/mixin.kt` |
| Extension points | `src/main/kotlin/com/github/xepozz/temporal/common/extensionPoints/` |
| Run configuration | `src/main/kotlin/com/github/xepozz/temporal/common/run/` |
| Tool window / Web UI | `src/main/kotlin/com/github/xepozz/temporal/common/uiViewer/` |
| Architecture guide | `CONTRIBUTING.md` |
