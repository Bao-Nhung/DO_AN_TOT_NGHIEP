<template>
  <div class="d-flex" style="min-height:100vh">
    <aside class="z-admin-sidebar" :class="{ open: sidebarOpen }">
      <RouterLink class="z-admin-logo text-decoration-none" to="/admin" aria-label="Zestia - Tổng quan quản lý">
        <img class="z-admin-logo-mark" src="/images/brand/zestia-mark.png" alt="" aria-hidden="true">
        <span class="z-admin-logo-text">Zest<span style="color:var(--z-accent)">ia</span></span>
        <span class="z-admin-badge">{{ roleBadge }}</span>
      </RouterLink>

      <nav class="z-admin-nav">
        <RouterLink v-for="item in navItems" :key="item.path"
                    :to="item.path" class="z-admin-nav-item"
                    @click="sidebarOpen = false"
                    :class="{ active: $route.path === item.path }">
          <i class="bi" :class="item.icon"></i>
          <span>{{ item.label }}</span>
          <span v-if="item.badgeRef === 'pending' && pendingCount > 0" class="z-admin-nav-badge">
            {{ compactCount(pendingCount) }}
          </span>
        </RouterLink>
      </nav>

      <div class="mt-auto px-3 py-3 z-admin-account">
        <div class="d-flex align-items-center gap-2 mb-3">
          <div class="z-admin-avatar">{{ adminInitial }}</div>
          <div class="min-w-0">
            <div class="z-admin-user-name">{{ adminName }}</div>
            <div class="z-admin-user-email">{{ adminEmail }}</div>
          </div>
        </div>
        <button type="button" class="z-admin-nav-item w-100 text-start z-logout-btn" @click="handleLogout">
          <i class="bi bi-box-arrow-right"></i>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>

    <button class="z-admin-menu-toggle" type="button" title="Mở menu quản lý" aria-label="Mở menu quản lý"
            :aria-expanded="sidebarOpen" @click="sidebarOpen = !sidebarOpen">
      <i class="bi" :class="sidebarOpen ? 'bi-x-lg' : 'bi-list'"></i>
    </button>
    <div v-if="sidebarOpen" class="z-admin-sidebar-backdrop" @click="sidebarOpen = false"></div>

    <main class="z-admin-main">
      <div class="z-admin-utility-bar">
        <RouterLink to="/" class="z-admin-language text-decoration-none" title="Xem Website Cửa Hàng" aria-label="Xem Website Cửa Hàng">
          <i class="bi bi-box-arrow-up-right" aria-hidden="true"></i>
          <span>Xem Website</span>
        </RouterLink>
        <button type="button" class="z-admin-language" data-no-i18n
                :title="locale === 'vi' ? 'Chuyển sang tiếng Anh' : 'Switch to Vietnamese'"
                :aria-label="locale === 'vi' ? 'Chuyển sang tiếng Anh' : 'Switch to Vietnamese'"
                @click="toggleLocale">
          <i class="bi bi-globe2" aria-hidden="true"></i>
          <span>{{ locale.toUpperCase() }}</span>
        </button>
        <div ref="taskCenterRef" class="z-task-center">
          <button type="button" class="z-task-trigger"
                  aria-haspopup="dialog" :aria-expanded="taskCenterOpen"
                  :aria-label="taskTotal > 0 ? `Mở danh sách ${taskTotal} việc cần xử lý` : 'Mở danh sách việc cần xử lý'"
                  title="Việc cần xử lý"
                  @click="toggleTaskCenter">
            <i class="bi bi-bell" aria-hidden="true"></i>
            <span class="d-none d-sm-inline">Việc cần xử lý</span>
            <span v-if="taskTotal > 0" class="z-task-trigger-badge">{{ compactCount(taskTotal) }}</span>
          </button>

          <div v-if="taskCenterOpen" class="z-task-dropdown" role="dialog"
               aria-labelledby="staff-task-title">
            <div class="z-task-header">
              <div>
                <strong id="staff-task-title">Việc cần xử lý</strong>
                <span>{{ taskTotal > 0 ? `${taskTotal} việc đang chờ` : 'Danh sách công việc hiện tại' }}</span>
              </div>
              <button type="button" class="z-task-icon-btn" title="Làm mới" aria-label="Làm mới danh sách công việc"
                      :disabled="tasksLoading" @click="loadTasks()">
                <i class="bi bi-arrow-clockwise" :class="{ 'z-spin': tasksLoading }"></i>
              </button>
            </div>

            <div class="z-task-list">
              <div v-if="tasksLoading && taskItems.length === 0" class="z-task-state">
                <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
                <span>Đang tải công việc...</span>
              </div>

              <div v-else-if="taskError && taskItems.length === 0" class="z-task-state z-task-error">
                <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
                <span>{{ taskError }}</span>
                <button type="button" class="z-task-retry-btn" @click="loadTasks()">Thử lại</button>
              </div>

              <div v-else-if="taskItems.length === 0" class="z-task-state">
                <i class="bi bi-check2-circle z-task-complete" aria-hidden="true"></i>
                <strong>Không có việc tồn đọng</strong>
                <span>Các hàng đợi hiện đã được xử lý.</span>
              </div>

              <template v-else>
                <button v-for="task in taskItems" :key="task.key" type="button"
                        class="z-task-item" @click="openTask(task)">
                  <span class="z-task-item-icon" :class="`priority-${task.priority || 'medium'}`">
                    <i class="bi" :class="taskIcon(task.key)" aria-hidden="true"></i>
                  </span>
                  <span class="z-task-copy">
                    <strong>{{ task.title }}</strong>
                    <small>{{ task.description }}</small>
                  </span>
                  <span class="z-task-count">{{ compactCount(task.count) }}</span>
                  <i class="bi bi-chevron-right z-task-arrow" aria-hidden="true"></i>
                </button>
              </template>
            </div>

            <div v-if="tasksUpdatedAt" class="z-task-footer">
              Cập nhật lúc {{ tasksUpdatedAt }}
            </div>
          </div>
        </div>
      </div>

      <slot />
    </main>

    <!-- Human support needs the lower-right compose controls unobstructed. -->
    <AdminAiCopilot v-if="$route.path !== '/admin/support-chat'" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, useAuth } from '@/composables/useApi'
