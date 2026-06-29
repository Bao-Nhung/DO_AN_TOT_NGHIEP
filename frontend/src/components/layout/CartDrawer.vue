<template>
  <!-- Overlay -->
  <div class="lm-cart-overlay" :class="{ open: cart.state.isOpen }" @click="cart.closeCart()">
    <div class="lm-cart-drawer" @click.stop>

      <!-- Header -->
      <div class="d-flex justify-content-between align-items-center px-4 py-3"
           style="border-bottom:1px solid var(--lm-beige)">
        <h2 class="lm-cart-title mb-0">
          Giỏ hàng
          <em style="font-style:italic;color:var(--lm-gray);font-size:18px">({{ cart.totalCount.value }})</em>
        </h2>
        <button class="lm-cart-close" @click="cart.closeCart()">
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
             class="d-grid py-3" style="grid-template-columns:80px 1fr;gap:16px;border-bottom:1px solid var(--lm-beige)">
          <!-- Thumb -->
          <div style="aspect-ratio:3/4;overflow:hidden;border-radius:8px">
            <img v-if="item.image" :src="item.image" :alt="item.name" style="width:100%;height:100%;object-fit:cover" />
            <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                 :style="{ background: item.bg, fontFamily: 'var(--lm-font-display)', fontSize: '20px', color: 'rgba(255,255,255,0.3)', fontStyle: 'italic' }">
              {{ item.letter }}
            </div>
          </div>
          <!-- Info -->
          <div class="d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start">
              <div style="font-family:var(--lm-font-display);font-size:16px;font-weight:400;color:var(--lm-black)">{{ item.name }}</div>
              <button @click="cart.removeItem(item.id)"
                      style="border:none;background:none;cursor:pointer;color:var(--lm-gray);font-size:14px;padding:0;line-height:1"
                      title="Xoá">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div style="font-size:11px;color:var(--lm-gray);letter-spacing:0.05em;margin-bottom:12px;flex:1">
              <div v-if="item.maVayChiTiet">Mã SP: {{ item.maVayChiTiet }}</div>
              <div>Màu: {{ item.color || 'N/A' }} &nbsp;|&nbsp; Size: {{ item.size || 'N/A' }}</div>
            </div>
            <div class="d-flex justify-content-between align-items-center">
              <div class="lm-qty-control">
                <button class="lm-qty-btn" @click="cart.changeQty(item.id, -1)">−</button>
                <span class="lm-qty-num">{{ item.qty }}</span>
                <button class="lm-qty-btn" @click="cart.changeQty(item.id, 1)">+</button>
              </div>
              <div style="font-family:var(--lm-font-display);font-size:18px;font-weight:400">
                {{ cart.formatPrice(item.price) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-4 py-3" style="border-top:1px solid var(--lm-beige)">
        <div class="d-flex justify-content-between align-items-center mb-1">
          <span style="font-size:10px;font-weight:500;letter-spacing:0.2em;text-transform:uppercase;color:var(--lm-gray)">Tạm tính</span>
          <span style="font-family:var(--lm-font-display);font-size:24px;font-weight:300;color:var(--lm-black)">{{ cart.formatPrice(cart.subtotal.value) }}</span>
        </div>
        <p style="font-size:10px;color:var(--lm-gray-light);text-align:center;margin-bottom:20px">Phí vận chuyển & thuế tính khi thanh toán</p>
        <RouterLink to="/checkout" class="lm-btn-primary w-100 justify-content-center mb-2 text-decoration-none"
                    @click="cart.closeCart()">
          <span>Thanh toán ngay</span>
        </RouterLink>
        <button class="lm-btn-outline-light w-100 justify-content-center"
                style="border-color:rgba(0,0,0,0.15);color:var(--lm-black);padding:14px"
                @click="cart.closeCart()">
          Tiếp tục mua sắm
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCart } from '@/composables/useCart'
const cart = useCart()
</script>
