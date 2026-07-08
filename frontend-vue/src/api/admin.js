import request from './request'

/** 管理后台仪表盘 */
export function getDashboard() {
  return request.get('/admin/dashboard')
}

/** 管理后台 KPI 统计（真实数据） */
export function getDashboardStats() {
  return request.get('/admin/stats')
}

/** 领养趋势 */
export function getTrend() {
  return request.get('/admin/trend')
}

/** 用户管理列表 */
export function getUsers(params = {}) {
  return request.get('/admin/users', { params })
}

/** 启用/禁用用户 */
export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, { status })
}

/** 宠物管理列表（含已下架） */
export function getAdminPets(params = {}) {
  return request.get('/admin/pets', { params })
}

/** 修改宠物状态（审核） */
export function updateAdminPetStatus(id, status) {
  return request.put(`/admin/pets/${id}/status`, { status })
}

/** 平台数据统计 */
export function getStatistics() {
  return request.get('/admin/statistics')
}

/** 管理端：领养申请详情 */
export function getAdminApplicationDetail(id) {
  return request.get(`/admin/applications/${id}`)
}

/** 管理端：通过领养申请 */
export function adminApproveApplication(id) {
  return request.put(`/admin/applications/${id}/approve`)
}

/** 管理端：拒绝领养申请 */
export function adminRejectApplication(id, rejectReason) {
  return request.put(`/admin/applications/${id}/reject`, { rejectReason })
}
