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
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
            <input v-model="search" class="lm-input" placeholder="Tìm sản phẩm theo tên hoặc mã..." style="border:none;padding:8px 0;box-shadow:none">
          </div>
        </div>

        <div class="z-admin-card" style="padding:0;overflow:hidden;max-height:calc(100vh - 280px);overflow-y:auto">
          <div v-for="p in paginatedProducts" :key="p.id"
               class="d-flex align-items-center gap-3 z-pos-item" @click="addToCart(p)">
            <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
              <img v-if="p.image" :src="p.image" style="width:100%;height:100%;object-fit:cover">
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
            <button class="z-add-btn"><i class="bi bi-plus"></i></button>
          </div>
          <div v-if="filteredProducts.length === 0" class="text-center py-5">
            <i class="bi bi-search" style="font-size:32px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Không tìm thấy sản phẩm</p>
          </div>
          <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center px-3 py-3" style="border-top:1px solid var(--z-gray-border)">
            <span style="font-size:12px;color:var(--z-gray)">
              {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, filteredProducts.length) }} / {{ filteredProducts.length }}
            </span>
            <div class="d-flex gap-2">
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

          <div v-if="cart.length === 0" class="text-center py-4">
            <i class="bi bi-cart-x" style="font-size:36px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Chưa có sản phẩm nào</p>
          </div>

          <div v-else>
            <div class="d-flex flex-column gap-2 mb-3" style="max-height:300px;overflow-y:auto">
              <div v-for="(item, i) in cart" :key="i"
                   class="d-flex align-items-center gap-3 p-2" style="background:var(--z-bg-alt);border-radius:var(--z-radius)">
                <div class="flex-grow-1">
                  <div style="font-size:13px;font-weight:500">{{ item.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">
                    {{ fmtPrice(item.price) }}
                    <span v-if="item.color || item.size" style="color:var(--z-accent);font-weight:500"> | {{ item.color }} - {{ item.size }}</span>
                  </div>
                </div>
                <div class="d-flex align-items-center gap-1">
                  <button class="z-qty-btn" @click="item.qty > 1 ? item.qty-- : removeFromCart(i)">
                    <i class="bi" :class="item.qty > 1 ? 'bi-dash' : 'bi-trash'"></i>
                  </button>
                  <span style="width:28px;text-align:center;font-size:13px;font-weight:600">{{ item.qty }}</span>
                  <button class="z-qty-btn" @click="item.qty < item.maxQty ? item.qty++ : showToast('Đạt giới hạn tồn kho!')">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
                <div style="font-size:13px;font-weight:600;min-width:80px;text-align:right">{{ fmtPrice(item.price * item.qty) }}</div>
              </div>
            </div>

            <!-- Customer info -->
            <div class="mb-3 pt-3" style="border-top:1px solid var(--z-gray-border)">
              <label class="z-label">Khách hàng (tuỳ chọn)</label>
              <input v-model="customerName" class="lm-input" placeholder="Tên khách hàng" style="font-size:13px;padding:8px 12px">
            </div>
            <div class="mb-3">
              <label class="z-label">Số điện thoại (tuỳ chọn)</label>
              <input v-model="customerPhone" class="lm-input" placeholder="Số điện thoại" style="font-size:13px;padding:8px 12px">
            </div>

            <!-- Voucher -->
            <div class="mb-3">
              <label class="z-label">Mã giảm giá</label>
              <div class="d-flex gap-2">
                <input v-model="voucherCode" class="lm-input" placeholder="Nhập mã voucher" style="font-size:13px;padding:8px 12px;text-transform:uppercase"
                       :disabled="!!appliedVoucher" @keyup.enter="applyVoucher">
                <button v-if="!appliedVoucher" class="z-pm-btn" style="flex:0 0 auto;background:var(--z-dark);color:#fff;border-color:var(--z-dark)" @click="applyVoucher">Áp dụng</button>
                <button v-else class="z-pm-btn" style="flex:0 0 auto" @click="removeVoucher">Bỏ</button>
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
          <button class="z-icon-btn" @click="showVariantModal = false"><i class="bi bi-x-lg"></i></button>
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
          <button class="lm-btn-primary flex-fill" style="height:40px;" @click="confirmAddVariant">Thêm vào giỏ</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api, useAuth } from '@/composables/useApi'
import { mapProduct, fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { createVietQrUrl, isVietQrConfigured } from '@/config/paymentConfig'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const { getUser } = useAuth()

const search = ref('')
const allProducts = ref([])
const cart = ref([])
const customerName = ref('')
const customerPhone = ref('')
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

onMounted(async () => {
  try {
    const data = await api().getVay()
    const sortedData = [...data].sort((a, b) => {
      const da = a.ngayTao ? new Date(a.ngayTao).getTime() : Number(a.id || 0)
      const db = b.ngayTao ? new Date(b.ngayTao).getTime() : Number(b.id || 0)
      return db - da
    })
    allProducts.value = sortedData.filter(p => p.trangThai === 1).map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, priceDisplay: fmtPrice(m.salePrice || m.price), rawId: p.id }
    })
  } catch (e) { console.error(e) }
})

