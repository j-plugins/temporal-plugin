# Kotlin in IntelliJ Platform plugins

Official docs: <https://plugins.jetbrains.com/docs/intellij/using-kotlin.html>

## Version alignment

- IntelliJ Platform bundles its own Kotlin runtime. Your plugin's compiled
  Kotlin must be **binary-compatible** with the bundled version.
- Kotlin **2.x is required** for platform `2025.1+`, recommended for 2024.3+.
- Compile with the JVM toolchain the platform uses — currently **Java 21**.

## Don't ship the stdlib

```properties
# gradle.properties
kotlin.stdlib.default.dependency = false
```

The IDE already ships `kotlin-stdlib`. Shipping a second copy causes
`NoSuchMethodError` / classloader collisions. `verifyPlugin` checks this.

## Idiomatic APIs

```kotlin
val app  = service<AppService>()                   // application-level service
val svc  = project.service<TemporalSettings>()     // project-level service
val ep   = ExtensionPointName.create<Activity>("com.example.activity")
ep.lazyDumbAwareExtensions(project).forEach { /* ... */ }
```

## Anti-patterns

- **No `object` declarations as extensions.** The platform uses DI to
  instantiate extension classes; a Kotlin singleton can't be re-instantiated
  on reload. Use a regular `class` + an `@Service` if you need a singleton.
- **No heavy work in `companion object { init { ... } }`.** That runs on
  classload, which in turn runs on IDE startup. Defer to first method call.
- **No `lateinit var` for platform services.** Always re-fetch via
  `service<T>()` / `project.service<T>()`.
- **No `!!` on PSI calls.** PSI operations frequently return null during
  indexing — use `?: return`.

## Interop when Java code calls you

If Java code needs to call your Kotlin API:

```kotlin
class Api {
    companion object {
        @JvmStatic
        fun of(project: Project): Api = project.service()
    }
}
```

- `@JvmStatic` on companion methods to avoid `Api.Companion.of(...)`.
- `@JvmField` on companion constants to expose them as real static fields.
- `@file:JvmName("Xyz")` on top-level function files if you want a cleaner
  Java-visible class name.

## Coroutines

Modern platform APIs expose suspending variants:

```kotlin
suspend fun load(project: Project): List<Workflow> = readAction {
    Workflow.all(project)
}

scope.launch {
    val data = load(project)
    withContext(Dispatchers.EDT) { updateUi(data) }
}
```

Do not create your own top-level `GlobalScope.launch` — bind scopes to the
lifecycle of a service or a `Disposable`. The easiest way is an
`@Service`-annotated class taking a `CoroutineScope` as a constructor
parameter (see `services.md`).

## Incremental compile hiccups

If builds OOM on large JARs, disable the classpath snapshot (rare, usually
only for platform = 2024.1 / Kotlin 1.8.20):

```properties
kotlin.incremental.useClasspathSnapshot = false
```

Most recent Kotlin versions handle this correctly.

## Style

- `val` over `var`. `data class` for plain data. `sealed class`/`sealed interface`
  for closed hierarchies.
- Extension functions over static util classes.
- Keep files short and named after the primary declaration.
- No wildcard imports.
