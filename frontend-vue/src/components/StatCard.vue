<script setup>
defineProps({
  number: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    required: true,
  },
  color: {
    type: String,
    default: '#E07856',
  },
})
</script>

<template>
  <div class="stat-card">
    <div class="stat-number" :style="{ color }">{{ number }}</div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>
