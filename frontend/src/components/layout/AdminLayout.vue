<template>
  <div class="d-flex" style="min-height:100vh">
    <!-- Sidebar -->
    <aside class="z-admin-sidebar" :class="{ open: sidebarOpen }">
      <div class="z-admin-logo" @click="$router.push('/admin')">
        <span class="z-admin-logo-text">Zest<span style="color:var(--z-accent)">ia</span></span>
        <span class="z-admin-badge">{{ roleBadge }}</span>
      </div>

      <nav class="z-admin-nav">
        <RouterLink v-for="item in navItems" :key="item.path"
                    :to="item.path" class="z-admin-nav-item"
                    @click="sidebarOpen = false"
                    :class="{ active: $route.path === item.path }">
          <i class="bi" :class="item.icon"></i>
          <span>{{ item.label }}</span>
          <span v-if="item.badgeRef === 'pending' && pendingCount > 0" class="z-admin-nav-badge">{{ pendingCount }}</span>
        </RouterLink>
      </nav>

      <div class="mt-auto px-3 py-3" style="border-top:1px solid var(--z-gray-border)">
        <div class="d-flex align-items-center gap-3 mb-3">
          <div style="width:36px;height:36px;border-radius:50%;background:var(--z-accent);display:flex;align-items:center;justify-content:center;color:var(--z-white);font-weight:600;font-size:14px">{{ adminInitial }}</div>
          <div>
            <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ adminName }}</div>
            <div style="font-size:11px;color:var(--z-gray)">{{ adminEmail }}</div>
          </div>
        </div>
        <button class="z-admin-nav-item w-100 text-start" style="border:none;background:none;padding:10px 12px" @click="handleLogout">
          <i class="bi bi-box-arrow-right"></i>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>

    <button class="z-admin-menu-toggle" type="button" title="Mở menu quản lý" @click="sidebarOpen = !sidebarOpen">
      <i class="bi" :class="sidebarOpen ? 'bi-x-lg' : 'bi-list'"></i>
    </button>
    <div v-if="sidebarOpen" class="z-admin-sidebar-backdrop" @click="sidebarOpen = false"></div>

    <!-- Main content -->
    <main class="z-admin-main">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/composables/useApi'
import { useAuth } from '@/composables/useApi'

const pendingCount = ref(0)
const shiftCanOperate = ref(false)
const sidebarOpen = ref(false)
const router = useRouter()
const { getUser, logout } = useAuth()
const currentUser = computed(() => getUser() || {})
const adminName = computed(() => currentUser.value.hoVaTen || currentUser.value.username || 'Nhân viên')
const adminEmail = computed(() => currentUser.value.email || currentUser.value.role || '')
const adminInitial = computed(() => (adminName.value || 'N').charAt(0).toUpperCase())
const roleName = computed(() => currentUser.value.role || '')
const isAdmin = computed(() => roleName.value === 'Admin')
const isInventory = computed(() => ['QuanLyKho', 'Quản lý kho'].includes(roleName.value))
const isEmployee = computed(() => ['NhanVien', 'Nhân viên'].includes(roleName.value))
const roleBadge = computed(() => isAdmin.value ? 'Admin' : isInventory.value ? 'Quản lý kho' : 'Nhân viên')

const allNavItems = [
  { path: '/admin',           icon: 'bi-grid-1x2',    label: 'Tổng quan', adminOnly: true },
  { path: '/admin/thong-ke',  icon: 'bi-bar-chart',    label: 'Thống kê', adminOnly: true },
  { path: '/admin/pos',       icon: 'bi-shop',         label: 'Bán tại quầy' },
  { path: '/admin/products',  icon: 'bi-bag',          label: 'Sản phẩm', adminOnly: true },
  { path: '/admin/orders',    icon: 'bi-receipt',      label: 'Đơn hàng', badgeRef: 'pending' },
  { path: '/admin/returns',   icon: 'bi-arrow-left-right', label: 'Đổi / trả hàng' },
  { path: '/admin/support-chat', icon: 'bi-headset',    label: 'Hỗ trợ trực tuyến' },
  { path: '/admin/customers', icon: 'bi-people',       label: 'Khách hàng', adminOnly: true },
  { path: '/admin/employees', icon: 'bi-person-badge', label: 'Nhân viên', adminOnly: true },
  { path: '/admin/schedule',  icon: 'bi-calendar-week', label: 'Lịch làm việc' },
  { path: '/admin/vouchers',  icon: 'bi-tag',          label: 'Voucher', adminOnly: true },
  { path: '/admin/promotions', icon: 'bi-calendar2-event', label: 'Đợt khuyến mãi', adminOnly: true },
  { path: '/admin/notifications', icon: 'bi-bell',     label: 'Thông báo', adminOnly: true },
  { path: '/admin/settings',  icon: 'bi-gear',         label: 'Cài đặt', adminOnly: true },
]

const navItems = computed(() => {
  if (isAdmin.value) return allNavItems
  if (isInventory.value) return allNavItems.filter(item => ['/admin', '/admin/products'].includes(item.path))
  if (isEmployee.value && !shiftCanOperate.value) {
    return allNavItems.filter(item => ['/admin', '/admin/schedule'].includes(item.path))
  }
  return allNavItems.filter(item => !item.adminOnly || item.path === '/admin')
})

onMounted(async () => {
  if (isInventory.value) return
  if (isEmployee.value) {
    await loadShiftStatus()
    window.addEventListener('zestia-shift-changed', loadShiftStatus)
    if (!shiftCanOperate.value) return
  }
  try {
    const orders = await api().getHoaDon()
    pendingCount.value = orders.filter(o => o.trangThai === 0).length
  } catch (e) { /* ignore */ }
})

onBeforeUnmount(() => window.removeEventListener('zestia-shift-changed', loadShiftStatus))

async function loadShiftStatus() {
  if (!isEmployee.value) return
  try {
    const status = await api().getWorkShiftStatus()
    shiftCanOperate.value = Boolean(status?.canOperate)
  } catch (e) {
    shiftCanOperate.value = false
  }
}

function handleLogout() {
  logout()
  router.push('/login')
}
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
.z-admin-menu-toggle { display: none; }
.z-admin-sidebar-backdrop { display: none; }
@media (max-width: 900px) {
  .z-admin-sidebar { transform: translateX(-100%); transition: transform .25s ease; }
  .z-admin-sidebar.open { transform: translateX(0); }
  .z-admin-main { width: 100%; margin-left: 0; padding: 72px 16px 24px; overflow-x: hidden; }
  .z-admin-menu-toggle {
    position: fixed; z-index: 103; top: 14px; left: 14px;
    display: grid; place-items: center; width: 42px; height: 42px;
    border: 1px solid var(--z-gray-border); border-radius: var(--z-radius);
    background: var(--z-white); color: var(--z-dark); font-size: 20px;
  }
  .z-admin-sidebar-backdrop { position: fixed; z-index: 99; inset: 0; display: block; background: rgba(0,0,0,.35); }
}
</style>
