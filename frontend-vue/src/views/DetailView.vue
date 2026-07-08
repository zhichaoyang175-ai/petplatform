<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { getPetDetail, searchPets } from '../api/pets'
import { addFavorite, removeFavorite, checkFavorited } from '../api/favorites'

const router = useRouter()
const route = useRoute()
const petId = computed(() => Number(route.params.id))

const loading = ref(true)
const error = ref('')
const pet = ref(null)
const similarPets = ref([])
const activeThumb = ref(0)
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 主图随缩略图切换（修复缩略图点击无反馈的问题）
const mainImage = computed(() => {
  const imgs = pet.value?.images
  if (imgs && imgs.length && imgs[activeThumb.value]) return imgs[activeThumb.value].imageUrl
  return pet.value?.coverImage || ''
})

// 返回：优先 history.back，避免直接访问详情页时离开站点
function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

// 加载宠物详情
async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    pet.value = await getPetDetail(petId.value)
    document.title = pet.value.name ? `${pet.value.name} - PetHome` : '宠物详情'
  } catch (e) {
    error.value = e.message || '加载宠物详情失败'
  } finally {
    loading.value = false
  }
}

// 加载相似宠物
async function loadSimilar() {
  try {
    const res = await searchPets({ breed: pet.value?.breed, size: 4 })
    similarPets.value = (res?.records || []).filter(p => p.id !== pet.value?.id)
  } catch (e) { /* ignore */ }
}

// 检查收藏状态
async function checkFavorite() {
  if (!petId.value) return
  try {
    const res = await checkFavorited(petId.value)
    // 后端返回 { favorited: boolean }，需用 .favorited 判定（!!res 对非空对象恒为 true）
    isFavorited.value = !!(res && res.favorited)
  } catch (e) { /* ignore */ }
}

