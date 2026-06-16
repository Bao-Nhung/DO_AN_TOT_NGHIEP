<template>
  <div>
    <div class="container py-5" style="min-height: 70vh;">
      <div class="z-result-card">
        <!-- Success -->
        <template v-if="status === 'success'">
          <div class="z-result-icon z-success">
            <i class="bi bi-check-lg"></i>
          </div>
          <h2 class="z-result-title">Đặt hàng thành công!</h2>
          <p class="z-result-desc">
            Cảm ơn bạn đã mua sắm tại Zestia.
            <template v-if="method === 'COD'">
              Đơn hàng sẽ được giao đến bạn sớm nhất. Vui lòng thanh toán khi nhận hàng.
            </template>
            <template v-else-if="method === 'MOMO' || method === 'ZALOPAY' || method === 'VNPAY'">
              Thanh toán thành công! Đơn hàng của bạn đã được <strong>xác nhận</strong> và sẽ sớm được giao.
            </template>
            <template v-else>
              Thanh toán của bạn đã được xử lý thành công.
            </template>
          </p>
        </template>

        <!-- Failed -->
        <template v-else-if="status === 'failed'">
          <div class="z-result-icon z-failed">
            <i class="bi bi-x-lg"></i>
          </div>
          <h2 class="z-result-title">Thanh toán thất bại</h2>
          <p class="z-result-desc">
            Rất tiếc, giao dịch không thành công. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.
          </p>
        </template>

        <!-- Error -->
        <template v-else>
          <div class="z-result-icon z-failed">
            <i class="bi bi-exclamation-triangle"></i>
          </div>
          <h2 class="z-result-title">Đã xảy ra lỗi</h2>
          <p class="z-result-desc">{{ errorMsg }}</p>
        </template>

        <!-- Order details -->
        <div v-if="orderId" class="z-result-details">
          <div class="z-result-row">
            <span>Mã đơn hàng</span>
            <strong>{{ orderId }}</strong>
          </div>
          <div v-if="amount" class="z-result-row">
            <span>Tổng tiền</span>
            <strong>{{ formatAmount }}</strong>
          </div>
          <div v-if="method" class="z-result-row">
            <span>Phương thức</span>
            <strong>{{ methodLabel }}</strong>
          </div>
          <div v-if="txn" class="z-result-row">
            <span>Mã giao dịch</span>
            <strong>{{ txn }}</strong>
          </div>
        </div>

        <div class="z-result-actions">
          <RouterLink to="/" class="lm-btn-primary"><span>Về trang chủ</span></RouterLink>
          <RouterLink to="/collections" class="lm-btn-outline" style="margin-left:12px">
            <span>Tiếp tục mua sắm</span>
          </RouterLink>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()

const status = computed(() => route.query.status || 'error')
const orderId = computed(() => route.query.orderId || '')
const amount = computed(() => route.query.amount || '')
const txn = computed(() => route.query.txn || '')
const method = computed(() => route.query.method || '')
const code = computed(() => route.query.code || '')

const errorMsg = computed(() => {
  if (code.value === 'INVALID_SIGNATURE') return 'Chữ ký giao dịch không hợp lệ.'
  if (code.value === 'ORDER_NOT_FOUND') return 'Không tìm thấy đơn hàng.'
  return 'Đã có lỗi xảy ra trong quá trình xử lý.'
})

const formatAmount = computed(() => {
  const n = Number(amount.value)
  if (!n) return ''
  return n.toLocaleString('vi-VN') + 'đ'
})

const methodLabel = computed(() => {
  const map = {
    VNPAY: 'Thẻ / VNPay',
    MOMO: 'Ví MoMo',
    ZALOPAY: 'Ví ZaloPay',
    COD: 'Thanh toán khi nhận hàng'
  }
  return map[method.value] || method.value
})
</script>

<style scoped>
.z-result-card {
  max-width: 520px;
  margin: 60px auto;
  text-align: center;
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: 16px;
  padding: 48px 36px;
}
.z-result-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 32px;
}
.z-success { background: #E8F5E9; color: #2E7D32; }
.z-failed  { background: #FFEBEE; color: #C62828; }

.z-result-title {
  font-family: var(--z-font-display);
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 12px;
}
.z-result-desc {
  font-size: 14px;
  color: var(--z-gray);
  line-height: 1.6;
  margin-bottom: 28px;
}
.z-result-details {
  text-align: left;
  background: var(--z-bg);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 28px;
}
.z-result-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 8px 0;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-result-row:last-child { border-bottom: none; }
.z-result-row span { color: var(--z-gray); }
.z-result-row strong { color: var(--z-dark); }

.z-result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
.lm-btn-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 28px;
  border: 1.5px solid var(--z-dark);
  background: transparent;
  color: var(--z-dark);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.5px;
  border-radius: 0;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}
.lm-btn-outline:hover {
  background: var(--z-dark);
  color: white;
}
</style>
