<?php

namespace App\Service;

// Plain class — calls to its methods must NOT be flagged.
class OrderRepository
{
    public function find(string $id): ?string { return null; }

    public function save(string $id): void {}
}

function run(OrderRepository $repo): void
{
    $repo->find('x');
    $repo->save('x');
}
