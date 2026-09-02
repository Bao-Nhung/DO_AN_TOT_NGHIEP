<template>
  <div>
    <div class="lm-page-hero" data-title="COLLECTION">
      <div class="container">
        <p class="lm-eyebrow mb-3">Bộ Sưu Tập</p>
        <h1 class="mb-3">Tất cả <em>sản phẩm</em></h1>
        <p style="max-width:480px">Khám phá bộ sưu tập thời trang độc quyền từ Zestia — nơi mỗi thiết kế đều mang một câu chuyện riêng.</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="lm-filter-bar">
      <div class="container">
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <span style="font-size:13px;font-weight:600;color:var(--z-dark);flex-shrink:0">Lọc:</span>
          <div class="d-flex gap-2 flex-wrap">
            <button v-for="f in filters" :key="f"
                    class="lm-filter-tag d-inline-flex align-items-center gap-2" :class="{ active: activeFilter === f }"
                    @click="selectFilter(f)">
              <img v-if="getFilterImage(f)" :src="getFilterImage(f)" :alt="f"
                   style="width:22px;height:22px;border-radius:50%;object-fit:cover;border:1px solid rgba(0,0,0,0.12);flex-shrink:0" />
              <i v-else-if="f === 'Tất cả'" class="bi bi-grid-fill" style="font-size:12px"></i>
              <span>{{ f }}</span>
            </button>
          </div>
          <div class="ms-auto d-flex align-items-center gap-3">
            <button type="button" class="lm-btn-secondary z-visual-search-btn" @click="showVisualModal = true">
              <i class="bi bi-camera-fill"></i> Tìm theo màu từ ảnh
            </button>

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

        <!-- Image color search results -->
        <div v-if="aiVisualResults" class="z-visual-result p-3 mt-3 text-start">
          <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
            <div class="d-flex align-items-center gap-2 text-danger fw-bold" style="font-size:14px;">
              <i class="bi bi-eyedropper fs-5"></i>
              <span>Kết quả tìm theo màu ảnh ({{ aiVisualResults.results?.length || 0 }} sản phẩm phù hợp)</span>
            </div>
            <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-3" style="font-size:12px;" @click="clearAiVisualSearch">
              <i class="bi bi-x-circle me-1"></i> Xóa kết quả ảnh
            </button>
          </div>
          <div class="d-flex gap-2 flex-wrap">
            <span v-for="tag in aiVisualResults.detectedTags" :key="tag" class="badge bg-danger">
              <i class="bi bi-tag-fill me-1"></i>{{ tag }}
            </span>
          </div>
        </div>

        <!-- Active filters -->
        <div v-if="activeFilter !== 'Tất cả' || priceRange !== 'all'" class="d-flex align-items-center gap-2 mt-3">
          <span style="font-size:12px;color:var(--z-gray)">Đang lọc:</span>
          <span v-if="activeFilter !== 'Tất cả'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ activeFilter }}
            <i class="bi bi-x" style="cursor:pointer" @click="selectFilter('Tất cả')"></i>
          </span>
          <span v-if="priceRange !== 'all'"
                style="font-size:12px;padding:4px 12px;background:var(--z-accent-soft);color:var(--z-accent);border-radius:20px;display:inline-flex;align-items:center;gap:4px">
            {{ priceLabels[priceRange] }}
            <i class="bi bi-x" style="cursor:pointer" @click="priceRange = 'all'"></i>
          </span>
          <button type="button" class="z-text-action" @click="selectFilter('Tất cả'); priceRange = 'all'">
            Xoá tất cả
          </button>
        </div>
      </div>
    </div>

    <!-- Results count -->
    <div class="container pt-4 pb-2">
      <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap">
        <p class="mb-0" style="font-size:13px;color:var(--z-gray)">
          Hiển thị
          <strong style="color:var(--z-dark)">{{ resultStart }}-{{ resultEnd }}</strong>
          / {{ sortedProducts.length }} sản phẩm
        </p>
        <PageSizeSelect v-model="pageSize" :options="[6, 12, 24, 48]" />
      </div>
    </div>

    <!-- Products Grid -->
    <div class="container pb-5">
      <div class="row g-4">
        <div v-for="(p, i) in paginatedProducts" :key="p.id"
             class="col-6 col-lg-4 z-fade-item" :style="{ animationDelay: i * 0.04 + 's' }">
          <ProductCard :product="p" />
        </div>
      </div>

      <div v-if="sortedProducts.length === 0" class="text-center py-5">
        <i class="bi bi-search mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
        <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Không tìm thấy sản phẩm</h3>
        <p style="color:var(--z-gray);font-size:14px">Thử thay đổi bộ lọc để tìm sản phẩm phù hợp.</p>
        <button class="lm-btn-primary mt-3" @click="selectFilter('Tất cả'); priceRange = 'all'">
          <span>Xoá bộ lọc</span>
        </button>
      </div>
      <nav v-else-if="totalPages > 1" class="z-public-pagination" aria-label="Phân trang sản phẩm">
        <button type="button" class="z-page-button" :disabled="currentPage === 1" @click="currentPage--">
          <i class="bi bi-arrow-left"></i><span>Trước</span>
        </button>
        <span>Trang {{ currentPage }} / {{ totalPages }}</span>
        <button type="button" class="z-page-button" :disabled="currentPage === totalPages" @click="currentPage++">
          <span>Sau</span><i class="bi bi-arrow-right"></i>
        </button>
      </nav>
    </div>

    <Transition name="z-compare-dock">
      <div v-if="compareCount" class="z-compare-dock">
        <div>
          <i class="bi bi-columns-gap"></i>
          <span data-no-i18n>{{ compareStatusLabel }}</span>
        </div>
        <button type="button" class="z-compare-clear" data-no-i18n @click="clearComparison">
          {{ isEn ? 'Clear' : 'Xóa hết' }}
        </button>
        <RouterLink to="/compare" class="lm-btn-primary">
          <span data-no-i18n>{{ isEn ? 'View comparison' : 'Xem so sánh' }}</span>
          <i class="bi bi-arrow-right"></i>
        </RouterLink>
      </div>
    </Transition>

    <!-- Image color search modal -->
    <div v-if="showVisualModal" class="z-modal-overlay" @click.self="showVisualModal = false" style="z-index:2000; backdrop-filter:blur(2px);">
      <div class="z-modal" style="max-width:550px">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3 style="font-size:18px;font-weight:600;margin:0" class="text-danger">
            <i class="bi bi-camera-fill me-2"></i>Tìm sản phẩm theo màu từ ảnh
          </h3>
          <button type="button" class="z-icon-btn" @click="showVisualModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <p style="font-size:13px; color:var(--z-gray);" class="mb-3">
          Tải lên ảnh trang phục bạn yêu thích. Hệ thống sẽ đọc màu nổi bật và tìm biến thể còn hàng có màu gần nhất.
        </p>

        <div class="p-4 border-2 rounded-3 text-center mb-3" style="border: 2px dashed #fcc2d7; background: #fff5f5; cursor: pointer;" @click="$refs.visualFileInput.click()">
          <i class="bi bi-cloud-arrow-up text-danger" style="font-size: 36px;"></i>
          <div style="font-size: 14px; font-weight: 600;" class="mt-2 text-danger">Tải ảnh mẫu lên hoặc Kéo thả vào đây</div>
          <div style="font-size: 11px; color: var(--z-gray);">Hỗ trợ file ảnh JPG, PNG, WEBP (Tối đa 10MB)</div>
          <input ref="visualFileInput" type="file" accept="image/*" class="d-none" @change="handleVisualFileSelected" />
        </div>

        <div v-if="analyzingVisual" class="text-center py-3">
          <div class="spinner-border text-danger mb-2"></div>
          <div style="font-size: 13px; font-weight: 500;" class="text-danger">Đang phân tích màu sắc trong ảnh...</div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductCard from '@/components/ui/ProductCard.vue'
