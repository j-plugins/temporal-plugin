<?php

namespace App\Workflow;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class ShippingActivity
{
    #[ActivityMethod]
    public function ship(string $orderId): void
    {
    }

    #[ActivityMethod(name: 'trackShipment')]
    public function track(string $orderId): string
    {
        return '';
    }
}

function run(ShippingActivity $activity): void
{
    $activity->ship('order-1');
    $activity->track('order-1');
}
