---
name: temporal-query
description: Add a #[QueryMethod] handler to an existing Temporal PHP Workflow. Use when the user asks to "add a query", "expose workflow state", or "register a workflow query".
---

# Temporal Query (PHP)

Add a query handler to a workflow. Queries are **synchronous, read-only** peeks
into workflow state — they must be pure, deterministic, and cheap.

## Ask the user (only if unclear)

- Target workflow interface (FQCN)
- Query name (camelCase, e.g. `getStatus`)
- Return type

## Changes

### In `<Workflow>Interface.php`

```php
use Temporal\Workflow\QueryMethod;

#[QueryMethod(name: '{{queryName}}')]
public function {{queryName}}(): {{returnType}};
```

### In `<Workflow>.php`

```php
public function {{queryName}}(): {{returnType}}
{
    return $this->{{field}};
}
```

## Conventions

- **Read-only**: a query handler must **not** mutate workflow state, start
  activities, or emit signals. Temporal will fail the query if it detects a
  mutation.
- **Pure / deterministic**: derive results from the current workflow fields
  only.
- Exceptions thrown from a query are delivered to the caller as a
  `WorkflowQueryException`.
- Reserved/system query names (prefix `__`) are handled by Temporal itself —
  don't collide with them.

## After generation

- Show how to invoke the query from a client:

```php
$stub = $workflowClient->newRunningWorkflowStub({{Workflow}}Interface::class, $workflowId);
$status = $stub->{{queryName}}();
```
