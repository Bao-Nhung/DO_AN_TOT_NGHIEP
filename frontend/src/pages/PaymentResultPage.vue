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
            <template v-else-if="method === 'MOMO' || method === 'ZALOPAY'">
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

        <template v-else-if="status === 'pending'">
          <div class="z-result-icon z-pending">
            <i class="bi bi-hourglass-split"></i>
          </div>
          <h2 class="z-result-title">Đang đối soát thanh toán</h2>
          <p class="z-result-desc">
            Zestia chưa nhận được kết quả có chữ ký hợp lệ từ cổng thanh toán. Đơn hàng chưa được xác nhận và giỏ hàng vẫn được giữ nguyên.
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
            <div class="d-flex align-items-center gap-2">
              <strong>{{ orderId }}</strong>
              <button type="button" class="btn btn-sm btn-light border py-0 px-2 d-inline-flex align-items-center gap-1" style="font-size:11px" title="Sao chép mã đơn hàng" @click="copyOrderCode(orderId)">
                <i class="bi bi-clipboard"></i> Copy
              </button>
            </div>
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

        <!-- Banner Vòng quay may mắn -->
        <div v-if="status === 'success' && (Number(amount) >= 1000000 || !amount)" class="z-lucky-eligibility-banner mb-4 text-start">
          <div class="d-flex align-items-center gap-3">
            <div class="z-lucky-eligibility-icon"><i class="bi bi-gift" aria-hidden="true"></i></div>
            <div class="flex-grow-1">
              <div style="font-weight: 600; font-size: 14px; color: #c92a2a;">Đơn hàng đủ điều kiện Quay Thưởng!</div>
              <div style="font-size: 12px; color: #495057;">Đơn từ 1.000.000đ được nhận 1 lượt Vòng Quay May Mắn trúng quà 100%.</div>
            </div>
            <RouterLink :to="`/lucky-wheel?orderCode=${orderId}`" class="btn btn-sm btn-danger px-3 py-2 fw-bold text-nowrap shadow-sm">
              Quay Ngay <i class="bi bi-arrow-right ms-1"></i>
            </RouterLink>
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
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const { clearCart, syncCartNow } = useCart()
const { showToast } = useToast()

function copyOrderCode(codeVal) {
  if (!codeVal) return
  navigator.clipboard.writeText(codeVal)
  showToast(`Đã sao chép mã đơn hàng "${codeVal}"!`)
}

const status = computed(() => route.query.status || 'error')
const orderId = computed(() => route.query.orderId || '')
const amount = computed(() => route.query.amount || '')
const txn = computed(() => route.query.txn || '')
const method = computed(() => route.query.method || '')
const code = computed(() => route.query.code || '')

const errorMsg = computed(() => {
  if (code.value === 'INVALID_SIGNATURE') return 'Chữ ký giao dịch không hợp lệ.'
  if (code.value === 'UNVERIFIED_RETURN') return 'Chưa xác minh được chữ ký phản hồi của cổng thanh toán.'
  if (code.value === 'PAYMENT_PENDING') return 'Cổng thanh toán vẫn đang xử lý giao dịch.'
  if (code.value === 'ORDER_NOT_FOUND') return 'Không tìm thấy đơn hàng.'
  return 'Đã có lỗi xảy ra trong quá trình xử lý.'
})

onMounted(async () => {
  if (status.value === 'failed') {
    sessionStorage.removeItem('zestia_pending_payment')
    sessionStorage.removeItem('zestia_checkout_request')
    return
  }
  if (status.value !== 'success') return
  const pending = readPendingPayment()
  if (!pending || !orderId.value || pending.orderCode === orderId.value) {
    clearCart()
    await syncCartNow()
    sessionStorage.removeItem('zestia_pending_payment')
    sessionStorage.removeItem('zestia_checkout_request')
  }
})

function readPendingPayment() {
  try {
    return JSON.parse(sessionStorage.getItem('zestia_pending_payment') || 'null')
  } catch {
    return null
  }
}

const formatAmount = computed(() => {
  const n = Number(amount.value)
  if (!n) return ''
  return n.toLocaleString('vi-VN') + 'đ'
})

const methodLabel = computed(() => {
  const map = {
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
  border-radius: var(--z-radius-lg);
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
.z-pending { background: #FFF7E0; color: #B26A00; }

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
  border-radius: var(--z-radius);
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
  letter-spacing: 0;
  border-radius: var(--z-radius);
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}
.lm-btn-outline:hover {
  background: var(--z-dark);
  color: white;
}
</style>
