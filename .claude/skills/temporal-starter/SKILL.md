---
name: temporal-starter
description: Scaffold a Temporal PHP client starter that creates a WorkflowClient and kicks off a workflow run. Use when the user asks to "start a workflow", "create a client", or "add workflow starter".
---

# Temporal Workflow Starter (PHP)

Generate a client-side script that connects to Temporal and starts (or signals)
a workflow.

## Ask the user (only if unclear)

- Target workflow interface FQCN
- Task queue the worker is listening on
- Desired `workflowId` strategy (deterministic vs. random)
- Arguments to pass to the workflow method
- Timeouts (`executionStartToCloseTimeout`, `runStartToCloseTimeout`)

## File to create

### `starter.php` (or `bin/start-{{workflow}}.php`)

```php
<?php

declare(strict_types=1);

use Temporal\Client\GRPC\ServiceClient;
use Temporal\Client\WorkflowClient;
use Temporal\Client\WorkflowOptions;

require_once __DIR__ . '/vendor/autoload.php';

$workflowClient = WorkflowClient::create(
    ServiceClient::create($_ENV['TEMPORAL_ADDRESS'] ?? '127.0.0.1:7233'),
);

$workflow = $workflowClient->newWorkflowStub(
    {{Workflow}}Interface::class,
    WorkflowOptions::new()
        ->withTaskQueue('{{taskQueue}}')
        ->withWorkflowId('{{workflowId}}')
        ->withWorkflowExecutionTimeout('1 hour'),
);

// Fire-and-forget start (returns immediately with WorkflowRun):
$run = $workflowClient->start($workflow, {{argNames}});

printf("Started %s / %s\n", $run->getExecution()->getID(), $run->getExecution()->getRunID());

// To wait for the result synchronously instead:
// $result = $workflow->{{method}}({{argNames}});
// var_dump($result);
```

## Conventions

- **One WorkflowClient per process** is fine; it holds the gRPC channel.
- Prefer deterministic `workflowId`s (e.g. `order-{$orderId}`) so duplicate
  starts are rejected idempotently via
  `WorkflowIdReusePolicy::REJECT_DUPLICATE`.
- `start()` returns immediately with a `WorkflowRunInterface`; calling the
  annotated workflow method on the stub directly blocks until completion.
- For signal-with-start use `$workflowClient->startWithSignal(...)`.

## After generation

- Show how to query state later:
  `$stub = $workflowClient->newRunningWorkflowStub({{Workflow}}Interface::class, $workflowId);`
- Suggest wiring this into the app's CLI (e.g. a Symfony/Laravel command) rather
  than keeping a standalone script.
