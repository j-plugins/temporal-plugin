---
name: temporal-workflow
description: Scaffold an official Temporal PHP Workflow (interface + implementation) following the Temporal PHP SDK conventions recognized by the temporal-plugin. Use when the user asks to "create a workflow", "new Temporal workflow", "add workflow class", etc.
---

# Temporal Workflow (PHP)

Generate a Temporal Workflow pair: a `#[WorkflowInterface]` contract plus a concrete
implementation. The plugin's indexes (`PhpWorkflowClassIndex`,
`PhpWorkflowMethodIndex`) and `TemporalTypeProvider` rely on these exact
annotations and FQCNs.

## Ask the user (only if unclear)

- Fully qualified namespace (e.g. `App\Workflow\Order`)
- Workflow name (PascalCase, e.g. `OrderWorkflow`)
- The primary business method name (e.g. `run`, `handle`, `execute`)
- Return type (`void`, a DTO, `\Generator`, etc.)
- Input arguments (name + type)

## Files to create

### `<Namespace>/<Name>Interface.php`

```php
<?php

declare(strict_types=1);

namespace {{Namespace}};

use Temporal\Workflow\WorkflowInterface;
use Temporal\Workflow\WorkflowMethod;

#[WorkflowInterface]
interface {{Name}}Interface
{
    #[WorkflowMethod(name: '{{Name}}')]
    public function {{method}}({{args}}): {{returnType}};
}
```

### `<Namespace>/<Name>.php`

```php
<?php

declare(strict_types=1);

namespace {{Namespace}};

use Temporal\Activity\ActivityOptions;
use Temporal\Common\RetryOptions;
use Temporal\Workflow;

final class {{Name}} implements {{Name}}Interface
{
    public function {{method}}({{args}}): {{returnType}}
    {
        // Replace with real activity stubs / child workflows as needed.
        // Example:
        // $activity = Workflow::newActivityStub(
        //     SomeActivityInterface::class,
        //     ActivityOptions::new()
        //         ->withStartToCloseTimeout(30)
        //         ->withRetryOptions(RetryOptions::new()->withMaximumAttempts(3)),
        // );
        // return yield $activity->doWork($input);
    }
}
```

## Conventions

- Always put the `#[WorkflowInterface]` and `#[WorkflowMethod]` attributes on the
  **interface**. `WorkflowMethod::name` should default to the class short name.
- Implementation must be a **final** class that `implements` the interface.
- Keep workflow code **deterministic**: no direct I/O, no `random_*`, no `time()`.
  Use `Workflow::now()`, `Workflow::getInfo()`, `Workflow::sideEffect()`,
  `Workflow::mutableSideEffect()` instead.
- For waits use `yield Workflow::timer(...)` or `yield Workflow::await(...)`.
- For any long-running or non-deterministic work, call an **Activity** via
  `Workflow::newActivityStub()`.

## After generation

- Mention that the plugin's `PhpWorkflowMethodInspection` / indexes will pick up
  the new workflow automatically.
- If the user is adding this to an existing worker, remind them to register the
  workflow class with `WorkerFactory` / `WorkerInterface::registerWorkflowTypes()`.
