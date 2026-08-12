<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
      <div>
        <h1 class="z-display mb-1 page-title">Báo cáo & Phân tích kinh doanh</h1>
        <p class="page-subtitle">Số liệu được tổng hợp từ các đơn hàng hoàn thành trong khoảng đã chọn.</p>
      </div>
      <div class="d-flex gap-2">
        <button type="button" class="btn btn-outline-dark btn-sm d-flex align-items-center gap-2" :disabled="loading || !hasData" @click="exportCsv">
          <i class="bi bi-file-earmark-spreadsheet"></i> Xuất CSV
        </button>
        <button type="button" class="btn btn-dark btn-sm d-flex align-items-center gap-2" :disabled="loading" @click="printReport">
          <i class="bi bi-printer"></i> In báo cáo
        </button>
      </div>
    </div>

    <div class="z-admin-card mb-4 filter-panel">
      <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
        <div class="d-flex gap-2 flex-wrap">
          <button
            v-for="preset in presets"
            :key="preset.id"
            type="button"
            class="lm-filter-tag"
            :class="{ active: activePreset === preset.id }"
            @click="applyPreset(preset.id)"
          >
            <i class="bi" :class="preset.icon"></i> {{ preset.label }}
          </button>
        </div>

        <div class="d-flex align-items-center gap-2 flex-wrap">
          <input v-model="filter.startDate" type="date" class="form-control form-control-sm date-input" aria-label="Từ ngày" @change="activePreset = ''" />
          <span class="text-muted small">đến</span>
          <input v-model="filter.endDate" type="date" class="form-control form-control-sm date-input" aria-label="Đến ngày" @change="activePreset = ''" />
          <select v-model="filter.timeType" class="form-select form-select-sm grouping-select" aria-label="Cách nhóm biểu đồ">
            <option value="ngay">Nhóm theo ngày</option>
            <option value="thang">Nhóm theo tháng</option>
          </select>
          <button class="btn btn-dark btn-sm" :disabled="loading" @click="loadThongKe">
            <span v-if="loading" class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
            <i v-else class="bi bi-filter me-1"></i>Lọc
          </button>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger d-flex align-items-center gap-2" role="alert">
      <i class="bi bi-exclamation-circle"></i>
      <span>{{ errorMessage }}</span>
      <button type="button" class="btn btn-sm btn-outline-danger ms-auto" @click="loadThongKe">Thử lại</button>
    </div>

    <div class="row g-3 mb-4">
      <div v-for="item in kpiCards" :key="item.label" class="col-12 col-sm-6 col-xl-3">
        <div class="card-box h-100">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <span class="kpi-label">{{ item.label }}</span>
            <div class="z-stat-icon-wrap" :style="{ background: item.bg, color: item.color }">
              <i class="bi" :class="item.icon"></i>
            </div>
          </div>
          <h2 class="kpi-value">{{ item.value }}</h2>
          <span class="text-muted small">{{ item.note }}</span>
        </div>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-lg-8">
        <div class="chart-box h-100">
          <div class="mb-3">
            <h3 class="section-title">Xu hướng doanh thu</h3>
            <small class="text-muted">Đơn vị: VNĐ</small>
          </div>
          <div v-if="lineRevenue.labels.length" class="chart"><LineChartUI :chartData="lineRevenue" /></div>
          <div v-else class="empty-chart">Chưa có doanh thu trong khoảng thời gian này.</div>
        </div>
      </div>
      <div class="col-lg-4">
        <div class="chart-box h-100">
          <h3 class="section-title mb-3">Doanh thu theo danh mục</h3>
          <div v-if="pieCategory.labels.length" class="chart chart-small"><PieChartUI :chartData="pieCategory" /></div>
          <div v-else class="empty-chart">Chưa có dữ liệu danh mục.</div>
        </div>
      </div>
    </div>

    <div class="z-admin-card mb-4 table-card">
      <div class="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
        <div>
          <h3 class="section-title">Top sản phẩm bán chạy</h3>
          <p class="section-note">Chỉ tính các đơn đã hoàn thành trong khoảng đã chọn.</p>
        </div>
        <RouterLink to="/admin/products" class="btn btn-outline-dark btn-sm">Xem kho</RouterLink>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 report-table">
          <thead class="table-light">
            <tr>
              <th>Hạng</th><th>Sản phẩm</th><th>Danh mục</th><th>Giá hiện tại</th>
              <th class="text-center">Đã bán</th><th>Doanh thu sản phẩm</th><th>Tỷ trọng top 5</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(prod, index) in topSellingProductsList" :key="prod.id || prod.code || index">
              <td>#{{ index + 1 }}</td>
              <td>
                <div class="d-flex align-items-center gap-3">
                  <img v-if="prod.image" :src="prod.image" :alt="prod.name" class="product-thumb" @error="prod.image = ''" />
                  <div v-else class="product-placeholder" aria-hidden="true"><i class="bi bi-image"></i></div>
                  <div><strong>{{ prod.name }}</strong><div class="product-code">Mã: {{ prod.code || '—' }}</div></div>
                </div>
              </td>
              <td>{{ prod.category || 'Chưa phân loại' }}</td>
              <td>{{ formatMoney(prod.price) }}</td>
              <td class="text-center"><strong>{{ formatNumber(prod.sold) }}</strong></td>
              <td class="revenue-cell">{{ formatMoney(prod.revenue) }}</td>
              <td>{{ formatPercent(prod.share) }}</td>
            </tr>
            <tr v-if="!topSellingProductsList.length">
              <td colspan="7" class="text-center text-muted py-4">Chưa có sản phẩm bán ra trong khoảng thời gian này.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <div class="col-lg-6">
        <div class="chart-box h-100">
          <h3 class="section-title mb-3">Số lượng bán theo kích thước</h3>
          <div v-if="barSize.labels.length" class="chart"><BarChartUI :chartData="barSize" /></div>
          <div v-else class="empty-chart">Chưa có dữ liệu kích thước.</div>
        </div>
      </div>
      <div class="col-lg-6">
        <div class="chart-box h-100">
          <h3 class="section-title mb-3">Số lượng bán theo màu sắc</h3>
          <div v-if="barColor.labels.length" class="chart"><BarChartUI :chartData="barColor" /></div>
          <div v-else class="empty-chart">Chưa có dữ liệu màu sắc.</div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import LineChartUI from '@/components/charts/LineChartUI.vue'
