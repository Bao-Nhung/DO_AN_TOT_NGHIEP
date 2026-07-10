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
        <div class="d-flex gap-2">
          <RouterLink to="/admin/pos" class="lm-btn-primary"><span><i class="bi bi-shop me-1"></i>Bán tại quầy</span></RouterLink>
          <RouterLink to="/admin/orders" class="lm-btn-secondary">Xem đơn hàng</RouterLink>
        </div>
      </div>

      <div class="row g-3 mb-4">
        <div v-for="stat in staffStats" :key="stat.label" class="col-6 col-xl-3">
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
        <div class="col-xl-5">
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

        <div class="col-xl-7">
          <div class="z-admin-card h-100">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h3 class="z-admin-card-title">Đơn tại quầy gần đây</h3>
              <RouterLink to="/admin/orders" class="z-inline-link">Tất cả đơn</RouterLink>
            </div>
            <div class="table-responsive">
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

      <div class="row g-3 mb-4">
        <div v-for="stat in stats" :key="stat.label" class="col-6 col-xl-3">
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
                  <td style="font-weight:600">{{ order.id }}</td>
                  <td>{{ order.customer }}</td>
                  <td style="font-weight:500">{{ order.total }}</td>
                  <td><span class="z-status" :class="order.statusClass">{{ order.status }}</span></td>
                  <td style="color:var(--z-gray)">{{ order.date }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3">Sản phẩm nổi bật</h3>
            <div class="d-flex flex-column gap-3">
              <div v-for="(p, i) in topProducts" :key="p.name" class="d-flex align-items-center gap-3">
                <div style="width:20px;font-size:14px;font-weight:700;color:var(--z-gray-light)">#{{ i + 1 }}</div>
                <div style="width:44px;height:52px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                  <img v-if="p.image" :src="p.image" style="width:100%;height:100%;object-fit:cover">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                       :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
                    {{ p.letter }}
                  </div>
                </div>
                <div class="flex-grow-1">
                  <div style="font-size:13px;font-weight:500;color:var(--z-dark)">{{ p.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">Đã bán: {{ p.sold }}</div>
                </div>
                <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ p.revenue }}</div>
              </div>
            </div>
          </div>

          <div class="z-admin-card mt-3">
            <h3 class="z-admin-card-title mb-3">Tồn kho thấp</h3>
            <div v-if="lowStockVariants.length" class="d-flex flex-column gap-3">
              <div v-for="v in lowStockVariants" :key="v.variantId" class="d-flex justify-content-between gap-3">
                <div>
                  <div style="font-size:13px;font-weight:500;color:var(--z-dark)">{{ v.tenVay }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">
                    {{ v.maVay }} · {{ v.mauSac || 'N/A' }} · Size {{ v.kichThuoc || 'N/A' }}
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
import { fmtPrice, mapProduct } from '@/composables/useProducts'

const { getUser } = useAuth()
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

statusMap[8] = { text: 'Yêu cầu đổi/trả', cls: 'warning' }
statusMap[9] = { text: 'Đã hoàn tiền', cls: 'danger' }

const stats = ref([
  { label: 'Doanh thu', value: '...', change: '', up: true, icon: 'bi-graph-up', color: '#16a34a' },
  { label: 'Đơn hàng', value: '...', change: '', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
  { label: 'Khách hàng', value: '...', change: '', up: true, icon: 'bi-people', color: '#6366f1' },
  { label: 'Sản phẩm', value: '...', change: '', up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
])
const recentOrders = ref([])
const topProducts = ref([])
const lowStockVariants = ref([])
const staffStats = ref([])
const todayShifts = ref([])
const myRecentOrders = ref([])

onMounted(async () => {
  if (isStaffDashboard.value) await loadStaffDashboard()
  else await loadAdminDashboard()
})

async function loadAdminDashboard() {
  try {
    const [s, orders, prods] = await Promise.all([
      api().getDashboardStats(),
      api().getHoaDon(),
      api().getVay()
    ])
    stats.value = [
      { label: 'Doanh thu', value: fmtPrice(s.doanhThu), change: `${s.tongLoaiVay} loại váy`, up: true, icon: 'bi-graph-up', color: '#16a34a' },
      { label: 'Đơn hàng', value: String(s.tongDonHang), change: 'Tổng đơn trong hệ thống', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
      { label: 'Khách hàng', value: String(s.tongKhachHang), change: 'Tài khoản khách hàng', up: true, icon: 'bi-people', color: '#6366f1' },
      { label: 'Sản phẩm', value: String(s.tongSanPham), change: `${s.tongBienThe || 0} biến thể`, up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
    ]

    recentOrders.value = sortByDate(orders).slice(0, 6).map(mapOrder)

    lowStockVariants.value = s.lowStockVariants || []
    const topSelling = s.topSellingProducts || []
    if (topSelling.length) {
      topProducts.value = topSelling.map((p, i) => ({
        name: p.tenVay,
        sold: String(p.soLuongBan || 0),
        revenue: fmtPrice(p.doanhThu || 0),
        letter: (p.tenVay || 'Z').charAt(0),
        bg: ['#D4A99E', '#C4A98E', '#A8A49E'][i % 3],
        image: null
      }))
    } else {
      const mapped = prods.map(mapProduct).sort((a, b) => b.stock - a.stock)
      topProducts.value = mapped.slice(0, 5).map(p => ({
        name: p.name, sold: String(p.stock), revenue: fmtPrice((p.salePrice || p.price) * Math.max(1, p.stock || 1)),
        letter: p.letter, bg: p.bg, image: p.image || null
      }))
    }
  } catch (e) {
    console.error('Dashboard load failed:', e)
  }
}

async function loadStaffDashboard() {
  try {
    const today = inputDate(new Date())
    const [orders, shifts] = await Promise.all([
      api().getHoaDon(),
      api().getLichLamViec({ startDate: today, endDate: today, nhanVienId: currentUser.value.userId })
    ])
    const userId = Number(currentUser.value.userId)
    const myOrders = orders.filter(o =>
      Number(o.nhanVienId) === userId ||
      (o.nhanVien && o.nhanVien === userName.value) ||
      (o.nguoiTaoDon && o.nguoiTaoDon === userName.value)
    )
    const todayOrders = myOrders.filter(o => isSameDay(o.ngayTao, today))
    const todayPosOrders = todayOrders.filter(o => Number(o.hinhThucNhanHang) === 0)
    const revenue = todayPosOrders
      .filter(o => Number(o.trangThai) === 4 || o.daThanhToan)
      .reduce((sum, o) => sum + Number(o.tongTien || 0), 0)
    const pendingOrders = orders.filter(o => Number(o.trangThai) === 0)

    todayShifts.value = shifts
    myRecentOrders.value = sortByDate(myOrders).slice(0, 6).map(mapOrder)
    staffStats.value = [
      { label: 'Ca hôm nay', value: String(shifts.length), hint: shifts.length ? 'Đã có lịch làm việc' : 'Chưa có lịch', icon: 'bi-calendar2-week' },
      { label: 'Đơn đã tạo', value: String(todayOrders.length), hint: 'Tính trong hôm nay', icon: 'bi-receipt-cutoff' },
      { label: 'Doanh số tại quầy', value: fmtPrice(revenue), hint: 'Đơn POS đã thanh toán', icon: 'bi-cash-stack' },
      { label: 'Đơn chờ xử lý', value: String(pendingOrders.length), hint: 'Cần xác nhận trong hệ thống', icon: 'bi-hourglass-split' },
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

function sortByDate(list) {
  return [...(list || [])].sort((a, b) => {
    const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    return db - da
  })
}

function inputDate(date) {
  return new Date(date).toISOString().slice(0, 10)
}

function isSameDay(value, today) {
  if (!value) return false
  return inputDate(value) === today
}

function shortTime(value) {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}
</script>

<style scoped>
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
</style>
