-- =============================================
-- 西藏电信仓库管理系统 - 初始化数据脚本
-- 版本: v1.0
-- 创建日期: 2025-11-11
-- =============================================

-- =============================================
-- 1. 初始化部门数据
-- =============================================

INSERT INTO `tb_dept` (`id`, `dept_name`, `dept_code`, `parent_id`, `ancestors`, `sort`, `status`) VALUES
(1, '西藏电信', 'ROOT', 0, '0', 0, 0),
(2, '网络运维部', 'WL', 1, '0,1', 1, 0),
(3, '市场营销部', 'SC', 1, '0,1', 2, 0),
(4, '客户服务部', 'KF', 1, '0,1', 3, 0),
(5, '财务部', 'CW', 1, '0,1', 4, 0),
(6, '人力资源部', 'RZ', 1, '0,1', 5, 0),
(7, '技术支持部', 'JS', 1, '0,1', 6, 0),
(8, '综合管理部', 'ZH', 1, '0,1', 7, 0);

-- =============================================
-- 2. 初始化角色数据
-- =============================================

INSERT INTO `tb_role` (`id`, `role_name`, `role_code`, `role_level`, `description`, `status`) VALUES
(1, '系统管理员', 'admin', 0, '系统最高权限，管理所有功能', 0),
(2, '部门管理员', 'dept_admin', 1, '管理本部门用户、仓库、出入库、审批', 0),
(3, '仓库管理员', 'warehouse', 2, '管理本部门出入库、审批员工申请', 0),
(4, '普通员工', 'user', 3, '申请出库、查看库存', 0);

-- =============================================
-- 3. 初始化用户数据
-- =============================================

-- 密码均为: 123456 (BCrypt加密后的值)
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH

