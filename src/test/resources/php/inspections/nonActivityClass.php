<?php

namespace App\Service;

// Plain PHP class with no ActivityInterface attribute — the inspection must ignore it.
class OrderService
{
    public function reserve(string $orderId): void {}

    public function cancel(string $orderId): void {}
}
