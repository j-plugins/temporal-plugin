---
name: kotlin-dev
description: Write new Kotlin code for this IntelliJ Platform plugin following the project's common/languages architecture, Extension Point pattern, and performance/naming rules. Use whenever the user asks to "add a Kotlin class", "implement an inspection", "add an action", "create a service", "write a completion provider", or any new plugin-side feature.
---

# Kotlin Developer — temporal-plugin stack

This is an IntelliJ Platform plugin (`com.github.xepozz.temporal`, since-build
`251`, platform `2025.1.1`). All production code is Kotlin 2.3, JVM toolchain
21. Follow the architecture and conventions below *exactly* — the project is
small and consistent, and divergence is noise.

## Before you write code

1. **Locate the right package** (`common/` vs `languages/<lang>/`):
   - Language-agnostic logic → `common/...`
   - Logic that touches PSI of a specific language → `languages/<lang>/...`
   - Feature shared across languages → define an EP in `common/extensionPoints/`
     and add language adapters under `languages/<lang>/...`.
2. **Check for an existing Extension Point** in `common/extensionPoints/` first
   (`Workflow`, `Activity`). Reuse before creating.
3. **Check `languages/php/mixin.kt`** — helpers like `PhpClass.isActivity()`,
   `Method.isWorkflow()`, `PhpAttributesOwner.hasAttribute(fqn)` already exist.
4. **Check `languages/php/TemporalClasses.kt`** for the Temporal FQCN you need —
   never hard-code `"\\Temporal\\..."` strings in new code.

## Extension Point recipe

When a feature must work across languages, add five pieces in this order:

1. **EP interface** in `common/extensionPoints/<Entity>.kt`:

   ```kotlin
   interface Workflow {
       fun getWorkflows(project: Project, module: Module? = null): List<WorkflowModel>

       companion object {
           val EP = ExtensionPointName.create<Workflow>("com.github.xepozz.temporal.workflow")

           fun getWorkflows(project: Project, module: Module? = null): List<WorkflowModel> =
               EP.lazyDumbAwareExtensions(project).flatMap { it.getWorkflows(project, module) }.toList()
       }
   }
   ```

2. **Register the EP** in `src/main/resources/META-INF/plugin.xml`:

   ```xml
   <extensionPoints>
       <extensionPoint name="workflow" dynamic="true"
                       interface="com.github.xepozz.temporal.common.extensionPoints.Workflow"/>
   </extensionPoints>
   ```

3. **Shared consumer** in `common/<feature>/<Something>Provider.kt` — iterate
   via `Workflow.getWorkflows(project)`, never `EP.extensionList`.
4. **Language adapter** in `languages/php/<feature>/Php<Something>.kt` —
   prefixed with the language name, implements the interface.
5. **Register the adapter** in the language-specific XML
   (`src/main/resources/META-INF/language-php.xml`) under
   `defaultExtensionNs="com.github.xepozz.temporal"`.

## Hard rules

- **Kotlin-first**: never add new Java files. All new code is Kotlin.
- **Namespaces**: every EP name/ID and every plugin extension ID starts with
  `com.github.xepozz.temporal`.
- **Naming**:
  - EP interfaces are named after the entity — `Workflow`, `Activity`, never
    `WorkflowEP` / `ActivityEP`.
  - Language adapters are prefixed: `PhpWorkflow`, `PhpActivity`. Mirror the
    sub-package path (`languages/php/navigation` ↔ future `languages/go/navigation`).
- **Mirror package layout** when adding a new language adapter — copy PHP's
  sub-packages (`endpoints/`, `index/`, `inspections/`).
- **i18n**: every user-facing string goes through `TemporalBundle.message("...")`,
  with the key defined in
  `src/main/resources/messages/TemporalBundle.properties`. No inline English.
- **Inspection descriptions**: when adding a `<localInspection>`, also add
  `src/main/resources/inspectionDescriptions/<shortName>.html`.