import { useI18n } from '@/composables/useI18n'
import AdminAiCopilot from '@/components/admin/AdminAiCopilot.vue'

const pendingCount = ref(0)
const shiftCanOperate = ref(false)
const sidebarOpen = ref(false)
const taskCenterOpen = ref(false)
const tasksLoading = ref(false)
const taskItems = ref([])
const taskTotal = ref(0)
const taskError = ref('')
const tasksUpdatedAt = ref('')
const taskCenterRef = ref(null)

const router = useRouter()
const { getUser, logout } = useAuth()
const { locale, toggleLocale } = useI18n()
const currentUser = getUser() || {}
const adminName = computed(() => currentUser.hoVaTen || currentUser.username || 'Nhân viên')
const adminEmail = computed(() => currentUser.email || currentUser.role || '')
const adminInitial = computed(() => (adminName.value || 'N').charAt(0).toUpperCase())
const roleName = computed(() => currentUser.role || '')
const isAdmin = computed(() => roleName.value === 'Admin')
const isEmployee = computed(() => ['NhanVien', 'Nhân viên'].includes(roleName.value))
const roleBadge = computed(() => isAdmin.value ? 'Admin' : 'Nhân viên')

const allNavItems = [
  { path: '/admin', icon: 'bi-grid-1x2', label: 'Tổng quan', adminOnly: true },
  { path: '/admin/thong-ke', icon: 'bi-bar-chart', label: 'Thống kê', adminOnly: true },
  { path: '/admin/pos', icon: 'bi-shop', label: 'Bán tại quầy' },
  { path: '/admin/products', icon: 'bi-bag', label: 'Sản phẩm', adminOnly: true },
  { path: '/admin/orders', icon: 'bi-receipt', label: 'Đơn hàng', badgeRef: 'pending' },
  { path: '/admin/returns', icon: 'bi-arrow-left-right', label: 'Đổi / trả hàng' },
  { path: '/admin/support-chat', icon: 'bi-headset', label: 'Hỗ trợ trực tuyến' },
  { path: '/admin/customers', icon: 'bi-people', label: 'Khách hàng', adminOnly: true },
  { path: '/admin/employees', icon: 'bi-person-badge', label: 'Nhân viên', adminOnly: true },
  { path: '/admin/schedule', icon: 'bi-calendar-week', label: 'Lịch làm việc' },
  { path: '/admin/vouchers', icon: 'bi-tag', label: 'Voucher', adminOnly: true },
  { path: '/admin/promotions', icon: 'bi-calendar2-event', label: 'Đợt khuyến mãi', adminOnly: true },
  { path: '/admin/lucky-wheel', icon: 'bi-stars', label: 'Vòng quay may mắn', adminOnly: true },
  { path: '/admin/channels', icon: 'bi-diagram-3', label: 'Kênh bán ngoài', adminOnly: true },
  { path: '/admin/notifications', icon: 'bi-bell', label: 'Thông báo khách hàng', adminOnly: true },
  { path: '/admin/settings', icon: 'bi-gear', label: 'Cài đặt', adminOnly: true },
]

