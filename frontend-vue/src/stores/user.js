import { ref, computed, reactive } from 'vue'

// 模块级单例 ref，确保多个组件调用 useAuthStore 共享同一份状态
const token = ref(localStorage.getItem('token') || '')
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

export function useAuthStore() {
  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || 0)

  function saveLogin(loginData) {
    token.value = loginData.token
    userInfo.value = {
      userId: loginData.userId,
      role: loginData.role,
      nickname: loginData.nickname,
      avatarUrl: loginData.avatarUrl || '',
      // 登录时若已携带 phone/email 则一并写入，后续 getProfile 会补全缺失字段
      phone: loginData.phone || '',
      email: loginData.email || '',
    }
    localStorage.setItem('token', loginData.token)
    if (loginData.refreshToken) {
      localStorage.setItem('refreshToken', loginData.refreshToken)
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function clearLogin() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  function getToken() {
    return token.value
  }

  // 用后端 UserVO 合并更新本地 userInfo（补全 phone/email/nickname 等）
  function setProfile(p) {
    if (!p) return
    userInfo.value = { ...userInfo.value, ...p }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  // 用 reactive 包裹；reactive 会自动解包属性里的 ref/computed，
  // 因此 authStore.userInfo 是对象、authStore.isLoggedIn 是布尔
  const authStore = reactive({
    isLoggedIn,
    role,
    userInfo,
    saveLogin,
    clearLogin,
    getToken,
    setProfile,
  })
  return authStore
}
