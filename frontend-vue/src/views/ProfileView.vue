<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import AppBadge from '../components/AppBadge.vue'
import ReviewCenter from './ReviewCenter.vue'
import { useAuthStore } from '../stores/user'
import { logout } from '../api/auth'
import {
  getMyApplications,
  getReceivedApplications,
  reviewApplication,
} from '../api/applications'
import { getFavorites } from '../api/favorites'
import { getProfile, updateProfile, submitAdopterApplication, getAdopterApplication } from '../api/users'
// 新增：内联 tab 所需的 API
import {
  getNotifications,
  markAsRead,
  markAllRead,
  getUnreadCount,
} from '../api/notifications'
import { getMyAdoptions } from '../api/adoptions'
import { getMyPets } from '../api/pets'
import {
  getMyTasks,
  getTaskDetail,
  submitFollowUp,
  getFollowUpRecords,
} from '../api/followUps'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const roleMap = { 1: '领养人', 2: '送养人', 3: '管理员', 4: '审核员' }
const roleName = computed(() => roleMap[authStore.userInfo?.role] || '未知')
const isApplicant = computed(() => authStore.userInfo?.role === 1)
const isReviewer = computed(() => authStore.userInfo?.role === 4)
const displayName = computed(() => authStore.userInfo?.nickname || '用户')
const avatarLetter = computed(() => displayName.value.charAt(0).toUpperCase())
const maskedPhone = computed(() => {
  const p = authStore.userInfo?.phone || ''
  return p ? p.substring(0, 3) + '****' + p.substring(7) : ''
})

const navLinks = [
  { text: '首页', to: '/' },
  { text: '浏览宠物', to: '/#pets' },
  { text: '我的', to: '/profile/applications' },
]

const activeTab = computed(() => route.params.tab || 'applications')

// 全部入口统一为 tab 内联切换（含原本跳独立页面的账户中心入口）
const menuItems = computed(() => {
  const items = [
    { key: 'applications', label: '我的申请', to: '/profile/applications' },
    { key: 'favorites', label: '我的收藏', to: '/profile/favorites' },
    { key: 'settings', label: '个人设置', to: '/profile/settings' },
    { key: 'notifications', label: '通知', to: '/profile/notifications' },
    { key: 'adoptions', label: '领养记录', to: '/profile/adoptions' },
    { key: 'my-pets', label: '我送养的宠物', to: '/profile/my-pets' },
    { key: 'follow-ups', label: '回访任务', to: '/profile/follow-ups' },
    { key: 'received', label: '收到的申请', to: '/profile/received' },
  ]
  if (isReviewer.value) items.push({ key: 'review', label: '等待审核申请', to: '/profile/review' })
  return items
})

async function handleLogout() {
  try { await logout() } catch (e) { /* ignore */ }
  authStore.clearLogin()
  router.push('/')
}

function goDetail(petId) {
  router.push(`/detail/${petId}`)
}

// ---- 我的申请（API） ----
const applications = ref([])
const appsLoading = ref(false)

async function loadApplications() {
  appsLoading.value = true
  try {
    const res = await getMyApplications({ page: 1, size: 20 })
    applications.value = (res?.records || []).map(a => ({
      appId: a.id,
      petId: a.petId,
      petName: a.petName || '未知',
      breed: a.petBreed || '',
      emoji: a.petEmoji || '🐕',
      date: a.createdAt ? a.createdAt.substring(0, 10) : '',
      status: a.statusName || '未知',
      statusType: statusTypeMap[a.status] || 'info',
    }))
  } catch (e) { applications.value = [] }
  finally { appsLoading.value = false }
}

// ---- 我的收藏（API） ----
const favorites = ref([])
const favLoading = ref(false)

async function loadFavorites() {
  favLoading.value = true
  try {
    const res = await getFavorites({ page: 1, size: 20 })
    favorites.value = (res?.records || []).map(f => ({
      favId: f.id,
      petId: f.id,
      petName: f.name || '未知',
      breed: f.breed || '',
      emoji: getEmoji(f.breed),
      date: f.createdAt ? f.createdAt.substring(0, 10) : '',
      status: f.statusName || '可领养',
      statusType: 'success',
    }))
  } catch (e) { favorites.value = [] }
  finally { favLoading.value = false }
}

const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'error', 4: 'error' }

function getEmoji(breed) {
  if (!breed) return '🐕'
  if (breed.includes('猫')) return '🐱'
  return '🐕'
}

// ---- 申请送养人（提交资料进入审核流程） ----
const showApplyModal = ref(false)
const applyForm = reactive({ realName: '', idCard: '', phone: '', email: '', reason: '' })
const applySubmitting = ref(false)
const applyError = ref('')
const applyStatus = ref(null) // 当前认证申请 ReviewTaskVO

function adopterStatusClass(status) {
  // 0-待审核 1-已通过 2-已驳回 3-已取消
  return { 0: 'info', 1: 'success', 2: 'error', 3: 'muted' }[status] || 'info'
}

async function loadAdopterApplication() {
  if (!isApplicant.value) return
  try {
    const res = await getAdopterApplication()
    applyStatus.value = res || null
  } catch (e) {
    applyStatus.value = null
  }
}

