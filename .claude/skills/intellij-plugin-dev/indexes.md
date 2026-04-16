# File-based & stub indexes

Official docs: <https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html>

## When to index

You index when you need to answer "which files/elements match X?" across the
whole project *quickly*. Linear PSI scans are fine for a single file; for
project-wide lookups use an index.

## File-based index — skeleton

```kotlin
object WorkflowMethodKey {
    val NAME = ID.create<String, Void>("com.example.WorkflowMethodIndex")
}

class WorkflowMethodIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = WorkflowMethodKey.NAME
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getVersion(): Int = 1                       // bump on schema changes
    override fun dependsOnFileContent(): Boolean = true
    override fun getInputFilter(): FileBasedIndex.InputFilter =
        DefaultFileTypeSpecificInputFilter(PhpFileType.INSTANCE)

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { content ->
            val keys = mutableMapOf<String, Void?>()
            val file = content.psiFile as? PhpFile ?: return@DataIndexer keys
            file.accept(object : PhpElementVisitor() {
                override fun visitPhpMethod(m: Method) {
                    if (m.isWorkflow()) keys["${m.containingClass?.fqn}::${m.name}"] = null
                    super.visitPhpMethod(m)
                }
            })
            keys
        }
}
```

Register:

```xml
<fileBasedIndex implementation="com.example.WorkflowMethodIndex"/>
```

This repo already has `common/index/AbstractIndex.kt` — subclass it rather
than re-implementing the boilerplate.

## Querying

```kotlin
val idx = FileBasedIndex.getInstance()
val keys = idx.getAllKeys(WorkflowMethodKey.NAME, project)
val files = idx.getContainingFiles(
    WorkflowMethodKey.NAME, "App\\OrderWorkflow::run",
    GlobalSearchScope.projectScope(project),
)
idx.processValues(WorkflowMethodKey.NAME, key, /*scope*/null, { file, _ -> /* ... */ true }, scope)
```

`getAllKeys` returns *all* keys ever indexed — filter out stale ones by
checking `getContainingFiles(...).isNotEmpty()` inside a read action.

## Variants

- `ScalarIndexExtension<K>` — key only, no value. Simplest.
- `FileBasedIndexExtension<K, V>` — key + value (provide a
  `DataExternalizer<V>`).
- **Stub indexes** (`StringStubIndexExtension` et al.) — built on top of
  stored stubs of a PSI tree; return PSI elements instead of files. Preferred
  for custom-language plugins when you need element-level granularity.

## Dumb mode

Indexes are built in a background "dumb" phase. While dumb:
- `FileBasedIndex.getAllKeys` may throw `IndexNotReadyException`.
- Features that need indexes must either wait (`DumbService.runWhenSmart`) or
  implement `DumbAware` with a restricted fallback.

## Hygiene

- **Bump `getVersion()`** whenever key/value layout changes — stale caches
  otherwise linger after upgrade and cause data corruption.
- Keep indexers pure and fast; no I/O beyond `FileContent`, no PSI access
  outside `content.psiFile`, no project services.
- Filter aggressively via `getInputFilter()` — indexing irrelevant files is
  the #1 cause of slow project open.
