<template>
  <div>
    <!-- Page Hero -->
    <div class="lm-page-hero" data-title="NOTIFICATIONS">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thông Báo</p>
        <h1 class="mb-3">Cập nhật <em>mới nhất</em></h1>
        <p>Theo dõi các tin tức, ưu đãi cực hot và thông báo hệ thống từ Zestia</p>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container py-5">
      <div class="row g-4">
        <!-- Sidebar controls (Filters & Actions) -->
        <div class="col-lg-3">
          <div class="z-notif-sidebar p-4 shadow-sm">
            <h5 class="mb-3 font-weight-bold" style="font-size: 16px; color: var(--z-dark)">Bộ lọc</h5>
            
            <!-- Type Tabs -->
            <div class="d-flex flex-column gap-2 mb-4">
              <button 
                v-for="tab in filterTabs" 
                :key="tab.value"
                class="lm-filter-btn text-start d-flex justify-content-between align-items-center"
                :class="{ active: currentTab === tab.value }"
                @click="currentTab = tab.value"
              >
                <span>{{ tab.label }}</span>
                <span class="badge rounded-pill" :class="currentTab === tab.value ? 'bg-dark' : 'bg-light text-dark'">
                  {{ getCountByTab(tab.value) }}
                </span>
              </button>
            </div>

            <hr style="border-color: var(--z-gray-border)" />

            <!-- Actions -->
            <button 
              class="lm-btn-outline-dark w-100 d-flex align-items-center justify-content-center gap-2 py-2 mb-2"
              @click="markAllRead"
              :disabled="unreadCount === 0"
            >
              <i class="bi bi-check2-all"></i>
              <span>Đọc tất cả</span>
            </button>
          </div>
        </div>

        <!-- Notification List -->
        <div class="col-lg-9">
          <!-- Search & Header bar -->
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div class="position-relative flex-grow-1" style="max-width: 480px;">
              <input 
                v-model="searchQuery" 
                type="text" 
                class="lm-input ps-5" 
                placeholder="Tìm kiếm thông báo..."
              />
              <i class="bi bi-search position-absolute" style="left: 18px; top: 50%; transform: translateY(-50%); color: var(--z-gray-light)"></i>
            </div>
            <div style="font-size: 13px; color: var(--z-gray)">
              Hiển thị <strong>{{ filteredNotifs.length }}</strong> thông báo
            </div>
          </div>

          <!-- Loader -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-secondary mb-3"></div>
            <p class="text-muted">Đang tải danh sách thông báo...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredNotifs.length === 0" class="z-empty-state text-center py-5">
            <div class="mb-4">
              <i class="bi bi-bell-slash" style="font-size: 56px; color: var(--z-gray-light)"></i>
            </div>
            <h3 class="lm-display mb-2" style="font-weight: 500; color: var(--z-dark)">Không tìm thấy thông báo</h3>
            <p class="text-muted mx-auto" style="max-width: 420px; font-size: 14px;">
              Hiện tại không có thông báo nào phù hợp với bộ lọc hoặc từ khóa tìm kiếm của bạn. Hãy thử thay đổi bộ lọc hoặc quay lại sau!
            </p>
          </div>

          <!-- List -->
          <div v-else class="d-flex flex-column gap-3">
            <div 
              v-for="(n, i) in paginatedNotifs" 
              :key="n.id"
              class="z-notif-card"
              :class="{ unread: !n.read, expanded: expandedId === n.id }"
              :style="{ animationDelay: i * 0.05 + 's' }"
              @click="toggleExpand(n)"
            >
              <!-- Card Header -->
              <div class="d-flex justify-content-between align-items-start gap-3">
                <div class="d-flex gap-3 align-items-start">
                  <div class="z-icon-wrapper" :class="n.loai">
                    <i :class="getIconClass(n.loai)"></i>
                  </div>
                  <div>
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                      <span class="badge-type" :class="n.loai">{{ getTypeName(n.loai) }}</span>
                      <span v-if="!n.read" class="badge-unread">Mới</span>
                    </div>
                    <h4 class="z-card-title mt-2 mb-1">{{ n.tieuDe }}</h4>
                    <p class="z-card-meta mb-0">{{ formatDateTime(n.ngayTao) }}</p>
                  </div>
                </div>
                <div class="z-arrow-icon">
                  <i class="bi bi-chevron-down" :class="{ rotated: expandedId === n.id }"></i>
                </div>
              </div>

              <!-- Card Body (Collapsed/Expanded) -->
              <div class="z-card-body" :class="{ show: expandedId === n.id }">
                <div class="z-card-content mt-3 p-3 rounded">
                  {{ n.noiDung }}
                </div>
                
                <!-- CTA buttons based on type -->
                <div v-if="n.loai === 'Voucher'" class="d-flex gap-2 mt-3 justify-content-end">
                  <button class="lm-btn-primary py-2 px-4" style="height:auto; font-size:12px;" @click.stop="$router.push('/collections')">
                    <span>Mua Sắm Ngay</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="d-flex justify-content-center gap-2 mt-5">
            <button class="lm-pagination-btn" :disabled="currentPage === 1" @click="currentPage--">
              <i class="bi bi-chevron-left"></i>
            </button>
            <button 
              v-for="page in totalPages" 
              :key="page"
              class="lm-pagination-btn"
              :class="{ active: currentPage === page }"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
            <button class="lm-pagination-btn" :disabled="currentPage === totalPages" @click="currentPage++">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const loading = ref(false)
