---
name: temporal-child-workflow
description: Scaffold invocation of a child Temporal workflow from a parent workflow using ChildWorkflowOptions. Use when the user asks to "call a child workflow", "invoke child workflow", or "start child workflow".
---

# Temporal Child Workflow (PHP)

Add code to a parent workflow that launches and awaits a child workflow.
The plugin recognizes `\Temporal\Workflow\ChildWorkflowStubInterface`
(`TemporalClasses.CHILD_WORKFLOW_STUB`).

## Ask the user (only if unclear)

- Parent workflow class
- Child workflow interface FQCN
- Whether the parent should `await` the child (typed stub) or fire-and-forget
  (untyped `ChildWorkflowStub`)
- Parent-close policy (terminate / abandon / cancel)

## Snippet — typed child stub (awaits result)

```php
use Temporal\Workflow;
use Temporal\Workflow\ChildWorkflowOptions;
use Temporal\Workflow\ParentClosePolicy;

$child = Workflow::newChildWorkflowStub(
    {{Child}}Interface::class,
    ChildWorkflowOptions::new()
        ->withWorkflowId('{{childWorkflowId}}')
        ->withTaskQueue('{{taskQueue}}')
        ->withParentClosePolicy(ParentClosePolicy::POLICY_TERMINATE),
);

$result = yield $child->{{method}}({{argNames}});
```

## Snippet — untyped stub (fire-and-forget, signal-capable)

```php
use Temporal\Workflow;
use Temporal\Workflow\ChildWorkflowStubInterface;

$child = Workflow::newUntypedChildWorkflowStub(
    '{{ChildWorkflowName}}',
    ChildWorkflowOptions::new()->withWorkflowId('{{childWorkflowId}}'),
);

yield $child->start({{argNames}});
// $child->signal('someSignal', $payload);
```

## Conventions

- Typed stubs enforce the child's interface — prefer them for normal call/await.
- The child is part of the parent's history; cancelling the parent propagates
  to the child unless `ParentClosePolicy::POLICY_ABANDON` is used.
- Don't instantiate a child stub outside a `#[WorkflowMethod]` scope — the
  `Workflow::` static helpers only work inside a running workflow.
- `ChildWorkflowOptions::withWorkflowId` is required for deterministic IDs; omit
  for an auto-generated UUID.

## After generation

- Point out that launching the same child twice with the same workflow ID
  triggers Temporal's duplicate-workflow handling (controlled by
  `WorkflowIdReusePolicy`).
