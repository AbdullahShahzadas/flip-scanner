-- Flip Scanner database schema (Part 1)
-- MySQL 8 / MariaDB. Run: mysql -u root -p flipscanner < database/schema.sql

CREATE DATABASE IF NOT EXISTS flipscanner CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE flipscanner;

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    google_id VARCHAR(255) NULL UNIQUE,
    display_name VARCHAR(255) NULL,
    country_code CHAR(2) NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP NULL
);

CREATE TABLE auth_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL,
    device_id VARCHAR(255) NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE subscriptions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    purchase_token TEXT NOT NULL,
    order_id VARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL,
    is_trial TINYINT(1) NOT NULL DEFAULT 0,
    auto_renewing TINYINT(1) NOT NULL DEFAULT 0,
    started_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    last_verified_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE billing_events (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usage_daily (
    user_id BIGINT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    scans_used INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE scans (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    image_path VARCHAR(1024) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'pending',
    item_title VARCHAR(255) NULL,
    brand VARCHAR(255) NULL,
    model VARCHAR(255) NULL,
    category VARCHAR(100) NULL,
    condition_guess VARCHAR(50) NULL,
    confidence DECIMAL(5,2) NULL,
    ai_raw JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE price_estimates (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scan_id BIGINT UNSIGNED NOT NULL,
    source VARCHAR(20) NOT NULL,
    low DECIMAL(10,2) NULL,
    mid DECIMAL(10,2) NULL,
    high DECIMAL(10,2) NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    sample_count INT NOT NULL DEFAULT 0,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE CASCADE
);

CREATE TABLE comparables (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scan_id BIGINT UNSIGNED NOT NULL,
    platform VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    `condition` VARCHAR(50) NULL,
    url TEXT NULL,
    image_url TEXT NULL,
    FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE CASCADE
);

CREATE TABLE listing_drafts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scan_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    platform VARCHAR(50) NOT NULL,
    title VARCHAR(255) NULL,
    description TEXT NULL,
    tags JSON NULL,
    suggested_price DECIMAL(10,2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE inventory_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    scan_id BIGINT UNSIGNED NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NULL,
    source VARCHAR(100) NULL,
    purchase_price DECIMAL(10,2) NULL,
    purchase_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'in_stock',
    listed_price DECIMAL(10,2) NULL,
    sold_price DECIMAL(10,2) NULL,
    sold_date DATE NULL,
    sold_platform VARCHAR(50) NULL,
    fees DECIMAL(10,2) NULL,
    shipping_cost DECIMAL(10,2) NULL,
    notes TEXT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE SET NULL
);

CREATE TABLE inventory_photos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT UNSIGNED NOT NULL,
    path VARCHAR(1024) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE CASCADE
);

CREATE TABLE ai_usage (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    provider VARCHAR(50) NOT NULL,
    input_tokens INT NOT NULL DEFAULT 0,
    output_tokens INT NOT NULL DEFAULT 0,
    cost_usd DECIMAL(10,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE app_config (
    `key` VARCHAR(100) NOT NULL PRIMARY KEY,
    `value` TEXT NOT NULL
);

INSERT INTO app_config (`key`, `value`) VALUES
    ('free_scans_per_day', '3'),
    ('paywall_variant', 'trial_first'),
    ('min_app_version', '1.0');
