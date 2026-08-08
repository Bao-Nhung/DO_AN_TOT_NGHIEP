<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Bán hàng tại quầy</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Tạo đơn hàng trực tiếp cho khách tại cửa hàng</p>
      </div>
    </div>

    <div class="row g-3">
      <!-- Product list -->
      <div class="col-lg-7">
        <div class="z-admin-card mb-3" style="padding:14px 20px">
          <div class="d-flex align-items-center gap-2 mb-2" style="border-bottom: 1px solid var(--z-gray-border); padding-bottom: 8px;">
            <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
            <input v-model="search" class="lm-input" placeholder="Tìm sản phẩm theo tên hoặc mã..." style="border:none;padding:8px 0;box-shadow:none">
          </div>
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <span style="font-size:12px;font-weight:600;color:var(--z-gray);flex-shrink:0">Mục:</span>
            <div class="d-flex gap-2 flex-wrap">
              <button v-for="f in filters" :key="f"
                      class="lm-filter-tag" :class="{ active: activeFilter === f }"
                      style="padding: 4px 12px; font-size: 12px;"
                      @click="activeFilter = f">{{ f }}</button>
            </div>
          </div>
        </div>

        <div class="z-admin-card" style="padding:0;overflow:hidden;max-height:calc(100vh - 320px);overflow-y:auto">
          <button v-for="p in paginatedProducts" :key="p.id" type="button"
               class="d-flex align-items-center gap-3 z-pos-item" :aria-label="`Chọn ${p.name}, mã ${p.code}`" @click="addToCart(p)">
            <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
              <img v-if="p.image" :src="p.image" :alt="p.name" style="width:100%;height:100%;object-fit:cover">
              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                   :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'12px', color:'rgba(255,255,255,0.3)' }">
                {{ p.letter }}
              </div>
            </div>
            <div class="flex-grow-1">
              <div style="font-size:13px;font-weight:500">{{ p.name }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ p.code }} · Tồn: {{ p.stock }}</div>
            </div>
            <div style="font-size:14px;font-weight:600;color:var(--z-accent)">{{ p.priceDisplay }}</div>
            <span class="z-add-btn" aria-hidden="true"><i class="bi bi-plus"></i></span>
          </button>
          <div v-if="filteredProducts.length === 0" class="text-center py-5">
            <i class="bi bi-search" style="font-size:32px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Không tìm thấy sản phẩm</p>
          </div>
          <div v-if="filteredProducts.length" class="d-flex justify-content-between align-items-center flex-wrap gap-2 px-3 py-3" style="border-top:1px solid var(--z-gray-border)">
            <span style="font-size:12px;color:var(--z-gray)">
              {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, filteredProducts.length) }} / {{ filteredProducts.length }}
            </span>
            <PageSizeSelect v-model="itemsPerPage" />
            <div v-if="totalPages > 1" class="d-flex gap-2">
              <button class="lm-btn-secondary" style="padding:6px 10px;font-size:12px;height:auto;border-radius:6px" :disabled="currentPage === 1" @click.stop="currentPage--">Trước</button>
              <button class="lm-btn-secondary" style="padding:6px 10px;font-size:12px;height:auto;border-radius:6px" :disabled="currentPage === totalPages" @click.stop="currentPage++">Sau</button>
            </div>
          </div>
        </div>
      </div>
      <!-- Cart -->
      <div class="col-lg-5">
        <div class="z-admin-card" style="position:sticky;top:100px">
          <h3 class="z-admin-card-title mb-3">
            <i class="bi bi-cart3 me-2"></i>Giỏ hàng
            <span v-if="cart.length" style="font-size:12px;color:var(--z-gray);font-weight:400"> ({{ cart.length }} sản phẩm)</span>
          </h3>
          <div v-if="cart.length" class="z-pos-hold-status mb-3">
            <span>
              <i class="bi bi-shield-check me-1"></i>
              Đã giữ tồn kho
              <template v-if="holdCountdown">· còn {{ holdCountdown }}</template>
            </span>
            <button type="button" :disabled="releasingCart" @click="clearReservedCart">
              {{ releasingCart ? 'Đang xóa...' : 'Xóa giỏ' }}
            </button>
          </div>

          <div v-if="cart.length === 0" class="text-center py-4">
            <i class="bi bi-cart-x" style="font-size:36px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Chưa có sản phẩm nào</p>
          </div>

          <div v-else>
            <div class="d-flex flex-column gap-2 mb-3" style="max-height:300px;overflow-y:auto">
              <div v-for="item in cart" :key="item.variantId"
                   class="d-flex align-items-center gap-3 p-2" style="background:var(--z-bg-alt);border-radius:var(--z-radius)">
                <img v-if="item.image" :src="item.image" :alt="item.name" class="z-pos-cart-image">
                <div class="flex-grow-1">
                  <div style="font-size:13px;font-weight:500">{{ item.name }}</div>
                  <div style="font-size:11px;color:var(--z-gray)">Mã SP: {{ item.code }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">
                    {{ fmtPrice(item.price) }}
                    <span v-if="item.color || item.size" style="color:var(--z-accent);font-weight:500"> | {{ item.color }} - {{ item.size }}</span>
                  </div>
                </div>
                <div class="d-flex align-items-center gap-1">
                  <button
                    type="button"
                    class="z-qty-btn"
                    :disabled="updatingVariantId === item.variantId"
                    :aria-label="item.qty > 1 ? `Giảm số lượng ${item.name}` : `Xóa ${item.name}`"
                    @click="changeCartQuantity(item, item.qty - 1)"
                  >
                    <i class="bi" :class="item.qty > 1 ? 'bi-dash' : 'bi-trash'"></i>
                  </button>
                  <span style="width:28px;text-align:center;font-size:13px;font-weight:600">{{ item.qty }}</span>
                  <button
                    type="button"
                    class="z-qty-btn"
                    :disabled="updatingVariantId === item.variantId"
                    :aria-label="`Tăng số lượng ${item.name}`"
                    @click="item.qty < item.maxQty ? changeCartQuantity(item, item.qty + 1) : showToast('Đạt giới hạn tồn kho!')"
                  >
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
                <div style="font-size:13px;font-weight:600;min-width:80px;text-align:right">{{ fmtPrice(item.price * item.qty) }}</div>
              </div>
            </div>

            <!-- Customer info -->
            <div class="mb-3 pt-3" style="border-top:1px solid var(--z-gray-border)">
              <label class="z-label">Tìm khách hàng</label>
              <div class="z-customer-search">
                <i class="bi bi-search"></i>
                <input v-model="customerSearch" class="lm-input" placeholder="Nhập tên, mã, SĐT hoặc email..." style="font-size:13px;padding:8px 12px 8px 34px">
                <div v-if="customerResults.length" class="z-customer-results">
                  <button v-for="customer in customerResults" :key="customer.id" type="button" @click="selectCustomer(customer)">
                    <strong>{{ customer.hoVaTen }}</strong>
                    <span>{{ customer.soDienThoai }}<template v-if="customer.email"> · {{ customer.email }}</template></span>
                  </button>
                </div>
              </div>
            </div>
            <div class="mb-3">
              <label class="z-label">Số điện thoại *</label>
              <input
                v-model="customerPhone"
                class="lm-input"
                type="text"
                inputmode="numeric"
                autocomplete="tel"
                maxlength="10"
                placeholder="0901234567"
                style="font-size:13px;padding:8px 12px"
                @input="onCustomerPhoneInput"
              >
              <small style="display:block;margin-top:5px;color:var(--z-gray);font-size:10px">Nhập đủ 10 số để hệ thống tự tìm khách đã mua.</small>
            </div>
            <div class="mb-3">
              <label class="z-label">Tên khách hàng *</label>
              <input v-model="customerName" class="lm-input" placeholder="Tên khách hàng" style="font-size:13px;padding:8px 12px">
            </div>
            <div class="mb-3">
              <label class="z-label">Email (không bắt buộc)</label>
              <input v-model="customerEmail" type="email" class="lm-input" placeholder="email@example.com" style="font-size:13px;padding:8px 12px">
            </div>
            <div v-if="selectedCustomerId" class="z-selected-customer mb-3">
              <span><i class="bi bi-person-check me-1"></i>Đã chọn hồ sơ {{ selectedCustomerCode }}</span>
              <button type="button" @click="clearSelectedCustomer">Đổi khách</button>
            </div>
            <div v-else class="mb-3">
              <button type="button" class="lm-btn-secondary w-100" style="height:38px" :disabled="savingCustomer" @click="quickSaveCustomer">
                <i class="bi bi-person-plus"></i>
                {{ savingCustomer ? 'Đang lưu...' : 'Lưu nhanh khách mới' }}
              </button>
            </div>

            <div class="mb-3">
              <label class="z-label">Mã giảm giá</label>
              <div class="d-flex gap-2">
                <input v-model="voucherCode" class="lm-input" placeholder="Nhập mã voucher" style="font-size:13px;padding:8px 12px;text-transform:uppercase"
                       :disabled="!!appliedVoucher" @keyup.enter="applyVoucher">
                <button v-if="!appliedVoucher" class="z-pm-btn" style="flex:0 0 auto;background:var(--z-dark);color:#fff;border-color:var(--z-dark)" @click="applyVoucher">Áp dụng</button>
                <button v-else class="z-pm-btn" style="flex:0 0 auto" @click="removeVoucher">Bỏ</button>
              </div>

              <div v-if="activeVouchers.length" class="mt-2">
                <span style="font-size:11px;color:var(--z-gray)">Voucher hiện có:</span>
                <div class="z-pos-voucher-list mt-1">
                  <button v-for="v in activeVouchers" :key="v.id"
                          class="z-pos-voucher-item"
                          :class="{ disabled: !isVoucherEligible(v), selected: appliedVoucher === v.maGiamGia }"
                          :disabled="!isVoucherEligible(v) || !!appliedVoucher"
                          @click="selectVoucher(v)">
                    <span><strong>{{ v.maGiamGia }}</strong> (-{{ fmtVoucherDiscount(v) }})</span>
                    <span v-if="bestVoucherCode === v.maGiamGia" class="z-best-voucher">Tốt nhất</span>
                    <small>{{ voucherEligibilityText(v) }}</small>
                  </button>
                </div>
              </div>
              <div v-else class="mt-1" style="font-size:11px;color:var(--z-gray)">
                Hiện chưa có voucher đang hoạt động
              </div>

              <div v-if="voucherMsg" :style="{ fontSize:'12px', marginTop:'6px', color: appliedVoucher ? '#2E7D32' : '#C62828' }">{{ voucherMsg }}</div>
            </div>

            <!-- Payment method -->
            <div class="mb-3">
              <label class="z-label">Hình thức thanh toán</label>
              <div class="d-flex gap-2">
                <button v-for="pm in availablePaymentMethods" :key="pm.value"
                        class="z-pm-btn" :class="{ active: paymentMethod === pm.value }"
                        @click="paymentMethod = pm.value">
                  <i class="bi" :class="pm.icon"></i> {{ pm.label }}
                </button>
              </div>
            </div>

            <!-- Cash: tiền khách đưa + tiền thối -->
            <div v-if="paymentMethod === 'cash'" class="z-pay-panel mb-3">
              <label class="z-label">Tiền khách đưa</label>
              <input v-model.number="tienKhachDua" type="number" class="lm-input" placeholder="Nhập số tiền khách đưa" style="font-size:14px;padding:8px 12px">
              <div class="d-flex gap-2 mt-2 flex-wrap">
                <button v-for="q in quickCash" :key="q" class="z-quick-cash" @click="tienKhachDua = q">{{ fmtPrice(q) }}</button>
                <button class="z-quick-cash" @click="tienKhachDua = finalTotal">Đủ tiền</button>
              </div>
              <div class="d-flex justify-content-between align-items-center mt-3 pt-2" style="border-top:1px dashed var(--z-gray-border)">
                <span style="font-size:13px;color:var(--z-gray)">Tiền thối lại</span>
                <strong :style="{ fontSize:'16px', color: change >= 0 ? '#2E7D32' : '#C62828' }">
                  {{ change >= 0 ? fmtPrice(change) : 'Thiếu ' + fmtPrice(-change) }}
                </strong>
              </div>
            </div>

            <!-- Transfer: chọn ví + QR -->
            <div v-else-if="paymentMethod === 'transfer'" class="z-pay-panel mb-3">
              <label class="z-label">Chọn ví / ngân hàng</label>
              <div class="d-flex gap-2 mb-3">
                <button v-for="t in transferMethods" :key="t.value"
                        class="z-pm-btn" :class="{ active: transferMethod === t.value }"
                        @click="transferMethod = t.value">{{ t.label }}</button>
              </div>
              <div class="text-center">
                <div v-if="loadingQr" class="d-flex align-items-center justify-content-center" style="width:200px;height:200px;margin:0 auto;border:1px solid var(--z-gray-border);border-radius:8px;background:var(--z-white)">
                  <div class="spinner-border spinner-border-sm text-secondary"></div>
                </div>
                <img v-else-if="transferQrSrc" :src="transferQrSrc" alt="QR thanh toán"
                     style="width:200px;height:200px;border:1px solid var(--z-gray-border);border-radius:8px;background:#fff">
                <div v-else style="font-size:12px;color:#C62828;padding:20px">Không tạo được mã QR, thử lại</div>
                <div style="font-size:12px;color:var(--z-gray);margin-top:6px">
                  Khách quét mã {{ transferHint }} — {{ fmtPrice(finalTotal) }}
                </div>
              </div>
              <!-- Xác nhận đã nhận tiền -->
              <div class="mt-3 pt-3" style="border-top:1px dashed var(--z-gray-border)">
                <button v-if="!paymentConfirmed" class="z-confirm-btn" @click="paymentConfirmed = true">
                  <i class="bi bi-check2-circle me-1"></i>Đã nhận được tiền chuyển khoản
                </button>
                <div v-else class="z-confirmed">
                  <span><i class="bi bi-check-circle-fill me-1"></i>Đã xác nhận nhận tiền</span>
                  <button class="z-undo" @click="paymentConfirmed = false">Huỷ</button>
                </div>
              </div>
            </div>
            <!-- Note -->
            <div class="mb-3">
              <label class="z-label">Ghi chú</label>
              <input v-model="note" class="lm-input" placeholder="Ghi chú đơn hàng..." style="font-size:13px;padding:8px 12px">
            </div>

            <!-- Totals -->
            <div class="pt-3 mb-3" style="border-top:2px solid var(--z-dark)">
              <div class="d-flex justify-content-between mb-1" style="font-size:13px;color:var(--z-gray)">
                <span>Tạm tính</span><span>{{ fmtPrice(cartTotal) }}</span>
              </div>
              <div v-if="discount > 0" class="d-flex justify-content-between mb-1" style="font-size:13px">
                <span style="color:var(--z-gray)">Giảm giá ({{ appliedVoucher }})</span>
                <span style="color:var(--z-accent)">-{{ fmtPrice(discount) }}</span>
              </div>
              <div class="d-flex justify-content-between align-items-center mt-2">
                <div style="font-size:16px;font-weight:600">Tổng cộng</div>
                <div style="font-size:24px;font-weight:700;color:var(--z-accent)">{{ fmtPrice(finalTotal) }}</div>
              </div>
            </div>

            <button class="lm-btn-primary w-100 justify-content-center" @click="createOrder" :disabled="creating || !canPay">
              <i class="bi bi-check2-circle" style="position:relative;z-index:1"></i>
              <span>{{ creating ? 'Đang tạo...' : 'Thanh toán & Hoàn thành' }}</span>
            </button>
            <div v-if="paymentMethod === 'cash' && tienKhachDua && change < 0" class="text-center mt-2" style="font-size:12px;color:#C62828">
              Khách đưa chưa đủ tiền
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Variant Selection Modal -->
    <div v-if="showVariantModal" class="z-modal-overlay" @click.self="showVariantModal = false" style="z-index: 2000; backdrop-filter: blur(2px);">
      <div class="z-modal" style="max-width: 450px;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h4 class="z-display mb-0" style="font-size:18px; font-weight:600">Chọn biến thể</h4>
          <button type="button" class="z-icon-btn" aria-label="Đóng bảng chọn biến thể" @click="showVariantModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        
        <div v-if="selectedProduct" class="mb-4">
          <h5 style="font-size:15px; font-weight:500; color:var(--z-dark)">{{ selectedProduct.name }}</h5>
          <p style="font-size:12px; color:var(--z-gray)">Mã: {{ selectedProduct.code }}</p>
          
          <!-- Colors -->
          <div class="mb-3">
            <label class="z-label mb-2">Màu sắc *</label>
            <div class="d-flex flex-wrap gap-2">
              <button v-for="c in uniqueColors" :key="c.name"
                      class="lm-btn-secondary d-flex align-items-center gap-2"
                      :style="{
                        padding:'6px 12px', fontSize:'12px', height:'auto',
                        borderColor: selectedColor === c.name ? 'var(--z-accent)' : '',
                        background: selectedColor === c.name ? 'var(--z-accent-soft)' : ''
                      }"
                      @click="selectedColor = c.name">
                <span :style="{ width:'12px', height:'12px', borderRadius:'50%', background: c.hex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                {{ c.name }}
              </button>
            </div>
          </div>
          
          <!-- Sizes -->
          <div class="mb-3">
            <label class="z-label mb-2">Kích thước *</label>
            <div class="d-flex flex-wrap gap-2">
              <button v-for="s in uniqueSizes" :key="s"
                      class="lm-btn-secondary"
                      :style="{
                        padding:'6px 12px', fontSize:'12px', height:'auto',
                        borderColor: selectedSize === s ? 'var(--z-accent)' : '',
                        background: selectedSize === s ? 'var(--z-accent-soft)' : ''
                      }"
                      @click="selectedSize = s">
                {{ s }}
              </button>
            </div>
          </div>
        </div>
        
        <div class="d-flex gap-3">
          <button class="lm-btn-secondary flex-fill" style="height:40px;" @click="showVariantModal = false">Đóng</button>
          <button
            class="lm-btn-primary flex-fill"
            style="height:40px;"
            :disabled="addingVariant"
            @click="confirmAddVariant"
          >
            {{ addingVariant ? 'Đang giữ hàng...' : 'Thêm vào giỏ' }}
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api, useAuth } from '@/composables/useApi'
import { mapProduct, fmtPrice, MOCK_PRODUCTS } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { createVietQrUrl, isVietQrConfigured } from '@/config/paymentConfig'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const { getUser } = useAuth()

