<template>
  <main>
    <section class="lm-page-hero" data-title="COMPARE">
      <div class="container">
        <p class="lm-eyebrow mb-3">Zestia Selection</p>
        <h1 class="mb-3">{{ t('compare.title') }}</h1>
        <p>Đặt các thiết kế cạnh nhau để chọn đúng mức giá, chất liệu, phom và kích thước phù hợp.</p>
      </div>
    </section>

    <section class="container py-5">
      <div v-if="loading" class="z-compare-loading">
        <div class="spinner-border spinner-border-sm" role="status"></div>
        <span>{{ t('common.loading') }}</span>
      </div>

      <div v-else-if="!comparedProducts.length" class="z-compare-empty">
        <i class="bi bi-columns-gap"></i>
        <h2>{{ t('compare.empty') }}</h2>
        <p>Chọn biểu tượng so sánh trên tối đa 4 sản phẩm để xem khác biệt rõ ràng.</p>
        <RouterLink to="/collections" class="lm-btn-primary">
          <span>{{ t('nav.products') }}</span>
        </RouterLink>
      </div>

      <template v-else>
        <div class="z-compare-toolbar">
          <p>{{ comparedProducts.length }}/{{ maxProducts }} sản phẩm</p>
          <button type="button" class="lm-btn-secondary" @click="clearComparison">
            <i class="bi bi-trash3"></i>
            Xóa danh sách
          </button>
        </div>

        <div class="z-compare-scroll">
          <table class="z-compare-table">
            <thead>
              <tr>
                <th scope="col">Tiêu chí</th>
                <th v-for="product in comparedProducts" :key="product.id" scope="col">
                  <div class="z-compare-product">
                    <button
                      type="button"
                      class="z-compare-remove"
                      :aria-label="`${t('compare.remove')} ${product.tenSanPham}`"
                      @click="removeProduct(product.id)"
                    >
                      <i class="bi bi-x-lg"></i>
                    </button>
                    <RouterLink :to="`/product/${product.id}`">
                      <img v-if="product.anhUrl" :src="product.anhUrl" :alt="product.tenSanPham">
                      <div v-else class="z-compare-placeholder">Z</div>
                      <strong>{{ product.tenSanPham }}</strong>
                      <span>{{ product.maSanPham }}</span>
                    </RouterLink>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th scope="row">{{ t('compare.price') }}</th>
                <td v-for="product in comparedProducts" :key="`price-${product.id}`" class="z-compare-price">
                  {{ fmtPrice(product.giaBan) }}
                </td>
              </tr>
              <tr>
                <th scope="row">Loại sản phẩm</th>
                <td v-for="product in comparedProducts" :key="`category-${product.id}`">
                  {{ product.loaiSanPham || 'N/A' }}
                </td>
              </tr>
              <tr>
                <th scope="row">{{ t('compare.material') }}</th>
                <td v-for="product in comparedProducts" :key="`material-${product.id}`">
                  {{ product.chatLieu || 'N/A' }}
                </td>
              </tr>
              <tr>
                <th scope="row">{{ t('compare.fit') }}</th>
                <td v-for="product in comparedProducts" :key="`fit-${product.id}`">
                  {{ product.moTaPhom || 'Chưa cập nhật' }}
                </td>
              </tr>
              <tr>
                <th scope="row">{{ t('compare.sizes') }}</th>
                <td v-for="product in comparedProducts" :key="`sizes-${product.id}`">
                  <div class="z-compare-sizes">
                    <span v-for="size in availableSizes(product)" :key="size">{{ size }}</span>
                    <em v-if="!availableSizes(product).length">Hết hàng</em>
                  </div>
                </td>
              </tr>
              <tr>
                <th scope="row">{{ t('compare.rating') }}</th>
                <td v-for="product in comparedProducts" :key="`rating-${product.id}`">
                  <div class="z-compare-rating">
                    <i class="bi bi-star-fill"></i>
                    <strong>{{ Number(product.diemDanhGia || 0).toFixed(1) }}</strong>
                    <span>({{ product.soDanhGia || 0 }} đánh giá)</span>
                  </div>
                </td>
              </tr>
              <tr>
                <th scope="row">{{ t('compare.stock') }}</th>
                <td v-for="product in comparedProducts" :key="`stock-${product.id}`">
                  {{ product.tonKho || 0 }} sản phẩm
                </td>
              </tr>
              <tr>
                <th scope="row"></th>
                <td v-for="product in comparedProducts" :key="`action-${product.id}`">
                  <RouterLink :to="`/product/${product.id}`" class="lm-btn-primary z-compare-view">
                    <span>Xem sản phẩm</span>
                    <i class="bi bi-arrow-right"></i>
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </section>

    <AppFooter />
  </main>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api } from '@/composables/useApi'
