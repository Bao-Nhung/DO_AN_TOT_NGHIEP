<template>
  <!-- Custom Cursor (client pages only) -->
  <div v-if="!isAdminPage" id="lm-cursor"></div>
  <div v-if="!isAdminPage" id="lm-cursor-ring"></div>

  <!-- Page Transition (client only) -->
  <div v-if="!isAdminPage" id="lm-page-transition" :class="transitionClass"></div>

  <!-- Toast -->
  <ToastNotification />

  <!-- Navbar (hidden on login & admin pages) -->
  <AppNavbar v-if="!isLoginPage && !isAdminPage" />

  <!-- Cart Drawer -->
  <CartDrawer v-if="!isLoginPage && !isAdminPage" />

  <!-- Page content -->
  <main :style="{ paddingTop: (isLoginPage || isAdminPage) ? '0' : '72px' }">
    <RouterView v-slot="{ Component }">
      <Transition name="page" @before-leave="onBeforeLeave" @after-enter="onAfterEnter">
        <component :is="Component" :key="$route.path" />
      </Transition>
    </RouterView>
  </main>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar        from '@/components/layout/AppNavbar.vue'
import CartDrawer       from '@/components/layout/CartDrawer.vue'
import ToastNotification from '@/components/layout/ToastNotification.vue'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'login')
const isAdminPage = computed(() => route.path.startsWith('/admin'))
const transitionClass = ref('')

watch(isAdminPage, (val) => {
  document.body.classList.toggle('z-admin-active', val)
}, { immediate: true })

function onBeforeLeave() {
  transitionClass.value = 'entering'
  setTimeout(() => { transitionClass.value = 'leaving' }, 500)
}
function onAfterEnter() {
  setTimeout(() => { transitionClass.value = '' }, 500)
}

// ── Custom Cursor ──
onMounted(() => {
  const cursor = document.getElementById('lm-cursor')
  const ring   = document.getElementById('lm-cursor-ring')
  if (!cursor || !ring) return

  document.addEventListener('mousemove', e => {
    cursor.style.left = e.clientX + 'px'
    cursor.style.top  = e.clientY + 'px'
    setTimeout(() => {
      ring.style.left = e.clientX + 'px'
      ring.style.top  = e.clientY + 'px'
    }, 60)
  })

  const targets = 'a, button, .lm-product-card, .lm-collection-card, .lm-wishlist-card, .lm-nav-icon-btn, .lm-nav-link'
  document.addEventListener('mouseover', e => {
    if (e.target.closest(targets)) document.body.classList.add('cursor-hover')
  })
  document.addEventListener('mouseout', e => {
    if (e.target.closest(targets)) document.body.classList.remove('cursor-hover')
  })
})
</script>

<style>
/* Vue page transition fallback (CSS) */
.page-enter-active,
.page-leave-active { transition: opacity 0.3s ease; }
.page-enter-from,
.page-leave-to     { opacity: 0; }
</style>
