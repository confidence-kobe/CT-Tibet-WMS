-- =============================================
-- 西藏电信仓库管理系统 - 数据库表结构脚本
-- 版本: v1.0
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 创建日期: 2025-11-11
-- =============================================

-- =============================================
-- 1. 系统管理模块
-- =============================================

-- 1.1 用户表
CREATE TABLE `tb_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `dept_id` BIGINT NOT NULL COMMENT '所属部门ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1禁用',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信openid',
  `wechat_nickname` VARCHAR(50) DEFAULT NULL COMMENT '微信昵称',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `password_update_time` DATETIME DEFAULT NULL COMMENT '密码最后修改时间',
  `is_first_login` TINYINT NOT NULL DEFAULT 1 COMMENT '是否首次登录:0否 1是',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表
CREATE TABLE `tb_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_level` TINYINT NOT NULL DEFAULT 0 COMMENT '角色级别:0系统管理员 1部门管理员 2仓库管理员 3普通员工',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1禁用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 部门表
CREATE TABLE `tb_dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `dept_code` VARCHAR(10) NOT NULL COMMENT '部门编码(用于单号生成)',
  `parent_id` BIGINT DEFAULT 0 COMMENT '上级部门ID(0表示根部门)',
  `ancestors` VARCHAR(200) DEFAULT '' COMMENT '祖级列表(1,2,3)',
  `leader_id` BIGINT DEFAULT NULL COMMENT '部门负责人ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1禁用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 1.4 菜单表
CREATE TABLE `tb_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `menu_code` VARCHAR(50) NOT NULL COMMENT '菜单编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID(0表示根菜单)',
  `menu_type` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型:1目录 2菜单 3按钮',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识(如:user:add)',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见:0隐藏 1显示',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1禁用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 1.5 角色菜单关联表
CREATE TABLE `tb_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- =============================================
-- 2. 基础数据模块
-- =============================================

-- 2.1 物资表
CREATE TABLE `tb_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物资ID',
  `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
  `material_code` VARCHAR(50) NOT NULL COMMENT '物资编码(全局唯一)',
  `category` VARCHAR(50) NOT NULL COMMENT '物资类别(光缆类/设备类/配件类/工具类)',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位(条/台/米/个)',
  `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价(元)',
  `min_stock` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低库存预警阈值',
  `description` TEXT DEFAULT NULL COMMENT '物资描述',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '物资图片URL',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1停用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资表';

-- 2.2 仓库表
CREATE TABLE `tb_warehouse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
  `warehouse_code` VARCHAR(50) NOT NULL COMMENT '仓库编码',
  `dept_id` BIGINT NOT NULL COMMENT '所属部门ID',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '仓库地址',
  `manager_id` BIGINT DEFAULT NULL COMMENT '仓库管理员ID',
  `capacity` DECIMAL(10,2) DEFAULT NULL COMMENT '仓库容量(平方米)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0启用 1禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_manager_id` (`manager_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- =============================================
-- 3. 业务数据模块
-- =============================================

-- 3.1 入库单表
CREATE TABLE `tb_inbound` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `inbound_no` VARCHAR(50) NOT NULL COMMENT '入库单号(RK_部门编码_YYYYMMDD_流水号)',
  `warehouse_id` BIGINT NOT NULL COMMENT '入库仓库ID',
  `inbound_type` TINYINT NOT NULL DEFAULT 1 COMMENT '入库类型:1采购入库 2退货入库 3调拨入库 4其他',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID(仓管员)',
  `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名(冗余)',
  `inbound_time` DATETIME NOT NULL COMMENT '入库时间',
  `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '入库总金额',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1已入库',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_inbound_time` (`inbound_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- 3.2 入库明细表
