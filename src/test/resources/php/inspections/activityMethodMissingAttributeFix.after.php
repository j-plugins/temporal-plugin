<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class OrderActivity
{
    #[\Temporal\Activity\ActivityMethod] public function reserve(string $orderId): void
    {
    }

    #[ActivityMethod(name: 'cancel')]
    public function cancel(string $orderId): void
    {
    }
}