const search = ref('')
const activeFilter = ref('Tất cả')
const allProducts = ref([])
const filters = computed(() => [
  'Tất cả',
  ...new Set(allProducts.value.filter(p => p.active).map(p => p.category).filter(Boolean))
])
const vouchersList = ref([])
const cart = ref([])
const reservationToken = ref('')
const reservationExpiresAt = ref(null)
const updatingVariantId = ref(null)
const addingVariant = ref(false)
const releasingCart = ref(false)
const reservationClock = ref(Date.now())
const customerName = ref('')
const customerPhone = ref('')
const customerEmail = ref('')
const customerSearch = ref('')
const customerResults = ref([])
const selectedCustomerId = ref(null)
const selectedCustomerCode = ref('')
const selectedCustomerPhone = ref('')
const savingCustomer = ref(false)
const paymentMethod = ref('cash')
const transferMethod = ref('vietqr')
const tienKhachDua = ref(null)
const note = ref('')
const creating = ref(false)

// Variant Selection Modal State
const showVariantModal = ref(false)
const selectedProduct = ref(null)
const selectedColor = ref(null)
const selectedSize = ref(null)
const productVariants = ref([])

const uniqueColors = computed(() => {
  const colorsMap = {}
  productVariants.value.forEach(v => {
    if (v.mauSac && v.trangThai === 1) {
      colorsMap[v.mauSac] = { name: v.mauSac, hex: v.maHex }
    }
  })
  return Object.values(colorsMap)
})

