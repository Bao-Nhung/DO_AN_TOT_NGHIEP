<template>
  <div>
    <div class="lm-page-hero" data-title="WISHLIST">
      <div class="container">
        <p class="lm-eyebrow mb-3">Danh sách yêu thích</p>
        <h1 class="mb-3">Bộ sưu tập <em>của bạn</em></h1>
        <p>{{ items.length }} sản phẩm bạn đã lưu</p>
      </div>
    </div>

    <div class="container py-5">
      <div class="row g-4">
        <div v-for="(item, i) in items" :key="item.id"
             class="col-6 col-lg-4 lm-reveal" :style="{ transitionDelay: i * 0.1 + 's' }">
          <div class="lm-product-card position-relative" @click="$router.push('/product/' + item.id)">
            <div class="lm-product-image">
              <div class="lm-product-img-inner" :style="{ background: item.bg }">{{ item.letter }}</div>
              <!-- Remove button -->
              <button class="lm-wishlist-remove" @click.stop="removeItem(item.id)"
                      style="position:absolute;top:14px;right:14px;width:32px;height:32px;background:var(--lm-white);border-radius:50%;display:flex;align-items:center;justify-content:center;cursor:pointer;z-index:2;box-shadow:0 2px 8px rgba(0,0,0,0.1);border:none;transition:all 0.3s">
                <i class="bi bi-x" style="font-size:16px;color:var(--lm-gray)"></i>
              </button>
            </div>
            <div class="lm-product-cat">{{ item.category }}</div>
            <div class="lm-product-name">{{ item.name }}</div>
            <div class="lm-product-price">
              <span :class="item.salePrice ? 'lm-price-sale' : ''">
                {{ formatPrice(item.salePrice || item.price) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="items.length === 0" class="col-12 text-center py-5">
          <i class="bi bi-heart mb-3" style="font-size:48px;color:var(--lm-beige-dark)"></i>
          <h3 class="lm-display" style="font-weight:300;color:var(--lm-gray)">Danh sách trống</h3>
          <p style="color:var(--lm-gray);font-size:13px">Hãy khám phá bộ sưu tập và lưu những sản phẩm yêu thích.</p>
          <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Khám phá ngay</span></RouterLink>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useToast } from '@/composables/useToast'
import { useCart }  from '@/composables/useCart'
import { useReveal } from '@/composables/useReveal'

useReveal()
const { showToast } = useToast()
const { formatPrice } = useCart()

const items = ref([
  { id: 1, name: 'Silk Wrap Dress',   category: 'Đầm',      price: 4290000, salePrice: null, letter: 'A', bg: 'linear-gradient(160deg,#EDE6D8,#C5B89A)' },
  { id: 2, name: 'Cashmere Blazer',   category: 'Áo khoác', price: 9890000, salePrice: 7490000, letter: 'B', bg: 'linear-gradient(160deg,#E0D4C4,#B8A88A)' },
  { id: 4, name: 'Structured Tote Bag', category: 'Phụ kiện', price: 5890000, salePrice: null, letter: 'D', bg: 'linear-gradient(160deg,#E8E2D8,#CBBEA8)' },
  { id: 5, name: 'A-Line Midi Skirt', category: 'Váy',      price: 1890000, salePrice: null, letter: 'E', bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)' },
])

function removeItem(id) {
  items.value = items.value.filter(i => i.id !== id)
  showToast('Đã xóa khỏi yêu thích')
}
</script>
