<script setup>
import { ref, onMounted } from 'vue'
import AppNavbar from '../components/AppNavbar.vue'
import { useAuthStore } from '../stores/user'
import {
  listMyVisits,
  listReceivedVisits,
  confirmVisit,
  completeVisit,
  cancelVisit,
} from '../api/visits'

const authStore = useAuthStore()

const navLinks = [
  { text: '首页', to: '/' },
  { text: '浏览宠物', to: '/#pets' },
  { text: '我的', to: '/profile/applications' },
]

// 0-待确认 1-已确认 2-已完成 3-已取消
const visitStatusClass = { 0: 'info', 1: 'warning', 2: 'success', 3: 'muted' }

// ---- 我预约的 ----
const myVisits = ref([])
const myLoading = ref(false)

// ---- 我收到的 ----
const recvVisits = ref([])
const recvLoading = ref(false)

async function loadMy() {
  myLoading.value = true
  try {
    const res = await listMyVisits()
    myVisits.value = (res || []).map((v) => ({
      id: v.id,
      petId: v.petId,
      petName: v.petName || '未知宠物',
      ownerName: v.ownerName || '送养人',
      appointmentTime: v.appointmentTime ? String(v.appointmentTime).substring(0, 16) : '—',
      note: v.note || '',
      status: v.status,
      statusName: v.statusName || '未知',
    }))
  } catch (e) {
    myVisits.value = []
  } finally {
    myLoading.value = false
  }
}

async function loadReceived() {
  recvLoading.value = true
  try {
    const res = await listReceivedVisits()
    recvVisits.value = (res || []).map((v) => ({
      id: v.id,
      petId: v.petId,
      petName: v.petName || '未知宠物',
      applicantName: v.applicantName || '匿名',
      appointmentTime: v.appointmentTime ? String(v.appointmentTime).substring(0, 16) : '—',
      note: v.note || '',
      status: v.status,
      statusName: v.statusName || '未知',
    }))
  } catch (e) {
    recvVisits.value = []
  } finally {
    recvLoading.value = false
  }
}

async function handleCancel(v) {
  if (!confirm('确定取消该预约吗？')) return
  try {
    await cancelVisit(v.id)
    await loadMy()
    await loadReceived()
  } catch (e) {
    alert(e.message || '取消失败')
  }
}

async function handleConfirm(v) {
  try {
    await confirmVisit(v.id)
    await loadReceived()
  } catch (e) {
    alert(e.message || '确认失败')
  }
}

async function handleComplete(v) {
  if (!confirm('确定标记该预约为已完成吗？')) return
  try {
    await completeVisit(v.id)
    await loadReceived()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

onMounted(() => {
  loadMy()
  loadReceived()
})
</script>

<template>
  <div id="visits" class="page-container">
    <AppNavbar :nav-links="navLinks" :show-actions="false" />

    <div class="content-section">
      <h2>预约看宠</h2>
      <p class="text-muted" style="margin-top: 4px;">
        在这里管理你发起的看宠预约，以及收到的探视申请。
      </p>
    </div>

    <div class="two-col">
      <!-- 我预约的 -->
      <div class="col-main">
        <div class="content-section">
          <h3>我预约的</h3>

          <div v-if="myLoading" class="flex-center" style="padding: 40px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="myVisits.length === 0" class="card card-padded" style="text-align: center; padding: 48px;">
            <p class="text-muted" style="font-size: 15px;">暂无预约</p>
          </div>

          <div v-for="v in myVisits" :key="v.id" v-else class="card card-padded" style="margin-bottom: 12px;">
            <div class="flex-between">
              <div class="app-card">
                <div class="app-img"><div class="img-placeholder">🐾</div></div>
                <div class="app-content">
                  <div class="app-name">{{ v.petName }}</div>
                  <div class="app-meta">送养人：{{ v.ownerName }} · {{ v.appointmentTime }}</div>
                  <div v-if="v.note" class="app-meta text-muted">备注：{{ v.note }}</div>
                </div>
              </div>
              <span class="badge" :class="'badge-' + (visitStatusClass[v.status] || 'info')">{{ v.statusName }}</span>
            </div>
            <div class="flex-row" style="margin-top: 12px;" v-if="v.status === 0 || v.status === 1">
              <button class="btn btn-ghost btn-sm" @click="handleCancel(v)">取消预约</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 我收到的 -->
      <div class="col-main">
        <div class="content-section">
          <h3>我收到的</h3>

          <div v-if="recvLoading" class="flex-center" style="padding: 40px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="recvVisits.length === 0" class="card card-padded" style="text-align: center; padding: 48px;">
            <p class="text-muted" style="font-size: 15px;">暂无收到的预约</p>
          </div>

          <div v-for="v in recvVisits" :key="v.id" v-else class="card card-padded" style="margin-bottom: 12px;">
            <div class="flex-between">
              <div class="app-card">
                <div class="app-img"><div class="img-placeholder">🐾</div></div>
                <div class="app-content">
                  <div class="app-name">{{ v.petName }}</div>
                  <div class="app-meta">申请人：{{ v.applicantName }} · {{ v.appointmentTime }}</div>
                  <div v-if="v.note" class="app-meta text-muted">备注：{{ v.note }}</div>
                </div>
              </div>
              <span class="badge" :class="'badge-' + (visitStatusClass[v.status] || 'info')">{{ v.statusName }}</span>
            </div>
            <div class="flex-row" style="margin-top: 12px;" v-if="v.status === 0">
              <button class="btn btn-primary btn-sm" @click="handleConfirm(v)">确认</button>
              <button class="btn btn-ghost btn-sm" @click="handleCancel(v)">取消</button>
            </div>
            <div class="flex-row" style="margin-top: 12px;" v-else-if="v.status === 1">
              <button class="btn btn-success btn-sm" @click="handleComplete(v)">完成</button>
              <button class="btn btn-ghost btn-sm" @click="handleCancel(v)">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.flex-row {
  display: flex;
  gap: 10px;
}
</style>
