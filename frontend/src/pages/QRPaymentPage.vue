<template>
  <div>
    <div class="lm-page-hero" data-title="THANH TOÁN">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thanh Toán</p>
        <h1 class="mb-3">Quét mã <em>QR</em></h1>
      </div>
    </div>

    <div class="container py-5">
      <div class="z-qr-card">
        <!-- Header -->
        <div class="z-qr-header" :style="{ background: cfg.gradient }">
          <div class="z-qr-method-icon">
            <svg viewBox="0 0 40 40" width="36" height="36">
              <rect width="40" height="40" rx="8" fill="#fff"/>
              <text x="20" y="26" text-anchor="middle" :fill="cfg.color" font-size="10" font-weight="700">{{ cfg.short }}</text>
            </svg>
          </div>
          <div>
            <div class="z-qr-method-label">{{ cfg.label }}</div>
            <div class="z-qr-amount">{{ formatPrice(amount) }}</div>
          </div>
        </div>

        <!-- Payment Body -->
        <div class="z-qr-body">
          <div class="z-qr-image-wrap">
            <img :src="qrUrl" alt="QR Code thanh toán" class="z-qr-image" @error="qrError = true" />
            <div v-if="qrError" class="z-qr-fallback">
              <i class="bi bi-qr-code" style="font-size:80px;color:var(--z-gray-border)"></i>
              <p style="color:var(--z-gray);font-size:13px;margin-top:8px">Không tải được mã QR</p>
            </div>
          </div>
          <p class="z-qr-instruction">
            Mở ứng dụng <strong>{{ cfg.appName }}</strong> → Quét mã QR → Xác nhận thanh toán
          </p>

          <!-- Transfer Info -->
          <div class="z-qr-info">
            <div class="z-qr-info-row">
              <span>{{ method === 'MOMO' ? 'Số điện thoại' : 'Tài khoản' }}</span>
              <div class="z-qr-copy-group">
                <strong>{{ cfg.account }}</strong>
                <button class="z-copy-btn" @click="copyText(cfg.account)" :title="'Sao chép'">
                  <i class="bi" :class="copied === cfg.account ? 'bi-check-lg' : 'bi-clipboard'"></i>
                </button>
              </div>
            </div>
            <div class="z-qr-info-row">
              <span>Người nhận</span>
              <strong>{{ cfg.holder }}</strong>
            </div>
            <div class="z-qr-info-row">
              <span>Số tiền</span>
              <div class="z-qr-copy-group">
                <strong style="color:var(--z-accent)">{{ formatPrice(amount) }}</strong>
                <button class="z-copy-btn" @click="copyText(String(amount))" title="Sao chép">
                  <i class="bi" :class="copied === String(amount) ? 'bi-check-lg' : 'bi-clipboard'"></i>
                </button>
              </div>
            </div>
            <div class="z-qr-info-row">
              <span>Nội dung CK</span>
              <div class="z-qr-copy-group">
                <strong>{{ transferContent }}</strong>
                <button class="z-copy-btn" @click="copyText(transferContent)" title="Sao chép">
                  <i class="bi" :class="copied === transferContent ? 'bi-check-lg' : 'bi-clipboard'"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Sandbox note -->
          <div class="z-qr-sandbox">
            <i class="bi bi-info-circle"></i>
            Đây là thanh toán <strong>test/sandbox</strong>. Nhấn "Tôi đã thanh toán" để mô phỏng giao dịch thành công.
          </div>

          <!-- Timer -->
          <div class="z-qr-timer">
            <i class="bi bi-clock"></i>
            Vui lòng thanh toán trong <strong>{{ timerDisplay }}</strong>
          </div>

          <!-- Actions -->
          <div class="z-qr-actions">
            <button class="lm-btn-primary w-100" @click="confirmDone" :disabled="confirming">
              <span><i class="bi bi-check2-circle me-2"></i>{{ confirming ? 'Đang xác nhận...' : 'Tôi đã thanh toán' }}</span>
            </button>
            <button class="z-qr-cancel" @click="requestCancelOrder">
              Huỷ đơn hàng
            </button>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const router = useRouter()
const { confirmDialog } = useConfirm()

const method = computed(() => route.query.method || 'MOMO')
const orderId = computed(() => route.query.orderId || '')       // id số của hoá đơn
const maHoaDon = computed(() => route.query.maHoaDon || route.query.orderId || '')
const amount = computed(() => Number(route.query.amount) || 0)

const qrError = ref(false)
const copied = ref('')
const confirming = ref(false)
const remainSeconds = ref(15 * 60)
let timer = null

