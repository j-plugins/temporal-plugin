<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class OrderActivity
{
    public function <warning descr="Activity method should be marked with #[ActivityMethod] attribute.">reserve</warning>(string $orderId): void
    {
    }

    #[ActivityMethod(name: 'cancel')]
    public function cancel(string $orderId): void
    {
    }
}
