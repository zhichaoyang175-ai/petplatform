<script setup>
import { ref, onMounted } from 'vue'
import AppNavbar from '../components/AppNavbar.vue'
import PetCard from '../components/PetCard.vue'
import AppPagination from '../components/AppPagination.vue'
import { getRecommendations } from '../api/match'
import { useAuthStore } from '../stores/user'

const authStore = useAuthStore()

const loading = ref(true)
const error = ref('')
const pets = ref([])
const total = ref(0)

const currentPage = ref(1)
const pageSize = 12

async function loadRecommendations() {
  loading.value = true
  error.value = ''
  try {
    // 已登录则带上 userId，未登录后端按默认推荐返回
    const params = {
      page: currentPage.value,
      size: pageSize,
    }
    const res = await getRecommendations(params)
    pets.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  loadRecommendations()
}

onMounted(loadRecommendations)
</script>

<template>
  <div class="page-container">
    <AppNavbar :show-actions="true" />

    <div class="content-section">
      <!-- 顶部说明 -->
      <div class="card-padded two-col" style="align-items: center; margin-bottom: 24px;">
        <div class="col-main">
          <h1 style="margin: 0 0 8px;">为你推荐</h1>
          <p class="text-muted" style="margin: 0; font-size: 15px; line-height: 1.6;">
            根据你的领养画像（住房类型、养宠经验、家人态度），我们为待领养的毛孩子做了轻量、可解释的匹配排序。
            <template v-if="authStore.isLoggedIn">已结合你的偏好为你精选。</template>
            <template v-else>登录后将结合你的偏好，推荐更贴合的伙伴。</template>
          </p>
        </div>
        <div style="text-align: right;">
          <span class="badge badge-info">智能匹配</span>
        </div>
      </div>

      <!-- 加载 / 错误 / 空 -->
      <div v-if="loading" class="flex-center" style="padding: 40px 0;">
        <p class="text-muted">正在为你匹配毛孩子...</p>
      </div>

      <div v-else-if="error" class="card-padded" style="text-align: center; padding: 40px;">
        <p class="text-muted" style="font-size: 15px;">{{ error }}</p>
        <button class="btn btn-primary btn-sm" style="margin-top: 12px;" @click="loadRecommendations">重试</button>
      </div>

      <div v-else-if="pets.length === 0" class="card-padded" style="text-align: center; padding: 48px;">
        <p class="text-muted" style="font-size: 15px;">暂时没有可推荐的待领养宠物</p>
      </div>

      <div v-else>
        <div class="flex-between" style="margin-bottom: 16px;">
          <h2 style="margin-bottom: 0;">为你精选</h2>
          <span class="text-muted" style="font-size: 14px;">共 {{ total }} 只</span>
        </div>
        <div class="pet-grid">
          <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
        </div>
      </div>

      <AppPagination
        v-if="total > pageSize"
        :current="currentPage"
        :total="Math.ceil(total / pageSize) || 1"
        @page="handlePageChange"
      />
    </div>
  </div>
</template>