// 切换收藏
async function toggleFavorite() {
  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(petId.value)
      isFavorited.value = false
    } else {
      await addFavorite(petId.value)
      isFavorited.value = true
    }
  } catch (e) {
    alert(e.message || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

// 分享
function handleShare() {
  const url = window.location.href
  if (navigator.share) {
    navigator.share({ title: pet.value?.name || '宠物详情', url })
  } else {
    navigator.clipboard.writeText(url).then(() => alert('链接已复制'))
  }
}

onMounted(() => {
  loadDetail().then(() => {
    if (pet.value) {
      loadSimilar()
      checkFavorite()
    }
  })
})

// 健康状态映射
const healthItems = computed(() => {
  if (!pet.value) return []
  const items = []
  if (pet.value.neutered) items.push('已绝育')
  if (pet.value.vaccinated) items.push('已接种疫苗')
  if (pet.value.dewormed) items.push('已驱虫')
  if (items.length === 0) return ['已绝育', '已驱虫', '已接种疫苗']
  return items
})

// 领养要求列表
const requirementList = computed(() => {
  if (pet.value?.adoptionRequirements) {
    return pet.value.adoptionRequirements.split('\n').filter(Boolean)
  }
  return ['年满22周岁，有稳定收入和固定住所', '家庭成员一致同意领养', '接受定期回访，保持与救助站的联系']
})
</script>

<template>
  <div id="detail" class="page-container">
    <!-- Error -->
    <div v-if="error" class="card-padded" style="text-align: center; padding: 64px; margin-top: 40px;">
      <p style="font-size: 16px; color: #dc2626; margin-bottom: 16px;">{{ error }}</p>
      <button class="btn btn-primary" @click="loadDetail">重试</button>
    </div>

    <template v-else>
      <!-- Navbar -->
      <nav class="navbar">
        <div class="navbar-back" @click="goBack">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" />
          </svg>
          返回列表
        </div>
        <div class="navbar-logo-text">PetHome</div>
        <button class="btn btn-ghost" @click="handleShare">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" />
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" /><line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
          </svg>
          分享
        </button>
      </nav>

      <!-- Loading -->
      <div v-if="loading" class="flex-center" style="padding: 80px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <template v-else-if="pet">
        <!-- Gallery -->
        <div class="gallery">
          <div class="gallery-hero">
            <img v-if="mainImage" :src="mainImage" :alt="pet.name" class="gallery-img" />
            <div v-else class="img-placeholder">🐕</div>
          </div>
          <div class="gallery-thumbs" v-if="pet.images && pet.images.length">
            <div v-for="(img, idx) in pet.images" :key="img.id ?? idx" class="gallery-thumb"
              :class="{ active: activeThumb === idx }" @click="activeThumb = idx">
              <img :src="img.imageUrl" :alt="pet.name" />
            </div>
          </div>
          <div v-else class="gallery-thumbs">
            <div v-for="idx in 4" :key="idx" class="gallery-thumb"
              :class="{ active: activeThumb === idx - 1 }" @click="activeThumb = idx - 1">
              <div class="img-placeholder">🐕</div>
            </div>
          </div>
        </div>

        <div class="two-col">
          <div class="col-main">
            <!-- Header -->
            <div class="card-padded" style="margin-bottom: 16px;">
              <div class="flex-between" style="margin-bottom: 12px;">
                <h2 style="margin-bottom: 0;">{{ pet.name }}</h2>
                <span class="badge" :class="pet.status === 0 ? 'badge-success' : 'badge-warning'">
                  {{ pet.status === 0 ? '可领养' : '申请中' }}
                </span>
              </div>
              <div class="flex-row gap-sm" style="flex-wrap: wrap;">
                <span class="tag">{{ pet.breed }}</span>
                <span class="tag tag-warm">{{ pet.gender === 0 ? '公' : '母' }}</span>
                <span class="tag">{{ pet.ageMonths ? (pet.ageMonths < 12 ? pet.ageMonths + '个月' : Math.floor(pet.ageMonths/12) + '岁') : '-' }}</span>
                <span class="tag">{{ pet.locationProvince + '·' + (pet.locationCity || '') }}</span>
              </div>
            </div>

            <!-- About -->
            <div class="card-padded" style="margin-bottom: 16px;">
              <div class="section-header">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#E07856">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                </svg>
                <h3 style="margin-bottom: 0;">关于{{ pet.name }}</h3>
              </div>
              <p style="font-size: 15px; color: #5C5A55; line-height: 1.7;">
                {{ pet.description || '暂无描述' }}
              </p>
            </div>

            <!-- Health -->
            <div class="card-padded" style="margin-bottom: 16px;">
              <div class="section-header">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#E07856" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="20" x2="18" y2="10" /><line x1="12" y1="20" x2="12" y2="4" /><line x1="6" y1="20" x2="6" y2="14" />
                </svg>
                <h3 style="margin-bottom: 0;">健康状况</h3>
              </div>
              <div class="health-grid">
                <div v-for="item in healthItems" :key="item" class="health-item">
                  <span class="health-check">&#10003;</span>
                  <span class="health-text">{{ item }}</span>
                </div>
              </div>
            </div>

            <!-- Requirements -->
            <div class="card-padded">
              <div class="section-header">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#E07856" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" />
                </svg>
                <h3 style="margin-bottom: 0;">领养要求</h3>
              </div>
              <div v-for="(req, idx) in requirementList" :key="idx" class="req-item">
                <span class="req-icon">&#10003;</span>
                <span>{{ req }}</span>
              </div>
            </div>
          </div>

          <div class="col-side">
            <div class="accent-card" style="margin-bottom: 16px;">
              <h3>想给{{ pet.name }}一个家？</h3>
              <p>填写申请表，我们的工作人员将在3个工作日内联系您。</p>
              <RouterLink :to="`/apply/${pet.id}`" class="btn btn-fw" style="background: #fff; color: #E07856;">
                申请领养
              </RouterLink>
              <button class="btn btn-fw" @click="toggleFavorite" :disabled="favoriteLoading"
                style="margin-top: 8px; background: transparent; color: #fff; border: 1.5px solid rgba(255,255,255,0.4); border-radius: 12px; padding: 10px 20px; font-size: 15px; cursor: pointer;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px;">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                </svg>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </button>
            </div>

            <div class="card-padded shelter-card">
              <h4>救助站信息</h4>
              <p class="shelter-name">{{ pet.shelterName || '爱心动物救助站' }}</p>
              <div class="shelter-row">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#5C5A55" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ pet.locationProvince + '·' + (pet.locationCity || '') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Similar Pets -->
        <div v-if="similarPets.length > 0" class="section-padding">
          <h3>你可能也会喜欢</h3>
          <div class="similar-grid">
            <div v-for="s in similarPets" :key="s.id" class="similar-card" @click="router.push(`/detail/${s.id}`)">
              <div class="img-placeholder">{{ s.name?.charAt(0) || '🐕' }}</div>
              <div class="similar-body">
                <div class="similar-name">{{ s.name }}</div>
                <div class="similar-breed">{{ s.breed }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.gallery-img { width: 100%; height: 320px; object-fit: cover; border-radius: 14px; }
.gallery-thumb img { width: 100%; height: 100%; object-fit: cover; }
</style>
