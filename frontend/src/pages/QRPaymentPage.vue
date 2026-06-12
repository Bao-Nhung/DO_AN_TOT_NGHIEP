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
        <div class="z-qr-header" :style="{ background: methodColor }">
          <div class="z-qr-method-icon">
            <template v-if="method === 'VNPAY'">
              <svg viewBox="0 0 40 40" width="36" height="36">
                <rect width="40" height="40" rx="8" fill="#fff"/>
                <text x="20" y="25" text-anchor="middle" fill="#0066CC" font-size="11" font-weight="700">VCB</text>
              </svg>
            </template>
            <template v-else>
              <svg viewBox="0 0 40 40" width="36" height="36">
                <rect width="40" height="40" rx="8" fill="#fff"/>
                <text x="20" y="26" text-anchor="middle" fill="#AE2070" font-size="9" font-weight="700">MoMo</text>
              </svg>
            </template>
          </div>
          <div>
            <div class="z-qr-method-label">{{ method === 'VNPAY' ? 'Chuyển khoản Ngân hàng' : 'Chuyển khoản MoMo' }}</div>
            <div class="z-qr-amount">{{ formatPrice(amount) }}</div>
          </div>
        </div>

        <!-- Payment Body -->
        <div class="z-qr-body">
          <!-- VNPay: show QR -->
          <template v-if="method === 'VNPAY'">
            <div class="z-qr-image-wrap">
              <img :src="qrUrl" alt="QR Code thanh toán" class="z-qr-image" @error="qrError = true" />
              <div v-if="qrError" class="z-qr-fallback">
                <i class="bi bi-qr-code" style="font-size:80px;color:var(--z-gray-border)"></i>
                <p style="color:var(--z-gray);font-size:13px;margin-top:8px">Không tải được mã QR</p>
              </div>
            </div>
            <p class="z-qr-instruction">
              Mở ứng dụng <strong>Ngân hàng</strong> → Quét mã QR → Xác nhận thanh toán
            </p>
          </template>

          <!-- MoMo: show transfer info only -->
          <template v-else>
            <div class="z-momo-icon-wrap">
              <svg viewBox="0 0 80 80" width="80" height="80">
                <rect width="80" height="80" rx="20" fill="#AE2070"/>
                <text x="40" y="48" text-anchor="middle" fill="white" font-size="18" font-weight="700">MoMo</text>
              </svg>
            </div>
            <p class="z-qr-instruction">
              Mở ứng dụng <strong>MoMo</strong> → Chuyển tiền → Nhập thông tin bên dưới
            </p>
          </template>

          <!-- Account Info -->
          <div class="z-qr-info">
            <template v-if="method === 'VNPAY'">
              <div class="z-qr-info-row">
                <span>Ngân hàng</span>
                <strong>Vietcombank (VCB)</strong>
              </div>
              <div class="z-qr-info-row">
                <span>Số tài khoản</span>
                <div class="z-qr-copy-group">
                  <strong>9869167207</strong>
                  <button class="z-copy-btn" @click="copyText('9869167207')" title="Sao chép">
                    <i class="bi" :class="copied === '9869167207' ? 'bi-check-lg' : 'bi-clipboard'"></i>
                  </button>
                </div>
              </div>
              <div class="z-qr-info-row">
                <span>Chủ tài khoản</span>
                <strong>NGUYEN TIEN THANH</strong>
              </div>
            </template>
            <template v-else>
              <div class="z-qr-info-row">
                <span>Số điện thoại</span>
                <div class="z-qr-copy-group">
                  <strong>0869167207</strong>
                  <button class="z-copy-btn" @click="copyText('0869167207')" title="Sao chép">
                    <i class="bi" :class="copied === '0869167207' ? 'bi-check-lg' : 'bi-clipboard'"></i>
                  </button>
                </div>
              </div>
              <div class="z-qr-info-row">
                <span>Tên người nhận</span>
                <strong>Nguyễn Tiến Thành</strong>
              </div>
            </template>
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

          <!-- Timer -->
          <div class="z-qr-timer">
            <i class="bi bi-clock"></i>
            Vui lòng thanh toán trong <strong>{{ timerDisplay }}</strong>
          </div>

          <!-- Actions -->
          <div class="z-qr-actions">
            <button class="lm-btn-primary w-100" @click="confirmDone">
              <span><i class="bi bi-check2-circle me-2"></i>Tôi đã thanh toán</span>
            </button>
            <button class="z-qr-cancel" @click="cancelOrder">
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
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const router = useRouter()

const method = computed(() => route.query.method || 'VNPAY')
const orderId = computed(() => route.query.orderId || '')
const amount = computed(() => Number(route.query.amount) || 0)

const qrError = ref(false)
const copied = ref('')
const remainSeconds = ref(15 * 60)
let timer = null

function copyText(text) {
  navigator.clipboard.writeText(text).catch(() => {})
  copied.value = text
  setTimeout(() => { copied.value = '' }, 2000)
}

const transferContent = computed(() => {
  return 'ZESTIA ' + orderId.value
})

const methodColor = computed(() =>
  method.value === 'VNPAY' ? 'linear-gradient(135deg, #0066CC, #004999)' : 'linear-gradient(135deg, #AE2070, #8C1A5A)'
)

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

function confirmDone() {
  router.push({
    path: '/payment-result',
    query: {
      status: 'success',
      orderId: orderId.value,
      amount: amount.value,
      method: method.value
    }
  })
}

function cancelOrder() {
  router.push({
    path: '/payment-result',
    query: {
      status: 'failed',
      orderId: orderId.value,
      method: method.value
    }
  })
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
  margin-bottom: 20px;
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
.z-momo-icon-wrap {
  text-align: center;
  margin-bottom: 20px;
}
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