function openApplyModal() {
  applyError.value = ''
  applyForm.realName = ''
  applyForm.idCard = ''
  applyForm.phone = ''
  applyForm.email = ''
  applyForm.reason = ''
  showApplyModal.value = true
}

async function handleSubmitApplication() {
  applyError.value = ''
  if (!applyForm.realName.trim()) { applyError.value = '请填写真实姓名'; return }
  if (!applyForm.idCard.trim()) { applyError.value = '请填写身份证号'; return }
  if (!/^\d{15}$|^\d{17}[\dXx]$/.test(applyForm.idCard.trim())) { applyError.value = '身份证号格式不正确'; return }
  if (!applyForm.phone.trim()) { applyError.value = '请填写联系电话'; return }
  applySubmitting.value = true
  try {
    const res = await submitAdopterApplication({ ...applyForm })
    applyStatus.value = res
    showApplyModal.value = false
    alert('认证申请已提交，请等待审核结果。')
  } catch (e) {
    applyError.value = e.message || '提交失败'
  } finally {
    applySubmitting.value = false
  }
}

// ---- 通知 tab ----
const notifList = ref([])
const notifTotal = ref(0)
const notifLoading = ref(false)
const unreadCount = ref(0)
// '' 全部 / '0' 未读 / '1' 已读
const notifFilterRead = ref('')

async function loadNotifications() {
  notifLoading.value = true
  try {
    const params = { page: 1, size: 30 }
    if (notifFilterRead.value !== '') params.readStatus = Number(notifFilterRead.value)
    const res = await getNotifications(params)
    notifList.value = (res?.records || []).map((n) => ({
      id: n.id,
      title: n.title || '通知',
      content: n.content || '',
      type: n.type,
      typeName: n.typeName || '',
      readStatus: !!n.readStatus,
      refType: n.refType,
      refId: n.refId,
      createdAt: n.createdAt ? String(n.createdAt).substring(0, 16) : '',
    }))
    notifTotal.value = res?.total || 0
  } catch (e) {
    notifList.value = []
  } finally {
    notifLoading.value = false
  }
}

// 响应拦截器把 R.ok(count) 拆成 inner data，故 res 即 { count }
async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res?.count || 0
  } catch (e) {
    unreadCount.value = 0
  }
}

async function handleMarkRead(id) {
  try {
    await markAsRead(id)
    const item = notifList.value.find((n) => n.id === id)
    if (item) item.readStatus = true
    loadUnreadCount()
  } catch (e) { /* 忽略 */ }
}

async function handleMarkAll() {
  try {
    await markAllRead()
    notifList.value.forEach((n) => (n.readStatus = true))
    unreadCount.value = 0
  } catch (e) { /* 忽略 */ }
}

function switchNotifFilter(v) {
  notifFilterRead.value = v
  loadNotifications()
}

function handleNotifClick(n) {
  if (n.refType === 'application' && n.refId) {
    router.push('/profile/received')
  } else if (n.refType === 'pet' && n.refId) {
    router.push(`/detail/${n.refId}`)
  } else if (n.refType === 'review' && n.refId) {
    router.push('/review-center')
  }
  if (!n.readStatus) handleMarkRead(n.id)
}

// ---- 领养记录 tab（我的领养，领养人视角） ----
const adoptRecords = ref([])
const adoptTotal = ref(0)
const adoptLoading = ref(false)
// 0-领养中 1-回访中 2-已完成
const adoptStatusClass = { 0: 'info', 1: 'warning', 2: 'success' }

// sent：我送养的宠物（来自 /api/v1/pets/my，PetVO）
const sentPets = ref([])
const sentLoading = ref(false)
// PetVO.status 含义：0-草稿 1-待审核 2-已上架 3-已领养 4-已完成 5-已下架 6-已拒绝
const sentStatusClass = { 0: 'info', 1: 'warning', 2: 'success', 3: 'success', 4: 'success', 5: 'success', 6: 'muted' }

async function loadAdoptionRecords() {
  adoptLoading.value = true
  try {
    const res = await getMyAdoptions({ page: 1, size: 30 })
    adoptRecords.value = (res?.records || []).map((r) => ({
      id: r.id,
      petName: r.petName || '未知',
      petBreed: r.petBreed || '',
      adopterName: r.adopterName || '',
      applicantName: r.applicantName || '',
      adoptedAt: r.adoptedAt ? String(r.adoptedAt).substring(0, 10) : '',
      status: r.status,
      statusName: r.statusName || '未知',
      followUpMonths: r.followUpMonths || 0,
    }))
    adoptTotal.value = res?.total || 0
  } catch (e) {
    adoptRecords.value = []
  } finally {
    adoptLoading.value = false
  }
}

