import request from './request'

/** 手机号+密码登录 */
export function login(phone, password) {
  return request.post('/auth/login', { phone, password })
}

/** 手机号注册 */
export function register(phone, password, nickname, role) {
  return request.post('/auth/register', { phone, password, nickname, role })
}

/** 发送短信验证码 */
export function sendCode(phone) {
  return request.post('/auth/send-code', { phone })
}

/** 刷新令牌 */
export function refreshToken(refreshToken) {
  return request.post('/auth/refresh-token', { refreshToken })
}

/** 登出 */
export function logout() {
  return request.post('/auth/logout')
}
