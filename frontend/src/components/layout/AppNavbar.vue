<template>
  <nav class="lm-navbar" :class="{ scrolled: isScrolled }">
    <div class="container-fluid z-nav-shell h-100 d-flex align-items-center justify-content-between">

      <RouterLink class="lm-nav-logo text-decoration-none" to="/" aria-label="Zestia - Trang chủ">
        <img class="z-brand-mark" src="/images/brand/zestia-mark.png" alt="" aria-hidden="true">
        <span>Zest<span class="lm-gold-text">ia</span></span>
      </RouterLink>

      <ul class="d-none d-xxl-flex list-unstyled mb-0 gap-3 align-items-center">
        <li><RouterLink class="lm-nav-link" to="/">{{ t('home') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/collections">{{ t('products') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/lookbook">{{ t('lookbook') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/reviews">{{ t('reviews') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/tracking">{{ t('tracking') }}</RouterLink></li>
        <li><RouterLink v-if="isLoggedIn()" class="lm-nav-link" to="/my-orders">{{ t('myOrders') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/wishlist">{{ t('wishlist') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/about">{{ t('about') }}</RouterLink></li>
        <li><RouterLink class="lm-nav-link" to="/profile">{{ t('account') }}</RouterLink></li>
        <li v-if="isStaffUser"><RouterLink class="lm-nav-link text-warning font-weight-bold" to="/admin" style="color:var(--z-accent)!important;font-weight:600;"><i class="bi bi-speedometer2 me-1"></i>Admin</RouterLink></li>
      </ul>

      <div class="d-flex align-items-center gap-1">
        <button type="button" class="lm-lang-btn ms-1 me-2" @click="toggleLocale"
                :title="t('switchLanguage')" :aria-label="t('switchLanguage')">
          <i class="bi bi-globe2" aria-hidden="true"></i>
          <span>{{ locale.toUpperCase() }}</span>
        </button>

        <button type="button" class="lm-nav-icon-btn" @click="toggleSearch" :title="t('search')" :aria-label="t('search')" :aria-expanded="searchOpen">
          <i class="bi bi-search"></i>
        </button>
        <button type="button" class="lm-nav-icon-btn d-none d-sm-flex" @click="$router.push('/wishlist')" :title="t('wishlist')" :aria-label="t('wishlist')">
          <i class="bi bi-heart"></i>
        </button>
        <button type="button" class="lm-nav-icon-btn d-none d-sm-flex" @click="$router.push('/compare')" :title="t('nav.compare')" :aria-label="t('nav.compare')">
          <i class="bi bi-columns-gap"></i>
          <span v-if="compareCount" class="lm-cart-badge">{{ compareCount }}</span>
        </button>
        <button type="button" class="lm-nav-icon-btn" @click="$router.push('/lucky-wheel')" :title="t('luckyWheel')" :aria-label="t('luckyWheel')">
          <i class="bi bi-stars"></i>
          <span class="z-new-feature-dot" aria-hidden="true"></span>
        </button>
        <button type="button" class="lm-nav-icon-btn text-danger" @click="$router.push('/collections?aiSearch=true')" title="Tìm sản phẩm theo màu từ ảnh" aria-label="Tìm sản phẩm theo màu từ ảnh">
          <i class="bi bi-camera-fill"></i>
        </button>

        <!-- Notifications Dropdown -->
        <div v-if="!isStaffUser" class="position-relative d-inline-block z-notif-container">
          <button type="button" class="lm-nav-icon-btn" @click="toggleNotifs" :title="t('notifications')" :aria-label="t('notifications')" :aria-expanded="notifOpen">
            <i class="bi bi-bell"></i>
            <span v-if="unreadCount > 0" class="lm-cart-badge" style="background:var(--z-accent)">{{ unreadCount }}</span>
          </button>
          
          <div v-if="notifOpen" class="z-notif-dropdown shadow">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-light">
              <strong style="font-size:12px;color:var(--z-dark)">{{ t('notifTitle') }}</strong>
              <button type="button" class="z-text-action" @click="markAllAsRead">{{ t('notifMarkAll') }}</button>
            </div>
            <div class="z-notif-list">
              <div v-if="loadingNotif" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-secondary"></div>
              </div>
              <div v-else-if="notifs.length === 0" class="text-center py-4 text-muted" style="font-size:11px">
                {{ t('notifEmpty') }}
              </div>
              <button v-else v-for="n in notifs" :key="n.id" type="button" class="z-notif-item" :class="{ unread: !n.read }" @click="viewNotif(n)">
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span :class="['z-status', getLabelClass(n.loai)]" style="font-size:8px; padding:1px 5px; height:auto; line-height:1.2;">{{ getTypeName(n.loai) }}</span>
                  <span class="z-notif-time">{{ formatTime(n.ngayTao) }}</span>
                </div>
                <div class="z-notif-title">{{ n.tieuDe }}</div>
                <div class="z-notif-body">{{ n.noiDung }}</div>
              </button>
            </div>
            <div class="p-2 border-top text-center bg-light">
              <RouterLink to="/notifications" class="text-decoration-none" style="font-size:11px;color:var(--z-dark);font-weight:600;display:block;padding:4px;" @click="notifOpen = false">
                {{ t('notifViewAll') }} <i class="bi bi-arrow-right ms-1"></i>
              </RouterLink>
            </div>
          </div>
        </div>

        <button type="button" class="lm-nav-icon-btn" @click="openCart()" :title="t('cart')" :aria-label="t('cart')">
          <i class="bi bi-bag"></i>
          <span class="lm-cart-badge" :style="badgeScale">{{ totalCount }}</span>
        </button>
        <button type="button" class="lm-nav-icon-btn d-none d-md-flex" @click="$router.push('/profile')" :title="t('account')" :aria-label="t('account')">
          <i class="bi bi-person"></i>
        </button>
        <button type="button" class="lm-nav-icon-btn d-xxl-none" @click="mobileOpen = !mobileOpen" :title="t('menu')" :aria-label="t('menu')" :aria-expanded="mobileOpen">
          <i class="bi" :class="mobileOpen ? 'bi-x-lg' : 'bi-list'"></i>
        </button>
      </div>
    </div>
  </nav>

  <Transition name="z-mobile-menu">
    <div v-if="mobileOpen" class="z-mobile-menu" @click="mobileOpen = false">
      <nav class="z-mobile-menu-inner" @click.stop>
        <RouterLink class="z-mobile-link" to="/" @click="mobileOpen = false">{{ t('home') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/collections" @click="mobileOpen = false">{{ t('products') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/lookbook" @click="mobileOpen = false">{{ t('lookbook') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/reviews" @click="mobileOpen = false">{{ t('reviews') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/tracking" @click="mobileOpen = false">{{ t('tracking') }}</RouterLink>
        <RouterLink v-if="isLoggedIn()" class="z-mobile-link" to="/my-orders" @click="mobileOpen = false">{{ t('myOrders') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/wishlist" @click="mobileOpen = false">{{ t('wishlist') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/compare" @click="mobileOpen = false">
          {{ t('nav.compare') }}<span v-if="compareCount"> ({{ compareCount }})</span>
        </RouterLink>
        <RouterLink class="z-mobile-link" to="/lucky-wheel" @click="mobileOpen = false">{{ t('luckyWheel') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/about" @click="mobileOpen = false">{{ t('about') }}</RouterLink>
        <RouterLink class="z-mobile-link" to="/profile" @click="mobileOpen = false">{{ t('account') }}</RouterLink>
        <RouterLink v-if="isStaffUser" class="z-mobile-link text-warning fw-bold" to="/admin" @click="mobileOpen = false" style="color:var(--z-accent)!important;font-weight:600;"><i class="bi bi-speedometer2 me-1"></i>Trang Admin</RouterLink>
        <div class="p-3 border-top mt-2 d-flex justify-content-between align-items-center">
          <span style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ t('languageLabel') }}</span>
          <button type="button" class="lm-lang-btn" @click="toggleLocale"
                  :title="t('switchLanguage')" :aria-label="t('switchLanguage')">
            <i class="bi bi-globe2" aria-hidden="true"></i>
            <span>{{ locale.toUpperCase() }}</span>
          </button>
        </div>
      </nav>
    </div>
  </Transition>

  <div class="z-search-overlay" :class="{ open: searchOpen }" :aria-hidden="!searchOpen" :inert="!searchOpen" @click="searchOpen = false">
    <div class="z-search-box" @click.stop>
      <div class="d-flex align-items-center gap-3 px-2">
        <i class="bi bi-search" style="font-size:20px;color:var(--z-gray-light)"></i>
        <input ref="searchInput" class="z-search-input" v-model="searchQuery"
               :placeholder="t('searchPlaceholder')"
               @keydown.enter="doSearch" @keydown.esc="searchOpen = false">
        <button v-if="searchQuery" type="button" class="z-input-icon-btn" :aria-label="t('clearSearch')" @click="searchQuery = ''">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div v-if="searchResults.length" class="z-search-results">
        <button v-for="r in searchResults" :key="r.id" type="button" class="z-search-item"
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
            <div style="font-size:12px;color:var(--z-gray)">{{ r.category }} · {{ formatPrice(r.price) }}</div>
          </div>
        </button>
      </div>
      <div v-else-if="searchQuery.length >= 2" class="px-4 py-3" style="color:var(--z-gray);font-size:14px;border-top:1px solid var(--z-gray-border)">
        {{ t('searchEmpty') }}
      </div>
    </div>
  </div>

  <!-- Notification Detail Modal -->
  <div v-if="activeNotifDetail" class="z-modal-overlay" @click.self="activeNotifDetail = null" style="z-index: 10000;">
    <div class="z-modal" style="max-width: 480px; background: var(--z-white); border-radius: var(--z-radius-lg); padding: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.12)">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <span :class="['z-status', getLabelClass(activeNotifDetail.loai)]">{{ getTypeName(activeNotifDetail.loai) }}</span>
        <button type="button" class="z-icon-btn z-icon-btn--close" :aria-label="t('closeNotification')" @click="activeNotifDetail = null"><i class="bi bi-x-lg"></i></button>
      </div>
      <h4 style="font-size:15px; font-weight:700; color:var(--z-dark); margin-bottom:8px;">{{ activeNotifDetail.tieuDe }}</h4>
      <div style="font-size:11px; color:var(--z-gray); margin-bottom:16px;">{{ t('publishedAt') }}: {{ formatDateTime(activeNotifDetail.ngayTao) }}</div>
      <div class="p-3 rounded bg-light" style="font-size:13px; line-height:1.6; color:var(--z-dark); border:1px solid var(--z-gray-border); white-space:pre-wrap;">
        {{ activeNotifDetail.noiDung }}
      </div>
      <div class="d-flex justify-content-end mt-4">
        <button class="lm-btn-primary" style="padding:10px 24px; font-size:12px; height:auto;" @click="activeNotifDetail = null">
          <span>{{ t('close') }}</span>
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
import { useI18n } from '@/composables/useI18n'
import { useCompare } from '@/composables/useCompare'

const router = useRouter()
const { openCart, totalCount, formatPrice } = useCart()
const { isLoggedIn, getUser } = useAuth()
const isStaffUser = computed(() => {
  if (!isLoggedIn()) return false
  const user = getUser()
  return user && ['Admin', 'NhanVien', 'Nhân viên'].includes(user.role)
})
const { t, toggleLocale, locale } = useI18n()
const { count: compareCount } = useCompare()
const isScrolled = ref(false)
const badgeScale = ref('')
const searchOpen = ref(false)
const mobileOpen = ref(false)
const searchQuery = ref('')
const searchInput = ref(null)

onMounted(() => {
  loadProducts().catch(() => {})
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
  window.addEventListener('notifs-changed', fetchNotifs)
  window.addEventListener('zestia-auth-changed', fetchNotifs)
  window.addEventListener('zestia-close-notifications', closeNotifications)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('click', closeNotifsOutside)
  window.removeEventListener('notifs-changed', fetchNotifs)
  window.removeEventListener('zestia-auth-changed', fetchNotifs)
  window.removeEventListener('zestia-close-notifications', closeNotifications)
})

// Notifications State & Actions
const notifOpen = ref(false)
const notifs = ref([])
const loadingNotif = ref(false)
const activeNotifDetail = ref(null)
const notifUnreadTotal = ref(0)

const unreadCount = computed(() => {
  return isLoggedIn() ? notifUnreadTotal.value : notifs.value.filter(n => !n.read).length
})

async function fetchNotifs() {
  if (isStaffUser.value) {
    notifs.value = []
    notifUnreadTotal.value = 0
    return
  }
  loadingNotif.value = true
  try {
    const authenticated = isLoggedIn()
    const data = authenticated
      ? await api().getCustomerNotifications({ page: 0, size: 6 })
      : await api().getThongBaoActive({ page: 0, size: 6 })
    const list = Array.isArray(data.content) ? data.content : []
    const guestReadIds = authenticated ? new Set() : loadGuestReadIds()
    notifs.value = list.map(n => ({
      ...n,
      read: authenticated ? Boolean(n.read) : isGuestNotificationRead(n, guestReadIds)
    }))
    notifUnreadTotal.value = authenticated
      ? Number(data.unreadCount || 0)
      : notifs.value.filter(n => !n.read).length
  } catch (e) {
    console.error('Lỗi khi tải thông báo', e)
  } finally {
    loadingNotif.value = false
  }
}

function toggleNotifs() {
  notifOpen.value = !notifOpen.value
  if (notifOpen.value) {
    window.dispatchEvent(new Event('zestia-close-customer-chat'))
    fetchNotifs()
    setTimeout(() => {
      document.addEventListener('click', closeNotifsOutside)
    }, 10)
  } else {
    document.removeEventListener('click', closeNotifsOutside)
  }
}

function closeNotifications() {
  notifOpen.value = false
  document.removeEventListener('click', closeNotifsOutside)
}

function closeNotifsOutside(e) {
  const container = document.querySelector('.z-notif-container')
  if (container && !container.contains(e.target)) {
    notifOpen.value = false
    document.removeEventListener('click', closeNotifsOutside)
  }
}

async function markAllAsRead() {
  try {
    if (isLoggedIn()) await api().markAllCustomerNotificationsRead()
    else saveGuestReadBefore(new Date().toISOString())
    notifs.value.forEach(n => { n.read = true })
    notifUnreadTotal.value = 0
    window.dispatchEvent(new Event('notifs-changed'))
  } catch (e) {
    console.error('Không thể đánh dấu thông báo', e)
  }
}

function viewNotif(n) {
  if (!n.read) {
    n.read = true
    if (isLoggedIn()) {
      notifUnreadTotal.value = Math.max(0, notifUnreadTotal.value - 1)
      api().markCustomerNotificationRead(n.id)
        .then(() => window.dispatchEvent(new Event('notifs-changed')))
        .catch(e => {
          n.read = false
          notifUnreadTotal.value++
          console.error('Không thể đánh dấu thông báo', e)
        })
    } else {
      saveGuestReadIds([...loadGuestReadIds(), n.id])
    }
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

function loadGuestReadIds() {
  try {
    return new Set(JSON.parse(sessionStorage.getItem('zestia_guest_read_notifications') || '[]').map(Number))
  } catch {
    return new Set()
  }
}

function saveGuestReadIds(ids) {
  sessionStorage.setItem('zestia_guest_read_notifications', JSON.stringify([...new Set(ids.map(Number))]))
}

function isGuestNotificationRead(notification, readIds = loadGuestReadIds()) {
  if (readIds.has(Number(notification.id))) return true
  const readBefore = Date.parse(sessionStorage.getItem('zestia_guest_notifications_read_before') || '')
  const createdAt = Date.parse(notification.ngayTao || '')
  return Number.isFinite(readBefore) && Number.isFinite(createdAt) && createdAt <= readBefore
}

function saveGuestReadBefore(value) {
  sessionStorage.setItem('zestia_guest_notifications_read_before', value)
}
</script>

<style scoped>
.z-nav-shell { padding-inline: 24px; }
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
  width: 100%;
  padding: 12px 16px;
  border: 0;
  border-bottom: 1px solid var(--z-bg-alt);
  background: var(--z-white);
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

.lm-lang-btn {
  border: 1px solid var(--z-gray-border);
  background: var(--z-bg-alt);
  min-width: 54px;
  height: 34px;
  padding: 0 9px;
  border-radius: var(--z-radius);
  font-size: 11px;
  font-weight: 700;
  color: var(--z-dark);
  cursor: pointer;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  transition: var(--z-ease);
}
.z-new-feature-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 7px;
  height: 7px;
  border: 2px solid var(--z-bg);
  border-radius: 50%;
  background: var(--z-accent);
}
.lm-lang-btn:hover {
  background: var(--z-accent);
  color: var(--z-white);
  border-color: var(--z-accent);
}

@media (max-width: 480px) {
  .z-nav-shell { padding-inline: 12px; }
  .lm-lang-btn.ms-1 { display: none; }
}
</style>
