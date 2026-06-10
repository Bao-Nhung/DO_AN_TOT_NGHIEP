<template>
  <div>
    <!-- Page Hero -->
    <div class="lm-page-hero" data-title="COLLECTION">
      <div class="container">
        <p class="lm-eyebrow mb-3">Bộ Sưu Tập</p>
        <h1 class="mb-3">Thu Đông <em>2025</em></h1>
        <p style="max-width:480px">Những thiết kế được lấy cảm hứng từ vẻ đẹp tối giản — nơi mỗi đường kim mũi chỉ đều mang trong mình một câu chuyện.</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="lm-filter-bar">
      <div class="container d-flex align-items-center gap-3 flex-wrap">
        <span style="font-size:9px;font-weight:600;letter-spacing:0.3em;text-transform:uppercase;color:var(--lm-black);flex-shrink:0">Lọc:</span>
        <div class="d-flex gap-2 flex-wrap">
          <button v-for="f in filters" :key="f"
                  class="lm-filter-tag" :class="{ active: activeFilter === f }"
                  @click="activeFilter = f">{{ f }}</button>
        </div>
        <div class="ms-auto d-flex align-items-center gap-2" style="font-size:10px;color:var(--lm-gray)">
          <span>Sắp xếp:</span>
          <select class="lm-input" style="width:auto;padding:8px 12px">
            <option>Mới nhất</option>
            <option>Giá tăng dần</option>
            <option>Giá giảm dần</option>
            <option>Phổ biến nhất</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Products Grid -->
    <div class="container py-5">
      <div class="row g-4">
        <div v-for="(p, i) in filteredProducts" :key="p.id"
             class="col-6 col-lg-4 lm-reveal" :style="{ transitionDelay: i * 0.07 + 's' }">
          <ProductCard :product="p" />
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ProductCard from '@/components/ui/ProductCard.vue'
import AppFooter   from '@/components/layout/AppFooter.vue'
import { useReveal }  from '@/composables/useReveal'
import { products }   from '@/composables/useProducts'

useReveal()

const filters = ['Tất cả', 'Đầm & Váy', 'Áo khoác', 'Quần', 'Phụ kiện', 'Sale']
const activeFilter = ref('Tất cả')

const filteredProducts = computed(() => {
  if (activeFilter.value === 'Tất cả') return products
  if (activeFilter.value === 'Sale') return products.filter(p => p.salePrice)
  return products.filter(p => {
    const cat = activeFilter.value.toLowerCase()
    return p.category.toLowerCase().includes(cat.split(' ')[0].toLowerCase())
  })
})
</script>
