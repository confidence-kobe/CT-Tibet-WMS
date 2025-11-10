/**
 * 常量定义
 */

// 申请单状态
export const APPLY_STATUS = {
  PENDING: 0,     // 待审批
  APPROVED: 1,    // 已通过
  REJECTED: 2,    // 已拒绝
  COMPLETED: 3,   // 已出库
  CANCELED: 4     // 已取消
}

export const APPLY_STATUS_MAP = {
  0: { text: '待审批', color: '#faad14', icon: '⏳' },
  1: { text: '已通过', color: '#52c41a', icon: '✓' },
  2: { text: '已拒绝', color: '#f5222d', icon: '✗' },
  3: { text: '已出库', color: '#8c8c8c', icon: '✓' },
  4: { text: '已取消', color: '#bfbfbf', icon: '⊘' }
}

// 出库单状态
export const OUTBOUND_STATUS = {
  PENDING: 0,     // 待领取
  COMPLETED: 1,   // 已出库
  CANCELED: 2     // 已取消
}

export const OUTBOUND_STATUS_MAP = {
  0: { text: '待领取', color: '#faad14', icon: '⏳' },
  1: { text: '已出库', color: '#52c41a', icon: '✓' },
  2: { text: '已取消', color: '#bfbfbf', icon: '⊘' }
}

// 库存状态
export const STOCK_STATUS = {
  NORMAL: 0,      // 正常
  LOW: 1,         // 低库存
  OUT: 2          // 缺货
}

export const STOCK_STATUS_MAP = {
  0: { text: '正常', color: '#52c41a', icon: '✓' },
  1: { text: '低库存', color: '#faad14', icon: '⚠️' },
  2: { text: '缺货', color: '#f5222d', icon: '⛔' }
}

// 入库类型
export const INBOUND_TYPE = {
  PURCHASE: 1,    // 采购入库
  RETURN: 2,      // 退货入库
  TRANSFER: 3,    // 调拨入库
  OTHER: 4        // 其他
}

export const INBOUND_TYPE_MAP = {
  1: '采购入库',
  2: '退货入库',
  3: '调拨入库',
  4: '其他'
}

export const INBOUND_TYPE_OPTIONS = [
  { value: 1, label: '采购入库' },
  { value: 2, label: '退货入库' },
  { value: 3, label: '调拨入库' },
  { value: 4, label: '其他' }
]

// 出库类型
export const OUTBOUND_TYPE = {
  USE: 1,         // 领用出库
  SCRAP: 2,       // 报废出库
  TRANSFER: 3,    // 调拨出库
  OTHER: 4        // 其他
}

export const OUTBOUND_TYPE_MAP = {
  1: '领用出库',
  2: '报废出库',
  3: '调拨出库',
  4: '其他'
}

export const OUTBOUND_TYPE_OPTIONS = [
  { value: 1, label: '领用出库' },
  { value: 2, label: '报废出库' },
  { value: 3, label: '调拨出库' },
  { value: 4, label: '其他' }
]

// 出库来源
export const OUTBOUND_SOURCE = {
  DIRECT: 1,      // 直接创建
  APPLY: 2        // 申请自动创建
}

export const OUTBOUND_SOURCE_MAP = {
  1: '直接创建',
  2: '申请创建'
}

// 消息类型
export const MESSAGE_TYPE = {
  SYSTEM: 1,      // 系统通知
  APPLY: 2,       // 申请通知
  APPROVAL: 3,    // 审批通知
  STOCK: 4        // 库存预警
}

export const MESSAGE_TYPE_MAP = {
  1: '系统通知',
  2: '申请通知',
  3: '审批通知',
  4: '库存预警'
}

// 角色代码
export const ROLE_CODE = {
  ADMIN: 'admin',           // 系统管理员
  DEPT_ADMIN: 'dept_admin', // 部门管理员
  WAREHOUSE: 'warehouse',   // 仓库管理员
  USER: 'user'              // 普通员工
}

// 物资类别
export const MATERIAL_CATEGORIES = [
  '光缆类',
  '设备类',
  '配件类',
  '工具类'
]

// 日期格式化选项
export const DATE_FORMAT = {
  DATE: 'yyyy-MM-dd',
  DATETIME: 'yyyy-MM-dd HH:mm:ss',
  TIME: 'HH:mm:ss',
  MONTH: 'yyyy-MM',
  YEAR: 'yyyy'
}

// 分页默认配置
export const PAGE_CONFIG = {
  PAGE_NUM: 1,
  PAGE_SIZE: 20
}
