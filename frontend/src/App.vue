<template>
  <!-- Custom Cursor -->
  <div id="lm-cursor"></div>
  <div id="lm-cursor-ring"></div>

  <!-- Page Transition -->
  <div id="lm-page-transition" :class="transitionClass"></div>

  <!-- Toast -->
  <ToastNotification />

  <!-- Navbar (hidden on login page) -->
  <AppNavbar v-if="!isLoginPage" />

  <!-- Cart Drawer -->
  <CartDrawer v-if="!isLoginPage" />

  <!-- Page content -->
  <main :style="{ paddingTop: isLoginPage ? '0' : '72px' }">
    <RouterView v-slot="{ Component }">
      <Transition name="page" @before-leave="onBeforeLeave" @after-enter="onAfterEnter">
        <component :is="Component" :key="$route.path" />
      </Transition>
    </RouterView>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar        from '@/components/layout/AppNavbar.vue'
import CartDrawer       from '@/components/layout/CartDrawer.vue'
import ToastNotification from '@/components/layout/ToastNotification.vue'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'login')
const transitionClass = ref('')

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
