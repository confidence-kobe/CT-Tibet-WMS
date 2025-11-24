-- H2 Test Database Schema
-- CT-Tibet-WMS (西藏电信仓库管理系统)
-- Created for integration testing

SET MODE MySQL;

-- ============================
-- 1. tb_role - 角色表
-- ============================
CREATE TABLE IF NOT EXISTS tb_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码（唯一标识）',
    role_level INT COMMENT '角色级别（0-系统管理员 1-部门管理员 2-仓库管理员 3-普通员工）',
    description VARCHAR(500) COMMENT '描述',
    status INT DEFAULT 0 COMMENT '状态（0-启用 1-禁用）',
    sort INT COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '角色表';

-- ============================
-- 2. tb_dept - 部门表
-- ============================
CREATE TABLE IF NOT EXISTS tb_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(64) COMMENT '部门编码（用于单号生成）',
    parent_id BIGINT COMMENT '上级部门ID（0表示顶级部门）',
    ancestors VARCHAR(500) COMMENT '祖级列表（1,2,3,路径）',
    leader_id BIGINT COMMENT '部门负责人ID',
    phone VARCHAR(11) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status INT DEFAULT 0 COMMENT '状态（0-启用 1-禁用）',
    sort INT COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '部门表';

-- ============================
-- 3. tb_user - 用户表
-- ============================
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名（登录名，全局唯一）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(11) UNIQUE COMMENT '手机号（全局唯一）',
    email VARCHAR(100) COMMENT '邮箱',
    dept_id BIGINT COMMENT '部门ID',
    role_id BIGINT COMMENT '角色ID',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    avatar VARCHAR(500) COMMENT '头像URL',
    wechat_openid VARCHAR(100) COMMENT '微信openid（小程序登录）',
    wechat_nickname VARCHAR(64) COMMENT '微信昵称',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    password_update_time TIMESTAMP COMMENT '密码最后修改时间',
    is_first_login INT DEFAULT 1 COMMENT '是否首次登录（0-否 1-是）',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '用户表';

-- ============================
-- 4. tb_material - 物资表
-- ============================
CREATE TABLE IF NOT EXISTS tb_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_name VARCHAR(255) NOT NULL COMMENT '物资名称',
    material_code VARCHAR(100) NOT NULL UNIQUE COMMENT '物资编码（唯一）',
    category VARCHAR(100) COMMENT '物资类别',
    spec VARCHAR(255) COMMENT '规格型号',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10, 2) DEFAULT 0 COMMENT '单价',
    min_stock DECIMAL(10, 2) COMMENT '最低库存预警阈值',
    description VARCHAR(500) COMMENT '描述',
    image VARCHAR(500) COMMENT '图片URL',
    status INT DEFAULT 0 COMMENT '状态（0-启用 1-停用）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '物资表';

-- ============================
-- 5. tb_warehouse - 仓库表
-- ============================
CREATE TABLE IF NOT EXISTS tb_warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_name VARCHAR(255) NOT NULL COMMENT '仓库名称',
    warehouse_code VARCHAR(100) COMMENT '仓库编码',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    address VARCHAR(255) COMMENT '仓库地址',
    manager_id BIGINT COMMENT '仓库管理员ID',
    capacity DECIMAL(10, 2) COMMENT '仓库容量（平方米）',
    status INT DEFAULT 0 COMMENT '状态（0-启用 1-禁用）',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '仓库表';

CREATE INDEX IF NOT EXISTS idx_warehouse_dept_id ON tb_warehouse(dept_id);

-- ============================
-- 6. tb_inventory - 库存表
-- ============================
CREATE TABLE IF NOT EXISTS tb_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    material_id BIGINT NOT NULL COMMENT '物资ID',
    quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '当前库存数量',
    locked_quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '锁定数量（预留功能）',
    available_quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '可用数量（quantity - lockedQuantity）',
    last_inbound_time TIMESTAMP COMMENT '最后入库时间',
    last_outbound_time TIMESTAMP COMMENT '最后出库时间',
    version INT DEFAULT 0 COMMENT '乐观锁版本号（用于并发控制）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）',
    UNIQUE KEY uk_warehouse_material (warehouse_id, material_id)
) COMMENT '库存表';

CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_id ON tb_inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_material_id ON tb_inventory(material_id);

