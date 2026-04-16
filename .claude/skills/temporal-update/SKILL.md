---
name: temporal-update
description: Add a #[UpdateMethod] handler (and optional #[UpdateValidatorMethod]) to an existing Temporal PHP Workflow. Use when the user asks to "add an update", "add update handler", or "validate workflow update".
---

# Temporal Update (PHP)

Updates are synchronous, validated, state-mutating RPCs against a running
workflow. Unlike signals, they **can return a value**. They are the recommended
primitive for request/response interactions with a workflow.

## Ask the user (only if unclear)

- Target workflow interface (FQCN)
- Update name (camelCase, e.g. `approveOrder`)
- Arguments + return type
- Whether a validator is needed (recommended for anything with preconditions)

## Changes

### In `<Workflow>Interface.php`

```php
use Temporal\Workflow\UpdateMethod;
use Temporal\Workflow\UpdateValidatorMethod;

#[UpdateMethod(name: '{{updateName}}')]
public function {{updateName}}({{args}}): {{returnType}};

#[UpdateValidatorMethod(forUpdate: '{{updateName}}')]
public function validate{{UpdateName}}({{args}}): void;
```

### In `<Workflow>.php`

```php
public function validate{{UpdateName}}({{args}}): void
{
    // Throw \Temporal\Exception\Failure\ApplicationFailure if invalid.
    // Must be pure / read-only — no state mutation, no activities.
    if (!$this->canAccept({{argNames}})) {
        throw new \InvalidArgumentException('Cannot accept update right now');
    }
}

public function {{updateName}}({{args}}): {{returnType}}
{
    // Mutate state and/or yield to activities.
    $this->{{field}} = {{argNames}};
    return {{returnExpr}};
}
```

## Conventions

- Validators must be **pure** and **read-only** — no state changes, no
  activities, no signals. Rejecting an update in the validator is much cheaper
  than rejecting it inside the handler.
- Validator name is linked to the update via `#[UpdateValidatorMethod(forUpdate: '...')]`.
- The handler itself may `yield` activities and mutate state.
- Updates preserve ordering with signals and other updates in the workflow
  history.

## After generation

- Show the call site:

```php
$stub = $workflowClient->newRunningWorkflowStub({{Workflow}}Interface::class, $workflowId);
$result = $stub->{{updateName}}({{argNames}});
```
