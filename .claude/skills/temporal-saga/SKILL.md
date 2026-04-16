---
name: temporal-saga
description: Scaffold a Temporal Saga compensation pattern inside a PHP workflow using the official \Temporal\Internal\Workflow\ActivityProxy / Saga helper. Use when the user asks for "saga", "compensation", or "rollback pattern".
---

# Temporal Saga / Compensation (PHP)

Generate the classic Saga scaffold: a sequence of activities each paired with a
compensating action, rolled back in reverse order on failure.

## Ask the user (only if unclear)

- Host workflow class (FQCN)
- Ordered list of forward activities and their compensations
- Whether the saga should continue-on-failure or abort on first failure

## Snippet — inside a `#[WorkflowMethod]`

```php
use Temporal\Activity\ActivityOptions;
use Temporal\Workflow;

$opts = ActivityOptions::new()->withStartToCloseTimeout(30);

$bookFlight  = Workflow::newActivityStub(FlightActivityInterface::class,  $opts);
$bookHotel   = Workflow::newActivityStub(HotelActivityInterface::class,   $opts);
$bookCar     = Workflow::newActivityStub(CarActivityInterface::class,     $opts);

$saga = new Workflow\Saga();
$saga->setParallelCompensation(false); // true => run compensations concurrently

try {
    $flightRef = yield $bookFlight->reserve($tripId);
    $saga->addCompensation(fn () => yield $bookFlight->cancel($flightRef));

    $hotelRef = yield $bookHotel->reserve($tripId);
    $saga->addCompensation(fn () => yield $bookHotel->cancel($hotelRef));

    $carRef = yield $bookCar->reserve($tripId);
    $saga->addCompensation(fn () => yield $bookCar->cancel($carRef));

    return new TripReceipt($flightRef, $hotelRef, $carRef);
} catch (\Throwable $e) {
    yield $saga->compensate();
    throw $e;
}
```

## Conventions

- Register each compensation **after** its forward step succeeds — never before.
- Compensations run in **reverse** registration order by default.
- Compensations themselves should be idempotent — Temporal may retry the whole
  compensation cycle after a worker crash.
- Prefer `setParallelCompensation(false)` for ordering-sensitive rollbacks
  (flights must be cancelled before the payment is refunded, etc.).
- Don't `throw` from inside a compensation unless you want to abort the rollback
  chain — Temporal surfaces the original exception after `compensate()` resolves.

## After generation

- Remind the user that each activity + its compensation should be modeled as
  methods on the same `#[ActivityInterface]`, and that the plugin will
  highlight any compensation method missing `#[ActivityMethod]`.
