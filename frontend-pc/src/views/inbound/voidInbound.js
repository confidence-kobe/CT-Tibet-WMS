/**
 * 入库单作废确认
 * 列表页和详情页共用：弹窗填写作废原因，确认后调用接口
 */

import { ElMessage, ElMessageBox } from 'element-plus'
import { voidInbound } from '@/api/inbound'

/** 入库单状态：已作废 */
export const INBOUND_STATUS_VOID = 2

/**
 * 弹出作废确认框
 * @param {Object} inbound - 入库单
 * @param {number} inbound.id - 入库单ID
 * @param {string} inbound.inboundNo - 入库单号
 * @returns {Promise<boolean>} 作废成功返回 true，取消或失败返回 false
 */
export async function confirmVoidInbound(inbound) {
  let reason
  try {
    const result = await ElMessageBox.prompt(
      `作废后，入库单"${inbound.inboundNo}"增加的库存会被扣回，且不能恢复。如果这批货已被领用或被审批锁定，将无法作废。`,
      '作废入库单',
      {
        confirmButtonText: '确认作废',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        type: 'warning',
        inputPlaceholder: '请填写作废原因（必填）',
        inputValidator: (value) => {
          const text = (value || '').trim()
          if (!text) return '请填写作废原因'
          if (text.length > 100) return '作废原因不能超过100字'
          return true
        }
      }
    )
    reason = result.value.trim()
  } catch {
    // 用户取消
    return false
  }

  try {
    await voidInbound(inbound.id, reason)
    ElMessage.success('已作废，库存已扣回')
    return true
  } catch (error) {
    // 错误提示（如库存不足）已由请求拦截器展示
    console.error('作废入库单失败:', error)
    return false
  }
}
