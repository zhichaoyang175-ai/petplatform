<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/user'
import { getFeed, likeFeed } from '../api/feeds'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const feedId = computed(() => Number(route.params.id))

const feed = ref(null)
const loading = ref(false)
const errorMsg = ref('')
const liking = ref(false)

const isLoggedIn = computed(() => authStore.isLoggedIn)

async function loadFeed() {
  loading.value = true
  errorMsg.value = ''
  try {
    const f = await getFeed(feedId.value)
    feed.value = {
      id: f.id,
      authorId: f.authorId,
      authorName: f.authorName || '匿名',
      petId: f.petId,
      content: f.content || '',
      images: Array.isArray(f.images) ? f.images : [],
      likeCount: f.likeCount || 0,
      createdAt: f.createdAt ? String(f.createdAt).substring(0, 16) : '',
    }
  } catch (e) {
    feed.value = null
    errorMsg.value = e.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!isLoggedIn.value) {
    router.push({ name: 'login', query: { redirect: `/feed/${feedId.value}` } })
    return
  }
  if (liking.value) return
  liking.value = true
  try {
    await likeFeed(feed.value.id)
    feed.value.likeCount += 1
  } catch (e) {
    errorMsg.value = e.message || '点赞失败，请稍后重试'
  } finally {
    liking.value = false
  }
}

onMounted(() => {
  loadFeed()
})
</script>

<template>
  <div id="feed-detail" class="page-container">
    <nav class="navbar">
      <div class="navbar-back" @click="router.back()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" />
        </svg>
        返回
      </div>
      <div class="navbar-logo-text">PetHome</div>
      <div></div>
    </nav>

    <div class="section-padding">
      <div v-if="loading" class="flex-center" style="padding: 48px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="errorMsg" class="card card-padded" style="text-align: center; padding: 48px;">
        <p class="text-muted" style="font-size: 15px;">{{ errorMsg }}</p>
        <RouterLink to="/feed" class="btn btn-primary" style="margin-top: 16px;">返回动态列表</RouterLink>
      </div>

      <div v-else-if="feed" class="content-section">
        <div class="card card-padded">
          <div class="app-card">
            <div class="app-img"><div class="img-placeholder">🐾</div></div>
            <div class="app-content">
              <div class="app-name">{{ feed.authorName }}</div>
              <div class="app-meta">{{ feed.createdAt }}</div>
            </div>
          </div>

          <p style="margin: 16px 0; line-height: 1.7; color: var(--text-secondary); white-space: pre-wrap; font-size: 15px;">{{ feed.content }}</p>

          <div v-if="feed.images.length" class="feed-images">
            <img
              v-for="(img, idx) in feed.images"
              :key="idx"
              :src="img"
              alt="feed image"
              class="feed-image"
            />
          </div>

          <div v-if="feed.petId" class="tag tag-warm" style="margin-top: 12px;">关联宠物 #{{ feed.petId }}</div>

          <div class="flex-row" style="margin-top: 20px;">
            <button class="btn btn-primary" :disabled="liking" @click="handleLike">❤ 点赞 {{ feed.likeCount }}</button>
            <RouterLink to="/feed" class="btn btn-ghost">返回列表</RouterLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feed-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
  margin-top: 8px;
}
.feed-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 10px;
}
</style>
