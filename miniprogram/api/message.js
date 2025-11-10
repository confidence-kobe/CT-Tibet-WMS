/**
 * 消息通知相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 消息列表
 * @param {Object} params - 查询参数
 */
export function getMessageList(params) {
  return $uRequest({
    url: '/api/messages',
    method: 'GET',
    data: params
  })
}

/**
 * 标记已读
 * @param {Number} id - 消息ID
 */
export function markAsRead(id) {
  return $uRequest({
    url: `/api/messages/${id}/read`,
    method: 'PUT'
  })
}

/**
 * 全部标记已读
 */
export function markAllAsRead() {
  return $uRequest({
    url: '/api/messages/read-all',
    method: 'PUT'
  })
}

/**
 * 未读数量
 */
export function getUnreadCount() {
  return $uRequest({
    url: '/api/messages/unread-count',
    method: 'GET'
  })
}