import AppFooter   from '@/components/layout/AppFooter.vue'
import { products, loadProducts } from '@/composables/useProducts'
import { api } from '@/composables/useApi'
import { useCompare } from '@/composables/useCompare'
import { useI18n } from '@/composables/useI18n'
import { useToast } from '@/composables/useToast'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'
import {
  categoryNamesEqual,
  findCategoryImage,
  getCategoryDefinition,
  normalizeCategoryName,
  productBelongsToCategory,
  productCategoryDefinitions,
} from '@/config/productCategories'

const route = useRoute()
const router = useRouter()
const bestsellerRank = ref(new Map())
const { count: compareCount, clear: clearComparison } = useCompare()
const { isEn } = useI18n()
const { showToast } = useToast()
const compareStatusLabel = computed(() => isEn.value
  ? `Selected ${compareCount.value}/4 products`
  : `Đã chọn ${compareCount.value}/4 sản phẩm`)

onMounted(async () => {
  try {
    await loadProducts()
  } catch (error) {
    console.warn('Không tải được danh sách sản phẩm', error)
  }
  try {
    const summary = await api().getStorefrontSummary()
    bestsellerRank.value = new Map((summary?.bestsellers || []).map((item, index) => [Number(item.productId), index]))
  } catch (error) {
    console.warn('Không tải được thứ tự bán chạy', error)
  }
  applyRouteQuery()
})
watch(() => route.query, applyRouteQuery, { deep: true })

