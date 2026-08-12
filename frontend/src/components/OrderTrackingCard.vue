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

    <!-- 5-Step Visual Stepper -->
    <div v-if="!isCancelledOrFailed" class="mb-4 pb-3 border-bottom">
      <div class="d-flex justify-content-between align-items-center position-relative px-2">
        <div class="position-absolute top-50 start-0 end-0 translate-middle-y bg-light" style="height:4px; z-index:1; margin: 0 30px;">
          <div class="bg-success h-100 transition-all" :style="{ width: stepProgressPercent + '%' }"></div>
        </div>
        <div v-for="(step, idx) in progressSteps" :key="step.key" class="position-relative text-center" style="z-index:2;">
          <div class="rounded-circle d-flex align-items-center justify-content-center mx-auto mb-1"
               :class="idx <= currentStepIndex ? 'bg-success text-white shadow-sm' : 'bg-light text-muted border'"
               style="width: 32px; height: 32px; font-size: 13px; font-weight: 600;">
            <i v-if="idx < currentStepIndex" class="bi bi-check-lg"></i>
            <span v-else>{{ idx + 1 }}</span>
          </div>
          <span style="font-size: 11px;" :class="idx <= currentStepIndex ? 'text-dark fw-bold' : 'text-muted'">{{ step.label }}</span>
        </div>
      </div>
    </div>

    <div class="z-timeline mt-4">
      <div v-for="(track, index) in displayHistory" :key="index" class="z-timeline-item">
        <div :class="['z-timeline-dot', `z-timeline-dot-${track.trangThai}`]"></div>
        <div class="z-timeline-content">
          <strong class="d-block" style="color: var(--z-dark);">{{ getTrackingStatusName(track.trangThai) }}</strong>
          <span class="text-muted d-block" style="font-size: 12px; margin-top: 2px;">
            <i class="bi bi-clock-history"></i> {{ formatDateTime(track.ngayCapNhat) }}
          </span>
          <p class="mt-2 mb-0" style="font-size: 13px; color: #4b5563;" v-if="track.moTa">{{ track.moTa }}</p>
        </div>
      </div>
      <div v-if="!displayHistory.length" class="text-muted" style="font-size:13px">
        Chưa có bản ghi cập nhật trạng thái cho đơn hàng này.
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  order: { type: Object, required: true }
})

// Map trạng thái số sang tracking key chuẩn 7 bước
const mapTrangThai = {
  0: 'pending',
  1: 'confirmed',
  2: 'processing',
  3: 'shipped',
  4: 'delivered',
  5: 'cancelled',
  6: 'failed',
  7: 'payment_failed',
  8: 'return_requested',
  9: 'refunded'
}

const computedTrackingStatus = computed(() => {
  if (props.order.trangThaiTracking && props.order.trangThaiTracking !== 'pending') {
    return props.order.trangThaiTracking;
  }
  return mapTrangThai[props.order.trangThai] || 'pending';
})

const progressSteps = [
  { key: 'pending', label: 'Đặt hàng' },
  { key: 'confirmed', label: 'Xác nhận' },
  { key: 'processing', label: 'Chuẩn bị' },
  { key: 'shipped', label: 'Đang giao' },
  { key: 'delivered', label: 'Hoàn tất' }
]

const currentStepIndex = computed(() => {
  const status = computedTrackingStatus.value
  const map = { pending: 0, confirmed: 1, processing: 2, shipped: 3, delivered: 4 }
  return map[status] ?? 0
})

const stepProgressPercent = computed(() => (currentStepIndex.value / (progressSteps.length - 1)) * 100)
const isCancelledOrFailed = computed(() => ['cancelled', 'failed', 'payment_failed', 'return_requested', 'refunded'].includes(computedTrackingStatus.value))

const statusClass = computed(() => {
  const map = {
    'pending': 'bg-secondary',
    'confirmed': 'bg-warning text-dark',
    'processing': 'bg-info text-dark',
    'shipped': 'bg-primary',
    'delivered': 'bg-success',
    'cancelled': 'bg-danger',
    'failed': 'bg-danger',
    'payment_failed': 'bg-danger',
    'return_requested': 'bg-warning text-dark',
    'refunded': 'bg-danger'
  }
  return map[computedTrackingStatus.value] || 'bg-secondary'
})

const statusText = computed(() => {
  return getTrackingStatusName(computedTrackingStatus.value)
})

const displayHistory = computed(() => {
  const history = Array.isArray(props.order.trackingHistory) ? props.order.trackingHistory : []
  return [...history].sort((a, b) => new Date(b.ngayCapNhat) - new Date(a.ngayCapNhat))
})

function getTrackingStatusName(status) {
  const map = {
    'pending': 'Chờ xác nhận',
    'confirmed': 'Đã xác nhận',
    'processing': 'Đang chuẩn bị hàng',
    'shipped': 'Đang giao hàng',
    'delivered': 'Giao thành công',
    'cancelled': 'Đã hủy',
    'failed': 'Giao thất bại'
  }
  map.payment_failed = 'Thanh toán thất bại'
  map.return_requested = 'Yêu cầu đổi/trả'
  map.refunded = 'Đã hoàn tiền'
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
  border-radius: var(--z-radius);
  padding: 24px;
}

.z-timeline {
  position: relative;
  padding-left: 24px;
  margin-left: 8px;
  border-left: 2px solid var(--z-gray-border);
}

.z-timeline-item {
  position: relative;
  padding-bottom: 28px;
}

.z-timeline-item:last-child {
  padding-bottom: 0;
}

.z-timeline-dot {
  position: absolute;
  left: -33px;
  top: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--z-gray-border);
  background: var(--z-white);
  z-index: 2;
}

.z-timeline-dot-pending { border-color: #6c757d; background: #e9ecef; }
.z-timeline-dot-confirmed { border-color: #f59e0b; background: #fef3c7; }
.z-timeline-dot-processing { border-color: #0dcaf0; background: #cff4fc; }
.z-timeline-dot-shipped { border-color: #0d6efd; background: #cfe2ff; }
.z-timeline-dot-delivered { border-color: #198754; background: #d1e7dd; }
.z-timeline-dot-cancelled { border-color: #dc3545; background: #f8d7da; }
.z-timeline-dot-failed { border-color: #dc3545; background: #f8d7da; }
.z-timeline-dot-payment_failed { border-color: #dc3545; background: #f8d7da; }
.z-timeline-dot-return_requested { border-color: #f59e0b; background: #fef3c7; }
.z-timeline-dot-refunded { border-color: #dc3545; background: #f8d7da; }
</style>
