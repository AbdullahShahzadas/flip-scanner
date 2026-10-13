<?php
declare(strict_types=1);

namespace FlipScanner;

/** Reads configuration from $_ENV with safe defaults. */
final class Config
{
    public static function get(string $key, string $default = ''): string
    {
        $value = $_ENV[$key] ?? $default;
        return is_string($value) ? $value : $default;
    }

    public static function dbDsn(): string
    {
        $host = self::get('DB_HOST', '127.0.0.1');
        $port = self::get('DB_PORT', '3306');
        $name = self::get('DB_NAME', 'flipscanner');
        return "mysql:host={$host};port={$port};dbname={$name};charset=utf8mb4";
    }
}
