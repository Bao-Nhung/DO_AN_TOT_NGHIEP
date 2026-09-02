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

            <div v-if="savedAddresses.length" class="z-saved-addresses mb-4">
              <div class="z-label mb-2">Địa chỉ đã lưu</div>
              <div class="z-saved-address-list">
                <button
                  v-for="address in savedAddresses"
                  :key="address.id"
                  type="button"
                  class="z-saved-address"
                  :class="{ active: selectedSavedAddressId === address.id }"
                  :aria-pressed="selectedSavedAddressId === address.id"
                  @click="applySavedAddress(address)"
                >
                  <i class="bi" :class="selectedSavedAddressId === address.id ? 'bi-check-circle-fill' : 'bi-geo-alt'"></i>
                  <span>{{ formatSavedAddress(address) }}</span>
                  <small>{{ selectedSavedAddressId === address.id ? 'Đang dùng' : (address.macDinh ? 'Mặc định' : 'Chọn') }}</small>
                </button>
              </div>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="z-label">Họ và tên *</label>
                <input v-model="form.hoTen" class="lm-input" placeholder="Nguyễn Văn A" />
              </div>
              <div class="col-md-6">
                <label class="z-label">Số điện thoại *</label>
                <input
                  v-model="form.soDienThoai"
                  class="lm-input"
                  :class="{ 'z-input-invalid': phoneTouched && phoneError }"
                  type="text"
                  inputmode="numeric"
                  autocomplete="tel"
                  maxlength="10"
                  pattern="0[35789][0-9]{8}"
                  placeholder="0901234567"
                  aria-describedby="checkout-phone-help"
                  @input="onPhoneInput"
                  @blur="phoneTouched = true"
                />
                <small id="checkout-phone-help" class="z-field-help" data-no-i18n
                       :class="{ error: phoneTouched && phoneError }">{{ phoneHelpText }}</small>
              </div>
              <div class="col-12">
                <label class="z-label">Email *</label>
                <input v-model="form.email" type="email" class="lm-input" placeholder="email@example.com"
                       autocomplete="email" required />
              </div>
              
              <div class="col-md-4">
                <label class="z-label">Tỉnh / Thành phố *</label>
                <select v-model="selectedCity" class="lm-input" @change="onCityChange">
                  <option value="">Chọn Tỉnh/Thành</option>
                  <option v-for="c in addressData" :key="c.code" :value="c.code">{{ localizedAdministrativeName(c.name) }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="z-label">Quận / Huyện *</label>
                <select v-model="selectedDistrict" class="lm-input" :disabled="!selectedCity" @change="onDistrictChange">
                  <option value="">Chọn Quận/Huyện</option>
                  <option v-for="d in availableDistricts" :key="d.code" :value="d.code">{{ localizedAdministrativeName(d.name) }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="z-label">Phường / Xã *</label>
                <select v-model="selectedWard" class="lm-input" :disabled="!selectedDistrict" @change="clearSavedAddressSelection">
                  <option value="">Chọn Phường/Xã</option>
                  <option v-for="w in availableWards" :key="w.code" :value="w.code">{{ localizedAdministrativeName(w.name) }}</option>
                </select>
              </div>

              <div class="col-12">
                <label class="z-label">Địa chỉ cụ thể *</label>
                <input v-model="specificAddress" class="lm-input" placeholder="Số nhà, tên đường, ngõ ngách..." @input="clearSavedAddressSelection" />
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
              <label class="z-payment-option"
                     :class="{ active: form.hinhThuc === 'MOMO', disabled: !paymentMethodAvailable('MOMO') }"
                     :aria-disabled="!paymentMethodAvailable('MOMO')">
                <input type="radio" v-model="form.hinhThuc" value="MOMO"
                       :disabled="!paymentMethodAvailable('MOMO')" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#AE2070"/>
                      <text x="20" y="26" text-anchor="middle" fill="white" font-size="9" font-weight="700">MoMo</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Ví MoMo</strong>
                    <p>{{ paymentMethodStatus('MOMO') }}</p>
                  </div>
                </div>
              </label>

              <label class="z-payment-option"
                     :class="{ active: form.hinhThuc === 'ZALOPAY', disabled: !paymentMethodAvailable('ZALOPAY') }"
                     :aria-disabled="!paymentMethodAvailable('ZALOPAY')">
                <input type="radio" v-model="form.hinhThuc" value="ZALOPAY"
                       :disabled="!paymentMethodAvailable('ZALOPAY')" />
                <div class="z-payment-option-content">
                  <div class="z-payment-option-icon">
                    <svg viewBox="0 0 40 40" width="32" height="32">
                      <rect width="40" height="40" rx="8" fill="#0068FF"/>
                      <text x="20" y="26" text-anchor="middle" fill="white" font-size="13" font-weight="700">Z</text>
                    </svg>
                  </div>
                  <div>
                    <strong>Ví ZaloPay</strong>
                    <p>{{ paymentMethodStatus('ZALOPAY') }}</p>
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
            <p v-if="paymentMethodsError" class="z-payment-status-error mb-0 mt-2">
              <i class="bi bi-info-circle me-1"></i>{{ paymentMethodsError }}. COD vẫn khả dụng.
            </p>
          </div>

        </div>

        <div class="col-lg-5">
          <div class="z-order-summary">
            <h3 class="z-checkout-title">
              <i class="bi bi-bag"></i>
              <span data-no-i18n>{{ orderSummaryLabel }}</span>
            </h3>

            <div class="z-order-items">
              <div v-for="item in state.items" :key="item.id" class="z-order-item">
                <div class="z-order-item-img" :style="!item.image ? { background: item.bg } : null">
                  <img v-if="item.image" :src="item.image" :alt="item.name" />
                  <span v-else>{{ item.letter }}</span>
                </div>
                <div class="z-order-item-info">
                  <div class="z-order-item-name">{{ item.name }}</div>
                  <div class="z-order-item-variant" data-no-i18n>{{ orderItemVariantLabel(item) }}</div>
                  <div class="z-order-item-qty">x{{ item.qty }}</div>
                </div>
                <div class="z-order-item-price">
                  <span>{{ formatPrice(Number(item.price) * item.qty) }}</span>
                </div>
              </div>
            </div>

            <!-- Freeship Progress Bar -->
            <div class="mb-4 p-3 rounded border bg-light">
              <div class="d-flex justify-content-between align-items-center mb-1" style="font-size: 13px; font-weight: 500;">
                <span><i class="bi bi-truck text-danger me-1"></i> Tiềm năng ưu đãi Vận chuyển</span>
                <span class="text-danger fw-bold">{{ freeShipProgress >= 100 ? 'Đã đạt Freeship!' : `Thiếu ${formatPrice(300000 - subtotal)}` }}</span>
              </div>
              <div class="progress mb-2" style="height: 6px; background: #e9ecef;">
                <div class="progress-bar bg-danger progress-bar-striped progress-bar-animated" role="progressbar" :style="{ width: freeShipProgress + '%' }"></div>
              </div>
              <div style="font-size: 12px; color: var(--z-gray);">
                <i v-if="freeShipProgress >= 100" class="bi bi-check2-circle text-success me-1" aria-hidden="true"></i>
                {{ freeShipProgress >= 100 ? 'Đơn hàng đã đạt mốc 300.000đ để áp dụng ưu đãi Freeship.' : `Mua thêm ${formatPrice(300000 - subtotal)} để tiết kiệm 30.000đ phí giao hàng.` }}
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
              <!-- Active Vouchers List -->
              <div v-if="activeVouchers.length" class="mt-3">
                <div style="font-size:12px; font-weight:600; color:var(--z-dark); margin-bottom:8px">Voucher hiện có:</div>
                <div class="z-voucher-list">
                  <button v-for="v in activeVouchers" :key="v.id"
                       type="button"
                       class="z-voucher-tag"
                       :class="{ disabled: !voucherEligible(v), selected: appliedVoucher === v.maGiamGia }"
                       :disabled="!voucherEligible(v) || !!appliedVoucher"
                       @click="selectVoucher(v)">
                    <strong style="color:var(--z-accent)">{{ v.maGiamGia }}</strong>:
                    <span data-no-i18n>{{ voucherDiscountLabel(v) }}</span>
                    <span v-if="bestVoucherCode === v.maGiamGia" class="z-best-voucher">Tốt nhất</span>
                    <div style="font-size:9px; color:var(--z-gray); margin-top:2px">{{ voucherEligibilityText(v) }}</div>
                  </button>
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

            <button class="lm-btn-primary w-100 mt-4" @click="handlePlaceOrder" :disabled="loading">
              <span v-if="loading">
                <i class="bi bi-arrow-repeat z-spin"></i> Đang xử lý...
              </span>
              <span v-else data-no-i18n>
                <i class="bi bi-lock"></i>
                {{ checkoutActionLabel }}
              </span>
            </button>

            <p class="text-center mt-3" style="font-size:12px;color:var(--z-gray)">
              <i class="bi bi-shield-check"></i>
              Thanh toán trực tuyến được xử lý qua cổng MoMo hoặc ZaloPay
            </p>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />

    <!-- Confirm Payment Modal -->
    <div v-if="showConfirmModal" class="z-modal-overlay" @click.self="showConfirmModal = false" style="z-index: 2000; backdrop-filter: blur(2px);">
      <div class="z-modal z-payment-confirm-modal" role="dialog" aria-modal="true" aria-labelledby="payment-confirm-title">
        <div class="mb-3" style="font-size:48px; color:var(--z-accent)">
          <i class="bi bi-question-circle"></i>
        </div>
        <h4 id="payment-confirm-title" class="z-display mb-3" style="font-size:18px; font-weight:600">Xác nhận thanh toán</h4>
        <p style="font-size:14px; color:var(--z-gray); margin-bottom:24px">Bạn có chắc chắn muốn thanh toán đơn hàng này?</p>
        <div class="z-payment-confirm-actions">
          <button class="lm-btn-secondary z-payment-confirm-button z-payment-confirm-cancel" @click="showConfirmModal = false">Hủy</button>
          <button class="lm-btn-primary z-payment-confirm-button" @click="confirmAndPlaceOrder" :disabled="loading">
            <span>{{ loading ? 'Đang xử lý...' : 'Xác nhận' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'
import { useI18n } from '@/composables/useI18n'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const { state, totalCount, subtotal, formatPrice, clearCart, refreshItems, syncCartNow } = useCart()
const { showToast } = useToast()
const { getUser } = useAuth()
const { isEn, translateUiText } = useI18n()
const freeShipProgress = computed(() => Math.min(100, Math.floor(((subtotal.value || 0) / 300000) * 100)))
const orderSummaryLabel = computed(() => isEn.value
  ? `Order (${totalCount.value} products)`
  : `Đơn hàng (${totalCount.value} sản phẩm)`)
const checkoutActionLabel = computed(() => {
  if (form.value.hinhThuc === 'COD') return isEn.value ? 'Place order' : 'Đặt hàng'
  return `${isEn.value ? 'Pay' : 'Thanh toán'} ${formatPrice(finalTotal.value)}`
})

const loading = ref(false)
const paymentMethodsLoading = ref(true)
const paymentMethodsError = ref('')
const paymentCapabilities = ref({
  COD: { available: true, sandbox: false },
  MOMO: { available: false, sandbox: false },
  ZALOPAY: { available: false, sandbox: false }
})

const form = ref({
  hoTen: '',
  soDienThoai: '',
  email: '',
  ghiChu: '',
  hinhThuc: 'COD'
})

function paymentMethodAvailable(method) {
  return method === 'COD' || paymentCapabilities.value[method]?.available === true
}

function paymentMethodStatus(method) {
  if (paymentMethodsLoading.value) return 'Đang kiểm tra trạng thái cổng thanh toán...'
  if (!paymentMethodAvailable(method)) return 'Hiện chưa khả dụng — vui lòng chọn phương thức khác'
  return paymentCapabilities.value[method]?.sandbox
    ? 'Sẵn sàng (môi trường sandbox/test)'
    : 'Sẵn sàng thanh toán'
}

async function loadPaymentMethods() {
  paymentMethodsLoading.value = true
  paymentMethodsError.value = ''
  try {
    const response = await api().getPaymentMethods()
    const methods = response?.methods || response || {}
    paymentCapabilities.value = {
      COD: { available: true, sandbox: false },
      MOMO: {
        available: methods.MOMO?.available === true,
        sandbox: methods.MOMO?.sandbox === true
      },
      ZALOPAY: {
        available: methods.ZALOPAY?.available === true,
        sandbox: methods.ZALOPAY?.sandbox === true
      }
    }
    if (!paymentMethodAvailable(form.value.hinhThuc)) form.value.hinhThuc = 'COD'
  } catch (error) {
    paymentMethodsError.value = error?.error || 'Không kiểm tra được trạng thái cổng thanh toán'
    form.value.hinhThuc = 'COD'
  } finally {
    paymentMethodsLoading.value = false
  }
}
const phoneTouched = ref(false)
const phoneError = computed(() => {
  const phone = form.value.soDienThoai
  if (!phone) return 'Vui lòng nhập số điện thoại'
  if (phone.length !== 10) return 'Số điện thoại phải có đúng 10 chữ số'
  if (!/^0[35789]\d{8}$/.test(phone)) return 'Đầu số điện thoại không hợp lệ'
  return ''
})
const phoneHelpText = computed(() => {
  const source = phoneTouched.value && phoneError.value
    ? phoneError.value
    : 'Nhập 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09'
  if (!isEn.value) return source
  return {
    'Vui lòng nhập số điện thoại': 'Please enter a phone number',
    'Số điện thoại phải có đúng 10 chữ số': 'The phone number must contain exactly 10 digits',
    'Đầu số điện thoại không hợp lệ': 'The phone number prefix is invalid',
    'Nhập 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09':
      'Enter 10 digits starting with 03, 05, 07, 08 or 09',
  }[source] || source
})

function localizedAdministrativeName(value) {
  if (!isEn.value) return value
  return String(value || '')
    .replace(/^Thành phố\s+/i, 'City ')
    .replace(/^Tỉnh\s+/i, 'Province ')
    .replace(/^Quận\s+/i, 'District ')
    .replace(/^Huyện\s+/i, 'District ')
    .replace(/^Thị xã\s+/i, 'Town ')
    .replace(/^Phường\s+/i, 'Ward ')
    .replace(/^Xã\s+/i, 'Commune ')
    .replace(/^Thị trấn\s+/i, 'Township ')
}

function orderItemVariantLabel(item) {
  const parts = []
  if (item.size) parts.push(`Size: ${item.size}`)
  if (item.color) {
    const color = translateUiText(item.color, isEn.value ? 'en' : 'vi')
    parts.push(`${isEn.value ? 'Color' : 'Màu'}: ${color}`)
  }
  if (!parts.length) {
    return item.variant || (isEn.value ? 'Default' : 'Mặc định')
  }
  return parts.join(' | ')
}

function voucherDiscountLabel(voucher) {
  if (Number(voucher.phanTramGiam || 0) > 0) {
    return ` ${isEn.value ? 'Save' : 'Giảm'} ${voucher.phanTramGiam}%`
  }
  if (Number(voucher.gioTriGiam || 0) > 0) {
    return ` ${isEn.value ? 'Save' : 'Giảm'} ${formatPrice(voucher.gioTriGiam)}`
  }
  return ''
}

function normalizeVietnamPhoneInput(value) {
  let digits = String(value || '').replace(/\D/g, '')
  if (digits.startsWith('84') && digits.length >= 11) digits = `0${digits.slice(2)}`
  return digits.slice(0, 10)
}

function onPhoneInput(event) {
  phoneTouched.value = true
  form.value.soDienThoai = normalizeVietnamPhoneInput(event.target.value)
}

// Province data is bundled locally so checkout keeps working without internet.
const addressData = ref([])
const savedAddresses = ref([])
const selectedSavedAddressId = ref(null)
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
  clearSavedAddressSelection()
}

function onDistrictChange() {
  selectedWard.value = ''
  clearSavedAddressSelection()
}

function clearSavedAddressSelection() {
  selectedSavedAddressId.value = null
}

function normalizeAdministrativeName(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/đ/g, 'd')
    .replace(/^(thanh pho|tp|tinh|quan|q|huyen|thi xa|phuong|p|xa|thi tran)\.?\s*/i, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

function findAdministrativeUnit(units, savedName) {
  const normalizedName = normalizeAdministrativeName(savedName)
  return (units || []).find(unit => normalizeAdministrativeName(unit.name) === normalizedName)
}

function applySavedAddress(address) {
  if (!address) return
  const city = findAdministrativeUnit(addressData.value, address.tinhThanhPho)
  const district = findAdministrativeUnit(city?.districts, address.quanHuyen)
  const ward = findAdministrativeUnit(district?.wards, address.xaPhuong)

  if (!city || !district || !ward) {
    showToast('Địa chỉ đã lưu chưa khớp dữ liệu địa giới hiện tại. Vui lòng cập nhật lại địa chỉ này.', 'warning')
    return
  }

  selectedCity.value = city.code
  selectedDistrict.value = district.code
  selectedWard.value = ward.code
  specificAddress.value = address.duong || ''
  selectedSavedAddressId.value = address.id
}

function formatSavedAddress(address) {
  return [address.duong, address.xaPhuong, address.quanHuyen, address.tinhThanhPho]
    .filter(Boolean)
    .join(', ')
}

// CẬP NHẬT LOGIC TÍNH PHÍ VẬN CHUYỂN DỰA THEO ĐỊA CHỈ
const shippingFee = computed(() => {
  if (!selectedCity.value || !selectedDistrict.value) return 0
  if (subtotal.value >= 1000000) return 0

  // Mã 01 là Hà Nội, Mã 005 là Cầu Giấy (theo chuẩn API open-api.vn)
  if (selectedCity.value === 1 || selectedCity.value === '01' || selectedCity.value === '1') {
    if (selectedDistrict.value === 5 || selectedDistrict.value === '005' || selectedDistrict.value === '5') {
      return 0
    }
    return 30000
  }
  return 50000
})

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref('')
const discount = ref(0)
const voucherMsg = ref('')
const applyingVoucher = ref(false)
const bestVoucherCode = ref('')
const autoVoucherDisabled = ref(false)
let bestVoucherRequestId = 0
let voucherValidationRequestId = 0

// Cập nhật finalTotal cộng thêm phí vận chuyển
const finalTotal = computed(() => Math.max(0, subtotal.value + shippingFee.value - discount.value))

async function applyVoucher() {
  const code = voucherCode.value.trim()
  if (!code) return showToast('Vui lòng nhập mã giảm giá')
  const amount = subtotal.value
  const requestId = ++voucherValidationRequestId
  autoVoucherDisabled.value = true
  applyingVoucher.value = true
  voucherMsg.value = ''
  try {
    const res = await api().applyVoucher(code, amount)
    if (requestId !== voucherValidationRequestId || amount !== subtotal.value) return
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
    if (requestId !== voucherValidationRequestId) return
    voucherMsg.value = e.message || 'Không áp dụng được mã'
  } finally {
    if (requestId === voucherValidationRequestId) applyingVoucher.value = false
  }
}

function removeVoucher() {
  voucherValidationRequestId += 1
  applyingVoucher.value = false
  autoVoucherDisabled.value = true
  appliedVoucher.value = ''
  discount.value = 0
  voucherCode.value = ''
  voucherMsg.value = ''
}

const vouchersList = ref([])
const activeVouchers = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return vouchersList.value.filter(v => {
    if (v.trangThai !== 1) return false
    if (v.soLuong !== null && v.soLuong <= 0) return false
    if (v.ngayBatDau && v.ngayBatDau > today) return false
    if (v.ngayKetThuc && v.ngayKetThuc < today) return false
    return true
  })
})

function voucherEligible(voucher) {
  return subtotal.value >= Number(voucher.giaTriDonToiThieu || 0)
}

function voucherEligibilityText(voucher) {
  const minimum = Number(voucher.giaTriDonToiThieu || 0)
  if (subtotal.value < minimum) {
    return isEn.value
      ? `Add ${formatPrice(minimum - subtotal.value)} more`
      : `Cần thêm ${formatPrice(minimum - subtotal.value)}`
  }
  return isEn.value
    ? `Minimum order: ${formatPrice(minimum)}`
    : `Đơn tối thiểu: ${formatPrice(minimum)}`
}

function selectVoucher(voucher) {
  if (!voucherEligible(voucher) || appliedVoucher.value) return
  voucherCode.value = voucher.maGiamGia
  applyVoucher()
}

async function applyBestVoucher(force = false) {
  if ((!force && autoVoucherDisabled.value) || subtotal.value <= 0) return
  const amount = subtotal.value
  const requestId = ++bestVoucherRequestId
  try {
    const res = await api().getBestVoucher(amount)
    if (requestId !== bestVoucherRequestId
        || autoVoucherDisabled.value
        || amount !== subtotal.value) return
    if (!res?.valid) {
      bestVoucherCode.value = ''
      voucherCode.value = ''
      appliedVoucher.value = ''
      discount.value = 0
      voucherMsg.value = ''
      return
    }
    bestVoucherCode.value = res.maGiamGia
    voucherCode.value = res.maGiamGia
    appliedVoucher.value = res.maGiamGia
    discount.value = Number(res.giamGia) || 0
    voucherMsg.value = `${res.tenGiamGia} — tự động giảm ${formatPrice(discount.value)}`
  } catch (e) {
    console.error('Không thể tự chọn voucher:', e)
  }
}

let voucherRefreshTimer
let voucherRefreshRequestId = 0
watch(subtotal, () => {
  clearTimeout(voucherRefreshTimer)
  const requestId = ++voucherRefreshRequestId
  voucherRefreshTimer = setTimeout(async () => {
    const amount = subtotal.value
    if (subtotal.value <= 0) {
      removeVoucher()
      autoVoucherDisabled.value = false
      return
    }
    if (autoVoucherDisabled.value && appliedVoucher.value) {
      const validationRequestId = ++voucherValidationRequestId
      try {
        const res = await api().applyVoucher(appliedVoucher.value, amount)
        if (requestId !== voucherRefreshRequestId
            || validationRequestId !== voucherValidationRequestId
            || amount !== subtotal.value) return
        if (!res?.valid) {
          showToast(`Đã tự động gỡ voucher ${appliedVoucher.value} do đơn hàng không còn đủ điều kiện tối thiểu.`, 'warning')
          removeVoucher()
          autoVoucherDisabled.value = false
          await applyBestVoucher(true)
        } else {
          discount.value = Number(res.giamGia) || 0
          voucherMsg.value = `${res.tenGiamGia} — giảm ${formatPrice(discount.value)}`
        }
      } catch (e) {
        if (requestId !== voucherRefreshRequestId
            || validationRequestId !== voucherValidationRequestId) return
        removeVoucher()
      }
    } else {
      voucherValidationRequestId += 1
      await applyBestVoucher()
    }
  }, 150)
})

onBeforeUnmount(() => {
  clearTimeout(voucherRefreshTimer)
  voucherRefreshRequestId += 1
  bestVoucherRequestId += 1
  voucherValidationRequestId += 1
})

const showConfirmModal = ref(false)
const checkoutRequestId = ref('')

function handlePlaceOrder() {
  if (loading.value) return
  if (!paymentMethodAvailable(form.value.hinhThuc)) {
    return showToast('Phương thức thanh toán này hiện chưa khả dụng. Vui lòng chọn phương thức khác')
  }
  if (state.items.some(item => item.unavailable)) return showToast('Vui lòng xóa sản phẩm không còn khả dụng khỏi giỏ hàng')
  if (!state.items.length) return showToast('Giỏ hàng đang trống')
  if (!form.value.hoTen.trim()) return showToast('Vui lòng nhập họ và tên')
  phoneTouched.value = true
  if (phoneError.value) return showToast(phoneError.value)
  if (!isValidEmail(form.value.email)) return showToast('Vui lòng nhập email hợp lệ để nhận hóa đơn')
  
  if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value || !specificAddress.value.trim()) {
    return showToast('Vui lòng chọn và nhập đầy đủ địa chỉ giao hàng')
  }
  
  showConfirmModal.value = true
}

function confirmAndPlaceOrder() {
  if (loading.value) return
  showConfirmModal.value = false
  placeOrder()
}

onMounted(async () => {
  loadPaymentMethods()
  await refreshItems(productId => api().getSanPhamById(productId))

  const { isLoggedIn } = useAuth()
  if (isLoggedIn()) {
    const user = getUser()
    if (user) {
      form.value.hoTen = user.hoVaTen || ''
      form.value.soDienThoai = normalizeVietnamPhoneInput(user.soDienThoai)
      form.value.email = user.email || ''
    }
  }

  // Load the checked-in province snapshot instead of a runtime third-party API.
  try {
    const res = await fetch('/data/vietnam-provinces.json')
    if (!res.ok) throw new Error('Không đọc được dữ liệu địa chỉ')
    addressData.value = await res.json()
    
    if (isLoggedIn()) {
      const addresses = await api().getProfileAddresses()
      savedAddresses.value = Array.isArray(addresses) ? addresses : []
      const preferred = savedAddresses.value.find(address => address.macDinh) || savedAddresses.value[0]
      if (preferred) applySavedAddress(preferred)
    }
  } catch(e) {
    console.error("Lỗi khi tải danh sách tỉnh thành", e)
  }

  // Load active vouchers
  try {
    const list = await api().getVouchers()
    vouchersList.value = Array.isArray(list) ? list : []
    await applyBestVoucher(true)
  } catch (e) {
    console.error("Lỗi khi tải vouchers:", e)
  }
})

async function placeOrder() {
  if (loading.value) return
  if (!paymentMethodAvailable(form.value.hinhThuc)) {
    return showToast('Phương thức thanh toán này hiện chưa khả dụng. Vui lòng chọn phương thức khác')
  }
  if (!form.value.hoTen.trim()) return showToast('Vui lòng nhập họ và tên')
  phoneTouched.value = true
  if (phoneError.value) return showToast(phoneError.value)
  if (!isValidEmail(form.value.email)) return showToast('Vui lòng nhập email hợp lệ để nhận hóa đơn')
  
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
    const fingerprint = JSON.stringify({
      items: state.items.map(i => [Number(i.variantId), Number(i.qty)]).sort((a, b) => a[0] - b[0]),
      method: form.value.hinhThuc,
      phone: form.value.soDienThoai.trim(),
      address: fullAddress,
      voucher: appliedVoucher.value || ''
    })
    const savedRequest = JSON.parse(sessionStorage.getItem('zestia_checkout_request') || 'null')
    if (!savedRequest || savedRequest.fingerprint !== fingerprint) {
      checkoutRequestId.value = window.crypto?.randomUUID?.()
        || `checkout-${Date.now()}-${Math.random().toString(16).slice(2)}`
      sessionStorage.setItem('zestia_checkout_request', JSON.stringify({
        id: checkoutRequestId.value,
        fingerprint
      }))
    } else {
      checkoutRequestId.value = savedRequest.id
    }

    const orderData = {
      hoTen: form.value.hoTen,
      soDienThoai: form.value.soDienThoai,
      email: form.value.email.trim(),
      diaChi: fullAddress, // Gửi chuỗi địa chỉ đã ghép
      ghiChu: form.value.ghiChu,
      hinhThucThanhToan: form.value.hinhThuc,
      maGiamGia: appliedVoucher.value || null,
      checkoutRequestId: checkoutRequestId.value,
      tinhThanhCode: selectedCity.value,
      quanHuyenCode: selectedDistrict.value,
      tinhThanhPho: cityName,
      quanHuyen: districtName,
      xaPhuong: wardName,
      duong: specificAddress.value.trim(),
      items: state.items.map(i => ({ 
        productId: Number(i.productId),
        variantId: i.variantId || null,
        qty: i.qty,
        size: i.size,   // Truyền size khách đã chọn
        color: i.color  // Truyền màu khách đã chọn
      }))
    }

    const order = await api().createOrder(orderData)

    if (form.value.hinhThuc === 'COD') {
      clearCart()
      await syncCartNow()
      sessionStorage.removeItem('zestia_checkout_request')
      sessionStorage.removeItem('zestia_pending_payment')
      router.push({
        path: '/payment-result',
        query: { status: 'success', orderId: order.maHoaDon, amount: order.tongTien, method: 'COD' }
      })
      return
    }

    // MoMo / ZaloPay: gọi cổng sandbox thật -> chuyển sang trang thanh toán của cổng
    if (form.value.hinhThuc === 'MOMO' || form.value.hinhThuc === 'ZALOPAY') {
      const res = form.value.hinhThuc === 'MOMO'
        ? await api().createMomoPayment(order.orderId, order.maHoaDon, form.value.soDienThoai)
        : await api().createZaloPayment(order.orderId, order.maHoaDon, form.value.soDienThoai)
      if (res && res.payUrl) {
        sessionStorage.setItem('zestia_pending_payment', JSON.stringify({
          orderId: order.orderId,
          orderCode: order.maHoaDon,
          method: form.value.hinhThuc,
          amount: order.tongTien,
          origin: 'checkout',
          phone: form.value.soDienThoai,
          purchasedItems: state.items
            .map(item => ({ variantId: Number(item.variantId), qty: Number(item.qty) }))
            .filter(item => item.variantId > 0 && item.qty > 0)
        }))
        window.location.href = res.payUrl
        return
      }
      showToast(res?.error || 'Không tạo được thanh toán, vui lòng thử lại')
      return
    }
  } catch (err) {
    if (err.paymentFailed && (err.maHoaDon || err.orderId)) {
      sessionStorage.removeItem('zestia_checkout_request')
      sessionStorage.removeItem('zestia_pending_payment')
      router.push({
        path: '/payment-result',
        query: {
          status: 'failed',
          orderId: err.maHoaDon || err.orderId,
          amount: err.amount || finalTotal.value,
          method: form.value.hinhThuc
        }
      })
      return
    }
    showToast(err.error || 'Đã xảy ra lỗi khi đặt hàng')
  } finally {
    loading.value = false
  }
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim())
}
</script>

