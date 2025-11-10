// 模拟数据
const mockData = {
    // 部门数据
    departments: [
        { id: 1, name: '网络运维部', code: 'WLYW', manager: '张三' },
        { id: 2, name: '政企客户部', code: 'ZQKH', manager: '李四' },
        { id: 3, name: '市场营销部', code: 'SCYX', manager: '王五' },
        { id: 4, name: '客户服务部', code: 'KHFW', manager: '赵六' },
        { id: 5, name: '综合管理部', code: 'ZHGL', manager: '钱七' },
        { id: 6, name: '信息技术部', code: 'XXJS', manager: '孙八' },
        { id: 7, name: '财务部', code: 'CWB', manager: '周九' }
    ],

    // 仓库数据
    warehouses: [
        { id: 1, name: '网络运维部仓库', deptId: 1, deptName: '网络运维部', manager: '张三', location: '1号楼2层' },
        { id: 2, name: '网络运维部备品仓', deptId: 1, deptName: '网络运维部', manager: '张三', location: '1号楼3层' },
        { id: 3, name: '政企客户部仓库', deptId: 2, deptName: '政企客户部', manager: '李四', location: '2号楼1层' },
        { id: 4, name: '市场营销部仓库', deptId: 3, deptName: '市场营销部', manager: '王五', location: '2号楼2层' },
        { id: 5, name: '客户服务部仓库', deptId: 4, deptName: '客户服务部', manager: '赵六', location: '3号楼1层' },
        { id: 6, name: '信息技术部仓库', deptId: 6, deptName: '信息技术部', manager: '孙八', location: '3号楼3层' }
    ],

    // 物资分类
    materialCategories: [
        { id: 1, name: '网络设备', code: 'WLSB' },
        { id: 2, name: '终端设备', code: 'ZDSB' },
        { id: 3, name: '办公用品', code: 'BGYP' },
        { id: 4, name: '线缆配件', code: 'XLPJ' },
        { id: 5, name: '工具仪表', code: 'GJYB' },
        { id: 6, name: '劳保用品', code: 'LBYP' }
    ],

    // 物资数据
    materials: [
        { id: 1, code: 'M001', name: '光纤收发器', categoryId: 1, category: '网络设备', spec: 'TP-LINK TL-FC311A', unit: '台', price: 280 },
        { id: 2, code: 'M002', name: '交换机', categoryId: 1, category: '网络设备', spec: 'H3C S5120V2-28P-LI', unit: '台', price: 3500 },
        { id: 3, code: 'M003', name: '路由器', categoryId: 1, category: '网络设备', spec: 'H3C ER3200G2', unit: '台', price: 1200 },
        { id: 4, code: 'M004', name: '光猫', categoryId: 2, category: '终端设备', spec: 'HS8145V5', unit: '台', price: 150 },
        { id: 5, code: 'M005', name: '机顶盒', categoryId: 2, category: '终端设备', spec: 'EC6108V9C', unit: '台', price: 120 },
        { id: 6, code: 'M006', name: 'A4打印纸', categoryId: 3, category: '办公用品', spec: '70g 500张/包', unit: '包', price: 25 },
        { id: 7, code: 'M007', name: '签字笔', categoryId: 3, category: '办公用品', spec: '晨光 黑色 0.5mm', unit: '支', price: 2 },
        { id: 8, code: 'M008', name: '网线', categoryId: 4, category: '线缆配件', spec: '超六类 305米/箱', unit: '箱', price: 650 },
        { id: 9, code: 'M009', name: '光纤跳线', categoryId: 4, category: '线缆配件', spec: 'SC-SC 单模 3米', unit: '根', price: 15 },
        { id: 10, code: 'M010', name: '水晶头', categoryId: 4, category: '线缆配件', spec: 'RJ45 超六类', unit: '个', price: 1.5 },
        { id: 11, code: 'M011', name: '光功率计', categoryId: 5, category: '工具仪表', spec: 'JW3208A', unit: '台', price: 850 },
        { id: 12, code: 'M012', name: '网线钳', categoryId: 5, category: '工具仪表', spec: '宝工 CP-376TR', unit: '把', price: 180 },
        { id: 13, code: 'M013', name: '安全帽', categoryId: 6, category: '劳保用品', spec: 'ABS 黄色', unit: '顶', price: 35 },
        { id: 14, code: 'M014', name: '工作手套', categoryId: 6, category: '劳保用品', spec: '防滑 尼龙', unit: '副', price: 8 },
        { id: 15, code: 'M015', name: '反光背心', categoryId: 6, category: '劳保用品', spec: 'XL 橙色', unit: '件', price: 25 }
    ],

    // 用户数据
    users: [
        { id: 1, username: 'admin', name: '系统管理员', role: 'ADMIN', roleName: '系统管理员', deptId: 5, deptName: '综合管理部', phone: '13800138000' },
        { id: 2, username: 'wl_warehouse', name: '张三', role: 'WAREHOUSE', roleName: '仓库管理员', deptId: 1, deptName: '网络运维部', phone: '13800138001' },
        { id: 3, username: 'wl_user1', name: '李明', role: 'USER', roleName: '普通员工', deptId: 1, deptName: '网络运维部', phone: '13800138002' },
        { id: 4, username: 'wl_user2', name: '王芳', role: 'USER', roleName: '普通员工', deptId: 1, deptName: '网络运维部', phone: '13800138003' },
        { id: 5, username: 'zq_warehouse', name: '李四', role: 'WAREHOUSE', roleName: '仓库管理员', deptId: 2, deptName: '政企客户部', phone: '13800138004' },
        { id: 6, username: 'zq_user1', name: '赵强', role: 'USER', roleName: '普通员工', deptId: 2, deptName: '政企客户部', phone: '13800138005' }
    ],

    // 入库单列表
    inboundList: [
        {
            id: 1,
            code: 'RK20250111001',
            warehouseId: 1,
            warehouseName: '网络运维部仓库',
            createUser: '张三',
            createTime: '2025-01-11 09:30:00',
            totalAmount: 8400,
            itemCount: 2,
            remark: '设备升级采购',
            details: [
                { materialId: 2, materialCode: 'M002', materialName: '交换机', spec: 'H3C S5120V2-28P-LI', quantity: 2, price: 3500, amount: 7000 },
                { materialId: 3, materialCode: 'M003', materialName: '路由器', spec: 'H3C ER3200G2', quantity: 1, price: 1200, amount: 1200 }
            ]
        },
        {
            id: 2,
            code: 'RK20250111002',
            warehouseId: 1,
            warehouseName: '网络运维部仓库',
            createUser: '张三',
            createTime: '2025-01-10 14:20:00',
            totalAmount: 2800,
            itemCount: 4,
            remark: '日常补货',
            details: [
                { materialId: 8, materialCode: 'M008', materialName: '网线', spec: '超六类 305米/箱', quantity: 4, price: 650, amount: 2600 },
                { materialId: 9, materialCode: 'M009', materialName: '光纤跳线', spec: 'SC-SC 单模 3米', quantity: 10, price: 15, amount: 150 }
            ]
        },
        {
            id: 3,
            code: 'RK20250110003',
            warehouseId: 3,
            warehouseName: '政企客户部仓库',
            createUser: '李四',
            createTime: '2025-01-10 10:15:00',
            totalAmount: 1650,
            itemCount: 3,
            remark: '客户资料用品',
            details: [
                { materialId: 4, materialCode: 'M004', materialName: '光猫', spec: 'HS8145V5', quantity: 10, price: 150, amount: 1500 },
                { materialId: 10, materialCode: 'M010', materialName: '水晶头', spec: 'RJ45 超六类', quantity: 100, price: 1.5, amount: 150 }
            ]
        }
    ],

    // 出库单列表
    outboundList: [
        {
            id: 1,
            code: 'CK20250111001',
            warehouseId: 1,
            warehouseName: '网络运维部仓库',
            source: 1, // 1-直接出库 2-申请出库
            sourceText: '直接出库',
            receiver: '张三',
            receiveTime: '2025-01-11 10:30:00',
            createUser: '张三',
            createTime: '2025-01-11 10:30:00',
            status: 1, // 0-待领取 1-已完成 2-已取消
            statusText: '已完成',
            totalAmount: 280,
            itemCount: 1,
            remark: '紧急维修使用',
            details: [
                { materialId: 1, materialCode: 'M001', materialName: '光纤收发器', spec: 'TP-LINK TL-FC311A', quantity: 1, price: 280, amount: 280 }
            ]
        },
        {
            id: 2,
            code: 'CK20250111002',
            warehouseId: 1,
            warehouseName: '网络运维部仓库',
            source: 2,
            sourceText: '申请出库',
            applyId: 1,
            receiver: '李明',
            receiveTime: null,
            createUser: '张三',
            createTime: '2025-01-11 11:00:00',
            status: 0,
            statusText: '待领取',
            totalAmount: 195,
            itemCount: 2,
            remark: '',
            details: [
                { materialId: 9, materialCode: 'M009', materialName: '光纤跳线', spec: 'SC-SC 单模 3米', quantity: 10, price: 15, amount: 150 },
                { materialId: 10, materialCode: 'M010', materialName: '水晶头', spec: 'RJ45 超六类', quantity: 30, price: 1.5, amount: 45 }
            ]
        },
        {
            id: 3,
            code: 'CK20250110003',
            warehouseId: 1,
            warehouseName: '网络运维部仓库',
            source: 2,
            sourceText: '申请出库',
            applyId: 3,
            receiver: '王芳',
            receiveTime: '2025-01-10 15:20:00',
            createUser: '张三',
            createTime: '2025-01-10 14:30:00',
            status: 1,
            statusText: '已完成',
            totalAmount: 50,
            itemCount: 1,
            remark: '',
            details: [
                { materialId: 6, materialCode: 'M006', materialName: 'A4打印纸', spec: '70g 500张/包', quantity: 2, price: 25, amount: 50 }
            ]
        }
    ],

    // 申请单列表
    applyList: [
        {
            id: 1,
            code: 'SQ20250111001',
            applyUser: '李明',
            applyUserId: 3,
            applyDept: '网络运维部',
            applyDeptId: 1,
            applyTime: '2025-01-11 09:00:00',
            purpose: '小区宽带故障维修',
            status: 1, // 0-待审批 1-已通过 2-已拒绝 3-已完成 4-已取消
            statusText: '已通过',
            approver: '张三',
            approveTime: '2025-01-11 09:30:00',
            approveRemark: '同意',
            outboundId: 2,
            totalAmount: 195,
            itemCount: 2,
            details: [
                { materialId: 9, materialCode: 'M009', materialName: '光纤跳线', spec: 'SC-SC 单模 3米', quantity: 10, stockQuantity: 50, price: 15, amount: 150 },
                { materialId: 10, materialCode: 'M010', materialName: '水晶头', spec: 'RJ45 超六类', quantity: 30, stockQuantity: 200, price: 1.5, amount: 45 }
            ]
        },
        {
            id: 2,
            code: 'SQ20250111002',
            applyUser: '王芳',
            applyUserId: 4,
            applyDept: '网络运维部',
            applyDeptId: 1,
            applyTime: '2025-01-11 10:00:00',
            purpose: '机房设备维护',
            status: 0,
            statusText: '待审批',
            approver: null,
            approveTime: null,
            approveRemark: null,
            outboundId: null,
            totalAmount: 3780,
            itemCount: 2,
            details: [
                { materialId: 2, materialCode: 'M002', materialName: '交换机', spec: 'H3C S5120V2-28P-LI', quantity: 1, stockQuantity: 2, price: 3500, amount: 3500 },
                { materialId: 1, materialCode: 'M001', materialName: '光纤收发器', spec: 'TP-LINK TL-FC311A', quantity: 1, stockQuantity: 5, price: 280, amount: 280 }
            ]
        },
        {
            id: 3,
            code: 'SQ20250110003',
            applyUser: '王芳',
            applyUserId: 4,
            applyDept: '网络运维部',
            applyDeptId: 1,
            applyTime: '2025-01-10 14:00:00',
            purpose: '办公用品申领',
            status: 3,
            statusText: '已完成',
            approver: '张三',
            approveTime: '2025-01-10 14:10:00',
            approveRemark: '同意',
            outboundId: 3,
            totalAmount: 50,
            itemCount: 1,
            details: [
                { materialId: 6, materialCode: 'M006', materialName: 'A4打印纸', spec: '70g 500张/包', quantity: 2, stockQuantity: 20, price: 25, amount: 50 }
            ]
        },
        {
            id: 4,
            code: 'SQ20250109004',
            applyUser: '李明',
            applyUserId: 3,
            applyDept: '网络运维部',
            applyDeptId: 1,
            applyTime: '2025-01-09 15:00:00',
            purpose: '测试申请',
            status: 2,
            statusText: '已拒绝',
            approver: '张三',
            approveTime: '2025-01-09 15:30:00',
            approveRemark: '库存不足，暂不通过',
            outboundId: null,
            totalAmount: 1200,
            itemCount: 1,
            details: [
                { materialId: 3, materialCode: 'M003', materialName: '路由器', spec: 'H3C ER3200G2', quantity: 1, stockQuantity: 0, price: 1200, amount: 1200 }
            ]
        }
    ],

    // 库存数据
    inventoryList: [
        { id: 1, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 1, materialCode: 'M001', materialName: '光纤收发器', category: '网络设备', spec: 'TP-LINK TL-FC311A', unit: '台', quantity: 5, minQuantity: 3, maxQuantity: 20, price: 280, amount: 1400, status: 1, statusText: '正常' },
        { id: 2, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 2, materialCode: 'M002', materialName: '交换机', category: '网络设备', spec: 'H3C S5120V2-28P-LI', unit: '台', quantity: 2, minQuantity: 2, maxQuantity: 10, price: 3500, amount: 7000, status: 2, statusText: '低库存' },
        { id: 3, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 3, materialCode: 'M003', materialName: '路由器', category: '网络设备', spec: 'H3C ER3200G2', unit: '台', quantity: 0, minQuantity: 2, maxQuantity: 10, price: 1200, amount: 0, status: 3, statusText: '缺货' },
        { id: 4, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 6, materialCode: 'M006', materialName: 'A4打印纸', category: '办公用品', spec: '70g 500张/包', unit: '包', quantity: 18, minQuantity: 10, maxQuantity: 50, price: 25, amount: 450, status: 1, statusText: '正常' },
        { id: 5, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 8, materialCode: 'M008', materialName: '网线', category: '线缆配件', spec: '超六类 305米/箱', unit: '箱', quantity: 4, minQuantity: 2, maxQuantity: 10, price: 650, amount: 2600, status: 1, statusText: '正常' },
        { id: 6, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 9, materialCode: 'M009', materialName: '光纤跳线', category: '线缆配件', spec: 'SC-SC 单模 3米', unit: '根', quantity: 50, minQuantity: 20, maxQuantity: 100, price: 15, amount: 750, status: 1, statusText: '正常' },
        { id: 7, warehouseId: 1, warehouseName: '网络运维部仓库', materialId: 10, materialCode: 'M010', materialName: '水晶头', category: '线缆配件', spec: 'RJ45 超六类', unit: '个', quantity: 200, minQuantity: 100, maxQuantity: 500, price: 1.5, amount: 300, status: 1, statusText: '正常' },
        { id: 8, warehouseId: 3, warehouseName: '政企客户部仓库', materialId: 4, materialCode: 'M004', materialName: '光猫', category: '终端设备', spec: 'HS8145V5', unit: '台', quantity: 10, minQuantity: 5, maxQuantity: 30, price: 150, amount: 1500, status: 1, statusText: '正常' },
        { id: 9, warehouseId: 3, warehouseName: '政企客户部仓库', materialId: 5, materialCode: 'M005', materialName: '机顶盒', category: '终端设备', spec: 'EC6108V9C', unit: '台', quantity: 3, minQuantity: 5, maxQuantity: 20, price: 120, amount: 360, status: 2, statusText: '低库存' }
    ],

    // 统计数据
    statistics: {
        today: {
            inboundCount: 2,
            inboundAmount: 11200,
            outboundCount: 1,
            outboundAmount: 280,
            applyCount: 2,
            approveCount: 1
        },
        week: {
            inboundCount: 8,
            inboundAmount: 45600,
            outboundCount: 12,
            outboundAmount: 8950,
            applyCount: 15,
            approveCount: 13
        },
        month: {
            inboundCount: 28,
            inboundAmount: 186500,
            outboundCount: 45,
            outboundAmount: 32400,
            applyCount: 52,
            approveCount: 48
        },
        // 按物资分类统计
        byCategory: [
            { category: '网络设备', inboundCount: 12, inboundAmount: 98500, outboundCount: 8, outboundAmount: 15600 },
            { category: '终端设备', inboundCount: 6, inboundAmount: 28400, outboundCount: 15, outboundAmount: 4500 },
            { category: '办公用品', inboundCount: 4, inboundAmount: 1250, outboundCount: 12, outboundAmount: 600 },
            { category: '线缆配件', inboundCount: 3, inboundAmount: 3800, outboundCount: 6, outboundAmount: 980 },
            { category: '工具仪表', inboundCount: 2, inboundAmount: 2060, outboundCount: 3, outboundAmount: 1700 },
            { category: '劳保用品', inboundCount: 1, inboundAmount: 490, outboundCount: 1, outboundAmount: 20 }
        ],
        // 每日趋势（最近7天）
        dailyTrend: [
            { date: '01-05', inboundCount: 3, outboundCount: 5 },
            { date: '01-06', inboundCount: 2, outboundCount: 4 },
            { date: '01-07', inboundCount: 1, outboundCount: 3 },
            { date: '01-08', inboundCount: 4, outboundCount: 6 },
            { date: '01-09', inboundCount: 2, outboundCount: 7 },
            { date: '01-10', inboundCount: 3, outboundCount: 5 },
            { date: '01-11', inboundCount: 2, outboundCount: 1 }
        ]
    },

    // 消息通知
    notifications: [
        { id: 1, type: 'approval', title: '待审批提醒', content: '王芳提交的申请单 SQ20250111002 待审批', time: '2025-01-11 10:00:00', read: false },
        { id: 2, type: 'stock', title: '库存预警', content: '交换机库存低于最小库存，当前库存：2台', time: '2025-01-11 09:30:00', read: false },
        { id: 3, type: 'stock', title: '库存预警', content: '路由器已缺货，请及时补货', time: '2025-01-11 08:00:00', read: true },
        { id: 4, type: 'approval', title: '审批通过', content: '您的申请单 SQ20250111001 已通过审批，请及时领取', time: '2025-01-11 09:30:00', read: true },
        { id: 5, type: 'system', title: '系统通知', content: '系统将于今晚22:00-23:00进行维护，请提前保存数据', time: '2025-01-10 16:00:00', read: true }
    ]
};

