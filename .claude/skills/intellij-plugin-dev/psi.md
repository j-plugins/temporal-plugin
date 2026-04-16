# PSI — Program Structure Interface

Official docs: <https://plugins.jetbrains.com/docs/intellij/psi.html>

## What is PSI?

PSI is the semantic code model the platform builds on top of the lexer/parser
AST. Every source file becomes a `PsiFile` with a tree of `PsiElement` nodes
(classes, methods, expressions, etc.). Plugins operate on PSI — never on raw
text.

- **AST** = raw syntax tree (`ASTNode`). Rarely needed directly.
- **PSI** = AST + language-specific semantics (`PhpClass`, `PsiMethod`,
  `KtFunction`, ...). Use this.

## Getting PSI

```kotlin
val psiFile: PsiFile? = PsiManager.getInstance(project).findFile(virtualFile)
val psiEl: PsiElement  = editor.caretModel.currentCaret.let { psiFile!!.findElementAt(it.offset)!! }
```

Getting PSI is a **read** operation — wrap in a read action on background
threads.

## Navigation

```kotlin
element.parent                  // up one level
element.children                // direct children (most)
element.firstChild / .lastChild
element.nextSibling / .prevSibling
element.containingFile          // PsiFile
PsiTreeUtil.getParentOfType(element, PhpClass::class.java)
PsiTreeUtil.findChildOfType(element, Method::class.java)
PsiTreeUtil.findChildrenOfType(file, Method::class.java)
```

Always null-check `getParentOfType` results.

## Visitors

Use a visitor instead of manual recursion:

```kotlin
file.accept(object : PsiRecursiveElementWalkingVisitor() {
    override fun visitElement(element: PsiElement) {
        if (element is PhpClass && element.isWorkflow()) {
            process(element)
        }
        super.visitElement(element)
    }
})
```

For language-specific visits, use the language's visitor (e.g.
`PhpElementVisitor`, `JavaElementVisitor`, `KtTreeVisitorVoid`) — gives you
typed `visit<Node>` methods.

## References

`PsiReference` links a usage site to its declaration:

```kotlin
val refs = ReferencesSearch.search(targetElement).findAll()
val resolved = reference.resolve()           // PsiElement or null
val polyResolved = (reference as PsiPolyVariantReference).multiResolve(false)
```

Contribute new references via `PsiReferenceContributor` + `PsiReferenceProvider`
registered under `<psi.referenceContributor>`.

## Patterns

`PlatformPatterns` / `StandardPatterns` describe where a contribution applies —
used for completion, references, injections:

```kotlin
val pattern = PlatformPatterns
    .psiElement(PhpTokenTypes.STRING_LITERAL)
    .inside(PlatformPatterns.psiElement(StringLiteralExpression::class.java))
    .withLanguage(PhpLanguage.INSTANCE)
```

## Modifying PSI

Build new elements via a language's `ElementFactory` (e.g.
`PhpPsiElementFactory.createPhpAttributeFromText(project, "#[ActivityMethod]")`)
and splice them in with `addBefore`, `addAfter`, `replace`. Wrap **every
modification** in a `WriteCommandAction` (see `threading.md`).

## Validity

PSI elements can become invalid after a reparse. When jumping between actions
or threads:

```kotlin
if (!element.isValid) return
```

## Debugging

`Tools → View PSI Structure` in the IDE shows the live PSI tree of the current
file — invaluable when writing visitors or inspections.