// 我送养的宠物（来自 /api/v1/pets/my，PetVO）
async function loadMyPets() {
  sentLoading.value = true
  try {
    const res = await getMyPets({ page: 1, size: 30 })
    sentPets.value = (res?.records || []).map((p) => ({
      id: p.id,
      name: p.name || '未命名',
      breed: p.breed || '',
      coverImage: p.coverImage || '',
      status: p.status,
      statusName: p.statusName || '未知',
      createdAt: p.createdAt ? String(p.createdAt).substring(0, 10) : '',
    }))
  } catch (e) {
    sentPets.value = []
  } finally {
    sentLoading.value = false
  }
}

// ---- 回访任务 tab ----
const followTasks = ref([])
const followTotal = ref(0)
const followLoading = ref(false)
const followSelectedTask = ref(null)
const followTaskLoading = ref(false)
const followSubmitting = ref(false)
const followSubmitError = ref('')
const followSubmitSuccess = ref(false)
const followHistory = ref([])
const followHistoryLoading = ref(false)
const followForm = ref({ content: '', imageUrls: '' })
// 0-待执行 1-已提醒 2-已完成 3-已逾期
const followStatusClass = { 0: 'info', 1: 'warning', 2: 'success', 3: 'error' }

async function loadFollowTasks() {
  followLoading.value = true
  try {
    const res = await getMyTasks({ page: 1, size: 30 })
    followTasks.value = (res?.records || []).map((t) => ({
      id: t.id,
      adoptionRecordId: t.adoptionRecordId,
      petName: t.petName || '未知',
      periodNumber: t.periodNumber,
      scheduledDate: t.scheduledDate ? String(t.scheduledDate).substring(0, 10) : '',
      dueDate: t.dueDate ? String(t.dueDate).substring(0, 10) : '',
      status: t.status,
      statusName: t.statusName || '未知',
    }))
    followTotal.value = res?.total || 0
  } catch (e) {
    followTasks.value = []
  } finally {
    followLoading.value = false
  }
}

async function selectFollowTask(task) {
  followSelectedTask.value = null
  followForm.value = { content: '', imageUrls: '' }
  followSubmitError.value = ''
  followSubmitSuccess.value = false
  followHistory.value = []
  followTaskLoading.value = true
  const recordId = task.adoptionRecordId
  try {
    const detail = await getTaskDetail(task.id)
    followSelectedTask.value = {
      id: detail?.id ?? task.id,
      adoptionRecordId: detail?.adoptionRecordId ?? task.adoptionRecordId,
      petName: detail?.petName || task.petName,
      periodNumber: detail?.periodNumber ?? task.periodNumber,
      scheduledDate: detail?.scheduledDate ? String(detail.scheduledDate).substring(0, 10) : task.scheduledDate,
      dueDate: detail?.dueDate ? String(detail.dueDate).substring(0, 10) : task.dueDate,
      status: detail?.status ?? task.status,
      statusName: detail?.statusName || task.statusName,
    }
  } catch (e) {
    // 详情获取失败时回退到列表数据
    followSelectedTask.value = { ...task }
  } finally {
    followTaskLoading.value = false
  }
  if (recordId) loadFollowHistory(recordId)
}

async function loadFollowHistory(adoptionRecordId) {
  followHistoryLoading.value = true
  try {
    const res = await getFollowUpRecords({ adoptionRecordId })
    followHistory.value = (res?.records || []).map((h) => ({
      id: h.id,
      taskId: h.taskId,
      content: h.content || '',
      imageUrl: h.imageUrl || '',
      submittedAt: h.submittedAt ? String(h.submittedAt).substring(0, 16) : '',
    }))
  } catch (e) {
    followHistory.value = []
  } finally {
    followHistoryLoading.value = false
  }
}

async function handleSubmitFollowUp() {
  if (!followSelectedTask.value) return
  if (!followForm.value.content.trim()) {
    followSubmitError.value = '请填写回访描述'
    return
  }
  followSubmitting.value = true
  followSubmitError.value = ''
  followSubmitSuccess.value = false
  try {
    const imageUrls = followForm.value.imageUrls
      .split(/[\n,]/)
      .map((s) => s.trim())
      .filter(Boolean)
    await submitFollowUp(followSelectedTask.value.id, {
      content: followForm.value.content.trim(),
      imageUrls,
    })
    followSubmitSuccess.value = true
    await loadFollowTasks()
    if (followSelectedTask.value.adoptionRecordId) loadFollowHistory(followSelectedTask.value.adoptionRecordId)
    setTimeout(() => { followSubmitSuccess.value = false }, 3000)
  } catch (e) {
    followSubmitError.value = e.message || '提交失败，请稍后重试'
  } finally {
    followSubmitting.value = false
  }
}

// ---- 收到的申请 tab ----
// 路由守卫已拦截非送养人(role!=2)，这里做二次兜底提示
const recvNoPermission = computed(() => authStore.userInfo?.role !== 2)
const recvApps = ref([])
const recvTotal = ref(0)
const recvLoading = ref(false)
// '' 全部 / 0-待审核 / 1-审核中 / 2-已通过 / 3-已驳回
const recvStatusFilter = ref('')
const recvStatusClass = { 0: 'info', 1: 'warning', 2: 'success', 3: 'error', 4: 'error' }
const recvReviewTarget = ref(null)
const recvRejectReason = ref('')
const recvReviewing = ref(false)
const recvReviewError = ref('')

