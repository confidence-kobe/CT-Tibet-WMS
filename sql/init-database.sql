-- Production Database Initialization Script for CT-Tibet-WMS
-- MySQL 8.0 Compatible
-- Created: 2025-11-24
-- Description: Creates database schema and initializes tables for the warehouse management system

-- ============================================================================
-- DATABASE CREATION
-- ============================================================================
-- Create database with UTF-8 support
CREATE DATABASE IF NOT EXISTS ct_tibet_wms
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ct_tibet_wms;

-- ============================================================================
-- USERS AND ROLES TABLES
-- ============================================================================

-- Create roles table
CREATE TABLE IF NOT EXISTS tb_role (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(32) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(32) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_role_code (role_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- Create departments table
CREATE TABLE IF NOT EXISTS tb_dept (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(32) NOT NULL UNIQUE COMMENT '部门编码',
    parent_id INT COMMENT '上级部门ID',
    dept_level INT COMMENT '部门级别',
    description VARCHAR(255) COMMENT '部门描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_code (dept_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- Create users table
CREATE TABLE IF NOT EXISTS tb_user (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(bcrypt加密)',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    role_id INT NOT NULL COMMENT '角色ID',
    dept_id INT NOT NULL COMMENT '部门ID',
    status TINYINT(1) DEFAULT 1 COMMENT '用户状态(1:正常,0:禁用)',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (role_id) REFERENCES tb_role(id),
    FOREIGN KEY (dept_id) REFERENCES tb_dept(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- MATERIAL AND WAREHOUSE TABLES
-- ============================================================================

-- Create materials table
CREATE TABLE IF NOT EXISTS tb_material (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '物料ID',
    material_code VARCHAR(64) NOT NULL UNIQUE COMMENT '物料编码',
    material_name VARCHAR(128) NOT NULL COMMENT '物料名称',
    material_type VARCHAR(32) COMMENT '物料类型',
    description VARCHAR(255) COMMENT '物料描述',
    unit VARCHAR(32) COMMENT '计量单位',
    price DECIMAL(10, 2) COMMENT '单价',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_material_code (material_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料表';

-- Create warehouses table
CREATE TABLE IF NOT EXISTS tb_warehouse (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '仓库ID',
    warehouse_code VARCHAR(64) NOT NULL UNIQUE COMMENT '仓库编码',
    warehouse_name VARCHAR(128) NOT NULL COMMENT '仓库名称',
    warehouse_type VARCHAR(32) COMMENT '仓库类型',
    dept_id INT NOT NULL COMMENT '部门ID',
    location VARCHAR(255) COMMENT '仓库位置',
    capacity INT COMMENT '仓库容量',
    description VARCHAR(255) COMMENT '仓库描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_warehouse_code (warehouse_code),
    INDEX idx_dept_id (dept_id),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (dept_id) REFERENCES tb_dept(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- ============================================================================
-- INVENTORY TABLES
-- ============================================================================

-- Create inventory table
CREATE TABLE IF NOT EXISTS tb_inventory (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    warehouse_id INT NOT NULL COMMENT '仓库ID',
    material_id INT NOT NULL COMMENT '物料ID',
    stock_qty INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    reserved_qty INT NOT NULL DEFAULT 0 COMMENT '预留数量',
    available_qty INT NOT NULL DEFAULT 0 COMMENT '可用数量',
    warning_qty INT COMMENT '预警数量',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uniq_warehouse_material (warehouse_id, material_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_material_id (material_id),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (warehouse_id) REFERENCES tb_warehouse(id),
    FOREIGN KEY (material_id) REFERENCES tb_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ============================================================================
-- INBOUND TABLES
-- ============================================================================

-- Create inbound orders table
CREATE TABLE IF NOT EXISTS tb_inbound (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '入库单ID',
    inbound_code VARCHAR(64) NOT NULL UNIQUE COMMENT '入库单编号',
    warehouse_id INT NOT NULL COMMENT '仓库ID',
    creator_id INT NOT NULL COMMENT '创建人ID',
    status INT DEFAULT 0 COMMENT '入库状态(0:待审核,1:已完成,2:已取消)',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_inbound_code (inbound_code),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (warehouse_id) REFERENCES tb_warehouse(id),
    FOREIGN KEY (creator_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- Create inbound detail table
CREATE TABLE IF NOT EXISTS tb_inbound_detail (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '入库明细ID',
    inbound_id INT NOT NULL COMMENT '入库单ID',
    material_id INT NOT NULL COMMENT '物料ID',
    qty INT NOT NULL COMMENT '数量',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_inbound_id (inbound_id),
    INDEX idx_material_id (material_id),
    FOREIGN KEY (inbound_id) REFERENCES tb_inbound(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES tb_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库明细表';

-- ============================================================================
-- APPLICATION AND APPROVAL TABLES
-- ============================================================================

-- Create material applications table
CREATE TABLE IF NOT EXISTS tb_apply (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '申请单ID',
    apply_code VARCHAR(64) NOT NULL UNIQUE COMMENT '申请单编号',
    applicant_id INT NOT NULL COMMENT '申请人ID',
    warehouse_id INT NOT NULL COMMENT '仓库ID',
    status INT DEFAULT 0 COMMENT '申请状态(0:待审批,1:已批准,2:已拒绝,3:已完成,4:已取消)',
    approver_id INT COMMENT '审批人ID',
    approval_time TIMESTAMP COMMENT '审批时间',
    approval_remark VARCHAR(255) COMMENT '审批备注',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    pickup_deadline TIMESTAMP COMMENT '取货截止时间(7天后)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_apply_code (apply_code),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (applicant_id) REFERENCES tb_user(id),
    FOREIGN KEY (warehouse_id) REFERENCES tb_warehouse(id),
    FOREIGN KEY (approver_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料申请单表';

-- Create application detail table
CREATE TABLE IF NOT EXISTS tb_apply_detail (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '申请明细ID',
    apply_id INT NOT NULL COMMENT '申请单ID',
    material_id INT NOT NULL COMMENT '物料ID',
    qty INT NOT NULL COMMENT '申请数量',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_apply_id (apply_id),
    INDEX idx_material_id (material_id),
    FOREIGN KEY (apply_id) REFERENCES tb_apply(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES tb_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请明细表';

-- ============================================================================
-- OUTBOUND TABLES
-- ============================================================================

-- Create outbound orders table
CREATE TABLE IF NOT EXISTS tb_outbound (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '出库单ID',
    outbound_code VARCHAR(64) NOT NULL UNIQUE COMMENT '出库单编号',
    warehouse_id INT NOT NULL COMMENT '仓库ID',
    creator_id INT NOT NULL COMMENT '创建人ID',
    source INT COMMENT '出库来源(1:直接出库,2:申请单)',
    apply_id INT COMMENT '关联的申请单ID',
    status INT DEFAULT 0 COMMENT '出库状态(0:待取货,1:已完成,2:已取消)',
    confirmor_id INT COMMENT '确认人ID',
    confirm_time TIMESTAMP COMMENT '确认时间',
    pickup_deadline TIMESTAMP COMMENT '取货截止时间',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_outbound_code (outbound_code),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_apply_id (apply_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (warehouse_id) REFERENCES tb_warehouse(id),
    FOREIGN KEY (creator_id) REFERENCES tb_user(id),
    FOREIGN KEY (apply_id) REFERENCES tb_apply(id),
    FOREIGN KEY (confirmor_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- Create outbound detail table
CREATE TABLE IF NOT EXISTS tb_outbound_detail (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '出库明细ID',
    outbound_id INT NOT NULL COMMENT '出库单ID',
    material_id INT NOT NULL COMMENT '物料ID',
    qty INT NOT NULL COMMENT '数量',
    remark VARCHAR(255) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_outbound_id (outbound_id),
    INDEX idx_material_id (material_id),
    FOREIGN KEY (outbound_id) REFERENCES tb_outbound(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES tb_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库明细表';

-- ============================================================================
-- NOTIFICATION TABLES
-- ============================================================================

-- Create messages table
CREATE TABLE IF NOT EXISTS tb_message (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    sender_id INT COMMENT '发送人ID',
    receiver_id INT NOT NULL COMMENT '接收人ID',
    type INT COMMENT '消息类型(1:申请,2:审批,3:出库,4:系统)',
    title VARCHAR(255) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    related_id INT COMMENT '关联的业务单据ID',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    read_at TIMESTAMP COMMENT '已读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (receiver_id) REFERENCES tb_user(id),
    FOREIGN KEY (sender_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================================================
-- STATISTICS AND LOGGING TABLES
-- ============================================================================

-- Create operation logs table
CREATE TABLE IF NOT EXISTS tb_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id INT COMMENT '操作用户ID',
    operation_type VARCHAR(64) COMMENT '操作类型',
    table_name VARCHAR(64) COMMENT '表名',
    operation_content TEXT COMMENT '操作内容',
    result TINYINT(1) COMMENT '操作结果(0:失败,1:成功)',
    error_msg VARCHAR(255) COMMENT '错误信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================================================

-- Additional indexes for common queries
ALTER TABLE tb_inbound ADD INDEX idx_created_at (created_at);
ALTER TABLE tb_outbound ADD INDEX idx_created_at (created_at);
ALTER TABLE tb_apply ADD INDEX idx_created_at (created_at);
ALTER TABLE tb_apply ADD INDEX idx_pickup_deadline (pickup_deadline);
ALTER TABLE tb_outbound ADD INDEX idx_pickup_deadline (pickup_deadline);

-- ============================================================================
-- INITIAL DATA (Optional - Uncomment and modify as needed)
-- ============================================================================

-- Insert default system administrator role
INSERT INTO tb_role (role_name, role_code, description) VALUES
('系统管理员', 'SYSTEM_ADMIN', 'System administrator with full access'),
('部门管理员', 'DEPT_ADMIN', 'Department administrator'),
('仓库管理员', 'WAREHOUSE_ADMIN', 'Warehouse manager'),
('普通员工', 'USER', 'Regular employee')
ON DUPLICATE KEY UPDATE role_name = role_name;

-- Insert default department
INSERT INTO tb_dept (dept_name, dept_code, dept_level, description) VALUES
('总公司', 'COMPANY_HQ', 1, 'Company headquarters')
ON DUPLICATE KEY UPDATE dept_name = dept_name;

-- Create default system administrator user (password: admin123)
-- Note: In production, use strong passwords and bcrypt hashing
-- Use Spring Security's BCryptPasswordEncoder to generate: new BCryptPasswordEncoder().encode("admin123")
-- Example bcrypt: $2a$10$eImiTXuWVxfaHNYY0iPieOmkdl8d3P.y.bJc/MXu2L9TZ4b1MlIVG
INSERT INTO tb_user (username, password, real_name, email, phone, role_id, dept_id, status)
VALUES ('admin', '$2a$10$eImiTXuWVxfaHNYY0iPieOmkdl8d3P.y.bJc/MXu2L9TZ4b1MlIVG', '系统管理员', 'admin@ct-wms.com', '13900000000', 1, 1, 1)
ON DUPLICATE KEY UPDATE username = username;

-- ============================================================================
-- VIEWS (Optional - for common queries)
-- ============================================================================

-- View for inventory summary
CREATE OR REPLACE VIEW v_inventory_summary AS
SELECT
    i.id,
    w.warehouse_name,
    m.material_name,
    i.stock_qty,
    i.reserved_qty,
    i.available_qty,
    i.warning_qty,
    (i.stock_qty <= i.warning_qty) AS is_warning,
    i.last_updated
FROM tb_inventory i
JOIN tb_warehouse w ON i.warehouse_id = w.id
JOIN tb_material m ON i.material_id = m.id
WHERE i.deleted = 0 AND w.deleted = 0 AND m.deleted = 0;

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================
-- Database initialization completed successfully
-- Next steps:
-- 1. Verify all tables have been created: SHOW TABLES;
-- 2. Update default admin password in tb_user table
-- 3. Add sample data for testing
-- 4. Create database backups
-- 5. Configure access control and user permissions

SELECT 'Database initialization completed successfully' AS status;
