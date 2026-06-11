<template>
  <div>
    <div class="lm-page-hero" data-title="COLLECTION">
      <div class="container">
        <p class="lm-eyebrow mb-3">Bo Suu Tap</p>
        <h1 class="mb-3">Tat ca <em>san pham</em></h1>
        <p style="max-width:480px">Kham pha bo suu tap vay doc quyen tu Zestia — noi moi thiet ke deu mang mot cau chuyen rieng.</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="lm-filter-bar">
      <div class="container">
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <span style="font-size:13px;font-weight:600;color:var(--z-dark);flex-shrink:0">Loc:</span>
          <div class="d-flex gap-2 flex-wrap">
            <button v-for="f in filters" :key="f"
                    class="lm-filter-tag" :class="{ active: activeFilter === f }"
                    @click="activeFilter = f">{{ f }}</button>
          </div>
          <div class="ms-auto d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <span style="font-size:13px;color:var(--z-gray);white-space:nowrap">Gia:</span>
              <select v-model="priceRange" class="lm-input" style="width:auto;padding:8px 12px;font-size:13px">
                <option value="all">Tat ca</option>
                <option value="under2m">Duoi 2 trieu</option>
                <option value="2m-5m">2 - 5 trieu</option>
                <option value="over5m">Tren 5 trieu</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span style="font-size:13px;color:var(--z-gray);white-space:nowrap">Sap xep:</span>
              <select v-model="sortBy" class="lm-input" style="width:auto;padding:8px 12px;font-size:13px">
                <option value="newest">Moi nhat</option>
                <option value="price-asc">Gia tang dan</option>
                <option value="price-desc">Gia giam dan</option>
                <option value="name">Ten A-Z</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Active filters -->
        <div v-if="activeFilter !== 'Tat ca' || priceRange !== 'all'" class="d-flex align-items-center gap-2 mt-3">
          <span style="font-size:12px;color:var(--z-gray)">Dang loc:</span>
          <span v-if="activeFilter !== 'Tat ca'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ activeFilter }}
            <i class="bi bi-x" style="cursor:pointer" @click="activeFilter = 'Tat ca'"></i>
          </span>
          <span v-if="priceRange !== 'all'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ priceLabels[priceRange] }}
            <i class="bi bi-x" style="cursor:pointer" @click="priceRange = 'all'"></i>
          </span>
          <button @click="activeFilter = 'Tat ca'; priceRange = 'all'"
                  style="font-size:12px;color:var(--z-gray);border:none;background:none;cursor:pointer;text-decoration:underline">
            Xoa tat ca
          </button>
        </div>
      </div>
    </div>

    <!-- Results count -->
    <div class="container pt-4 pb-2">
      <p style="font-size:13px;color:var(--z-gray)">
        Hien thi <strong style="color:var(--z-dark)">{{ sortedProducts.length }}</strong> san pham
      </p>
    </div>

    <!-- Products Grid -->
    <div class="container pb-5">
      <div class="row g-4">
        <div v-for="(p, i) in sortedProducts" :key="p.id"
             class="col-6 col-lg-4 lm-reveal" :style="{ transitionDelay: i * 0.05 + 's' }">
          <ProductCard :product="p" />
        </div>
      </div>

      <div v-if="sortedProducts.length === 0" class="text-center py-5">
        <i class="bi bi-search mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
        <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Khong tim thay san pham</h3>
        <p style="color:var(--z-gray);font-size:14px">Thu thay doi bo loc de tim san pham phu hop.</p>
        <button class="lm-btn-primary mt-3" @click="activeFilter = 'Tat ca'; priceRange = 'all'">
          <span>Xoa bo loc</span>
        </button>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ProductCard from '@/components/ui/ProductCard.vue'
import AppFooter   from '@/components/layout/AppFooter.vue'
import { useReveal }  from '@/composables/useReveal'
import { products, loadProducts } from '@/composables/useProducts'

useReveal()
onMounted(() => loadProducts())

const filters = ['Tat ca', 'Vay truyen thong', 'Vay cach tan', 'Vay da hoi', 'Vay cong so', 'Sale']
const activeFilter = ref('Tat ca')
const priceRange = ref('all')
const sortBy = ref('newest')

const priceLabels = {
  'under2m': 'Duoi 2 trieu',
  '2m-5m': '2 - 5 trieu',
  'over5m': 'Tren 5 trieu'
}

const filteredProducts = computed(() => {
  let result = [...products.value]

  if (activeFilter.value !== 'Tat ca') {
    if (activeFilter.value === 'Sale') {
      result = result.filter(p => p.salePrice)
    } else {
      result = result.filter(p => {
        const cat = activeFilter.value.toLowerCase()
        return p.category.toLowerCase().includes(cat.split(' ')[0].toLowerCase())
      })
    }
  }

  if (priceRange.value !== 'all') {
    result = result.filter(p => {
      const price = p.salePrice || p.price
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
  if (sortBy.value === 'price-asc') arr.sort((a, b) => (a.salePrice || a.price) - (b.salePrice || b.price))
  if (sortBy.value === 'price-desc') arr.sort((a, b) => (b.salePrice || b.price) - (a.salePrice || a.price))
  if (sortBy.value === 'name') arr.sort((a, b) => a.name.localeCompare(b.name))
  return arr
})
</script>
