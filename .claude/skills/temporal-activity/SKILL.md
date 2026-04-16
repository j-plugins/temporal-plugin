---
name: temporal-activity
description: Scaffold an official Temporal PHP Activity (interface + implementation) using #[ActivityInterface] / #[ActivityMethod] as recognized by the temporal-plugin. Use when the user asks to "create an activity", "new Temporal activity", or "add activity class".
---

# Temporal Activity (PHP)

Generate a Temporal Activity pair following the PHP SDK conventions. The plugin's
`PhpActivityClassIndex`, `PhpActivityMethodIndex`, `PhpActivityMethodInspection`,
and `PhpActivityMethodUsageInspection` all key off these attributes.

## Ask the user (only if unclear)

- Fully qualified namespace (e.g. `App\Activity\Order`)
- Activity class name (PascalCase, e.g. `OrderActivity`)
- One or more methods (name, args, return type)
- Optional activity prefix (passed to `#[ActivityInterface(prefix: '...')]`)

## Files to create

### `<Namespace>/<Name>Interface.php`

```php
<?php

declare(strict_types=1);

namespace {{Namespace}};

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface(prefix: '{{prefix}}')]
interface {{Name}}Interface
{
    #[ActivityMethod(name: '{{methodName}}')]
    public function {{method}}({{args}}): {{returnType}};
}
```

### `<Namespace>/<Name>.php`

```php
<?php

declare(strict_types=1);

namespace {{Namespace}};

use Temporal\Activity;

final class {{Name}} implements {{Name}}Interface
{
    public function {{method}}({{args}}): {{returnType}}
    {
        // Regular PHP code: blocking I/O, DB calls, HTTP, etc. are fine here.
        // Use Activity::heartbeat($progress) for long-running work.
        // Throw any exception to signal failure; Temporal retries per RetryOptions.
    }
}
```

## Conventions

- The `#[ActivityInterface]` attribute goes on the **interface**, not the class.
- Every method that should be callable from a workflow should carry
  `#[ActivityMethod]`. The plugin's `PhpActivityMethodInspection` surfaces a
  warning + quick-fix (`AddActivityMethodAttributeQuickFix`) if it is missing.
- Keep activity constructors free of framework magic; Temporal resolves them via
  your container. Inject dependencies normally.
- For idempotency use `Activity::getInfo()->attempt` and deterministic IDs.
- Use `Activity::heartbeat($details)` inside long-running activities to support
  cancellation and progress reporting.

## After generation

- Suggest wiring the activity into the worker bootstrap:
  `$worker->registerActivity({{Name}}::class, fn ($ctx) => $container->get({{Name}}::class));`
- Remind the user to declare a stub in the calling workflow:
  `Workflow::newActivityStub({{Name}}Interface::class, ActivityOptions::new()->withStartToCloseTimeout(30))`.
