<template>
  <div>
    <div class="lm-page-hero" data-title="CHECKOUT">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thanh Toán</p>
        <h1 class="mb-3">Xác nhận <em>đơn hàng</em></h1>
      </div>
    </div>

    <div class="container py-5">
      <div v-if="!state.items.length" class="text-center py-5">
        <i class="bi bi-cart-x mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
        <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Giỏ hàng trống</h3>
        <p style="color:var(--z-gray);font-size:14px">Hãy thêm sản phẩm vào giỏ trước khi thanh toán.</p>
        <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Mua sắm ngay</span></RouterLink>
      </div>

      <div v-else class="row g-5">

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
              
              <div class="col-md-4">
                <label class="z-label">Tỉnh / Thành phố *</label>
                <select v-model="selectedCity" class="lm-input" @change="onCityChange">
                  <option value="">Chọn Tỉnh/Thành</option>
                  <option v-for="c in addressData" :key="c.code" :value="c.code">{{ c.name }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="z-label">Quận / Huyện *</label>
                <select v-model="selectedDistrict" class="lm-input" :disabled="!selectedCity" @change="onDistrictChange">
                  <option value="">Chọn Quận/Huyện</option>
                  <option v-for="d in availableDistricts" :key="d.code" :value="d.code">{{ d.name }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="z-label">Phường / Xã *</label>
                <select v-model="selectedWard" class="lm-input" :disabled="!selectedDistrict">
                  <option value="">Chọn Phường/Xã</option>
                  <option v-for="w in availableWards" :key="w.code" :value="w.code">{{ w.name }}</option>
                </select>
              </div>

              <div class="col-12">
                <label class="z-label">Địa chỉ cụ thể *</label>
                <input v-model="specificAddress" class="lm-input" placeholder="Số nhà, tên đường, ngõ ngách..." />
              </div>

              <div class="col-12">
                <label class="z-label">Ghi chú</label>
                <textarea v-model="form.ghiChu" class="lm-input" rows="3"
                          placeholder="Ghi chú cho đơn hàng (không bắt buộc)"></textarea>
              </div>
            </div>
          </div>

          <div class="z-checkout-section mt-4">
            <h3 class="z-checkout-title">
              <i class="bi bi-credit-card"></i> Phương thức thanh toán (Chỉ COD mới được xin hủy đơn)
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
                  <div class="z-order-item-variant">
                    <span v-if="item.size">Size: {{ item.size }}</span>
                    <span v-if="item.color"> | Màu: {{ item.color }}</span>
                    <span v-if="!item.size && !item.color">{{ item.variant || 'Mặc định' }}</span>
                  </div>
                  <div class="z-order-item-qty">x{{ item.qty }}</div>
                </div>
                <div class="z-order-item-price">
                  {{ formatPrice(item.price * item.qty) }}
                </div>
              </div>
            </div>

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
                <span v-if="shippingFee === 0" style="color:var(--z-accent)">Miễn phí</span>
                <span v-else>+{{ formatPrice(shippingFee) }}</span>
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
  ghiChu: '',
  hinhThuc: 'MOMO'
})

// Variables cho Form Địa Chỉ (Sử dụng API provinces.open-api.vn)
const addressData = ref([])
const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedWard = ref('')
const specificAddress = ref('')

const availableDistricts = computed(() => {
  const city = addressData.value.find(c => c.code === selectedCity.value)
  return city ? city.districts : []
})

const availableWards = computed(() => {
  const district = availableDistricts.value.find(d => d.code === selectedDistrict.value)
  return district ? district.wards : []
})

function onCityChange() {
  selectedDistrict.value = ''
  selectedWard.value = ''
}

function onDistrictChange() {
  selectedWard.value = ''
}

// CẬP NHẬT LOGIC TÍNH PHÍ VẬN CHUYỂN DỰA THEO ĐỊA CHỈ
const shippingFee = computed(() => {
  // Nếu chưa chọn đủ Tỉnh/Quận thì mặc định là 0 để người dùng không bị rối
  if (!selectedCity.value || !selectedDistrict.value) return 0;

  // Mã 01 là Hà Nội, Mã 005 là Cầu Giấy (theo chuẩn API open-api.vn)
  if (selectedCity.value === 1 || selectedCity.value === '01' || selectedCity.value === '1') {
    if (selectedDistrict.value === 5 || selectedDistrict.value === '005' || selectedDistrict.value === '5') {
      return 0; // Trung tâm Cầu Giấy -> Miễn phí
    }
    return 30000; // Các quận huyện khác thuộc Hà Nội -> 30k
  }
  
  // Nếu không phải Hà Nội -> Các tỉnh khác -> 50k
  return 50000; 
})

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref('')
const discount = ref(0)
const voucherMsg = ref('')
const applyingVoucher = ref(false)

// Cập nhật finalTotal cộng thêm phí vận chuyển
const finalTotal = computed(() => Math.max(0, subtotal.value + shippingFee.value - discount.value))

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

onMounted(async () => {
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

  // Tự động load dữ liệu Tỉnh thành VN khi mở trang
  try {
    const res = await fetch('https://provinces.open-api.vn/api/?depth=3')
    addressData.value = await res.json()
  } catch(e) {
    console.error("Lỗi khi tải danh sách tỉnh thành", e)
  }
})

async function placeOrder() {
  if (!form.value.hoTen.trim()) return showToast('Vui lòng nhập họ và tên')
  if (!form.value.soDienThoai.trim()) return showToast('Vui lòng nhập số điện thoại')
  
  // Validate địa chỉ mới
  if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value || !specificAddress.value.trim()) {
    return showToast('Vui lòng chọn và nhập đầy đủ địa chỉ giao hàng')
  }

  loading.value = true
  try {
    // Trích xuất tên Tỉnh, Huyện, Xã từ code đang chọn và ghép thành chuỗi hoàn chỉnh
    const cityName = addressData.value.find(c => c.code === selectedCity.value)?.name || ''
    const districtName = availableDistricts.value.find(d => d.code === selectedDistrict.value)?.name || ''
    const wardName = availableWards.value.find(w => w.code === selectedWard.value)?.name || ''
    
    // Nối thành chuỗi địa chỉ để Backend nhận y như cũ
    const fullAddress = `${specificAddress.value.trim()}, ${wardName}, ${districtName}, ${cityName}`

    const orderData = {
      hoTen: form.value.hoTen,
      soDienThoai: form.value.soDienThoai,
      diaChi: fullAddress, // Gửi chuỗi địa chỉ đã ghép
      ghiChu: form.value.ghiChu,
      hinhThucThanhToan: form.value.hinhThuc,
      maGiamGia: appliedVoucher.value || null,
      phiVanChuyen: shippingFee.value, // CẬP NHẬT GỬI PHÍ SHIP LÊN BACKEND
      items: state.items.map(i => ({ 
        productId: i.productId || parseInt(i.id), // Lấy đúng ID gốc của váy
        qty: i.qty,
        size: i.size,   // Truyền size khách đã chọn
        color: i.color  // Truyền màu khách đã chọn
      }))
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
/* Toàn bộ CSS giữ nguyên để không làm vỡ layout của bạn */
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
