<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { submitApplication } from '../api/applications'
import { getPetDetail } from '../api/pets'

const router = useRouter()
const route = useRoute()
const petId = computed(() => Number(route.params.id))

const submitting = ref(false)
const submitted = ref(false)
const errorMsg = ref('')
const pet = ref(null)

const housingOptions = [
  { label: '自有住房', value: 1 },
  { label: '租房', value: 2 },
  { label: '合租', value: 3 },
]

const petExperienceOptions = [
  { label: '无养宠经验', value: 0 },
  { label: '有过养宠经验', value: 1 },
  { label: '正在养宠', value: 2 },
]

const form = reactive({
  housingType: 1,
  monthlyIncome: '',
  petExperience: 1,
  familyAttitude: '',
  currentPets: '',
  reason: '',
})

const fieldErrors = reactive({})

function validate() {
  Object.keys(fieldErrors).forEach(k => delete fieldErrors[k])
  if (form.housingType == null) fieldErrors.housingType = '请选择住房情况'
  if (!form.monthlyIncome || Number(form.monthlyIncome) <= 0) fieldErrors.monthlyIncome = '请输入月收入'
  if (form.petExperience == null) fieldErrors.petExperience = '请选择养宠经验'
  if (!form.familyAttitude.trim()) fieldErrors.familyAttitude = '请填写家人态度'
  if (!form.reason.trim()) fieldErrors.reason = '请说明领养原因'
  return Object.keys(fieldErrors).length === 0
}

async function handleSubmit() {
  errorMsg.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    await submitApplication({
      petId: petId.value,
      housingType: form.housingType,
      monthlyIncome: Number(form.monthlyIncome),
      petExperience: form.petExperience,
      familyAttitude: form.familyAttitude,
      currentPets: form.currentPets,
      reason: form.reason,
    })
    submitted.value = true
  } catch (e) {
    errorMsg.value = e.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

// 加载宠物信息用于展示
onMounted(async () => {
  try {
    pet.value = await getPetDetail(petId.value)
  } catch (e) { /* 展示失败不影响表单使用 */ }
})
</script>

<template>
  <div id="apply" class="page-container">
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

    <div class="section-padding flex-center">
      <div v-if="submitted" class="card-padded" style="max-width: 680px; width: 100%; text-align: center; padding: 64px;">
        <div style="font-size: 48px; margin-bottom: 16px;">✅</div>
        <h2 style="margin-bottom: 8px;">申请已提交</h2>
        <p class="text-muted" style="margin-bottom: 24px;">我们将在3个工作日内审核你的申请并联系你。</p>
        <button class="btn btn-primary btn-lg" @click="router.push('/')">返回首页</button>
      </div>

      <div v-else class="card-padded" style="max-width: 680px; width: 100%;">
        <h2>领养申请表</h2>
        <p class="text-muted" style="margin-bottom: 24px; font-size: 14px;">
          你正在为<strong>{{ pet?.name || '宠物' }}</strong>
          （{{ pet?.breed || '' }}{{ pet?.ageMonths ? ' · ' + (pet.ageMonths < 12 ? pet.ageMonths + '个月' : Math.floor(pet.ageMonths/12) + '岁') : '' }}）提交领养申请
        </p>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="form-error-banner">{{ errorMsg }}</div>

        <form @submit.prevent="handleSubmit">
          <h3>住房与收入</h3>
          <div class="input-group">
            <label>住房情况</label>
            <div class="radio-group">
              <div v-for="option in housingOptions" :key="option.value"
                class="radio-item" :class="{ active: form.housingType === option.value }"
                @click="form.housingType = option.value">{{ option.label }}</div>
            </div>
            <span v-if="fieldErrors.housingType" class="field-error">{{ fieldErrors.housingType }}</span>
          </div>
          <div class="input-group">
            <label>月收入（元）</label>
            <input v-model.number="form.monthlyIncome" class="input-field" type="number" placeholder="请输入月收入" min="0" />
            <span v-if="fieldErrors.monthlyIncome" class="field-error">{{ fieldErrors.monthlyIncome }}</span>
          </div>

          <div class="divider"></div>

          <h3>养宠经验</h3>
          <div class="input-group">
            <label>养宠经验</label>
            <div class="radio-group">
              <div v-for="option in petExperienceOptions" :key="option.value"
                class="radio-item" :class="{ active: form.petExperience === option.value }"
                @click="form.petExperience = option.value">{{ option.label }}</div>
            </div>
            <span v-if="fieldErrors.petExperience" class="field-error">{{ fieldErrors.petExperience }}</span>
          </div>
          <div class="input-group">
            <label>家人态度</label>
            <input v-model="form.familyAttitude" class="input-field" type="text" placeholder="家人是否支持领养宠物" />
            <span v-if="fieldErrors.familyAttitude" class="field-error">{{ fieldErrors.familyAttitude }}</span>
          </div>
          <div class="input-group">
            <label>现有宠物</label>
            <input v-model="form.currentPets" class="input-field" type="text" placeholder="家中是否已有其他宠物（选填）" />
          </div>

          <div class="divider"></div>

          <h3>领养原因</h3>
          <div class="input-group">
            <label>为什么想领养？</label>
            <textarea v-model="form.reason" class="textarea" placeholder="请说明你想领养的原因" style="min-height: 120px;"></textarea>
            <span v-if="fieldErrors.reason" class="field-error">{{ fieldErrors.reason }}</span>
          </div>

          <div class="flex-between" style="margin-top: 24px;">
            <span style="font-size: 13px; color: var(--text-muted);">提交即表示同意《领养协议》条款</span>
            <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交申请' }}
            </button>
          </div>
        </form>
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
.field-error {
  display: block;
  color: #dc2626;
  font-size: 12px;
  margin-top: 4px;
}
</style>