INSERT INTO `tb_user` (`id`, `username`, `password`, `real_name`, `phone`, `dept_id`, `role_id`, `status`, `is_first_login`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800000000', 1, 1, 0, 0),
(2, 'wl_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '网运部管理员', '13800000001', 2, 2, 0, 1),
(3, 'wl_warehouse', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李军', '13800000002', 2, 3, 0, 1),
(4, 'wl_user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张强', '13800000003', 2, 4, 0, 1),
(5, 'wl_user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王小明', '13800000004', 2, 4, 0, 1);

-- =============================================
-- 4. 初始化物资数据
-- =============================================

INSERT INTO `tb_material` (`id`, `material_name`, `material_code`, `category`, `spec`, `unit`, `price`, `min_stock`, `status`) VALUES
-- 光缆类
(1, '光缆12芯', 'GX001', '光缆类', '12芯单模', '条', 1500.00, 100.00, 0),
(2, '光缆24芯', 'GX002', '光缆类', '24芯单模', '条', 2800.00, 50.00, 0),
(3, '光缆48芯', 'GX003', '光缆类', '48芯单模', '条', 5000.00, 30.00, 0),
(4, '光缆72芯', 'GX004', '光缆类', '72芯单模', '条', 7200.00, 20.00, 0),

-- 设备类
(5, '华为交换机S5720', 'SB001', '设备类', 'S5720-28X-SI', '台', 8500.00, 10.00, 0),
(6, 'H3C交换机', 'SB002', '设备类', 'S5130-28S-EI', '台', 7800.00, 10.00, 0),
(7, '华为路由器AR2220', 'SB003', '设备类', 'AR2220E', '台', 12000.00, 5.00, 0),
(8, 'OLT设备', 'SB004', '设备类', 'MA5800-X7', '台', 45000.00, 3.00, 0),
(9, 'ONU设备', 'SB005', '设备类', 'HG8310M', '台', 180.00, 50.00, 0),

-- 配件类
(10, '超五类网线', 'PJ001', '配件类', 'Cat5e UTP', '米', 2.50, 500.00, 0),
(11, '六类网线', 'PJ002', '配件类', 'Cat6 UTP', '米', 3.50, 500.00, 0),
(12, '光纤跳线SC-SC', 'PJ003', '配件类', 'SC-SC单模3米', '条', 15.00, 200.00, 0),
(13, '光纤跳线LC-LC', 'PJ004', '配件类', 'LC-LC单模3米', '条', 12.00, 200.00, 0),
(14, '配线架24口', 'PJ005', '配件类', '24口CAT6', '个', 280.00, 20.00, 0),
(15, '机柜', 'PJ006', '配件类', '42U网络机柜', '个', 1800.00, 5.00, 0),
(16, '光纤熔接盘', 'PJ007', '配件类', '12芯熔接盘', '个', 35.00, 50.00, 0),

-- 工具类
(17, '光纤熔接机', 'GJ001', '工具类', '住友T-600C', '台', 28000.00, 2.00, 0),
(18, 'OTDR测试仪', 'GJ002', '工具类', 'JDSU MTS-2000', '台', 85000.00, 1.00, 0),
(19, '网线钳', 'GJ003', '工具类', 'RJ45压线钳', '把', 45.00, 20.00, 0),
(20, '光功率计', 'GJ004', '工具类', '手持式光功率计', '台', 1200.00, 5.00, 0);

-- =============================================
-- 5. 初始化仓库数据
-- =============================================

INSERT INTO `tb_warehouse` (`id`, `warehouse_name`, `warehouse_code`, `dept_id`, `address`, `manager_id`, `capacity`, `status`) VALUES
(1, '网络运维部仓库', 'WH_WL_001', 2, '西藏拉萨市城关区江苏路1号1楼', 3, 120.00, 0),
(2, '市场营销部仓库', 'WH_SC_001', 3, '西藏拉萨市城关区江苏路1号2楼', NULL, 60.00, 0),
(3, '客户服务部仓库', 'WH_KF_001', 4, '西藏拉萨市城关区江苏路1号3楼', NULL, 50.00, 0),
(4, '财务部仓库', 'WH_CW_001', 5, '西藏拉萨市城关区江苏路1号4楼', NULL, 30.00, 0),
(5, '人力资源部仓库', 'WH_RZ_001', 6, '西藏拉萨市城关区江苏路1号5楼', NULL, 30.00, 0),
(6, '技术支持部仓库', 'WH_JS_001', 7, '西藏拉萨市城关区江苏路1号6楼', NULL, 80.00, 0),
(7, '综合管理部仓库', 'WH_ZH_001', 8, '西藏拉萨市城关区江苏路1号7楼', NULL, 40.00, 0);

-- =============================================
-- 6. 初始化库存数据（网络运维部仓库）
-- =============================================

INSERT INTO `tb_inventory` (`warehouse_id`, `material_id`, `quantity`, `locked_quantity`, `available_quantity`) VALUES
-- 光缆类
(1, 1, 850.00, 0.00, 850.00),
(1, 2, 320.00, 0.00, 320.00),
(1, 3, 150.00, 0.00, 150.00),
(1, 4, 80.00, 0.00, 80.00),

-- 设备类
(1, 5, 23.00, 0.00, 23.00),
(1, 6, 18.00, 0.00, 18.00),
(1, 7, 12.00, 0.00, 12.00),
(1, 8, 5.00, 0.00, 5.00),
(1, 9, 156.00, 0.00, 156.00),

-- 配件类
(1, 10, 1850.00, 0.00, 1850.00),
(1, 11, 1230.00, 0.00, 1230.00),
(1, 12, 480.00, 0.00, 480.00),
(1, 13, 520.00, 0.00, 520.00),
(1, 14, 45.00, 0.00, 45.00),
(1, 15, 12.00, 0.00, 12.00),
(1, 16, 180.00, 0.00, 180.00),

-- 工具类
(1, 17, 3.00, 0.00, 3.00),
(1, 18, 2.00, 0.00, 2.00),
(1, 19, 35.00, 0.00, 35.00),
(1, 20, 8.00, 0.00, 8.00);

-- =============================================
-- 7. 初始化菜单数据（基础菜单）
-- =============================================

INSERT INTO `tb_menu` (`id`, `menu_name`, `menu_code`, `parent_id`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`) VALUES
-- 一级菜单
(1, '系统管理', 'system', 0, 1, '/system', 'system', 1, 1, 0),
(2, '基础数据', 'basic', 0, 1, '/basic', 'setting', 2, 1, 0),
(3, '入库管理', 'inbound', 0, 1, '/inbound', 'upload', 3, 1, 0),
(4, '出库管理', 'outbound', 0, 1, '/outbound', 'download', 4, 1, 0),
(5, '申请审批', 'apply', 0, 1, '/apply', 'audit', 5, 1, 0),
(6, '库存管理', 'inventory', 0, 1, '/inventory', 'stock', 6, 1, 0),
(7, '统计报表', 'report', 0, 1, '/report', 'chart', 7, 1, 0),

-- 系统管理子菜单
(101, '用户管理', 'user_manage', 1, 2, '/system/user', 'user', 1, 1, 0),
(102, '角色管理', 'role_manage', 1, 2, '/system/role', 'role', 2, 1, 0),
(103, '部门管理', 'dept_manage', 1, 2, '/system/dept', 'dept', 3, 1, 0),
(104, '菜单管理', 'menu_manage', 1, 2, '/system/menu', 'menu', 4, 1, 0),

-- 基础数据子菜单
(201, '物资管理', 'material_manage', 2, 2, '/basic/material', 'material', 1, 1, 0),
(202, '仓库管理', 'warehouse_manage', 2, 2, '/basic/warehouse', 'warehouse', 2, 1, 0),

-- 入库管理子菜单
(301, '入库单', 'inbound_order', 3, 2, '/inbound/order', 'order', 1, 1, 0),

-- 出库管理子菜单
(401, '出库单', 'outbound_order', 4, 2, '/outbound/order', 'order', 1, 1, 0),
(402, '审批管理', 'approval_manage', 4, 2, '/outbound/approval', 'audit', 2, 1, 0),

-- 申请审批子菜单
(501, '我的申请', 'my_apply', 5, 2, '/apply/my', 'list', 1, 1, 0),
(502, '新建申请', 'new_apply', 5, 2, '/apply/new', 'add', 2, 1, 0),

-- 库存管理子菜单
(601, '库存查询', 'inventory_query', 6, 2, '/inventory/query', 'search', 1, 1, 0),
(602, '库存预警', 'inventory_alert', 6, 2, '/inventory/alert', 'warning', 2, 1, 0),

-- 统计报表子菜单
(701, '出入库统计', 'inout_stats', 7, 2, '/report/inout', 'chart', 1, 1, 0),
(702, '物资统计', 'material_stats', 7, 2, '/report/material', 'chart', 2, 1, 0),
(703, '审批统计', 'approval_stats', 7, 2, '/report/approval', 'chart', 3, 1, 0);

-- =============================================
-- 8. 初始化角色菜单关联
-- =============================================

-- 系统管理员：全部菜单
INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM tb_menu;

-- 部门管理员：除系统管理外的所有菜单
INSERT INTO `tb_role_menu` (`role_id`, `menu_id`)
SELECT 2, id FROM tb_menu WHERE parent_id != 1 AND id != 1;

-- 仓库管理员：基础数据、入库、出库、审批、库存、报表
INSERT INTO `tb_role_menu` (`role_id`, `menu_id`) VALUES
(3, 2), (3, 3), (3, 4), (3, 6), (3, 7),
(3, 201), (3, 202),
(3, 301),
(3, 401), (3, 402),
(3, 601), (3, 602),
(3, 701), (3, 702), (3, 703);

-- 普通员工：申请审批、库存查询
INSERT INTO `tb_role_menu` (`role_id`, `menu_id`) VALUES
(4, 5), (4, 6),
(4, 501), (4, 502),
(4, 601);

-- =============================================
-- 9. 初始化示例数据（可选，测试用）
-- =============================================

-- 示例入库单
INSERT INTO `tb_inbound` (`inbound_no`, `warehouse_id`, `inbound_type`, `operator_id`, `operator_name`, `inbound_time`, `total_amount`, `status`) VALUES
('RK_WL_20251101_0001', 1, 1, 3, '李军', '2025-11-01 09:30:00', 15000.00, 1);

-- 示例入库明细
INSERT INTO `tb_inbound_detail` (`inbound_id`, `material_id`, `material_name`, `material_code`, `spec`, `unit`, `quantity`, `unit_price`, `amount`) VALUES
(1, 1, '光缆12芯', 'GX001', '12芯单模', '条', 100.00, 1500.00, 150000.00);

-- 示例申请单
INSERT INTO `tb_apply` (`apply_no`, `applicant_id`, `applicant_name`, `applicant_phone`, `dept_id`, `dept_name`, `purpose`, `apply_time`, `status`) VALUES
('SQ_WL_20251105_0001', 4, '张强', '13800000003', 2, '网络运维部', 'XX小区光缆施工', '2025-11-05 10:00:00', 0);

-- 示例申请明细
INSERT INTO `tb_apply_detail` (`apply_id`, `material_id`, `material_name`, `material_code`, `spec`, `unit`, `quantity`) VALUES
(1, 1, '光缆12芯', 'GX001', '12芯单模', '条', 10.00),
(1, 6, 'H3C交换机', 'SB002', 'S5130-28S-EI', '台', 2.00);

-- =============================================
-- 初始化完成
-- =============================================

SELECT '初始化数据完成！' AS message;
SELECT CONCAT('部门数量: ', COUNT(*)) AS dept_count FROM tb_dept;
SELECT CONCAT('角色数量: ', COUNT(*)) AS role_count FROM tb_role;
SELECT CONCAT('用户数量: ', COUNT(*)) AS user_count FROM tb_user;
SELECT CONCAT('物资数量: ', COUNT(*)) AS material_count FROM tb_material;
SELECT CONCAT('仓库数量: ', COUNT(*)) AS warehouse_count FROM tb_warehouse;
SELECT CONCAT('库存记录数: ', COUNT(*)) AS inventory_count FROM tb_inventory;
