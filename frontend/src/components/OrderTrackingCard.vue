<template>
  <div class="z-tracking-card">
    <div class="d-flex justify-content-between align-items-center mb-4 pb-3" style="border-bottom: 1px solid var(--z-gray-border);">
      <div>
        <h5 class="mb-1" style="font-weight: 600;">Mã đơn: {{ order.maHoaDon }}</h5>
        <span class="badge" :class="statusClass">{{ statusText }}</span>
      </div>
      <div class="text-end">
        <p class="mb-0 text-muted" style="font-size: 13px;">Ngày đặt: {{ formatDate(order.ngayTao) }}</p>
        <p class="mb-0 text-muted" style="font-size: 13px;" v-if="order.ngayGiaoHangDuKien">
          Dự kiến giao: <strong class="text-dark">{{ formatDate(order.ngayGiaoHangDuKien) }}</strong>
        </p>
      </div>
    </div>

    <div class="z-timeline mt-4">
      <div v-if="!order.trackingHistory || order.trackingHistory.length === 0" class="text-center text-muted py-3">
        Chưa có lịch sử cập nhật cho đơn hàng này.
      </div>
      
      <div v-for="(track, index) in order.trackingHistory" :key="index" class="z-timeline-item">
        <div :class="['z-timeline-dot', `z-timeline-dot-${track.trangThai}`]"></div>
        <div class="z-timeline-content">
          <strong class="d-block">{{ getTrackingStatusName(track.trangThai) }}</strong>
          <span class="text-muted d-block" style="font-size: 12px;">{{ formatDateTime(track.ngayCapNhat) }}</span>
          <p class="mt-1 mb-0" style="font-size: 14px;" v-if="track.moTa">{{ track.moTa }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  order: { type: Object, required: true }
})

const statusClass = computed(() => {
  const map = {
    'pending': 'bg-secondary',
    'processing': 'bg-info text-dark',
    'shipped': 'bg-primary',
    'delivered': 'bg-success',
    'cancelled': 'bg-danger'
  }
  return map[props.order.trangThaiTracking] || 'bg-secondary'
})

const statusText = computed(() => {
  return getTrackingStatusName(props.order.trangThaiTracking)
})

function getTrackingStatusName(status) {
  const map = {
    'pending': 'Chờ xác nhận',
    'processing': 'Đang chuẩn bị hàng',
    'shipped': 'Đang giao hàng',
    'delivered': 'Giao thành công',
    'cancelled': 'Đã hủy'
  }
  return map[status] || 'Không xác định'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' })
}
</script>

<style scoped>
.z-tracking-card {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 24px;
}

/* Tracking Timeline CSS (Từ bản commit của đồng đội) */
.z-timeline {
  position: relative;
  padding-left: 24px;
  margin-left: 8px;
  border-left: 2px solid var(--z-gray-border);
}

.z-timeline-item {
  position: relative;
  padding-bottom: 24px;
}

.z-timeline-item:last-child {
  padding-bottom: 0;
}

.z-timeline-dot {
  position: absolute;
  left: -33px; /* Bù trừ border */
  top: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--z-gray-border);
  background: var(--z-white);
  z-index: 2;
}

.z-timeline-dot-pending { border-color: #6c757d; background: #e9ecef; }
.z-timeline-dot-processing { border-color: #0dcaf0; background: #cff4fc; }
.z-timeline-dot-shipped { border-color: #0d6efd; background: #cfe2ff; }
.z-timeline-dot-delivered { border-color: #198754; background: #d1e7dd; }
.z-timeline-dot-cancelled { border-color: #dc3545; background: #f8d7da; }

.z-timeline-content strong { color: var(--z-dark); }
</style>