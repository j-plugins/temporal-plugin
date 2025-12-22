# Temporal Plugin Contribution Guidelines

This document outlines the architecture and standards for the Temporal IntelliJ IDEA plugin. It is intended for both human contributors and AI agents.

## Project Overview
The plugin aims to improve the Developer Experience (DX) for [Temporal](https://temporal.io/) users across all IDEs based on the IntelliJ Platform. It supports multiple languages through a modular architecture.

## Architecture

The project is structured around a **Base Package**: `com.github.xepozz.temporal`

### 1. `common` Package
Contains language-agnostic logic, base classes, and Extension Points (EPs).
- **Location**: `src/main/kotlin/com/github/xepozz/temporal/common`
- **Sub-packages**:
    - `extensionPoints`: Contains all Extension Point interfaces.
    - `model`: Contains common data classes representing Temporal entities (Workflows, Activities).
- **Purpose**: Define how features should work across all languages.
- **Components**:
    - **Extension Points (EP)**: Interfaces or abstract classes that define the contract for language-specific support.
    - **Shared Providers**: Implementations of IntelliJ Platform features (like `CompletionProvider`) that iterate over registered extensions to provide a unified experience.

### 2. `languages` Package
Contains language-specific implementations (adapters).
- **Location**: `src/main/kotlin/com/github/xepozz/temporal/languages/<lang>`
- **Purpose**: Adapt language-specific PSI (Program Structure Interface) to the common Temporal features.
- **Current Status**: Currently, the focus is exclusively on **PHP**. Support for other languages (Go, Java, TypeScript, etc.) will be added in the future.
- **Example**: `com.github.xepozz.temporal.languages.php`

## Extension Point Pattern

When implementing a feature that should work across multiple languages (e.g., Activity Name completion), follow this pattern:

1.  **Define the EP Interface** in `common.extensionPoints`:
    ```kotlin
    // common/extensionPoints/Activity.kt
    interface Activity {
        fun getActivities(project: Project, module: Module? = null): List<ActivityModel>

        companion object {
            val ACTIVITY_EP = ExtensionPointName.create<Activity>("com.github.xepozz.temporal.activity")

            fun getActivities(project: Project, module: Module? = null): List<ActivityModel> {
                return ACTIVITY_EP.lazyDumbAwareExtensions(project).flatMap { it.getActivities(project, module) }.toList()
            }
        }
    }
    ```
2.  **Register the EP** in `plugin.xml`:
    ```xml
    <extensionPoints>
        <extensionPoint name="activity" interface="com.github.xepozz.temporal.common.extensionPoints.Activity"/>
    </extensionPoints>
    ```
3.  **Implement the Shared Provider** in `common`:
    ```kotlin
    // common/completion/ActivityCompletionProvider.kt
    class ActivityCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            ActivityCompletion.getActivityNames(parameters.editor.project!!).forEach {
                result.addElement(LookupElementBuilder.create(it))
            }
        }
    }
    ```
4.  **Implement the Language Adapter**:
    ```kotlin
    // languages/php/endpoints/PhpActivity.kt
    class PhpActivity : Activity {
        override fun getActivities(project: Project, module: Module?): List<ActivityModel> {
            // PHP-specific logic to find Activities
        }
    }
    ```
5.  **Register the Adapter** in the language-specific XML (e.g., `language-php.xml`):
    ```xml
    <extensions defaultExtensionNs="com.github.xepozz.temporal">
        <activity implementation="com.github.xepozz.temporal.languages.php.endpoints.PhpActivity"/>
    </extensions>
    ```

## Coding Standards

- **Kotlin First**: All new code should be written in Kotlin.
- **Performance**: Use `CachedValue` and `DumbService.isDumb()` checks where appropriate. Use `lazyDumbAwareExtensions(project)` instead of `extensionList` when accessing Extension Points to ensure better performance and compatibility with dumb mode.
- **PHP Utils**: For PHP support, use utility methods from `com.github.xepozz.temporal.languages.php.MixinKt` such as `isActivity()` and `isWorkflow()` on `PhpClass` and `Method` instead of manually checking attributes. Note that `Method.isActivity()` and `Method.isWorkflow()` are tolerant and return `true` for public methods even without explicit attributes if the containing class is an Activity or Workflow.
- **Consistency**: Follow the existing package structure. For example, if a feature is implemented for PHP in `languages.php.navigation`, any future language implementations should follow the same sub-package structure (e.g., `languages.go.navigation`).
- **Naming**:
    - Extension Points should **not** end with `EP`. They should represent the entity or feature (e.g., `ActivityCompletion`, `Workflow`).
    - Language-specific implementations should be prefixed with the language name (e.g., `Php...`).
- **Namespaces**:
    - All Extension Point names and IDs **must** start with `com.github.xepozz.temporal`.

## AI Agent Instructions

When tasked with adding a new feature:
1.  **Check for existing EP**: See if there's already a relevant EP in `common`.
2.  **Create EP if missing**: If the feature is new and can be shared, create the EP in `common` first.
3.  **Use existing patterns**: Look at `languages/php` for reference on how to interact with language-specific PSI.
4.  **Update XML**: Don't forget to register new classes in `plugin.xml` or language-specific XML files.
5.  **Be Minimal**: Implement the smallest possible change to achieve the goal while maintaining architectural integrity.
6.  **Namespace Compliance**: Ensure all new extension points and IDs start with `com.github.xepozz.temporal`.
