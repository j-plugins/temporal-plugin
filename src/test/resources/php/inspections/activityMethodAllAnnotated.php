<?php

namespace App\Activity;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;

#[ActivityInterface]
class PaymentActivity
{
    #[ActivityMethod]
    public function charge(int $amountCents): string
    {
        return '';
    }

    #[ActivityMethod(name: 'refundPayment')]
    public function refund(string $paymentId): void
    {
    }
}