const uniqueSizes = computed(() => {
  const sizes = new Set()
  productVariants.value.forEach(v => {
    if (v.kichThuoc && v.trangThai === 1) {
      sizes.add(v.kichThuoc)
    }
  })
  return Array.from(sizes)
})

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref('')
const discount = ref(0)
const voucherMsg = ref('')
const bestVoucherCode = ref('')
const autoVoucherDisabled = ref(false)

// QR chuyển khoản
const transferQrSrc = ref('')
const loadingQr = ref(false)
const paymentConfirmed = ref(false)   // nhân viên đã báo "đã nhận tiền"

const paymentMethods = [
  { value: 'cash', label: 'Tiền mặt', icon: 'bi-cash-stack' },
  { value: 'transfer', label: 'Chuyển khoản', icon: 'bi-bank' },
]
const transferMethods = [
  { value: 'vietqr', label: 'VietQR' },
  { value: 'momo', label: 'MoMo' },
  { value: 'zalopay', label: 'ZaloPay' },
]
const availablePaymentMethods = computed(() => paymentMethods)
const quickCash = [100000, 200000, 500000, 1000000]

function reservationStorageKey() {
  const user = getUser()
  return `zestia_pos_reservation_${user?.userId || user?.username || 'staff'}`
}

function clearReservationLocalState() {
  sessionStorage.removeItem(reservationStorageKey())
  reservationToken.value = ''
  reservationExpiresAt.value = null
  cart.value = []
  appliedVoucher.value = ''
  voucherCode.value = ''
  discount.value = 0
  voucherMsg.value = ''
}

