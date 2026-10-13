<?php
declare(strict_types=1);

/**
 * Front controller. All API requests go through here.
 * Dev server: php -S 127.0.0.1:8000 -t public
 */
require __DIR__ . '/../src/bootstrap.php';

use FlipScanner\Api\HealthController;
use FlipScanner\Api\Router;

$router = new Router();

// Part 1: health check only. Part 3 adds auth, scans, listings,
// inventory, dashboard, billing and config endpoints.
$router->get('/api/health', [HealthController::class, 'check']);

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
$router->dispatch($method, $path);
