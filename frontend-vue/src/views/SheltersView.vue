<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/user'
import { listShelters, createShelter } from '../api/shelters'

const router = useRouter()
const authStore = useAuthStore()

const shelters = ref([])
const total = ref(0)
const loading = ref(false)

// 过滤条件
const filters = reactive({ province: '', city: '', keyword: '', page: 1, size: 12 })

// 申请成为救助站弹窗
const showModal = ref(false)
const submitting = ref(false)
const formError = ref('')
const form = reactive({
  name: '',
  description: '',
  address: '',
  province: '',
  city: '',
  contactPhone: '',
  logoUrl: '',
})

const statusClass = { 0: 'info', 1: 'success', 2: 'muted' }

function goDetail(id) {
  router.push(`/shelter/${id}`)
}

async function loadShelters() {
  loading.value = true
  try {
    const res = await listShelters({ ...filters })
    shelters.value = (res?.records || []).map((s) => ({
      id: s.id,
      name: s.name || '未命名救助站',
      description: s.description || '',
      province: s.province || '',
      city: s.city || '',
      fullLocation: s.fullLocation || '',
      logoUrl: s.logoUrl || '',
      status: s.status,
      statusName: s.statusName || '未知',
      ownerUserId: s.ownerUserId,
    }))
    total.value = res?.total || 0
  } catch (e) {
    shelters.value = []
  } finally {
    loading.value = false
  }
}

function openModal() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  formError.value = ''
  form.name = ''
  form.description = ''
  form.address = ''
  form.province = ''
  form.city = ''
  form.contactPhone = ''
  form.logoUrl = ''
  showModal.value = true
}

async function handleSubmit() {
  formError.value = ''
  if (!form.name.trim()) { formError.value = '请填写救助站名称'; return }
  submitting.value = true
  try {
    const res = await createShelter({ ...form })
    showModal.value = false
    alert('救助站已提交，等待审核。')
    // 新创建的救助站可直接查看
    if (res && res.id) router.push(`/shelter/${res.id}`)
    else loadShelters()
  } catch (e) {
    formError.value = e.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(loadShelters)
</script>

<template>
  <div class="page-container">
    <div class="content-section">
      <div class="flex-between" style="margin-bottom: 16px;">
        <h1>救助站</h1>
        <button class="btn btn-primary" @click="openModal">申请成为救助站</button>
      </div>

      <!-- 过滤 -->
      <div class="flex-row" style="gap: 12px; margin-bottom: 20px; flex-wrap: wrap;">
        <input v-model="filters.keyword" class="input-field" style="max-width: 240px;"
          placeholder="搜索名称/简介/地址" @keyup.enter="loadShelters" />
        <input v-model="filters.province" class="input-field" style="max-width: 140px;"
          placeholder="省" @keyup.enter="loadShelters" />
        <input v-model="filters.city" class="input-field" style="max-width: 140px;"
          placeholder="市" @keyup.enter="loadShelters" />
        <button class="btn btn-outline" @click="loadShelters">搜索</button>
      </div>

      <div v-if="loading" class="flex-center" style="padding: 48px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="shelters.length === 0" class="card card-padded" style="text-align: center; padding: 56px;">
        <p class="text-muted" style="font-size: 15px;">暂无救助站</p>
      </div>

      <div v-else class="pet-grid">
        <div v-for="s in shelters" :key="s.id"
          class="card card-padded shelter-card-clickable" @click="goDetail(s.id)">
          <div class="shelter-logo">
            <img v-if="s.logoUrl" :src="s.logoUrl" :alt="s.name" class="shelter-logo-img" />
            <div v-else class="img-placeholder">{{ (s.name || '🏠').charAt(0).toUpperCase() }}</div>
            <span class="badge" :class="'badge-' + (statusClass[s.status] || 'info')">{{ s.statusName }}</span>
          </div>
          <div class="shelter-body">
            <div class="shelter-name">{{ s.name }}</div>
            <div v-if="s.fullLocation" class="tag tag-warm">{{ s.fullLocation }}</div>
            <p class="shelter-desc">{{ s.description || '暂无简介' }}</p>
            <span class="btn btn-outline btn-sm" style="margin-top: 8px; pointer-events: none;">查看详情</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 申请创建弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="card card-padded modal-body">
        <h4>申请成为救助站</h4>
        <div v-if="formError" class="form-error-banner">{{ formError }}</div>
        <div class="input-group">
          <label>救助站名称 *</label>
          <input v-model="form.name" class="input-field" placeholder="请输入救助站名称" />
        </div>
        <div class="input-group">
          <label>所在省</label>
          <input v-model="form.province" class="input-field" placeholder="如：广东省" />
        </div>
        <div class="input-group">
          <label>所在市</label>
          <input v-model="form.city" class="input-field" placeholder="如：深圳市" />
        </div>
        <div class="input-group">
          <label>详细地址</label>
          <input v-model="form.address" class="input-field" placeholder="请输入详细地址" />
        </div>
        <div class="input-group">
          <label>联系手机号</label>
          <input v-model="form.contactPhone" class="input-field" placeholder="请输入联系手机号" />
        </div>
        <div class="input-group">
          <label>LOGO/头像 URL（选填）</label>
          <input v-model="form.logoUrl" class="input-field" placeholder="https://..." />
        </div>
        <div class="input-group">
          <label>简介（选填）</label>
          <textarea v-model="form.description" class="textarea" placeholder="请简要介绍救助站..."></textarea>
        </div>
        <div class="flex-row">
          <button class="btn btn-primary btn-sm" :disabled="submitting" @click="handleSubmit">
            {{ submitting ? '提交中...' : '提交申请' }}
          </button>
          <button class="btn btn-ghost btn-sm" @click="showModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全局 style.css 未提供的局部类（与 ProfileView 风格一致） */
.badge-info { background: #eef2ff; color: #4f46e5; }
.badge-success { background: #f0fdf4; color: #16a34a; }
.badge-muted { background: #f3f4f6; color: #6b7280; }
.badge-warning { background: #fffbeb; color: #d97706; }
.badge-error { background: #fef2f2; color: #dc2626; }

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

.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.shelter-card-clickable {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.shelter-card-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}
.shelter-logo {
  position: relative;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-soft, #f5f1ea);
}
.shelter-logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.shelter-logo .badge {
  position: absolute;
  top: 8px;
  right: 8px;
}
.shelter-body {
  padding: 12px 4px 4px;
}
.shelter-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}
.shelter-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
