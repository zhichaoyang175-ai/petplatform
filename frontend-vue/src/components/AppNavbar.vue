<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/user'

const props = defineProps({
  navLinks: {
    type: Array,
    default: () => [
      { text: '首页', to: '/' },
      { text: '浏览宠物', to: '/#pets' },
      { text: '关于我们', to: '/#about' },
    ],
  },
  showActions: {
    type: Boolean,
    default: true,
  },
  logoText: {
    type: String,
    default: 'PetHome',
  },
  admin: {
    type: Boolean,
    default: false,
  },
})

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const isApplicant = computed(() => authStore.userInfo?.role === 1)
const isReviewer = computed(() => authStore.userInfo?.role === 4)
const displayName = computed(() => authStore.userInfo?.nickname || '')
const avatarLetter = computed(() => displayName.value.charAt(0).toUpperCase())

// 顶部主导航：默认静态链接 + 条件追加的业务模块入口
const allNavLinks = computed(() => {
  const links = [...props.navLinks]
  links.push({ text: '领养故事', to: '/feed' })
  if (isLoggedIn.value) links.push({ text: '为你推荐', to: '/recommend' })
  return links
})

// 登录后显示的账户中心导航（统一收敛到 /profile/* 内联 tab，消除重复页面）
const accountLinks = computed(() => {
  const links = [
    { text: '通知', to: '/profile/notifications' },
    { text: '领养记录', to: '/profile/adoptions' },
    { text: '回访', to: '/profile/follow-ups' },
    { text: '收到的申请', to: '/profile/received' },
  ]
  if (isLoggedIn.value) links.push({ text: '预约看宠', to: '/visits' })
  if (isReviewer.value) links.push({ text: '审核中心', to: '/review-center' })
  return links
})

const mobileMenuOpen = ref(false)
function toggleMobileMenu() { mobileMenuOpen.value = !mobileMenuOpen.value }
function closeMobileMenu() { mobileMenuOpen.value = false }

// 处理锚点链接：如果在当前页面滚动，否则跳转
function handleNavClick(link) {
  closeMobileMenu()
  if (link.to.startsWith('/#') && window.location.pathname === '/') {
    const id = link.to.substring(2)
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-left">
      <RouterLink to="/" class="navbar-logo-link" style="display: flex; align-items: center; gap: 8px;">
        <span class="navbar-logo-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2Z" fill="#E07856"/>
            <path d="M19 10C19 13.9 15.4 17 12 22C8.6 17 5 13.9 5 10C5 7.2 6.5 4.8 8.8 3.5C10 2.8 11.5 3.5 12 4.8C12.5 3.5 14 2.8 15.2 3.5C17.5 4.8 19 7.2 19 10Z" fill="#E07856" opacity="0.3"/>
          </svg>
        </span>
        <span class="navbar-logo-text">{{ logoText }}</span>
      </RouterLink>
    </div>

    <button class="navbar-burger" :class="{ open: mobileMenuOpen }" @click="toggleMobileMenu" aria-label="菜单">
      <span></span><span></span><span></span>
    </button>

    <div class="navbar-center" :class="{ open: mobileMenuOpen }">
      <div class="navbar-links">
        <RouterLink
          v-for="link in allNavLinks"
          :key="link.text"
          :to="link.to"
          class="nav-link"
          @click="handleNavClick(link)"
        >
          {{ link.text }}
        </RouterLink>
        <template v-if="isLoggedIn">
          <RouterLink
            v-for="link in accountLinks"
            :key="link.text"
            :to="link.to"
            class="nav-link"
            @click="closeMobileMenu"
          >
            {{ link.text }}
          </RouterLink>
        </template>
      </div>
    </div>

    <div class="navbar-right">
      <div v-if="admin" class="navbar-avatar">A</div>
      <div v-else-if="showActions && isLoggedIn" class="navbar-actions">
        <RouterLink v-if="isApplicant" to="/profile/settings" class="btn btn-outline btn-sm" style="margin-right: 8px;" @click="closeMobileMenu">申请送养人</RouterLink>
        <RouterLink to="/publish" class="btn btn-primary btn-sm" style="margin-right: 12px;" @click="closeMobileMenu">发布领养</RouterLink>
        <RouterLink to="/profile/applications" class="navbar-avatar" :title="displayName" @click="closeMobileMenu">{{ avatarLetter }}</RouterLink>
      </div>
      <div v-else-if="showActions" class="navbar-actions">
        <RouterLink to="/login" class="btn btn-ghost">登录</RouterLink>
        <RouterLink to="/login" class="btn btn-primary">注册</RouterLink>
      </div>
    </div>
  </nav>
</template>
