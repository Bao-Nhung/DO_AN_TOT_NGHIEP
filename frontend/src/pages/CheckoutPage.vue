<template>
  <div>
    <div class="lm-page-hero" data-title="CHECKOUT">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thanh Toán</p>
        <h1 class="mb-3">Xác nhận <em>đơn hàng</em></h1>
      </div>
    </div>

    <div class="container py-5">
      <!-- Empty cart -->
      <div v-if="!state.items.length" class="text-center py-5">
        <i class="bi bi-cart-x mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
        <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Giỏ hàng trống</h3>
        <p style="color:var(--z-gray);font-size:14px">Hãy thêm sản phẩm vào giỏ trước khi thanh toán.</p>
        <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Mua sắm ngay</span></RouterLink>
      </div>

      <!-- Checkout form -->
      <div v-else class="row g-5">

        <!-- Left: Shipping info -->
        <div class="col-lg-7">
          <div class="z-checkout-section">
            <h3 class="z-checkout-title">
              <i class="bi bi-geo-alt"></i> Thông tin giao hàng
            </h3>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="z-label">Họ và tên *</label>
                <input v-model="form.hoTen" class="lm-input" placeholder="Nguyễn Văn A" />
              </div>
              <div class="col-md-6">
                <label class="z-label">Số điện thoại *</label>
                <input v-model="form.soDienThoai" class="lm-input" placeholder="0901234567" />
              </div>
              <div class="col-12">
                <label class="z-label">Email</label>
                <input v-model="form.email" type="email" class="lm-input" placeholder="email@example.com" />
              </div>
              <div class="col-12">
                <label class="z-label">Địa chỉ giao hàng *</label>
                <input v-model="form.diaChi" class="lm-input" placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành" />
              </div>
              <div class="col-12">
                <label class="z-label">Ghi chú</label>
                <textarea v-model="form.ghiChu" class="lm-input" rows="3"
                          placeholder="Ghi chú cho đơn hàng (không bắt buộc)"></textarea>
              </div>
            </div>
          </div>

          <!-- Payment method -->
          <div class="z-checkout-section mt-4">
            <h3 class="z-checkout-title">
              <i class="bi bi-credit-card"></i> Phương thức thanh toán
            </h3>

            <div class="z-payment-options">
              <label class="z-payment-option" :class="{ active: form.hinhThuc === 'MOMO' }">
                <input type="radio" v-model="form.hinhThuc" value="MOMO" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#AE2070"/>
                      <text x="20" y="26" text-anchor="middle" fill="white" font-size="9" font-weight="700">MoMo</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Ví MoMo</strong>
                    <p>Quét mã QR qua ứng dụng MoMo (thanh toán test)</p>
                  </div>
                </div>
              </label>

              <label class="z-payment-option" :class="{ active: form.hinhThuc === 'ZALOPAY' }">
                <input type="radio" v-model="form.hinhThuc" value="ZALOPAY" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#0068FF"/>
                      <text x="20" y="26" text-anchor="middle" fill="white" font-size="13" font-weight="700">Z</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Ví ZaloPay</strong>
                    <p>Quét mã QR qua ứng dụng ZaloPay (sandbox)</p>
                  </div>
                </div>
              </label>

              <label class="z-payment-option" :class="{ active: form.hinhThuc === 'COD' }">
                <input type="radio" v-model="form.hinhThuc" value="COD" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#4A9D5B"/>
                      <text x="20" y="26" text-anchor="middle" fill="white" font-size="10" font-weight="700">COD</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Thanh toán khi nhận hàng (COD)</strong>
                    <p>Thanh toán bằng tiền mặt khi nhận hàng</p>
                  </div>
                </div>
              </label>
            </div>
          </div>
        </div>

        <!-- Right: Order summary -->
        <div class="col-lg-5">
          <div class="z-order-summary">
            <h3 class="z-checkout-title">
              <i class="bi bi-bag"></i> Đơn hàng ({{ totalCount }} sản phẩm)
            </h3>

            <div class="z-order-items">
              <div v-for="item in state.items" :key="item.id" class="z-order-item">
                <div class="z-order-item-img" :style="{ background: item.bg }">
                  {{ item.letter }}
                </div>
                <div class="z-order-item-info">
                  <div class="z-order-item-name">{{ item.name }}</div>
                  <div class="z-order-item-variant">{{ item.variant || 'Mặc định' }}</div>
                  <div class="z-order-item-qty">x{{ item.qty }}</div>
                </div>
                <div class="z-order-item-price">
                  {{ formatPrice(item.price * item.qty) }}
                </div>
              </div>
            </div>

            <!-- Voucher -->
            <div class="z-voucher-box">
              <div class="z-voucher-input-row">
                <i class="bi bi-ticket-perforated" style="color:var(--z-accent)"></i>
                <input v-model="voucherCode" class="z-voucher-input" placeholder="Nhập mã giảm giá"
                       :disabled="!!appliedVoucher" @keyup.enter="applyVoucher" />
                <button v-if="!appliedVoucher" class="z-voucher-btn" @click="applyVoucher" :disabled="applyingVoucher">
                  {{ applyingVoucher ? '...' : 'Áp dụng' }}
                </button>
                <button v-else class="z-voucher-btn z-voucher-remove" @click="removeVoucher">Bỏ</button>
              </div>
              <div v-if="voucherMsg" class="z-voucher-msg" :class="appliedVoucher ? 'ok' : 'err'">
                <i class="bi" :class="appliedVoucher ? 'bi-check-circle' : 'bi-exclamation-circle'"></i>
                {{ voucherMsg }}
              </div>
              <div class="z-voucher-hint">Mã thử: <strong>ZESTIA10</strong>, <strong>SUMMER20</strong></div>
            </div>

            <div class="z-order-totals">
              <div class="z-order-row">
                <span>Tạm tính</span>
                <span>{{ formatPrice(subtotal) }}</span>
              </div>
              <div class="z-order-row">
                <span>Phí vận chuyển</span>
                <span style="color:var(--z-accent)">Miễn phí</span>
              </div>
              <div v-if="discount > 0" class="z-order-row">
                <span>Giảm giá ({{ appliedVoucher }})</span>
                <span style="color:var(--z-accent)">-{{ formatPrice(discount) }}</span>
              </div>
              <div class="z-order-row z-order-total">
                <span>Tổng cộng</span>
                <span>{{ formatPrice(finalTotal) }}</span>
              </div>
            </div>

            <button class="lm-btn-primary w-100 mt-4" @click="placeOrder" :disabled="loading">
              <span v-if="loading">
                <i class="bi bi-arrow-repeat z-spin"></i> Đang xử lý...
              </span>
              <span v-else>
                <i class="bi bi-lock"></i>
                {{ form.hinhThuc === 'COD' ? 'Đặt hàng' : 'Thanh toán ' + formatPrice(finalTotal) }}
              </span>
            </button>

            <p class="text-center mt-3" style="font-size:12px;color:var(--z-gray)">
              <i class="bi bi-shield-check"></i>
              Thông tin thanh toán được bảo mật 100%
            </p>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const { state, totalCount, subtotal, formatPrice, clearCart } = useCart()
