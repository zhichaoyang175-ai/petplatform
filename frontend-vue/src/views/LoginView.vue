<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, register } from '../api/auth'
import { useAuthStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const tab = ref('login')
const loading = ref(false)
const errorMsg = ref('')

// ---- 登录表单 ----
const loginForm = reactive({
  phone: '',
  password: '',
})

// ---- 注册表单 ----
const registerForm = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  role: 1, // 1-领养人 2-送养人
})

// ---- 表单校验 ----
const loginErrors = reactive({})
const registerErrors = reactive({})

function validateLogin() {
  Object.keys(loginErrors).forEach((k) => delete loginErrors[k])
  if (!loginForm.phone) {
    loginErrors.phone = '请输入手机号'
  } else if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    loginErrors.phone = '手机号格式不正确'
  }
  if (!loginForm.password) {
    loginErrors.password = '请输入密码'
  }
  return Object.keys(loginErrors).length === 0
}

function validateRegister() {
  Object.keys(registerErrors).forEach((k) => delete registerErrors[k])
  if (!registerForm.phone) {
    registerErrors.phone = '请输入手机号'
  } else if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    registerErrors.phone = '手机号格式不正确'
  }
  if (!registerForm.password) {
    registerErrors.password = '请输入密码'
  } else if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    registerErrors.password = '密码长度需在6-20位之间'
  }
  if (!registerForm.confirmPassword) {
    registerErrors.confirmPassword = '请再次输入密码'
  } else if (registerForm.confirmPassword !== registerForm.password) {
    registerErrors.confirmPassword = '两次输入的密码不一致'
  }
  if (!registerForm.nickname) {
    registerErrors.nickname = '请输入昵称'
  } else if (registerForm.nickname.length < 2 || registerForm.nickname.length > 20) {
    registerErrors.nickname = '昵称长度需在2-20位之间'
  }
  if (!registerForm.role) {
    registerErrors.role = '请选择角色'
  }
  return Object.keys(registerErrors).length === 0
}