import BarChartUI from '@/components/charts/BarChartUI.vue'
import PieChartUI from '@/components/charts/PieChartUI.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(false)
const errorMessage = ref('')
const activePreset = ref('thisMonth')
const reportData = ref(null)

const presets = [
  { id: 'today', label: 'Hôm nay', icon: 'bi-calendar-event' },
  { id: '7days', label: '7 ngày qua', icon: 'bi-calendar-week' },
  { id: 'thisMonth', label: 'Tháng này', icon: 'bi-calendar-month' },
  { id: 'thisQuarter', label: 'Quý này', icon: 'bi-pie-chart' }
]

const toDateInput = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const today = new Date()
const filter = ref({
  startDate: toDateInput(new Date(today.getFullYear(), today.getMonth(), 1)),
  endDate: toDateInput(today),
  timeType: 'ngay'
})

const kpiCards = ref([])
const lineRevenue = ref({ labels: [], datasets: [] })
const barSize = ref({ labels: [], datasets: [] })
const barColor = ref({ labels: [], datasets: [] })
const pieCategory = ref({ labels: [], datasets: [] })
const topSellingProductsList = ref([])
const hasData = computed(() => Number(reportData.value?.tongQuan?.tongDonHang || 0) > 0)

const chartColors = ['#1f2937', '#d4564e', '#2563eb', '#15803d', '#d97706', '#7c3aed', '#0e7490', '#be123c']

function applyPreset(id) {
  activePreset.value = id
  const now = new Date()
  let start = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  if (id === '7days') start.setDate(start.getDate() - 6)
  if (id === 'thisMonth') start = new Date(now.getFullYear(), now.getMonth(), 1)
  if (id === 'thisQuarter') start = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1)
  filter.value.startDate = toDateInput(start)
  filter.value.endDate = toDateInput(now)
  filter.value.timeType = id === 'thisQuarter' ? 'thang' : 'ngay'
  loadThongKe()
}

