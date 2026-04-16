<?php

namespace Temporal\Activity {
    #[\Attribute(\Attribute::TARGET_CLASS)]
    final class ActivityInterface
    {
        public function __construct(public string $prefix = '') {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class ActivityMethod
    {
        public function __construct(public ?string $name = null) {}
    }
}

namespace Temporal\Workflow {
    #[\Attribute(\Attribute::TARGET_CLASS | \Attribute::TARGET_INTERFACE)]
    final class WorkflowInterface {}

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class WorkflowMethod
    {
        public function __construct(public ?string $name = null) {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class SignalMethod
    {
        public function __construct(public ?string $name = null) {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class QueryMethod
    {
        public function __construct(public ?string $name = null) {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class UpdateMethod
    {
        public function __construct(public ?string $name = null) {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class UpdateValidatorMethod
    {
        public function __construct(public string $forUpdate) {}
    }

    #[\Attribute(\Attribute::TARGET_METHOD)]
    final class WorkflowInit {}
}
