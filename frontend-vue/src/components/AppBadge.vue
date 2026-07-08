<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'info',
    validator: (v) => ['success', 'warning', 'danger', 'info'].includes(v),
  },
  text: {
    type: String,
    required: true,
  },
})

const badgeClass = computed(() => `badge badge-${props.type}`)
</script>

<template>
  <span :class="badgeClass">{{ text }}</span>
</template>
