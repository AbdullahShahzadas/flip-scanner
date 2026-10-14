<?php
declare(strict_types=1);

use FlipScanner\Api\ComparableController;
use FlipScanner\Api\HealthController;
use FlipScanner\Api\Router;
use FlipScanner\Api\ScanController;

/**
 * Front controller. All API requests go through here.
 * Dev server: php -S 127.0.0.1:8000 -t public
 */
require __DIR__ . '/../src/bootstrap.php';

$router = new Router();

$router->get('/api/health', [HealthController::class, 'check']);

// Part 3: scan + comparables.
$router->post('/api/scan', [ScanController::class, 'scan']);
$router->get('/api/comparables', [ComparableController::class, 'search']);

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
$router->dispatch($method, $path);
