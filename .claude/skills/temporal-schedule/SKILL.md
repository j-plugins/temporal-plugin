---
name: temporal-schedule
description: Scaffold a Temporal Schedule (cron-like recurring workflow) using ScheduleClient and ScheduleSpec. Use when the user asks to "schedule a workflow", "cron workflow", or "recurring workflow".
---

# Temporal Schedule (PHP)

Generate a script that creates a Temporal Schedule — the modern replacement for
cron-based workflows. A Schedule fires a configured workflow on a `ScheduleSpec`
(interval, calendar expression, or cron string).

## Ask the user (only if unclear)

- Workflow interface FQCN to schedule
- Task queue
- Schedule ID (e.g. `daily-report`)
- Cadence: interval (`PT1H`) **or** cron (`0 9 * * *`) **or** calendar spec

## File to create

### `schedule.php` (or `bin/schedule-{{workflow}}.php`)

```php
<?php

declare(strict_types=1);

use Temporal\Client\GRPC\ServiceClient;
use Temporal\Client\Schedule\Action\StartWorkflowAction;
use Temporal\Client\Schedule\Schedule;
use Temporal\Client\Schedule\ScheduleOptions;
use Temporal\Client\Schedule\Spec\ScheduleSpec;
use Temporal\Client\ScheduleClient;

require_once __DIR__ . '/vendor/autoload.php';

$schedules = ScheduleClient::create(
    ServiceClient::create($_ENV['TEMPORAL_ADDRESS'] ?? '127.0.0.1:7233'),
);

$action = StartWorkflowAction::new({{Workflow}}Interface::class)
    ->withTaskQueue('{{taskQueue}}')
    ->withWorkflowId('{{workflowIdPrefix}}-{{scheduledStartTime}}')
    ->withInput([{{argNames}}]);

$spec = ScheduleSpec::new()
    ->withCronExpressions('{{cron}}'); // or ->withIntervalList('PT1H')

$schedule = Schedule::new()
    ->withAction($action)
    ->withSpec($spec);

$schedules->createSchedule(
    schedule: $schedule,
    options: ScheduleOptions::new()->withTriggerImmediately(false),
    scheduleId: '{{scheduleId}}',
);
```

## Conventions

- **One schedule per logical job**. Don't overload a schedule with multiple
  unrelated workflows — create separate schedules instead.
- `ScheduleSpec` supports cron, interval, and calendar specs — pick one, mixing
  is rarely what you want.
- Use `overlap policy` (`ScheduleOverlapPolicy`) to decide what happens when a
  new trigger fires while the previous run is still executing (skip / buffer
  one / buffer all / cancel other / terminate other / allow all).
- Use `pause(reason: ...)` on the schedule handle to temporarily halt firing
  without deleting history.

## After generation

- Show the management commands the user is most likely to need next:

```php
$handle = $schedules->getHandle('{{scheduleId}}');
$handle->describe();
$handle->pause('maintenance');
$handle->unpause();
$handle->trigger();      // one-off run, respects overlap policy
$handle->delete();
```
