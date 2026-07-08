<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import { useAuthStore } from '../stores/user'
import {
  createEvaluation,
  getEvaluations,
  getCredit,
  getMyCredit,
} from '../api/evaluations'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const userId = computed(() => authStore.userInfo?.userId || null)
const currentUserName = computed(() => authStore.userInfo?.nickname || '用户')

const navLinks = [
  { text: '首页', to: '/' },
  { text: '浏览宠物', to: '/#pets' },
  { text: '我的', to: '/profile/applications' },
]

const targetTypeMap = { 1: '用户', 2: '救助站' }

// ────────── 我的信用分 ──────────
const myCredit = ref(null)
const myCreditLoading = ref(false)

// ────────── 目标（来自 URL query） ──────────
const qTargetType = route.query.targetType ? Number(route.query.targetType) : null
const qTargetId = route.query.targetId ? Number(route.query.targetId) : null
const hasTarget = computed(() => qTargetType != null && qTargetId != null)

const targetCredit = ref(null)
const evaluations = ref([])
const listLoading = ref(false)
const listTotal = ref(0)

// ────────── 评价表单 ──────────
const form = reactive({
  targetType: qTargetType || 2,
  targetId: qTargetId || '',
  score: 5,
  comment: '',
})
const submitting = ref(false)
const error = ref('')
const success = ref(false)

async function loadMyCredit() {
  myCreditLoading.value = true
  try {
    const res = await getMyCredit()
    myCredit.value = res || null
  } catch (e) {
    myCredit.value = null
  } finally {
    myCreditLoading.value = false
  }
}

async function loadTarget() {
  if (!hasTarget.value) return
  form.targetType = qTargetType
  form.targetId = qTargetId
  try {
    const credit = await getCredit({ targetType: qTargetType, targetId: qTargetId })
    targetCredit.value = credit || null
  } catch (e) {
    targetCredit.value = null
  }
  await loadEvaluations()
}

async function loadEvaluations() {
  if (!hasTarget.value) return
  listLoading.value = true
  try {
    const res = await getEvaluations({
      targetType: qTargetType,
      targetId: qTargetId,
      page: 1,
      size: 30,
    })
    evaluations.value = (res?.records || []).map((e) => ({
      id: e.id,
      reviewerName: e.reviewerName || '匿名用户',
      targetTypeName: e.targetTypeName || targetTypeMap[e.targetType] || '',
      score: e.score || 0,
      comment: e.comment || '',
      createdAt: e.createdAt ? String(e.createdAt).substring(0, 16) : '',
    }))
    listTotal.value = res?.total || 0
  } catch (e) {
    evaluations.value = []
    listTotal.value = 0
  } finally {
    listLoading.value = false
  }
}

function scoreStars(score) {
  const s = Number(score) || 0
  return '★'.repeat(s) + '☆'.repeat(5 - s)
}