import { useCompare } from '@/composables/useCompare'
import { fmtPrice } from '@/composables/useProducts'
import { useI18n } from '@/composables/useI18n'

const { ids, removeProduct, clear, maxProducts } = useCompare()
const { t } = useI18n()
const comparedProducts = ref([])
const loading = ref(true)

async function loadComparison() {
  loading.value = true
  try {
    const results = await Promise.allSettled(ids.value.map(id => api().getSanPhamById(id)))
    comparedProducts.value = results
      .filter(result => result.status === 'fulfilled')
      .map(result => result.value)
    const availableIds = new Set(comparedProducts.value.map(product => Number(product.id)))
    ids.value
      .filter(id => !availableIds.has(Number(id)))
      .forEach(removeProduct)
  } finally {
    loading.value = false
  }
}

function availableSizes(product) {
  return [...new Set(
    (product.bienThe || [])
      .filter(variant => Number(variant.soLuong || 0) > 0 && Number(variant.trangThai) === 1)
      .map(variant => variant.kichThuoc)
      .filter(Boolean)
  )]
}

function clearComparison() {
  clear()
  comparedProducts.value = []
}

watch(ids, loadComparison, { deep: true })
onMounted(loadComparison)
</script>

<style scoped>
.z-compare-loading,
.z-compare-empty {
  min-height: 340px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 14px; text-align: center;
}
.z-compare-empty > i { font-size: 44px; color: var(--z-gray-light); }
.z-compare-empty h2 { margin: 0; font-family: var(--z-font-display); font-size: 28px; }
.z-compare-empty p { color: var(--z-gray); font-size: 14px; }
.z-compare-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  gap: 16px; margin-bottom: 18px;
}
.z-compare-toolbar p { margin: 0; color: var(--z-gray); font-size: 13px; }
.z-compare-scroll { overflow-x: auto; border: 1px solid var(--z-gray-border); }
.z-compare-table { width: 100%; min-width: 760px; border-collapse: collapse; table-layout: fixed; }
.z-compare-table th,
.z-compare-table td {
  border-right: 1px solid var(--z-gray-border);
  border-bottom: 1px solid var(--z-gray-border);
  padding: 16px; vertical-align: top; font-size: 13px; line-height: 1.55;
}
.z-compare-table tr:last-child th,
.z-compare-table tr:last-child td { border-bottom: 0; }
.z-compare-table th:last-child,
.z-compare-table td:last-child { border-right: 0; }
.z-compare-table thead th { background: var(--z-white); }
.z-compare-table thead th:first-child,
.z-compare-table tbody th {
  width: 150px; background: var(--z-bg-alt); color: var(--z-gray);
  font-weight: 600; position: sticky; left: 0; z-index: 2;
}
.z-compare-product { position: relative; }
.z-compare-product a { display: flex; flex-direction: column; gap: 8px; color: var(--z-dark); text-decoration: none; }
.z-compare-product img,
.z-compare-placeholder { width: 100%; aspect-ratio: 3 / 4; object-fit: cover; background: var(--z-bg-alt); }
.z-compare-placeholder {
  display: grid; place-items: center; font-family: var(--z-font-display);
  font-size: 40px; color: var(--z-gray-light);
}
.z-compare-product strong { font-size: 14px; min-height: 42px; }
.z-compare-product span { color: var(--z-gray); font-size: 11px; }
.z-compare-remove {
  position: absolute; z-index: 2; top: 8px; right: 8px;
  width: 32px; height: 32px; display: grid; place-items: center;
  border: 0; border-radius: 50%; background: var(--z-white); color: var(--z-dark);
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
}
.z-compare-remove:hover { background: var(--z-accent); color: var(--z-white); }
.z-compare-price { color: var(--z-accent); font-size: 16px !important; font-weight: 700; }
.z-compare-sizes { display: flex; flex-wrap: wrap; gap: 6px; }
.z-compare-sizes span { min-width: 30px; padding: 4px 8px; border: 1px solid var(--z-gray-border); text-align: center; }
.z-compare-sizes em { color: var(--z-gray); font-style: normal; }
.z-compare-rating { display: flex; align-items: center; gap: 5px; flex-wrap: wrap; }
.z-compare-rating i { color: #e3a008; }
.z-compare-rating span { color: var(--z-gray); font-size: 11px; }
.z-compare-view { width: 100%; justify-content: center; }
@media (max-width: 767px) {
  .z-compare-table thead th:first-child,
  .z-compare-table tbody th { width: 110px; }
  .z-compare-table th,
  .z-compare-table td { padding: 12px; }
}
</style>
