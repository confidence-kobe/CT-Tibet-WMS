-- =============================================
-- 西藏电信仓库管理系统 - 创建数据库脚本
-- 版本: v1.0
-- 创建日期: 2025-11-11
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ct_tibet_wms`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `ct_tibet_wms`;

-- 设置时区
SET time_zone = '+08:00';

-- 显示数据库信息
SELECT
    '数据库创建成功！' AS message,
    DATABASE() AS database_name,
    @@character_set_database AS charset,
    @@collation_database AS collation;
