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

        <template v-else-if="status === 'verifying'">
          <div class="z-result-icon z-pending">
            <i class="bi bi-arrow-repeat z-result-spin"></i>
          </div>
          <h2 class="z-result-title">Đang xác minh thanh toán</h2>
          <p class="z-result-desc">
            Zestia đang đọc trạng thái đơn hàng từ máy chủ. Vui lòng chờ trong giây lát.
          </p>
        </template>

        <template v-else-if="status === 'pending'">
          <div class="z-result-icon z-pending">
            <i class="bi bi-hourglass-split"></i>
          </div>
          <h2 class="z-result-title">Đang đối soát thanh toán</h2>
          <p class="z-result-desc">
            {{ pendingMessage }}
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

        <div v-if="cartCleanupMessage" class="alert alert-warning py-2 px-3 mb-4" style="font-size:12px">
          {{ cartCleanupMessage }}
        </div>

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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api } from '@/composables/useApi'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'

const ONLINE_METHODS = new Set(['MOMO', 'ZALOPAY'])
const MAX_STATUS_CHECKS = 4
const STATUS_CHECK_DELAY_MS = 2000

const route = useRoute()
const { removePurchasedItems, syncCartNow, awaitCartHydration } = useCart()
const { showToast } = useToast()
const pendingPayment = readPendingPayment()
const verifiedContext = ref(null)
const verifiedOrder = ref(null)
const onlineStatus = ref('verifying')
const verificationError = ref('')
const pendingMessage = ref('Zestia đang kiểm tra trạng thái giao dịch với máy chủ.')
const cartCleanupMessage = ref('')
let verificationCancelled = false

const queryMethod = computed(() => normalizeMethod(route.query.method))
const isOnlineReturn = computed(() => ONLINE_METHODS.has(queryMethod.value))
const status = computed(() => {
  if (isOnlineReturn.value) return onlineStatus.value
  if (queryMethod.value === 'COD' && route.query.status === 'success') return 'success'
  if (queryMethod.value === 'COD' && route.query.status === 'failed') return 'failed'
  return 'error'
})
const orderId = computed(() => isOnlineReturn.value
  ? (verifiedOrder.value?.maHoaDon || verifiedContext.value?.orderCode || '')
  : String(route.query.orderId || ''))
const amount = computed(() => isOnlineReturn.value
  ? (verifiedOrder.value?.tongTien || '')
  : route.query.amount || '')
const txn = computed(() => isOnlineReturn.value ? '' : route.query.txn || '')
const method = computed(() => isOnlineReturn.value
  ? (normalizeMethod(verifiedOrder.value?.hinhThucThanhToan) || queryMethod.value)
  : queryMethod.value)

function copyOrderCode(codeVal) {
  if (!codeVal) return
  navigator.clipboard.writeText(codeVal).catch(() => {})
  showToast(`Đã sao chép mã đơn hàng "${codeVal}"!`)
}

const errorMsg = computed(() => verificationError.value
  || 'Không thể xác minh kết quả thanh toán. Giỏ hàng của bạn không bị thay đổi.')

onMounted(async () => {
  if (isOnlineReturn.value) await verifyOnlinePayment()
})

onBeforeUnmount(() => {
  verificationCancelled = true
})

async function verifyOnlinePayment() {
  if (!pendingMatchesReturn(pendingPayment)) {
    onlineStatus.value = 'error'
    verificationError.value = 'Phiên thanh toán không khớp hoặc đã hết. Vui lòng tra cứu đơn hàng để xem trạng thái chính xác.'
    return
  }

  verifiedContext.value = pendingPayment
  let lastError = null
  for (let attempt = 0; attempt < MAX_STATUS_CHECKS && !verificationCancelled; attempt++) {
    try {
      const order = await api().searchOrder({
        maHoaDon: pendingPayment.orderCode,
        soDienThoai: pendingPayment.phone
      })
      if (verificationCancelled) return
      if (!orderMatchesPending(order, pendingPayment)) {
        onlineStatus.value = 'error'
        verificationError.value = 'Thông tin đơn hàng trả về không khớp phiên thanh toán hiện tại.'
        return
      }

      verifiedOrder.value = order
      lastError = null
      const canonicalStatus = paymentStatusFromOrder(order)
      if (canonicalStatus === 'success') {
        onlineStatus.value = 'success'
        await finishSuccessfulPayment(pendingPayment)
        return
      }
      if (canonicalStatus === 'failed') {
        onlineStatus.value = 'failed'
        clearMatchingPendingPayment(pendingPayment)
        return
      }
      onlineStatus.value = 'pending'
      pendingMessage.value = 'Cổng thanh toán vẫn đang xử lý giao dịch. Zestia sẽ kiểm tra lại trong giây lát.'
    } catch (error) {
      lastError = error
      onlineStatus.value = 'pending'
      pendingMessage.value = 'Chưa kết nối được máy chủ để xác minh. Giỏ hàng vẫn được giữ nguyên.'
    }

    if (attempt < MAX_STATUS_CHECKS - 1) await delay(STATUS_CHECK_DELAY_MS)
  }

  if (!verificationCancelled) {
    onlineStatus.value = 'pending'
    pendingMessage.value = lastError
      ? 'Tạm thời chưa xác minh được giao dịch. Vui lòng tra cứu lại đơn hàng sau ít phút.'
      : 'Giao dịch vẫn đang được xử lý. Vui lòng tra cứu lại đơn hàng sau ít phút.'
  }
}

