<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <!-- Sidebar -->
        <div class="col-lg-3">
          <div style="position:sticky;top:100px">
            <div class="position-relative mb-3" style="width:80px;height:80px">
              <div style="width:100%;height:100%;border-radius:50%;background:linear-gradient(135deg,var(--z-accent-soft),var(--z-accent));display:flex;align-items:center;justify-content:center;font-family:var(--z-font-display);font-size:28px;font-weight:500;color:var(--z-white)">
                {{ userInitial }}
              </div>
            </div>
            <div class="z-display mb-1" style="font-size:24px;font-weight:500;color:var(--z-dark)">{{ user.hoVaTen || 'Khách hàng' }}</div>
            <div class="d-inline-flex align-items-center gap-2 mb-4"
                 style="padding:4px 12px;background:var(--z-accent-soft);font-size:11px;font-weight:600;color:var(--z-accent);border-radius:20px">
              <i class="bi bi-star-fill" style="font-size:10px"></i> {{ user.role === 'Admin' ? 'Admin' : 'Thành viên' }}
            </div>

            <nav style="border-top:1px solid var(--z-gray-border)">
              <div v-for="item in navItems" :key="item.tab"
                   class="lm-profile-nav-item" :class="{ active: activeTab === item.tab }"
                   @click="activeTab = item.tab">
                <i class="bi" :class="item.icon"></i>
                {{ item.label }}
              </div>
              <div class="lm-profile-nav-item" @click="doLogout">
                <i class="bi bi-box-arrow-right"></i>
                Đăng xuất
              </div>
            </nav>
          </div>
        </div>

        <!-- Content -->
        <div class="col-lg-9 pt-2">

          <!-- Orders Tab -->
          <div v-if="activeTab === 'orders'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Đơn hàng <em style="font-style:italic;color:var(--z-gray)">của tôi</em></h2>
            <div class="row g-3 mb-5">
              <div v-for="stat in stats" :key="stat.label" class="col-6 col-md-3">
                <div style="padding:20px;background:var(--z-white);border:1px solid var(--z-gray-border);text-align:center;border-radius:var(--z-radius-lg)">
                  <div class="z-display" style="font-size:28px;font-weight:500;color:var(--z-dark)">{{ stat.num }}</div>
                  <div style="font-size:12px;font-weight:500;color:var(--z-gray);margin-top:4px">{{ stat.label }}</div>
                </div>
              </div>
            </div>
            <div v-if="sortedOrders.length === 0" class="text-center py-5">
              <i class="bi bi-bag mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
              <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Chưa có đơn hàng</h3>
              <p style="color:var(--z-gray);font-size:14px">Hãy khám phá bộ sưu tập và đặt đơn hàng đầu tiên.</p>
              <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Mua sắm ngay</span></RouterLink>
            </div>
            <div v-else class="d-flex flex-column gap-3">
              <div v-for="order in sortedOrders" :key="order.id"
                   class="z-order-card">
                <div class="d-flex justify-content-between align-items-start mb-3 pb-3" style="border-bottom:1px solid var(--z-gray-border)">
                  <div>
                    <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ order.maHoaDon }}</div>
                    <div style="font-size:13px;color:var(--z-gray)">{{ fmtDate(order.ngayTao) }} · {{ order.hinhThucThanhToan }}</div>
                  </div>
                  <span :class="'lm-status-' + (statusMap[order.trangThai]?.key || 'pending')">
                    {{ statusMap[order.trangThai]?.label || 'Chờ xử lý' }}
                  </span>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="z-display" style="font-size:20px;font-weight:500;color:var(--z-dark)">{{ fmtMoney(order.tongTien) }}</div>
                    <div style="font-size:12px;color:var(--z-gray-light);margin-top:2px">{{ order.soSanPham || 0 }} sản phẩm</div>
                  </div>
                  <div class="d-flex gap-2">
                    <button class="z-order-btn-small" @click="openOrderDetail(order)" title="Xem chi tiết">
                      <i class="bi bi-eye"></i> Chi tiết
                    </button>
                    <button class="z-order-btn-small z-order-btn-tracking" @click="openOrderTracking(order)" title="Xem tracking">
                      <i class="bi bi-box-seam"></i> Tracking
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-if="activeTab === 'settings'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Thông tin <em style="font-style:italic;color:var(--z-gray)">cá nhân</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-md-6"><label class="lm-form-label mb-2">Họ và tên</label><input class="lm-input" v-model="profile.hoVaTen"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Email</label><input class="lm-input" type="email" v-model="profile.email" disabled></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Số điện thoại</label><input class="lm-input" type="tel" v-model="profile.soDienThoai"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Giới tính</label>
                <select class="lm-input" v-model="profile.gioiTinh"><option value="">Chọn</option><option>Nữ</option><option>Nam</option></select>
              </div>
            </div>
            <button class="lm-btn-primary" @click="saveProfile" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}</span></button>
          </div>

          <!-- Address Tab -->
          <div v-if="activeTab === 'address'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Địa chỉ <em style="font-style:italic;color:var(--z-gray)">giao hàng</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-12"><label class="lm-form-label mb-2">Địa chỉ</label><input class="lm-input" v-model="address.street"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Quận / Huyện</label><input class="lm-input" v-model="address.district"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Tỉnh / Thành phố</label>
                <select class="lm-input" v-model="address.city"><option>Hà Nội</option><option>TP. Hồ Chí Minh</option><option>Đà Nẵng</option></select>
              </div>
            </div>
            <button class="lm-btn-primary" @click="showToast('Đã lưu địa chỉ!', 'success')"><span>Lưu địa chỉ</span></button>
          </div>

        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showDetail" class="z-modal-overlay" @click.self="showDetail = false">
      <div class="z-modal" style="max-width:700px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết đơn hàng</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ detailOrder?.maHoaDon }}</div>
          </div>
          <button class="z-icon-btn" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
          <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Đang tải...</p>
        </div>

        <div v-else-if="detailOrder">
          <div class="d-flex justify-content-between align-items-center mb-3 pb-3" style="border-bottom:1px solid var(--z-gray-border)">
            <div>
              <div style="font-size:13px;color:var(--z-gray)">Ngày đặt: {{ fmtDate(detailOrder.ngayTao) }}</div>
              <div style="font-size:13px;color:var(--z-gray)">Thanh toán: {{ detailOrder.hinhThucThanhToan }}</div>
              <div v-if="detailOrder.diaChiGiaoHang" style="font-size:13px;color:var(--z-gray)">Địa chỉ: {{ detailOrder.diaChiGiaoHang }}</div>
            </div>
            <span :class="'lm-status-' + (statusMap[detailOrder.trangThai]?.key || 'pending')">
              {{ statusMap[detailOrder.trangThai]?.label || 'Chờ xử lý' }}
            </span>
          </div>

          <div class="d-flex flex-column gap-0 mb-4">
            <div v-for="item in detailOrder.chiTiets" :key="item.id"
                 class="d-flex align-items-center gap-3" style="padding:12px 0;border-bottom:1px solid var(--z-gray-border)">
              <div style="width:56px;height:64px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:18px;color:var(--z-gray-light)">
                  <i class="bi bi-image"></i>
                </div>
              </div>
              <div class="flex-grow-1">
                <div style="font-size:14px;font-weight:500;color:var(--z-dark)">{{ item.tenVay || 'Sản phẩm' }}</div>
                <div style="font-size:12px;color:var(--z-gray)">
                  <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                    <span v-if="item.maHex" :style="{ width:'10px', height:'10px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                    {{ item.mauSac }}
                  </span>
                  <span v-if="item.mauSac && item.kichThuoc"> · </span>
                  <span v-if="item.kichThuoc">Size {{ item.kichThuoc }}</span>
                  <span> · SL: {{ item.soLuong }}</span>
                </div>
              </div>
              <div style="font-size:14px;font-weight:600;color:var(--z-dark);white-space:nowrap">{{ fmtMoney(item.donGia) }}</div>
            </div>
          </div>

          <div v-if="detailOrder.ghiChu" class="mb-3 p-3" style="background:var(--z-bg-alt);border-radius:var(--z-radius);font-size:13px;color:var(--z-gray)">
            <strong>Ghi chú:</strong> {{ detailOrder.ghiChu }}
          </div>

          <div class="d-flex justify-content-between align-items-center pt-3" style="border-top:2px solid var(--z-dark)">
            <div>Tổng tiền:</div>
            <div class="z-display" style="font-size:20px;font-weight:600">{{ fmtMoney(detailOrder.tongTien) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Tracking Modal -->
    <div v-if="showTracking" class="z-modal-overlay" @click.self="showTracking = false">
      <div class="z-modal" style="max-width:700px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Theo dõi đơn hàng</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ trackingOrder?.maHoaDon }}</div>
          </div>
          <button class="z-icon-btn" @click="showTracking = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingTracking" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
          <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Đang tải...</p>
        </div>

        <div v-else-if="trackingOrder">
          <!-- Tracking Summary -->
          <div class="z-tracking-summary mb-4 p-4" style="background:var(--z-accent-soft);border-radius:var(--z-radius-lg)">
            <div style="font-size:13px;color:var(--z-gray);margin-bottom:8px">Trạng thái hiện tại</div>
            <div style="font-size:18px;font-weight:600;color:var(--z-accent);margin-bottom:12px">
              {{ trackingStatusLabel }}
            </div>
            <div style="font-size:12px;color:var(--z-gray)">
              <div v-if="trackingOrder.ngayGiaoHangDuKien">
                📅 Dự kiến giao: {{ fmtDate(trackingOrder.ngayGiaoHangDuKien) }}
              </div>
              <div v-if="trackingOrder.ngayGiaoHangThucTe">
                ✓ Đã giao: {{ fmtDate(trackingOrder.ngayGiaoHangThucTe) }}
              </div>
              <div v-if="trackingOrder.diaChiGiaoHang">
                📍 {{ trackingOrder.diaChiGiaoHang }}
              </div>
            </div>
          </div>

          <!-- Tracking Timeline -->
          <div v-if="trackingOrder.trackingHistory && trackingOrder.trackingHistory.length > 0">
            <h4 style="font-size:14px;font-weight:600;margin-bottom:16px">Lịch sử cập nhật</h4>
            <div class="z-timeline">
              <div v-for="(event, idx) in trackingOrder.trackingHistory" :key="idx" class="z-timeline-item">
                <div class="z-timeline-dot" :class="'z-timeline-dot-' + event.trangThai"></div>
                <div class="z-timeline-content">
                  <div style="font-size:14px;font-weight:500;color:var(--z-dark)">{{ getTrackingLabel(event.trangThai) }}</div>
                  <div v-if="event.moTa" style="font-size:13px;color:var(--z-gray);margin-top:4px">{{ event.moTa }}</div>
                  <div style="font-size:12px;color:var(--z-gray-light);margin-top:6px">{{ fmtDateTime(event.ngayCapNhat) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'

const router = useRouter()
const { showToast } = useToast()
const { getUser, isLoggedIn, logout } = useAuth()

const activeTab = ref('orders')

const user = ref(getUser() || {})
const userInitial = computed(() => {
  const name = user.value.hoVaTen || user.value.username || ''
  return name.charAt(0).toUpperCase() || 'U'
})

const gioiTinhMap = { 0: 'Nữ', 1: 'Nam' }
const gioiTinhReverse = { 'Nữ': '0', 'Nam': '1' }

const profile = ref({
  hoVaTen: user.value.hoVaTen || '',
  email: user.value.email || '',
  soDienThoai: user.value.soDienThoai || '',
  gioiTinh: gioiTinhMap[user.value.gioiTinh] || '',
})

const address = ref({ street: '', district: '', city: 'Hà Nội' })

const orders = ref([])
const showDetail = ref(false)
const detailOrder = ref(null)
const loadingDetail = ref(false)

const showTracking = ref(false)
const trackingOrder = ref(null)
const loadingTracking = ref(false)

const statusMap = {
  0: { key: 'pending', label: 'Chờ xử lý' },
  1: { key: 'paid', label: 'Đã xác nhận' },
  2: { key: 'shipping', label: 'Đang giao' },
  3: { key: 'delivered', label: 'Đã giao' },
  4: { key: 'cancelled', label: 'Đã huỷ' },
  5: { key: 'failed', label: 'Thất bại' },
}

const trackingStatusMap = {
  'pending': 'Chờ xử lý',
  'processing': 'Đang xử lý',
  'shipped': 'Đã gửi đi',
  'delivered': 'Đã giao',
  'cancelled': 'Đã hủy'
}

const trackingStatusLabel = computed(() => {
  return trackingStatusMap[trackingOrder.value?.trangThaiTracking] || 'Chưa xác định'
})

const sortedOrders = computed(() => {
  return [...orders.value].sort((a, b) => {
    const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    return db - da
  })
})

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

const stats = computed(() => {
  const total = orders.value.length
  const spent = orders.value
    .filter(o => o.trangThai === 1 || o.trangThai === 3)
    .reduce((s, o) => s + Number(o.tongTien || 0), 0)
  return [
    { num: String(total), label: 'Tổng đơn' },
    { num: fmtMoney(spent), label: 'Đã chi tiêu' },
    { num: String(Math.floor(spent / 10000)), label: 'Điểm tích luỹ' },
    { num: total >= 10 ? 'Vàng' : total >= 5 ? 'Bạc' : 'Mới', label: 'Hạng thành viên' },
  ]
})

async function openOrderDetail(order) {
  showDetail.value = true
  loadingDetail.value = true
  try {
    detailOrder.value = await api().getHoaDonById(order.id)
  } catch (e) {
    detailOrder.value = order
  } finally {
    loadingDetail.value = false
  }
}

async function openOrderTracking(order) {
  showTracking.value = true
  loadingTracking.value = true
  try {
    trackingOrder.value = await api().getOrderTracking(order.id)
  } catch (e) {
    showToast('Không thể tải thông tin tracking', 'error')
    showTracking.value = false
  } finally {
    loadingTracking.value = false
  }
}

onMounted(async () => {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  try {
    const data = await api().getMyOrders()
    orders.value = data || []
  } catch (e) {
    console.error('Failed to load orders:', e)
  }
})

const saving = ref(false)

async function saveProfile() {
  saving.value = true
  try {
    const data = {
      hoVaTen: profile.value.hoVaTen,
      soDienThoai: profile.value.soDienThoai,
      gioiTinh: gioiTinhReverse[profile.value.gioiTinh] || '',
    }
    await api().updateProfile(data)
    const stored = getUser()
    if (stored) {
      stored.hoVaTen = profile.value.hoVaTen
      stored.soDienThoai = profile.value.soDienThoai
      stored.gioiTinh = gioiTinhReverse[profile.value.gioiTinh] != null
        ? Number(gioiTinhReverse[profile.value.gioiTinh]) : null
      localStorage.setItem('zestia_user', JSON.stringify(stored))
      user.value = stored
    }
    showToast('Đã lưu thông tin!', 'success')
  } catch (e) {
    showToast(e.error || 'Lỗi khi lưu thông tin', 'error')
  } finally {
    saving.value = false
  }
}

function doLogout() {
  logout()
  showToast('Đã đăng xuất', 'info')
  router.push('/login')
}

const navItems = [
  { tab: 'orders',   icon: 'bi-file-text',   label: 'Đơn hàng của tôi' },
  { tab: 'settings', icon: 'bi-person',       label: 'Thông tin cá nhân' },
  { tab: 'address',  icon: 'bi-geo-alt',      label: 'Địa chỉ giao hàng' },
]
</script>

<style scoped>
.z-order-card {
  border: 1px solid var(--z-gray-border);
  padding: 20px;
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  transition: all 0.2s;
}
.z-order-card:hover {
  border-color: var(--z-accent);
  box-shadow: 0 4px 16px rgba(212,86,78,0.08);
}

.z-order-btn-small {
  padding: 6px 12px;
  font-size: 12px;
  border: 1px solid var(--z-gray-border);
  background: var(--z-white);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--z-gray);
}

.z-order-btn-small:hover {
  border-color: var(--z-accent);
  color: var(--z-accent);
  background: var(--z-accent-soft);
}

.z-order-btn-tracking {
  color: var(--z-accent);
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
}

.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }

/* Tracking Timeline */
.z-timeline {
  position: relative;
  padding-left: 24px;
}

.z-timeline-item {
  position: relative;
  padding-bottom: 24px;
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
  height: 24px;
  background: var(--z-gray-border);
}
</style>