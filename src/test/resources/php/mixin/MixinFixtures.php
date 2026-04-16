<?php

namespace App\Fixtures;

use Temporal\Activity\ActivityInterface;
use Temporal\Activity\ActivityMethod;
use Temporal\Workflow\WorkflowInterface;
use Temporal\Workflow\WorkflowMethod;

#[ActivityInterface]
interface MyActivityInterface
{
    public function abstractMethod(): void;
}

// Concrete class with #[ActivityInterface] — lets us test Method.isActivity() tolerance
// on concrete (non-abstract) methods, which is the code path the indexer uses.
#[ActivityInterface]
class ConcreteActivityClass
{
    #[ActivityMethod]
    public function withAttr(): void {}

    public function withoutAttr(): void {}

    public static function staticMethod(): void {}

    public function __construct() {}

    protected function protectedMethod(): void {}
}

#[WorkflowInterface]
interface MyWorkflowInterface
{
    public function abstractMethod(): void;
}

#[WorkflowInterface]
class ConcreteWorkflowClass
{
    #[WorkflowMethod]
    public function run(): string
    {
        return '';
    }

    public function helperWithoutAttribute(): void {}
}

class PlainClass
{
    public function method(): void {}
}
