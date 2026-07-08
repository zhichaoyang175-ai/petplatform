<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShelter, getShelterPets } from '../api/shelters'
import PetCard from '../components/PetCard.vue'

const route = useRoute()
const router = useRouter()

const shelter = ref(null)
const pets = ref([])
const loading = ref(false)
const petsLoading = ref(false)

const statusClass = { 0: 'info', 1: 'success', 2: 'muted' }

async function loadDetail() {
  const id = route.params.id
  loading.value = true
  try {
    const res = await getShelter(id)
    shelter.value = res || null
  } catch (e) {
    shelter.value = null
  } finally {
    loading.value = false
  }
}

async function loadPets() {
  const id = route.params.id
  petsLoading.value = true
  try {
    const res = await getShelterPets(id)
    pets.value = (res || []).map((p) => ({
      ...p,
      coverImage: p.coverImage || '',
    }))
  } catch (e) {
    pets.value = []
  } finally {
    petsLoading.value = false
  }
}

function goBack() {
  router.push('/shelters')
}

onMounted(() => {
  loadDetail()
  loadPets()
})
</script>

<template>
  <div class="page-container">
    <div class="content-section">
      <button class="btn btn-ghost btn-sm" style="margin-bottom: 16px;" @click="goBack">← 返回救助站列表</button>

      <div v-if="loading" class="flex-center" style="padding: 48px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="!shelter" class="card card-padded" style="text-align: center; padding: 56px;">
        <p class="text-muted" style="font-size: 15px;">救助站不存在或已下架</p>
        <button class="btn btn-primary" style="margin-top: 16px;" @click="goBack">返回列表</button>
      </div>

      <template v-else>
        <!-- 救助站信息 -->
        <div class="two-col">
          <div class="col-main">
            <div class="card card-padded">
              <div class="flex-between">
                <div class="flex-row" style="gap: 16px; align-items: center;">
                  <div class="shelter-logo-lg">
                    <img v-if="shelter.logoUrl" :src="shelter.logoUrl" :alt="shelter.name" />
                    <div v-else class="img-placeholder">{{ (shelter.name || '🏠').charAt(0).toUpperCase() }}</div>
                  </div>
                  <div>
                    <h2 style="margin: 0;">{{ shelter.name }}</h2>
                    <span class="badge" :class="'badge-' + (statusClass[shelter.status] || 'info')"
                      style="margin-top: 8px; display: inline-block;">{{ shelter.statusName }}</span>
                  </div>
                </div>
              </div>

              <div class="info-grid" style="margin-top: 20px;">
                <div class="info-item" v-if="shelter.fullLocation">
                  <span class="info-label">所在地区</span>
                  <span class="info-value">{{ shelter.fullLocation }}</span>
                </div>
                <div class="info-item" v-if="shelter.address">
                  <span class="info-label">详细地址</span>
                  <span class="info-value">{{ shelter.address }}</span>
                </div>
                <div class="info-item" v-if="shelter.contactPhone">
                  <span class="info-label">联系电话</span>
                  <span class="info-value">{{ shelter.contactPhone }}</span>
                </div>
                <div class="info-item" v-if="shelter.createdAt">
                  <span class="info-label">创建时间</span>
                  <span class="info-value">{{ String(shelter.createdAt).substring(0, 10) }}</span>
                </div>
              </div>

              <div v-if="shelter.description" class="content-section" style="margin-top: 16px;">
                <h4>简介</h4>
                <p class="text-secondary" style="line-height: 1.7;">{{ shelter.description }}</p>
              </div>
            </div>
          </div>

          <div class="col-side-sm">
            <div class="card card-padded">
              <div class="app-card">
                <div class="app-img"><div class="img-placeholder">🏠</div></div>
                <div class="app-content">
                  <div class="app-name">{{ shelter.name }}</div>
                  <div class="app-meta">{{ shelter.fullLocation || '暂无地区' }}</div>
                </div>
              </div>
              <p class="text-muted" style="font-size: 12px; margin-top: 12px;">
                本救助站在养宠物如下，欢迎领养。
              </p>
            </div>
          </div>
        </div>

        <!-- 在养宠物 -->
        <div class="content-section" style="margin-top: 8px;">
          <h3>在养宠物</h3>

          <div v-if="petsLoading" class="flex-center" style="padding: 40px 0;">
            <p class="text-muted">加载中...</p>
          </div>

          <div v-else-if="pets.length === 0" class="card card-padded" style="text-align: center; padding: 48px;">
            <p class="text-muted" style="font-size: 15px;">该救助站暂无可领养宠物</p>
          </div>

          <div v-else class="pet-grid">
            <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
          </div>
        </div>
      </template>
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

.shelter-logo-lg {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-soft, #f5f1ea);
  flex-shrink: 0;
}
.shelter-logo-lg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>