const navItems = computed(() => {
  if (isAdmin.value) return allNavItems
  if (isEmployee.value && !shiftCanOperate.value) {
    return allNavItems.filter(item => item.path === '/admin/schedule')
  }
  return allNavItems.filter(item => !item.adminOnly || item.path === '/admin')
})

let taskRefreshTimer

onMounted(async () => {
  document.addEventListener('click', handleDocumentClick)
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('zestia-shift-changed', refreshShiftAndTasks)
  window.addEventListener('zestia-shift-required', handleShiftRequired)

  if (isEmployee.value) await loadShiftStatus()
  await loadTasks()

  taskRefreshTimer = window.setInterval(() => {
    if (document.visibilityState === 'visible') loadTasks({ silent: true })
  }, 30_000)
})

onBeforeUnmount(() => {
  window.clearInterval(taskRefreshTimer)
  document.removeEventListener('click', handleDocumentClick)
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('zestia-shift-changed', refreshShiftAndTasks)
  window.removeEventListener('zestia-shift-required', handleShiftRequired)
})

async function refreshShiftAndTasks() {
  await loadShiftStatus()
  await loadTasks({ silent: true })
}

function handleShiftRequired() {
  shiftCanOperate.value = false
  setScheduleTask('Bạn cần check-in đúng ca trước khi sử dụng các chức năng bán hàng.')
  if (router.currentRoute.value.name !== 'admin-schedule') {
    router.push({ name: 'admin-schedule' })
  }
}

async function loadShiftStatus() {
  if (!isEmployee.value) return
  try {
    const status = await api().getWorkShiftStatus()
    shiftCanOperate.value = Boolean(status?.canOperate)
    if (!shiftCanOperate.value) {
      setScheduleTask(status?.reason || 'Bạn chưa đủ điều kiện làm việc trong ca hiện tại.')
    }
  } catch (error) {
    shiftCanOperate.value = false
    setScheduleTask('Không thể xác minh ca làm. Vui lòng mở lịch làm việc để kiểm tra.')
  }
}

