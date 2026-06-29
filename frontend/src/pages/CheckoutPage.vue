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
              <label class="z-payment-option" :class="{ active: form.hinhThuc === 'VNPAY' }">
                <input type="radio" v-model="form.hinhThuc" value="VNPAY" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#0066CC"/>
                      <text x="20" y="25" text-anchor="middle" fill="white" font-size="11" font-weight="700">VN</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Chuyển khoản ngân hàng</strong>
                    <p>Quét mã QR qua app ngân hàng (Vietcombank)</p>
                  </div>
                </div>
              </label>

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
                    <p>Quét mã QR qua ứng dụng MoMo</p>
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

            <div class="z-order-totals">
              <div class="z-order-row">
                <span>Tạm tính</span>
                <span>{{ formatPrice(subtotal) }}</span>
              </div>
              <div class="z-order-row">
                <span>Phí vận chuyển</span>
                <span style="color:var(--z-accent)">Miễn phí</span>
              </div>
              <div class="z-order-row z-order-total">
                <span>Tổng cộng</span>
                <span>{{ formatPrice(subtotal) }}</span>
              </div>
            </div>

            <button class="lm-btn-primary w-100 mt-4" @click="placeOrder" :disabled="loading">
              <span v-if="loading">
                <i class="bi bi-arrow-repeat z-spin"></i> Đang xử lý...
              </span>
              <span v-else>
                <i class="bi bi-lock"></i>
                {{ form.hinhThuc === 'COD' ? 'Đặt hàng' : 'Thanh toán ' + formatPrice(subtotal) }}
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
import { ref, onMounted } from 'vue'
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
  hinhThuc: 'VNPAY'
})

onMounted(() => {
  const { isLoggedIn } = useAuth()
  if (!isLoggedIn()) {
    showToast('Vui lòng đăng nhập để thanh toán', 'warning')
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
  if (!form.value.hoTen.trim()) return showToast('Vui lòng nhập họ và tên', 'warning')
  if (!form.value.soDienThoai.trim()) return showToast('Vui lòng nhập số điện thoại', 'warning')
  if (!form.value.diaChi.trim()) return showToast('Vui lòng nhập địa chỉ giao hàng', 'warning')

  loading.value = true
  try {
    const orderData = {
      hoTen: form.value.hoTen,
      soDienThoai: form.value.soDienThoai,
      diaChi: form.value.diaChi,
      ghiChu: form.value.ghiChu,
      hinhThucThanhToan: form.value.hinhThuc,
      items: state.items.map(i => ({ productId: i.id, qty: i.qty }))
    }

    const order = await api().createOrder(orderData)

    // Hiển thị thông báo thành công
    showToast(`✓ Đơn hàng ${order.maHoaDon} được tạo thành công!`, 'success')

    if (form.value.hinhThuc === 'COD') {
      clearCart()
      setTimeout(() => {
        router.push({
          path: '/payment-result',
          query: { status: 'success', orderId: order.maHoaDon, amount: order.tongTien, method: 'COD' }
        })
      }, 1500)
      return
    }

    if (form.value.hinhThuc === 'VNPAY' || form.value.hinhThuc === 'MOMO') {
      clearCart()
      setTimeout(() => {
        router.push({
          path: '/qr-payment',
          query: { method: form.value.hinhThuc, orderId: order.maHoaDon, amount: order.tongTien }
        })
      }, 1500)
      return
    }
  } catch (err) {
    showToast(err.error || 'Đã xảy ra lỗi khi đặt hàng', 'error')
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

.z-order-totals { border-top: 1px solid var(--z-gray-border); padding-top: 16px; }
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