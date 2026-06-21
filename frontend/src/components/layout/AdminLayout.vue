<template>
  <div class="d-flex" style="min-height:100vh">
    <!-- Sidebar -->
    <aside class="z-admin-sidebar">
      <div class="z-admin-logo" @click="$router.push('/admin')">
        <span class="z-admin-logo-text">Zest<span style="color:var(--z-accent)">ia</span></span>
        <span class="z-admin-badge">Admin</span>
      </div>

      <nav class="z-admin-nav">
        <RouterLink v-for="item in navItems" :key="item.path"
                    :to="item.path" class="z-admin-nav-item"
                    :class="{ active: $route.path === item.path }">
          <i class="bi" :class="item.icon"></i>
          <span>{{ item.label }}</span>
          <span v-if="item.badgeRef === 'pending' && pendingCount > 0" class="z-admin-nav-badge">{{ pendingCount }}</span>
        </RouterLink>
      </nav>

      <div class="mt-auto px-3 py-3" style="border-top:1px solid var(--z-gray-border)">
        <div class="d-flex align-items-center gap-3 mb-3">
          <div style="width:36px;height:36px;border-radius:50%;background:var(--z-accent);display:flex;align-items:center;justify-content:center;color:var(--z-white);font-weight:600;font-size:14px">A</div>
          <div>
            <div style="font-size:13px;font-weight:600;color:var(--z-dark)">Admin</div>
            <div style="font-size:11px;color:var(--z-gray)">admin@zestia.vn</div>
          </div>
        </div>
        <button class="z-admin-nav-item w-100 text-start" style="border:none;background:none;padding:10px 12px" @click="$router.push('/login')">
          <i class="bi bi-box-arrow-right"></i>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <main class="z-admin-main">
      <slot/>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '@/composables/useApi'

const pendingCount = ref(0)

const navItems = [
  { path: '/admin',           icon: 'bi-grid-1x2',    label: 'Tổng quan' },
  { path: '/admin/pos',       icon: 'bi-shop',         label: 'Bán tại quầy' },
  { path: '/admin/products',  icon: 'bi-bag',          label: 'Sản phẩm' },
  { path: '/admin/orders',    icon: 'bi-receipt',      label: 'Đơn hàng', badgeRef: 'pending' },
  { path: '/admin/customers', icon: 'bi-people',       label: 'Khách hàng' },
  { path: '/admin/staff',     icon: 'bi-person-workspace', label: 'Nhân viên' },
  { path: '/admin/vouchers',  icon: 'bi-tag',          label: 'Khuyến mãi' },
  { path: '/admin/settings',  icon: 'bi-gear',         label: 'Cài đặt' },
]

onMounted(async () => {
  try {
    const orders = await api().getHoaDon()
    pendingCount.value = orders.filter(o => o.trangThai === 0).length
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.z-admin-sidebar {
  width: 240px; flex-shrink: 0;
  background: var(--z-white);
  border-right: 1px solid var(--z-gray-border);
  display: flex; flex-direction: column;
  position: fixed; top: 0; left: 0; bottom: 0;
  z-index: 100;
}
.z-admin-logo {
  padding: 20px 20px;
  font-family: var(--z-font-display);
  font-size: 22px; font-weight: 600;
  color: var(--z-dark);
  cursor: pointer;
  border-bottom: 1px solid var(--z-gray-border);
  display: flex; align-items: center; gap: 10px;
}
.z-admin-logo-text { white-space: nowrap; }
.z-admin-badge {
  font-family: var(--z-font-body);
  font-size: 10px; font-weight: 600;
  background: var(--z-accent-soft);
  color: var(--z-accent);
  padding: 2px 8px;
  border-radius: 20px;
}
.z-admin-nav { padding: 12px 8px; flex: 1; }
.z-admin-nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px;
  font-size: 14px; font-weight: 400;
  color: var(--z-gray);
  text-decoration: none;
  border-radius: var(--z-radius);
  transition: all 0.2s;
  cursor: pointer;
  margin-bottom: 2px;
}
.z-admin-nav-item:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-admin-nav-item.active { background: var(--z-accent-soft); color: var(--z-accent); font-weight: 600; }
.z-admin-nav-item i { font-size: 16px; width: 20px; text-align: center; }
.z-admin-nav-badge {
  margin-left: auto;
  font-size: 10px; font-weight: 700;
  background: var(--z-accent); color: var(--z-white);
  padding: 2px 7px; border-radius: 20px;
  min-width: 18px; text-align: center;
}
.z-admin-main {
  flex: 1; margin-left: 240px;
  background: var(--z-bg);
  padding: 28px 32px;
  min-height: 100vh;
}
</style>