async function handleSubmit() {
  error.value = ''
  success.value = false
  if (!form.targetType) {
    error.value = '请选择评价目标类型'
    return
  }
  if (!form.targetId) {
    error.value = '请填写评价目标ID'
    return
  }
  if (!form.score || form.score < 1 || form.score > 5) {
    error.value = '评分必须在 1-5 之间'
    return
  }
  submitting.value = true
  try {
    await createEvaluation({
      targetType: Number(form.targetType),
      targetId: Number(form.targetId),
      score: Number(form.score),
      comment: form.comment.trim(),
    })
    success.value = true
    form.comment = ''
    if (hasTarget.value) {
      await loadTarget()
    }
    await loadMyCredit()
    setTimeout(() => { success.value = false }, 3000)
  } catch (e) {
    error.value = e.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

function viewTarget(type, id) {
  router.push(`/evaluations?targetType=${type}&targetId=${id}`)
}

onMounted(() => {
  loadMyCredit()
  loadTarget()
})
</script>

<template>
  <div id="evaluations" class="page-container">
    <AppNavbar :nav-links="navLinks" :show-actions="false" />

    <div class="content-section">
      <div class="flex-between" style="margin-bottom: 16px;">
        <h2 style="margin-bottom: 0;">评价 / 信用</h2>
        <RouterLink to="/profile/applications" class="btn btn-outline btn-sm">返回我的</RouterLink>
      </div>

      <!-- 我的信用分 -->
      <div class="card card-padded">
        <div class="flex-between">
          <h3 style="margin: 0;">我的信用分</h3>
          <span class="tag tag-warm">{{ currentUserName }}（用户）</span>
        </div>
        <div v-if="myCreditLoading" class="text-muted" style="margin-top: 12px;">加载中...</div>
        <div v-else-if="myCredit" class="info-grid" style="margin-top: 16px;">
          <div class="info-item">
            <span class="info-label">平均分</span>
            <span class="info-value" style="font-size: 22px; color: var(--accent-primary);">
              {{ myCredit.avgScore != null ? myCredit.avgScore : '0.00' }}
              <span style="font-size: 13px; color: var(--text-muted);"> / 5</span>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">评价数</span>
            <span class="info-value">{{ myCredit.count || 0 }}</span>
          </div>
        </div>
        <div v-else class="text-muted" style="margin-top: 12px;">暂无信用分数据</div>
      </div>

      <!-- 目标信用分（来自 URL query） -->
      <div v-if="hasTarget" class="card card-padded" style="margin-top: 16px;">
        <div class="flex-between">
          <h3 style="margin: 0;">
            目标信用分 · {{ targetTypeMap[qTargetType] }} #{{ qTargetId }}
          </h3>
          <span class="tag tag-warm">{{ targetCredit?.targetTypeName || targetTypeMap[qTargetType] }}</span>
        </div>
        <div v-if="targetCredit" class="info-grid" style="margin-top: 16px;">
          <div class="info-item">
            <span class="info-label">平均分</span>
            <span class="info-value" style="font-size: 22px; color: var(--accent-primary);">
              {{ targetCredit.avgScore != null ? targetCredit.avgScore : '0.00' }}
              <span style="font-size: 13px; color: var(--text-muted);"> / 5</span>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">评价数</span>
            <span class="info-value">{{ targetCredit.count || 0 }}</span>
          </div>
        </div>
        <div v-else class="text-muted" style="margin-top: 12px;">暂无信用分数据</div>

        <!-- 目标评价列表 -->
        <h4 style="margin-top: 24px;">全部评价（{{ listTotal }}）</h4>
        <div v-if="listLoading" class="text-muted">加载中...</div>
        <div v-else-if="evaluations.length === 0" class="text-muted">暂无评价</div>
        <div v-else>
          <div v-for="e in evaluations" :key="e.id" class="card card-padded app-card">
            <div class="app-card">
              <div class="app-content" style="width: 100%;">
                <div class="flex-between">
                  <div class="app-name">{{ e.reviewerName }}</div>
                  <span class="badge badge-primary">{{ scoreStars(e.score) }} {{ e.score }}</span>
                </div>
                <div v-if="e.comment" class="app-meta" style="margin-top: 6px;">{{ e.comment }}</div>
                <div class="app-meta text-muted">{{ e.createdAt }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价他人 / 救助站 -->
      <div class="card card-padded" style="margin-top: 16px;">
        <h3 style="margin-top: 0;">评价他人 / 救助站</h3>
        <div v-if="success" class="form-error-banner" style="background: #f0fdf4; border-color: #86efac; color: #16a34a;">
          评价提交成功
        </div>
        <div v-if="error" class="form-error-banner">{{ error }}</div>

        <div class="input-group">
          <label>评价目标类型</label>
          <div class="chip-group">
            <button type="button" class="chip" :class="{ active: form.targetType === 1 }"
              @click="form.targetType = 1">用户</button>
            <button type="button" class="chip" :class="{ active: form.targetType === 2 }"
              @click="form.targetType = 2">救助站</button>
          </div>
        </div>

        <div class="input-group">
          <label>评价目标ID</label>
          <input v-model="form.targetId" class="input-field" type="number" placeholder="请输入用户ID或救助站ID" />
        </div>

        <div class="input-group">
          <label>评分（1-5）</label>
          <div class="chip-group">
            <button v-for="n in 5" :key="n" type="button" class="chip"
              :class="{ active: form.score === n }" @click="form.score = n">
              {{ n }} 分
            </button>
          </div>
        </div>

        <div class="input-group">
          <label>评价内容（选填）</label>
          <textarea v-model="form.comment" class="textarea" placeholder="说说你的评价..."></textarea>
        </div>

        <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
        <p class="text-muted" style="font-size: 12px; margin-top: 10px;">
          提示：在 URL 后附加 <code>?targetType=1&amp;targetId=123</code> 可查看指定用户/救助站的信用分与评价。
        </p>
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
</style>
