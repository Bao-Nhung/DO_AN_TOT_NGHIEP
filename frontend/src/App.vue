<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<template>
  <AppHeader />
  <router-view />
  <AppFooter />
</template>

<style scoped>
body {
  margin: 0;
  padding: 0;
}
</style>
