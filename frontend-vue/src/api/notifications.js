import request from './request'

/** 通知列表 */
export function getNotifications(params = {}) {
  return request.get('/notifications', { params })
}

/** 标记单条已读 */
export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

/** 全部标为已读 */
export function markAllRead() {
  return request.put('/notifications/read-all')
}

/** 未读数量 */
export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}
