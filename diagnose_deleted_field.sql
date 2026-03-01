-- =============================================
-- 诊断脚本：检查逻辑删除字段是否正确配置
-- 此脚本用于判断当前数据库状态和问题所在
-- =============================================

USE ct_tibet_wms;

-- 诊断1：检查是否存在 is_deleted 列（表示问题仍存在）
SELECT
  '诊断1: 查找 is_deleted 列（问题指示器）' AS diagnostic,
  TABLE_NAME,
  COLUMN_NAME,
  ORDINAL_POSITION,
  COLUMN_TYPE,
  COLUMN_DEFAULT,
  IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
  AND COLUMN_NAME = 'is_deleted'
ORDER BY TABLE_NAME;

-- 诊断2：检查是否已修复 deleted 列
SELECT
  '诊断2: 查找 deleted 列（修复指示器）' AS diagnostic,
  TABLE_NAME,
  COLUMN_NAME,
  ORDINAL_POSITION,
  COLUMN_TYPE,
  COLUMN_DEFAULT,
  IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
  AND COLUMN_NAME = 'deleted'
ORDER BY TABLE_NAME;

-- 诊断3：统计概览
SELECT
  '诊断3: 统计' AS diagnostic,
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'is_deleted') AS remaining_is_deleted_columns,
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'deleted') AS fixed_deleted_columns;

-- 诊断4：检查每张表的删除字段状态
SELECT
  '诊断4: 每张表的删除字段情况' AS diagnostic,
  t.TABLE_NAME,
  CASE
    WHEN COLUMN_NAME = 'deleted' THEN '已修复'
    WHEN COLUMN_NAME = 'is_deleted' THEN '需要修复'
    ELSE '无删除字段'
  END AS status,
  COLUMN_NAME AS current_column_name
FROM (
  SELECT DISTINCT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
  WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND TABLE_TYPE = 'BASE TABLE'
) t
LEFT JOIN INFORMATION_SCHEMA.COLUMNS c
  ON t.TABLE_NAME = c.TABLE_NAME
  AND TABLE_SCHEMA = 'ct_tibet_wms'
  AND COLUMN_NAME IN ('deleted', 'is_deleted')
WHERE t.TABLE_NAME IN ('tb_user', 'tb_role', 'tb_dept', 'tb_menu', 'tb_material', 'tb_warehouse')
ORDER BY t.TABLE_NAME;

-- 诊断5：测试查询（模拟登录API的查询）
-- 这个查询会成功则表示已修复，失败则表示还需修复
SELECT 'SELECT TEST: 若此行显示，表示 deleted 列可用' AS test_result,
       id, dept_name, deleted
FROM tb_dept
WHERE deleted = 0
LIMIT 1;

-- 诊断结论信息
SELECT '
========== 诊断结论 ==========

修复步骤检查清单：
[ ] 诊断1结果为空 - is_deleted已全部改名
[ ] 诊断2结果>=5行 - deleted列已正确创建
[ ] 诊断3: remaining_is_deleted_columns=0, fixed_deleted_columns>=5
[ ] 诊断4: 所有主要表的status均为已修复
[ ] 诊断5: SELECT TEST 成功执行（不报错）

如果以上全部满足，则数据库修复完毕，需要重启后端应用。
' AS fix_summary;
