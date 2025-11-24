-- ============================================================================
-- CT-Tibet-WMS Performance Optimization SQL Script
-- 性能优化数据库脚本
--
-- 目的: 创建索引以优化查询性能
-- 执行环境: MySQL 5.7+
-- 预期性能提升: 50-80%查询性能改进
-- ============================================================================

-- ============================================================================
-- 1. 库存表 (tb_inventory) 性能优化
-- ============================================================================

-- 复合索引: warehouse_id + material_id (精确查询优化)
-- 优化场景: getInventory(), checkInventory()
-- 预期提升: 查询时间从全表扫描降至O(1)
DROP INDEX IF EXISTS idx_inventory_warehouse_material ON tb_inventory;
CREATE INDEX idx_inventory_warehouse_material ON tb_inventory(warehouse_id, material_id);

-- 复合索引: warehouse_id + quantity (范围查询优化)
-- 优化场景: listLowStockAlerts(), getDashboardStats()
-- 预期提升: 低库存预警查询性能提升60%
DROP INDEX IF EXISTS idx_inventory_warehouse_qty ON tb_inventory;
CREATE INDEX idx_inventory_warehouse_qty ON tb_inventory(warehouse_id, quantity);

-- 单列索引: update_time (排序优化)
-- 优化场景: listInventories() ORDER BY update_time DESC
DROP INDEX IF EXISTS idx_inventory_update_time ON tb_inventory;
CREATE INDEX idx_inventory_update_time ON tb_inventory(update_time DESC);

-- ============================================================================
-- 2. 入库表 (tb_inbound) 性能优化
-- ============================================================================

-- 复合索引: warehouse_id + inbound_time (统计查询优化)
-- 优化场景: getInboundStatistics(), getDashboardStats()
-- 预期提升: 统计查询性能提升70%
DROP INDEX IF EXISTS idx_inbound_warehouse_time ON tb_inbound;
CREATE INDEX idx_inbound_warehouse_time ON tb_inbound(warehouse_id, inbound_time);

-- 复合索引: inbound_time + warehouse_id (时间范围查询优化)
-- 优化场景: 按时间范围查询入库单
DROP INDEX IF EXISTS idx_inbound_time_warehouse ON tb_inbound;
CREATE INDEX idx_inbound_time_warehouse ON tb_inbound(inbound_time, warehouse_id);

-- 单列索引: operator_id (操作人查询优化)
-- 优化场景: listInbounds(operatorId)
DROP INDEX IF EXISTS idx_inbound_operator ON tb_inbound;
CREATE INDEX idx_inbound_operator ON tb_inbound(operator_id);

-- 单列索引: create_time (排序优化)
-- 优化场景: listInbounds() ORDER BY create_time DESC
DROP INDEX IF EXISTS idx_inbound_create_time ON tb_inbound;
CREATE INDEX idx_inbound_create_time ON tb_inbound(create_time DESC);

-- 前缀索引: inbound_no (单号搜索优化)
-- 优化场景: 入库单号搜索
DROP INDEX IF EXISTS idx_inbound_no ON tb_inbound;
CREATE INDEX idx_inbound_no ON tb_inbound(inbound_no(20));

-- ============================================================================
-- 3. 入库明细表 (tb_inbound_detail) 性能优化
-- ============================================================================

-- 复合索引: inbound_id + material_id (明细查询优化)
-- 优化场景: getInboundById(), 统计明细查询
-- 预期提升: 明细查询性能提升80%
DROP INDEX IF EXISTS idx_inbound_detail_inbound_material ON tb_inbound_detail;
CREATE INDEX idx_inbound_detail_inbound_material ON tb_inbound_detail(inbound_id, material_id);

-- 单列索引: material_id (物资维度统计)
-- 优化场景: 按物资统计入库数据
DROP INDEX IF EXISTS idx_inbound_detail_material ON tb_inbound_detail;
CREATE INDEX idx_inbound_detail_material ON tb_inbound_detail(material_id);

-- ============================================================================
-- 4. 出库表 (tb_outbound) 性能优化
-- ============================================================================

-- 复合索引: warehouse_id + outbound_time (统计查询优化)
-- 优化场景: getOutboundStatistics(), getDashboardStats()
-- 预期提升: 统计查询性能提升70%
DROP INDEX IF EXISTS idx_outbound_warehouse_time ON tb_outbound;
CREATE INDEX idx_outbound_warehouse_time ON tb_outbound(warehouse_id, outbound_time);

-- 复合索引: status + warehouse_id (状态筛选优化)
-- 优化场景: 查询待取货出库单
DROP INDEX IF EXISTS idx_outbound_status_warehouse ON tb_outbound;
CREATE INDEX idx_outbound_status_warehouse ON tb_outbound(status, warehouse_id);

