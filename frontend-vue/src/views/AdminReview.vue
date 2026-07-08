<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAdminApplicationDetail, adminApproveApplication, adminRejectApplication } from '../api/admin'

const router = useRouter()
const route = useRoute()
const appId = computed(() => Number(route.params.id))

const loading = ref(true)
const error = ref('')
const app = ref(null)
const reviewing = ref(false)
const reviewResult = ref(null) // { success, message }
const rejectReason = ref('')

async function loadApplication() {
  loading.value = true
  error.value = ''
  try {
    app.value = await getAdminApplicationDetail(appId.value)
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleReview(action) {
  reviewing.value = true
  reviewResult.value = null
  try {
    if (action === 'approve') {
      await adminApproveApplication(appId.value)
    } else {
      await adminRejectApplication(appId.value, rejectReason.value.trim())
    }
    reviewResult.value = {
      success: true,
      message: action === 'approve' ? '申请已通过！领养记录已创建。' : '申请已拒绝。',
      action,
    }
  } catch (e) {
    reviewResult.value = { success: false, message: e.message || '操作失败' }
  } finally {
    reviewing.value = false
  }
}

// 状态映射
const statusText = computed(() => {
  const s = app.value?.status
  if (s === 0) return '待审核'
  if (s === 1) return '审核中'
  if (s === 2) return '已通过'
  if (s === 3) return '已拒绝'
  if (s === 4) return '已取消'
  return '-'
})

onMounted(loadApplication)
</script>

<template>
  <div id="admin-review" class="page-container">
    <nav class="navbar">
      <div class="navbar-back" @click="router.back()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" />
        </svg>
        返回
      </div>
      <div class="navbar-logo-text">PetHome Admin</div>
      <div></div>
    </nav>

    <div v-if="loading" class="flex-center" style="padding: 80px 0;">
      <p class="text-muted">加载中...</p>
    </div>

    <div v-else-if="error" class="card-padded" style="text-align: center; padding: 48px; margin-top: 40px;">
      <p style="font-size: 16px; color: #dc2626; margin-bottom: 16px;">{{ error }}</p>
      <button class="btn btn-primary" @click="loadApplication">重试</button>
    </div>

    <template v-else-if="app">
      <h2 style="padding-top: 24px;">领养申请审核</h2>

      <div class="admin-two-col">
        <div class="admin-main-col">
          <div class="card-padded" style="margin-bottom: 16px;">
            <h3>申请人信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">姓名</span>
                <span class="info-value">{{ app.applicantName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">住房情况</span>
                <span class="info-value">{{ app.housingType || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">月收入</span>
                <span class="info-value">{{ app.monthlyIncome ? '¥' + app.monthlyIncome : '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">申请状态</span>
                <span class="info-value">{{ statusText }}</span>
              </div>
            </div>
          </div>

          <div class="card-padded" style="margin-bottom: 16px;">
            <h3>养宠经验</h3>
            <p style="font-size: 14px; color: #5C5A55; line-height: 1.7;">{{ app.petExperience || '暂无' }}</p>
          </div>

          <div class="card-padded">
            <h3>领养原因</h3>
            <p style="font-size: 14px; color: #5C5A55; line-height: 1.7;">{{ app.reason || '暂无' }}</p>
          </div>
        </div>

        <div class="admin-side-col">
          <div class="card-padded" style="margin-bottom: 16px;">
            <h4>领养宠物</h4>
            <div class="img-placeholder" style="width: 100%; height: 160px; font-size: 48px; margin-bottom: 12px;">🐕</div>
            <div style="font-size: 16px; font-weight: 600; color: #1F1E1B; margin-bottom: 4px;">{{ app.petName || '-' }}</div>
            <div style="font-size: 13px; color: #BFBBB6;">{{ app.petBreed || '' }}</div>
          </div>

          <!-- 审核操作 -->
          <div v-if="!reviewResult || !reviewResult.success" class="card-padded">
            <h4>审核操作</h4>

            <div class="input-group" style="margin-bottom: 12px;">
              <label>驳回原因（拒绝时填写）</label>
              <textarea v-model="rejectReason" class="textarea" placeholder="请填写驳回原因..."></textarea>
            </div>

            <div v-if="reviewResult" class="form-error-banner" style="margin-bottom: 12px;">
              {{ reviewResult.message }}
            </div>

            <button class="btn btn-success btn-lg btn-fw" style="margin-bottom: 12px;"
              :disabled="reviewing" @click="handleReview('approve')">
              {{ reviewing ? '处理中...' : '通过申请' }}
            </button>
            <button class="btn btn-danger-outline btn-lg btn-fw"
              :disabled="reviewing" @click="handleReview('reject')">
              {{ reviewing ? '处理中...' : '拒绝申请' }}
            </button>
          </div>

          <!-- 审核完成 -->
          <div v-else class="card-padded" style="text-align: center;">
            <div style="font-size: 36px; margin-bottom: 8px;">{{ reviewResult.action === 'approve' ? '✅' : '❌' }}</div>
            <p style="font-size: 14px; color: #5C5A55; margin-bottom: 16px;">{{ reviewResult.message }}</p>
            <button class="btn btn-primary btn-sm" @click="router.back()">返回列表</button>
          </div>
        </div>
      </div>
    </template>
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
  text-align: center;
}
</style>
