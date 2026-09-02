<template>
  <!-- Overlay -->
  <div class="lm-cart-overlay" :class="{ open: cart.state.isOpen }" :aria-hidden="!cart.state.isOpen" :inert="!cart.state.isOpen" @click="cart.closeCart()">
    <div class="lm-cart-drawer" role="dialog" aria-modal="true" aria-labelledby="cart-drawer-title" @click.stop>

      <!-- Header -->
      <div class="d-flex justify-content-between align-items-center px-4 py-3"
           style="border-bottom:1px solid var(--z-gray-border)">
        <h2 id="cart-drawer-title" class="lm-cart-title mb-0">
          Giỏ hàng
          <em style="font-style:italic;color:var(--z-gray);font-size:18px">({{ cart.totalCount.value }})</em>
        </h2>
        <button type="button" class="lm-cart-close" aria-label="Đóng giỏ hàng" @click="cart.closeCart()">
          <i class="bi bi-x" style="font-size:18px"></i>
        </button>
      </div>

      <!-- Items -->
      <div class="flex-grow-1 overflow-auto px-4 py-3">
        <div v-if="!cart.state.items.length" class="d-flex flex-column align-items-center justify-content-center h-100">
          <i class="bi bi-bag mb-3" style="font-size:40px;color:var(--z-gray-border)"></i>
          <p style="color:var(--z-gray);font-size:14px">Giỏ hàng trống</p>
        </div>
        <div v-for="item in cart.state.items" :key="item.id"
             class="d-grid py-3" :class="{ 'z-cart-item-unavailable': item.unavailable }"
             style="grid-template-columns:80px 1fr;gap:16px;border-bottom:1px solid var(--z-gray-border)">
          <!-- Thumb -->
          <div style="aspect-ratio:3/4;overflow:hidden;border-radius:8px">
            <img v-if="item.image" :src="item.image" :alt="item.name" style="width:100%;height:100%;object-fit:cover" />
            <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                 :style="{ background: item.bg, fontFamily: 'var(--z-font-display)', fontSize: '20px', color: 'rgba(255,255,255,0.3)', fontStyle: 'italic' }">
              {{ item.letter }}
            </div>
          </div>
          <!-- Info -->
          <div class="d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start">
              <div style="font-family:var(--z-font-display);font-size:16px;font-weight:400;color:var(--z-dark)">{{ item.name }}</div>
              <button type="button" class="z-line-icon-btn z-line-icon-btn--danger" :aria-label="`Xóa ${item.name} khỏi giỏ hàng`" @click="cart.removeItem(item.id)"
                      title="Xoá">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div style="font-size:10px;color:var(--z-gray);letter-spacing:0;text-transform:uppercase;margin-bottom:12px;flex:1">{{ item.variant }}</div>
            <div v-if="item.unavailable" class="z-cart-item-warning">{{ item.unavailableReason }}</div>
            <div class="d-flex justify-content-between align-items-center">
              <div class="lm-qty-control">
                <button type="button" class="lm-qty-btn" :aria-label="`Giảm số lượng ${item.name}`" :disabled="item.unavailable" @click="cart.changeQty(item.id, -1)">−</button>
                <span class="lm-qty-num">{{ item.qty }}</span>
                <button type="button" class="lm-qty-btn" :aria-label="`Tăng số lượng ${item.name}`" :disabled="item.unavailable || item.qty >= item.maxQty" @click="cart.changeQty(item.id, 1)">+</button>
              </div>
              <ProductPrice
                v-if="!item.unavailable"
                class="lm-cart-item-price"
                :price="item.price"
                :original-price="item.originalPrice"
                size="compact"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-4 py-3" style="border-top:1px solid var(--z-gray-border)">
        <div class="d-flex justify-content-between align-items-center mb-1">
          <span style="font-size:10px;font-weight:500;letter-spacing:0;text-transform:uppercase;color:var(--z-gray)">Tạm tính</span>
          <span style="font-family:var(--z-font-body);font-size:22px;font-weight:600;color:var(--z-dark);font-variant-numeric:tabular-nums">{{ cart.formatPrice(cart.subtotal.value) }}</span>
        </div>
        <p style="font-size:10px;color:var(--z-gray-light);text-align:center;margin-bottom:20px">Phí vận chuyển & thuế tính khi thanh toán</p>
        <RouterLink v-if="cart.state.items.length && !hasUnavailableItems" to="/checkout" class="lm-btn-primary w-100 justify-content-center mb-2 text-decoration-none"
                    @click="cart.closeCart()">
          <span>Thanh toán ngay</span>
        </RouterLink>
        <button v-else-if="hasUnavailableItems" class="lm-btn-primary w-100 justify-content-center mb-2" disabled>
          <span>Xóa sản phẩm không khả dụng để tiếp tục</span>
        </button>
        <button class="lm-btn-outline-light w-100 justify-content-center"
                style="border-color:rgba(0,0,0,0.15);color:var(--z-dark);padding:14px"
                @click="cart.closeCart()">
          Tiếp tục mua sắm
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useCart } from '@/composables/useCart'
import { api } from '@/composables/useApi'
import ProductPrice from '@/components/ui/ProductPrice.vue'

const cart = useCart()
const hasUnavailableItems = computed(() => cart.state.items.some(item => item.unavailable))

onMounted(() => {
  cart.refreshItems(productId => api().getSanPhamById(productId))
})
</script>

<style scoped>
.lm-cart-item-price {
  min-width: 100px;
  max-width: 190px;
}
.z-cart-item-unavailable { opacity: .72; }
.z-cart-item-warning { margin: -7px 0 9px; color: var(--z-danger); font-size: 11px; font-weight: 600; }
.lm-qty-btn:disabled { cursor: not-allowed; opacity: .4; }
</style>
