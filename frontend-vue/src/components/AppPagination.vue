<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    required: true,
  },
  total: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['page'])

const pages = computed(() => {
  const result = []
  if (props.total <= 7) {
    for (let i = 1; i <= props.total; i++) result.push(i)
  } else {
    result.push(1)
    if (props.current > 3) result.push('...')
    for (let i = Math.max(2, props.current - 1); i <= Math.min(props.total - 1, props.current + 1); i++) {
      result.push(i)
    }
    if (props.current < props.total - 2) result.push('...')
    result.push(props.total)
  }
  return result
})
</script>

<template>
  <div v-if="total > 1" class="pagination">
    <button
      class="page-btn"
      :disabled="current === 1"
      @click="emit('page', current - 1)"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="15 18 9 12 15 6" />
      </svg>
    </button>

    <template v-for="(p, idx) in pages" :key="idx">
      <span v-if="p === '...'" class="page-dots">...</span>
      <button
        v-else
        class="page-btn"
        :class="{ active: p === current }"
        @click="emit('page', p)"
      >
        {{ p }}
      </button>
    </template>

    <button
      class="page-btn"
      :disabled="current === total"
      @click="emit('page', current + 1)"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="9 18 15 12 9 6" />
      </svg>
    </button>
  </div>
</template>
