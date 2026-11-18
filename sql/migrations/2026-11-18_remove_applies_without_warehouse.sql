-- =============================================
-- 清理没有仓库的申请单（2026-11-18）
--
-- 原因：2026-11-12 的迁移给 tb_apply 新增了 warehouse_id 列，但升级前已存在的申请单该列为空。
-- 这类申请单（如示例数据 SQ_WL_20251105_0001）任何管理员和仓管都无权查看或审批，只会挂在员工的"待审批"里。
-- 处理方式：逻辑删除（deleted = 1），不物理删除，需要时可以恢复。
-- 只影响 warehouse_id 为空的申请单；全新安装的数据库没有这类数据，执行无副作用，可重复执行。
--
-- 执行：mysql -uroot -p ct_tibet_wms < sql/migrations/2026-11-18_remove_applies_without_warehouse.sql
-- =============================================
SET NAMES utf8mb4;

UPDATE tb_apply_detail d
JOIN tb_apply a ON a.id = d.apply_id
SET d.deleted = 1
WHERE a.warehouse_id IS NULL AND a.deleted = 0;

UPDATE tb_apply
SET deleted = 1
WHERE warehouse_id IS NULL AND deleted = 0;

SELECT CONCAT('剩余无仓库申请单: ', COUNT(*)) AS result FROM tb_apply WHERE warehouse_id IS NULL AND deleted = 0;
