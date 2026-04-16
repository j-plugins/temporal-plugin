---
name: temporal-signal
description: Add a #[SignalMethod] handler to an existing Temporal PHP Workflow. Use when the user asks to "add a signal", "handle a signal", or "register a workflow signal".
---

# Temporal Signal (PHP)

Add a signal handler to a workflow interface. Signals are **fire-and-forget**
messages sent to a running workflow; they must return `void` and should only
update internal state (the workflow loop will react via `Workflow::await()` /
polling state).

## Ask the user (only if unclear)

- Target workflow interface (FQCN)
- Signal name (camelCase, e.g. `cancelOrder`)
- Signal payload (args + types)

## Changes

### In `<Workflow>Interface.php`

Add the method to the interface, alongside the existing `#[WorkflowMethod]`:

```php
use Temporal\Workflow\SignalMethod;

#[SignalMethod(name: '{{signalName}}')]
public function {{signalName}}({{args}}): void;
```

### In `<Workflow>.php`

Implement the handler — store arguments on the workflow instance; do **not**
perform activities/I/O directly from a signal.

```php
private array $pendingSignals = [];

public function {{signalName}}({{args}}): void
{
    $this->pendingSignals[] = [{{argNames}}];
}
```

Then in the main `#[WorkflowMethod]` loop, await/drain:

```php
yield Workflow::await(fn () => $this->pendingSignals !== []);
$next = array_shift($this->pendingSignals);
```

## Conventions

- Signals **must return `void`** — returning anything else is a contract
  violation and will surface as a non-deterministic workflow.
- Signal handlers are **synchronous** from the workflow scheduler's perspective
  — keep them trivial (append to a queue, flip a flag, etc.).
- Use `#[SignalMethod(name: '...')]` to decouple the external signal name from
  the PHP method name (otherwise the method name is used).
- Signals are delivered at-least-once; handlers must be idempotent when that
  matters.

## After generation

- Tell the user how to send the signal from a client:

```php
$workflow = $workflowClient->newWorkflowStub({{Workflow}}Interface::class);
$run = $workflowClient->start($workflow, /* args */);
$workflow->{{signalName}}({{argNames}}); // or: $workflowClient->signalWorkflow(...)
```