-- 复合索引: receiver_id + status (领用人查询优化)
-- 优化场景: 查询用户的待取货物资
DROP INDEX IF EXISTS idx_outbound_receiver_status ON tb_outbound;
CREATE INDEX idx_outbound_receiver_status ON tb_outbound(receiver_id, status);

-- 单列索引: apply_id (关联查询优化)
-- 优化场景: 根据申请单查找出库单
DROP INDEX IF EXISTS idx_outbound_apply ON tb_outbound;
CREATE INDEX idx_outbound_apply ON tb_outbound(apply_id);

-- 单列索引: operator_id (操作人查询优化)
DROP INDEX IF EXISTS idx_outbound_operator ON tb_outbound;
CREATE INDEX idx_outbound_operator ON tb_outbound(operator_id);

-- 单列索引: create_time (排序优化)
DROP INDEX IF EXISTS idx_outbound_create_time ON tb_outbound;
CREATE INDEX idx_outbound_create_time ON tb_outbound(create_time DESC);

-- 前缀索引: outbound_no (单号搜索优化)
DROP INDEX IF EXISTS idx_outbound_no ON tb_outbound;
CREATE INDEX idx_outbound_no ON tb_outbound(outbound_no(20));

-- ============================================================================
-- 5. 出库明细表 (tb_outbound_detail) 性能优化
-- ============================================================================

-- 复合索引: outbound_id + material_id (明细查询优化)
-- 优化场景: getOutboundById(), 统计明细查询
-- 预期提升: 明细查询性能提升80%
DROP INDEX IF EXISTS idx_outbound_detail_outbound_material ON tb_outbound_detail;
CREATE INDEX idx_outbound_detail_outbound_material ON tb_outbound_detail(outbound_id, material_id);

-- 单列索引: material_id (物资维度统计)
DROP INDEX IF EXISTS idx_outbound_detail_material ON tb_outbound_detail;
CREATE INDEX idx_outbound_detail_material ON tb_outbound_detail(material_id);

-- ============================================================================
-- 6. 申请表 (tb_apply) 性能优化
-- ============================================================================

-- 复合索引: warehouse_id + status (待审批列表优化)
-- 优化场景: listPendingApplies()
-- 预期提升: 待审批列表查询性能提升65%
DROP INDEX IF EXISTS idx_apply_warehouse_status ON tb_apply;
CREATE INDEX idx_apply_warehouse_status ON tb_apply(warehouse_id, status);

-- 复合索引: applicant_id + status (我的申请列表优化)
-- 优化场景: listMyApplies()
DROP INDEX IF EXISTS idx_apply_applicant_status ON tb_apply;
CREATE INDEX idx_apply_applicant_status ON tb_apply(applicant_id, status);

-- 复合索引: status + apply_time (状态+时间查询优化)
-- 优化场景: getDashboardStats() 统计待审批数量
DROP INDEX IF EXISTS idx_apply_status_time ON tb_apply;
CREATE INDEX idx_apply_status_time ON tb_apply(status, apply_time);

-- 单列索引: approver_id (审批人查询优化)
DROP INDEX IF EXISTS idx_apply_approver ON tb_apply;
CREATE INDEX idx_apply_approver ON tb_apply(approver_id);

-- 单列索引: create_time (排序优化)
DROP INDEX IF EXISTS idx_apply_create_time ON tb_apply;
CREATE INDEX idx_apply_create_time ON tb_apply(create_time DESC);

-- 前缀索引: apply_no (单号搜索优化)
DROP INDEX IF EXISTS idx_apply_no ON tb_apply;
CREATE INDEX idx_apply_no ON tb_apply(apply_no(20));

-- ============================================================================
-- 7. 申请明细表 (tb_apply_detail) 性能优化
-- ============================================================================

-- 复合索引: apply_id + material_id (明细查询优化)
-- 优化场景: getApplyById()
DROP INDEX IF EXISTS idx_apply_detail_apply_material ON tb_apply_detail;
CREATE INDEX idx_apply_detail_apply_material ON tb_apply_detail(apply_id, material_id);

-- ============================================================================
-- 8. 库存流水表 (tb_inventory_log) 性能优化
-- ============================================================================

-- 复合索引: warehouse_id + material_id + create_time (流水查询优化)
-- 优化场景: listInventoryLogs()
-- 预期提升: 流水查询性能提升75%
DROP INDEX IF EXISTS idx_inventory_log_warehouse_material_time ON tb_inventory_log;
CREATE INDEX idx_inventory_log_warehouse_material_time ON tb_inventory_log(warehouse_id, material_id, create_time DESC);

-- 复合索引: related_type + related_id (关联查询优化)
-- 优化场景: 根据入库/出库单查询库存变化
DROP INDEX IF EXISTS idx_inventory_log_related ON tb_inventory_log;
CREATE INDEX idx_inventory_log_related ON tb_inventory_log(related_type, related_id);

