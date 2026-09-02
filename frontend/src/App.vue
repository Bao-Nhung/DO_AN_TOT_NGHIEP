<template>
  <!-- Toast -->
  <ToastNotification />
  <ConfirmModal />
  <DialogAccessibility />

  <!-- Navbar (hidden on login & admin pages) -->
  <AppNavbar v-if="!isLoginPage && !isAdminPage" />

  <!-- Cart Drawer -->
  <CartDrawer v-if="!isLoginPage && !isAdminPage" />
  <CustomerChatWidget v-if="!isLoginPage && !isAdminPage" />

  <!-- Page content -->
  <main :class="{ 'z-app-main': !isLoginPage && !isAdminPage }">
    <RouterView v-slot="{ Component }">
      <Transition name="page" mode="out-in">
        <component :is="Component" :key="$route.path" />
      </Transition>
    </RouterView>
  </main>
</template>

<script setup>
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar        from '@/components/layout/AppNavbar.vue'
import CartDrawer       from '@/components/layout/CartDrawer.vue'
import CustomerChatWidget from '@/components/layout/CustomerChatWidget.vue'
import ToastNotification from '@/components/layout/ToastNotification.vue'
import ConfirmModal      from '@/components/ui/ConfirmModal.vue'
import DialogAccessibility from '@/components/ui/DialogAccessibility.vue'
import { api, useAuth } from '@/composables/useApi'
import { useCart } from '@/composables/useCart'
import { useWishlist } from '@/composables/useWishlist'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'login')
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const { getUser } = useAuth()
const {
  hydrateCart,
  resetCartForGuest,
  beginCartHydration,
  completeCartHydration
} = useCart()
const { hydrateWishlist, resetWishlistForGuest } = useWishlist()

if (getUser()?.role === 'KhachHang') beginCartHydration()

watch(isAdminPage, (val) => {
  document.body.classList.toggle('z-admin-active', val)
}, { immediate: true })

onMounted(() => {
  hydrateAccountData(true)
  window.addEventListener('zestia-auth-changed', onAuthChanged)
})

onUnmounted(() => window.removeEventListener('zestia-auth-changed', onAuthChanged))

function onAuthChanged() {
  hydrateAccountData(false)
}

async function hydrateAccountData(initialLoad) {
  const user = getUser()
  if (!user || user.role !== 'KhachHang') {
    completeCartHydration(true)
    if (!initialLoad) {
      resetCartForGuest()
      resetWishlistForGuest()
    }
    return
  }
  beginCartHydration()
  let cartHydrated = false
  try {
    const data = await api().getCustomerData()
    await hydrateCart(data?.cart || [], user.userId)
    cartHydrated = true
    hydrateWishlist(data?.wishlistIds || [], user.userId)
  } catch (error) {
    console.warn('Không thể tải dữ liệu mua sắm của tài khoản', error)
  } finally {
    completeCartHydration(cartHydrated)
  }
}
</script>

<style>
/* Vue page transition fallback (CSS) */
.page-enter-active,
.page-leave-active { transition: opacity 0.16s ease; }
.page-enter-from,
.page-leave-to     { opacity: 0; }
.z-app-main { padding-top: 72px; }
@media (max-width: 768px) { .z-app-main { padding-top: 60px; } }
</style>