const { showToast } = useToast()
const { getUser } = useAuth()

const loading = ref(false)

const form = ref({
  hoTen: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  ghiChu: '',
  hinhThuc: 'MOMO'
})

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref('')
const discount = ref(0)
const voucherMsg = ref('')
const applyingVoucher = ref(false)

const finalTotal = computed(() => Math.max(0, subtotal.value - discount.value))

async function applyVoucher() {
  if (!voucherCode.value.trim()) return showToast('Vui lòng nhập mã giảm giá')
  applyingVoucher.value = true
  voucherMsg.value = ''
  try {
    const res = await api().applyVoucher(voucherCode.value.trim(), subtotal.value)
    if (res.valid) {
      appliedVoucher.value = res.maGiamGia
      discount.value = Number(res.giamGia) || 0
      voucherMsg.value = `${res.tenGiamGia} — giảm ${formatPrice(discount.value)}`
    } else {
      appliedVoucher.value = ''
      discount.value = 0
      voucherMsg.value = res.message || 'Mã không hợp lệ'
    }
  } catch (e) {
    voucherMsg.value = e.message || 'Không áp dụng được mã'
  } finally {
    applyingVoucher.value = false
  }
}

function removeVoucher() {
  appliedVoucher.value = ''
  discount.value = 0
  voucherCode.value = ''
  voucherMsg.value = ''
}

onMounted(() => {
  const { isLoggedIn } = useAuth()
  if (!isLoggedIn()) {
    showToast('Vui lòng đăng nhập để thanh toán')
    router.push('/login')
    return
  }
  const user = getUser()
  if (user) {
    form.value.hoTen = user.hoVaTen || ''
    form.value.soDienThoai = user.soDienThoai || ''
    form.value.email = user.email || ''
  }
})

