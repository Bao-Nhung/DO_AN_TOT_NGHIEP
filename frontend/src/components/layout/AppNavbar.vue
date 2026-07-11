<template>
  <nav class="lm-navbar" :class="{ scrolled: isScrolled }">
    <div class="container-fluid px-4 h-100 d-flex align-items-center justify-content-between">

      <div class="lm-nav-logo" @click="$router.push('/')">
        Zest<span class="lm-gold-text">ia</span>
      </div>

      <ul class="d-none d-lg-flex list-unstyled mb-0 gap-4 align-items-center">
        <li><RouterLink class="lm-nav-link" to="/">Trang Chủ</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/collections">Sản Phẩm</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/tracking">Tra Cứu Đơn</RouterLink></li>
        <li><RouterLink v-if="isLoggedIn()" class="lm-nav-link" to="/my-orders">Đơn Hàng</RouterLink></li>
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

        <!-- Notifications Dropdown -->
        <div class="position-relative d-inline-block z-notif-container">
          <button class="lm-nav-icon-btn" @click="toggleNotifs" title="Thông báo">
            <i class="bi bi-bell"></i>
            <span v-if="unreadCount > 0" class="lm-cart-badge" style="background:var(--z-accent)">{{ unreadCount }}</span>
          </button>
          
          <div v-if="notifOpen" class="z-notif-dropdown shadow">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-light">
              <strong style="font-size:12px;color:var(--z-dark)">Thông báo</strong>
              <button @click="markAllAsRead" style="border:none;background:none;font-size:11px;color:var(--z-accent);font-weight:600;cursor:pointer">Đọc hết</button>
            </div>
            <div class="z-notif-list">
              <div v-if="loadingNotif" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-secondary"></div>
              </div>
              <div v-else-if="notifs.length === 0" class="text-center py-4 text-muted" style="font-size:11px">
                Không có thông báo mới
              </div>
              <div v-else v-for="n in notifs" :key="n.id" class="z-notif-item" :class="{ unread: !n.read }" @click="viewNotif(n)">
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span :class="['z-status', getLabelClass(n.loai)]" style="font-size:8px; padding:1px 5px; height:auto; line-height:1.2;">{{ getTypeName(n.loai) }}</span>
                  <span class="z-notif-time">{{ formatTime(n.ngayTao) }}</span>
                </div>
                <div class="z-notif-title">{{ n.tieuDe }}</div>
                <div class="z-notif-body">{{ n.noiDung }}</div>
              </div>
            </div>
            <div class="p-2 border-top text-center bg-light">
              <RouterLink to="/notifications" class="text-decoration-none" style="font-size:11px;color:var(--z-dark);font-weight:600;display:block;padding:4px;" @click="notifOpen = false">
                Xem tất cả thông báo <i class="bi bi-arrow-right ms-1"></i>
              </RouterLink>
            </div>
          </div>
        </div>

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

  <Transition name="z-mobile-menu">
    <div v-if="mobileOpen" class="z-mobile-menu" @click="mobileOpen = false">
      <nav class="z-mobile-menu-inner" @click.stop>
        <RouterLink class="z-mobile-link" to="/" @click="mobileOpen = false">Trang Chủ</RouterLink>
        <RouterLink class="z-mobile-link" to="/collections" @click="mobileOpen = false">Sản Phẩm</RouterLink>
        <RouterLink class="z-mobile-link" to="/tracking" @click="mobileOpen = false">Tra Cứu Đơn</RouterLink>
        <RouterLink v-if="isLoggedIn()" class="z-mobile-link" to="/my-orders" @click="mobileOpen = false">Đơn Hàng</RouterLink>
        <RouterLink class="z-mobile-link" to="/wishlist" @click="mobileOpen = false">Yêu Thích</RouterLink>
        <RouterLink class="z-mobile-link" to="/about" @click="mobileOpen = false">Thông Tin</RouterLink>
        <RouterLink class="z-mobile-link" to="/profile" @click="mobileOpen = false">Tài Khoản</RouterLink>
      </nav>
    </div>
  </Transition>

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

  <!-- Notification Detail Modal -->
  <div v-if="activeNotifDetail" class="z-modal-overlay" @click.self="activeNotifDetail = null" style="z-index: 10000;">
    <div class="z-modal" style="max-width: 480px; background: var(--z-white); border-radius: var(--z-radius-lg); padding: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.12)">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <span :class="['z-status', getLabelClass(activeNotifDetail.loai)]">{{ getTypeName(activeNotifDetail.loai) }}</span>
        <button class="z-icon-btn" @click="activeNotifDetail = null"><i class="bi bi-x-lg"></i></button>
      </div>
      <h4 style="font-size:15px; font-weight:700; color:var(--z-dark); margin-bottom:8px;">{{ activeNotifDetail.tieuDe }}</h4>
      <div style="font-size:11px; color:var(--z-gray); margin-bottom:16px;">Ngày đăng: {{ formatDateTime(activeNotifDetail.ngayTao) }}</div>
      <div class="p-3 rounded bg-light" style="font-size:13px; line-height:1.6; color:var(--z-dark); border:1px solid var(--z-gray-border); white-space:pre-wrap;">
        {{ activeNotifDetail.noiDung }}
      </div>
      <div class="d-flex justify-content-end mt-4">
        <button class="lm-btn-primary" style="padding:10px 24px; font-size:12px; height:auto;" @click="activeNotifDetail = null">
          <span>Đóng</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { products, loadProducts } from '@/composables/useProducts'
