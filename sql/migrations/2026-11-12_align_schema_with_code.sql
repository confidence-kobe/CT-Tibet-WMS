-- =============================================
-- 迁移脚本：补齐数据库表结构与后端代码不一致的字段
-- 日期: 2026-11-12
--
-- 适用于：用旧版 sql/schema.sql 建好、已经在使用的数据库。
-- 全新安装请直接执行 create_database.sql → schema.sql → init_data.sql，无需执行本脚本。
--
-- 执行方式（只执行一次；重复执行会报 "Duplicate column" 错误）：
--   mysql -uroot -p --default-character-set=utf8mb4 ct_tibet_wms < migrations/2026-11-12_align_schema_with_code.sql
--
-- 执行前请先备份数据库：
--   mysqldump -uroot -p ct_tibet_wms > backup_before_migration.sql
-- =============================================

SET NAMES utf8mb4;


-- 逻辑删除标记：所有实体继承 BaseEntity，查询都会带 deleted = 0
ALTER TABLE tb_apply            ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_apply_detail     ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_inbound          ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_inbound_detail   ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_inventory        ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_inventory_log    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_message          ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_outbound         ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';
ALTER TABLE tb_outbound_detail  ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除 1-已删除)';

-- 审计字段
ALTER TABLE tb_apply            ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_apply_detail     ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_inbound_detail   ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_inventory        ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_inventory_log    ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_message          ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';
ALTER TABLE tb_outbound_detail  ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID', ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID';

-- 库存乐观锁版本号（防止并发超卖）
ALTER TABLE tb_inventory        ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

-- 申请单所属仓库（审批、出库、部门隔离都依赖）
ALTER TABLE tb_apply            ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT '申请仓库ID' AFTER apply_no,
                                ADD KEY idx_warehouse_id (warehouse_id);

-- 库存流水操作人允许为空：锁定/解锁库存、定时任务自动取消等系统操作没有操作人，
-- 原来的 NOT NULL 约束导致审批、直接出库、取消出库时写流水报错
ALTER TABLE tb_inventory_log    MODIFY COLUMN operator_id BIGINT DEFAULT NULL COMMENT '操作人ID(定时任务等系统操作为空)',
                                MODIFY COLUMN operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名(冗余)';

-- 备注、排序
ALTER TABLE tb_user             ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE tb_dept             ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE tb_role             ADD COLUMN sort INT NOT NULL DEFAULT 0 COMMENT '排序', ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注';

-- 回填已有申请单的仓库：部门只有一个仓库时直接对应；部门有多个仓库的申请需人工确认
UPDATE tb_apply a
JOIN (SELECT dept_id, MIN(id) AS warehouse_id FROM tb_warehouse WHERE deleted = 0 GROUP BY dept_id HAVING COUNT(*) = 1) w
  ON a.dept_id = w.dept_id
SET a.warehouse_id = w.warehouse_id
WHERE a.warehouse_id IS NULL;

-- 仍未能回填仓库的申请单（需人工处理）
SELECT id, apply_no, dept_id FROM tb_apply WHERE warehouse_id IS NULL;
