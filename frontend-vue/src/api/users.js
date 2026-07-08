import request from './request'

/** 获取当前用户信息 */
export function getProfile() {
  return request.get('/users/me')
}

/** 更新个人信息 */
export function updateProfile(data) {
  return request.put('/users/me', data)
}

/** 提交送养人认证申请（进入审核流程） */
export function submitAdopterApplication(data) {
  return request.put('/users/me/adopter-application', data)
}

/** 查询我的送养人认证申请状态 */
export function getAdopterApplication() {
  return request.get('/users/me/adopter-application')
}