function syncReservationState(state, adjustProductStock = true) {
  if (!state || state.status !== 'ACTIVE') {
    clearReservationLocalState()
    return
  }

  const previousByProduct = new Map()
  cart.value.forEach(item => {
    previousByProduct.set(
      item.id,
      (previousByProduct.get(item.id) || 0) + Number(item.qty || 0)
    )
  })

  const nextCart = (state.items || []).map(item => ({
    key: `variant-${item.variantId}`,
    id: item.productId,
    variantId: item.variantId,
    name: item.productName,
    code: item.productCode,
    price: Number(item.unitPrice || 0),
    qty: Number(item.quantity || 0),
    color: item.color,
    size: item.size,
    maxQty: Number(item.maxQuantity || item.quantity || 0),
    image: item.image || null
  }))
  const nextByProduct = new Map()
  nextCart.forEach(item => {
    nextByProduct.set(item.id, (nextByProduct.get(item.id) || 0) + item.qty)
  })
  if (adjustProductStock && allProducts.value.length) {
    allProducts.value = allProducts.value.map(product => {
      const deltaHeld = (nextByProduct.get(product.id) || 0) - (previousByProduct.get(product.id) || 0)
      return deltaHeld
        ? { ...product, stock: Math.max(0, Number(product.stock || 0) - deltaHeld) }
        : product
    })
  }

  reservationToken.value = state.token
  reservationExpiresAt.value = state.expiresAt || null
  sessionStorage.setItem(reservationStorageKey(), state.token)
  cart.value = nextCart
  appliedVoucher.value = state.voucher?.code || ''
  voucherCode.value = state.voucher?.code || ''
  discount.value = Number(state.discount || 0)
  voucherMsg.value = state.voucher
    ? `${state.voucher.name} — đã giữ voucher, giảm ${fmtPrice(discount.value)}`
    : ''
}

async function restoreReservation() {
  const storedToken = sessionStorage.getItem(reservationStorageKey())
  if (!storedToken) return
  try {
    syncReservationState(await api().getPosReservation(storedToken), false)
  } catch {
    clearReservationLocalState()
  }
}

async function loadProducts() {
  try {
    const data = await api().getVay().catch(() => null)
    if (Array.isArray(data) && data.length > 0) {
      const sortedData = [...data].sort((a, b) => {
        const da = a.ngayTao ? new Date(a.ngayTao).getTime() : Number(a.id || 0)
        const db = b.ngayTao ? new Date(b.ngayTao).getTime() : Number(b.id || 0)
        return db - da
      })
      allProducts.value = sortedData.filter(p => p.trangThai === 1).map((p, i) => {
        const m = mapProduct(p, i)
        return { ...m, priceDisplay: fmtPrice(m.price), rawId: p.id }
      })
    } else {
      allProducts.value = MOCK_PRODUCTS.map((p, i) => {
        const m = mapProduct(p, i)
        return { ...m, priceDisplay: fmtPrice(m.price), rawId: p.id }
      })
    }
  } catch (e) {
    allProducts.value = MOCK_PRODUCTS.map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, priceDisplay: fmtPrice(m.price), rawId: p.id }
    })
  }
}

