<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class OrderActivity
{
    #[ActivityMethod]
    public function reserve(string $orderId): void
    {
    }

    #[ActivityMethod(name: 'cancelOrder')]
    public function cancel(string $orderId): void
    {
    }

    // Public method WITHOUT the attribute — still picked up by Method.isActivity()
    public function track(string $orderId): string
    {
        return '';
    }
}