function endExclusive(dateText) {
  const [year, month, day] = dateText.split('-').map(Number)
  const date = new Date(year, month - 1, day + 1)
  return `${toDateInput(date)}T00:00`
}

function resetReport() {
  reportData.value = null
  kpiCards.value = [
    makeKpi('Doanh thu thuần', formatMoney(0), 'Đơn đã hoàn thành', 'bi-currency-dollar', '#eef2ff', '#3730a3'),
    makeKpi('Đơn hàng hoàn tất', '0 / 0', 'Hoàn tất / tổng đơn', 'bi-check-circle', '#ecfdf5', '#047857'),
    makeKpi('Khách hàng mới', '0', 'Tài khoản tạo trong kỳ', 'bi-people', '#fff7ed', '#c2410c'),
    makeKpi('Giá trị đơn trung bình', formatMoney(0), 'Trên đơn hoàn tất', 'bi-cart-check', '#fdf2f8', '#be185d')
  ]
  lineRevenue.value = { labels: [], datasets: [] }
  pieCategory.value = { labels: [], datasets: [] }
  barSize.value = { labels: [], datasets: [] }
  barColor.value = { labels: [], datasets: [] }
  topSellingProductsList.value = []
}

function makeKpi(label, value, note, icon, bg, color) {
  return { label, value, note, icon, bg, color }
}

function mapReport(data) {
  reportData.value = data
  const total = Number(data?.tongQuan?.tongDonHang || 0)
  const completed = Number(data?.tongQuan?.donHangThanhCong || 0)
  const revenue = Number(data?.tongQuan?.doanhThu || 0)
  const newCustomers = Number(data?.tongQuan?.khachHangMoi || 0)
  const aov = completed > 0 ? revenue / completed : 0

  kpiCards.value = [
    makeKpi('Doanh thu thuần', formatMoney(revenue), 'Đơn đã hoàn thành', 'bi-currency-dollar', '#eef2ff', '#3730a3'),
    makeKpi('Đơn hàng hoàn tất', `${formatNumber(completed)} / ${formatNumber(total)}`, 'Hoàn tất / tổng đơn', 'bi-check-circle', '#ecfdf5', '#047857'),
    makeKpi('Khách hàng mới', formatNumber(newCustomers), 'Tài khoản tạo trong kỳ', 'bi-people', '#fff7ed', '#c2410c'),
    makeKpi('Giá trị đơn trung bình', formatMoney(aov), 'Trên đơn hoàn tất', 'bi-cart-check', '#fdf2f8', '#be185d')
  ]

  const revenueTimeline = data?.doanhThuTheoThoiGian || []
  lineRevenue.value = {
    labels: revenueTimeline.map((item) => item.thoiGian),
    datasets: [{ label: 'Doanh thu', data: revenueTimeline.map((item) => Number(item.doanhThu || 0)), borderWidth: 3, borderColor: '#1f2937', backgroundColor: '#1f2937' }]
  }

  const categories = data?.doanhThuDanhMuc || []
  pieCategory.value = {
    labels: categories.map((item) => item.tenDanhMuc),
    datasets: [{ data: categories.map((item) => Number(item.tongDoanhThu || 0)), backgroundColor: chartColors, borderWidth: 2 }]
  }

  const sizes = data?.theoSize || []
  barSize.value = {
    labels: sizes.map((item) => item.ten),
    datasets: [{ label: 'Số lượng bán', data: sizes.map((item) => Number(item.tongSoLuong || 0)), backgroundColor: '#1f2937', borderWidth: 0 }]
  }

  const colors = data?.theoMau || []
  barColor.value = {
    labels: colors.map((item) => item.ten),
    datasets: [{ label: 'Số lượng bán', data: colors.map((item) => Number(item.tongSoLuong || 0)), backgroundColor: colors.map((item, index) => item.maHex || chartColors[index % chartColors.length]), borderColor: '#d1d5db', borderWidth: 1 }]
  }

  const top = data?.topSanPham || []
  const topRevenue = top.reduce((sum, item) => sum + Number(item.doanhThu || 0), 0)
  topSellingProductsList.value = top.map((item) => ({
    id: item.id,
    name: item.ten || 'Sản phẩm',
    code: item.ma,
    category: item.danhMuc,
    price: Number(item.giaBan || 0),
    sold: Number(item.tongSoLuong || 0),
    revenue: Number(item.doanhThu || 0),
    image: item.anhUrl || '',
    share: topRevenue > 0 ? Number(item.doanhThu || 0) * 100 / topRevenue : 0
  }))
}

