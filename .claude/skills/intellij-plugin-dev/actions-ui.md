# Actions, listeners, notifications, tool windows

## Actions

Official docs: <https://plugins.jetbrains.com/docs/intellij/basic-action-system.html>

```kotlin
class RefreshPageAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        project.service<TemporalWebUIPanel>().reload()
    }
}
```

Registration:

```xml
<actions>
    <action id="Temporal.RefreshPage"
            class="com.example.my.RefreshPageAction"
            text="Refresh" description="Refresh the Temporal UI"
            icon="AllIcons.Actions.Refresh">
        <add-to-group group-id="ToolWindowContextMenu" anchor="last"/>
        <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt R"/>
    </action>
</actions>
```

Rules:
- **Always override `getActionUpdateThread()`** — pick `BGT` when `update()`
  reads project/PSI state; `EDT` only for pure UI checks. Using BGT avoids
  freezes.
- `update()` must be fast. Offload work to `actionPerformed`.
- Define a `groupId` via `<group id="..." class="com.intellij.openapi.actionSystem.DefaultActionGroup">`
  if you need a submenu; add actions to it with `<add-to-group>`.

## Listeners & MessageBus

Official docs: <https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html>

Declarative (preferred — lazy, no startup cost):

```xml
<applicationListeners>
    <listener class="com.example.my.MyAppListener"
              topic="com.intellij.openapi.application.ApplicationActivationListener"/>
</applicationListeners>

<projectListeners>
    <listener class="com.example.my.MyProjectListener"
              topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>
</projectListeners>
```

Programmatic:

```kotlin
project.messageBus.connect(parentDisposable)
    .subscribe(BulkFileListener.TOPIC, object : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) { /* ... */ }
    })
```

Custom topics:

```kotlin
interface ServerListener {
    fun onServerStarted(event: ServerStarted)
    companion object {
        @Topic.ProjectLevel
        val TOPIC: Topic<ServerListener> = Topic.create("Temporal server", ServerListener::class.java)
    }
}
```

Rules:
- Listener implementations must be **stateless**; persist state in services.
- Always `connect(parentDisposable)` programmatically — orphan connections
  leak.

## Notifications

```xml
<extensions defaultExtensionNs="com.intellij">
    <notificationGroup id="Temporal"
                       displayType="BALLOON"
                       isLogByDefault="true"
                       bundle="messages.TemporalBundle"
                       key="notification.group"/>
</extensions>
```

`displayType`: `BALLOON` (transient popup), `STICKY_BALLOON` (stays until
dismissed), `TOOL_WINDOW` (shown inside the target tool window, needs
`toolWindowId=`), `NONE` (event log only).

Emit:

```kotlin
NotificationGroupManager.getInstance()
    .getNotificationGroup("Temporal")
    .createNotification(
        TemporalBundle.message("notification.server.started.title"),
        TemporalBundle.message("notification.server.started.content", port),
        NotificationType.INFORMATION,
    )
    .notify(project)
```

## Tool windows

Official docs: <https://plugins.jetbrains.com/docs/intellij/tool-windows.html>

```xml
<toolWindow id="Temporal"
            icon="/icons/temporal/icon.svg"
            anchor="right"
            factoryClass="com.example.my.TemporalWindowFactory"
            doNotActivateOnStart="true"
            secondary="false"/>
```

```kotlin
class TemporalWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = TemporalWebUIPanel(project)
        val content = toolWindow.contentManager.factory.createContent(panel, "Web UI", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project) = true   // gate visibility
}
```

Retrieve at runtime:

```kotlin
ToolWindowManager.getInstance(project).getToolWindow("Temporal")?.show()
```

Rules:
- Implement `DumbAware` unless the tool window genuinely needs indexes.
- `createToolWindowContent` runs on EDT — keep it cheap; defer heavy UI wiring
  to a background task or to a button click inside the panel.
