<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="section-title text-center mb-5">
    <h2 class="fw-bold position-relative d-inline-block">
      {{ title }}
      <span class="title-underline"></span>
    </h2>
    <p v-if="subtitle" class="text-muted mt-2 fs-5">{{ subtitle }}</p>
  </div>
</template>

<style scoped>
.title-underline {
  display: block;
  width: 50px;
  height: 3px;
  background-color: #e91e63;
  margin: 8px auto 0;
  border-radius: 2px;
}
</style>