async function loadThongKe() {
  if (!filter.value.startDate || !filter.value.endDate || filter.value.startDate > filter.value.endDate) {
    showToast('Khoảng ngày thống kê không hợp lệ', 'error')
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await api().getThongKeTongHop({
      startDate: `${filter.value.startDate}T00:00`,
      endDate: endExclusive(filter.value.endDate),
      timeType: filter.value.timeType
    })
    mapReport(data)
  } catch (error) {
    resetReport()
    errorMessage.value = error?.message || 'Không thể tải báo cáo lúc này.'
  } finally {
    loading.value = false
  }
}

function csvCell(value) {
  return `"${String(value ?? '').replaceAll('"', '""')}"`
}

function exportCsv() {
  const rows = [
    ['BÁO CÁO KINH DOANH ZESTIA'],
    [`Từ ${filter.value.startDate} đến ${filter.value.endDate}`],
    [],
    ['Chỉ số', 'Giá trị'],
    ...kpiCards.value.map((item) => [item.label, item.value]),
    [],
    ['Hạng', 'Mã sản phẩm', 'Tên sản phẩm', 'Danh mục', 'Giá hiện tại', 'Đã bán', 'Doanh thu sản phẩm'],
    ...topSellingProductsList.value.map((item, index) => [index + 1, item.code, item.name, item.category, item.price, item.sold, item.revenue])
  ]
  const csv = `\uFEFF${rows.map((row) => row.map(csvCell).join(',')).join('\r\n')}`
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `bao-cao-kinh-doanh-${filter.value.startDate}-${filter.value.endDate}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

function printReport() {
  window.print()
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0))
}

function formatNumber(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}

function formatPercent(value) {
  return `${new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 1 }).format(Number(value || 0))}%`
}

resetReport()
onMounted(loadThongKe)
</script>

<style scoped>
.page-title { font-size: 26px; font-weight: 600; color: var(--z-dark); }
.page-subtitle, .section-note { margin: 0; color: var(--z-gray); font-size: 13px; }
.filter-panel { padding: 16px 20px; }
.lm-filter-tag { padding: 6px 14px; font-size: 12px; font-weight: 600; }
.date-input { width: 142px; }
.grouping-select { width: 155px; }
.card-box, .chart-box { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; box-shadow: 0 3px 10px rgba(0,0,0,.035); }
.card-box { padding: 20px; }
.chart-box { padding: 22px; }
.kpi-label { color: var(--z-gray); font-size: 13px; font-weight: 600; }
.kpi-value { margin: 0 0 8px; color: #111; font-size: 24px; font-weight: 700; }
.z-stat-icon-wrap { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.section-title { margin: 0; font-size: 16px; font-weight: 700; }
.chart { height: 340px; }
.chart-small { height: 300px; }
.empty-chart { min-height: 300px; display: grid; place-items: center; color: #6b7280; text-align: center; }
.table-card { padding: 22px; }
.report-table { font-size: 13px; }
.product-thumb, .product-placeholder { width: 40px; height: 50px; border-radius: 6px; background: #f3f4f6; }
.product-thumb { object-fit: cover; }
.product-placeholder { display: grid; place-items: center; color: #9ca3af; font-size: 18px; }
.product-code { margin-top: 2px; color: #6b7280; font-size: 11px; }
.revenue-cell { color: var(--z-accent); font-weight: 700; }
@media (max-width: 575px) { .date-input, .grouping-select { width: 100%; } }
</style>
