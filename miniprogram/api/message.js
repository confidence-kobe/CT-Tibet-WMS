/**
 * 消息管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 查询消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.messageType - 消息类型（可选）1-系统通知 2-审批通知 3-出库通知
 * @param {number} params.readStatus - 阅读状态（可选）0-未读 1-已读
 * @returns {Promise} 返回消息列表
 */
export function getMessages(params) {
  return $uRequest({
    url: '/api/messages',
    method: 'GET',
    data: params
  })
}

/**
 * 查询未读消息数量
 * @returns {Promise} 返回未读消息数量
 */
export function getUnreadCount() {
  return $uRequest({
    url: '/api/messages/unread-count',
    method: 'GET'
  })
}

/**
 * 标记消息为已读
 * @param {number} id - 消息ID
 * @returns {Promise} 返回标记结果
 */
export function markRead(id) {
  return $uRequest({
    url: `/api/messages/${id}/read`,
    method: 'POST'
  })
}

/**
 * 标记所有消息为已读
 * @returns {Promise} 返回标记结果
 */
export function markAllRead() {
  return $uRequest({
    url: '/api/messages/read-all',
    method: 'POST'
  })
}

/**
 * 删除消息
 * @param {number} id - 消息ID
 * @returns {Promise} 返回删除结果
 */
export function deleteMessage(id) {
  return $uRequest({
    url: `/api/messages/${id}`,
    method: 'DELETE'
  })
}

/**
 * 批量删除消息
 * @param {Array} ids - 消息ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function batchDeleteMessages(ids) {
  return $uRequest({
    url: '/api/messages/batch-delete',
    method: 'POST',
    data: {
      ids
    }
  })
}

/**
 * 查询消息详情
 * @param {number} id - 消息ID
 * @returns {Promise} 返回消息详情
 */
export function getMessageDetail(id) {
  return $uRequest({
    url: `/api/messages/${id}`,
    method: 'GET'
  })
}

export default {
  getMessages,
  getUnreadCount,
  markRead,
  markAllRead,
  deleteMessage,
  batchDeleteMessages,
  getMessageDetail
}
