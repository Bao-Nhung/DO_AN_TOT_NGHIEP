<template>
  <div>
    <div class="lm-page-hero" data-title="WISHLIST">
      <div class="container">
        <p class="lm-eyebrow mb-3">{{ isEn ? 'Wishlist' : 'Yêu Thích' }}</p>
        <h1 class="mb-3">Bộ sưu tập <em>của bạn</em></h1>
        <p data-no-i18n>{{ wishlistSummaryLabel }}</p>
      </div>
    </div>

    <div class="container py-5">
      <div class="row g-4">
        <div v-for="(item, i) in wishlistProducts" :key="item.id"
             class="col-6 col-lg-4 z-fade-item" :style="{ animationDelay: i * 0.04 + 's' }">
          <div class="lm-product-card position-relative" role="link" tabindex="0" :aria-label="`Xem ${item.name}`"
               @click="$router.push('/product/' + item.id)"
               @keydown.enter="$router.push('/product/' + item.id)"
               @keydown.space.prevent="$router.push('/product/' + item.id)">
            <div class="lm-product-image">
              <div v-if="item.image" class="lm-product-img-inner"><img :src="item.image" :alt="item.name" loading="lazy" /></div>
              <div v-else class="lm-product-img-inner" :style="{ background: item.bg }">{{ item.letter }}</div>
              <button type="button" class="position-absolute d-flex align-items-center justify-content-center" :aria-label="`Xóa ${item.name} khỏi yêu thích`"
                      @click.stop="removeFromWishlist(item.id)"
                      style="top:12px;right:12px;width:36px;height:36px;background:var(--z-white);border-radius:50%;cursor:pointer;z-index:2;box-shadow:0 2px 8px rgba(0,0,0,0.08);border:none;transition:all 0.3s">
                <i class="bi bi-x" style="font-size:16px;color:var(--z-gray)"></i>
              </button>
            </div>
            <div class="lm-product-cat">{{ item.category }}</div>
            <div class="lm-product-name">{{ item.name }}</div>
            <div class="lm-product-price">{{ formatPrice(item.price) }}</div>
          </div>
        </div>

        <div v-if="wishlistProducts.length === 0" class="col-12 text-center py-5">
          <i class="bi bi-heart mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
          <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Danh sách trống</h3>
          <p style="color:var(--z-gray);font-size:14px;max-width:400px;margin:0 auto">
            Hãy thêm cho bạn những sản phẩm yêu thích của chính mình! Khám phá bộ sưu tập và bấm vào biểu tượng trái tim để lưu lại.
          </p>
          <RouterLink to="/collections" class="lm-btn-primary mt-4 d-inline-flex"><span>Khám phá ngay</span></RouterLink>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useToast } from '@/composables/useToast'
import { useCart }  from '@/composables/useCart'
import { useWishlist } from '@/composables/useWishlist'
import { products, loadProducts } from '@/composables/useProducts'
import { useI18n } from '@/composables/useI18n'

onMounted(() => loadProducts())

const { showToast } = useToast()
const { formatPrice } = useCart()
const { wishlistIds, removeFromWishlist: removeWl } = useWishlist()
const { isEn } = useI18n()

const wishlistProducts = computed(() => {
  return products.value.filter(p => wishlistIds.value.includes(p.id))
})
const wishlistSummaryLabel = computed(() => isEn.value
  ? `${wishlistProducts.value.length} saved products`
  : `${wishlistProducts.value.length} sản phẩm bạn đã lưu`)

function removeFromWishlist(id) {
  removeWl(id)
  showToast('Đã xoá khỏi yêu thích')
}
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
