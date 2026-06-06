-- Test data initialization script
-- CT-Tibet-WMS
-- For H2 test database

-- Role data
INSERT INTO tb_role (id, role_name, role_code, role_level, description, status, sort, deleted) VALUES
(1, 'System Admin', 'ADMIN', 0, 'System administrator with all permissions', 0, 1, 0),
(2, 'Dept Admin', 'DEPT_ADMIN', 1, 'Department administrator', 0, 2, 0),
(3, 'Warehouse Manager', 'WAREHOUSE', 2, 'Warehouse manager', 0, 3, 0),
(4, 'Regular User', 'USER', 3, 'Regular employee', 0, 4, 0);

-- Department data
INSERT INTO tb_dept (id, dept_name, dept_code, parent_id, ancestors, leader_id, phone, email, status, sort, deleted) VALUES
(1, 'Headquarters', 'HQ', 0, '0', 1, '13800000000', 'hq@ct-tibet.com', 0, 1, 0),
(2, 'Network Dept', 'WL', 1, '0,1', 3, '13800000001', 'wl@ct-tibet.com', 0, 2, 0),
(3, 'Marketing Dept', 'SC', 1, '0,1', NULL, '13800000002', 'sc@ct-tibet.com', 0, 3, 0);

-- User data (password: 123456, BCrypt encrypted)
-- status: 0=ENABLED, 1=DISABLED (matches UserStatus enum)
INSERT INTO tb_user (id, username, password, real_name, phone, email, dept_id, role_id, status, deleted) VALUES
(1, 'admin', '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e', 'Admin', '13800000010', 'admin@ct-tibet.com', 1, 1, 0, 0),
(2, 'dept_admin', '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e', 'Dept Admin', '13800000011', 'dept@ct-tibet.com', 2, 2, 0, 0),
(3, 'warehouse', '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e', 'Warehouse Mgr', '13800000012', 'warehouse@ct-tibet.com', 2, 3, 0, 0),
(4, 'employee1', '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e', 'Emp Zhang', '13800000013', 'emp1@ct-tibet.com', 2, 4, 0, 0),
(5, 'employee2', '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e', 'Emp Li', '13800000014', 'emp2@ct-tibet.com', 3, 4, 0, 0);

-- Warehouse data
INSERT INTO tb_warehouse (id, warehouse_name, warehouse_code, dept_id, address, manager_id, capacity, status, deleted) VALUES
(1, 'Network Warehouse', 'WH_WL_001', 2, 'Lhasa Chengguan District', 3, 120.00, 0, 0),
(2, 'Marketing Warehouse', 'WH_SC_001', 3, 'Lhasa Beijing Road', NULL, 80.00, 0, 0);

-- Material data
INSERT INTO tb_material (id, material_name, material_code, category, spec, unit, price, min_stock, description, status, deleted) VALUES
(1, 'Cable 12 Core', 'GX001', 'Cable', '12 Core Single Mode', 'pc', 1500.00, 100.00, '12 core single mode cable', 0, 0),
(2, 'Cable 24 Core', 'GX002', 'Cable', '24 Core Single Mode', 'pc', 2800.00, 50.00, '24 core single mode cable', 0, 0),
(3, 'Fiber Patch Cord', 'GJ001', 'Fiber', 'SC-SC 3m', 'pc', 45.00, 200.00, 'SC-SC fiber patch cord 3m', 0, 0),
(4, 'Fiber Pigtail', 'GJ002', 'Fiber', 'SC 1.5m', 'pc', 25.00, 300.00, 'SC fiber pigtail 1.5m', 0, 0),
(5, 'Cable Junction Box', 'XJ001', 'Cable Equipment', '12 Core', 'pc', 850.00, 20.00, '12 core cable junction box', 0, 0),
(6, 'ODF Frame', 'ODF001', 'Cable Equipment', '24 Port', 'pc', 3200.00, 10.00, '24 port ODF fiber distribution frame', 0, 0),
(7, 'Network Crystal Head', 'RJ001', 'Network Parts', 'RJ45', 'pc', 1.50, 1000.00, 'RJ45 network crystal head', 0, 0),
(8, 'Network Cable CAT5E', 'WB001', 'Network Cable', 'CAT5E 305m/box', 'box', 580.00, 30.00, 'Cat5e network cable 305m/box', 0, 0),
(9, 'Switch 24 Port', 'SW001', 'Network Equipment', '24 Port Gigabit', 'pc', 2500.00, 15.00, '24 port gigabit switch', 0, 0),
(10, 'Enterprise Router', 'RT001', 'Network Equipment', 'Enterprise Dual WAN', 'pc', 1800.00, 10.00, 'Enterprise dual WAN router', 0, 0);

-- Inventory data
INSERT INTO tb_inventory (id, warehouse_id, material_id, quantity, locked_quantity, available_quantity, deleted) VALUES
(1, 1, 1, 500.00, 0.00, 500.00, 0),
(2, 1, 2, 200.00, 0.00, 200.00, 0),
(3, 1, 3, 1000.00, 0.00, 1000.00, 0),
(4, 1, 4, 1500.00, 0.00, 1500.00, 0),
(5, 1, 5, 50.00, 0.00, 50.00, 0),
(6, 1, 6, 25.00, 0.00, 25.00, 0),
(7, 1, 7, 5000.00, 0.00, 5000.00, 0),
(8, 1, 8, 100.00, 0.00, 100.00, 0),
(9, 1, 9, 30.00, 0.00, 30.00, 0),
(10, 1, 10, 20.00, 0.00, 20.00, 0),
(11, 2, 7, 3000.00, 0.00, 3000.00, 0),
(12, 2, 8, 50.00, 0.00, 50.00, 0),
(13, 2, 9, 10.00, 0.00, 10.00, 0);
