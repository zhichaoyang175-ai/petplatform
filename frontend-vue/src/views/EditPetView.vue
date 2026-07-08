<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import { useAuthStore } from '../stores/user'
import { getPetDetail, updatePet, uploadPetImages } from '../api/pets'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const petId = route.params.id
const loading = ref(false)
const pageLoading = ref(true)   // 详情加载态
const error = ref('')
const success = ref('')

// 表单数据 — 对应 PetUpdateRequest（全字段可选）
const form = ref({
  name: '',
  breed: '',
  gender: null,           // 0-未知 1-公 2-母
  ageMonths: null,
  weightKg: null,
  neutered: 0,            // 0-否 1-是
  healthStatus: 1,        // 1-健康 2-轻微疾病 3-治疗中 4-残疾
  locationProvince: '',
  locationCity: '',
  description: '',
  adoptionRequirements: '',
})

// 图片：已有图片 URL（只读预览），新选文件对象，新选文件预览 URL
const images = ref([])
const imageFiles = ref([])
const newImagePreviews = ref([])
const uploading = ref(false)

// 品种列表
const breeds = ['金毛寻回犬', '柯基犬', '柴犬', '拉布拉多', '哈士奇', '泰迪', '博美',
  '布偶猫', '英国短毛猫', '橘猫', '美短', '暹罗猫', '波斯猫', '其他']

const provinces = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '南京', '重庆', '西安', '其他']

// 加载详情并预填表单
onMounted(async () => {
  try {
    const d = await getPetDetail(petId)
    form.value = {
      name: d.name ?? '',
      breed: d.breed ?? '',
      gender: d.gender ?? null,
      ageMonths: d.ageMonths ?? null,
      weightKg: d.weightKg ?? null,
      neutered: d.neutered ? 1 : 0,
      healthStatus: d.healthStatus ?? 1,
      locationProvince: d.locationProvince ?? '',
      locationCity: d.locationCity ?? '',
      description: d.description ?? '',
      adoptionRequirements: d.adoptionRequirements ?? '',
    }
    // 已有图片回显（只读）
    if (d.images && d.images.length) {
      images.value = d.images.map((it) => it.url || it.imageUrl || '')
    } else if (d.coverImage) {
      images.value = [d.coverImage]
    }
  } catch (e) {
    error.value = e.message || '加载宠物信息失败'
  } finally {
    pageLoading.value = false
  }
})

function handleImageSelect(e) {
  const files = Array.from(e.target.files || [])
  if (images.value.length + imageFiles.value.length + files.length > 5) {
    error.value = '最多上传5张图片'
    return
  }
  for (const file of files) {
    newImagePreviews.value.push(URL.createObjectURL(file))
    imageFiles.value.push(file)
  }
  // 清空 input，允许重复选择同一文件
  e.target.value = ''
}

// 仅移除未上传的新图；已有图片为只读预览，不在本次范围
function removeImage(index) {
  newImagePreviews.value.splice(index, 1)
  imageFiles.value.splice(index, 1)
}

