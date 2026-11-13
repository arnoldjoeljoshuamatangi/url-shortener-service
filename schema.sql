-- ========================================================
-- URL Shortener Service Database Schema
-- Compatible with MySQL 8.0+
-- ========================================================

CREATE DATABASE IF NOT EXISTS `url_shortener_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `url_shortener_db`;

-- URLs Table
CREATE TABLE IF NOT EXISTS `urls` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `original_url` VARCHAR(2048) NOT NULL,
    `short_code` VARCHAR(64) NOT NULL UNIQUE,
    `custom_alias` VARCHAR(64) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NULL,
    `click_count` BIGINT NOT NULL DEFAULT 0,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX `idx_short_code` (`short_code`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Click Analytics Table
CREATE TABLE IF NOT EXISTS `click_analytics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `short_code` VARCHAR(64) NOT NULL,
    `clicked_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address` VARCHAR(64) NULL,
    `user_agent` VARCHAR(512) NULL,
    `referer` VARCHAR(512) NULL,
    `browser` VARCHAR(64) NULL,
    `os` VARCHAR(64) NULL,
    `device_type` VARCHAR(32) NULL,
    `country` VARCHAR(64) NULL,
    INDEX `idx_analytics_short_code` (`short_code`),
    INDEX `idx_analytics_clicked_at` (`clicked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