CREATE TABLE `tb_inbound_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `inbound_id` BIGINT NOT NULL COMMENT '入库单ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称(冗余)',
  `material_code` VARCHAR(50) NOT NULL COMMENT '物资编码(冗余)',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号(冗余)',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位(冗余)',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '入库数量',
  `unit_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
  `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额(quantity * unit_price)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_inbound_id` (`inbound_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库明细表';

-- 3.3 出库单表
CREATE TABLE `tb_outbound` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `outbound_no` VARCHAR(50) NOT NULL COMMENT '出库单号(CK_部门编码_YYYYMMDD_流水号)',
  `warehouse_id` BIGINT NOT NULL COMMENT '出库仓库ID',
  `outbound_type` TINYINT NOT NULL DEFAULT 1 COMMENT '出库类型:1领用出库 2报废出库 3调拨出库 4其他',
  `source` TINYINT NOT NULL DEFAULT 1 COMMENT '出库来源:1直接创建 2申请自动创建',
  `apply_id` BIGINT DEFAULT NULL COMMENT '关联申请单ID(来源为2时)',
  `receiver_id` BIGINT DEFAULT NULL COMMENT '领用人ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '领用人姓名',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '领用人手机号(冗余)',
  `purpose` VARCHAR(500) DEFAULT NULL COMMENT '领用用途',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID(仓管员)',
  `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名(冗余)',
  `outbound_time` DATETIME DEFAULT NULL COMMENT '实际出库时间(确认领取时间)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待领取 1已出库 2已取消',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_apply_id` (`apply_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_outbound_time` (`outbound_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status_create` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- 3.4 出库明细表
CREATE TABLE `tb_outbound_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `outbound_id` BIGINT NOT NULL COMMENT '出库单ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称(冗余)',
  `material_code` VARCHAR(50) NOT NULL COMMENT '物资编码(冗余)',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号(冗余)',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位(冗余)',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '出库数量',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_outbound_id` (`outbound_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库明细表';

-- 3.5 申请单表
CREATE TABLE `tb_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请单ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请单号(SQ_部门编码_YYYYMMDD_流水号)',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名(冗余)',
  `applicant_phone` VARCHAR(20) DEFAULT NULL COMMENT '申请人手机号(冗余)',
  `dept_id` BIGINT NOT NULL COMMENT '申请人部门ID',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '申请人部门名称(冗余)',
  `purpose` TEXT NOT NULL COMMENT '领用用途(必填)',
  `apply_time` DATETIME NOT NULL COMMENT '申请时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待审批 1已通过 2已拒绝 3已出库 4已取消',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
  `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名(冗余)',
  `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approval_opinion` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `outbound_id` BIGINT DEFAULT NULL COMMENT '关联出库单ID(审批通过后)',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`),
  KEY `idx_approver_id` (`approver_id`),
  KEY `idx_dept_status` (`dept_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请单表';

-- 3.6 申请明细表
CREATE TABLE `tb_apply_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `apply_id` BIGINT NOT NULL COMMENT '申请单ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称(冗余)',
  `material_code` VARCHAR(50) NOT NULL COMMENT '物资编码(冗余)',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号(冗余)',
  `unit` VARCHAR(20) NOT NULL COMMENT '单位(冗余)',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '申请数量',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_apply_id` (`apply_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请明细表';

-- 3.7 库存表
CREATE TABLE `tb_inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前库存数量',
  `locked_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '锁定数量(待领取的申请)',
  `available_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '可用数量(quantity-locked_quantity)',
  `last_inbound_time` DATETIME DEFAULT NULL COMMENT '最后入库时间',
  `last_outbound_time` DATETIME DEFAULT NULL COMMENT '最后出库时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_material` (`warehouse_id`, `material_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_quantity` (`quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 3.8 库存流水表
CREATE TABLE `tb_inventory_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `material_id` BIGINT NOT NULL COMMENT '物资ID',
  `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称(冗余)',
  `change_type` TINYINT NOT NULL COMMENT '变动类型:1入库 2出库',
  `change_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动数量(正数为增加,负数为减少)',
  `before_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动前数量',
  `after_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动后数量',
  `related_no` VARCHAR(50) NOT NULL COMMENT '关联单号(入库单号/出库单号)',
  `related_type` TINYINT NOT NULL COMMENT '关联类型:1入库单 2出库单',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名(冗余)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_warehouse_material` (`warehouse_id`, `material_id`),
  KEY `idx_related_no` (`related_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- =============================================
-- 4. 消息通知模块
-- =============================================

-- 4.1 消息表
CREATE TABLE `tb_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型:1系统通知 2申请通知 3审批通知 4库存预警',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID(申请单ID/出库单ID等)',
  `related_type` TINYINT DEFAULT NULL COMMENT '关联类型:1申请单 2出库单 3入库单',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读:0未读 1已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 5. 系统日志模块
-- =============================================

-- 5.1 操作日志表
CREATE TABLE `tb_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人账号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `module` VARCHAR(50) NOT NULL COMMENT '操作模块(用户管理/入库管理/出库管理等)',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型(新增/修改/删除/审批)',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式(GET/POST/PUT/DELETE)',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
  `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态:0失败 1成功',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `execute_time` INT DEFAULT NULL COMMENT '执行时长(毫秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 5.2 登录日志表
CREATE TABLE `tb_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `login_type` TINYINT NOT NULL DEFAULT 1 COMMENT '登录类型:1PC端 2小程序',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
  `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录状态:0失败 1成功',
  `message` VARCHAR(200) DEFAULT NULL COMMENT '提示信息',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
