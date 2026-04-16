# Claude Code Skills for temporal-plugin

These skills scaffold **official Temporal PHP SDK** patterns that the plugin
already recognizes (via `TemporalClasses.kt`, the file-based indexes, and the
PHP inspections). Invoke any of them in Claude Code with `/<skill-name>`.

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
prompt, the code template, and the conventions Claude must follow. The templates
follow the FQCNs declared in
`src/main/kotlin/com/github/xepozz/temporal/languages/php/TemporalClasses.kt`,
so anything they generate is picked up by the plugin's indexes and inspections
automatically.