async function loadVouchers() {
  try {
    const vData = await api().getVouchers()
    vouchersList.value = vData || []
  } catch (e) { console.error('Failed to load vouchers:', e) }
}

let reservationClockTimer
onMounted(async () => {
  await restoreReservation()
  await Promise.all([loadProducts(), loadVouchers()])
  if (cart.value.length && !appliedVoucher.value) {
    await applyBestVoucher(true)
  }
  reservationClockTimer = window.setInterval(() => {
    reservationClock.value = Date.now()
  }, 1000)
})

const filteredProducts = computed(() => {
  let result = allProducts.value.filter(p => p.active)

  if (activeFilter.value !== 'Tất cả') {
    result = result.filter(p => {
      const cat = (p.category || p.loaiVay || '').toLowerCase()
      const code = (p.code || p.maVay || '').toUpperCase()
      const filter = activeFilter.value.toLowerCase()

      if (filter.includes('khoác') || filter.includes('blazer')) {
        return cat.includes('khoác') || cat.includes('blazer') || code.startsWith('AKH')
      }
      if (filter.includes('quần') || filter.includes('jeans')) {
        return cat.includes('quần') || cat.includes('jeans') || code.startsWith('QTY') || code.startsWith('QJN')
      }
      if (filter.includes('phụ kiện')) {
        return cat.includes('phụ kiện') || code.startsWith('PKT')
      }
      if (filter.includes('công sở')) {
        return cat.includes('công sở') || code.startsWith('TCS')
      }
      if (filter.includes('dự tiệc')) {
        return cat.includes('dự tiệc') || code.startsWith('DTP')
      }
      if (filter.includes('váy') || filter.includes('đầm')) {
        return cat.includes('váy') || cat.includes('đầm') || code.startsWith('VDH') || code.startsWith('DTP')
      }
      if (filter.includes('áo')) {
        return (cat.includes('áo') && !cat.includes('khoác')) || code.startsWith('ASM')
      }
      return cat.includes(filter)
    })
  }

  if (search.value) {
    const q = search.value.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(q) || (p.code || '').toLowerCase().includes(q)
    )
  }

  return result
})

const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage.value))
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  return filteredProducts.value.slice(start, start + itemsPerPage.value)
})

watch([search, activeFilter, itemsPerPage], () => {
  currentPage.value = 1
})

const cartTotal = computed(() => cart.value.reduce((s, item) => s + item.price * item.qty, 0))
const finalTotal = computed(() => Math.max(0, cartTotal.value - discount.value))
const change = computed(() => (Number(tienKhachDua.value) || 0) - finalTotal.value)
const holdCountdown = computed(() => {
  reservationClock.value
  if (!reservationExpiresAt.value) return ''
  const seconds = Math.max(0, Math.floor((new Date(reservationExpiresAt.value).getTime() - Date.now()) / 1000))
  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`
})
const reservationIsCurrent = computed(() => {
  reservationClock.value
  return !!reservationToken.value
    && !!reservationExpiresAt.value
    && new Date(reservationExpiresAt.value).getTime() > Date.now()
})

const vietqrUrl = computed(() => {
  return createVietQrUrl(finalTotal.value, 'ZESTIA POS')
})

const transferHint = computed(() => {
  if (transferMethod.value === 'momo') return 'bằng app MoMo'
  if (transferMethod.value === 'zalopay') return 'bằng app ZaloPay'
  return 'qua app ngân hàng'
})

function qrImage(content) {
  return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&margin=10&data=${encodeURIComponent(content)}`
}

let qrReqId = 0
async function refreshTransferQr() {
  if (paymentMethod.value !== 'transfer' || finalTotal.value < 1000) { transferQrSrc.value = ''; return }
  if (transferMethod.value === 'vietqr') {
    if (!isVietQrConfigured()) {
      transferQrSrc.value = ''
      showToast('Chưa cấu hình VietQR trong frontend/.env.local')
      return
    }
    transferQrSrc.value = vietqrUrl.value
    return
  }
  // MoMo / ZaloPay: gọi cổng thật lấy nội dung QR
  const myReq = ++qrReqId
  loadingQr.value = true
  transferQrSrc.value = ''
  try {
    const res = transferMethod.value === 'momo'
      ? await api().momoQr(finalTotal.value)
      : await api().zaloQr(finalTotal.value)
    if (myReq !== qrReqId) return // đã có yêu cầu mới hơn
    if (res && res.qrContent) transferQrSrc.value = qrImage(res.qrContent)
    else { transferQrSrc.value = ''; showToast(res?.error || 'Không tạo được mã QR') }
  } catch (e) {
    if (myReq === qrReqId) { transferQrSrc.value = ''; showToast('Lỗi tạo mã QR') }
  } finally {
    if (myReq === qrReqId) loadingQr.value = false
  }
}

watch([paymentMethod, transferMethod, finalTotal], () => {
  paymentConfirmed.value = false   // đổi phương thức/số tiền -> phải xác nhận lại
  refreshTransferQr()
})

const canPay = computed(() => {
  if (cart.value.length === 0 || !reservationIsCurrent.value) return false
  if (customerName.value.trim().length < 2 || !/^0[35789]\d{8}$/.test(customerPhone.value)) return false
  if (customerEmail.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerEmail.value)) return false
  if (paymentMethod.value === 'cash') return change.value >= 0
  // Chuyển khoản / thẻ: nhân viên phải báo đã nhận tiền
  return paymentConfirmed.value
})

async function addToCart(p) {
  try {
    const detail = await api().getVayById(p.rawId || p.id)
    selectedProduct.value = p
    productVariants.value = detail.bienThe || []
    
    // Reset selection
    selectedColor.value = null
    selectedSize.value = null
    showVariantModal.value = true
  } catch (e) {
    showToast('Lỗi khi tải chi tiết sản phẩm: ' + e.message)
  }
}

