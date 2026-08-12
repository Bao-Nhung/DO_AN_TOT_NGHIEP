<template>
  <AdminLayout>
    <template v-if="isStaffDashboard">
      <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-3">
        <div>
          <p class="lm-eyebrow mb-2">Ca làm hôm nay</p>
          <h1 class="z-display mb-1" style="font-size:30px;font-weight:500;color:var(--z-dark)">
            Xin chào, {{ userName }}
          </h1>
          <p style="font-size:14px;color:var(--z-gray);margin:0">
            Theo dõi nhanh ca làm, đơn tại quầy và các đơn đang cần xử lý.
          </p>
        </div>
        <div v-if="staffCanOperate" class="d-flex gap-2">
          <RouterLink to="/admin/pos" class="lm-btn-primary"><span><i class="bi bi-shop me-1"></i>Bán tại quầy</span></RouterLink>
          <RouterLink to="/admin/orders" class="lm-btn-secondary">Xem đơn hàng</RouterLink>
        </div>
        <RouterLink v-else to="/admin/schedule" class="lm-btn-primary"><span><i class="bi bi-calendar-check me-1"></i>Xem và xác nhận ca</span></RouterLink>
      </div>

      <div v-if="!staffCanOperate" class="z-admin-card mb-4" style="border-left:4px solid var(--z-accent)">
        <div style="font-size:15px;font-weight:650;color:var(--z-dark)">{{ staffShiftReason }}</div>
        <div style="font-size:12px;color:var(--z-gray);margin-top:5px">Bạn chỉ có thể bán hàng và xem số liệu trong đúng ca đã check-in.</div>
      </div>

      <div v-if="staffCanOperate" class="row g-3 mb-4">
        <div v-for="stat in staffStats" :key="stat.label" class="col-12 col-sm-6 col-xl-3">
          <div class="z-stat-card z-staff-stat">
            <div class="d-flex align-items-center justify-content-between mb-3">
              <span class="z-stat-label">{{ stat.label }}</span>
              <i class="bi" :class="stat.icon"></i>
            </div>
            <div class="z-stat-value">{{ stat.value }}</div>
            <div class="z-stat-change">{{ stat.hint }}</div>
          </div>
        </div>
      </div>

      <div class="row g-3">
        <div :class="staffCanOperate ? 'col-xl-5' : 'col-12'">
          <div class="z-admin-card h-100">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="z-admin-card-title">Lịch hôm nay</h3>
              <RouterLink to="/admin/schedule" class="z-inline-link">Xem lịch</RouterLink>
            </div>
            <div v-if="todayShifts.length" class="d-flex flex-column gap-3">
              <div v-for="shift in todayShifts" :key="shift.id" class="z-shift-row">
                <div>
                  <div style="font-weight:700;color:var(--z-dark)">{{ shift.caLam || 'Ca làm' }}</div>
                  <div style="font-size:13px;color:var(--z-gray)">{{ shortTime(shift.gioBatDau) }} - {{ shortTime(shift.gioKetThuc) }}</div>
                </div>
                <span class="z-status" :class="shift.trangThai === 1 ? 'success' : 'pending'">{{ shift.trangThai === 1 ? 'Đã xác nhận' : 'Chờ xác nhận' }}</span>
              </div>
            </div>
            <div v-else class="z-empty-state">
              <i class="bi bi-calendar2-check"></i>
              <div>Hôm nay chưa có ca được phân công.</div>
            </div>
          </div>
        </div>

        <div v-if="staffCanOperate" class="col-xl-7">
          <div class="z-admin-card h-100">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="z-admin-card-title">Đơn tại quầy gần đây</h3>
              <RouterLink to="/admin/orders" class="z-inline-link">Tất cả đơn</RouterLink>
            </div>
            <div class="table-responsive z-dashboard-table-wrap">
              <table class="z-table">
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="myRecentOrders.length === 0">
                    <td colspan="5" class="text-center py-4" style="color:var(--z-gray)">Chưa có đơn tại quầy nào.</td>
                  </tr>
                  <tr v-for="order in myRecentOrders" :key="order.id">
                    <td style="font-weight:700">{{ order.code }}</td>
                    <td>{{ order.customer }}</td>
                    <td style="font-weight:600">{{ order.total }}</td>
                    <td><span class="z-status" :class="order.statusClass">{{ order.status }}</span></td>
                    <td style="color:var(--z-gray)">{{ order.date }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Tổng quan</h1>
      <p style="font-size:14px;color:var(--z-gray);margin-bottom:24px">Chào mừng trở lại, Admin. Đây là tình hình cửa hàng hôm nay.</p>

      <div v-if="dashboardError" class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
        <i class="bi bi-exclamation-triangle"></i>
        <span>{{ dashboardError }}</span>
      </div>

      <div v-if="lowStockVariants.length" class="z-admin-card mb-4" style="background:#FFFBEB;border:1px solid #FCD34D;padding:14px 20px">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div class="d-flex align-items-center gap-3">
            <div style="width:38px;height:38px;border-radius:50%;background:#F59E0B;color:#fff" class="d-flex align-items-center justify-content-center">
              <i class="bi bi-exclamation-triangle-fill" style="font-size:18px"></i>
            </div>
            <div>
              <strong style="font-size:14px;color:#92400E">Cảnh báo tồn kho: Có {{ lowStockVariants.length }} sản phẩm/biến thể sắp hết hàng (dưới 5 chiếc)</strong>
              <div style="font-size:12px;color:#B45309">Vui lòng kiểm tra kho và cập nhật số lượng để đảm bảo duy trì bán hàng POS & Online.</div>
            </div>
          </div>
          <RouterLink to="/admin/products" class="btn btn-warning btn-sm fw-bold" style="font-size:12px;color:#78350F">
            <i class="bi bi-box-seam me-1"></i>Kiểm tra kho ngay
          </RouterLink>
        </div>
      </div>

      <div class="row g-3 mb-4">
        <div v-for="stat in stats" :key="stat.label" class="col-12 col-sm-6 col-xl-3">
          <div class="z-stat-card">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="z-stat-label">{{ stat.label }}</span>
              <i class="bi" :class="stat.icon" :style="{ color: stat.color, fontSize: '18px' }"></i>
            </div>
            <div class="z-stat-value">{{ stat.value }}</div>
            <div class="z-stat-change" :style="{ color: stat.up ? '#16a34a' : 'var(--z-accent)' }">
              <i class="bi" :class="stat.up ? 'bi-arrow-up-right' : 'bi-arrow-down-right'"></i>
              {{ stat.change }}
            </div>
          </div>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-lg-8">
          <div class="z-admin-card">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="z-admin-card-title">Đơn hàng gần đây</h3>
              <RouterLink to="/admin/orders" class="z-inline-link">Xem tất cả <i class="bi bi-arrow-right"></i></RouterLink>
            </div>
            <div class="table-responsive z-dashboard-table-wrap">
              <table class="z-table">
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in recentOrders" :key="order.id">
                    <td style="font-weight:600">
                      <div class="d-flex align-items-center gap-1">
                        <span>{{ order.code }}</span>
                        <button type="button" class="btn btn-sm btn-light border py-0 px-1" style="font-size:10px" title="Sao chép mã đơn" @click.stop="copyOrderCode(order.code)">
                          <i class="bi bi-clipboard"></i>
                        </button>
                      </div>
                    </td>
                    <td>{{ order.customer }}</td>
                    <td style="font-weight:500">{{ order.total }}</td>
                    <td><span class="z-status" :class="order.statusClass">{{ order.status }}</span></td>
                    <td style="color:var(--z-gray)">{{ order.date }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3">Sản phẩm nổi bật</h3>
            <div v-if="topProducts.length" class="d-flex flex-column gap-3">
              <div v-for="(p, i) in topProducts" :key="p.name" class="z-top-product-row">
                <div class="z-top-product-rank">#{{ i + 1 }}</div>
                <div class="z-top-product-thumb">
                  <img v-if="p.image" :src="p.image" :alt="p.name" style="width:100%;height:100%;object-fit:cover">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                       :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
                    {{ p.letter }}
                  </div>
                </div>
                <div class="z-top-product-copy">
                  <div class="z-top-product-name">{{ p.name }}</div>
                  <div class="z-top-product-sold">Đã bán: {{ p.sold }}</div>
                  <div class="z-top-product-revenue">
                    <span>Doanh thu</span>
                    <strong>{{ p.revenue }}</strong>
                  </div>
                </div>
              </div>
            </div>
            <div v-else style="font-size:13px;color:var(--z-gray)">
              Chưa có đơn hoàn thành để xếp hạng sản phẩm.
            </div>
          </div>

          <div class="z-admin-card mt-3">
            <h3 class="z-admin-card-title mb-3">Tồn kho thấp</h3>
            <div v-if="lowStockVariants.length" class="d-flex flex-column gap-3">
              <div v-for="v in lowStockVariants" :key="v.variantId" class="d-flex align-items-center justify-content-between gap-3">
                <div class="d-flex align-items-center gap-3" style="min-width:0">
                  <div class="z-dashboard-product-thumb">
                    <img v-if="v.image" :src="v.image" :alt="v.tenSanPham">
                    <i v-else class="bi bi-image"></i>
                  </div>
                  <div style="min-width:0">
                    <div class="z-dashboard-product-name">{{ v.tenSanPham }}</div>
                    <div style="font-size:12px;color:var(--z-gray)">
                      {{ v.maSanPham }} · {{ v.mauSac || 'N/A' }} · Size {{ v.kichThuoc || 'N/A' }}
                    </div>
                  </div>
                </div>
                <span class="z-status danger">Còn {{ v.soLuong }}</span>
              </div>
            </div>
            <div v-else style="font-size:13px;color:var(--z-gray)">Chưa có biến thể tồn kho thấp.</div>
          </div>
        </div>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api, useAuth } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const { getUser } = useAuth()

function copyOrderCode(codeVal) {
  if (!codeVal) return
  navigator.clipboard.writeText(codeVal)
  showToast(`Đã sao chép mã đơn hàng "${codeVal}"!`)
}
const currentUser = computed(() => getUser() || {})
const isStaffDashboard = computed(() => currentUser.value.role !== 'Admin')
const userName = computed(() => currentUser.value.hoVaTen || currentUser.value.username || 'Nhân viên')

const statusMap = {
  0: { text: 'Chờ xử lý', cls: 'pending' },
  1: { text: 'Đã xác nhận', cls: 'warning' },
  2: { text: 'Đang chuẩn bị', cls: 'info' },
  3: { text: 'Đang giao', cls: 'primary' },
  4: { text: 'Hoàn thành', cls: 'success' },
  5: { text: 'Đã huỷ', cls: 'danger' },
  6: { text: 'Giao thất bại', cls: 'danger' },
  7: { text: 'Thanh toán thất bại', cls: 'danger' },
}

const stats = ref([
  { label: 'Doanh thu', value: '...', change: '', up: true, icon: 'bi-graph-up', color: '#16a34a' },
  { label: 'Đơn hàng', value: '...', change: '', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
  { label: 'Khách hàng', value: '...', change: '', up: true, icon: 'bi-people', color: '#6366f1' },
  { label: 'Sản phẩm', value: '...', change: '', up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
])
const recentOrders = ref([])
const topProducts = ref([])
const lowStockVariants = ref([])
const dashboardError = ref('')
const staffStats = ref([])
const todayShifts = ref([])
const myRecentOrders = ref([])
const staffCanOperate = ref(false)
const staffShiftReason = ref('Bạn chưa check-in ca làm')

onMounted(async () => {
  if (isStaffDashboard.value) await loadStaffDashboard()
  else await loadAdminDashboard()
})

async function loadAdminDashboard() {
  dashboardError.value = ''
  try {
    const [s, ordersPage] = await Promise.all([
      api().getDashboardStats().catch(() => null),
      api().getHoaDonPage({ page: 0, size: 6 }).catch(() => null)
    ])
    if (s && s.doanhThu !== undefined) {
      stats.value = [
        { label: 'Doanh thu', value: fmtPrice(s.doanhThu), change: `${s.tongLoaiSanPham} loại sản phẩm`, up: true, icon: 'bi-graph-up', color: '#16a34a' },
        { label: 'Đơn hàng', value: String(s.tongDonHang), change: 'Tổng đơn trong hệ thống', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
        { label: 'Khách hàng', value: String(s.tongKhachHang), change: 'Tài khoản khách hàng', up: true, icon: 'bi-people', color: '#6366f1' },
        { label: 'Sản phẩm', value: String(s.tongSanPham), change: `${s.tongBienThe || 0} biến thể`, up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
      ]
      recentOrders.value = (ordersPage?.content || []).map(mapOrder)
      lowStockVariants.value = (s.lowStockVariants || []).map(v => ({ ...v, image: v.anhUrl || null }))
      const topSelling = s.topSellingProducts || []
      topProducts.value = topSelling.map((p, i) => ({
        name: p.tenSanPham,
        sold: String(p.soLuongBan || 0),
        revenue: fmtPrice(p.doanhThu || 0),
        letter: (p.tenSanPham || 'Z').charAt(0),
        bg: ['#D4A99E', '#C4A98E', '#A8A49E'][i % 3],
        image: p.anhUrl || null
      }))
    } else {
      throw new Error('API dashboard không trả về dữ liệu hợp lệ')
    }
  } catch (e) {
    stats.value = stats.value.map(item => ({ ...item, value: '—', change: 'Chưa tải được dữ liệu' }))
    recentOrders.value = []
    topProducts.value = []
    lowStockVariants.value = []
    dashboardError.value = 'Không thể tải số liệu quản trị. Hãy kiểm tra kết nối backend rồi thử lại.'
  }
}

async function loadStaffDashboard() {
  try {
    const today = inputDate(new Date())
    const [shiftStatus, shifts] = await Promise.all([
      api().getWorkShiftStatus(),
      api().getLichLamViec({ startDate: today, endDate: today, nhanVienId: currentUser.value.userId })
    ])
    staffCanOperate.value = Boolean(shiftStatus?.canOperate)
    staffShiftReason.value = shiftStatus?.reason || 'Bạn chưa check-in ca làm'
    todayShifts.value = shifts
    if (!staffCanOperate.value) {
      staffStats.value = []
      myRecentOrders.value = []
      return
    }

    const staffSummary = await api().getStaffDashboard()
    myRecentOrders.value = (staffSummary?.recentOrders || []).map(mapOrder)
    staffStats.value = [
      { label: 'Ca hôm nay', value: String(shifts.length), hint: shifts.length ? 'Đã có lịch làm việc' : 'Chưa có lịch', icon: 'bi-calendar2-week' },
      { label: 'Đơn đã tạo', value: String(staffSummary?.todayOrderCount || 0), hint: 'Tính trong hôm nay', icon: 'bi-receipt-cutoff' },
      { label: 'Doanh số tại quầy', value: fmtPrice(staffSummary?.todayPosRevenue || 0), hint: 'Đơn POS đã thanh toán', icon: 'bi-cash-stack' },
      { label: 'Đơn chờ xử lý', value: String(staffSummary?.pendingOrderCount || 0), hint: 'Cần xác nhận trong hệ thống', icon: 'bi-hourglass-split' },
    ]
  } catch (e) {
    console.error('Staff dashboard load failed:', e)
    staffStats.value = [
      { label: 'Ca hôm nay', value: '0', hint: 'Không tải được dữ liệu', icon: 'bi-calendar2-week' },
      { label: 'Đơn đã tạo', value: '0', hint: 'Không tải được dữ liệu', icon: 'bi-receipt-cutoff' },
      { label: 'Doanh số tại quầy', value: fmtPrice(0), hint: 'Không tải được dữ liệu', icon: 'bi-cash-stack' },
      { label: 'Đơn chờ xử lý', value: '0', hint: 'Không tải được dữ liệu', icon: 'bi-hourglass-split' },
    ]
  }
}

function mapOrder(o) {
  const st = statusMap[o.trangThai] || statusMap[0]
  return {
    id: o.id,
    code: o.maHoaDon,
    customer: o.khachHang || o.tenKhachHang || 'Khách lẻ',
    total: fmtPrice(o.tongTien),
    status: st.text,
    statusClass: st.cls,
    date: o.ngayTao ? new Date(o.ngayTao).toLocaleDateString('vi-VN') : ''
  }
}

function inputDate(date) {
  return new Date(date).toISOString().slice(0, 10)
}

function shortTime(value) {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}
</script>

<style scoped>
.z-stat-card {
  display: block;
  height: 100%;
  min-height: 148px;
  padding: 20px;
}
.z-stat-label {
  color: var(--z-gray);
  font-size: 12px;
  font-weight: 600;
}
.z-stat-value {
  color: var(--z-dark);
  font-family: var(--z-font-body);
  font-size: 30px;
  font-weight: 600;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.z-stat-change {
  margin-top: 8px;
  color: var(--z-gray);
  font-size: 12px;
  line-height: 1.45;
}
.z-staff-stat i {
  color: var(--z-accent);
  font-size: 20px;
}
.z-inline-link {
  color: var(--z-accent);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}
.z-shift-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-shift-row:last-child {
  border-bottom: none;
}
.z-empty-state {
  min-height: 180px;
  display: grid;
  place-items: center;
  gap: 8px;
  color: var(--z-gray);
  text-align: center;
  font-size: 14px;
}
.z-empty-state i {
  color: var(--z-accent);
  font-size: 34px;
}
.z-dashboard-product-thumb {
  width: 44px;
  height: 52px;
  flex: 0 0 44px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  color: var(--z-gray-light);
}
.z-dashboard-product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.z-dashboard-product-name {
  overflow: hidden;
  color: var(--z-dark);
  font-size: 13px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-dashboard-table-wrap {
  width: 100%;
  overscroll-behavior-inline: contain;
}
.z-dashboard-table-wrap .z-table {
  min-width: 680px;
}
.z-top-product-row {
  display: grid;
  grid-template-columns: 20px 44px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}
.z-top-product-rank { color: var(--z-gray-light); font-size: 14px; font-weight: 700; }
.z-top-product-thumb {
  width: 44px;
  height: 56px;
  overflow: hidden;
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
}
.z-top-product-copy { min-width: 0; }
.z-top-product-name {
  display: -webkit-box;
  overflow: hidden;
  color: var(--z-dark);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.z-top-product-sold { margin-top: 2px; color: var(--z-gray); font-size: 11px; }
.z-top-product-revenue { margin-top: 4px; text-align: left; }
.z-top-product-revenue span, .z-top-product-revenue strong { display: block; }
.z-top-product-revenue span { margin-bottom: 2px; color: var(--z-gray); font-size: 9px; text-transform: uppercase; }
.z-top-product-revenue strong { color: var(--z-dark); font-size: 12px; font-weight: 650; }
@media (max-width: 575px) {
  .z-stat-card {
    min-height: 142px;
    padding: 16px;
  }
  .z-stat-value {
    font-size: 26px;
  }
  .z-admin-card {
    padding: 18px;
  }
}
</style>