async function placeOrder() {
  if (!form.value.hoTen.trim()) return showToast('Vui lòng nhập họ và tên')
  if (!form.value.soDienThoai.trim()) return showToast('Vui lòng nhập số điện thoại')
  if (!form.value.diaChi.trim()) return showToast('Vui lòng nhập địa chỉ giao hàng')

  loading.value = true
  try {
    const orderData = {
      hoTen: form.value.hoTen,
      soDienThoai: form.value.soDienThoai,
      diaChi: form.value.diaChi,
      ghiChu: form.value.ghiChu,
      hinhThucThanhToan: form.value.hinhThuc,
      maGiamGia: appliedVoucher.value || null,
      items: state.items.map(i => ({ productId: i.id, qty: i.qty }))
    }

    const order = await api().createOrder(orderData)

    if (form.value.hinhThuc === 'COD') {
      clearCart()
      router.push({
        path: '/payment-result',
        query: { status: 'success', orderId: order.maHoaDon, amount: order.tongTien, method: 'COD' }
      })
      return
    }

    // MoMo / ZaloPay: gọi cổng sandbox thật -> chuyển sang trang thanh toán của cổng
    if (form.value.hinhThuc === 'MOMO' || form.value.hinhThuc === 'ZALOPAY') {
      const res = form.value.hinhThuc === 'MOMO'
        ? await api().createMomoPayment(order.orderId)
        : await api().createZaloPayment(order.orderId)
      if (res && res.payUrl) {
        clearCart()
        window.location.href = res.payUrl
        return
      }
      showToast(res?.error || 'Không tạo được thanh toán, vui lòng thử lại')
      return
    }
  } catch (err) {
    showToast(err.error || 'Đã xảy ra lỗi khi đặt hàng')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.z-checkout-section {
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 28px;
}
.z-checkout-title {
  font-family: var(--z-font-display);
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.z-checkout-title i { color: var(--z-accent); }
.z-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--z-dark);
  margin-bottom: 6px;
}

.z-payment-options { display: flex; flex-direction: column; gap: 12px; }
.z-payment-option {
  position: relative;
  cursor: pointer;
  border: 1.5px solid var(--z-gray-border);
  border-radius: 10px;
  padding: 16px;
  transition: all 0.2s;
}
.z-payment-option.active {
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
}
.z-payment-option input { position: absolute; opacity: 0; }
.z-payment-option-content {
  display: flex;
  align-items: center;
  gap: 14px;
}
.z-payment-option-icon { flex-shrink: 0; }
.z-payment-option-content strong {
  font-size: 14px;
  color: var(--z-dark);
}
.z-payment-option-content p {
  font-size: 12px;
  color: var(--z-gray);
  margin: 2px 0 0;
}

.z-order-summary {
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 28px;
  position: sticky;
  top: 90px;
}
.z-order-items { margin-bottom: 20px; }
.z-order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-order-item:last-child { border-bottom: none; }
.z-order-item-img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--z-font-display);
  font-size: 20px;
  color: rgba(0,0,0,0.15);
  flex-shrink: 0;
}
.z-order-item-info { flex: 1; min-width: 0; }
.z-order-item-name {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.z-order-item-variant { font-size: 12px; color: var(--z-gray); }
.z-order-item-qty { font-size: 12px; color: var(--z-gray); }
.z-order-item-price { font-size: 13px; font-weight: 600; white-space: nowrap; }

.z-voucher-box {
  border-top: 1px solid var(--z-gray-border);
  padding-top: 16px;
  margin-bottom: 4px;
}
.z-voucher-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1.5px solid var(--z-gray-border);
  border-radius: 10px;
  padding: 8px 12px;
}
.z-voucher-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  background: transparent;
  font-family: var(--z-font-body);
  text-transform: uppercase;
}
.z-voucher-btn {
  border: none;
  background: var(--z-dark);
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--z-font-body);
  white-space: nowrap;
}
.z-voucher-btn:hover { background: var(--z-accent); }
.z-voucher-remove { background: var(--z-gray); }
.z-voucher-msg {
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.z-voucher-msg.ok { color: #2E7D32; }
.z-voucher-msg.err { color: #C62828; }
.z-voucher-hint { font-size: 11px; color: var(--z-gray); margin-top: 6px; }
.z-voucher-hint strong { color: var(--z-accent); }

.z-order-totals { border-top: 1px solid var(--z-gray-border); padding-top: 16px; margin-top: 16px; }
.z-order-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--z-gray);
  margin-bottom: 8px;
}
.z-order-total {
  font-size: 16px;
  font-weight: 600;
  color: var(--z-dark);
  padding-top: 12px;
  border-top: 1px solid var(--z-gray-border);
  margin-top: 8px;
}

@keyframes z-spin-anim {
  to { transform: rotate(360deg); }
}
.z-spin {
  display: inline-block;
  animation: z-spin-anim 0.8s linear infinite;
}
</style>