function playBarcodeBeepSound() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext
    if (!AudioCtx) return
    const ctx = new AudioCtx()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'triangle'
    osc.frequency.setValueAtTime(1046.5, ctx.currentTime)
    gain.gain.setValueAtTime(0.1, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.1)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.1)
  } catch (_) {}
}

async function confirmAddVariant() {
  if (!selectedColor.value) {
    showToast('Vui lòng chọn màu sắc!', 'warning')
    return
  }
  if (!selectedSize.value) {
    showToast('Vui lòng chọn kích thước!', 'warning')
    return
  }
  
  const match = productVariants.value.find(v => 
    v.mauSac === selectedColor.value && 
    v.kichThuoc === selectedSize.value
  )
  
  if (!match) {
    showToast('Biến thể này không khả dụng!', 'error')
    return
  }
  
  if (match.soLuong <= 0) {
    showToast('Biến thể này đã hết hàng!', 'warning')
    return
  }

  playBarcodeBeepSound()
  
  const itemKey = `variant-${match.id}`
  const existing = cart.value.find(c => c.key === itemKey)
  const targetQuantity = (existing?.qty || 0) + 1
  if (existing && targetQuantity > existing.maxQty) {
    showToast('Không thể thêm quá số lượng tồn kho!', 'warning')
    return
  }
  addingVariant.value = true
  try {
    const state = await api().setPosReservationItem(
      reservationToken.value || null,
      match.id,
      targetQuantity
    )
    syncReservationState(state)
    showVariantModal.value = false
    showToast(`Đã giữ ${selectedProduct.value.name} (${selectedColor.value} / ${selectedSize.value})`)
    if (!autoVoucherDisabled.value) await applyBestVoucher(true)
  } catch (error) {
    showToast(error.error || error.message || 'Không thể giữ sản phẩm trong giỏ POS')
  } finally {
    addingVariant.value = false
  }
}

async function changeCartQuantity(item, targetQuantity) {
  if (!item || updatingVariantId.value !== null) return
  updatingVariantId.value = item.variantId
  try {
    const state = await api().setPosReservationItem(
      reservationToken.value,
      item.variantId,
      Math.max(0, targetQuantity)
    )
    const voucherWasRemoved = !!appliedVoucher.value && !state.voucher
    syncReservationState(state)
    if (voucherWasRemoved) {
      autoVoucherDisabled.value = false
      voucherMsg.value = 'Voucher cũ không còn đủ điều kiện'
    }
    if (!autoVoucherDisabled.value && cart.value.length) await applyBestVoucher(true)
  } catch (error) {
    showToast(error.error || error.message || 'Không thể cập nhật giỏ POS')
  } finally {
    updatingVariantId.value = null
  }
}

async function clearReservedCart() {
  if (!reservationToken.value || releasingCart.value) return
  const accepted = await confirmDialog({
    title: 'Xóa giỏ POS',
    message: 'Toàn bộ sản phẩm và voucher đang giữ sẽ được hoàn lại ngay. Bạn muốn tiếp tục?',
    confirmText: 'Xóa giỏ',
    variant: 'danger'
  })
  if (!accepted) return
  releasingCart.value = true
  try {
    await api().releasePosReservation(reservationToken.value)
    clearReservationLocalState()
    autoVoucherDisabled.value = false
    await loadProducts()
    showToast('Đã hoàn tồn kho và xóa giỏ POS')
  } catch (error) {
    showToast(error.error || error.message || 'Không thể xóa giỏ POS')
  } finally {
    releasingCart.value = false
  }
}

const activeVouchers = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  return vouchersList.value.filter(v => {
    if (v.trangThai !== 1) return false
    if (v.soLuong !== null && v.soLuong <= 0) return false
    if (v.ngayBatDau) {
      const startDate = new Date(v.ngayBatDau)
      startDate.setHours(0, 0, 0, 0)
      if (today < startDate) return false
    }
    if (v.ngayKetThuc) {
      const endDate = new Date(v.ngayKetThuc)
      endDate.setHours(0, 0, 0, 0)
      if (today > endDate) return false
    }
    return true
  })
})

function isVoucherEligible(voucher) {
  return cartTotal.value >= Number(voucher.giaTriDonToiThieu || 0)
}

function voucherEligibilityText(voucher) {
  const minimum = Number(voucher.giaTriDonToiThieu || 0)
  if (cartTotal.value < minimum) return `Cần thêm ${fmtPrice(minimum - cartTotal.value)}`
  return `Đơn tối thiểu ${fmtPrice(minimum)}`
}

function selectVoucher(v) {
  if (!isVoucherEligible(v)) return
  voucherCode.value = v.maGiamGia
  applyVoucher()
}

function fmtVoucherDiscount(v) {
  if (v.phanTramGiam && Number(v.phanTramGiam) > 0) {
    return Number(v.phanTramGiam) + '%'
  }
  if (v.gioTriGiam && Number(v.gioTriGiam) > 0) {
    return fmtPrice(v.gioTriGiam)
  }
  return '0đ'
}

async function applyVoucher() {
  const code = voucherCode.value.trim()
  if (!code) return showToast('Vui lòng nhập mã giảm giá')
  if (!reservationToken.value) return showToast('Vui lòng thêm sản phẩm trước khi dùng voucher')
  autoVoucherDisabled.value = true
  voucherMsg.value = ''
  try {
    const state = await api().setPosReservationVoucher(reservationToken.value, code)
    syncReservationState(state, false)
  } catch (error) {
    voucherMsg.value = error.error || error.message || 'Không áp dụng được mã'
  }
}

async function removeVoucher() {
  if (!reservationToken.value) return
  autoVoucherDisabled.value = true
  try {
    syncReservationState(
      await api().clearPosReservationVoucher(reservationToken.value),
      false
    )
    voucherMsg.value = 'Đã hoàn lượt voucher'
  } catch (error) {
    showToast(error.error || error.message || 'Không thể bỏ voucher')
  }
}

