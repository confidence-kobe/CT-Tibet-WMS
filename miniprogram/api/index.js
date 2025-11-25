/**
 * API统一导出
 * 使用方式：
 * import api from '@/api'
 * api.auth.login(data)
 * api.apply.createApply(data)
 */

import auth from './auth.js'
import apply from './apply.js'
import approval from './approval.js'
import inventory from './inventory.js'
import inbound from './inbound.js'
import outbound from './outbound.js'
import message from './message.js'
import user from './user.js'
import common from './common.js'

export default {
  // 认证授权
  auth,

  // 申请管理（员工端）
  apply,

  // 审批管理（仓管端）
  approval,

  // 库存管理
  inventory,

  // 入库管理（仓管端）
  inbound,

  // 出库管理（仓管端）
  outbound,

  // 消息管理
  message,

  // 用户管理
  user,

  // 公共接口（物资、仓库、部门等）
  common
}
