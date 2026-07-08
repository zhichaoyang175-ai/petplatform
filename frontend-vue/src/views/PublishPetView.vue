<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import { useAuthStore } from '../stores/user'
import { createPet, uploadPetImages } from '../api/pets'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const success = ref('')

// 表单数据 — 对应 PetCreateRequest
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

// 图片上传
const images = ref([])
const imageFiles = ref([])
const uploading = ref(false)

// 品种列表
const breeds = ['金毛寻回犬', '柯基犬', '柴犬', '拉布拉多', '哈士奇', '泰迪', '博美',
  '布偶猫', '英国短毛猫', '橘猫', '美短', '暹罗猫', '波斯猫', '其他']

const provinces = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '南京', '重庆', '西安', '其他']

function handleImageSelect(e) {
  const files = Array.from(e.target.files || [])
  if (images.value.length + files.length > 5) {
    error.value = '最多上传5张图片'
    return
  }
  for (const file of files) {
    images.value.push(URL.createObjectURL(file))
    imageFiles.value.push(file)
  }
}

function removeImage(index) {
  images.value.splice(index, 1)
  imageFiles.value.splice(index, 1)
}

async function handleSubmit() {
  if (!form.value.name || !form.value.breed || form.value.gender === null) {
    error.value = '请填写宠物名称、品种和性别'
    return
  }
  // 角色前置校验：仅送养人(role=2)可发布，避免非送养人触发后端 401/403 被踢到登录页
  if (authStore.userInfo?.role !== 2) {
    error.value = '仅送养人可以发布领养信息，请先申请成为送养人'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const pet = await createPet(form.value)
    success.value = '发布成功！'

    // 上传图片（关联宠物，全量上传，写入 t_pet_image）
    if (imageFiles.value.length > 0) {
      uploading.value = true
      await uploadPetImages(pet.id, imageFiles.value)
      uploading.value = false
    }

    setTimeout(() => router.push(`/detail/${pet.id}`), 800)
  } catch (e) {
    error.value = e.message || '发布失败，请稍后重试'
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
        <h2 style="margin-bottom: 0;">发布领养信息</h2>
        <RouterLink to="/" class="text-muted" style="font-size: 14px;">← 返回首页</RouterLink>
      </div>

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
            <div v-for="(img, i) in images" :key="i" class="image-preview" @click="removeImage(i)">
              <img :src="img" alt="preview" />
              <span class="image-remove">×</span>
            </div>
            <label v-if="images.length < 5" class="image-add-btn">
              <input type="file" accept="image/*" multiple hidden @change="handleImageSelect" />
              <span>+</span>
              <small>添加照片</small>
            </label>
          </div>
        </div>

        <!-- 提交按钮 -->
        <button class="btn btn-primary btn-lg btn-block" style="margin-top: 24px;"
          :disabled="loading" @click="handleSubmit">
          {{ loading ? '发布中...' : '发布领养信息' }}
        </button>
      </div>
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