async function applyBestVoucher(force = false) {
  if ((!force && autoVoucherDisabled.value) || cartTotal.value <= 0 || !reservationToken.value) return
  try {
    const state = await api().reserveBestPosVoucher(reservationToken.value)
    syncReservationState(state, false)
    bestVoucherCode.value = state.voucher?.code || ''
    if (state.voucher) {
      voucherMsg.value = `${state.voucher.name} — tự động giữ voucher tốt nhất, giảm ${fmtPrice(state.discount)}`
    }
  } catch (error) {
    console.error('Không thể tự chọn voucher:', error)
  }
}

onBeforeUnmount(() => {
  window.clearInterval(reservationClockTimer)
})

function paymentLabel() {
  if (paymentMethod.value === 'cash') return 'Tiền mặt'
  const t = { vietqr: 'Chuyển khoản (VietQR)', momo: 'Chuyển khoản (MoMo)', zalopay: 'Chuyển khoản (ZaloPay)' }
  return t[transferMethod.value] || 'Chuyển khoản'
}

function onCustomerPhoneInput(event) {
  let digits = String(event.target.value || '').replace(/\D/g, '')
  if (digits.startsWith('84') && digits.length >= 11) digits = `0${digits.slice(2)}`
  customerPhone.value = digits.slice(0, 10)
  if (selectedCustomerId.value && customerPhone.value !== selectedCustomerPhone.value) {
    selectedCustomerId.value = null
    selectedCustomerCode.value = ''
  }
}

let customerSearchTimer
watch(customerSearch, value => {
  clearTimeout(customerSearchTimer)
  const query = value.trim()
  if (query.length < 2) {
    customerResults.value = []
    return
  }
  customerSearchTimer = setTimeout(async () => {
    try {
      customerResults.value = await api().searchKhachHang(query) || []
    } catch {
      customerResults.value = []
    }
  }, 250)
})

let customerPhoneTimer
watch(customerPhone, phone => {
  clearTimeout(customerPhoneTimer)
  if (!/^0[35789]\d{8}$/.test(phone) || selectedCustomerId.value) return
  customerPhoneTimer = setTimeout(async () => {
    try {
      const matches = await api().searchKhachHang(phone) || []
      const exact = matches.find(customer => customer.soDienThoai === phone)
      if (exact) selectCustomer(exact)
    } catch { /* giữ dữ liệu nhân viên đang nhập */ }
  }, 250)
})

function selectCustomer(customer) {
  selectedCustomerId.value = customer.id
  selectedCustomerCode.value = customer.maKhachHang || ''
  selectedCustomerPhone.value = customer.soDienThoai || ''
  customerName.value = customer.hoVaTen || ''
  customerPhone.value = customer.soDienThoai || ''
  customerEmail.value = customer.email || ''
  customerSearch.value = ''
  customerResults.value = []
}

function clearSelectedCustomer() {
  selectedCustomerId.value = null
  selectedCustomerCode.value = ''
  selectedCustomerPhone.value = ''
  customerSearch.value = ''
}

async function quickSaveCustomer() {
  if (customerName.value.trim().length < 2) return showToast('Vui lòng nhập tên khách hàng')
  if (!/^0[35789]\d{8}$/.test(customerPhone.value)) return showToast('Vui lòng nhập số điện thoại hợp lệ')
  if (customerEmail.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerEmail.value)) {
    return showToast('Email khách hàng không hợp lệ')
  }
  savingCustomer.value = true
  try {
    const customer = await api().quickCreateKhachHang({
      hoVaTen: customerName.value.trim(),
      soDienThoai: customerPhone.value,
      email: customerEmail.value.trim() || null
    })
    selectCustomer(customer)
    showToast('Đã lưu hồ sơ khách hàng')
  } catch (e) {
    showToast(e.error || 'Không thể lưu khách hàng')
  } finally {
    savingCustomer.value = false
  }
}