<style scoped>
/* Toàn bộ CSS giữ nguyên để không làm vỡ layout của bạn */
.z-checkout-section {
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
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
.z-input-invalid { border-color: #c62828 !important; }
.z-field-help {
  display: block;
  min-height: 16px;
  margin-top: 5px;
  color: var(--z-gray);
  font-size: 10px;
  line-height: 1.4;
}
.z-field-help.error { color: #c62828; }

.z-payment-options { display: flex; flex-direction: column; gap: 12px; }
.z-payment-option {
  position: relative;
  cursor: pointer;
  border: 1.5px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  padding: 16px;
  transition: var(--z-ease);
}
.z-payment-option:hover { border-color: var(--z-accent-light); }
.z-payment-option.active {
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
}
.z-payment-option.disabled {
  opacity: 0.58;
  cursor: not-allowed;
  background: var(--z-bg);
}
.z-payment-option.disabled:hover { border-color: var(--z-gray-border); }
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
.z-payment-status-error {
  color: #9a6700;
  font-size: 12px;
}

.z-order-summary {
  background: white;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
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
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--z-font-display);
  font-size: 20px;
  color: rgba(0,0,0,0.15);
  flex-shrink: 0;
  overflow: hidden;
}
.z-order-item-img img { width: 100%; height: 100%; object-fit: cover; }
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
.z-order-item-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.z-saved-address-list { display: grid; gap: 8px; }
.z-saved-address {
  width: 100%; display: grid; grid-template-columns: 18px 1fr auto; gap: 8px;
  align-items: start; padding: 11px 12px; border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius); background: var(--z-white); color: var(--z-dark);
  text-align: left; font-size: 12px; cursor: pointer;
}
.z-saved-address:hover, .z-saved-address.active { border-color: var(--z-accent); background: var(--z-accent-soft); }
.z-saved-address i { color: var(--z-accent); }
.z-saved-address small { color: #166534; font-weight: 600; white-space: nowrap; }

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
  border-radius: var(--z-radius);
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
.z-voucher-btn:hover { background: var(--z-accent); color: var(--z-white); }
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
.z-voucher-list {
  display: grid;
  gap: 8px;
  max-height: 210px;
  overflow-y: auto;
  padding-right: 4px;
}
.z-voucher-tag {
  position: relative;
  width: 100%;
  padding: 8px 10px;
  border: 1px dashed var(--z-accent);
  border-radius: 6px;
  background: var(--z-accent-soft);
  color: var(--z-dark);
  text-align: left;
  font-size: 11px;
  cursor: pointer;
}
.z-voucher-tag.disabled { border-color: var(--z-gray-border); background: var(--z-bg-alt); opacity: .65; cursor: not-allowed; }
.z-voucher-tag.selected { border-style: solid; box-shadow: inset 0 0 0 1px var(--z-accent); }
.z-best-voucher { float: right; border-radius: 6px; background: #dcfce7; color: #166534; padding: 2px 7px; font-size: 10px; font-weight: 700; }

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
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-payment-confirm-modal {
  max-width: 400px;
  text-align: center;
}
.z-payment-confirm-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
}
.z-payment-confirm-button {
  width: 100%;
  min-width: 0;
  min-height: 42px;
  justify-content: center;
  padding: 10px 16px;
  line-height: 1.2;
}
.z-payment-confirm-cancel {
  border: 1px solid var(--z-gray-border);
  background: var(--z-white);
}
.z-payment-confirm-cancel:hover:not(:disabled) {
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
</style>