const notifs = ref([])
const currentTab = ref('all')
const searchQuery = ref('')
const expandedId = ref(null)
const currentPage = ref(1)
const itemsPerPage = 8

const readIds = ref(JSON.parse(localStorage.getItem('read_notif_ids') || '[]'))

const filterTabs = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Voucher & ưu đãi', value: 'Voucher' },
  { label: 'Hệ thống', value: 'HeThong' },
  { label: 'Đơn hàng', value: 'DonHang' }
]

onMounted(() => {
  loadNotifications()
})

async function loadNotifications() {
  loading.value = true
  try {
    const list = await api().getThongBaoActive()
    notifs.value = list.map(n => ({
      ...n,
      read: readIds.value.includes(n.id)
    }))
  } catch (e) {
    console.error('Lỗi khi tải thông báo', e)
  } finally {
    loading.value = false
  }
}

// Watch inputs to reset pagination
watch([currentTab, searchQuery], () => {
  currentPage.value = 1
  expandedId.value = null
})

// Counts
const unreadCount = computed(() => {
  return notifs.value.filter(n => !readIds.value.includes(n.id)).length
})

function getCountByTab(tabValue) {
  if (tabValue === 'all') return notifs.value.length
  return notifs.value.filter(n => n.loai === tabValue).length
}

// Filtered Notifications
const filteredNotifs = computed(() => {
  return notifs.value.filter(n => {
    const matchesTab = currentTab.value === 'all' || n.loai === currentTab.value
    const matchesSearch = !searchQuery.value.trim() || 
      n.tieuDe.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
      n.noiDung.toLowerCase().includes(searchQuery.value.toLowerCase())
    return matchesTab && matchesSearch
  })
})

// Paginated Notifications
const totalPages = computed(() => Math.ceil(filteredNotifs.value.length / itemsPerPage))
const paginatedNotifs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredNotifs.value.slice(start, start + itemsPerPage)
})

// Actions
function toggleExpand(n) {
  if (expandedId.value === n.id) {
    expandedId.value = null
  } else {
    expandedId.value = n.id
    if (!readIds.value.includes(n.id)) {
      readIds.value.push(n.id)
      n.read = true
      localStorage.setItem('read_notif_ids', JSON.stringify(readIds.value))
      window.dispatchEvent(new Event('notifs-changed'))
    }
  }
}

