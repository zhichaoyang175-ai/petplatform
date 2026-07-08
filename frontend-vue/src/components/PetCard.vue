<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/user'
import { addFavorite, removeFavorite, checkFavorited } from '../api/favorites'

const props = defineProps({
  pet: {
    type: Object,
    required: true,
  },
})

const router = useRouter()
const authStore = useAuthStore()

// 本地维护收藏状态
const isFavorited = ref(false)
const favLoading = ref(false)

onMounted(async () => {
  // 未登录不查询；列表卡片较多时默认 false，点击切换亦可
  if (!authStore.isLoggedIn) return
  try {
    const res = await checkFavorited(props.pet.id)
    // 后端返回 { favorited: boolean }，需用 .favorited 判定（!!res 对非空对象恒为 true）
    isFavorited.value = !!(res && res.favorited)
  } catch (e) {
    // 后端不在线/未返回时忽略，保持默认 false
  }
})

async function toggleFavorite() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (favLoading.value) return
  favLoading.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(props.pet.id)
      isFavorited.value = false
    } else {
      await addFavorite(props.pet.id)
      isFavorited.value = true
    }
  } catch (e) {
    // 失败时回滚状态
    isFavorited.value = !isFavorited.value
  } finally {
    favLoading.value = false
  }
}

function goDetail() {
  router.push(`/detail/${props.pet.id}`)
}
</script>

<template>
  <div class="card pet-card pet-card-clickable" @click="goDetail">
    <div class="pet-card-img">
      <img v-if="pet.coverImage" :src="pet.coverImage" :alt="pet.name" class="pet-card-cover" />
      <div v-else class="img-placeholder">{{ pet.name ? pet.name.charAt(0).toUpperCase() : '🐕' }}</div>
      <span v-if="pet.statusName" class="pet-card-badge">{{ pet.statusName }}</span>
      <!-- 收藏（心形）按钮 -->
      <button
        class="fav-btn"
        :class="{ 'fav-active': isFavorited }"
        :disabled="favLoading"
        :title="isFavorited ? '取消收藏' : '收藏'"
        @click.stop="toggleFavorite"
      >
        {{ isFavorited ? '♥' : '♡' }}
      </button>
    </div>
    <div class="pet-card-body">
      <div class="pet-card-header">
        <span class="pet-card-name">{{ pet.name }}</span>
        <span v-if="pet.genderName" class="tag tag-warm">{{ pet.genderName }}</span>
      </div>
      <div class="pet-card-breed">{{ pet.breed }}&nbsp;·&nbsp;{{ pet.ageMonths != null ? (pet.ageMonths < 12 ? pet.ageMonths + '个月' : Math.floor(pet.ageMonths / 12) + '岁') : '-' }}</div>
      <div v-if="pet.fullLocation" class="pet-card-location">{{ pet.fullLocation }}</div>
      <span class="pet-card-cta">了解更多</span>
    </div>
  </div>
</template>

<style scoped>
.fav-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-muted);
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  transition: all 0.2s;
}

.fav-btn:hover {
  color: var(--accent-error);
}

.fav-btn.fav-active {
  color: var(--accent-error);
}

.fav-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.pet-card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pet-card-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  z-index: 1;
}
</style>
