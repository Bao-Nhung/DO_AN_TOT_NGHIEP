<template>
  <AdminLayout>
    <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Tổng quan</h1>
    <p style="font-size:14px;color:var(--z-gray);margin-bottom:24px">Chào mừng trở lại, Admin. Đây là tình hình cửa hàng hôm nay.</p>

    <!-- Stats -->
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
            {{ stat.change }} so với tháng trước
          </div>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <!-- Recent orders -->
      <div class="col-lg-8">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title">Đơn hàng gần đây</h3>
            <RouterLink to="/admin/orders" style="font-size:13px;color:var(--z-accent);font-weight:500;text-decoration:none">
              Xem tất cả <i class="bi bi-arrow-right"></i>
            </RouterLink>
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

      <!-- Top products -->
      <div class="col-lg-4">
        <div class="z-admin-card">
          <h3 class="z-admin-card-title mb-3">Sản phẩm bán chạy</h3>
          <div class="d-flex flex-column gap-3">
            <div v-for="(p, i) in topProducts" :key="p.name"
                 class="d-flex align-items-center gap-3">
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
                <div style="font-size:12px;color:var(--z-gray)">{{ p.sold }} đã bán</div>
              </div>
              <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ p.revenue }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { fmtPrice, mapProduct } from '@/composables/useProducts'

const statusMap = { 0: { text: 'Chờ xử lý', cls: 'pending' }, 1: { text: 'Xác nhận', cls: 'warning' }, 2: { text: 'Đang giao', cls: 'info' }, 3: { text: 'Hoàn thành', cls: 'success' }, 4: { text: 'Đã huỷ', cls: 'danger' } }

const stats = ref([
  { label: 'Doanh thu', value: '...', change: '', up: true, icon: 'bi-graph-up', color: '#16a34a' },
  { label: 'Đơn hàng', value: '...', change: '', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
  { label: 'Khách hàng', value: '...', change: '', up: true, icon: 'bi-people', color: '#6366f1' },
  { label: 'Sản phẩm', value: '...', change: '', up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
])
const recentOrders = ref([])
const topProducts = ref([])

onMounted(async () => {
  try {
    const [s, orders, prods] = await Promise.all([
      api().getDashboardStats(),
      api().getHoaDon(),
      api().getVay()
    ])
    stats.value = [
      { label: 'Doanh thu', value: fmtPrice(s.doanhThu), change: s.tongLoaiVay + ' loại váy', up: true, icon: 'bi-graph-up', color: '#16a34a' },
      { label: 'Đơn hàng', value: String(s.tongDonHang), change: '', up: true, icon: 'bi-receipt', color: 'var(--z-accent)' },
      { label: 'Khách hàng', value: String(s.tongKhachHang), change: '', up: true, icon: 'bi-people', color: '#6366f1' },
      { label: 'Sản phẩm', value: String(s.tongSanPham), change: s.tongLoaiVay + ' loại', up: true, icon: 'bi-bag', color: 'var(--z-warm)' },
    ]

    const sortedOrders = [...orders].sort((a, b) => {
      const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return db - da
    })
    recentOrders.value = sortedOrders.slice(0, 6).map(o => {
      const st = statusMap[o.trangThai] || statusMap[0]
      return {
        id: o.maHoaDon, customer: o.khachHang || 'N/A',
        total: fmtPrice(o.tongTien), status: st.text, statusClass: st.cls,
        date: o.ngayTao ? new Date(o.ngayTao).toLocaleDateString('vi-VN') : ''
      }
    })

    const mapped = prods.map(mapProduct).sort((a, b) => b.stock - a.stock)
    topProducts.value = mapped.slice(0, 5).map(p => ({
      name: p.name, sold: String(p.stock), revenue: fmtPrice(p.price * p.stock),
      letter: p.letter, bg: p.bg, image: p.image || null
    }))
  } catch (e) { console.error('Dashboard load failed:', e) }
})
</script>

<style>
.z-stat-card {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius-lg);
  padding: 20px;
}
.z-stat-label { font-size: 13px; color: var(--z-gray); font-weight: 500; }
.z-stat-value { font-size: 28px; font-weight: 700; color: var(--z-dark); font-family: var(--z-font-body); margin-bottom: 4px; }
.z-stat-change { font-size: 12px; font-weight: 500; display: flex; align-items: center; gap: 4px; }
.z-admin-card {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius-lg);
  padding: 20px;
}
.z-admin-card-title { font-size: 16px; font-weight: 600; color: var(--z-dark); margin: 0; }
.z-table { width: 100%; border-collapse: collapse; }
.z-table th {
  font-size: 12px; font-weight: 600; color: var(--z-gray);
  text-align: left; padding: 10px 12px;
  border-bottom: 1px solid var(--z-gray-border);
}
.z-table td {
  font-size: 13px; color: var(--z-dark);
  padding: 12px; border-bottom: 1px solid var(--z-gray-border);
}
.z-table tr:last-child td { border-bottom: none; }
.z-table tr:hover td { background: var(--z-bg-alt); }
.z-status {
  font-size: 11px; font-weight: 600;
  padding: 4px 10px; border-radius: 20px;
  display: inline-block;
}
.z-status.success { background: #dcfce7; color: #16a34a; }
.z-status.info    { background: #dbeafe; color: #2563eb; }
.z-status.warning { background: var(--z-warm-light); color: #92400e; }
.z-status.pending { background: var(--z-bg-alt); color: var(--z-gray); }
.z-status.danger  { background: #fee2e2; color: #dc2626; }
</style>
