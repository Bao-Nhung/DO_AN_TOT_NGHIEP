<template>
  <div>
    <div class="lm-page-hero" data-title="WISHLIST">
      <div class="container">
        <p class="lm-eyebrow mb-3">Yeu thich</p>
        <h1 class="mb-3">Bo suu tap <em>cua ban</em></h1>
        <p>{{ items.length }} san pham ban da luu</p>
      </div>
    </div>

    <div class="container py-5">
      <div class="row g-4">
        <div v-for="(item, i) in items" :key="item.id"
             class="col-6 col-lg-4 lm-reveal" :style="{ transitionDelay: i * 0.1 + 's' }">
          <div class="lm-product-card position-relative" @click="$router.push('/product/' + item.id)">
            <div class="lm-product-image">
              <div class="lm-product-img-inner" :style="{ background: item.bg }">{{ item.letter }}</div>
              <button class="lm-wishlist-remove" @click.stop="removeItem(item.id)"
                      style="position:absolute;top:12px;right:12px;width:36px;height:36px;background:var(--z-white);border-radius:50%;display:flex;align-items:center;justify-content:center;cursor:pointer;z-index:2;box-shadow:0 2px 8px rgba(0,0,0,0.08);border:none;transition:all 0.3s">
                <i class="bi bi-x" style="font-size:16px;color:var(--z-gray)"></i>
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

        <div v-if="items.length === 0" class="col-12 text-center py-5">
          <i class="bi bi-heart mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
          <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Danh sach trong</h3>
          <p style="color:var(--z-gray);font-size:14px">Hay kham pha bo suu tap va luu nhung san pham yeu thich.</p>
          <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Kham pha ngay</span></RouterLink>
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
  { id: 1, name: 'Vay Lua To Tam Co Dien',     category: 'Vay truyen thong', price: 2890000, salePrice: null, letter: 'Z', bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)' },
  { id: 3, name: 'Vay Da Hoi Gam Hoang Gia',   category: 'Vay da hoi',       price: 5490000, salePrice: 4290000, letter: 's', bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)' },
  { id: 6, name: 'Vay Cuoi Lua Trang Tinh Khoi', category: 'Vay cuoi',        price: 8990000, salePrice: null, letter: 'a', bg: 'linear-gradient(160deg,#F5EDE3,#E8CFC9)' },
])

function removeItem(id) {
  items.value = items.value.filter(i => i.id !== id)
  showToast('Da xoa khoi yeu thich')
}
</script>
