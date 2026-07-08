<script setup>
import { ref, onMounted, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import PetCard from '../components/PetCard.vue'
import StatCard from '../components/StatCard.vue'
import AppPagination from '../components/AppPagination.vue'
import { searchPets, getStats } from '../api/pets'

const loading = ref(true)
const error = ref('')
const pets = ref([])
const total = ref(0)
const stats = ref({ pendingAdoption: 2486, successfulAdoption: 5832, partnerShelters: 156 })

const activeChip = ref('全部')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 6

// 高级筛选
const genderFilter = ref('')
const neuteredFilter = ref('')
const cityFilter = ref('')
const ageFilter = ref('')

// 品种映射
const breedMap = { '狗狗': '犬', '猫猫': '猫', '小宠': '小宠' }

async function loadPets() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      keyword: keyword.value || undefined,
      breed: breedMap[activeChip.value] || undefined,
      gender: genderFilter.value === '' ? undefined : Number(genderFilter.value),
      neutered: neuteredFilter.value === '' ? undefined : Number(neuteredFilter.value),
      city: cityFilter.value.trim() || undefined,
      ageMin: ageFilter.value ? Number(ageFilter.value.split('-')[0]) : undefined,
      ageMax: ageFilter.value ? Number(ageFilter.value.split('-')[1]) : undefined,
    }
    // 清除 undefined 参数
    Object.keys(params).forEach(k => params[k] === undefined && delete params[k])

    const res = await searchPets(params)
    pets.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    error.value = e.message || '加载失败'
    // API 失败时使用演示数据兜底
    if (pets.value.length === 0) {
      pets.value = [
        { id: 1, name: '布丁', breed: '金毛寻回犬', age: '2岁', location: '上海·浦东新区', gender: '公', status: '可领养', emoji: '🐕' },
        { id: 2, name: '雪球', breed: '布偶猫', age: '1岁', location: '北京·朝阳区', gender: '母', status: '可领养', emoji: '🐱' },
        { id: 3, name: '豆豆', breed: '柯基犬', age: '3岁', location: '杭州·西湖区', gender: '公', status: '可领养', emoji: '🐕' },
        { id: 4, name: '米糕', breed: '英国短毛猫', age: '8个月', location: '深圳·南山区', gender: '母', status: '可领养', emoji: '🐱' },
        { id: 5, name: '阿福', breed: '柴犬', age: '1岁半', location: '成都·武侯区', gender: '公', status: '可领养', emoji: '🐕' },
        { id: 6, name: '咪咪', breed: '橘猫', age: '2岁', location: '广州·天河区', gender: '母', status: '可领养', emoji: '🐱' },
      ]
      total.value = 6
    }
  } finally {
    loading.value = false
  }
}

// 筛选切换时重新加载
function switchChip(chip) {
  activeChip.value = chip
  currentPage.value = 1
  loadPets()
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadPets()
}

// 重置高级筛选
function resetFilters() {
  genderFilter.value = ''
  neuteredFilter.value = ''
  cityFilter.value = ''
  ageFilter.value = ''
  currentPage.value = 1
  loadPets()
}

// 分页
function handlePageChange(page) {
  currentPage.value = page
  loadPets()
}

onMounted(async () => {
  loadPets()
  try {
    const s = await getStats()
    if (s) stats.value = s
  } catch (e) { /* 使用默认统计 */ }
})
</script>

<template>
  <div id="home" class="page-container">
    <AppNavbar :show-actions="true" />

    <!-- Hero -->
    <section class="hero-section">
      <div class="hero-text">
        <h1 class="hero-title">找到你的<br />毛茸茸伙伴</h1>
        <p class="hero-desc">
          每一只毛孩子都在等待一个温暖的家，<br />领养代替购买，给它们一个被爱的机会。
        </p>
        <div class="hero-btns">
          <RouterLink to="/#pets" class="btn btn-primary btn-lg">浏览待领养宠物</RouterLink>
          <RouterLink to="/#about" class="btn btn-outline btn-lg">了解领养流程</RouterLink>
        </div>
      </div>
      <div class="hero-img">
        <div class="img-placeholder">🐕</div>
      </div>
    </section>

    <!-- Stats -->
    <div class="stat-grid">
      <StatCard :number="String(stats.pendingAdoption || 0)" label="待领养宠物" color="#E07856" />
      <StatCard :number="String(stats.successfulAdoption || 0)" label="成功领养" color="#87A878" />
      <StatCard :number="String(stats.partnerShelters || 0)" label="合作救助站" color="#D4A574" />
    </div>

    <!-- Filter -->
    <div class="filter-bar">
      <div class="search-bar">
        <svg style="position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #BFBBB6;"
          width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input v-model="keyword" type="text" placeholder="搜索宠物名称、品种..."
          @keyup.enter="handleSearch" />
      </div>
      <div class="chip-group">
        <button v-for="chip in ['全部', '狗狗', '猫猫', '小宠']" :key="chip"
          class="chip" :class="{ active: activeChip === chip }"
          @click="switchChip(chip)">{{ chip }}</button>
      </div>
      <div class="filter-extra">
        <select v-model="genderFilter" class="input-field filter-select" @change="handleSearch">
          <option value="">性别不限</option>
          <option value="0">公</option>
          <option value="1">母</option>
        </select>
        <select v-model="neuteredFilter" class="input-field filter-select" @change="handleSearch">
          <option value="">绝育不限</option>
          <option value="1">已绝育</option>
        </select>
        <select v-model="ageFilter" class="input-field filter-select" @change="handleSearch">
          <option value="">年龄不限</option>
          <option value="0-1">1岁内</option>
          <option value="1-3">1-3岁</option>
          <option value="3-99">3岁以上</option>
        </select>
        <input v-model="cityFilter" class="input-field filter-select" placeholder="城市，如 上海"
          @keyup.enter="handleSearch" @change="handleSearch" />
        <button class="btn btn-ghost btn-sm" @click="resetFilters">重置</button>
      </div>
    </div>

    <!-- Pet Grid -->
    <section id="pets" class="section-padding">
      <div class="flex-between" style="margin-bottom: 20px;">
        <h2 style="margin-bottom: 0;">等待领养的毛孩子</h2>
        <span class="text-muted" style="font-size: 14px;">共 {{ total }} 只</span>
      </div>

      <div v-if="loading" class="flex-center" style="padding: 40px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <div v-else-if="pets.length === 0" class="card-padded" style="text-align: center; padding: 48px;">
        <p class="text-muted" style="font-size: 15px;">暂无符合条件的宠物</p>
      </div>

      <div v-else class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" />
      </div>
    </section>

    <AppPagination :current="currentPage" :total="Math.ceil(total / pageSize) || 1" @page="handlePageChange" />

    <!-- Footer -->
    <footer id="about" class="footer">
      <div class="footer-brand">
        <div class="footer-brand-name">PetHome</div>
        <div class="footer-brand-desc">让每一个毛孩子都能找到温暖的家</div>
      </div>
      <div class="footer-links">
        <a href="#">关于我们</a><a href="#">领养指南</a><a href="#">合作救助站</a>
        <a href="#">联系我们</a><a href="#">隐私政策</a>
      </div>
      <div class="footer-copy">&copy; 2024 PetHome. All rights reserved.</div>
    </footer>
  </div>
</template>

<style scoped>
.filter-extra {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.filter-select {
  width: auto;
  min-width: 120px;
}
</style>
