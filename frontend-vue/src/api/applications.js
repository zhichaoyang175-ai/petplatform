import request from './request'

/** 提交领养申请 */
export function submitApplication(data) {
  return request.post('/applications', data)
}

/** 获取申请详情 */
export function getApplicationDetail(id) {
  return request.get(`/applications/${id}`)
}

/** 我的申请列表 */
export function getMyApplications(params = {}) {
  return request.get('/applications', { params })
}

/** 收到的申请（送养人） */
export function getReceivedApplications(params = {}) {
  return request.get('/applications/received', { params })
}

/** 审核申请 (action: "approve" | "reject") */
export function reviewApplication(id, action, rejectReason) {
  return request.put(`/applications/${id}/review`, { action, rejectReason })
}

/** 取消申请 */
export function cancelApplication(id) {
  return request.put(`/applications/${id}/cancel`)
}
