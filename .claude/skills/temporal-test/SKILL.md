---
name: temporal-test
description: Scaffold a PHPUnit test for a Temporal workflow or activity using the temporal/sdk testing helpers. Use when the user asks to "test a workflow", "test an activity", or "add temporal test".
---

# Temporal Test Scaffold (PHP)

Generate a PHPUnit test that exercises a workflow or activity without booting a
real Temporal server. Uses the `Temporal\Testing` helpers shipped with the PHP
SDK (`WorkflowEnvironment`, `ActivityMocker`).

## Ask the user (only if unclear)

- What is under test — a workflow, an activity, or an end-to-end flow?
- Target FQCN
- The PHPUnit test directory (`tests/Unit/...` vs `tests/Integration/...`)

## Snippet — workflow test with mocked activities

```php
<?php

declare(strict_types=1);

namespace Tests\Workflow;

use PHPUnit\Framework\TestCase;
use Temporal\Testing\ActivityMocker;
use Temporal\Testing\WorkflowEnvironment;

final class {{Workflow}}Test extends TestCase
{
    private WorkflowEnvironment $env;
    private ActivityMocker $mocker;

    protected function setUp(): void
    {
        $this->env = WorkflowEnvironment::startTimeSkipping();
        $this->mocker = new ActivityMocker();
    }

    protected function tearDown(): void
    {
        $this->mocker->clear();
        $this->env->stop();
    }

    public function testHappyPath(): void
    {
        $this->mocker->expectCompletion('{{ActivityName}}.{{method}}', 'fake-result');

        $client = $this->env->getWorkflowClient();
        $workflow = $client->newWorkflowStub({{Workflow}}Interface::class);

        $result = $workflow->{{method}}({{argNames}});

        self::assertSame('fake-result', $result);
    }
}
```

## Snippet — activity unit test (pure PHPUnit, no Temporal server)

Activities are plain PHP classes — just instantiate and call.

```php
final class {{Activity}}Test extends TestCase
{
    public function testDoWork(): void
    {
        $activity = new {{Activity}}(/* deps */);

        $result = $activity->{{method}}({{argNames}});

        self::assertSame({{expected}}, $result);
    }
}
```

## Conventions

- Prefer `WorkflowEnvironment::startTimeSkipping()` so `Workflow::timer()` and
  cron-style waits resolve instantly.
- Use `ActivityMocker` to stub out activity results per test — don't spin up a
  real activity worker in unit tests.
- Keep activity tests in `tests/Unit/Activity/...` and workflow tests in
  `tests/Integration/Workflow/...` (or mirror your project's existing layout).
- The IntelliJ plugin's `TemporalTypeProvider` resolves yield types for
  generator-style workflow bodies, so autocompletion inside tests is accurate.

## After generation

- Remind the user to add `temporal/sdk` testing deps if not yet installed:
  `composer require --dev temporal/sdk`.
- Wire the test into `phpunit.xml.dist` (`<testsuite name="Workflow">`).