// ---- 提交：登录 ----
async function handleLogin() {
  errorMsg.value = ''
  if (!validateLogin()) return
  loading.value = true
  try {
    const data = await login(loginForm.phone, loginForm.password)
    authStore.saveLogin(data)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ---- 提交：注册 ----
async function handleRegister() {
  errorMsg.value = ''
  if (!validateRegister()) return
  loading.value = true
  try {
    const data = await register(
      registerForm.phone,
      registerForm.password,
      registerForm.nickname,
      registerForm.role,
    )
    authStore.saveLogin(data)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    errorMsg.value = err.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ---- 切换 Tab 时清空错误 ----
function switchTab(t) {
  tab.value = t
  errorMsg.value = ''
  Object.keys(loginErrors).forEach((k) => delete loginErrors[k])
  Object.keys(registerErrors).forEach((k) => delete registerErrors[k])
}

const canSubmit = computed(() => !loading.value)
</script>

<template>
  <div id="login" class="page-container flex-center" style="min-height: 100vh;">
    <div class="flex-row" style="gap: 80px; width: 100%; max-width: 1000px; padding: 40px;">
      <!-- Left Brand -->
      <div style="width: 450px; flex-shrink: 0;">
        <h1 style="font-size: 36px; margin-bottom: 8px; color: #E07856;">PetHome</h1>
        <p style="font-size: 15px; color: #5C5A55; margin-bottom: 32px; line-height: 1.6;">
          宠物领养平台，连接每一个爱心家庭与等待温暖的毛孩子。
        </p>
        <div class="auth-features">
          <div class="auth-feature">
            <div class="auth-feature-icon" style="background: #E07856;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
              </svg>
            </div>
            <div class="auth-feature-text">
              <strong>爱心领养</strong>
              经过严格审核的救助站，确保宠物健康安全
            </div>
          </div>
          <div class="auth-feature">
            <div class="auth-feature-icon" style="background: #87A878;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </div>
            <div class="auth-feature-text">
              <strong>全程跟踪</strong>
              完善的领养回访机制，关怀每一只毛孩子
            </div>
          </div>
          <div class="auth-feature">
            <div class="auth-feature-icon" style="background: #D4A574;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="16" x2="12" y2="12" />
                <line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
            </div>
            <div class="auth-feature-text">
              <strong>领养知识</strong>
              丰富的养宠知识和领养指南，助你成为合格铲屎官
            </div>
          </div>
        </div>
      </div>

      <!-- Right Form -->
      <div class="card-padded" style="width: 440px; flex-shrink: 0;">
        <div class="tab-group">
          <button
            class="tab-btn"
            :class="{ active: tab === 'login', pending: tab !== 'login' }"
            @click="switchTab('login')"
          >
            登录
          </button>
          <button
            class="tab-btn"
            :class="{ active: tab === 'register', pending: tab !== 'register' }"
            @click="switchTab('register')"
          >
            注册
          </button>
        </div>

        <!-- 全局错误提示 -->
        <div v-if="errorMsg" class="form-error-banner">
          {{ errorMsg }}
        </div>

        <!-- Login -->
        <div v-if="tab === 'login'">
          <form @submit.prevent="handleLogin">
            <div class="input-group">
              <label>手机号</label>
              <input
                v-model.trim="loginForm.phone"
                class="input-field"
                type="tel"
                placeholder="请输入手机号"
                maxlength="11"
              />
              <span v-if="loginErrors.phone" class="field-error">{{ loginErrors.phone }}</span>
            </div>
            <div class="input-group">
              <label>密码</label>
              <input
                v-model="loginForm.password"
                class="input-field"
                type="password"
                placeholder="请输入密码"
              />
              <span v-if="loginErrors.password" class="field-error">{{ loginErrors.password }}</span>
            </div>
            <div style="margin-bottom: 16px;">
              <a href="#" class="text-primary-accent" style="font-size: 13px;">忘记密码？</a>
            </div>
            <button type="submit" class="btn btn-primary btn-lg btn-fw" :disabled="!canSubmit">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
          <div class="divider-or">其他方式登录</div>
          <div class="social-row">
            <button class="social-btn" type="button">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z" />
              </svg>
              WX
            </button>
            <button class="social-btn" type="button">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="1" y="3" width="15" height="18" rx="2" />
                <line x1="6" y1="16" x2="6.01" y2="16" />
                <line x1="11" y1="16" x2="18" y2="16" />
              </svg>
              SMS
            </button>
          </div>
        </div>

        <!-- Register -->
        <div v-else>
          <form @submit.prevent="handleRegister">
            <div class="input-group">
              <label>手机号</label>
              <input
                v-model.trim="registerForm.phone"
                class="input-field"
                type="tel"
                placeholder="请输入手机号"
                maxlength="11"
              />
              <span v-if="registerErrors.phone" class="field-error">{{ registerErrors.phone }}</span>
            </div>
            <div class="input-group">
              <label>昵称</label>
              <input
                v-model.trim="registerForm.nickname"
                class="input-field"
                type="text"
                placeholder="请输入昵称（2-20位）"
                maxlength="20"
              />
              <span v-if="registerErrors.nickname" class="field-error">{{ registerErrors.nickname }}</span>
            </div>
            <div class="input-group">
              <label>密码</label>
              <input
                v-model="registerForm.password"
                class="input-field"
                type="password"
                placeholder="请输入密码（6-20位）"
              />
              <span v-if="registerErrors.password" class="field-error">{{ registerErrors.password }}</span>
            </div>
            <div class="input-group">
              <label>确认密码</label>
              <input
                v-model="registerForm.confirmPassword"
                class="input-field"
                type="password"
                placeholder="请再次输入密码"
              />
              <span v-if="registerErrors.confirmPassword" class="field-error">{{ registerErrors.confirmPassword }}</span>
            </div>
            <div class="input-group">
              <label>注册身份</label>
              <div class="role-select">
                <label class="role-option" :class="{ active: registerForm.role === 1 }">
                  <input v-model="registerForm.role" type="radio" :value="1" />
                  <span>领养人</span>
                </label>
                <label class="role-option" :class="{ active: registerForm.role === 2 }">
                  <input v-model="registerForm.role" type="radio" :value="2" />
                  <span>送养人</span>
                </label>
              </div>
              <span v-if="registerErrors.role" class="field-error">{{ registerErrors.role }}</span>
            </div>
            <button type="submit" class="btn btn-primary btn-lg btn-fw" :disabled="!canSubmit">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>
          <div class="divider-or">其他方式注册</div>
          <div class="social-row">
            <button class="social-btn" type="button">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z" />
              </svg>
              WX
            </button>
            <button class="social-btn" type="button">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="1" y="3" width="15" height="18" rx="2" />
                <line x1="6" y1="16" x2="6.01" y2="16" />
                <line x1="11" y1="16" x2="18" y2="16" />
              </svg>
              SMS
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 16px;
  text-align: center;
}
.field-error {
  display: block;
  color: #dc2626;
  font-size: 12px;
  margin-top: 4px;
}
.role-select {
  display: flex;
  gap: 12px;
}
.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.role-option input {
  display: none;
}
.role-option.active {
  border-color: #E07856;
  background: #fef3ef;
  color: #E07856;
  font-weight: 600;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
