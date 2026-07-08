<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import { getAdminPets, updateAdminPetStatus } from '../api/admin'

const router = useRouter()

const navLinks = [
  { text: '概览', to: '/admin' },
  { text: '宠物管理', to: '/admin/pets' },
  { text: '申请审核', to: '/admin' },
]

const loading = ref(true)
const error = ref('')
const pets = ref([])

async function loadPets() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdminPets()
    pets.value = res?.records || res || []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleStatusToggle(pet) {
  const newStatus = pet.status === 6 ? 0 : 6
  try {
    await updateAdminPetStatus(pet.id, newStatus)
    pet.status = newStatus
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function getStatusBadge(status) {
  if (status === 6) return { text: '已下架', type: 'danger' }
  if (status === 0) return { text: '可领养', type: 'success' }
  return { text: '申请中', type: 'warning' }
}

onMounted(loadPets)
</script>

<template>
  <div id="admin-pets" class="page-container">
    <AppNavbar :nav-links="navLinks" :show-actions="false" :admin="true" logo-text="PetHome Admin" />

    <div class="section-padding">
      <div class="flex-between" style="margin-bottom: 20px;">
        <h2 style="margin-bottom: 0;">宠物管理</h2>
        <button v-if="error" class="btn btn-ghost btn-sm" @click="loadPets">刷新</button>
      </div>

      <div v-if="loading" class="flex-center" style="padding: 40px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="error" class="card-padded" style="text-align: center; padding: 32px;">
        <p style="color: #dc2626; font-size: 14px; margin-bottom: 12px;">{{ error }}</p>
        <button class="btn btn-primary btn-sm" @click="loadPets">重试</button>
      </div>

      <div v-else class="data-table">
        <table>
          <thead>
            <tr><th>ID</th><th>名称</th><th>品种</th><th>位置</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="pet in pets" :key="pet.id">
              <td>{{ pet.id }}</td>
              <td>{{ pet.name }}</td>
              <td>{{ pet.breed }}</td>
              <td>{{ pet.locationProvince }}{{ pet.locationCity ? '·' + pet.locationCity : '' }}</td>
              <td><span class="badge" :class="'badge-' + getStatusBadge(pet.status).type">{{ getStatusBadge(pet.status).text }}</span></td>
              <td>
                <button class="action-link" @click="router.push(`/detail/${pet.id}`)">查看</button>
                <button class="action-link" style="margin-left: 12px;" @click="handleStatusToggle(pet)">
                  {{ pet.status === 6 ? '上架' : '下架' }}
                </button>
                <button class="action-link action-link-danger" style="margin-left: 12px;" @click="handleStatusToggle(pet)">
                  {{ pet.status === 6 ? '恢复' : '删除' }}
                </button>
              </td>
            </tr>
            <tr v-if="pets.length === 0">
              <td colspan="6" style="text-align: center; color: #BFBBB6; padding: 32px;">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
