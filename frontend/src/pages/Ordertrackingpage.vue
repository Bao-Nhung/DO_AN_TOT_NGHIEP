<template>
  <div>
    <div class="lm-page-hero" data-title="TRA CỨU ĐƠN">
      <div class="container">
        <p class="lm-eyebrow mb-3">Hỗ trợ khách hàng</p>
        <h1 class="mb-3">Tra cứu <em>đơn hàng</em></h1>
        <p style="font-size:16px;color:var(--z-gray);max-width:600px">
          Nhập mã đơn hàng hoặc số điện thoại để xem trạng thái giao hàng của bạn.
        </p>
      </div>
    </div>

    <div class="container py-5">
      <!-- Search Form -->
      <div class="row mb-5">
        <div class="col-lg-8 mx-auto">
          <div style="background:white;padding:40px;border-radius:12px;border:1px solid var(--z-gray-border)">
            <h2 class="z-display mb-4" style="font-size:24px;font-weight:500;text-align:center">Tra cứu đơn hàng</h2>
            
            <div class="row g-3 mb-4">
              <div class="col-md-6">
                <label class="z-label">Mã đơn hàng *</label>
                <input v-model="searchForm.maHoaDon" class="lm-input" placeholder="VD: HD123456" />
              </div>
              <div class="col-md-6">
                <label class="z-label">Số điện thoại (tùy chọn)</label>
                <input v-model="searchForm.soDienThoai" class="lm-input" placeholder="0901234567" type="tel" />
              </div>
            </div>

            <button @click="searchOrder" :disabled="searching" class="lm-btn-primary w-100">
              <span v-if="searching">
                <i class="bi bi-arrow-repeat z-spin"></i> Đang tìm kiếm...
              </span>
              <span v-else>
                <i class="bi bi-search"></i> Tra cứu
              </span>
            </button>

            <p style="font-size:12px;color:var(--z-gray);text-align:center;margin-top:16px">
              <i class="bi bi-shield-check"></i> Thông tin của bạn được bảo mật 100%
            </p>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div v-if="searchDone && !foundOrder" class="row mb-5">
        <div class="col-lg-8 mx-auto">
          <div class="text-center py-5">
            <i class="bi bi-exclamation-circle mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
            <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Không tìm thấy đơn hàng</h3>
            <p style="color:var(--z-gray);font-size:14px">Vui lòng kiểm tra lại mã đơn hàng hoặc số điện thoại.</p>
          </div>
        </div>
      </div>

      <div v-else-if="foundOrder" class="row">
        <div class="col-lg-8 mx-auto">
          <!-- Order Info Summary -->
          <div style="background:white;border:1px solid var(--z-gray-border);border-radius:12px;padding:28px;margin-bottom:24px">
            <div class="row g-4">
              <div class="col-md-6">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:4px">Mã đơn hàng</div>
                <div class="z-display" style="font-size:20px;font-weight:600;color:var(--z-dark)">{{ foundOrder.maHoaDon }}</div>
              </div>
              <div class="col-md-6">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:4px">Trạng thái</div>
                <div :class="'lm-status-' + getStatusKey(foundOrder.trangThaiTracking)" style="font-weight:600">
                  {{ getTrackingLabel(foundOrder.trangThaiTracking) }}
                </div>
              </div>
              <div class="col-md-6">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:4px">Ngày đặt</div>
                <div style="font-size:14px;font-weight:500;color:var(--z-dark)">{{ fmtDate(foundOrder.ngayTao) }}</div>
              </div>
              <div class="col-md-6">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:4px">Tổng tiền</div>
                <div style="font-size:14px;font-weight:600;color:var(--z-accent)">{{ fmtMoney(foundOrder.tongTien) }}</div>
              </div>
              <div class="col-12">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:4px">Địa chỉ giao hàng</div>
                <div style="font-size:14px;color:var(--z-dark)">{{ foundOrder.diaChiGiaoHang }}</div>
              </div>
            </div>
          </div>

          <!-- Tracking Timeline -->
          <div style="background:white;border:1px solid var(--z-gray-border);border-radius:12px;padding:28px;margin-bottom:24px">
            <h3 style="font-size:18px;font-weight:600;margin-bottom:24px">Lịch sử vận chuyển</h3>
            
            <div v-if="foundOrder.trackingHistory && foundOrder.trackingHistory.length > 0" class="z-timeline">
              <div v-for="(event, idx) in foundOrder.trackingHistory" :key="idx" class="z-timeline-item">
                <div class="z-timeline-dot" :class="'z-timeline-dot-' + event.trangThai"></div>
                <div class="z-timeline-content">
                  <div style="font-size:15px;font-weight:600;color:var(--z-dark)">{{ getTrackingLabel(event.trangThai) }}</div>
                  <div v-if="event.moTa" style="font-size:14px;color:var(--z-gray);margin-top:6px">{{ event.moTa }}</div>
                  <div style="font-size:12px;color:var(--z-gray-light);margin-top:8px">
                    <i class="bi bi-clock"></i> {{ fmtDateTime(event.ngayCapNhat) }}
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-4">
              <p style="color:var(--z-gray);font-size:14px">Chưa có cập nhật tracking</p>
            </div>
          </div>

          <!-- Order Items -->
          <div style="background:white;border:1px solid var(--z-gray-border);border-radius:12px;padding:28px">
            <h3 style="font-size:18px;font-weight:600;margin-bottom:20px">Chi tiết đơn hàng</h3>
            
            <div v-if="foundOrder.chiTiets && foundOrder.chiTiets.length > 0" class="d-flex flex-column gap-0">
              <div v-for="item in foundOrder.chiTiets" :key="item.id"
                   class="d-flex align-items-center gap-3" style="padding:16px 0;border-bottom:1px solid var(--z-gray-border)">
                <div style="width:80px;height:80px;border-radius:8px;overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                  <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover" alt="Product">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:28px;color:var(--z-gray-light)">
                    <i class="bi bi-image"></i>
                  </div>
                </div>
                <div class="flex-grow-1">
                  <div style="font-size:15px;font-weight:600;color:var(--z-dark)">{{ item.tenVay || 'Sản phẩm' }}</div>
                  <div style="font-size:13px;color:var(--z-gray);margin-top:4px">
                    <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                      <span v-if="item.maHex" :style="{ width:'12px', height:'12px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                      {{ item.mauSac }}
                    </span>
                    <span v-if="item.mauSac && item.kichThuoc"> · </span>
                    <span v-if="item.kichThuoc">Size {{ item.kichThuoc }}</span>
                  </div>
                </div>
                <div class="text-end">
                  <div style="font-size:14px;font-weight:600;color:var(--z-dark)">{{ fmtMoney(item.donGia) }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">x{{ item.soLuong }}</div>
                </div>
              </div>
            </div>

            <div class="d-flex justify-content-end pt-3" style="border-top:2px solid var(--z-gray-border);margin-top:20px">
              <div class="text-end">
                <div style="font-size:12px;color:var(--z-gray);margin-bottom:6px">Tổng tiền:</div>
                <div class="z-display" style="font-size:22px;font-weight:600;color:var(--z-accent)">{{ fmtMoney(foundOrder.tongTien) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Access -->
      <div v-if="!foundOrder" class="row mt-5">
        <div class="col-lg-8 mx-auto">
          <div style="background:var(--z-bg-alt);border-radius:12px;padding:28px;text-align:center">
            <h3 style="font-size:18px;font-weight:600;margin-bottom:12px">Khách hàng đã đăng nhập?</h3>
            <p style="font-size:14px;color:var(--z-gray);margin-bottom:16px">Truy cập tài khoản của bạn để xem tất cả đơn hàng.</p>
            <RouterLink to="/profile" class="lm-btn-primary"><span>Xem đơn hàng của tôi</span></RouterLink>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import AppFooter from '@/components/layout/AppFooter.vue'

const { showToast } = useToast()

const searchForm = ref({
  maHoaDon: '',
  soDienThoai: ''
})

const searching = ref(false)
const searchDone = ref(false)
const foundOrder = ref(null)

const trackingStatusMap = {
  'pending': 'Chờ xử lý',
  'processing': 'Đang xử lý',
  'shipped': 'Đã gửi đi',
  'delivered': 'Đã giao',
  'cancelled': 'Đã hủy'
}

const statusKeyMap = {
  'pending': 'pending',
  'processing': 'shipping',
  'shipped': 'shipping',
  'delivered': 'delivered',
  'cancelled': 'cancelled'
}

function fmtMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

function fmtDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function fmtDateTime(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }) + ' ' + 
         dt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

function getTrackingLabel(status) {
  return trackingStatusMap[status] || 'Cập nhật'
}

function getStatusKey(status) {
  return statusKeyMap[status] || 'pending'
}

async function searchOrder() {
  if (!searchForm.value.maHoaDon.trim()) {
    return showToast('Vui lòng nhập mã đơn hàng', 'warning')
  }

  searching.value = true
  searchDone.value = false
  foundOrder.value = null

  try {
    const params = {
      maHoaDon: searchForm.value.maHoaDon,
      soDienThoai: searchForm.value.soDienThoai || undefined
    }

    const result = await useApi().searchOrder(params)
    foundOrder.value = result
    searchDone.value = true
    
    if (result) {
      showToast('Tìm thấy đơn hàng!', 'success')
    }
  } catch (err) {
    searchDone.value = true
    showToast(err.message || 'Không tìm thấy đơn hàng', 'error')
  } finally {
    searching.value = false
  }
}
</script>

<style scoped>
.z-timeline {
  position: relative;
  padding-left: 24px;
}

.z-timeline-item {
  position: relative;
  padding-bottom: 32px;
  display: flex;
  gap: 16px;
}

.z-timeline-item:last-child {
  padding-bottom: 0;
}

.z-timeline-dot {
  position: absolute;
  left: -32px;
  top: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--z-gray-border);
  background: var(--z-white);
  flex-shrink: 0;
}

.z-timeline-dot-pending {
  border-color: #9ca3af;
  background: #f3f4f6;
}

.z-timeline-dot-processing {
  border-color: #3b82f6;
  background: #eff6ff;
}

.z-timeline-dot-shipped {
  border-color: #f59e0b;
  background: #fffbeb;
}

.z-timeline-dot-delivered {
  border-color: #10b981;
  background: #f0fdf4;
}

.z-timeline-dot-cancelled {
  border-color: #ef4444;
  background: #fef2f2;
}

.z-timeline-content {
  flex: 1;
  padding-top: 2px;
}

.z-timeline-item:not(:last-child) .z-timeline-dot::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 32px;
  background: var(--z-gray-border);
}

@keyframes z-spin-anim {
  to { transform: rotate(360deg); }
}

.z-spin {
  display: inline-block;
  animation: z-spin-anim 0.8s linear infinite;
}
</style>