-- ============================
-- 7. tb_inbound - 入库单表
-- ============================
CREATE TABLE IF NOT EXISTS tb_inbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inbound_no VARCHAR(100) NOT NULL UNIQUE COMMENT '入库单号（格式：RK_部门编码_YYYYMMDD_流水号）',
    warehouse_id BIGINT NOT NULL COMMENT '入库仓库ID',
    inbound_type VARCHAR(20) COMMENT '入库类型（PURCHASE-采购入库 RETURN-退货入库 TRANSFER-调拨入库 OTHER-其他）',
    operator_id BIGINT COMMENT '操作人ID（仓管员）',
    operator_name VARCHAR(100) COMMENT '操作人姓名（冗余字段）',
    inbound_time TIMESTAMP COMMENT '入库时间',
    total_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '入库总金额',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '入库单表';

CREATE INDEX IF NOT EXISTS idx_inbound_warehouse_id ON tb_inbound(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inbound_operator_id ON tb_inbound(operator_id);

-- ============================
-- 8. tb_inbound_detail - 入库明细表
-- ============================
CREATE TABLE IF NOT EXISTS tb_inbound_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inbound_id BIGINT NOT NULL COMMENT '入库单ID',
    material_id BIGINT NOT NULL COMMENT '物资ID',
    material_name VARCHAR(255) COMMENT '物资名称（冗余字段）',
    material_code VARCHAR(100) COMMENT '物资编码（冗余字段）',
    spec VARCHAR(255) COMMENT '规格型号（冗余字段）',
    unit VARCHAR(20) COMMENT '单位（冗余字段）',
    quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '入库数量',
    unit_price DECIMAL(10, 2) DEFAULT 0 COMMENT '单价',
    amount DECIMAL(10, 2) DEFAULT 0 COMMENT '金额（quantity * unitPrice）',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '入库明细表';

CREATE INDEX IF NOT EXISTS idx_inbound_detail_inbound_id ON tb_inbound_detail(inbound_id);
CREATE INDEX IF NOT EXISTS idx_inbound_detail_material_id ON tb_inbound_detail(material_id);

-- ============================
-- 9. tb_apply - 申请单表
-- ============================
CREATE TABLE IF NOT EXISTS tb_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    apply_no VARCHAR(100) NOT NULL UNIQUE COMMENT '申请单号（格式：SQ_部门编码_YYYYMMDD_流水号）',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT NOT NULL COMMENT '申请人部门ID',
    warehouse_id BIGINT NOT NULL COMMENT '目标仓库ID',
    purpose VARCHAR(255) COMMENT '领用用途',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态（PENDING-待审批 APPROVED-已通过 REJECTED-已拒绝 COMPLETED-已出库 CANCELED-已取消）',
    applicant_name VARCHAR(100) COMMENT '申请人姓名（冗余字段）',
    applicant_phone VARCHAR(11) COMMENT '申请人手机号（冗余字段）',
    dept_name VARCHAR(100) COMMENT '部门名称（冗余字段）',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(100) COMMENT '审批人姓名（冗余字段）',
    approval_time TIMESTAMP COMMENT '审批时间',
    approval_opinion VARCHAR(500) COMMENT '审批意见',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    outbound_id BIGINT COMMENT '关联出库单ID（审批通过后自动创建）',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_time TIMESTAMP COMMENT '取消时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '申请单表';

CREATE INDEX IF NOT EXISTS idx_apply_applicant_id ON tb_apply(applicant_id);
CREATE INDEX IF NOT EXISTS idx_apply_dept_id ON tb_apply(dept_id);
CREATE INDEX IF NOT EXISTS idx_apply_approver_id ON tb_apply(approver_id);
CREATE INDEX IF NOT EXISTS idx_apply_status ON tb_apply(status);

-- ============================
-- 10. tb_apply_detail - 申请明细表
-- ============================
CREATE TABLE IF NOT EXISTS tb_apply_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    apply_id BIGINT NOT NULL COMMENT '申请单ID',
    material_id BIGINT NOT NULL COMMENT '物资ID',
    material_name VARCHAR(255) COMMENT '物资名称（冗余字段）',
    material_code VARCHAR(100) COMMENT '物资编码（冗余字段）',
    spec VARCHAR(255) COMMENT '规格型号（冗余字段）',
    unit VARCHAR(20) COMMENT '单位（冗余字段）',
    quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '申请数量',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '申请明细表';

CREATE INDEX IF NOT EXISTS idx_apply_detail_apply_id ON tb_apply_detail(apply_id);
CREATE INDEX IF NOT EXISTS idx_apply_detail_material_id ON tb_apply_detail(material_id);

-- ============================
-- 11. tb_outbound - 出库单表
-- ============================
CREATE TABLE IF NOT EXISTS tb_outbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_no VARCHAR(100) NOT NULL UNIQUE COMMENT '出库单号（格式：CK_部门编码_YYYYMMDD_流水号）',
    warehouse_id BIGINT NOT NULL COMMENT '出库仓库ID',
    outbound_type VARCHAR(20) COMMENT '出库类型（USE-领用出库 SCRAP-报废出库 TRANSFER-调拨出库 OTHER-其他）',
    source VARCHAR(20) COMMENT '出库来源（DIRECT-直接创建 FROM_APPLY-申请自动创建）',
    apply_id BIGINT COMMENT '关联申请单ID（来源为FROM_APPLY时）',
    receiver_id BIGINT COMMENT '领用人ID',
    receiver_name VARCHAR(100) COMMENT '领用人姓名（冗余字段）',
    receiver_phone VARCHAR(11) COMMENT '领用人手机号（冗余字段）',
    purpose VARCHAR(255) COMMENT '领用用途',
    operator_id BIGINT COMMENT '操作人ID（仓管员）',
    operator_name VARCHAR(100) COMMENT '操作人姓名（冗余字段）',
    outbound_time TIMESTAMP COMMENT '出库时间（确认领取时间）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态（PENDING-待领取 COMPLETED-已出库 CANCELED-已取消）',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    remark VARCHAR(500) COMMENT '备注',
    total_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '总金额',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '出库单表';

CREATE INDEX IF NOT EXISTS idx_outbound_warehouse_id ON tb_outbound(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_outbound_receiver_id ON tb_outbound(receiver_id);
CREATE INDEX IF NOT EXISTS idx_outbound_operator_id ON tb_outbound(operator_id);
CREATE INDEX IF NOT EXISTS idx_outbound_apply_id ON tb_outbound(apply_id);
CREATE INDEX IF NOT EXISTS idx_outbound_status ON tb_outbound(status);

-- ============================
-- 12. tb_outbound_detail - 出库明细表
-- ============================
CREATE TABLE IF NOT EXISTS tb_outbound_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_id BIGINT NOT NULL COMMENT '出库单ID',
    material_id BIGINT NOT NULL COMMENT '物资ID',
    material_name VARCHAR(255) COMMENT '物资名称（冗余字段）',
    material_code VARCHAR(100) COMMENT '物资编码（冗余字段）',
    spec VARCHAR(255) COMMENT '规格型号（冗余字段）',
    unit VARCHAR(20) COMMENT '单位（冗余字段）',
    quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '出库数量',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '出库明细表';

CREATE INDEX IF NOT EXISTS idx_outbound_detail_outbound_id ON tb_outbound_detail(outbound_id);
CREATE INDEX IF NOT EXISTS idx_outbound_detail_material_id ON tb_outbound_detail(material_id);

-- ============================
-- 13. tb_message - 消息表
-- ============================
CREATE TABLE IF NOT EXISTS tb_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收人ID',
    title VARCHAR(255) COMMENT '消息标题',
    content VARCHAR(1000) COMMENT '消息内容',
    type VARCHAR(20) COMMENT '消息类型（SYSTEM-系统通知 APPLY-申请通知 APPROVAL-审批通知 ALERT-库存预警）',
    related_id BIGINT COMMENT '关联业务ID（申请单ID、出库单ID等）',
    related_type INT COMMENT '关联业务类型（1-申请单 2-出库单）',
    is_read INT DEFAULT 0 COMMENT '是否已读（0-未读 1-已读）',
    read_time TIMESTAMP COMMENT '阅读时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '消息表';

CREATE INDEX IF NOT EXISTS idx_message_user_id ON tb_message(user_id);
CREATE INDEX IF NOT EXISTS idx_message_is_read ON tb_message(is_read);

-- ============================
-- 14. tb_inventory_log - 库存日志表（可选）
-- ============================
CREATE TABLE IF NOT EXISTS tb_inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    material_id BIGINT NOT NULL COMMENT '物资ID',
    before_quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '变动前数量',
    change_quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '变动数量',
    after_quantity DECIMAL(10, 2) DEFAULT 0 COMMENT '变动后数量',
    operation_type VARCHAR(50) COMMENT '操作类型（INBOUND-入库 OUTBOUND-出库 ADJUST-调整）',
    related_id BIGINT COMMENT '关联单据ID',
    related_type VARCHAR(50) COMMENT '关联单据类型（INBOUND-入库单 OUTBOUND-出库单）',
    reason VARCHAR(500) COMMENT '操作原因',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）'
) COMMENT '库存日志表';

CREATE INDEX IF NOT EXISTS idx_inventory_log_warehouse_material ON tb_inventory_log(warehouse_id, material_id);
CREATE INDEX IF NOT EXISTS idx_inventory_log_related ON tb_inventory_log(related_id, related_type);
