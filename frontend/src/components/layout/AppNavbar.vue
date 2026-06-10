<template>
  <nav class="lm-navbar" :class="{ scrolled: isScrolled }">
    <div class="container-fluid px-4 h-100 d-flex align-items-center justify-content-between">

      <!-- Logo -->
      <div class="lm-nav-logo" @click="$router.push('/')">
        LUM<span class="lm-gold-text">I</span>ÈRE
      </div>

      <!-- Links -->
      <ul class="d-none d-lg-flex list-unstyled mb-0 gap-4 align-items-center">
        <li><RouterLink class="lm-nav-link" to="/">Trang Chủ</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/collections">Bộ Sưu Tập</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/product/1">Chi Tiết SP</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/wishlist">Yêu Thích</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/profile">Tài Khoản</RouterLink></li>
      </ul>

      <!-- Actions -->
      <div class="d-flex align-items-center gap-2">
        <button class="lm-nav-icon-btn" @click="$router.push('/collections')" title="Tìm kiếm">
          <i class="bi bi-search"></i>
        </button>
        <button class="lm-nav-icon-btn" @click="$router.push('/wishlist')" title="Yêu thích">
          <i class="bi bi-heart"></i>
        </button>
        <button class="lm-nav-icon-btn" @click="openCart()" title="Giỏ hàng">
          <i class="bi bi-bag"></i>
          <span class="lm-cart-badge" :style="badgeScale">{{ totalCount }}</span>
        </button>
        <button class="lm-nav-icon-btn d-none d-md-flex" @click="$router.push('/profile')" title="Tài khoản">
          <i class="bi bi-person"></i>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useCart } from '@/composables/useCart'

const { openCart, totalCount } = useCart()
const isScrolled = ref(false)
const badgeScale = ref('')

// Animate badge on count change
import { watch } from 'vue'
watch(totalCount, () => {
  badgeScale.value = 'transform: scale(1.4)'
  setTimeout(() => { badgeScale.value = '' }, 300)
})

function onScroll() { isScrolled.value = window.scrollY > 50 }
onMounted  (() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>
