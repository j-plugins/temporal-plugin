<?php

namespace App\Workflow;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class BillingActivity
{
    // intentionally missing #[ActivityMethod]
    public function charge(int $amount): string
    {
        return '';
    }

    #[ActivityMethod]
    public function refund(string $paymentId): void
    {
    }
}

function run(BillingActivity $activity): void
{
    $activity-><warning descr="Used activity method should be marked with #[ActivityMethod] attribute.">charge</warning>(100);

    // OK — has ActivityMethod
    $activity->refund('x');
}