async function finishSuccessfulPayment(pending) {
  if (pending.origin === 'checkout') {
    const hydrated = await awaitCartHydration()
    if (verificationCancelled) return
    if (!hydrated) {
      cartCleanupMessage.value = 'Thanh toán đã được xác nhận nhưng giỏ hàng chưa thể đồng bộ. Vui lòng tải lại trang khi kết nối ổn định.'
      return
    }
    const purchasedItems = verifiedPurchasedItems(verifiedOrder.value, pending.purchasedItems)
    if (!purchasedItems.length) {
      cartCleanupMessage.value = 'Thanh toán đã được xác nhận nhưng sản phẩm trong phiên thanh toán không khớp đơn hàng. Giỏ hàng được giữ nguyên.'
      return
    }
    await removePurchasedItems(purchasedItems)
    const synced = await syncCartNow()
    if (!synced) {
      cartCleanupMessage.value = 'Thanh toán đã được xác nhận nhưng giỏ hàng chưa thể đồng bộ hoàn tất.'
      return
    }
  }
  clearMatchingPendingPayment(pending)
}

function pendingMatchesReturn(pending) {
  if (!pending || !['checkout', 'repayment'].includes(pending.origin)) return false
  const queryOrderCode = normalizeOrderCode(route.query.orderId)
  const pendingOrderCode = normalizeOrderCode(pending.orderCode)
  const pendingMethod = normalizeMethod(pending.method)
  const pendingOrderId = Number(pending.orderId)
  const pendingAmount = Number(pending.amount)
  return Boolean(queryOrderCode
    && pendingOrderCode
    && normalizePhone(pending.phone)
    && Number.isInteger(pendingOrderId)
    && pendingOrderId > 0
    && Number.isFinite(pendingAmount)
    && pendingAmount > 0
    && queryOrderCode === pendingOrderCode
    && queryMethod.value === pendingMethod
    && ONLINE_METHODS.has(pendingMethod))
}

function orderMatchesPending(order, pending) {
  if (!order) return false
  return Number(order.id) === Number(pending.orderId)
    && Number(order.tongTien) === Number(pending.amount)
    && normalizeOrderCode(order.maHoaDon) === normalizeOrderCode(pending.orderCode)
    && normalizeMethod(order.hinhThucThanhToan) === normalizeMethod(pending.method)
}

function verifiedPurchasedItems(order, pendingItems) {
  const authoritativeItems = normalizePurchasedItems(order?.chiTiets, 'soLuong')
  const storedItems = normalizePurchasedItems(pendingItems, 'qty')
  if (!authoritativeItems.length || JSON.stringify(authoritativeItems) !== JSON.stringify(storedItems)) return []
  return authoritativeItems
}

function normalizePurchasedItems(items, quantityKey) {
  if (!Array.isArray(items) || !items.length) return []
  const quantities = new Map()
  for (const item of items) {
    const variantId = Number(item?.variantId)
    const qty = Number(item?.[quantityKey])
    if (!(variantId > 0) || !(qty > 0)) return []
    quantities.set(variantId, (quantities.get(variantId) || 0) + qty)
  }
  return [...quantities.entries()]
    .sort(([left], [right]) => left - right)
    .map(([variantId, qty]) => ({ variantId, qty }))
}

function paymentStatusFromOrder(order) {
  const onlineResult = normalizeMethod(order.phuongThucThanhToanOnline)
  if (onlineResult === 'FAILED'
      || [5, 7].includes(Number(order.trangThai))
      || order.trangThaiTracking === 'payment_failed') return 'failed'
  if (order.daThanhToan === true
      && onlineResult === normalizeMethod(order.hinhThucThanhToan)) return 'success'
  return 'pending'
}

function clearMatchingPendingPayment(pending) {
  const current = readPendingPayment()
  if (!current
      || normalizeOrderCode(current.orderCode) !== normalizeOrderCode(pending.orderCode)
      || normalizeMethod(current.method) !== normalizeMethod(pending.method)
      || normalizePhone(current.phone) !== normalizePhone(pending.phone)
      || current.origin !== pending.origin) return
  sessionStorage.removeItem('zestia_pending_payment')
  if (pending.origin === 'checkout') sessionStorage.removeItem('zestia_checkout_request')
}

function readPendingPayment() {
  try {
    return JSON.parse(sessionStorage.getItem('zestia_pending_payment') || 'null')
  } catch {
    return null
  }
}

function normalizeMethod(value) {
  return String(value || '').trim().toUpperCase()
}

function normalizeOrderCode(value) {
  return String(value || '').trim().toUpperCase()
}

function normalizePhone(value) {
  const digits = String(value || '').replace(/\D/g, '')
  return digits.startsWith('84') && digits.length >= 11 ? `0${digits.slice(2)}` : digits
}

function delay(milliseconds) {
  return new Promise(resolve => setTimeout(resolve, milliseconds))
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
.z-result-spin { animation: z-result-spin 0.9s linear infinite; }
@keyframes z-result-spin { to { transform: rotate(360deg); } }

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