async function loadReceivedApps() {
  recvLoading.value = true
  try {
    const params = { page: 1, size: 30 }
    if (recvStatusFilter.value !== '') params.status = Number(recvStatusFilter.value)
    const res = await getReceivedApplications(params)
    recvApps.value = (res?.records || []).map((a) => ({
      id: a.id,
      petId: a.petId,
      petName: a.petName || '未知',
      petBreed: a.petBreed || '',
      petEmoji: a.petEmoji || '🐾',
      applicantId: a.applicantId,
      applicantName: a.applicantName || '匿名',
      housingTypeName: a.housingTypeName || '',
      petExperienceName: a.petExperienceName || '',
      monthlyIncome: a.monthlyIncome,
      familyAttitude: a.familyAttitude || '',
      currentPets: a.currentPets || '',
      reason: a.reason || '',
      status: a.status,
      statusName: a.statusName || '未知',
      rejectReason: a.rejectReason || '',
      createdAt: a.createdAt ? String(a.createdAt).substring(0, 16) : '',
    }))
    recvTotal.value = res?.total || 0
  } catch (e) {
    recvApps.value = []
  } finally {
    recvLoading.value = false
  }
}

function switchRecvStatusFilter(v) {
  recvStatusFilter.value = v
  loadReceivedApps()
}

function openRecvReview(app) {
  if (app.status !== 0) return // 仅待审核可审核
  recvReviewTarget.value = app
  recvRejectReason.value = ''
  recvReviewError.value = ''
}

function closeRecvReview() {
  recvReviewTarget.value = null
  recvRejectReason.value = ''
}

async function handleRecvReview(action) {
  if (!recvReviewTarget.value) return
  if (action === 'reject' && !recvRejectReason.value.trim()) {
    recvReviewError.value = '请填写驳回原因'
    return
  }
  recvReviewing.value = true
  recvReviewError.value = ''
  try {
    await reviewApplication(recvReviewTarget.value.id, action, recvRejectReason.value.trim())
    const idx = recvApps.value.findIndex((a) => a.id === recvReviewTarget.value.id)
    if (idx !== -1) {
      recvApps.value[idx] = {
        ...recvApps.value[idx],
        status: action === 'approve' ? 2 : 3,
        statusName: action === 'approve' ? '已通过' : '已驳回',
        rejectReason: action === 'reject' ? recvRejectReason.value.trim() : '',
      }
    }
    closeRecvReview()
  } catch (e) {
    recvReviewError.value = e.message || '操作失败，请稍后重试'
  } finally {
    recvReviewing.value = false
  }
}

// ---- tab 数据统一加载入口 ----
function loadTabData(tab) {
  switch (tab) {
    case 'applications':
      loadApplications()
      break
    case 'favorites':
      loadFavorites()
      break
    case 'notifications':
      loadNotifications()
      loadUnreadCount()
      break
    case 'adoptions':
      loadAdoptionRecords()
      break
    case 'my-pets':
      loadMyPets()
      break
    case 'follow-ups':
      loadFollowTasks()
      break
    case 'received':
      loadReceivedApps()
      break
  }
}

onMounted(() => {
  loadProfile()
  loadTabData(activeTab.value)
  loadAdopterApplication()
})

// 拉取最新个人信息（补全 phone/email/nickname）；未登录或后端不在线时静默失败
async function loadProfile() {
  try {
    const res = await getProfile()
    if (res) authStore.setProfile(res)
  } catch (e) {
    /* 忽略 */
  }
}

watch(activeTab, (tab) => {
  // 切换到某个 tab 时按需加载其数据
  loadTabData(tab)
})

// ---- 个人设置 ----
const settingsSaving = ref(false)
const settingsSaved = ref(false)
const settingsError = ref('')

const settingsForm = reactive({
  nickname: '',
  email: '',
})

// 每当 userInfo 变化时同步到表单（包括首次加载）
function syncSettings() {
  settingsForm.nickname = authStore.userInfo?.nickname || ''
  settingsForm.email = authStore.userInfo?.email || ''
}
syncSettings()

// 个人资料加载/更新后，同步到设置表单（含 onMounted 中 loadProfile 的回填）
watch(() => authStore.userInfo, () => { syncSettings() }, { deep: true })

async function handleSaveSettings() {
  settingsError.value = ''
  settingsSaved.value = false

  if (!settingsForm.nickname.trim()) {
    settingsError.value = '昵称不能为空'
    return
  }
  if (settingsForm.nickname.trim().length < 2) {
    settingsError.value = '昵称长度至少2位'
    return
  }

  settingsSaving.value = true
  try {
    // 先调用后端更新接口
    const res = await updateProfile({
      nickname: settingsForm.nickname.trim(),
      email: settingsForm.email.trim(),
    })
    // 用后端返回的最新数据更新 store（若后端未返回则本地兜底）
    if (res) {
      authStore.setProfile(res)
    } else if (authStore.userInfo) {
      authStore.userInfo.nickname = settingsForm.nickname.trim()
      authStore.userInfo.email = settingsForm.email.trim()
      localStorage.setItem('userInfo', JSON.stringify(authStore.userInfo))
    }
    settingsSaved.value = true
    settingsError.value = ''
    setTimeout(() => { settingsSaved.value = false }, 3000)
  } catch (e) {
    settingsError.value = e.message || '保存失败，请稍后重试'
  } finally {
    settingsSaving.value = false
  }
}
</script>

