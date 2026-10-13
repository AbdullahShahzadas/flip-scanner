<?php
declare(strict_types=1);

namespace FlipScanner\Api;

/** Tiny router for Part 1. Part 3 will add middleware (auth) here. */
final class Router
{
    /** @var array<string, array<string, callable|array>> */
    private array $routes = [];

    public function get(string $path, callable|array $handler): void
    {
        $this->routes['GET'][$path] = $handler;
    }

    public function post(string $path, callable|array $handler): void
    {
        $this->routes['POST'][$path] = $handler;
    }

    public function dispatch(string $method, string $path): void
    {
        header('Content-Type: application/json');
        $handler = $this->routes[$method][$path] ?? null;
        if ($handler === null) {
            http_response_code(404);
            echo json_encode(['error' => 'not_found', 'path' => $path]);
            return;
        }
        if (is_array($handler)) {
            [$class, $fn] = $handler;
            (new $class())->$fn();
            return;
        }
        $handler();
    }
}
