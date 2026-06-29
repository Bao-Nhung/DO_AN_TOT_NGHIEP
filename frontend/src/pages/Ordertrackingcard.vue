<template>
  <div class="z-order-tracking-card">
    <div class="z-order-header">
      <div>
        <div class="z-order-id">{{ order.maHoaDon }}</div>
        <div class="z-order-date">{{ formatDate(order.ngayTao) }}</div>
      </div>
      <span :class="['z-order-status', `z-status-${getStatusKey(order.trangThaiTracking)}`]">
        {{ getStatusLabel(order.trangThaiTracking) }}
      </span>
    </div>

    <div class="z-order-body">
      <!-- Progress Bar -->
      <div class="z-progress-section">
        <div class="z-progress-bar">
          <div class="z-progress-fill" :style="{ width: getProgressPercent() + '%' }"></div>
        </div>
        <div class="z-progress-steps">
          <div v-for="step in progressSteps" :key="step.id" class="z-progress-step" :class="{ active: isStepActive(step) }">
            <div class="z-step-icon">
              <i :class="step.icon"></i>
            </div>
            <div class="z-step-label">{{ step.label }}</div>
          </div>
        </div>
      </div>

      <!-- Estimated Delivery -->
      <div v-if="order.ngayGiaoHangDuKien" class="z-delivery-info">
        <i class="bi bi-calendar-event"></i>
        <div>
          <div class="z-delivery-label">Dự kiến giao hàng</div>
          <div class="z-delivery-date">{{ formatDate(order.ngayGiaoHangDuKien) }}</div>
        </div>
      </div>

      <!-- Address -->
      <div v-if="order.diaChiGiaoHang" class="z-address-info">
        <i class="bi bi-geo-alt"></i>
        <div>
          <div class="z-address-label">Địa chỉ giao</div>
          <div class="z-address-text">{{ order.diaChiGiaoHang }}</div>
        </div>
      </div>

      <!-- Total -->
      <div class="z-total-info">
        <span>Tổng tiền:</span>
        <span class="z-total-amount">{{ formatMoney(order.tongTien) }}</span>
      </div>
    </div>

    <!-- Action Button -->
    <button class="z-order-btn-view" @click="$emit('view')">
      <i class="bi bi-arrow-up-right"></i> Xem chi tiết
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['view'])

const progressSteps = [
  { id: 1, label: 'Chờ xử lý', icon: 'bi-hourglass-split', status: 'pending' },
  { id: 2, label: 'Đang xử lý', icon: 'bi-box', status: 'processing' },
  { id: 3, label: 'Đã gửi', icon: 'bi-truck', status: 'shipped' },
  { id: 4, label: 'Đã giao', icon: 'bi-check-circle', status: 'delivered' }
]

const statusMap = {
  pending: { key: 'pending', label: '⏳ Chờ xử lý' },
  processing: { key: 'processing', label: '📦 Đang xử lý' },
  shipped: { key: 'shipped', label: '🚚 Đã gửi' },
  delivered: { key: 'delivered', label: '✅ Đã giao' },
  cancelled: { key: 'cancelled', label: '❌ Đã hủy' }
}

const statusOrder = ['pending', 'processing', 'shipped', 'delivered']

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function formatMoney(amount) {
  return Number(amount || 0).toLocaleString('vi-VN') + 'đ'
}

function getStatusLabel(status) {
  return statusMap[status]?.label || '⏳ Chờ xử lý'
}

function getStatusKey(status) {
  return statusMap[status]?.key || 'pending'
}

function getProgressPercent() {
  const status = props.order.trangThaiTracking
  const index = statusOrder.indexOf(status)
  if (status === 'cancelled') return 0
  if (index === -1) return 0
  return ((index + 1) / statusOrder.length) * 100
}

function isStepActive(step) {
  const currentStatus = props.order.trangThaiTracking
  if (currentStatus === 'cancelled') return false
  const currentIndex = statusOrder.indexOf(currentStatus)
  const stepIndex = statusOrder.indexOf(step.status)
  return stepIndex <= currentIndex
}
</script>

<style scoped>
.z-order-tracking-card {
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.z-order-tracking-card:hover {
  border-color: var(--z-accent);
  box-shadow: 0 8px 24px rgba(212, 86, 78, 0.1);
}

.z-order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--z-gray-border);
}

.z-order-id {
  font-size: 14px;
  font-weight: 600;
  color: var(--z-dark);
  margin-bottom: 4px;
}

.z-order-date {
  font-size: 12px;
  color: var(--z-gray);
}

.z-order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
  white-space: nowrap;
}

.z-status-pending {
  background: #f3f4f6;
  color: #6b7280;
}

.z-status-processing {
  background: #eff6ff;
  color: #3b82f6;
}

.z-status-shipped {
  background: #fffbeb;
  color: #f59e0b;
}

.z-status-delivered {
  background: #f0fdf4;
  color: #10b981;
}

.z-status-cancelled {
  background: #fef2f2;
  color: #ef4444;
}

.z-order-body {
  margin-bottom: 16px;
}

/* Progress Section */
.z-progress-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--z-gray-border);
}

.z-progress-bar {
  height: 4px;
  background: var(--z-gray-border);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 16px;
}

.z-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--z-accent), #ef9b7f);
  transition: width 0.4s ease;
}

.z-progress-steps {
  display: flex;
  justify-content: space-between;
}

.z-progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.z-progress-step.active {
  opacity: 1;
}

.z-step-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--z-gray-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--z-gray);
  transition: all 0.3s ease;
}

.z-progress-step.active .z-step-icon {
  background: var(--z-accent);
  color: white;
}

.z-step-label {
  font-size: 11px;
  color: var(--z-gray);
  text-align: center;
  max-width: 60px;
  transition: color 0.3s ease;
}

.z-progress-step.active .z-step-label {
  color: var(--z-dark);
  font-weight: 500;
}

/* Info Sections */
.z-delivery-info,
.z-address-info {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 12px;
  background: var(--z-bg-alt);
  border-radius: 8px;
  font-size: 13px;
}

.z-delivery-info i,
.z-address-info i {
  font-size: 16px;
  color: var(--z-accent);
  flex-shrink: 0;
  margin-top: 2px;
}

.z-delivery-label,
.z-address-label {
  font-size: 11px;
  color: var(--z-gray);
  margin-bottom: 2px;
}

.z-delivery-date,
.z-address-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--z-dark);
}

.z-total-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: var(--z-accent-soft);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.z-total-amount {
  color: var(--z-accent);
  font-size: 16px;
}

/* Button */
.z-order-btn-view {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid var(--z-accent);
  background: white;
  color: var(--z-accent);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.z-order-btn-view:hover {
  background: var(--z-accent);
  color: white;
}

.z-order-btn-view:active {
  transform: scale(0.98);
}

@media (max-width: 640px) {
  .z-progress-steps {
    font-size: 10px;
  }

  .z-step-icon {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .z-progress-step {
    gap: 4px;
  }
}
</style>