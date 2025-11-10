/**
 * 全局常量定义
 * 与后端API和数据库设计保持一致
 */

// ============ 角色相关 ============
/**
 * 角色编码（与数据库tb_role.role_code字段一致）
 */
export const ROLE_CODE = {
  ADMIN: 'admin',           // 系统管理员
  DEPT_ADMIN: 'dept_admin', // 部门管理员
  WAREHOUSE: 'warehouse',   // 仓库管理员
  USER: 'user'              // 普通员工
}

/**
 * 角色名称映射
 */
export const ROLE_NAME_MAP = {
  [ROLE_CODE.ADMIN]: '系统管理员',
  [ROLE_CODE.DEPT_ADMIN]: '部门管理员',
  [ROLE_CODE.WAREHOUSE]: '仓库管理员',
  [ROLE_CODE.USER]: '普通员工'
}

// ============ 状态相关 ============
/**
 * 通用状态
 */
export const COMMON_STATUS = {
  ENABLED: 0,   // 启用
  DISABLED: 1   // 禁用
}

/**
 * 申请单状态（与数据库tb_apply.status字段一致）
 */
export const APPLY_STATUS = {
  PENDING: 0,    // 待审批
  APPROVED: 1,   // 已通过
  REJECTED: 2,   // 已拒绝
  COMPLETED: 3,  // 已出库
  CANCELED: 4    // 已取消
}

/**
 * 申请单状态名称映射
 */
export const APPLY_STATUS_NAME = {
  [APPLY_STATUS.PENDING]: '待审批',
  [APPLY_STATUS.APPROVED]: '已通过',
  [APPLY_STATUS.REJECTED]: '已拒绝',
  [APPLY_STATUS.COMPLETED]: '已出库',
  [APPLY_STATUS.CANCELED]: '已取消'
}

/**
 * 申请单状态标签类型映射（Element Plus Tag组件）
 */
export const APPLY_STATUS_TAG_TYPE = {
  [APPLY_STATUS.PENDING]: 'warning',
  [APPLY_STATUS.APPROVED]: 'success',
  [APPLY_STATUS.REJECTED]: 'danger',
  [APPLY_STATUS.COMPLETED]: 'info',
  [APPLY_STATUS.CANCELED]: 'info'
}

/**
 * 出库单状态（与数据库tb_outbound.status字段一致）
 */
export const OUTBOUND_STATUS = {
  PENDING: 0,    // 待领取
  COMPLETED: 1,  // 已出库
  CANCELED: 2    // 已取消
}

/**
 * 出库单状态名称映射
 */
export const OUTBOUND_STATUS_NAME = {
  [OUTBOUND_STATUS.PENDING]: '待领取',
  [OUTBOUND_STATUS.COMPLETED]: '已出库',
  [OUTBOUND_STATUS.CANCELED]: '已取消'
}

/**
 * 入库单状态（与数据库tb_inbound.status字段一致）
 */
export const INBOUND_STATUS = {
  COMPLETED: 1   // 已入库（入库单无审批流程，创建即完成）
}

// ============ 类型相关 ============
/**
 * 入库类型（与数据库tb_inbound.inbound_type字段一致）
 */
export const INBOUND_TYPE = {
  PURCHASE: 1,   // 采购入库
  RETURN: 2,     // 退货入库
  TRANSFER: 3,   // 调拨入库
  OTHER: 4       // 其他
}

/**
 * 入库类型名称映射
 */
export const INBOUND_TYPE_NAME = {
  [INBOUND_TYPE.PURCHASE]: '采购入库',
  [INBOUND_TYPE.RETURN]: '退货入库',
  [INBOUND_TYPE.TRANSFER]: '调拨入库',
  [INBOUND_TYPE.OTHER]: '其他'
}

/**
 * 出库类型（与数据库tb_outbound.outbound_type字段一致）
 */
export const OUTBOUND_TYPE = {
  RECEIVE: 1,    // 领用出库
  SCRAP: 2,      // 报废出库
  TRANSFER: 3,   // 调拨出库
  OTHER: 4       // 其他
}

/**
 * 出库类型名称映射
 */
export const OUTBOUND_TYPE_NAME = {
  [OUTBOUND_TYPE.RECEIVE]: '领用出库',
  [OUTBOUND_TYPE.SCRAP]: '报废出库',
  [OUTBOUND_TYPE.TRANSFER]: '调拨出库',
  [OUTBOUND_TYPE.OTHER]: '其他'
}

/**
 * 出库来源（与数据库tb_outbound.source字段一致）
 */
export const OUTBOUND_SOURCE = {
  DIRECT: 1,     // 直接创建（仓管直接出库）
  FROM_APPLY: 2  // 申请自动创建（审批通过后）
}

/**
 * 消息类型（与数据库tb_message.type字段一致）
 */
export const MESSAGE_TYPE = {
  SYSTEM: 1,     // 系统通知
  APPLY: 2,      // 申请通知
  APPROVAL: 3,   // 审批通知
  INVENTORY: 4   // 库存预警
}

// ============ 物资相关 ============
/**
 * 物资类别
 */
export const MATERIAL_CATEGORY = {
  CABLE: '光缆类',
  EQUIPMENT: '设备类',
  ACCESSORY: '配件类',
  TOOL: '工具类'
}

/**
 * 物资单位
 */
export const MATERIAL_UNIT = {
  PIECE: '条',
  UNIT: '台',
  METER: '米',
  ITEM: '个',
  BOX: '箱'
}

// ============ 库存相关 ============
/**
 * 库存状态
 */
export const STOCK_STATUS = {
  NORMAL: 'normal',    // 正常
  LOW: 'low',          // 低库存
  OUT: 'out'           // 缺货
}

/**
 * 库存状态名称映射
 */
export const STOCK_STATUS_NAME = {
  [STOCK_STATUS.NORMAL]: '正常',
  [STOCK_STATUS.LOW]: '低库存',
  [STOCK_STATUS.OUT]: '缺货'
}

/**
 * 库存状态标签类型映射
 */
export const STOCK_STATUS_TAG_TYPE = {
  [STOCK_STATUS.NORMAL]: 'success',
  [STOCK_STATUS.LOW]: 'warning',
  [STOCK_STATUS.OUT]: 'danger'
}

// ============ 业务规则常量 ============
/**
 * 审批超时时间（小时）
 */
export const APPROVAL_TIMEOUT_HOURS = 24

/**
 * 领取超时时间（天）
 */
export const PICKUP_TIMEOUT_DAYS = 7

/**
 * 分页默认配置
 */
export const PAGINATION = {
  PAGE: 1,
  SIZE: 20,
  SIZES: [10, 20, 50, 100]
}

// ============ 正则表达式 ============
/**
 * 常用正则表达式
 */
export const REGEX = {
  PHONE: /^1[3-9]\d{9}$/,                    // 手机号
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,       // 邮箱
  PASSWORD: /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/, // 密码（6-20位，包含字母和数字）
  ID_CARD: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/ // 身份证
}

// ============ 日期格式 ============
/**
 * 日期时间格式
 */
export const DATE_FORMAT = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY'
}

// ============ 文件上传相关 ============
/**
 * 文件上传配置
 */
export const UPLOAD_CONFIG = {
  MAX_SIZE: 10 * 1024 * 1024,  // 最大10MB
  ACCEPT_IMAGES: '.jpg,.jpeg,.png,.gif,.webp',
  ACCEPT_DOCS: '.doc,.docx,.xls,.xlsx,.pdf',
  ACCEPT_ALL: '*'
}