const filters = computed(() => {
  const categories = [
    ...productCategoryDefinitions.map(definition => definition.category),
    ...products.value
      .filter(product => !productCategoryDefinitions.some(definition =>
        productBelongsToCategory(product, definition.category)
      ))
      .map(product => product.category)
      .filter(Boolean),
  ]
  const seen = new Set()
  const uniqueCategories = categories.filter(category => {
    const normalized = normalizeCategoryName(category)
    if (!normalized || seen.has(normalized)) return false
    seen.add(normalized)
    return true
  })

  return ['Tất cả', ...uniqueCategories, 'Ưu đãi']
})

function getFilterImage(f) {
  if (f === 'Tất cả') return null
  if (f === 'Ưu đãi') {
    return products.value.find(product => product.promotionActive && product.image)?.image
      || '/images/products/catalog-v2/sp003_main.webp'
  }
  return findCategoryImage(products.value, f)
}
const activeFilter = ref('Tất cả')
const priceRange = ref('all')
const sortBy = ref('newest')
const pageSize = ref(12)
const currentPage = ref(1)

function applyRouteQuery() {
  const requestedSort = String(route.query.sort || '')
  if (['newest', 'bestseller', 'price-asc', 'price-desc', 'name'].includes(requestedSort)) {
    sortBy.value = requestedSort
  }
  const requestedCategory = String(route.query.category || '').trim()
  const occasion = String(route.query.occasion || '').trim().toLowerCase()
  const promotionOnly = route.query.promotion === 'true'
  const occasionCategory = {
    work: 'Trang phục công sở',
    party: 'Trang phục dự tiệc',
    wedding: 'Váy & Đầm',
    date: 'Áo thời trang',
  }[occasion]
  const categoryToSelect = promotionOnly ? 'Ưu đãi' : requestedCategory || occasionCategory || ''
  const normalizedCategory = getCategoryDefinition(categoryToSelect)?.category || categoryToSelect
  activeFilter.value = normalizedCategory
    ? filters.value.find(filter => categoryNamesEqual(filter, normalizedCategory)) || 'Tất cả'
    : 'Tất cả'
  if (route.query.aiSearch === 'true') {
    showVisualModal.value = true
  }
}

function selectFilter(filter) {
  activeFilter.value = filter

  const query = { ...route.query }
  delete query.category
  delete query.occasion
  delete query.promotion

  if (filter === 'Ưu đãi') query.promotion = 'true'
  else if (filter !== 'Tất cả') query.category = filter

  void router.replace({ query })
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
      result = result.filter(product => productBelongsToCategory(product, activeFilter.value))
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

const showVisualModal = ref(false)
const analyzingVisual = ref(false)
const aiVisualResults = ref(null)

async function handleVisualFileSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return
  event.target.value = ''
  if (!file.type.startsWith('image/')) {
    showToast('Vui lòng chọn một tệp hình ảnh hợp lệ.')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showToast('Ảnh không được vượt quá 5 MB.')
    return
  }
  analyzingVisual.value = true
  try {
    const res = await api().visualSearch(file)
    aiVisualResults.value = res
    showVisualModal.value = false
  } catch (error) {
    showToast(error.error || 'Không thể phân tích màu từ hình ảnh')
  } finally {
    analyzingVisual.value = false
  }
}

function clearAiVisualSearch() {
  aiVisualResults.value = null
}

