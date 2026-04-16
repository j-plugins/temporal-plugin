---
name: temporal-worker
description: Scaffold a Temporal PHP worker bootstrap (worker.php) that registers workflows and activities with a WorkerFactory. Use when the user asks to "create a worker", "set up temporal worker", or "add worker bootstrap".
---

# Temporal Worker Bootstrap (PHP)

Generate an entry-point script that boots a Temporal worker process using
RoadRunner + the Temporal PHP SDK.

## Ask the user (only if unclear)

- Task queue name (e.g. `default`, `orders`)
- List of workflow FQCNs to register
- List of activity FQCNs to register
- Whether a DI container is in use (PSR-11) and where it is wired up

## Files to create

### `worker.php` (project root)

```php
<?php

declare(strict_types=1);

use Temporal\WorkerFactory;

require_once __DIR__ . '/vendor/autoload.php';

$factory = WorkerFactory::create();

$worker = $factory->newWorker(
    taskQueue: '{{taskQueue}}',
);

// --- Workflows ---------------------------------------------------------------
// Workflows are stateless from the worker's perspective — Temporal instantiates
// them per-run; register the class name.
$worker->registerWorkflowTypes(
    {{Workflow1}}::class,
    // {{Workflow2}}::class,
);

// --- Activities --------------------------------------------------------------
// Activities are real objects — wire them through your container so
// constructor dependencies resolve.
$worker->registerActivity(
    {{Activity1}}::class,
    // Optional factory: fn (ReflectionClass $r) => $container->get($r->getName()),
);

// Start the worker; this blocks and serves RR requests.
$factory->run();
```

### `.rr.yaml` (if missing — ask before overwriting)

```yaml
version: '3'

rpc:
  listen: tcp://127.0.0.1:6001

server:
  command: "php worker.php"

temporal:
  address: ${TEMPORAL_ADDRESS:-127.0.0.1:7233}
  activities:
    num_workers: 4
```

## Conventions

- Register **workflow types** (class names) — the SDK instantiates a fresh one
  for each workflow run.
- Register **activity instances** (or factories) — activities are real PHP
  objects, usually constructed by your DI container.
- One worker = one task queue. Run multiple `newWorker(...)` calls on the same
  factory if you need multiple task queues in one process.
- Keep `worker.php` slim: the real wiring belongs in your container config.

## After generation

- Run `vendor/bin/rr serve` to start the worker locally.
- Remind the user that the plugin's run configuration (`TemporalConfigurationType`)
  boots the Temporal dev server; the worker connects to whatever
  `TEMPORAL_ADDRESS` resolves to.
