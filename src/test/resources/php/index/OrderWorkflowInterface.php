<?php

namespace App\Workflow;

use Temporal\Workflow\WorkflowInterface;
use Temporal\Workflow\WorkflowMethod;
use Temporal\Workflow\SignalMethod;

#[WorkflowInterface]
class OrderWorkflow
{
    #[WorkflowMethod(name: 'OrderWorkflow')]
    public function run(string $orderId): string
    {
        return '';
    }

    #[SignalMethod]
    public function cancel(): void
    {
    }
}
