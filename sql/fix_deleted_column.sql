-- =============================================
-- 修复逻辑删除字段名不一致问题
-- 问题：数据库表使用 is_deleted，Java使用 deleted
-- 解决：将所有表的 is_deleted 列重命名为 deleted
-- =============================================

-- 确保使用正确的数据库
USE ct_tibet_wms;

-- 修改用户表
ALTER TABLE `tb_user` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 修改角色表
ALTER TABLE `tb_role` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 修改部门表
ALTER TABLE `tb_dept` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 修改菜单表
ALTER TABLE `tb_menu` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 修改物资表
ALTER TABLE `tb_material` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 修改仓库表
ALTER TABLE `tb_warehouse` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)';

-- 验证修改结果 - 检查 deleted 列
SELECT TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'deleted'
ORDER BY TABLE_NAME;

-- 验证是否有遗漏的 is_deleted 列（应该返回空结果）
SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'is_deleted'
ORDER BY TABLE_NAME;

-- =============================================
-- 修复出库表缺少 total_amount 字段问题
-- =============================================

-- 检查是否已存在 total_amount 列，不存在则添加
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = 'ct_tibet_wms'
                   AND TABLE_NAME = 'tb_outbound'
                   AND COLUMN_NAME = 'total_amount');

SET @sql = IF(@col_exists = 0,
              'ALTER TABLE `tb_outbound` ADD COLUMN `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT ''出库总金额'' AFTER `remark`',
              'SELECT ''total_amount column already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证修改结果
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
  AND TABLE_NAME = 'tb_outbound'
  AND COLUMN_NAME = 'total_amount';