function markAllRead() {
  let countUpdated = 0
  notifs.value.forEach(n => {
    if (!readIds.value.includes(n.id)) {
      readIds.value.push(n.id)
      n.read = true
      countUpdated++
    }
  })
  if (countUpdated > 0) {
    localStorage.setItem('read_notif_ids', JSON.stringify(readIds.value))
    window.dispatchEvent(new Event('notifs-changed'))
    toast.showToast('Đã đánh dấu tất cả thông báo là đã đọc', 'success')
  }
}

// Helper methods
function getIconClass(type) {
  const icons = {
    HeThong: 'bi bi-cpu',
    Voucher: 'bi bi-ticket-perforated',
    DonHang: 'bi bi-box-seam'
  }
  return icons[type] || 'bi bi-bell'
}

function getTypeName(type) {
  const names = {
    HeThong: 'Hệ thống',
    Voucher: 'Voucher',
    DonHang: 'Đơn hàng'
  }
  return names[type] || type
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>

<style scoped>
/* Sidebar and filters */
.z-notif-sidebar {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
}
.lm-filter-btn {
  border: none;
  background: none;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--z-gray);
  border-radius: var(--z-radius);
  transition: all 0.3s ease;
  width: 100%;
}
.lm-filter-btn:hover {
  background: var(--z-bg-alt);
  color: var(--z-dark);
}
.lm-filter-btn.active {
  background: var(--z-dark);
  color: var(--z-white) !important;
}

.lm-btn-outline-dark {
  background: transparent;
  border: 1px solid var(--z-dark);
  color: var(--z-dark);
  border-radius: var(--z-radius);
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
}
.lm-btn-outline-dark:hover:not(:disabled) {
  background: var(--z-dark);
  color: var(--z-white);
}
.lm-btn-outline-dark:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  border-color: var(--z-gray-border);
  color: var(--z-gray-light);
}

/* Cards */
.z-notif-card {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
  padding: 20px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  animation: z-item-in 0.5s ease both;
}
.z-notif-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0,0,0,0.04);
  border-color: var(--z-gray);
}
.z-notif-card.unread {
  background: var(--z-accent-soft);
  border-left: 4px solid var(--z-accent);
}

/* Icons */
.z-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.z-icon-wrapper.HeThong {
  background: #e0f2fe;
  color: #0284c7;
}
.z-icon-wrapper.Voucher {
  background: #fef3c7;
  color: #d97706;
}
.z-icon-wrapper.DonHang {
  background: #dcfce7;
  color: #16a34a;
}

/* Badges */
.badge-type {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}
.badge-type.HeThong {
  background: #e0f2fe;
  color: #0284c7;
}
.badge-type.Voucher {
  background: #fef3c7;
  color: #d97706;
}
.badge-type.DonHang {
  background: #dcfce7;
  color: #16a34a;
}
.badge-unread {
  background: var(--z-accent);
  color: var(--z-white);
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  text-transform: uppercase;
}

/* Headings and metadata */
.z-card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--z-dark);
}
.z-card-meta {
  font-size: 12px;
  color: var(--z-gray);
}

/* Body collapsing */
.z-arrow-icon {
  font-size: 18px;
  color: var(--z-gray-light);
  transition: transform 0.3s ease;
}
.z-arrow-icon i.rotated {
  transform: rotate(180deg);
  display: inline-block;
}

.z-card-body {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.z-card-body.show {
  max-height: 500px;
  opacity: 1;
}
.z-card-content {
  background: var(--z-bg);
  border: 1px solid var(--z-gray-border);
  font-size: 14px;
  color: var(--z-dark-soft);
  line-height: 1.6;
  white-space: pre-wrap;
}

/* Pagination */
.lm-pagination-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  cursor: pointer;
}
.lm-pagination-btn:hover:not(:disabled) {
  border-color: var(--z-dark);
  background: var(--z-bg-alt);
}
.lm-pagination-btn.active {
  background: var(--z-dark);
  border-color: var(--z-dark);
  color: var(--z-white);
}
.lm-pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@keyframes z-item-in {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