const sortedProducts = computed(() => {
  if (aiVisualResults.value && Array.isArray(aiVisualResults.value.results)) {
    return aiVisualResults.value.results.map(item => {
      const original = products.value.find(p => p.id === item.id)
      return {
        id: item.id,
        name: item.tenSanPham || item.name,
        code: item.maSanPham || item.code,
        price: Number(item.giaCuoi || item.price || item.giaGoc || 0),
        basePrice: Number(item.giaGoc || item.giaCuoi || item.price || 0),
        promotionActive: Number(item.giaGoc || 0) > Number(item.giaCuoi || item.price || 0),
        category: item.danhMuc || original?.category || 'Thời trang',
        image: item.anhChinh || original?.image || '/images/products/catalog-v2/sp003_main.webp',
        badge: `${item.matchScore}% gần màu`
      }
    })
  }

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

const totalPages = computed(() => Math.max(1, Math.ceil(sortedProducts.value.length / pageSize.value)))
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedProducts.value.slice(start, start + pageSize.value)
})
const resultStart = computed(() => sortedProducts.value.length
  ? (currentPage.value - 1) * pageSize.value + 1
  : 0)
const resultEnd = computed(() => Math.min(currentPage.value * pageSize.value, sortedProducts.value.length))

watch([activeFilter, priceRange, sortBy, pageSize], () => {
  currentPage.value = 1
})
watch(totalPages, pages => {
  if (currentPage.value > pages) currentPage.value = pages
})
</script>

<style scoped>
.z-fade-item {
  animation: z-item-in 0.5s ease both;
}
.z-visual-search-btn {
  min-height: 38px;
  border-color: var(--z-accent-light);
  background: var(--z-white);
  color: var(--z-accent-dark);
}
.z-visual-result {
  border: 1px solid var(--z-accent-light);
  border-radius: var(--z-radius);
  background: var(--z-white);
}
@keyframes z-item-in {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.z-compare-dock {
  position: fixed; z-index: 1100; left: 50%; bottom: 20px; transform: translateX(-50%);
  min-width: min(520px, calc(100vw - 24px)); padding: 10px 12px 10px 16px;
  display: flex; align-items: center; gap: 14px;
  background: var(--z-white); border: 1px solid var(--z-gray-border);
  box-shadow: 0 12px 36px rgba(0,0,0,.16);
}
.z-compare-dock > div { display: flex; align-items: center; gap: 8px; flex: 1; font-size: 13px; font-weight: 600; }
.z-compare-dock > div i { color: var(--z-accent); }
.z-compare-clear {
  min-height: 32px;
  padding: 5px 8px;
  border: 1px solid transparent;
  border-radius: 5px;
  background: transparent;
  color: var(--z-gray);
  font-size: 11px;
  font-weight: 600;
}
.z-compare-clear:hover { border-color: var(--z-accent-light); background: var(--z-accent-soft); color: var(--z-accent-dark); }
.z-compare-dock .lm-btn-primary { min-height: 38px; padding: 8px 14px; }
.z-compare-dock-enter-active,
.z-compare-dock-leave-active { transition: opacity .2s ease, transform .2s ease; }
.z-compare-dock-enter-from,
.z-compare-dock-leave-to { opacity: 0; transform: translate(-50%, 12px); }
.z-public-pagination {
  margin-top: 36px; display: flex; align-items: center; justify-content: center; gap: 18px;
}
.z-public-pagination button {
  min-width: 88px; height: 38px; padding: 0 12px; display: inline-flex;
  align-items: center; justify-content: center; gap: 7px;
  border: 1px solid var(--z-gray-border); border-radius: 4px;
  background: var(--z-white); color: var(--z-dark); font-size: 12px;
}
.z-public-pagination button:hover:not(:disabled) { border-color: var(--z-accent); color: var(--z-accent); }
.z-public-pagination button:disabled { opacity: .45; cursor: not-allowed; }
.z-public-pagination > span { min-width: 90px; text-align: center; color: var(--z-gray); font-size: 12px; }
@media (max-width: 575px) {
  .z-compare-dock { gap: 8px; bottom: 10px; }
  .z-compare-dock > div span { font-size: 11px; }
  .z-compare-dock .lm-btn-primary { font-size: 11px; }
}
</style>
