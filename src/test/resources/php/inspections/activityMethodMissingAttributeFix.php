<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class OrderActivity
{
    public function rese<caret>rve(string $orderId): void
    {
    }

    #[ActivityMethod(name: 'cancel')]
    public function cancel(string $orderId): void
    {
    }
}