// 辅助函数
const mockUtils = {
    // 根据ID获取部门
    getDepartmentById(id) {
        return mockData.departments.find(d => d.id === id);
    },

    // 根据ID获取仓库
    getWarehouseById(id) {
        return mockData.warehouses.find(w => w.id === id);
    },

    // 根据ID获取物资
    getMaterialById(id) {
        return mockData.materials.find(m => m.id === id);
    },

    // 根据ID获取用户
    getUserById(id) {
        return mockData.users.find(u => u.id === id);
    },

    // 格式化金额
    formatMoney(amount) {
        return '¥' + amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },

    // 格式化日期
    formatDate(dateStr) {
        if (!dateStr) return '-';
        return dateStr.substring(0, 16);
    },

    // 获取状态标签样式
    getStatusClass(status, type = 'apply') {
        const statusMap = {
            apply: {
                0: 'warning',  // 待审批
                1: 'success',  // 已通过
                2: 'danger',   // 已拒绝
                3: 'info',     // 已完成
                4: 'default'   // 已取消
            },
            outbound: {
                0: 'warning',  // 待领取
                1: 'success',  // 已完成
                2: 'default'   // 已取消
            },
            inventory: {
                1: 'success',  // 正常
                2: 'warning',  // 低库存
                3: 'danger'    // 缺货
            }
        };
        return statusMap[type][status] || 'default';
    }
};