const filteredProducts = computed(() => {
  if (!search.value) return allProducts.value
  const q = search.value.toLowerCase()
  return allProducts.value.filter(p =>
    p.name.toLowerCase().includes(q) || (p.code || '').toLowerCase().includes(q)
  )
})

const currentPage = ref(1)
const itemsPerPage = 10
const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage))
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredProducts.value.slice(start, start + itemsPerPage)
})

watch(search, () => {
  currentPage.value = 1
})

const cartTotal = computed(() => cart.value.reduce((s, item) => s + item.price * item.qty, 0))
const finalTotal = computed(() => Math.max(0, cartTotal.value - discount.value))
const change = computed(() => (Number(tienKhachDua.value) || 0) - finalTotal.value)

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
  if (cart.value.length === 0) return false
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

function confirmAddVariant() {
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
  
  const itemKey = `variant-${match.id}`
  const existing = cart.value.find(c => c.key === itemKey)
  if (existing) {
    if (existing.qty >= match.soLuong) {
      showToast('Không thể thêm quá số lượng tồn kho!', 'warning')
      return
    }
    existing.qty++
  } else {
    cart.value.push({
      key: itemKey,
      id: selectedProduct.value.id,
      variantId: match.id,
      name: selectedProduct.value.name,
      price: match.giaBan || selectedProduct.value.price,
      qty: 1,
      color: selectedColor.value,
      size: selectedSize.value,
      maxQty: match.soLuong
    })
  }
  
  showVariantModal.value = false
  showToast(`Đã thêm ${selectedProduct.value.name} (${selectedColor.value} / ${selectedSize.value})`)
}

function removeFromCart(index) {
  cart.value.splice(index, 1)
}

async function applyVoucher() {
  if (!voucherCode.value.trim()) return showToast('Vui lòng nhập mã giảm giá')
  voucherMsg.value = ''
  try {
    const res = await api().applyVoucher(voucherCode.value.trim(), cartTotal.value)
    if (res.valid) {
      appliedVoucher.value = res.maGiamGia
      discount.value = Number(res.giamGia) || 0
      voucherMsg.value = `${res.tenGiamGia} — giảm ${fmtPrice(discount.value)}`
    } else {
      appliedVoucher.value = ''
      discount.value = 0
      voucherMsg.value = res.message || 'Mã không hợp lệ'
    }
  } catch (e) {
    voucherMsg.value = e.message || 'Không áp dụng được mã'
  }
}

function removeVoucher() {
  appliedVoucher.value = ''
  discount.value = 0
  voucherCode.value = ''
  voucherMsg.value = ''
}

function paymentLabel() {
  if (paymentMethod.value === 'cash') return 'Tiền mặt'
  const t = { vietqr: 'Chuyển khoản (VietQR)', momo: 'Chuyển khoản (MoMo)', zalopay: 'Chuyển khoản (ZaloPay)' }
  return t[transferMethod.value] || 'Chuyển khoản'
}

async function createOrder() {
  if (!canPay.value) return
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
      maGiamGia: appliedVoucher.value || null,
      nhanVienId: user?.userId || null,
      ghiChu: noteText,
      tenKhachHang: customerName.value || 'Khách lẻ',
      soDienThoai: customerPhone.value || null
    }
    await api().createOrder(orderData)
    showToast('Tạo đơn & thanh toán thành công!')
    cart.value = []
    customerName.value = ''
    customerPhone.value = ''
    note.value = ''
    tienKhachDua.value = null
    paymentConfirmed.value = false
    removeVoucher()
  } catch (e) {
    showToast('Lỗi: ' + (e.message || 'Không thể tạo đơn'))
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.z-pos-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--z-gray-border);
  cursor: pointer; transition: all 0.15s;
}
.z-pos-item:hover { background: var(--z-accent-soft); }
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
.z-confirm-btn:hover { background: #dcfce7; }
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
.z-test-card-row strong { color: var(--z-dark); font-family: monospace; }
</style>