import { api, useAuth } from '@/composables/useApi'

const router = useRouter()
const { openCart, totalCount, formatPrice } = useCart()
const { isLoggedIn } = useAuth()
const isScrolled = ref(false)
const badgeScale = ref('')
const searchOpen = ref(false)
const mobileOpen = ref(false)
const searchQuery = ref('')
const searchInput = ref(null)

onMounted(() => {
  loadProducts()
  fetchNotifs()
})

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
onMounted(() => {
  window.addEventListener('scroll', onScroll)
  window.addEventListener('notifs-changed', syncReadIds)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('click', closeNotifsOutside)
  window.removeEventListener('notifs-changed', syncReadIds)
})

// Notifications State & Actions
const notifOpen = ref(false)
const notifs = ref([])
const loadingNotif = ref(false)
const activeNotifDetail = ref(null)
const readIds = ref(JSON.parse(localStorage.getItem('read_notif_ids') || '[]'))

const unreadCount = computed(() => {
  return notifs.value.filter(n => !readIds.value.includes(n.id)).length
})

function syncReadIds() {
  readIds.value = JSON.parse(localStorage.getItem('read_notif_ids') || '[]')
  notifs.value.forEach(n => {
    n.read = readIds.value.includes(n.id)
  })
}

async function fetchNotifs() {
  loadingNotif.value = true
  try {
    const list = await api().getThongBaoActive()
    notifs.value = list.map(n => ({
      ...n,
      read: readIds.value.includes(n.id)
    }))
  } catch (e) {
    console.error('Lỗi khi tải thông báo', e)
  } finally {
    loadingNotif.value = false
  }
}

function toggleNotifs() {
  notifOpen.value = !notifOpen.value
  if (notifOpen.value) {
    fetchNotifs()
    setTimeout(() => {
      document.addEventListener('click', closeNotifsOutside)
    }, 10)
  } else {
    document.removeEventListener('click', closeNotifsOutside)
  }
}

function closeNotifsOutside(e) {
  const container = document.querySelector('.z-notif-container')
  if (container && !container.contains(e.target)) {
    notifOpen.value = false
    document.removeEventListener('click', closeNotifsOutside)
  }
}

function markAllAsRead() {
  notifs.value.forEach(n => {
    if (!readIds.value.includes(n.id)) {
      readIds.value.push(n.id)
    }
    n.read = true
  })
  localStorage.setItem('read_notif_ids', JSON.stringify(readIds.value))
}

function viewNotif(n) {
  if (!readIds.value.includes(n.id)) {
    readIds.value.push(n.id)
    n.read = true
    localStorage.setItem('read_notif_ids', JSON.stringify(readIds.value))
  }
  activeNotifDetail.value = n
  notifOpen.value = false
}

function getLabelClass(type) {
  const classes = {
    HeThong: 'info',
    Voucher: 'success',
    DonHang: 'warning'
  }
  return classes[type] || 'info'
}

function getTypeName(type) {
  const names = {
    HeThong: 'Hệ thống',
    Voucher: 'Voucher',
    DonHang: 'Đơn hàng'
  }
  return names[type] || type
}

function formatTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>

<style scoped>
.z-notif-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 320px;
  max-height: 420px;
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
  margin-top: 10px;
  overflow: hidden;
  z-index: 1010;
  display: flex;
  flex-direction: column;
}
.z-notif-list {
  overflow-y: auto;
  flex: 1;
}
.z-notif-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--z-bg-alt);
  cursor: pointer;
  transition: background 0.2s ease;
  text-align: left;
}
.z-notif-item:hover {
  background: var(--z-bg-alt);
}
.z-notif-item.unread {
  background: var(--z-accent-soft);
}
.z-notif-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--z-dark);
  margin-top: 4px;
}
.z-notif-body {
  font-size: 12px;
  color: var(--z-gray);
  margin-top: 2px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.z-notif-time {
  font-size: 10px;
  color: var(--z-gray-light);
}

.z-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(17, 17, 20, 0.48);
  backdrop-filter: blur(3px);
  box-sizing: border-box;
}

.z-modal {
  width: min(480px, calc(100vw - 32px));
  max-height: min(80vh, 640px);
  overflow-y: auto;
}
</style>
