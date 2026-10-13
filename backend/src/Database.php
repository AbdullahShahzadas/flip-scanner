<?php
declare(strict_types=1);

namespace FlipScanner;

use PDO;
use PDOException;

/** Single shared PDO connection. */
final class Database
{
    private static ?PDO $pdo = null;

    public static function pdo(): PDO
    {
        if (self::$pdo === null) {
            try {
                self::$pdo = new PDO(
                    Config::dbDsn(),
                    Config::get('DB_USER', 'root'),
                    Config::get('DB_PASS', ''),
                    [
                        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    ]
                );
            } catch (PDOException $e) {
                http_response_code(500);
                header('Content-Type: application/json');
                echo json_encode(['error' => 'database_unavailable']);
                exit;
            }
        }
        return self::$pdo;
    }
}
