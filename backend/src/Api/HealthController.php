<?php
declare(strict_types=1);

namespace FlipScanner\Api;

/** Liveness probe used by the app and by deploys. */
final class HealthController
{
    public function check(): void
    {
        echo json_encode([
            'status' => 'ok',
            'service' => 'flip-scanner-backend',
            'time' => gmdate('c'),
        ]);
    }
}
