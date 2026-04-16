# Extension points & extensions

Official docs: <https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html>

## Declaring your own EP

```xml
<extensionPoints>
    <extensionPoint name="activity" dynamic="true"
                    interface="com.example.my.extensionPoints.Activity"/>

    <!-- bean-class variant (XML-configurable) -->
    <extensionPoint name="frameworkSupport" beanClass="com.intellij.util.xmlb.BaseKeyedLazyInstance">
        <with attribute="implementationClass" implements="com.example.my.FrameworkSupport"/>
    </extensionPoint>
</extensionPoints>
```

Guidelines:

- `name` must be unique **within your plugin**; the full ID becomes
  `<pluginId>.<name>`.
- `dynamic="true"` allows the EP to be loaded/unloaded at runtime (required
  for v2 plugins that support hot reload).
- Choose `interface` for code-driven extensions, `beanClass` for XML-configured
  extensions with attributes.

## Consuming an EP in code

```kotlin
interface Activity {
    fun getActivities(project: Project): List<ActivityModel>

    companion object {
        val EP = ExtensionPointName.create<Activity>("com.example.my.activity")

        fun all(project: Project): List<ActivityModel> =
            EP.lazyDumbAwareExtensions(project)
              .flatMap { it.getActivities(project) }
              .toList()
    }
}
```

- Prefer `lazyDumbAwareExtensions(project)` — returns a lazy `Sequence` that
  skips non-`DumbAware` extensions while indexing is in progress.
- `extensionList` / `extensions` eagerly instantiate **all** extensions — use
  only if you actually need every one, and only from background threads.
- Extensions throwing `ExtensionNotApplicableException` in their constructor
  are silently skipped. Use this to opt out based on runtime state.

## Contributing to someone's EP

```xml
<extensions defaultExtensionNs="com.intellij">
    <localInspection implementationClass="com.example.my.MyInspection" .../>
    <applicationService serviceImplementation="com.example.my.MyService"/>
</extensions>

<!-- Contributing to YOUR OWN EP -->
<extensions defaultExtensionNs="com.example.my">
    <activity implementation="com.example.my.PhpActivity"/>
</extensions>
```

`defaultExtensionNs` is the plugin ID that **owns** the EP, not the caller.

## Rules for extension implementations

- **Stateless.** Per-instance mutable state breaks with dynamic reloading.
  Store runtime state in a service instead.
- **Cheap constructors.** No I/O, no heavy initialization. Defer work to the
  first real call.
- **No `object` singletons** in Kotlin for extension classes — the platform
  instantiates them itself.
- **Thread-safety.** Extensions are called from many threads; assume concurrent
  invocation.

## Optional-dependency pattern

1. `<depends optional="true" config-file="x.xml">other.plugin</depends>`.
2. Put all `<extensions>` that rely on `other.plugin`'s APIs inside `x.xml`.
3. If `other.plugin` is absent, `x.xml` is simply not loaded — the base plugin
   keeps working with reduced features.
