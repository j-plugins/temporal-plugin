# Threading model

Official docs: <https://plugins.jetbrains.com/docs/intellij/threading-model.html>

## Two worlds

- **EDT** (Event Dispatch Thread) — exactly one; owns UI updates and *all*
  write access to PSI/VFS/project model. Must stay responsive (< 100 ms).
- **BGT** (background threads) — many; perform long work, indexing reads,
  process launches, network I/O.

## Read / write locks

| Action | Where | Purpose |
|---|---|---|
| `ReadAction.run { ... }` / `runReadAction { ... }` | any thread | Read PSI / VFS / indexes |
| `ReadAction.nonBlocking { ... }.submit(...)` | BGT | Read that yields to pending writes |
| `WriteAction.run { ... }` / `runWriteAction { ... }` | **EDT only** | Modify PSI / VFS / project model |
| `invokeLater { ... }` | anywhere → EDT | Schedule work on EDT |
| `invokeAndWait { ... }` | BGT → EDT (blocking) | Rare; use sparingly |

## Patterns

### Short read on background, then update UI

```kotlin
ReadAction
    .nonBlocking<List<Workflow>> { Workflow.all(project) }
    .inSmartMode(project)
    .finishOnUiThread(ModalityState.defaultModalityState()) { list ->
        updateUi(list)
    }
    .submit(AppExecutorUtil.getAppExecutorService())
```

### Long work with progress

```kotlin
ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Indexing workflows") {
    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
        // do the work; periodically indicator.checkCanceled()
    }
})
```

### Writing PSI

```kotlin
WriteCommandAction.runWriteCommandAction(project, "Add ActivityMethod attribute", null, {
    // edit PSI here; wrapped in a write action + undo-friendly command
    method.addBefore(factory.createAttribute("..."), method.firstChild)
}, psiFile)
```

## Modality state

Dialogs create modal contexts. When scheduling EDT work from a background
thread, pass a `ModalityState`:

```kotlin
ApplicationManager.getApplication().invokeLater(
    { updateUi() },
    ModalityState.defaultModalityState(),
)
```

Without it, your runnable may fire while a modal dialog is open — risking
corrupted state.

## Kotlin coroutines (2024.1+)

```kotlin
@Service(Service.Level.PROJECT)
class Foo(private val scope: CoroutineScope) {
    fun refresh() = scope.launch {
        val data = readAction { Workflow.all(project) } // suspends; cooperative with writes
        withContext(Dispatchers.EDT) { updateUi(data) }
    }
}
```

- `readAction { ... }` and `writeAction { ... }` are suspending analogues of
  the blocking actions, and respect cancellation.
- Prefer `Dispatchers.EDT` over `invokeLater` inside coroutines.

## Hard rules

- Never call blocking I/O, `Thread.sleep`, or long computations on EDT.
- Never hold the read lock across EDT ↔ BGT transitions — release and reacquire.
- Always check object validity (`psiElement.isValid`) between read actions.
- Writes **must** be inside `WriteCommandAction` for anything user-visible
  (undo support).
