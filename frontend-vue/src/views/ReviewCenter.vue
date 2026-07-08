<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listReviews, approveReview, rejectReview } from '../api/reviews'

const activeTab = ref('pending') // pending | processed
const reviews = ref([])
const total = ref(0)
const loading = ref(false)

// 0-待审核 1-已通过 2-已驳回 3-已取消
const statusClass = { 0: 'info', 1: 'success', 2: 'error', 3: 'muted' }

const reviewTarget = ref(null)
const rejectReason = ref('')
const acting = ref(false)
const error = ref('')
const successMsg = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const status = activeTab.value === 'pending' ? 0 : null
    const params = { page: 1, size: 30 }
    if (status !== null) params.status = status
    else params.statusList = '1,2,3' // 已处理：通过/驳回/取消
    const res = await listReviews(params)
    reviews.value = (res?.records || []).map((r) => {
      let submit = {}
      try { submit = r.submitData ? JSON.parse(r.submitData) : {} } catch (e) { submit = {} }
      return {
        id: r.id,
        applicantName: r.applicantName || '匿名',
        typeName: r.typeName || '审核',
        title: r.title || '',
        status: r.status,
        statusName: r.statusName || '未知',
        submitData: submit,
        reviewComment: r.reviewComment || '',
        createdAt: r.createdAt ? String(r.createdAt).substring(0, 16) : '',
      }
    })
    total.value = res?.total || 0
  } catch (e) {
    reviews.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(t) {
  activeTab.value = t
  load()
}

function openReview(r) {
  if (r.status !== 0) return
  reviewTarget.value = r
  rejectReason.value = ''
  error.value = ''
}

function closeReview() {
  reviewTarget.value = null
  rejectReason.value = ''
}

async function handleApprove() {
  if (!reviewTarget.value) return
  acting.value = true
  error.value = ''
  successMsg.value = ''
  try {
    await approveReview(reviewTarget.value.id, '')
    successMsg.value = '已通过该申请'
    closeReview()
    await load()
    setTimeout(() => { successMsg.value = '' }, 2500)
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    acting.value = false
  }
}

async function handleReject() {
  if (!reviewTarget.value) return
  if (!rejectReason.value.trim()) {
    error.value = '请填写驳回原因'
    return
  }
  acting.value = true
  error.value = ''
  try {
    await rejectReview(reviewTarget.value.id, rejectReason.value.trim())
    successMsg.value = '已驳回该申请'
    closeReview()
    await load()
    setTimeout(() => { successMsg.value = '' }, 2500)
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    acting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="content-section">
    <div class="flex-between" style="margin-bottom: 16px;">
      <h2 style="margin: 0;">审核中心</h2>
      <span class="badge badge-info">{{ total }} 条</span>
    </div>

    <div v-if="successMsg" class="form-error-banner" style="background:#f0fdf4;border-color:#86efac;color:#16a34a;">
      {{ successMsg }}
    </div>

    <div class="chip-group">
      <button class="chip" :class="{ active: activeTab === 'pending' }" @click="switchTab('pending')">待审核</button>
      <button class="chip" :class="{ active: activeTab === 'processed' }" @click="switchTab('processed')">已处理</button>
    </div>

    <div v-if="loading" class="flex-center" style="padding: 48px 0;">
      <p class="text-muted">加载中...</p>
    </div>

    <div v-else-if="reviews.length === 0" class="card card-padded" style="text-align:center;padding:56px;margin-top:16px;">
      <p class="text-muted" style="font-size:15px;">{{ activeTab === 'pending' ? '暂无待审核申请' : '暂无已处理记录' }}</p>
    </div>

    <div v-else class="content-section" style="margin-top:16px;">
      <div v-for="r in reviews" :key="r.id" class="card card-padded">
        <div class="flex-between">
          <div>
            <div style="font-size:16px;font-weight:600;">{{ r.title }}</div>
            <div class="app-meta">申请人：{{ r.applicantName }} · 类型：{{ r.typeName }} · {{ r.createdAt }}</div>
          </div>
          <span class="badge" :class="'badge-' + (statusClass[r.status] || 'info')">{{ r.statusName }}</span>
        </div>

        <div class="info-grid" style="margin-top:16px;">
          <div class="info-item"><span class="info-label">真实姓名</span><span class="info-value">{{ r.submitData.realName || '—' }}</span></div>
          <div class="info-item"><span class="info-label">身份证号</span><span class="info-value">{{ r.submitData.idCard || '—' }}</span></div>
          <div class="info-item"><span class="info-label">联系电话</span><span class="info-value">{{ r.submitData.phone || '—' }}</span></div>
          <div class="info-item"><span class="info-label">邮箱</span><span class="info-value">{{ r.submitData.email || '—' }}</span></div>
        </div>
        <div v-if="r.submitData.reason" class="app-meta" style="margin-top:12px;">申请理由：{{ r.submitData.reason }}</div>
        <div v-if="r.status !== 0 && r.reviewComment" class="app-meta" :class="{ 'text-error': r.status === 2 }" style="margin-top:8px;">
          {{ r.status === 2 ? '驳回原因' : '审核意见' }}：{{ r.reviewComment }}
        </div>

        <div v-if="r.status === 0" class="flex-row" style="margin-top:16px;">
          <button class="btn btn-success btn-sm" :disabled="acting" @click="openReview(r)">审核</button>
        </div>
      </div>
    </div>

    <!-- 审核弹层 -->
    <div v-if="reviewTarget" class="modal-mask" @click.self="closeReview">
      <div class="card card-padded modal-body">
        <h4>审核申请 · {{ reviewTarget.title }}</h4>
        <div v-if="error" class="form-error-banner">{{ error }}</div>
        <div class="input-group">
          <label>驳回原因（仅拒绝时必填）</label>
          <textarea v-model="rejectReason" class="textarea" placeholder="请说明驳回原因..."></textarea>
        </div>
        <div class="flex-row">
          <button class="btn btn-success btn-sm" :disabled="acting" @click="handleApprove">通过</button>
          <button class="btn btn-danger-outline btn-sm" :disabled="acting" @click="handleReject">拒绝</button>
          <button class="btn btn-ghost btn-sm" @click="closeReview">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-error { color: var(--accent-error); }
.form-error-banner {
  background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
  padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; text-align: center;
}
.modal-mask {
  position: fixed; inset: 0; background: rgba(31,30,27,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 100; padding: 24px;
}
.modal-body { width: 100%; max-width: 480px; }
</style>
