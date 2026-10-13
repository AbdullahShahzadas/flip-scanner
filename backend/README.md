# Flip Scanner backend (PHP 8)

REST API for the Flip Scanner Android app. Part 1 = skeleton + health check.
Part 3 adds auth, scans, listings, inventory, dashboard, billing and config.

## Requirements

- PHP 8.1+ with `pdo`, `pdo_mysql`, `json`, `mbstring`
- MySQL 8 (or MariaDB)

## Quick start (local)

```bash
cd backend
cp .env.example .env          # then edit DB credentials
mysql -u root -p -e "CREATE DATABASE flipscanner CHARACTER SET utf8mb4;"
mysql -u root -p flipscanner < database/schema.sql
php -S 127.0.0.1:8000 -t public
```

Check: http://127.0.0.1:8000/api/health → `{"status":"ok",...}`

## Layout

- `public/index.php` — front controller, route list
- `src/bootstrap.php` — autoloader + `.env` loader
- `src/Config.php` — env-based config
- `src/Database.php` — shared PDO connection
- `src/Api/Router.php` — tiny router
- `src/Api/HealthController.php` — liveness probe
- `database/schema.sql` — full v1 schema
