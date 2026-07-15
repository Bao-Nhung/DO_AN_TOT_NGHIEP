<template>
  <div>
    <div class="lm-page-hero" data-title="COLLECTION">
      <div class="container">
        <p class="lm-eyebrow mb-3">Bộ Sưu Tập</p>
        <h1 class="mb-3">Tất cả <em>sản phẩm</em></h1>
        <p style="max-width:480px">Khám phá bộ sưu tập váy độc quyền từ Zestia — nơi mỗi thiết kế đều mang một câu chuyện riêng.</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="lm-filter-bar">
      <div class="container">
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <span style="font-size:13px;font-weight:600;color:var(--z-dark);flex-shrink:0">Lọc:</span>
          <div class="d-flex gap-2 flex-wrap">
            <button v-for="f in filters" :key="f"
                    class="lm-filter-tag" :class="{ active: activeFilter === f }"
                    @click="activeFilter = f">{{ f }}</button>
          </div>
          <div class="ms-auto d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <span style="font-size:13px;color:var(--z-gray);white-space:nowrap">Giá:</span>
              <select v-model="priceRange" class="lm-input" style="width:auto;padding:8px 12px;font-size:13px">
                <option value="all">Tất cả</option>
                <option value="under2m">Dưới 2 triệu</option>
                <option value="2m-5m">2 - 5 triệu</option>
                <option value="over5m">Trên 5 triệu</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span style="font-size:13px;color:var(--z-gray);white-space:nowrap">Sắp xếp:</span>
              <select v-model="sortBy" class="lm-input" style="width:auto;padding:8px 12px;font-size:13px">
                <option value="newest">Mới nhất</option>
                <option value="bestseller">Bán chạy</option>
                <option value="price-asc">Giá tăng dần</option>
                <option value="price-desc">Giá giảm dần</option>
                <option value="name">Tên A-Z</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Active filters -->
        <div v-if="activeFilter !== 'Tất cả' || priceRange !== 'all'" class="d-flex align-items-center gap-2 mt-3">
          <span style="font-size:12px;color:var(--z-gray)">Đang lọc:</span>
          <span v-if="activeFilter !== 'Tất cả'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ activeFilter }}
            <i class="bi bi-x" style="cursor:pointer" @click="activeFilter = 'Tất cả'"></i>
          </span>
          <span v-if="priceRange !== 'all'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ priceLabels[priceRange] }}
            <i class="bi bi-x" style="cursor:pointer" @click="priceRange = 'all'"></i>
          </span>
          <button @click="activeFilter = 'Tất cả'; priceRange = 'all'"
                  style="font-size:12px;color:var(--z-gray);border:none;background:none;cursor:pointer;text-decoration:underline">
            Xoá tất cả
          </button>
        </div>
      </div>
    </div>

    <!-- Results count -->
    <div class="container pt-4 pb-2">
      <p style="font-size:13px;color:var(--z-gray)">
        Hiển thị <strong style="color:var(--z-dark)">{{ sortedProducts.length }}</strong> sản phẩm
      </p>
    </div>

    <!-- Products Grid -->
    <div class="container pb-5">
      <div class="row g-4">
        <div v-for="(p, i) in sortedProducts" :key="p.id"
             class="col-6 col-lg-4 z-fade-item" :style="{ animationDelay: i * 0.04 + 's' }">
          <ProductCard :product="p" />
        </div>
      </div>

      <div v-if="sortedProducts.length === 0" class="text-center py-5">
        <i class="bi bi-search mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
        <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Không tìm thấy sản phẩm</h3>
        <p style="color:var(--z-gray);font-size:14px">Thử thay đổi bộ lọc để tìm sản phẩm phù hợp.</p>
        <button class="lm-btn-primary mt-3" @click="activeFilter = 'Tất cả'; priceRange = 'all'">
          <span>Xoá bộ lọc</span>
        </button>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ui/ProductCard.vue'
import AppFooter   from '@/components/layout/AppFooter.vue'
import { products, loadProducts } from '@/composables/useProducts'
import { api } from '@/composables/useApi'

const route = useRoute()
const bestsellerRank = ref(new Map())

onMounted(async () => {
  await loadProducts()
  try {
    const summary = await api().getStorefrontSummary()
    bestsellerRank.value = new Map((summary?.bestsellers || []).map((item, index) => [Number(item.productId), index]))
  } catch (error) {
    console.warn('Không tải được thứ tự bán chạy', error)
  }
  applyRouteQuery()
})
watch(() => route.query, applyRouteQuery, { deep: true })

const filters = computed(() => [
  'Tất cả',
  ...new Set(products.value.map(p => p.category).filter(Boolean)),
  'Ưu đãi'
])
const activeFilter = ref('Tất cả')
const priceRange = ref('all')
const sortBy = ref('newest')

function applyRouteQuery() {
  const requestedSort = String(route.query.sort || '')
  if (['newest', 'bestseller', 'price-asc', 'price-desc', 'name'].includes(requestedSort)) {
    sortBy.value = requestedSort
  }
  const category = String(route.query.category || '').trim().toLowerCase()
  const occasion = String(route.query.occasion || '').trim().toLowerCase()
  const keywords = {
    work: ['công sở'],
    party: ['dạ hội', 'dự tiệc'],
    wedding: ['cưới'],
    date: ['cách tân', 'lụa']
  }[occasion] || (category ? [category] : [])
  if (keywords.length) {
    const match = filters.value.find(filter => keywords.some(keyword => filter.toLowerCase().includes(keyword)))
    activeFilter.value = match || 'Tất cả'
  }
}

const priceLabels = {
  'under2m': 'Dưới 2 triệu',
  '2m-5m': '2 - 5 triệu',
  'over5m': 'Trên 5 triệu'
}

const filteredProducts = computed(() => {
  let result = [...products.value]

  if (activeFilter.value !== 'Tất cả') {
    if (activeFilter.value === 'Ưu đãi') {
      result = result.filter(p => p.promotionActive)
    } else {
      result = result.filter(p => {
        return p.category && p.category.toLowerCase().includes(activeFilter.value.toLowerCase())
      })
    }
  }

  if (priceRange.value !== 'all') {
    result = result.filter(p => {
      const price = p.price
      if (priceRange.value === 'under2m') return price < 2000000
      if (priceRange.value === '2m-5m') return price >= 2000000 && price <= 5000000
      if (priceRange.value === 'over5m') return price > 5000000
      return true
    })
  }

  return result
})

const sortedProducts = computed(() => {
  const arr = [...filteredProducts.value]
  if (sortBy.value === 'price-asc') arr.sort((a, b) => a.price - b.price)
  if (sortBy.value === 'price-desc') arr.sort((a, b) => b.price - a.price)
  if (sortBy.value === 'name') arr.sort((a, b) => a.name.localeCompare(b.name))
  if (sortBy.value === 'bestseller') arr.sort((a, b) =>
    (bestsellerRank.value.get(Number(a.id)) ?? Number.MAX_SAFE_INTEGER)
    - (bestsellerRank.value.get(Number(b.id)) ?? Number.MAX_SAFE_INTEGER)
  )
  if (sortBy.value === 'newest') arr.sort((a, b) => Number(b.id) - Number(a.id))
  return arr
})
</script>

<style scoped>
.z-fade-item {
  animation: z-item-in 0.5s ease both;
}
@keyframes z-item-in {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
