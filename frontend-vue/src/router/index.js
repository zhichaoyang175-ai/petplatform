import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DetailView from '../views/DetailView.vue'
import ApplyView from '../views/ApplyView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import PublishPetView from '../views/PublishPetView.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminPets from '../views/AdminPets.vue'
import AdminReview from '../views/AdminReview.vue'
import ReviewCenter from '../views/ReviewCenter.vue'
import EditPetView from '../views/EditPetView.vue'
import FeedView from '../views/FeedView.vue'
import FeedDetailView from '../views/FeedDetailView.vue'
import RecommendView from '../views/RecommendView.vue'
import EvaluationsView from '../views/EvaluationsView.vue'
import VisitsView from '../views/VisitsView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView, meta: { title: 'PetHome - 首页' } },
  { path: '/detail/:id', name: 'detail', component: DetailView, meta: { title: '宠物详情' } },
  { path: '/apply/:id', name: 'apply', component: ApplyView, meta: { title: '领养申请', requiresAuth: true } },
  { path: '/login', name: 'login', component: LoginView, meta: { title: '登录注册', guestOnly: true } },
  { path: '/publish', name: 'publish', component: PublishPetView, meta: { title: '发布领养信息', requiresAuth: true } },
  { path: '/pets/:id/edit', name: 'pet-edit', component: EditPetView, meta: { title: '编辑领养信息', requiresAuth: true } },
  { path: '/profile', redirect: '/profile/applications' },
  {
    path: '/profile/:tab(applications|favorites|settings|notifications|adoptions|my-pets|follow-ups|received|review)',
    name: 'profile',
    component: ProfileView,
    meta: { requiresAuth: true },
  },
  { path: '/admin', name: 'admin-dashboard', component: AdminDashboard, meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/pets', name: 'admin-pets', component: AdminPets, meta: { title: '宠物管理', requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/review/:id', name: 'admin-review', component: AdminReview, meta: { title: '审核详情', requiresAuth: true, requiresAdmin: true } },
  { path: '/notifications', redirect: '/profile/notifications' },
  { path: '/adoptions', redirect: '/profile/adoptions' },
  { path: '/follow-ups', redirect: '/profile/follow-ups' },
  { path: '/applications/received', redirect: '/profile/received' },
  { path: '/review-center', name: 'review-center', component: ReviewCenter, meta: { title: '审核中心', requiresAuth: true, requiresReviewer: true } },
  { path: '/feed', name: 'feed', component: FeedView, meta: { title: '领养故事' } },
  { path: '/feed/:id', name: 'feed-detail', component: FeedDetailView, meta: { title: '领养故事详情' } },
  { path: '/recommend', name: 'recommend', component: RecommendView, meta: { title: '为你推荐', requiresAuth: true } },
  { path: '/evaluations', name: 'evaluations', component: EvaluationsView, meta: { title: '信用评价' } },
  { path: '/visits', name: 'visits', component: VisitsView, meta: { title: '预约看宠', requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// 全局前置守卫 — 鉴权与角色校验
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  const isLoggedIn = !!token
  const isAdmin = userInfo?.role === 3

  // 已登录用户访问登录页 → 重定向到首页
  if (to.meta.guestOnly && isLoggedIn) {
    return next('/')
  }

  // 需要登录但未登录 → 跳转登录页并记录 redirect
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  // 需要管理员权限但不是管理员 → 跳转首页
  if (to.meta.requiresAdmin && !isAdmin) {
    return next('/')
  }

  // 需要送养人权限但当前角色不是送养人(role!=2) → 跳转首页
  if (to.meta.requiresAdopter && userInfo?.role !== 2) {
    return next('/')
  }

  // 需要审核员权限但当前角色不是审核员(role!=4) → 跳转首页
  if (to.meta.requiresReviewer && userInfo?.role !== 4) {
    return next('/')
  }

  next()
})

router.afterEach((to) => { document.title = to.meta.title || 'PetHome' })
export default router