const methodConfig = {
  MOMO: {
    label: 'Ví MoMo (test)', appName: 'MoMo', short: 'MoMo', color: '#AE2070',
    gradient: 'linear-gradient(135deg, #AE2070, #8C1A5A)',
    account: '0869167207', holder: 'NGUYEN TIEN THANH',
  },
  ZALOPAY: {
    label: 'Ví ZaloPay (sandbox)', appName: 'ZaloPay', short: 'Zalo', color: '#0068FF',
    gradient: 'linear-gradient(135deg, #0068FF, #0049B7)',
    account: '0869167207', holder: 'NGUYEN TIEN THANH',
  },
}
const cfg = computed(() => methodConfig[method.value] || methodConfig.MOMO)

function copyText(text) {
  navigator.clipboard.writeText(text).catch(() => {})
  copied.value = text
  setTimeout(() => { copied.value = '' }, 2000)
}

const transferContent = computed(() => 'ZESTIA ' + maHoaDon.value)

const qrUrl = computed(() => {
  const desc = encodeURIComponent(transferContent.value)
  return `https://img.vietqr.io/image/VCB-9869167207-compact2.png?amount=${amount.value}&addInfo=${desc}&accountName=NGUYEN%20TIEN%20THANH`
})

const timerDisplay = computed(() => {
  const m = Math.floor(remainSeconds.value / 60)
  const s = remainSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

async function confirmDone() {
  confirming.value = true
  try {
    await api().confirmPayment(Number(orderId.value) || null, method.value, maHoaDon.value)
    router.push({
      path: '/payment-result',
      query: { status: 'success', orderId: maHoaDon.value, amount: amount.value, method: method.value }
    })
    return
  } catch (e) {
    router.push({
      path: '/payment-result',
      query: { status: 'failed', orderId: maHoaDon.value, method: method.value, reason: 'confirm-denied' }
    })
    return
    // vẫn cho qua trang kết quả ở chế độ demo
  } finally {
    confirming.value = false
  }
}

function cancelOrder() {
  router.push({
    path: '/payment-result',
    query: { status: 'failed', orderId: maHoaDon.value, method: method.value }
  })
}

async function requestCancelOrder() {
  if (!await confirmDialog({
    title: 'Hủy thanh toán',
    message: 'Bạn có chắc muốn hủy phiên thanh toán này?',
    confirmText: 'Hủy thanh toán',
    variant: 'danger'
  })) return
  cancelOrder()
}

onMounted(() => {
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value--
    else {
      clearInterval(timer)
      cancelOrder()
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.z-qr-card {
  max-width: 480px;
  margin: 0 auto;
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: 16px;
  overflow: hidden;
}
.z-qr-header {
  padding: 24px 28px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
}
.z-qr-method-label {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.85;
}
.z-qr-amount {
  font-family: var(--z-font-display);
  font-size: 28px;
  font-weight: 500;
}
.z-qr-body {
  padding: 28px;
}
.z-qr-image-wrap {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}
.z-qr-image {
  width: 240px;
  height: 240px;
  object-fit: contain;
  border-radius: 12px;
  border: 1px solid var(--z-gray-border);
}
.z-qr-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--z-bg);
  border-radius: 12px;
}
.z-qr-instruction {
  text-align: center;
  font-size: 14px;
  color: var(--z-gray);
  margin-bottom: 20px;
  line-height: 1.6;
}
.z-qr-info {
  background: var(--z-bg);
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.z-qr-info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 8px 0;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-qr-info-row:last-child { border-bottom: none; }
.z-qr-info-row span { color: var(--z-gray); }
.z-qr-info-row strong { color: var(--z-dark); font-size: 13px; }
.z-qr-sandbox {
  font-size: 12px;
  color: #1565C0;
  background: #E3F2FD;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
  line-height: 1.5;
}
.z-qr-sandbox i { margin-right: 4px; }
.z-qr-timer {
  text-align: center;
  font-size: 13px;
  color: var(--z-gray);
  margin-bottom: 20px;
  padding: 12px;
  background: #FFF8E1;
  border-radius: 8px;
}
.z-qr-timer i { color: #F9A825; margin-right: 6px; }
.z-qr-timer strong { color: var(--z-accent); }
.z-qr-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}
.z-qr-cancel {
  border: none;
  background: none;
  color: var(--z-gray);
  font-size: 13px;
  cursor: pointer;
  text-decoration: underline;
  font-family: var(--z-font-body);
}
.z-qr-cancel:hover { color: var(--z-accent); }
.z-qr-copy-group {
  display: flex;
  align-items: center;
  gap: 6px;
}
.z-copy-btn {
  border: none;
  background: none;
  color: var(--z-gray);
  cursor: pointer;
  padding: 2px 6px;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.2s;
}
.z-copy-btn:hover { background: var(--z-gray-border); color: var(--z-dark); }
.z-copy-btn .bi-check-lg { color: #2E7D32; }
</style>