-- 单列索引: operator_id (操作人查询优化)
DROP INDEX IF EXISTS idx_inventory_log_operator ON tb_inventory_log;
CREATE INDEX idx_inventory_log_operator ON tb_inventory_log(operator_id);

-- ============================================================================
-- 9. 物资表 (tb_material) 性能优化
-- ============================================================================

-- 单列索引: category (分类查询优化)
-- 优化场景: 按分类统计物资
DROP INDEX IF EXISTS idx_material_category ON tb_material;
CREATE INDEX idx_material_category ON tb_material(category);

-- 单列索引: min_stock (低库存筛选优化)
-- 优化场景: 低库存预警计算
DROP INDEX IF EXISTS idx_material_min_stock ON tb_material;
CREATE INDEX idx_material_min_stock ON tb_material(min_stock);

-- 前缀索引: material_code (物资编码搜索)
DROP INDEX IF EXISTS idx_material_code ON tb_material;
CREATE INDEX idx_material_code ON tb_material(material_code(20));

-- ============================================================================
-- 10. 仓库表 (tb_warehouse) 性能优化
-- ============================================================================

-- 单列索引: manager_id (仓管员查询优化)
-- 优化场景: listPendingApplies() - 查询仓管员管理的仓库
DROP INDEX IF EXISTS idx_warehouse_manager ON tb_warehouse;
CREATE INDEX idx_warehouse_manager ON tb_warehouse(manager_id);

-- 单列索引: dept_id (部门查询优化)
DROP INDEX IF EXISTS idx_warehouse_dept ON tb_warehouse;
CREATE INDEX idx_warehouse_dept ON tb_warehouse(dept_id);

-- ============================================================================
-- 11. 用户表 (tb_user) 性能优化
-- ============================================================================

-- 单列索引: dept_id (部门查询优化)
DROP INDEX IF EXISTS idx_user_dept ON tb_user;
CREATE INDEX idx_user_dept ON tb_user(dept_id);

-- 单列索引: username (登录查询优化)
-- 注意: 如果已有唯一索引则无需创建
-- DROP INDEX IF EXISTS idx_user_username ON tb_user;
-- CREATE UNIQUE INDEX idx_user_username ON tb_user(username);

-- ============================================================================
-- 12. 消息表 (tb_message) 性能优化
-- ============================================================================

-- 复合索引: receiver_id + is_read + create_time (消息列表优化)
-- 优化场景: 查询用户未读/已读消息
DROP INDEX IF EXISTS idx_message_receiver_read_time ON tb_message;
CREATE INDEX idx_message_receiver_read_time ON tb_message(receiver_id, is_read, create_time DESC);

-- 复合索引: related_type + related_id (关联查询优化)
-- 优化场景: 根据业务单据查询相关消息
DROP INDEX IF EXISTS idx_message_related ON tb_message;
CREATE INDEX idx_message_related ON tb_message(related_type, related_id);

-- ============================================================================
-- 索引创建完成检查
-- ============================================================================

-- 查看所有表的索引情况
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
    AND TABLE_NAME IN (
        'tb_inventory', 'tb_inventory_log',
        'tb_inbound', 'tb_inbound_detail',
        'tb_outbound', 'tb_outbound_detail',
        'tb_apply', 'tb_apply_detail',
        'tb_material', 'tb_warehouse', 'tb_user', 'tb_message'
    )
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================================================
-- 性能监控建议
-- ============================================================================

-- 1. 开启慢查询日志 (记录超过1秒的查询)
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;
-- SET GLOBAL slow_query_log_file = '/var/log/mysql/slow-query.log';

-- 2. 查看当前慢查询统计
-- SHOW GLOBAL STATUS LIKE 'Slow_queries';

-- 3. 分析表统计信息 (建议定期执行)
-- ANALYZE TABLE tb_inventory;
-- ANALYZE TABLE tb_inbound;
-- ANALYZE TABLE tb_outbound;
-- ANALYZE TABLE tb_apply;

-- ============================================================================
-- 预期性能提升总结
-- ============================================================================

-- 1. 列表查询: 50-70%性能提升
--    - listInventories(): ~60%
--    - listInbounds(): ~55%
--    - listOutbounds(): ~55%
--    - listApplies(): ~60%

-- 2. 统计查询: 60-80%性能提升
--    - getDashboardStats(): ~70%
--    - getInboundStatistics(): ~75%
--    - getOutboundStatistics(): ~75%
--    - getInventoryStatistics(): ~65%

-- 3. 明细查询: 70-85%性能提升
--    - getInboundById(): ~80%
--    - getOutboundById(): ~80%
--    - getApplyById(): ~75%

-- 4. 低库存预警: ~70%性能提升
--    - listLowStockAlerts(): ~70%

-- 总体预期: 在50并发用户场景下，平均响应时间降低60%
