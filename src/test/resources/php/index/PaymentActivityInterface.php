<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface(prefix: 'payment.')]
class PaymentActivity
{
    #[ActivityMethod]
    public function charge(int $amountCents): string
    {
        return '';
    }

    #[ActivityMethod]
    public function refund(string $paymentId): void
    {
    }
}