<template>
  <div id="profile" class="page-container">
    <AppNavbar :nav-links="navLinks" :show-actions="false" />

    <div class="two-col">
      <div class="col-side-sm">
        <div class="profile-sidebar">
          <div class="card-padded">
            <div class="profile-user-card">
              <div class="profile-avatar">{{ avatarLetter }}</div>
              <div class="profile-name">{{ displayName }}</div>
              <div class="profile-role">{{ roleName }}</div>
              <div class="profile-phone">{{ maskedPhone }}</div>
            </div>
          </div>
          <!-- 申请送养人（提交审核） -->
          <div class="card-padded" v-if="isApplicant" style="margin-top: 12px;">
            <template v-if="applyStatus">
              <div class="app-meta">认证状态：
                <span class="badge" :class="'badge-' + adopterStatusClass(applyStatus.status)">{{ applyStatus.statusName }}</span>
              </div>
              <p v-if="applyStatus.status === 2" style="font-size:12px;color:var(--accent-error);margin-top:6px;">
                未通过原因：{{ applyStatus.reviewComment }}
              </p>
              <p v-if="applyStatus.status === 0" style="font-size:11px;color:#A69587;margin-top:6px;">资料审核中，请耐心等待</p>
            </template>
            <template v-else>
              <button class="btn btn-outline btn-fw" @click="openApplyModal">申请成为送养人</button>
              <p style="font-size: 11px; color: #A69587; margin-top: 6px;">成为送养人后可发布领养信息和审核申请</p>
            </template>
          </div>
          <div class="card-padded">
            <div class="profile-menu">
              <RouterLink v-for="item in menuItems" :key="item.key" :to="item.to"
                class="menu-item" :class="{ active: activeTab === item.key }">
                {{ item.label }}
              </RouterLink>
            </div>
            <button class="btn btn-ghost btn-fw" style="margin-top: 12px;" @click="handleLogout">退出登录</button>
          </div>
        </div>
      </div>

      <div class="col-main">
        <!-- 我的申请 -->
        <div v-if="activeTab === 'applications'" class="content-section">
          <h2>我的领养申请</h2>
          <div v-if="appsLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>
          <template v-else-if="applications.length === 0">
            <div class="card card-padded" style="text-align: center; padding: 48px;">
              <p class="text-muted" style="font-size: 15px;">暂无领养申请</p>
              <RouterLink to="/" class="btn btn-primary" style="margin-top: 16px;">去浏览宠物</RouterLink>
            </div>
          </template>
          <div v-for="app in applications" :key="app.appId" v-else
            class="card card-padded app-card-clickable" @click="goDetail(app.petId)">
            <div class="app-card">
              <div class="app-img"><div class="img-placeholder">{{ app.emoji }}</div></div>
              <div class="app-content">
                <div class="app-name">{{ app.petName }}</div>
                <div class="app-meta">{{ app.breed }} · 申请日期 {{ app.date }}</div>
              </div>
              <AppBadge :type="app.statusType" :text="app.status" />
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="content-section">
          <h2>我的收藏</h2>
          <div v-if="favLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>
          <template v-else-if="favorites.length === 0">
            <div class="card card-padded" style="text-align: center; padding: 48px;">
              <p class="text-muted" style="font-size: 15px;">暂无收藏</p>
              <RouterLink to="/" class="btn btn-primary" style="margin-top: 16px;">去浏览宠物</RouterLink>
            </div>
          </template>
          <div v-for="fav in favorites" :key="fav.favId" v-else
            class="card card-padded app-card-clickable" @click="goDetail(fav.petId)">
            <div class="app-card">
              <div class="app-img"><div class="img-placeholder">{{ fav.emoji }}</div></div>
              <div class="app-content">
                <div class="app-name">{{ fav.petName }}</div>
                <div class="app-meta">{{ fav.breed }} · 收藏日期 {{ fav.date }}</div>
              </div>
              <AppBadge :type="fav.statusType" :text="fav.status" />
            </div>
          </div>
        </div>

        <!-- 个人设置 -->
        <div v-if="activeTab === 'settings'" class="content-section">
          <h2>个人设置</h2>
          <div class="card card-padded">
            <!-- 保存成功提示 -->
            <div v-if="settingsSaved" class="form-error-banner" style="background: #f0fdf4; border-color: #86efac; color: #16a34a;">
              保存成功
            </div>
            <!-- 保存失败提示 -->
            <div v-if="settingsError" class="form-error-banner">{{ settingsError }}</div>

            <div class="input-group">
              <label>昵称</label>
              <input v-model="settingsForm.nickname" class="input-field" type="text"
                placeholder="请输入昵称（2-20位）" maxlength="20" />
            </div>
            <div class="input-group">
              <label>邮箱</label>
              <input v-model="settingsForm.email" class="input-field" type="email" placeholder="请输入邮箱" />
            </div>
            <div class="input-group">
              <label>手机号</label>
              <input class="input-field" type="tel" :value="maskedPhone" disabled
                style="opacity: 0.6; cursor: not-allowed;" />
              <span style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">手机号暂不支持修改</span>
            </div>
            <button class="btn btn-primary" style="margin-top: 8px;"
              :disabled="settingsSaving" @click="handleSaveSettings">
              {{ settingsSaving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>

        <!-- 通知（内联 tab） -->
        <div v-if="activeTab === 'notifications'" class="content-section">
          <div class="flex-between" style="margin-bottom: 16px;">
            <h2 style="margin-bottom: 0;">
              我的通知
              <span v-if="unreadCount" class="badge badge-danger" style="margin-left: 8px;">{{ unreadCount }} 条未读</span>
            </h2>
            <button class="btn btn-outline btn-sm" :disabled="!unreadCount" @click="handleMarkAll">全部已读</button>
          </div>

          <div class="chip-group">
            <button class="chip" :class="{ active: notifFilterRead === '' }" @click="switchNotifFilter('')">全部</button>
            <button class="chip" :class="{ active: notifFilterRead === '0' }" @click="switchNotifFilter('0')">未读</button>
            <button class="chip" :class="{ active: notifFilterRead === '1' }" @click="switchNotifFilter('1')">已读</button>
          </div>

          <div v-if="notifLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="notifList.length === 0" class="card card-padded" style="text-align: center; padding: 56px; margin-top: 16px;">
            <p class="text-muted" style="font-size: 15px;">暂无通知</p>
          </div>

          <div v-else class="content-section" style="margin-top: 16px;">
            <div
              v-for="n in notifList"
              :key="n.id"
              class="card card-padded notif-card"
              :class="{ 'notif-unread': !n.readStatus }"
              style="cursor: pointer;"
              @click="handleNotifClick(n)"
            >
              <div class="flex-between">
                <div class="notif-main">
                  <div class="notif-title">
                    <span v-if="!n.readStatus" class="notif-dot"></span>
                    {{ n.title }}
                  </div>
                  <div class="notif-content">{{ n.content }}</div>
                  <div class="notif-meta">
                    <span v-if="n.typeName" class="tag tag-warm">{{ n.typeName }}</span>
                    <span class="text-muted">{{ n.createdAt }}</span>
                  </div>
                </div>
                <div class="notif-actions">
                  <button v-if="!n.readStatus" class="btn btn-ghost btn-sm" @click="handleMarkRead(n.id)">标为已读</button>
                  <span v-else class="text-muted" style="font-size: 13px;">已读</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 领养记录（仅我的领养，无子tab） -->
        <div v-if="activeTab === 'adoptions'" class="content-section">
          <h2>领养记录</h2>

          <div v-if="adoptLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="adoptRecords.length === 0" class="card card-padded" style="text-align: center; padding: 56px;">
            <p class="text-muted" style="font-size: 15px;">暂无领养记录</p>
            <RouterLink to="/" class="btn btn-primary" style="margin-top: 16px;">去浏览宠物</RouterLink>
          </div>

          <div v-else class="content-section">
            <div v-for="r in adoptRecords" :key="r.id" class="card card-padded">
              <div class="app-card">
                <div class="app-img"><div class="img-placeholder">🐾</div></div>
                <div class="app-content">
                  <div class="app-name">{{ r.petName }}</div>
                  <div class="app-meta">{{ r.petBreed }} · 领养日期 {{ r.adoptedAt }}</div>
                  <div class="app-meta">送养人：{{ r.adopterName || '—' }}</div>
                  <div class="app-meta text-muted">回访周期：{{ r.followUpMonths }} 个月</div>
                </div>
                <AppBadge :type="adoptStatusClass[r.status] || 'info'" :text="r.statusName" />
              </div>
            </div>
          </div>
        </div>

        <!-- 我送养的宠物（独立一级菜单） -->
        <div v-if="activeTab === 'my-pets'" class="content-section">
          <h2>我送养的宠物</h2>

          <div v-if="sentLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="sentPets.length === 0" class="card card-padded" style="text-align: center; padding: 56px;">
            <p class="text-muted" style="font-size: 15px;">您还没有发布领养信息</p>
            <RouterLink to="/publish" class="btn btn-primary" style="margin-top: 16px;">去发布宠物</RouterLink>
          </div>

          <div v-else class="content-section">
            <div v-for="p in sentPets" :key="p.id" class="card card-padded">
              <div class="app-card">
                <div class="app-img">
                  <img v-if="p.coverImage" :src="p.coverImage" alt="cover" style="width:64px;height:64px;object-fit:cover;border-radius:8px;" />
                  <div v-else class="img-placeholder">🐾</div>
                </div>
                <div class="app-content">
                  <div class="app-name">{{ p.name }}</div>
                  <div class="app-meta">{{ p.breed }} · 发布于 {{ p.createdAt }}</div>
                </div>
                <div class="app-actions" style="display:flex;flex-direction:column;gap:8px;align-items:flex-end;">
                  <AppBadge :type="sentStatusClass[p.status] || 'info'" :text="p.statusName" />
                  <button class="btn btn-outline btn-sm" @click="router.push(`/pets/${p.id}/edit`)">编辑</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 回访任务（内联 tab） -->
        <div v-if="activeTab === 'follow-ups'" class="content-section">
          <h2>回访任务</h2>

          <div v-if="followLoading" class="flex-center" style="padding: 48px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="followTasks.length === 0" class="card card-padded" style="text-align: center; padding: 56px;">
            <p class="text-muted" style="font-size: 15px;">暂无回访任务</p>
          </div>

          <div v-else class="two-col">
            <div class="col-side-sm">
              <div class="card-padded">
                <div class="profile-menu">
                  <button
                    v-for="t in followTasks"
                    :key="t.id"
                    class="menu-item"
                    :class="{ active: followSelectedTask?.id === t.id }"
                    @click="selectFollowTask(t)"
                  >
                    <span>#{{ t.periodNumber }} · {{ t.petName }}</span>
                    <span class="text-muted" style="font-size: 12px;">{{ t.statusName }}</span>
                  </button>
                </div>
              </div>
            </div>

            <div class="col-main">
              <div v-if="followTaskLoading" class="flex-center" style="padding: 48px 0;">
                <p class="text-muted">加载中...</p>
              </div>
              <div v-else-if="!followSelectedTask" class="card card-padded" style="text-align: center; padding: 56px;">
                <p class="text-muted">请选择左侧任务查看详情</p>
              </div>
              <div v-else class="content-section">
                <div class="card card-padded">
                  <div class="flex-between">
                    <h3 style="margin: 0;">第 {{ followSelectedTask.periodNumber }} 次回访 · {{ followSelectedTask.petName }}</h3>
                    <AppBadge :type="followStatusClass[followSelectedTask.status] || 'info'" :text="followSelectedTask.statusName" />
                  </div>
                  <div class="info-grid" style="margin-top: 16px;">
                    <div class="info-item">
                      <span class="info-label">计划日期</span>
                      <span class="info-value">{{ followSelectedTask.scheduledDate || '—' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">截止日期</span>
                      <span class="info-value">{{ followSelectedTask.dueDate || '—' }}</span>
                    </div>
                  </div>
                </div>

                <!-- 提交回访表单 -->
                <div class="card card-padded">
                  <h4>提交回访</h4>
                  <div v-if="followSubmitSuccess" class="form-error-banner" style="background: #f0fdf4; border-color: #86efac; color: #16a34a;">提交成功</div>
                  <div v-if="followSubmitError" class="form-error-banner">{{ followSubmitError }}</div>
                  <div class="input-group">
                    <label>回访描述</label>
                    <textarea v-model="followForm.content" class="textarea" placeholder="请描述宠物当前状况..."></textarea>
                  </div>
                  <div class="input-group">
                    <label>回访照片 URL（多个用逗号或换行分隔，可选）</label>
                    <textarea v-model="followForm.imageUrls" class="textarea" placeholder="https://.../a.jpg, https://.../b.jpg"></textarea>
                  </div>
                  <button class="btn btn-primary" :disabled="followSubmitting" @click="handleSubmitFollowUp">
                    {{ followSubmitting ? '提交中...' : '提交回访' }}
                  </button>
                </div>

                <!-- 回访历史 -->
                <div class="card card-padded">
                  <h4>回访历史</h4>
                  <div v-if="followHistoryLoading" class="text-muted">加载中...</div>
                  <div v-else-if="followHistory.length === 0" class="text-muted">暂无回访记录</div>
                  <div v-else class="health-grid">
                    <div
                      v-for="h in followHistory"
                      :key="h.id"
                      class="req-item"
                      style="flex-direction: column; align-items: flex-start; gap: 6px;"
                    >
                      <div style="font-size: 14px; color: var(--text-primary);">{{ h.content }}</div>
                      <div class="text-muted" style="font-size: 12px;">{{ h.submittedAt }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 收到的申请（内联 tab） -->
        <div v-if="activeTab === 'received'" class="content-section">
          <h2>收到的领养申请</h2>

          <div v-if="recvNoPermission" class="card card-padded" style="text-align: center; padding: 56px;">
            <p class="text-muted" style="font-size: 15px;">仅送养人可查看收到的申请</p>
            <RouterLink to="/profile/applications" class="btn btn-primary" style="margin-top: 16px;">返回我的申请</RouterLink>
          </div>

          <template v-else>
            <div class="chip-group">
              <button class="chip" :class="{ active: recvStatusFilter === '' }" @click="switchRecvStatusFilter('')">全部</button>
              <button class="chip" :class="{ active: recvStatusFilter === '0' }" @click="switchRecvStatusFilter('0')">待审核</button>
              <button class="chip" :class="{ active: recvStatusFilter === '1' }" @click="switchRecvStatusFilter('1')">审核中</button>
              <button class="chip" :class="{ active: recvStatusFilter === '2' }" @click="switchRecvStatusFilter('2')">已通过</button>
              <button class="chip" :class="{ active: recvStatusFilter === '3' }" @click="switchRecvStatusFilter('3')">已驳回</button>
            </div>

            <div v-if="recvLoading" class="flex-center" style="padding: 48px 0;">
              <p class="text-muted">加载中...</p>
            </div>

            <div v-else-if="recvApps.length === 0" class="card card-padded" style="text-align: center; padding: 56px; margin-top: 16px;">
              <p class="text-muted" style="font-size: 15px;">暂无收到的申请</p>
            </div>

            <div v-else class="content-section" style="margin-top: 16px;">
              <div v-for="a in recvApps" :key="a.id" class="card card-padded">
                <div class="flex-between">
                  <div class="app-card">
                    <div class="app-img"><div class="img-placeholder">{{ a.petEmoji }}</div></div>
                    <div class="app-content">
                      <div class="app-name">{{ a.petName }}</div>
                      <div class="app-meta">{{ a.petBreed }} · 申请人 {{ a.applicantName }} · {{ a.createdAt }}</div>
                    </div>
                  </div>
                  <AppBadge :type="recvStatusClass[a.status] || 'info'" :text="a.statusName" />
                </div>
                <div class="app-meta" style="margin-top: 12px;">申请理由：{{ a.reason }}</div>
                <div class="app-meta">住房类型：{{ a.housingTypeName }} · 养宠经验：{{ a.petExperienceName }} · 月收入：{{ a.monthlyIncome || '—' }}</div>
                <div v-if="a.status === 3 && a.rejectReason" class="app-meta" style="color: var(--accent-error);">驳回原因：{{ a.rejectReason }}</div>

                <div v-if="a.status === 0" class="flex-row" style="margin-top: 16px;">
                  <button class="btn btn-primary btn-sm" @click="openRecvReview(a)">审核</button>
                </div>
              </div>
            </div>
          </template>

          <!-- 审核弹层 -->
          <div v-if="recvReviewTarget" class="modal-mask" @click.self="closeRecvReview">
            <div class="card card-padded modal-body">
              <h4>审核申请 · {{ recvReviewTarget.petName }}</h4>
              <div v-if="recvReviewError" class="form-error-banner">{{ recvReviewError }}</div>
              <div class="input-group">
                <label>驳回原因（仅拒绝时必填）</label>
                <textarea v-model="recvRejectReason" class="textarea" placeholder="请说明驳回原因..."></textarea>
              </div>
              <div class="flex-row">
                <button class="btn btn-success btn-sm" :disabled="recvReviewing" @click="handleRecvReview('approve')">通过</button>
                <button class="btn btn-danger-outline btn-sm" :disabled="recvReviewing" @click="handleRecvReview('reject')">拒绝</button>
                <button class="btn btn-ghost btn-sm" @click="closeRecvReview">取消</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 等待审核申请（审核员） -->
        <div v-if="activeTab === 'review' && isReviewer" class="content-section">
          <ReviewCenter />
        </div>

        <!-- 送养人认证申请弹窗 -->
        <div v-if="showApplyModal" class="modal-mask" @click.self="showApplyModal = false">
          <div class="card card-padded modal-body">
            <h4>申请成为送养人认证</h4>
            <div v-if="applyError" class="form-error-banner">{{ applyError }}</div>
            <div class="input-group">
              <label>真实姓名</label>
              <input v-model="applyForm.realName" class="input-field" placeholder="请输入真实姓名" />
            </div>
            <div class="input-group">
              <label>身份证号</label>
              <input v-model="applyForm.idCard" class="input-field" placeholder="请输入身份证号" />
            </div>
            <div class="input-group">
              <label>联系电话</label>
              <input v-model="applyForm.phone" class="input-field" placeholder="请输入联系电话" />
            </div>
            <div class="input-group">
              <label>邮箱（选填）</label>
              <input v-model="applyForm.email" class="input-field" placeholder="请输入邮箱" />
            </div>
            <div class="input-group">
              <label>申请理由（选填）</label>
              <textarea v-model="applyForm.reason" class="textarea" placeholder="请简述您申请成为送养人的原因..."></textarea>
            </div>
            <div class="flex-row">
              <button class="btn btn-primary btn-sm" :disabled="applySubmitting" @click="handleSubmitApplication">
                {{ applySubmitting ? '提交中...' : '提交申请' }}
              </button>
              <button class="btn btn-ghost btn-sm" @click="showApplyModal = false">取消</button>
            </div>
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

/* 通知卡片（原 NotificationView 内联样式） */
.notif-card {
  transition: background 0.2s;
}
.notif-unread {
  border-left: 3px solid var(--accent-primary);
}
.notif-main {
  flex: 1;
  min-width: 0;
}
.notif-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-primary);
  flex-shrink: 0;
}
.notif-content {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 8px 0;
  line-height: 1.5;
}
.notif-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.notif-actions {
  flex-shrink: 0;
  margin-left: 16px;
}

/* 审核弹层（原 ReceivedApplicationsView 内联样式） */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(31, 30, 27, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 24px;
}
.modal-body {
  width: 100%;
  max-width: 480px;
}
</style>
