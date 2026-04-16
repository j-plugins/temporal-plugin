# Claude Code Skills for temporal-plugin

Invoke any skill in Claude Code with `/<skill-name>`.

## Plugin-development skills (this repo's stack)

| Skill | Purpose |
|---|---|
| `/intellij-plugin-dev` | General IntelliJ Platform plugin-dev rules (sourced from the official JetBrains SDK docs). Entry point with topic files: `structure.md`, `extensions.md`, `services.md`, `threading.md`, `psi.md`, `indexes.md`, `inspections.md`, `actions-ui.md`, `gradle.md`, `kotlin.md`. |
| `/kotlin-dev` | Project-specific Kotlin conventions (common vs. languages packages, EP pattern, PHP mixins, naming, performance). |
| `/kotlin-test` | Writing JUnit 4 + IntelliJ Platform Test Framework tests for inspections, indexes, EP impls, and pure logic. |

**How they layer:** `/intellij-plugin-dev` is the foundation (general platform
knowledge). `/kotlin-dev` adds this repo's conventions on top. `/kotlin-test`
covers tests for everything either produces.

## Temporal SDK scaffolding skills

These generate **official Temporal PHP SDK** patterns the plugin already
recognises via `TemporalClasses.kt`, the file-based indexes, and the PHP
inspections.

| Skill | Purpose |
|---|---|
| `/temporal-workflow` | Create a `#[WorkflowInterface]` + implementation pair |
| `/temporal-activity` | Create an `#[ActivityInterface]` + implementation pair |
| `/temporal-signal` | Add a `#[SignalMethod]` handler to a workflow |
| `/temporal-query` | Add a `#[QueryMethod]` handler to a workflow |
| `/temporal-update` | Add an `#[UpdateMethod]` (+ optional validator) to a workflow |
| `/temporal-child-workflow` | Invoke a child workflow with `ChildWorkflowOptions` |
| `/temporal-saga` | Generate a Saga / compensation scaffold |
| `/temporal-schedule` | Create a Temporal Schedule (cron/interval) via `ScheduleClient` |
| `/temporal-worker` | Generate `worker.php` + `.rr.yaml` worker bootstrap |
| `/temporal-starter` | Generate a client script that starts a workflow |
| `/temporal-test` | Scaffold PHPUnit tests using `WorkflowEnvironment` / `ActivityMocker` |

Each skill lives under `.claude/skills/<name>/SKILL.md` and contains the full
prompt, the code template, and the conventions Claude must follow.