async function handleSubmit() {
  if (!form.value.name || !form.value.breed || form.value.gender === null) {
    error.value = '请填写宠物名称、品种和性别'
    return
  }
  // 角色前置校验：仅送养人(role=2)可编辑，避免非送养人触发后端异常
  if (authStore.userInfo?.role !== 2) {
    error.value = '仅送养人可以编辑领养信息'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const pet = await updatePet(petId, {
      name: form.value.name,
      breed: form.value.breed,
      gender: form.value.gender,
      ageMonths: form.value.ageMonths,
      weightKg: form.value.weightKg,
      neutered: form.value.neutered,
      healthStatus: form.value.healthStatus,
      locationProvince: form.value.locationProvince,
      locationCity: form.value.locationCity,
      description: form.value.description,
      adoptionRequirements: form.value.adoptionRequirements,
    })
    success.value = '保存成功！'

    // 上传新选图片（如有）
    if (imageFiles.value.length > 0) {
      uploading.value = true
      await uploadPetImages(petId, imageFiles.value)
      uploading.value = false
    }

    setTimeout(() => router.push(`/detail/${petId}`), 800)
  } catch (e) {
    error.value = e.message || '保存失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <AppNavbar :show-actions="true" />

    <div class="section-padding" style="max-width: 680px; margin: 0 auto;">
      <div class="flex-between" style="margin-bottom: 24px;">
        <h2 style="margin-bottom: 0;">编辑领养信息</h2>
        <RouterLink to="/profile/adoptions" class="text-muted" style="font-size: 14px;">← 返回领养记录</RouterLink>
      </div>

      <!-- 详情加载占位 -->
      <div v-if="pageLoading" class="flex-center" style="padding: 64px 0;">
        <p class="text-muted">加载中...</p>
      </div>

      <template v-else>
        <!-- Error / Success -->
        <div v-if="error" class="alert-error" style="margin-bottom: 16px;">{{ error }}</div>
        <div v-if="success" class="alert-success" style="margin-bottom: 16px;">{{ success }}</div>

        <div class="card-padded">
          <!-- 宠物名称 -->
          <div class="form-group">
            <label class="form-label">宠物名称 <span class="required">*</span></label>
            <input v-model="form.name" type="text" class="form-input" placeholder="如：布丁、雪球" maxlength="50" />
          </div>

          <!-- 品种 + 性别 -->
          <div class="form-row">
            <div class="form-group" style="flex: 1;">
              <label class="form-label">品种 <span class="required">*</span></label>
              <select v-model="form.breed" class="form-input">
                <option value="" disabled>请选择品种</option>
                <option v-for="b in breeds" :key="b" :value="b">{{ b }}</option>
              </select>
            </div>
            <div class="form-group" style="flex: 1;">
              <label class="form-label">性别 <span class="required">*</span></label>
              <div class="radio-group">
                <label class="radio-item"><input type="radio" v-model="form.gender" :value="1" /> 公</label>
                <label class="radio-item"><input type="radio" v-model="form.gender" :value="2" /> 母</label>
              </div>
            </div>
          </div>

          <!-- 月龄 + 体重 + 绝育 -->
          <div class="form-row form-row-3">
            <div class="form-group">
              <label class="form-label">月龄</label>
              <input v-model.number="form.ageMonths" type="number" class="form-input" placeholder="月" min="0" />
            </div>
            <div class="form-group">
              <label class="form-label">体重(kg)</label>
              <input v-model.number="form.weightKg" type="number" step="0.1" class="form-input" placeholder="kg" min="0" />
            </div>
            <div class="form-group">
              <label class="form-label">是否绝育</label>
              <select v-model="form.neutered" class="form-input">
                <option :value="0">否</option>
                <option :value="1">是</option>
              </select>
            </div>
          </div>

          <!-- 健康状态 -->
          <div class="form-group">
            <label class="form-label">健康状态</label>
            <select v-model="form.healthStatus" class="form-input">
              <option :value="1">健康</option>
              <option :value="2">轻微疾病</option>
              <option :value="3">治疗中</option>
              <option :value="4">残疾</option>
            </select>
          </div>

          <!-- 所在地区 -->
          <div class="form-row">
            <div class="form-group" style="flex: 1;">
              <label class="form-label">所在省</label>
              <select v-model="form.locationProvince" class="form-input">
                <option value="">请选择</option>
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>
            <div class="form-group" style="flex: 1;">
              <label class="form-label">所在市</label>
              <input v-model="form.locationCity" type="text" class="form-input" placeholder="如：浦东新区" />
            </div>
          </div>

          <!-- 详细描述 -->
          <div class="form-group">
            <label class="form-label">详细描述</label>
            <textarea v-model="form.description" class="form-textarea" rows="4"
              placeholder="描述宠物的性格、习惯、健康状况等..." maxlength="2000"></textarea>
          </div>

          <!-- 领养要求 -->
          <div class="form-group">
            <label class="form-label">领养要求</label>
            <textarea v-model="form.adoptionRequirements" class="form-textarea" rows="3"
              placeholder="如：有固定住所、接受定期回访、全家同意等..." maxlength="1000"></textarea>
          </div>

          <!-- 图片上传 -->
          <div class="form-group">
            <label class="form-label">宠物照片（最多5张）</label>
            <div class="image-upload-area">
              <!-- 已有图片（只读预览，不可删除） -->
              <div v-for="(img, i) in images" :key="'exist-' + i" class="image-preview image-readonly">
                <img :src="img" alt="preview" />
              </div>
              <!-- 新选图片（可删除） -->
              <div v-for="(img, i) in newImagePreviews" :key="'new-' + i" class="image-preview" @click="removeImage(i)">
                <img :src="img" alt="preview" />
                <span class="image-remove">×</span>
              </div>
              <label v-if="images.length + imageFiles.length < 5" class="image-add-btn">
                <input type="file" accept="image/*" multiple hidden @change="handleImageSelect" />
                <span>+</span>
                <small>添加照片</small>
              </label>
            </div>
            <p class="text-muted" style="font-size: 12px; margin-top: 6px;">已有照片为历史图片（只读），新增照片将在保存时上传。</p>
          </div>

          <!-- 提交按钮 -->
          <button class="btn btn-primary btn-lg btn-block" style="margin-top: 24px;"
            :disabled="loading || uploading" @click="handleSubmit">
            {{ loading ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: #595550; margin-bottom: 6px; }
.form-label .required { color: #E07856; }
.form-input, .form-textarea {
  width: 100%; padding: 10px 14px; border: 1.5px solid #DFD9D1; border-radius: 10px;
  font-size: 14px; color: #40342D; background: #FDFCFA; outline: none; transition: border .2s;
  box-sizing: border-box;
}
.form-input:focus, .form-textarea:focus { border-color: #E07856; }
.form-textarea { resize: vertical; font-family: inherit; }
.form-row { display: flex; gap: 16px; }
.form-row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
.radio-group { display: flex; gap: 16px; padding-top: 8px; }
.radio-item { display: flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }
.image-upload-area { display: flex; flex-wrap: wrap; gap: 10px; }
.image-preview { position: relative; width: 100px; height: 100px; border-radius: 10px; overflow: hidden; cursor: pointer; }
.image-preview.image-readonly { cursor: default; }
.image-preview img { width: 100%; height: 100%; object-fit: cover; }
.image-remove { position: absolute; top: 4px; right: 4px; background: rgba(0,0,0,.5); color: #fff; width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.image-add-btn { width: 100px; height: 100px; border: 2px dashed #DFD9D1; border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; color: #A69587; font-size: 24px; transition: border .2s; }
.image-add-btn:hover { border-color: #E07856; }
.image-add-btn small { font-size: 11px; margin-top: 2px; }
.alert-error { background: #FFF0ED; color: #C0392B; padding: 12px 16px; border-radius: 10px; font-size: 13px; }
.alert-success { background: #EDF7ED; color: #27AE60; padding: 12px 16px; border-radius: 10px; font-size: 13px; }
.btn-block { width: 100%; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
