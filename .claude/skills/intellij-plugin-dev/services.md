# Services (application / project / module)

Official docs: <https://plugins.jetbrains.com/docs/intellij/plugin-services.html>

## Scopes

| Scope | Lifetime | Declaration |
|---|---|---|
| Application | IDE process | `@Service` / `<applicationService>` |
| Project | Open project | `@Service(Service.Level.PROJECT)` / `<projectService>` |
| Module | Module lifetime | `<moduleService>` (avoid — memory heavy) |

## Light services (preferred)

```kotlin
@Service(Service.Level.APP)
class IconCache {
    private val cache = ConcurrentHashMap<String, Icon>()
    fun get(key: String): Icon = cache.getOrPut(key) { load(key) }
}

@Service(Service.Level.PROJECT)
class TemporalExecutablesSettings(private val project: Project) {
    var executables: List<TemporalExecutable> = emptyList()
}
```

Rules:
- Must be `final`.
- Constructor may take `Project` (for project services) and/or `CoroutineScope`.
- **No XML registration needed** — `@Service` is enough.
- Not overrideable by other plugins.

## Classic services

Use when you need an interface + swappable implementation, or when the service
must be visible as an API to other plugins:

```xml
<extensions defaultExtensionNs="com.intellij">
    <applicationService
        serviceInterface="com.example.my.Api"
        serviceImplementation="com.example.my.ApiImpl"/>
    <projectService
        serviceImplementation="com.example.my.StarterServerService"/>
</extensions>
```

## Retrieval

```kotlin
// Kotlin, idiomatic
val icons = service<IconCache>()
val settings = project.service<TemporalExecutablesSettings>()

// Java
IconCache icons = ApplicationManager.getApplication().getService(IconCache.class);
TemporalExecutablesSettings s = project.getService(TemporalExecutablesSettings.class);
```

Rules:
- **Always on-demand.** Never cache a service reference in a field — it breaks
  across project close / plugin reload.
- **No read action needed** to obtain a service. It's safe from any thread.
- **Don't inject services via constructor** of another service / extension —
  that pattern is deprecated; call `service<T>()` inside methods instead.

## State persistence

Implement `PersistentStateComponent<State>` and annotate with `@State`:

```kotlin
@Service(Service.Level.PROJECT)
@State(name = "TemporalSettings",
       storages = [Storage("temporal.xml")])
class TemporalSettings : PersistentStateComponent<TemporalSettings.State> {
    data class State(var address: String = "127.0.0.1:7233")
    private var state = State()
    override fun getState(): State = state
    override fun loadState(loaded: State) { state = loaded }
}
```

## Disposal & coroutines

- Services implementing `Disposable` receive a `dispose()` callback — register
  disposables with `Disposer.register(this, child)`.
- A `CoroutineScope` parameter is tied to the service lifetime:

```kotlin
@Service(Service.Level.PROJECT)
class BackgroundWorker(private val scope: CoroutineScope) {
    fun schedule() = scope.launch { /* cancelled on project close */ }
}
```
