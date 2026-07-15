<template>
  <div>
    <div class="lm-page-hero" data-title="TRA CỨU ĐƠN">
      <div class="container text-center">
        <p class="lm-eyebrow mb-3">Hỗ trợ khách hàng</p>
        <h1 class="mb-3">Tra cứu <em>đơn hàng</em></h1>
        <p style="font-size:16px;color:var(--z-gray);max-width:600px;margin: 0 auto;">
          Nhập mã đơn hàng và số điện thoại để xem trạng thái giao hàng của bạn.
        </p>
      </div>
    </div>

    <div class="container py-5" style="max-width: 800px; min-height: 50vh;">
      <div class="z-search-box mb-5">
        <h4 class="mb-4 text-center z-display">Thông tin đơn hàng</h4>
        <form @submit.prevent="handleSearch">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label" style="font-size: 13px; font-weight: 500;">Mã đơn hàng *</label>
              <input v-model="form.maHoaDon" class="lm-input" placeholder="VD: HD2606..." required />
            </div>
            <div class="col-md-6">
              <label class="form-label" style="font-size: 13px; font-weight: 500;">Số điện thoại *</label>
              <input v-model="form.soDienThoai" class="lm-input" placeholder="Nhập SĐT đặt hàng" required />
            </div>
            <div class="col-12 mt-4">
              <button type="submit" class="lm-btn-primary w-100" :disabled="loading">
                <span v-if="loading"><i class="bi bi-arrow-repeat z-spin"></i> Đang tra cứu...</span>
                <span v-else><i class="bi bi-search me-2"></i>Tra cứu ngay</span>
              </button>
            </div>
          </div>
        </form>
      </div>

      <!-- Kết quả tra cứu -->
      <div v-if="currentOrder" class="d-flex flex-column gap-4">
        
        <!-- Lộ trình vận chuyển -->
        <OrderTrackingCard :order="currentOrder" />

        <!-- Chi tiết đơn hàng -->
        <div class="z-order-details p-4">
          <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom flex-wrap gap-2">
            <div>
              <h4 class="m-0 font-weight-bold" style="color: var(--z-dark); font-size: 16px;">Chi tiết sản phẩm</h4>
              <span class="text-muted" style="font-size: 12px;">Đơn hàng gồm {{ currentOrder.chiTiets?.length || 0 }} sản phẩm</span>
            </div>
            <div class="d-flex gap-2">
              <!-- Repay Online -->
              <button 
                v-if="!currentOrder.daThanhToan && currentOrder.trangThai !== 5 && currentOrder.trangThai !== 7 && (currentOrder.hinhThucThanhToan === 'MOMO' || currentOrder.hinhThucThanhToan === 'ZALOPAY')"
                class="lm-btn-outline-accent py-2 px-3 d-flex align-items-center gap-2"
                style="font-size: 12px; height: auto;"
                @click="repayOrder(currentOrder)"
                :disabled="payingId === currentOrder.id"
              >
                <i class="bi bi-credit-card-2-back"></i>
                <span>{{ payingId === currentOrder.id ? 'Đang tạo...' : 'Thanh toán lại' }}</span>
              </button>

              <!-- Cancel Request -->
              <button 
                v-if="currentOrder.trangThai === 0"
                class="lm-btn-outline-danger py-2 px-3 d-flex align-items-center gap-2"
                style="font-size: 12px; height: auto;"
                @click="openCancel(currentOrder)"
              >
                <i class="bi bi-x-circle"></i>
                <span>Hủy đơn</span>
              </button>
            </div>
          </div>

          <!-- Thông tin giao nhận và thanh toán -->
          <div class="row g-3 mb-4">
            <div class="col-md-6">
              <div class="p-3 rounded h-100" style="background: var(--z-bg-alt); border: 1px solid var(--z-gray-border)">
                <h5 style="font-size:14px;font-weight:700;color:var(--z-dark);margin-bottom:12px;">
                  <i class="bi bi-geo-alt me-1"></i> Thông tin nhận hàng
                </h5>
                <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Người nhận:</strong> {{ currentOrder.khachHang || 'Khách hàng' }}</div>
                <div v-if="currentOrder.soDienThoai" style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Số điện thoại:</strong> {{ currentOrder.soDienThoai }}</div>
                <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Hình thức:</strong> {{ currentOrder.hinhThucNhanHang === 0 ? 'Nhận tại cửa hàng' : 'Giao hàng tận nơi' }}</div>
                <div v-if="currentOrder.diaChiGiaoHang && currentOrder.hinhThucNhanHang !== 0" style="font-size:13px;color:var(--z-dark);"><strong>Địa chỉ:</strong> {{ currentOrder.diaChiGiaoHang }}</div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="p-3 rounded h-100" style="background: var(--z-bg-alt); border: 1px solid var(--z-gray-border)">
                <h5 style="font-size:14px;font-weight:700;color:var(--z-dark);margin-bottom:12px;">
                  <i class="bi bi-credit-card me-1"></i> Thông tin thanh toán
                </h5>
                <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Ngày đặt:</strong> {{ formatDateTime(currentOrder.ngayTao) }}</div>
                <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Phương thức:</strong> {{ currentOrder.hinhThucThanhToan }}</div>
                <div style="font-size:13px;color:var(--z-dark);">
                  <strong>Trạng thái: </strong>
                  <span v-if="currentOrder.phuongThucThanhToanOnline === 'FAILED' || (currentOrder.trangThai === 5 && (currentOrder.hinhThucThanhToan === 'MOMO' || currentOrder.hinhThucThanhToan === 'ZALOPAY') && !currentOrder.daThanhToan)"
                        class="text-danger" style="font-weight: 600;">
                    Thanh toán thất bại
                  </span>
                  <span v-else :class="currentOrder.daThanhToan ? 'text-success' : 'text-warning'" style="font-weight: 600;">
                    {{ currentOrder.daThanhToan ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="d-flex flex-column gap-0 mb-4 rounded border" style="overflow:hidden;">
            <div v-for="item in currentOrder.chiTiets" :key="item.id"
                 class="d-flex align-items-center justify-content-between gap-3 bg-white p-3" style="border-bottom:1px solid var(--z-gray-border)">
              <div class="d-flex align-items-center gap-3">
                <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                  <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:18px;color:var(--z-gray-light)">
                    <i class="bi bi-image"></i>
                  </div>
                </div>
                <div>
                  <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ item.tenVay || 'Sản phẩm' }}</div>
                  <div style="font-size:11px;color:var(--z-gray);margin-top:2px">
                    <span v-if="item.maSanPham">Mã SP: {{ item.maSanPham }} · </span>
                    <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                      <span v-if="item.maHex" :style="{ width:'8px', height:'8px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                      {{ item.mauSac }}
                    </span>
                    <span v-if="item.mauSac && item.kichThuoc"> · </span>
                    <span v-if="item.kichThuoc">Size: {{ item.kichThuoc }}</span>
                    <span> · SL: {{ item.soLuong }}</span>
                  </div>
                </div>
              </div>
              <div style="font-size:13px;font-weight:700;color:var(--z-dark)">{{ formatMoney(item.donGia) }}</div>
            </div>
          </div>

          <!-- Ghi chú -->
          <div v-if="currentOrder.ghiChu" class="mb-3 p-3 rounded" style="background:var(--z-bg-alt);font-size:13px;color:var(--z-gray)">
            <strong>Ghi chú đơn hàng:</strong> {{ currentOrder.ghiChu }}
          </div>

          <!-- Bảng tính tiền -->
          <div class="p-3 bg-light rounded" style="font-size:13px; color:var(--z-gray)">
             <div class="d-flex justify-content-between mb-2">
                <span>Tổng tiền hàng:</span>
                <span style="font-weight: 500; color: var(--z-dark);">{{ formatMoney(currentOrder.tongTien + (currentOrder.giamGiaVoucher || 0) - (currentOrder.phiVanChuyen || 0)) }}</span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Phí vận chuyển:</span>
                <span style="font-weight: 500; color: var(--z-dark);">
                  {{ currentOrder.phiVanChuyen > 0 ? '+ ' + formatMoney(currentOrder.phiVanChuyen) : '0đ (Miễn phí)' }}
                </span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Voucher giảm giá:</span>
                <span class="text-danger font-weight-bold">
                  {{ currentOrder.giamGiaVoucher > 0 ? '- ' + formatMoney(currentOrder.giamGiaVoucher) : '0đ' }}
                </span>
             </div>
             
             <div class="d-flex justify-content-between align-items-center pt-2 mt-2" style="border-top:1px dashed var(--z-gray-border)">
                <div style="font-size:14px;font-weight:600; color: var(--z-dark);">Tổng cộng</div>
                <div class="z-display" style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ formatMoney(currentOrder.tongTien) }}</div>
             </div>
          </div>
        </div>

      </div>

      <!-- Cancel Confirmation Modal -->
      <div v-if="showCancelModal" class="z-modal-overlay" @click.self="showCancelModal = false" style="z-index: 1060; background: rgba(0,0,0,0.6); position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px);">
        <div class="z-modal bg-white p-4 shadow-lg" style="max-width:500px; width: 90%; border-radius: var(--z-radius-lg)">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 style="font-size:18px;font-weight:600;margin:0">
              {{ cancelStep === 'reason' ? 'Lý do huỷ đơn hàng' : 'Xác nhận mã OTP' }}
            </h3>
            <button class="z-icon-btn" @click="showCancelModal = false"><i class="bi bi-x-lg"></i></button>
          </div>
          <p style="font-size:14px;color:var(--z-gray);margin-bottom:20px">
            {{ cancelStep === 'reason'
              ? 'Vui lòng cho Zestia biết lý do bạn muốn huỷ đơn hàng này nhé:'
              : otpMessage }}
          </p>
          
          <div v-if="cancelStep === 'reason'" class="d-flex flex-column gap-3 mb-4">
            <label class="d-flex align-items-center gap-2" style="cursor:pointer">
              <input type="radio" v-model="cancelReason" value="Thay đổi ý định mua">
              <span style="font-size:14px;color:var(--z-dark)">Thay đổi ý định mua</span>
            </label>
            <label class="d-flex align-items-center gap-2" style="cursor:pointer">
              <input type="radio" v-model="cancelReason" value="Tìm thấy giá rẻ hơn ở nơi khác">
              <span style="font-size:14px;color:var(--z-dark)">Tìm thấy giá rẻ hơn ở nơi khác</span>
            </label>
            <label class="d-flex align-items-center gap-2" style="cursor:pointer">
              <input type="radio" v-model="cancelReason" value="Thời gian giao hàng quá lâu">
              <span style="font-size:14px;color:var(--z-dark)">Thời gian giao hàng quá lâu</span>
            </label>
            <label class="d-flex align-items-center gap-2" style="cursor:pointer">
              <input type="radio" v-model="cancelReason" value="Muốn nhập lại voucher khác">
              <span style="font-size:14px;color:var(--z-dark)">Muốn nhập lại voucher khác</span>
            </label>
            <label class="d-flex align-items-center gap-2" style="cursor:pointer">
              <input type="radio" v-model="cancelReason" value="Khác">
              <span style="font-size:14px;color:var(--z-dark)">Lý do khác</span>
            </label>
            
            <textarea 
              v-if="cancelReason === 'Khác'" 
              v-model="otherCancelReason" 
              class="lm-input mt-2" 
              rows="3" 
              placeholder="Nhập lý do hủy cụ thể của bạn..."
            ></textarea>
          </div>

          <div v-else class="mb-4">
            <label class="form-label" style="font-size:13px;font-weight:600">Mã OTP gồm 6 chữ số</label>
            <input
              v-model="cancelOtp"
              class="lm-input text-center"
              inputmode="numeric"
              autocomplete="one-time-code"
              maxlength="6"
              placeholder="000000"
              style="font-size:22px;letter-spacing:8px;font-weight:700"
              @input="normalizeCancelOtp"
            />
            <button
              type="button"
              class="btn btn-link p-0 mt-3"
              style="font-size:13px;color:var(--z-accent);text-decoration:none"
              :disabled="sendingOtp"
              @click="requestCancelOtp"
            >
              {{ sendingOtp ? 'Đang gửi...' : 'Gửi lại mã OTP' }}
            </button>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button class="lm-btn-secondary py-2 px-4" style="height:auto; font-size: 13px;" @click="showCancelModal = false">Đóng</button>
            <button
              v-if="cancelStep === 'reason'"
              class="z-danger-action-btn py-2 px-4"
              style="height:auto; font-size: 13px;"
              :disabled="sendingOtp"
              @click="requestCancelOtp"
            >
              <span>{{ sendingOtp ? 'Đang gửi...' : 'Gửi mã OTP' }}</span>
            </button>
            <button
              v-else
              class="z-danger-action-btn py-2 px-4"
              style="height:auto; font-size: 13px;"
              :disabled="cancelling || cancelOtp.length !== 6"
              @click="submitCancelOrder"
            >
              <span>{{ cancelling ? 'Đang huỷ...' : 'Xác nhận huỷ' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useOrders } from '@/composables/useOrders'
import { useToast } from '@/composables/useToast'
import AppFooter from '@/components/layout/AppFooter.vue'
import OrderTrackingCard from '@/components/OrderTrackingCard.vue'
import { api } from '@/composables/useApi'

const { searchOrder, currentOrder, loading } = useOrders()
const { showToast } = useToast()
const route = useRoute()

const form = ref({
  maHoaDon: '',
  soDienThoai: ''
})

const showCancelModal = ref(false)
const cancelReason = ref('')
const otherCancelReason = ref('')
const cancelStep = ref('reason')
const cancelOtp = ref('')
const otpMessage = ref('Mã OTP sẽ được gửi đến email nhận hóa đơn của bạn.')
const sendingOtp = ref(false)
const cancelling = ref(false)
const payingId = ref(null)

onMounted(() => {
  const maHoaDon = route.query.maHoaDon ? String(route.query.maHoaDon) : ''
  const soDienThoai = route.query.soDienThoai ? String(route.query.soDienThoai) : ''
  if (maHoaDon) form.value.maHoaDon = maHoaDon
  if (soDienThoai) form.value.soDienThoai = soDienThoai
  if (maHoaDon && soDienThoai) {
    handleSearch()
  }
})

async function handleSearch() {
  if (!form.value.maHoaDon.trim()) {
    showToast('Vui lòng nhập mã đơn hàng', 'warning')
    return
  }
  if (!form.value.soDienThoai.trim()) {
    showToast('Vui lòng nhập số điện thoại', 'warning')
    return
  }
  try {
    await searchOrder(form.value.maHoaDon.trim(), form.value.soDienThoai.trim())
    showToast('Tra cứu thành công', 'success')
  } catch (e) {
    currentOrder.value = null
    showToast(e.message || 'Không tìm thấy đơn hàng. Vui lòng kiểm tra lại mã.', 'error')
  }
}

// Repay Online
async function repayOrder(order) {
  payingId.value = order.id
  try {
    let res = null
    if (order.hinhThucThanhToan === 'MOMO') {
      res = await api().createMomoPayment(order.id, order.maHoaDon, order.soDienThoai)
    } else if (order.hinhThucThanhToan === 'ZALOPAY') {
      res = await api().createZaloPayment(order.id, order.maHoaDon, order.soDienThoai)
    }
    if (res && res.payUrl) {
      window.location.href = res.payUrl
    } else {
      showToast(res?.error || 'Không thể kết nối cổng thanh toán, vui lòng thử lại sau!', 'error')
    }
  } catch (e) {
    showToast(e.error || 'Lỗi khi kết nối thanh toán trực tuyến', 'error')
  } finally {
    payingId.value = null
  }
}

function openCancel(order) {
  cancelReason.value = ''
  otherCancelReason.value = ''
  cancelStep.value = 'reason'
  cancelOtp.value = ''
  otpMessage.value = 'Mã OTP sẽ được gửi đến email nhận hóa đơn của bạn.'
  showCancelModal.value = true
}

function getCancelReason() {
  if (!cancelReason.value) {
    showToast('Vui lòng chọn lý do hủy!', 'warning')
    return null
  }
  const finalReason = cancelReason.value === 'Khác' ? otherCancelReason.value : cancelReason.value
  if (cancelReason.value === 'Khác' && !finalReason.trim()) {
    showToast('Vui lòng nhập lý do cụ thể!', 'warning')
    return null
  }
  return finalReason.trim()
}

function normalizeCancelOtp() {
  cancelOtp.value = cancelOtp.value.replace(/\D/g, '').slice(0, 6)
}

async function requestCancelOtp() {
  if (!getCancelReason()) return

  sendingOtp.value = true
  try {
    const res = await api().requestGuestCancelOtp(
      currentOrder.value.id,
      currentOrder.value.maHoaDon,
      form.value.soDienThoai.trim()
    )
    cancelStep.value = 'otp'
    cancelOtp.value = ''
    otpMessage.value = res?.message || `Mã OTP đã được gửi tới ${res?.emailMasked || 'email nhận hóa đơn'}.`
    showToast('Đã gửi mã OTP qua email', 'success')
  } catch (e) {
    showToast(e.error || 'Không thể gửi mã OTP', 'error')
  } finally {
    sendingOtp.value = false
  }
}

async function submitCancelOrder() {
  const finalReason = getCancelReason()
  if (!finalReason) return
  if (!/^\d{6}$/.test(cancelOtp.value)) {
    showToast('Vui lòng nhập mã OTP gồm 6 chữ số', 'warning')
    return
  }

  cancelling.value = true
  try {
    const res = await api().cancelGuestOrder(
      currentOrder.value.id,
      currentOrder.value.maHoaDon,
      form.value.soDienThoai.trim(),
      cancelOtp.value,
      finalReason
    )
    if (res && res.error) {
      showToast(res.error, 'error')
    } else {
      showToast('Huỷ đơn hàng thành công!', 'success')
      showCancelModal.value = false
      // Load lại đơn hàng sau khi hủy thành công để cập nhật UI đồng bộ
      await searchOrder(form.value.maHoaDon.trim(), form.value.soDienThoai.trim())
    }
  } catch (e) {
    showToast(e.error || 'Lỗi khi hủy đơn hàng', 'error')
  } finally {
    cancelling.value = false
  }
}

// Formatters
function formatMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>

<style scoped>
.z-search-box {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
}

.z-order-details {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.z-icon-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: var(--z-radius);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--z-gray);
  transition: all 0.2s;
}

.z-icon-btn:hover {
  background: var(--z-bg-alt);
  color: var(--z-dark);
}

@keyframes spin { to { transform: rotate(360deg); } }
.z-spin { display: inline-block; animation: spin 1s linear infinite; }
</style>
