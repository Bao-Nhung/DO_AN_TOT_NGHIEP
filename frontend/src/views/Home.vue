<script setup lang="ts">
import HeroBanner from '@/components/home/HeroBanner.vue'
import CategoryGrid from '@/components/home/CategoryGrid.vue'
import NewProducts from '@/components/home/NewProducts.vue'
import BestSellers from '@/components/home/BestSellers.vue'

defineOptions({
  name: 'HomePage',
})
</script>

<template>
  <main class="main-content">
    <HeroBanner />
    <CategoryGrid />
    <NewProducts />
    <BestSellers />
  </main>
</template>

<style scoped>
.main-content {
  min-height: calc(100vh - 300px);
}
</style>