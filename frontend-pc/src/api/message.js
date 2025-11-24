/**
 * 消息中心API接口
 */

import request from './request'

/**
 * 获取消息列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, isRead, type }
 * @returns {Promise} 返回消息列表
 */
export function listMessages(params) {
  return request({
    url: '/messages',
    method: 'get',
    params
  })
}

/**
 * 标记消息为已读
 * @param {Number} id - 消息ID
 * @returns {Promise} 返回操作结果
 */
export function markAsRead(id) {
  return request({
    url: `/messages/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记所有消息为已读
 * @returns {Promise} 返回操作结果
 */
export function markAllAsRead() {
  return request({
    url: '/messages/read-all',
    method: 'put'
  })
}

/**
 * 删除消息
 * @param {Number} id - 消息ID
 * @returns {Promise} 返回操作结果
 */
export function deleteMessage(id) {
  return request({
    url: `/messages/${id}`,
    method: 'delete'
  })
}

/**
 * 获取未读消息数量
 * @returns {Promise} 返回未读消息数量
 */
export function getUnreadCount() {
  return request({
    url: '/messages/unread-count',
    method: 'get'
  })
}
