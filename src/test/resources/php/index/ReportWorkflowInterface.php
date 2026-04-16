<?php

namespace App\Workflow;

use Temporal\Workflow\WorkflowInterface;
use Temporal\Workflow\WorkflowMethod;

#[WorkflowInterface]
class ReportWorkflow
{
    #[WorkflowMethod]
    public function generate(string $period): string
    {
        return '';
    }
}
