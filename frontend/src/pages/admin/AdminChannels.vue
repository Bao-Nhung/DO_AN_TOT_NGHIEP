<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">
          Quản lý Kênh Bán Ngoài (Omnichannel Sync)
        </h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">
          Đồng bộ tồn kho, giá bán và đơn hàng tự động với các sàn TMĐT TikTok Shop, Shopee, Lazada
        </p>
      </div>
      <div class="d-flex gap-2">
        <button class="lm-btn-secondary" :disabled="syncing" @click="runSync('ALL')">
          <i class="bi bi-arrow-repeat" :class="{ 'z-spin': syncing }"></i>
          <span>{{ syncing ? 'Đang đồng bộ...' : 'Làm mới kênh' }}</span>
        </button>
      </div>
    </div>

    <!-- Active Sales Channels Cards -->
    <div class="row g-3 mb-4">
      <div v-for="ch in channels" :key="ch.id" class="col-12 col-md-4">
        <div class="z-admin-card h-100 d-flex flex-column justify-content-between">
          <div>
            <div class="d-flex align-items-center justify-content-between mb-3">
              <div class="d-flex align-items-center gap-3">
                <div class="z-stat-icon" :style="{ background: ch.bg, color: ch.color }">
                  <i class="bi" :class="ch.icon"></i>
                </div>
                <div>
                  <h3 style="font-size:16px;font-weight:600;margin:0;color:var(--z-dark)">{{ ch.name }}</h3>
                  <small style="font-size:11px;color:var(--z-gray)">Mã cửa hàng: {{ ch.shopId }}</small>
                </div>
              </div>
              <span class="badge rounded-pill" :class="ch.connected ? 'bg-success' : 'bg-secondary'">
                {{ ch.connected ? 'Đã kết nối' : 'Ngắt kết nối' }}
              </span>
            </div>

            <div class="border-top border-bottom py-2 my-2" style="font-size:12px">
              <div class="d-flex justify-content-between py-1">
                <span style="color:var(--z-gray)">Sản phẩm đồng bộ:</span>
                <strong style="color:var(--z-dark)">{{ ch.syncedProducts }} mẫu</strong>
              </div>
              <div class="d-flex justify-content-between py-1">
                <span style="color:var(--z-gray)">Đơn hàng đã kéo về:</span>
                <strong style="color:var(--z-dark)">{{ ch.syncedOrders }} đơn</strong>
              </div>
              <div class="d-flex justify-content-between py-1">
                <span style="color:var(--z-gray)">Lần đồng bộ cuối:</span>
                <span style="color:var(--z-gray)">{{ ch.lastSync }}</span>
              </div>
            </div>
          </div>

          <div class="d-flex gap-2 mt-3">
            <button class="lm-btn-primary w-100 text-center py-2" style="font-size:12px;height:auto"
                    :disabled="syncing" @click="runSync(ch.id)">
              <i class="bi bi-cloud-arrow-up me-1"></i> Đồng bộ ngay
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Sync Action Bar -->
    <div class="z-admin-card mb-4" style="padding:20px 24px">
      <h3 style="font-size:16px;font-weight:600;color:var(--z-dark);margin-bottom:12px">
        <i class="bi bi-gear-wide-connected me-2" style="color:var(--z-accent)"></i>Thao tác Đồng bộ Nhanh
      </h3>
      <div class="d-flex flex-wrap gap-3">
        <button class="lm-btn-secondary" :disabled="syncing" @click="syncProducts">
          <i class="bi bi-box-seam me-1"></i> Đồng bộ Sản phẩm & Tồn kho lên TikTok / Shopee
        </button>
        <button class="lm-btn-secondary" :disabled="syncing" @click="fetchOrders">
          <i class="bi bi-cart-down me-1"></i> Tải Đơn Hàng Mới Từ Sàn Bán Hàng
        </button>
        <button class="lm-btn-secondary" :disabled="syncing" @click="clearLogs">
          <i class="bi bi-trash me-1"></i> Dọn nhật ký cũ
        </button>
      </div>
    </div>

    <!-- Sync Logs Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <div class="p-3 border-bottom d-flex align-items-center justify-content-between">
        <strong style="font-size:15px;color:var(--z-dark)">
          <i class="bi bi-card-checklist me-2"></i>Nhật ký Đồng bộ Kênh Bán ngoài (Audit Logs)
        </strong>
        <span class="badge bg-light text-dark" style="font-weight:600">{{ logs.length }} bản ghi</span>
      </div>
      <div class="table-responsive">
        <table class="z-table" style="min-width:700px">
          <thead>
            <tr>
              <th style="width:140px">Thời gian</th>
              <th style="width:130px">Kênh bán</th>
              <th style="width:180px">Loại thao tác</th>
              <th>Chi tiết xử lý</th>
              <th style="width:120px">Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.id">
              <td style="font-size:12px;color:var(--z-gray)">{{ log.time }}</td>
              <td>
                <span class="fw-bold" :style="{ color: channelColor(log.channel) }">
                  {{ log.channel }}
                </span>
              </td>
              <td style="font-weight:500">{{ log.action }}</td>
              <td style="font-size:13px">{{ log.detail }}</td>
              <td>
                <span class="z-status" :class="log.success ? 'success' : 'danger'">
                  {{ log.success ? 'Thành công' : 'Thất bại' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Toast Notification -->
    <div v-if="toastMsg" class="position-fixed bottom-0 end-0 p-3" style="z-index: 11000">
      <div class="toast show text-white bg-dark border-0 p-2 shadow-lg" role="alert">
        <div class="d-flex align-items-center gap-2 px-2 py-1">
          <i class="bi bi-check-circle-fill text-success" style="font-size:18px"></i>
          <div style="font-size:13px">{{ toastMsg }}</div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const syncing = ref(false)
const toastMsg = ref('')

const channels = ref([
  {
    id: 'TIKTOK',
    name: 'TikTok Shop Zestia',
    shopId: 'TTS-VN-88421',
    icon: 'bi-tiktok',
    bg: '#fef2f2',
    color: '#000000',
    connected: true,
    syncedProducts: 548,
    syncedOrders: 142,
    lastSync: 'Hôm nay, 12:30'
  },
  {
    id: 'SHOPEE',
    name: 'Shopee Mall Zestia',
    shopId: 'SPE-OFFICIAL-01',
    icon: 'bi-bag-check',
    bg: '#fff7ed',
    color: '#ee4d2d',
    connected: true,
    syncedProducts: 548,
    syncedOrders: 218,
    lastSync: 'Hôm nay, 11:45'
  },
  {
    id: 'LAZADA',
    name: 'Lazada LazMall Zestia',
    shopId: 'LAZ-ZESTIA-FLAGSHIP',
    icon: 'bi-shop-window',
    bg: '#eff6ff',
    color: '#0f146d',
    connected: true,
    syncedProducts: 548,
    syncedOrders: 95,
    lastSync: 'Hôm qua, 18:20'
  }
])

const logs = ref([
  {
    id: 1,
    time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
    channel: 'TikTok Shop',
    action: 'Đồng bộ tồn kho',
    detail: 'Cập nhật biến động tồn kho 548 sản phẩm thành công',
    success: true
  },
  {
    id: 2,
    time: '11:45',
    channel: 'Shopee Mall',
    action: 'Tải đơn hàng mới',
    detail: 'Tải về 12 đơn hàng mới thành công và ghi nhận mã hóa đơn HD-SPE-091',
    success: true
  },
  {
    id: 3,
    time: '09:15',
    channel: 'Lazada LazMall',
    action: 'Cập nhật giá bán',
    detail: 'Đồng bộ giá đợt khuyến mãi Tháng 8 cho 120 mẫu váy dạ hội',
    success: true
  }
])

function channelColor(name) {
  if (name.includes('TikTok')) return '#000'
  if (name.includes('Shopee')) return '#ee4d2d'
  return '#0f146d'
}

function showToast(msg) {
  toastMsg.value = msg
  setTimeout(() => { toastMsg.value = '' }, 3000)
}

function runSync(channelId) {
  syncing.value = true
  setTimeout(() => {
    syncing.value = false
    const now = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    const targetChannel = channels.value.find(c => c.id === channelId)
    const channelName = targetChannel ? targetChannel.name : 'Tất cả các sàn'

    if (targetChannel) {
      targetChannel.lastSync = 'Vừa xong, ' + now
    } else {
      channels.value.forEach(c => { c.lastSync = 'Vừa xong, ' + now })
    }

    logs.value.unshift({
      id: Date.now(),
      time: now,
      channel: targetChannel ? targetChannel.name.split(' ')[0] : 'Omnichannel',
      action: 'Đồng bộ tức thì',
      detail: `Đã kết nối API và đồng bộ 548 biến thể tồn kho thành công trên ${channelName}`,
      success: true
    })

    showToast(`Đã đồng bộ thành công dữ liệu với ${channelName}!`)
  }, 1200)
}

function syncProducts() {
  runSync('ALL')
}

function fetchOrders() {
  syncing.value = true
  setTimeout(() => {
    syncing.value = false
    const now = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    logs.value.unshift({
      id: Date.now(),
      time: now,
      channel: 'Shopee & TikTok',
      action: 'Tải đơn hàng',
      detail: 'Kiểm tra Webhook và tải về 5 đơn hàng mới tự động vào hệ thống POS/Admin',
      success: true
    })
    showToast('Đã tải 5 đơn hàng mới từ Shopee/TikTok Shop!')
  }, 1000)
}

function clearLogs() {
  logs.value = logs.value.slice(0, 3)
  showToast('Đã dọn dẹp các bản ghi nhật ký cũ.')
}
</script>

<style scoped>
.z-spin {
  animation: z-spin 1s linear infinite;
  display: inline-block;
}
@keyframes z-spin {
  to { transform: rotate(360deg); }
}
</style>
