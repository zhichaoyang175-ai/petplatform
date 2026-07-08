<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import { getDashboardStats } from '../api/admin'

const router = useRouter()
function goReview(id) {
  if (id) router.push('/admin/review/' + id)
}

const navLinks = [
  { text: '概览', to: '/admin' },
  { text: '宠物管理', to: '/admin/pets' },
  { text: '申请审核', to: '/admin' },
]

const loading = ref(true)
const error = ref('')
const dashboard = ref(null)

// 默认兜底数据
const fallbackReviews = [
  { applicantName: '张先生', petName: '布丁', status: '审核中', statusType: 'warning' },
  { applicantName: '李女士', petName: '雪球', status: '已通过', statusType: 'success' },
  { applicantName: '王先生', petName: '豆豆', status: '待回访', statusType: 'info' },
]

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    dashboard.value = await getDashboardStats()
  } catch (e) {
    error.value = e.message || '加载失败'
    dashboard.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div id="admin-dashboard" class="page-container">
    <AppNavbar :nav-links="navLinks" :show-actions="false" :admin="true" logo-text="PetHome Admin" />

    <div class="section-padding">
      <div class="flex-between">
        <h2 style="margin-bottom: 0;">管理后台</h2>
        <button v-if="error" class="btn btn-ghost btn-sm" @click="loadDashboard">刷新</button>
      </div>
    </div>

    <div v-if="loading" class="flex-center" style="padding: 60px 0;">
      <p class="text-muted">加载中...</p>
    </div>

    <template v-else>
      <div v-if="error" class="card-padded" style="text-align: center; padding: 32px; margin-bottom: 24px;">
        <p style="color: #dc2626; font-size: 14px; margin-bottom: 12px;">{{ error }}</p>
        <button class="btn btn-primary btn-sm" @click="loadDashboard">重试</button>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">待审核申请</div>
          <div class="kpi-value" style="color: #D4A574;">{{ dashboard?.pendingApplicationCount ?? 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">宠物总数</div>
          <div class="kpi-value" style="color: #87A878;">{{ dashboard?.petTotal ?? 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">用户总数</div>
          <div class="kpi-value" style="color: #D4A574;">{{ dashboard?.userTotal ?? 0 }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">成功领养</div>
          <div class="kpi-value" style="color: #E07856;">{{ dashboard?.adoptedCount ?? 0 }}</div>
        </div>
      </div>

      <div class="admin-two-col" style="margin-top: 24px;">
        <div class="admin-main-col">
          <div class="card-padded">
            <h3>近30天领养趋势</h3>
            <div class="img-placeholder" style="height: 300px; font-size: 48px;">
              <div style="text-align: center;">
                <span style="font-size: 48px;">📊</span>
                <div style="font-size: 14px; color: #BFBBB6; margin-top: 8px;">图表区域</div>
              </div>
            </div>
          </div>
        </div>
        <div class="admin-side-col">
          <div class="card-padded">
            <h3>最近审核 <span class="text-muted" style="font-size:12px;font-weight:400;">（点击查看详情）</span></h3>
            <div class="data-table" style="box-shadow: none;">
              <table>
                <thead><tr><th>申请人</th><th>宠物</th><th>状态</th></tr></thead>
                <tbody>
                  <tr v-for="(item, idx) in (dashboard?.recentReviews || fallbackReviews)" :key="idx"
                      :style="item.id ? 'cursor:pointer' : ''" @click="goReview(item.id)">
                    <td>{{ item.applicantName || '-' }}</td>
                    <td>{{ item.petName || '-' }}</td>
                    <td><span class="badge" :class="item.statusType === 'warning' ? 'badge-warning' : item.statusType === 'success' ? 'badge-success' : item.statusType === 'error' ? 'badge-danger' : 'badge-info'">{{ item.statusName || item.status || '-' }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
