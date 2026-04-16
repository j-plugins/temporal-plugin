---
name: intellij-plugin-dev
description: Develop IntelliJ Platform plugins (IDEA / PhpStorm / etc.) following the official JetBrains SDK guidelines. Use when the user asks to add an extension point, service, action, listener, inspection, index, tool window, notification, run configuration, or any other platform-level plugin feature. This is the general platform skill — for this project's specific conventions also consult /kotlin-dev.
---

# IntelliJ Platform Plugin Developer

Sourced from the official JetBrains SDK docs
(<https://plugins.jetbrains.com/docs/intellij/>). Use this skill as the
foundation; layer `/kotlin-dev` on top for this repo's conventions and
`/kotlin-test` for tests.

## Decision tree — what kind of feature are you adding?

| I want to... | Open |
|---|---|
| Set up / modify `plugin.xml`, module structure, resources | `structure.md` |
| Add or consume an Extension Point | `extensions.md` |
| Expose shared state / long-lived logic | `services.md` |
| Run on EDT / background; read/write PSI safely | `threading.md` |
| Walk / analyze source code | `psi.md` |
| Look up symbols fast across the project | `indexes.md` |
| Surface code warnings + quick fixes | `inspections.md` |
| Add menu items, tool windows, notifications, listeners | `actions-ui.md` |
| Change Gradle / build / runIde / verifyPlugin | `gradle.md` |
| Kotlin-specific pitfalls and idioms | `kotlin.md` |

Each file is short and self-contained — load only the one(s) you need.

## Ten non-negotiable platform rules

1. **Never block the EDT.** Long work → `Task.Backgroundable`, coroutines, or
   `NonBlockingReadAction`.
2. **Mutate PSI / VFS inside a `WriteAction` on the EDT only.** Reads need a
   `ReadAction` from background threads.
3. **Get services on-demand; never cache them in fields** —
   `service<MyService>()` / `project.service<MyService>()`.
4. **Register listeners declaratively** in `plugin.xml`
   (`<applicationListeners>`, `<projectListeners>`) — they're created lazily.
5. **Iterate EPs lazily** — `EP.lazyDumbAwareExtensions(project)`, not
   `extensionList`, unless you truly need all extensions eagerly.
6. **Mark long-computation extensions as `DumbAware`** when they don't need
   indexes; otherwise gate them behind `DumbService.isDumb(project)`.
7. **Cache PSI-derived values** with `CachedValuesManager` + a
   `PsiModificationTracker`. Don't hand-roll memoization.
8. **Every user-visible string** comes from a `<resource-bundle>` — no inline
   English.
9. **Bump `getVersion()`** on any index whose key/value layout changes —
   otherwise stale data persists across upgrades.
10. **Inspections ship with an HTML description** at
    `inspectionDescriptions/<shortName>.html`, or they won't pass verification.

## Typical workflow for a new feature

1. Decide scope — application / project / module / per-PSI-element.
2. Pick the right mechanism (service vs. EP vs. listener vs. action).
3. Declare it in `plugin.xml` (or the right `config-file` for an optional
   dependency).
4. Implement in Kotlin. Keep classes `final` unless you *need* extensibility.
5. Wire i18n via `TemporalBundle.message("key")`.
6. Run `./gradlew build runIde verifyPlugin` — all three must pass.
7. Add tests per `/kotlin-test`.

## Reference links (official)

- Welcome: <https://plugins.jetbrains.com/docs/intellij/welcome.html>
- Plugin structure: <https://plugins.jetbrains.com/docs/intellij/plugin-structure.html>
- Threading: <https://plugins.jetbrains.com/docs/intellij/threading-model.html>
- PSI: <https://plugins.jetbrains.com/docs/intellij/psi.html>
- Indexes: <https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html>
- Kotlin for plugins: <https://plugins.jetbrains.com/docs/intellij/using-kotlin.html>
- IntelliJ Platform Gradle Plugin (v2): <https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html>
