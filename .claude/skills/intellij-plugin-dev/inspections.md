# Local inspections & quick fixes

Official docs: <https://plugins.jetbrains.com/docs/intellij/code-inspections.html>

## Anatomy

1. **Inspection class** — extends `LocalInspectionTool` (or a language-specific
   subclass like `PhpInspection`, `AbstractKotlinInspection`).
2. **Visitor** — walks the PSI and calls
   `holder.registerProblem(anchor, message, ...quickFixes)`.
3. **Quick fix** — implements `LocalQuickFix` and mutates the PSI.
4. **Registration** — `<localInspection>` in `plugin.xml` (or the language
   config-file).
5. **Description** — `inspectionDescriptions/<shortName>.html` (required;
   verifier fails without it).

## Skeleton (PHP flavour)

```kotlin
class PhpActivityMethodInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PhpElementVisitor() {
            override fun visitPhpMethod(method: Method) {
                if (!method.isActivity()) return
                if (method.hasAttribute(TemporalClasses.ACTIVITY_METHOD)) return

                holder.registerProblem(
                    method.nameIdentifier ?: method,
                    TemporalBundle.message("inspection.php.activity.method.attribute.missing.problem.description"),
                    AddActivityMethodAttributeQuickFix(),
                )
            }
        }
}
```

## Quick-fix skeleton

```kotlin
class AddActivityMethodAttributeQuickFix : LocalQuickFix {
    override fun getFamilyName(): String =
        TemporalBundle.message("inspection.php.activity.method.attribute.missing.quickfix.name")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val method = descriptor.psiElement.parentOfType<Method>() ?: return
        val attr = PhpPsiElementFactory.createPhpAttribute(project, "#[\\Temporal\\Activity\\ActivityMethod]")
        WriteCommandAction.runWriteCommandAction(project) {
            method.addBefore(attr, method.firstChild)
        }
    }
}
```

Notes:
- `getFamilyName()` is what shows up in the UI — localise it.
- Quick-fix PSI mutations must run inside `WriteCommandAction` (the inspection
  framework wraps `applyFix` in a command, but still run `runWriteCommandAction`
  if you need undo-grouping and modality control).
- Don't rely on `descriptor.psiElement` still being the element you
  registered — resolve the enclosing node fresh (`parentOfType<...>()`).

## Registration

```xml
<extensions defaultExtensionNs="com.intellij">
    <localInspection
        language="PHP"
        groupPath="Temporal"
        groupName="PHP"
        shortName="PhpActivityMethodInspection"
        key="inspection.php.activity.method.attribute.missing.display.name"
        bundle="messages.TemporalBundle"
        enabledByDefault="true"
        level="WARNING"
        implementationClass="com.example.my.PhpActivityMethodInspection"/>
</extensions>
```

Attributes:
- `shortName` must match the class name minus the `Inspection` suffix *and*
  the HTML description filename: `inspectionDescriptions/PhpActivityMethodInspection.html`.
- `key`/`bundle` resolves the display name via i18n.
- `level` — `ERROR | WARNING | WEAK WARNING | INFORMATION`.
- `groupPath` / `groupName` determine placement in `Settings → Editor → Inspections`.

## Description HTML

```html
<html>
<body>
Reports Activity methods missing the <code>#[ActivityMethod]</code> attribute.

<!-- tooltip end -->
Temporal uses <code>#[ActivityMethod]</code> to expose methods to the worker.
Methods without it are invisible to the runtime.
</body>
</html>
```

The `<!-- tooltip end -->` marker separates the short tooltip from the long
description shown in the settings dialog.

## Testing

See `/kotlin-test` for the `BasePlatformTestCase` recipe — inspection tests
call `myFixture.enableInspections(...)` in `setUp`, then
`configureByFile` + `checkHighlighting` / `findSingleIntention` +
`checkResultByFile`.