async function createOrder() {
  if (!canPay.value) return
  if (customerName.value.trim().length < 2) {
    showToast('Vui lòng nhập tên khách hàng')
    return
  }
  if (!/^0[35789]\d{8}$/.test(customerPhone.value)) {
    showToast('Số điện thoại phải có 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09')
    return
  }
  if (!await confirmDialog({
    title: 'Xác nhận thanh toán POS',
    message: `Hoàn tất đơn tại quầy với tổng tiền ${fmtPrice(finalTotal.value)} bằng ${paymentLabel()}?`,
    confirmText: 'Hoàn tất',
    variant: 'success'
  })) return
  creating.value = true
  try {
    const user = getUser()
    let noteText = `[Tại quầy] ${paymentLabel()}`
    if (paymentMethod.value === 'cash' && tienKhachDua.value) {
      noteText += ` · Khách đưa ${fmtPrice(tienKhachDua.value)}, thối ${fmtPrice(change.value)}`
    }
    if (note.value) noteText += ` · ${note.value}`

    const orderData = {
      items: cart.value.map(item => ({ 
        productId: item.id, 
        variantId: item.variantId,
        qty: item.qty,
        color: item.color,
        size: item.size
      })),
      hinhThucThanhToan: paymentLabel(),
      hinhThucNhanHang: 0,
      trangThai: 4,
      daThanhToan: true,
      tienKhachDua: paymentMethod.value === 'cash' ? Number(tienKhachDua.value) : null,
      maGiamGia: appliedVoucher.value || null,
      nhanVienId: user?.userId || null,
      ghiChu: noteText,
      tenKhachHang: customerName.value.trim(),
      soDienThoai: customerPhone.value,
      email: customerEmail.value.trim() || null,
      customerId: selectedCustomerId.value,
      posReservationToken: reservationToken.value
    }
    await api().createOrder(orderData)
    showToast('Tạo đơn & thanh toán thành công!')
    clearReservationLocalState()
    customerName.value = ''
    customerPhone.value = ''
    customerEmail.value = ''
    selectedCustomerId.value = null
    selectedCustomerCode.value = ''
    selectedCustomerPhone.value = ''
    note.value = ''
    tienKhachDua.value = null
    paymentConfirmed.value = false
    autoVoucherDisabled.value = false
    bestVoucherCode.value = ''
    await loadProducts()
  } catch (e) {
    showToast('Lỗi: ' + (e.error || e.message || 'Không thể tạo đơn'))
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.z-pos-cart-image { width: 42px; height: 50px; flex: 0 0 42px; object-fit: cover; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-pos-hold-status {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 8px 10px; border: 1px solid #bbf7d0; background: #f0fdf4;
  color: #166534; font-size: 11px;
}
.z-pos-hold-status button {
  border: 0; padding: 0; background: transparent; color: #991b1b;
  font-size: 11px; font-weight: 600;
}
.z-pos-hold-status button:disabled { opacity: .55; }
.z-customer-search { position: relative; }
.z-customer-search > i { position: absolute; z-index: 2; left: 12px; top: 10px; color: var(--z-gray); }
.z-customer-results {
  position: absolute; z-index: 20; top: calc(100% + 4px); left: 0; right: 0;
  max-height: 210px; overflow-y: auto; border: 1px solid var(--z-gray-border);
  background: var(--z-white); box-shadow: 0 10px 28px rgba(0,0,0,.12);
}
.z-customer-results button {
  width: 100%; display: flex; flex-direction: column; gap: 2px; padding: 10px 12px;
  border: 0; border-bottom: 1px solid var(--z-gray-border); background: var(--z-white);
  color: var(--z-dark); text-align: left; font-size: 12px;
}
.z-customer-results button:hover { background: var(--z-accent-soft); }
.z-customer-results span { color: var(--z-gray); font-size: 11px; }
.z-selected-customer {
  display: flex; justify-content: space-between; align-items: center; gap: 8px;
  padding: 9px 11px; background: #dcfce7; color: #166534; font-size: 12px;
}
.z-selected-customer button { border: 0; background: transparent; color: #166534; text-decoration: underline; }
.z-pos-item {
  width: 100%; border: 0; background: transparent; color: inherit;
  font: inherit; text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid var(--z-gray-border);
  cursor: pointer; transition: all 0.15s;
}
.z-pos-item:hover { background: var(--z-accent-soft); }
.z-pos-item:hover .z-add-btn { background: var(--z-accent); color: var(--z-white); }
.z-pos-item:last-child { border-bottom: none; }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000; padding: 20px; backdrop-filter: blur(2px);
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-add-btn {
  width: 32px; height: 32px; border: 1px solid var(--z-accent);
  background: var(--z-white); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-accent); font-size: 14px;
  transition: all 0.2s; flex-shrink: 0;
}
.z-add-btn:hover { background: var(--z-accent); color: var(--z-white); }
.z-qty-btn {
  width: 26px; height: 26px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 12px; color: var(--z-gray);
  transition: all 0.2s;
}
.z-qty-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-pm-btn {
  flex: 1; padding: 8px 12px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  font-size: 12px; font-weight: 500; color: var(--z-gray);
  cursor: pointer; transition: all 0.2s; text-align: center;
  font-family: var(--z-font-body);
  display: flex; align-items: center; justify-content: center; gap: 4px;
}
.z-pm-btn:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-pm-btn.active { background: var(--z-dark); color: var(--z-white); border-color: var(--z-dark); }
.z-label { display: block; font-size: 12px; font-weight: 500; color: var(--z-gray); margin-bottom: 6px; }
.z-pay-panel {
  background: var(--z-bg-alt); border-radius: var(--z-radius); padding: 14px 16px;
}
.z-quick-cash {
  border: 1px solid var(--z-gray-border); background: var(--z-white);
  border-radius: var(--z-radius); font-size: 12px; padding: 5px 10px;
  cursor: pointer; color: var(--z-dark); font-family: var(--z-font-body);
  transition: all 0.15s;
}
.z-quick-cash:hover { border-color: var(--z-accent); color: var(--z-accent); }
.z-confirm-btn {
  width: 100%; padding: 9px 12px; border: 1.5px solid #16a34a; background: var(--z-white);
  border-radius: var(--z-radius); font-size: 13px; font-weight: 600; color: #16a34a;
  cursor: pointer; transition: all 0.15s; font-family: var(--z-font-body);
}
.z-confirm-btn:hover { background: #dcfce7; color: #166534; }
.z-confirm-btn:disabled { opacity: 0.6; cursor: default; }
.z-confirmed {
  display: flex; align-items: center; justify-content: space-between;
  background: #dcfce7; color: #16a34a; border-radius: var(--z-radius);
  padding: 9px 12px; font-size: 13px; font-weight: 600;
}
.z-undo {
  border: none; background: transparent; color: var(--z-gray); font-size: 12px;
  cursor: pointer; text-decoration: underline; font-family: var(--z-font-body);
}
.z-test-card {
  background: var(--z-white); border: 1px dashed var(--z-gray-border);
  border-radius: var(--z-radius); padding: 10px 12px;
}
.z-test-card-row {
  display: flex; justify-content: space-between; font-size: 12px; padding: 4px 0;
}
.z-test-card-row span { color: var(--z-gray); }
.z-test-card-row strong { color: var(--z-dark); font-family: var(--z-font-body); font-variant-numeric: tabular-nums; }
.z-pos-voucher-list {
  display: grid;
  gap: 6px;
  max-height: 190px;
  overflow-y: auto;
  padding-right: 4px;
}
.z-pos-voucher-item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 2px 8px;
  width: 100%;
  padding: 7px 9px;
  border: 1px dashed #16a34a;
  border-radius: var(--z-radius);
  background: #f0fdf4;
  color: #166534;
  text-align: left;
  font-size: 11px;
  cursor: pointer;
}
.z-pos-voucher-item small { grid-column: 1 / -1; color: var(--z-gray); }
.z-pos-voucher-item.disabled { border-color: var(--z-gray-border); background: var(--z-bg-alt); color: var(--z-gray); opacity: .7; cursor: not-allowed; }
.z-pos-voucher-item.selected { border-style: solid; box-shadow: inset 0 0 0 1px #16a34a; }
.z-best-voucher { align-self: start; border-radius: 10px; background: #dcfce7; color: #166534; padding: 2px 7px; font-size: 9px; font-weight: 700; }
</style>



