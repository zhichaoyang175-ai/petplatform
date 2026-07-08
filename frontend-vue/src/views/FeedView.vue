<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/user'
import { listFeeds, createFeed, likeFeed } from '../api/feeds'

const router = useRouter()
const authStore = useAuthStore()

const feeds = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const errorMsg = ref('')

// 发布弹层
const showPublish = ref(false)
const publishing = ref(false)
const publishError = ref('')
const publishForm = reactive({ content: '', imageUrls: '' })

const isLoggedIn = computed(() => authStore.isLoggedIn)

async function loadFeeds() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await listFeeds({ page: page.value, size: size.value })
    feeds.value = (res?.records || []).map((f) => ({
      id: f.id,
      authorId: f.authorId,
      authorName: f.authorName || '匿名',
      petId: f.petId,
      content: f.content || '',
      images: Array.isArray(f.images) ? f.images : [],
      likeCount: f.likeCount || 0,
      createdAt: f.createdAt ? String(f.createdAt).substring(0, 16) : '',
    }))
    total.value = res?.total || 0
  } catch (e) {
    feeds.value = []
    errorMsg.value = e.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function openPublish() {
  publishError.value = ''
  publishForm.content = ''
  publishForm.imageUrls = ''
  showPublish.value = true
}

function closePublish() {
  showPublish.value = false
}

async function handlePublish() {
  publishError.value = ''
  if (!publishForm.content.trim()) {
    publishError.value = '请填写动态内容'
    return
  }
  publishing.value = true
  try {
    const images = publishForm.imageUrls
      .split(/[\n,，]/)
      .map((s) => s.trim())
      .filter(Boolean)
    await createFeed({
      content: publishForm.content.trim(),
      images,
    })
    showPublish.value = false
    page.value = 1
    await loadFeeds()
  } catch (e) {
    publishError.value = e.message || '发布失败，请稍后重试'
  } finally {
    publishing.value = false
  }
}

async function handleLike(feed) {
  if (!isLoggedIn.value) {
    router.push({ name: 'login', query: { redirect: '/feed' } })
    return
  }
  try {
    await likeFeed(feed.id)
    feed.likeCount += 1
  } catch (e) {
    errorMsg.value = e.message || '点赞失败，请稍后重试'
  }
}

function goDetail(id) {
  router.push(`/feed/${id}`)
}

function goLogin() {
  router.push({ name: 'login', query: { redirect: '/feed' } })
}

onMounted(() => {
  loadFeeds()
})
</script>

<template>
  <div id="feed" class="page-container">
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
      <div class="flex-between" style="margin-bottom: 20px;">
        <div>
          <h2 style="margin: 0;">领养故事</h2>
          <p class="text-muted" style="margin: 6px 0 0; font-size: 14px;">一起分享毛孩子的温暖瞬间</p>
        </div>
        <button v-if="isLoggedIn" class="btn btn-primary" @click="openPublish">发布动态</button>
        <button v-else class="btn btn-primary" @click="goLogin">登录后发布</button>
      </div>

      <div v-if="loading" class="flex-center" style="padding: 48px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="errorMsg" class="card card-padded" style="text-align: center; padding: 48px;">
        <p class="text-muted" style="font-size: 15px;">{{ errorMsg }}</p>
      </div>

      <div v-else-if="feeds.length === 0" class="card card-padded" style="text-align: center; padding: 56px;">
        <p class="text-muted" style="font-size: 15px;">还没有人分享故事，快来发布第一条吧～</p>
        <button v-if="isLoggedIn" class="btn btn-primary" style="margin-top: 16px;" @click="openPublish">发布动态</button>
      </div>

      <div v-else class="content-section">
        <div
          v-for="feed in feeds"
          :key="feed.id"
          class="card card-padded"
          style="margin-bottom: 16px; cursor: pointer;"
          @click="goDetail(feed.id)"
        >
          <div class="flex-between">
            <div class="app-card" style="cursor: default;" @click.stop>
              <div class="app-img"><div class="img-placeholder">🐾</div></div>
              <div class="app-content">
                <div class="app-name">{{ feed.authorName }}</div>
                <div class="app-meta">{{ feed.createdAt }}</div>
              </div>
            </div>
          </div>

          <p style="margin: 12px 0; line-height: 1.6; color: var(--text-secondary); white-space: pre-wrap;">{{ feed.content }}</p>

          <div v-if="feed.images.length" class="feed-images">
            <img
              v-for="(img, idx) in feed.images"
              :key="idx"
              :src="img"
              alt="feed image"
              class="feed-image"
              @click.stop
            />
          </div>

          <div class="flex-between" style="margin-top: 12px;">
            <span v-if="feed.petId" class="tag tag-warm">关联宠物 #{{ feed.petId }}</span>
            <span v-else></span>
            <button
              class="btn btn-outline btn-sm"
              @click.stop="handleLike(feed)"
            >❤ {{ feed.likeCount }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布动态弹层 -->
    <div v-if="showPublish" class="modal-mask" @click.self="closePublish">
      <div class="card card-padded modal-body">
        <h4>发布动态</h4>
        <div v-if="publishError" class="form-error-banner">{{ publishError }}</div>
        <div class="input-group">
          <label>动态内容</label>
          <textarea v-model="publishForm.content" class="textarea" placeholder="分享你的领养故事..." style="min-height: 120px;"></textarea>
        </div>
        <div class="input-group">
          <label>图片 URL（多个用逗号或换行分隔，可选）</label>
          <textarea v-model="publishForm.imageUrls" class="textarea" placeholder="https://.../a.jpg, https://.../b.jpg"></textarea>
        </div>
        <div class="flex-row">
          <button class="btn btn-primary btn-sm" :disabled="publishing" @click="handlePublish">
            {{ publishing ? '发布中...' : '发布' }}
          </button>
          <button class="btn btn-ghost btn-sm" @click="closePublish">取消</button>
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
.feed-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  margin-top: 4px;
}
.feed-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}
/* 复用 ProfileView 的弹层样式 */
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