async function loadTasks({ silent = false } = {}) {
  if (isEmployee.value && !shiftCanOperate.value) {
    if (taskItems.value.length === 0) {
      setScheduleTask('Bạn chưa đủ điều kiện làm việc trong ca hiện tại.')
    }
    return
  }

  if (!silent) tasksLoading.value = true
  try {
    const response = await api().getStaffTasks()
    taskItems.value = Array.isArray(response?.items) ? response.items : []
    taskTotal.value = Number(response?.total) || 0
    pendingCount.value = Number(taskItems.value.find(item => item.key === 'orders')?.count) || 0
    taskError.value = ''
    tasksUpdatedAt.value = new Date().toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch (error) {
    if (error?.code === 'SHIFT_REQUIRED') {
      handleShiftRequired()
      return
    }
    taskError.value = error?.error || error?.message || 'Không thể tải danh sách công việc'
  } finally {
    tasksLoading.value = false
  }
}

function setScheduleTask(description) {
  taskItems.value = [{
    key: 'schedule',
    title: 'Kiểm tra ca làm việc',
    description,
    count: 1,
    route: '/admin/schedule',
    priority: 'high',
  }]
  taskTotal.value = 1
  pendingCount.value = 0
  taskError.value = ''
}

function toggleTaskCenter() {
  taskCenterOpen.value = !taskCenterOpen.value
  if (taskCenterOpen.value) loadTasks({ silent: taskItems.value.length > 0 })
}

function openTask(task) {
  taskCenterOpen.value = false
  sidebarOpen.value = false
  if (task?.route) router.push(task.route)
}

function handleDocumentClick(event) {
  if (!taskCenterOpen.value || taskCenterRef.value?.contains(event.target)) return
  taskCenterOpen.value = false
}

function handleKeydown(event) {
  if (event.key === 'Escape') taskCenterOpen.value = false
}

function taskIcon(key) {
  return {
    orders: 'bi-receipt',
    'support-chat': 'bi-chat-dots',
    returns: 'bi-arrow-left-right',
    'low-stock': 'bi-box-seam',
    schedule: 'bi-calendar-check',
  }[key] || 'bi-check2-square'
}

function compactCount(value) {
  const count = Math.max(0, Number(value) || 0)
  return count > 99 ? '99+' : String(count)
}

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<style scoped>
.z-admin-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--z-white);
  border-right: 1px solid var(--z-gray-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 100;
}
.z-admin-logo {
  padding: 20px;
  font-family: var(--z-font-display);
  font-size: 22px;
  font-weight: 600;
  color: var(--z-dark);
  border-bottom: 1px solid var(--z-gray-border);
  display: flex;
  align-items: center;
  gap: 10px;
}
.z-admin-logo-text { white-space: nowrap; }
.z-admin-logo-mark { width: 28px; height: 28px; object-fit: contain; flex: none; }
.z-admin-badge {
  font-family: var(--z-font-body);
  font-size: 10px;
  font-weight: 600;
  background: var(--z-accent-soft);
  color: var(--z-accent);
  padding: 2px 8px;
  border-radius: 20px;
}
.z-admin-nav {
  min-height: 0;
  padding: 12px 8px;
  flex: 1;
  overflow-y: auto;
}
.z-admin-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 400;
  color: var(--z-gray);
  text-decoration: none;
  border-radius: var(--z-radius);
  transition: var(--z-ease);
  cursor: pointer;
  margin-bottom: 2px;
}
.z-admin-nav-item:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-admin-nav-item.active { background: var(--z-accent-soft); color: var(--z-accent); font-weight: 600; }
.z-admin-nav-item i { width: 20px; text-align: center; font-size: 16px; }
.z-admin-nav-badge {
  min-width: 20px;
  margin-left: auto;
  padding: 2px 6px;
  border-radius: 20px;
  background: var(--z-accent);
  color: var(--z-white);
  font-size: 10px;
  font-weight: 700;
  text-align: center;
}
.z-admin-account { border-top: 1px solid var(--z-gray-border); }
.z-admin-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--z-accent);
  color: var(--z-white);
  font-size: 13px;
  font-weight: 600;
}
.min-w-0 { min-width: 0; }
.z-admin-user-name { color: var(--z-dark); font-size: 12px; font-weight: 600; }
.z-admin-user-email {
  max-width: 165px;
  overflow: hidden;
  color: var(--z-gray);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-logout-btn { border: 0; background: transparent; padding: 10px 12px; }

.z-admin-main {
  flex: 1;
  min-width: 0;
  min-height: 100vh;
  margin-left: 240px;
  padding: 20px 32px 28px;
  background: var(--z-bg);
}
.z-admin-utility-bar {
  position: relative;
  z-index: 90;
  display: flex;
  gap: 8px;
  min-height: 38px;
  justify-content: flex-end;
  margin-bottom: 10px;
}
.z-task-center { position: relative; }
.z-task-trigger,
.z-admin-language {
  height: 38px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 11px;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 12px;
  font-weight: 600;
  transition: var(--z-ease);
}
.z-admin-language {
  min-width: 58px;
  justify-content: center;
}
.z-task-trigger:hover,
.z-admin-language:hover {
  border-color: var(--z-accent);
  color: var(--z-accent);
  background: var(--z-accent-soft);
}
.z-task-trigger-badge {
  min-width: 20px;
  padding: 2px 5px;
  border-radius: 20px;
  background: var(--z-accent);
  color: var(--z-white);
  font-size: 10px;
  line-height: 16px;
  text-align: center;
}
.z-task-dropdown {
  position: absolute;
  top: 46px;
  right: 0;
  width: min(380px, calc(100vw - 32px));
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  box-shadow: 0 18px 48px rgba(27, 27, 31, 0.16);
}
.z-task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-task-header > div { display: flex; flex-direction: column; gap: 2px; }
.z-task-header strong { color: var(--z-dark); font-size: 13px; }
.z-task-header span { color: var(--z-gray); font-size: 10px; }
.z-task-icon-btn {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: var(--z-radius);
  background: transparent;
  color: var(--z-gray);
}
.z-task-icon-btn:hover:not(:disabled) { background: var(--z-bg-alt); color: var(--z-dark); }
.z-task-list { max-height: min(430px, 60vh); overflow-y: auto; }
.z-task-item {
  width: 100%;
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr) auto 14px;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 0;
  border-bottom: 1px solid var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-dark);
  text-align: left;
  transition: var(--z-ease);
}
.z-task-item:hover { background: var(--z-bg-alt); }
.z-task-item-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  color: var(--z-gray);
}
.z-task-item-icon.priority-high { background: var(--z-accent-soft); color: var(--z-accent-dark); }
.z-task-copy { min-width: 0; display: flex; flex-direction: column; gap: 3px; }
.z-task-copy strong { font-size: 12px; font-weight: 600; }
.z-task-copy small {
  display: -webkit-box;
  overflow: hidden;
  color: var(--z-gray);
  font-size: 10px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.z-task-count {
  min-width: 25px;
  padding: 3px 6px;
  border-radius: 20px;
  background: var(--z-dark);
  color: var(--z-white);
  font-size: 10px;
  font-weight: 700;
  text-align: center;
}
.z-task-arrow { color: var(--z-gray-light); font-size: 11px; }
.z-task-state {
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  padding: 24px;
  color: var(--z-gray);
  font-size: 11px;
  text-align: center;
}
.z-task-state strong { color: var(--z-dark); font-size: 12px; }
.z-task-complete { color: #2e7d32; font-size: 24px; }
.z-task-error i { color: var(--z-danger); font-size: 20px; }
.z-task-error button,
.z-task-retry-btn {
  border: 0;
  background: transparent;
  color: var(--z-accent);
  font-size: 11px;
  font-weight: 600;
}
.z-task-footer {
  padding: 8px 14px;
  background: var(--z-bg-alt);
  color: var(--z-gray);
  font-size: 9px;
  text-align: right;
}
.z-spin { animation: z-task-spin .8s linear infinite; }
@keyframes z-task-spin { to { transform: rotate(360deg); } }

.z-admin-menu-toggle,
.z-admin-sidebar-backdrop { display: none; }

@media (max-width: 900px) {
  .z-admin-sidebar { transform: translateX(-100%); transition: transform .25s ease; }
  .z-admin-sidebar.open { transform: translateX(0); }
  .z-admin-main { width: 100%; margin-left: 0; padding: 72px 16px 24px; overflow-x: hidden; }
  .z-admin-menu-toggle {
    position: fixed;
    z-index: 103;
    top: 14px;
    left: 14px;
    width: 42px;
    height: 42px;
    display: grid;
    place-items: center;
    border: 1px solid var(--z-gray-border);
    border-radius: var(--z-radius);
    background: var(--z-white);
    color: var(--z-dark);
    font-size: 20px;
  }
  .z-admin-sidebar-backdrop {
    position: fixed;
    z-index: 99;
    inset: 0;
    display: block;
    background: rgba(0, 0, 0, .35);
  }
  .z-admin-utility-bar {
    position: absolute;
    top: 16px;
    right: 16px;
    margin: 0;
  }
  .z-task-dropdown { position: fixed; top: 64px; right: 16px; }
}
</style>
