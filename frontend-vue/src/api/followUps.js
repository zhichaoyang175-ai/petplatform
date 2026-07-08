import request from './request'

/** 我的回访任务 */
export function getMyTasks(params = {}) {
  return request.get('/follow-ups/tasks', { params })
}

/** 回访任务详情 */
export function getTaskDetail(id) {
  return request.get(`/follow-ups/tasks/${id}`)
}

/** 提交回访 */
export function submitFollowUp(id, data) {
  return request.post(`/follow-ups/tasks/${id}/submit`, data)
}

/** 宠物回访记录 */
export function getFollowUpRecords(params = {}) {
  return request.get('/follow-ups/records', { params })
}