- **Minimal change**: implement the smallest diff that satisfies the goal.
  No speculative abstractions, no unused parameters, no "just in case" try/catch.

## Performance rules (these matter inside the IDE)

- Iterate EPs with `EP.lazyDumbAwareExtensions(project)` — **never**
  `extensionList`. The former is dumb-mode aware and lazily instantiated.
- Guard PSI-heavy computations behind `DumbService.isDumb(project)` checks, or
  implement `DumbAware` on the extension.
- Cache expensive derivations with `CachedValuesManager.getCachedValue(element)`
  using `PsiModificationTracker.MODIFICATION_COUNT` (or a more specific
  tracker). Don't roll your own memoization.
- File-based indexes go through `com.github.xepozz.temporal.common.index.AbstractIndex`
  — subclass it, don't reimplement `FileBasedIndexExtension` from scratch.
- Long-running work (HTTP, process launches) must not block the EDT. Use
  `BackgroundTaskQueue`, `ProgressManager.run(Task.Backgroundable)`, or
  coroutines with `AppExecutorUtil`.

## PHP-specific guidance

- Prefer the mixin helpers over manual PSI walks:

  ```kotlin
  // Good
  if (phpClass.isWorkflow()) { ... }
  if (method.isActivity()) { ... }
  if (phpClass.hasInterface(TemporalClasses.WORKFLOW)) { ... }

  // Bad — don't reinvent these
  phpClass.attributes.any { it.fqn == "\\Temporal\\Workflow\\WorkflowInterface" }
  ```

- `Method.isActivity()` / `Method.isWorkflow()` are deliberately **tolerant**
  (public, non-static, non-abstract, in the right class — attribute optional).
  Use them when you want "conceptually an activity method"; use an explicit
  `hasAttribute(TemporalClasses.ACTIVITY_METHOD)` when you truly need the
  annotation to be present.
- Inspections extend `PhpInspection` and build a `PhpElementVisitor`. Return
  early on non-matches; register problems via `holder.registerProblem(anchor, bundleMessage, ...QuickFixes)`.

## Style

- Prefer `val` over `var`. Prefer data classes over hand-rolled equals/hashCode.
- Prefer `when` over chained `if`/`else if` for disjoint cases (see
  `mixin.kt:isActivity()`).
- No wildcard imports. No `!!` unless genuinely unreachable; use `?: return`,
  `?.let`, or `requireNotNull(...)` instead.
- No unused `@Suppress` annotations, no "TODO:" without an owner/context.

## Register & verify

Whenever you add a class extended by the platform:

1. Update `plugin.xml` (common) or `language-php.xml` (PHP) — **both locations
   are required** for adapters.
2. Run `./gradlew build` to confirm XML + Kotlin compile.
3. Run `./gradlew verifyPlugin` for API-compatibility checks.
4. Run `./gradlew runIde` to smoke-test the feature in the sandbox IDE.

## Reference files (skim these before writing similar code)

| You're writing... | Open this first |
|---|---|
| An inspection | `languages/php/inspections/PhpActivityMethodInspection.kt` |
| A quick fix | `languages/php/inspections/AddActivityMethodAttributeQuickFix.kt` |
| A file-based index | `languages/php/index/PhpActivityClassIndex.kt` + `common/index/AbstractIndex.kt` |
| An EP interface | `common/extensionPoints/Activity.kt` |
| A PHP adapter | `languages/php/endpoints/PhpActivity.kt` |
| An endpoints provider | `common/endpoints/TemporalActivityEndpointsProvider.kt` |
| A tool window / action | `common/uiViewer/TemporalWindowFactory.kt`, `common/uiViewer/actions/*` |
| A settings page | `common/configuration/TemporalExecutablesConfigurable.kt` |
| A run configuration | `common/run/TemporalConfigurationType.kt` + siblings |
| A PSI helper | `languages/php/mixin.kt` |
