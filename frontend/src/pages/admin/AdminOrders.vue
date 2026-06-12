<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý đơn hàng</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredOrders.length }} đơn hàng</p>
      </div>
    </div>

    <!-- Status tabs -->
    <div class="d-flex gap-2 mb-3 flex-wrap">
      <button v-for="tab in statusTabs" :key="tab.value"
              class="z-tab" :class="{ active: activeStatus === tab.value }"
              @click="activeStatus = tab.value">
        {{ tab.label }}
        <span class="z-tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- Search -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-2" style="max-width:400px">
        <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
        <input v-model="search" class="lm-input" placeholder="Tìm theo mã đơn, tên khách..." style="border:none;padding:8px 0;box-shadow:none">
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Sản phẩm</th>
            <th>Tổng tiền</th>
            <th>Thanh toán</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in filteredOrders" :key="o.id">
            <td style="font-weight:600">{{ o.id }}</td>
            <td>
              <div style="font-weight:500">{{ o.customer }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ o.phone }}</div>
            </td>
            <td>{{ o.items }} sản phẩm</td>
            <td style="font-weight:600">{{ o.total }}</td>
            <td>{{ o.payment }}</td>
            <td><span class="z-status" :class="o.statusClass">{{ o.status }}</span></td>
            <td style="color:var(--z-gray)">{{ o.date }}</td>
            <td>
              <select class="lm-input" style="padding:6px 10px;font-size:12px"
                      :value="o.statusValue" @change="updateStatus(o, $event)">
                <option value="0">Chờ xử lý</option>
                <option value="1">Xác nhận</option>
                <option value="2">Đang giao</option>
                <option value="3">Hoàn thành</option>
                <option value="4">Huỷ</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredOrders.length === 0" class="text-center py-5">
        <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có đơn hàng nào</p>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'

const statusMap = { 0: { text: 'Chờ xử lý', cls: 'pending' }, 1: { text: 'Xác nhận', cls: 'warning' }, 2: { text: 'Đang giao', cls: 'info' }, 3: { text: 'Hoàn thành', cls: 'success' }, 4: { text: 'Đã huỷ', cls: 'danger' } }

const search = ref('')
const activeStatus = ref('all')
const allOrders = ref([])

onMounted(async () => {
  try {
    const data = await api().getHoaDon()
    allOrders.value = data.map(o => {
      const st = statusMap[o.trangThai] || statusMap[0]
      return {
        id: o.maHoaDon, dbId: o.id, customer: o.khachHang || 'N/A', phone: o.soDienThoai || '',
        items: o.soSanPham || 0, total: fmtPrice(o.tongTien), payment: o.hinhThucThanhToan || 'N/A',
        status: st.text, statusClass: st.cls, statusValue: String(o.trangThai ?? 0),
        date: o.ngayTao ? new Date(o.ngayTao).toLocaleDateString('vi-VN') : ''
      }
    })
  } catch (e) { console.error('Không thể tải đơn hàng:', e) }
})

const statusTabs = computed(() => [
  { label: 'Tất cả',      value: 'all',    count: allOrders.value.length },
  { label: 'Chờ xử lý',   value: '0',      count: allOrders.value.filter(o => o.statusValue === '0').length },
  { label: 'Xác nhận',     value: '1',      count: allOrders.value.filter(o => o.statusValue === '1').length },
  { label: 'Đang giao',    value: '2',      count: allOrders.value.filter(o => o.statusValue === '2').length },
  { label: 'Hoàn thành',   value: '3',      count: allOrders.value.filter(o => o.statusValue === '3').length },
  { label: 'Đã huỷ',       value: '4',      count: allOrders.value.filter(o => o.statusValue === '4').length },
])

const filteredOrders = computed(() => {
  return allOrders.value.filter(o => {
    const matchSearch = !search.value || o.id.toLowerCase().includes(search.value.toLowerCase()) || o.customer.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = activeStatus.value === 'all' || o.statusValue === activeStatus.value
    return matchSearch && matchStatus
  })
})

async function updateStatus(order, event) {
  const newVal = Number(event.target.value)
  try {
    await api().updateOrderStatus(order.dbId, newVal)
    const st = statusMap[newVal] || statusMap[0]
    order.statusValue = String(newVal)
    order.status = st.text
    order.statusClass = st.cls
  } catch (e) { console.error('Lỗi cập nhật trạng thái:', e) }
}
</script>

<style scoped>
.z-tab {
  padding: 8px 16px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: 20px;
  font-size: 13px; font-weight: 500; color: var(--z-gray);
  cursor: pointer; transition: all 0.2s;
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--z-font-body);
}
.z-tab:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-tab.active { background: var(--z-dark); color: var(--z-white); border-color: var(--z-dark); }
.z-tab-count { font-size: 11px; font-weight: 700; opacity: 0.7; }
</style>
