<template>
  <nav class="lm-navbar" :class="{ scrolled: isScrolled }">
    <div class="container-fluid px-4 h-100 d-flex align-items-center justify-content-between">

      <div class="lm-nav-logo" @click="$router.push('/')">
        Zest<span class="lm-gold-text">ia</span>
      </div>

      <ul class="d-none d-lg-flex list-unstyled mb-0 gap-4 align-items-center">
        <li><RouterLink class="lm-nav-link" to="/">Trang Chủ</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/collections">Sản Phẩm</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/wishlist">Yêu Thích</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/about">Thông Tin</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/profile">Tài Khoản</RouterLink></li>
      </ul>

      <div class="d-flex align-items-center gap-1">
        <button class="lm-nav-icon-btn" @click="toggleSearch" title="Tìm kiếm">
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
        <button class="lm-nav-icon-btn d-lg-none" @click="mobileOpen = !mobileOpen" title="Menu">
          <i class="bi" :class="mobileOpen ? 'bi-x-lg' : 'bi-list'"></i>
        </button>
      </div>
    </div>
  </nav>

  <!-- Mobile Menu -->
  <Transition name="z-mobile-menu">
    <div v-if="mobileOpen" class="z-mobile-menu" @click="mobileOpen = false">
      <nav class="z-mobile-menu-inner" @click.stop>
        <RouterLink class="z-mobile-link" to="/" @click="mobileOpen = false">Trang Chủ</RouterLink>
        <RouterLink class="z-mobile-link" to="/collections" @click="mobileOpen = false">Sản Phẩm</RouterLink>
        <RouterLink class="z-mobile-link" to="/wishlist" @click="mobileOpen = false">Yêu Thích</RouterLink>
        <RouterLink class="z-mobile-link" to="/about" @click="mobileOpen = false">Thông Tin</RouterLink>
        <RouterLink class="z-mobile-link" to="/profile" @click="mobileOpen = false">Tài Khoản</RouterLink>
      </nav>
    </div>
  </Transition>

  <!-- Search Overlay -->
  <div class="z-search-overlay" :class="{ open: searchOpen }" @click="searchOpen = false">
    <div class="z-search-box" @click.stop>
      <div class="d-flex align-items-center gap-3 px-2">
        <i class="bi bi-search" style="font-size:20px;color:var(--z-gray-light)"></i>
        <input ref="searchInput" class="z-search-input" v-model="searchQuery"
               placeholder="Tìm kiếm sản phẩm, danh mục..."
               @keydown.enter="doSearch" @keydown.esc="searchOpen = false">
        <button v-if="searchQuery" @click="searchQuery = ''"
                style="border:none;background:none;cursor:pointer;color:var(--z-gray);font-size:18px">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div v-if="searchResults.length" class="z-search-results">
        <div v-for="r in searchResults" :key="r.id" class="z-search-item"
             @click="goProduct(r.id)">
          <div style="width:48px;height:60px;border-radius:6px;overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
            <img v-if="r.image" :src="r.image" :alt="r.name" style="width:100%;height:100%;object-fit:cover">
            <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                 :style="{ background: r.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
              {{ r.letter }}
            </div>
          </div>
          <div>
            <div style="font-weight:500;font-size:14px;color:var(--z-dark)">{{ r.name }}</div>
            <div style="font-size:12px;color:var(--z-gray)">{{ r.category }} · {{ formatPrice(r.salePrice || r.price) }}</div>
          </div>
        </div>
      </div>
      <div v-else-if="searchQuery.length >= 2" class="px-4 py-3" style="color:var(--z-gray);font-size:14px;border-top:1px solid var(--z-gray-border)">
        Không tìm thấy sản phẩm nào
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { products, loadProducts } from '@/composables/useProducts'

const router = useRouter()
const { openCart, totalCount, formatPrice } = useCart()
const isScrolled = ref(false)
const badgeScale = ref('')
const searchOpen = ref(false)
const mobileOpen = ref(false)
const searchQuery = ref('')
const searchInput = ref(null)

onMounted(() => loadProducts())

const searchResults = computed(() => {
  if (searchQuery.value.length < 2) return []
  const q = searchQuery.value.toLowerCase()
  return products.value.filter(p =>
    p.name.toLowerCase().includes(q) ||
    p.category.toLowerCase().includes(q)
  )
})

function toggleSearch() {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    nextTick(() => searchInput.value?.focus())
  }
}

function doSearch() {
  if (searchResults.value.length) {
    goProduct(searchResults.value[0].id)
  }
}

function goProduct(id) {
  searchOpen.value = false
  searchQuery.value = ''
  router.push('/product/' + id)
}

watch(totalCount, () => {
  badgeScale.value = 'transform: scale(1.4)'
  setTimeout(() => { badgeScale.value = '' }, 300)
})

function onScroll() { isScrolled.value = window.scrollY > 50 }
onMounted  (() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>
