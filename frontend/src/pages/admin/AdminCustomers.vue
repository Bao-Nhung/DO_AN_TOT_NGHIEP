<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý khách hàng</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ customers.length }} khách hàng</p>
      </div>
    </div>

    <!-- Search -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-2" style="max-width:400px">
        <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
        <input v-model="search" class="lm-input" placeholder="Tìm theo tên, email, SĐT..." style="border:none;padding:8px 0;box-shadow:none">
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Khách hàng</th>
            <th>Số điện thoại</th>
            <th>Email</th>
            <th>Tổng đơn</th>
            <th>Tổng chi tiêu</th>
            <th>Ngày tham gia</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filteredCustomers" :key="c.id">
            <td>
              <div class="d-flex align-items-center gap-3">
                <div :style="{ width:'36px', height:'36px', borderRadius:'50%', background: c.color, display:'flex', alignItems:'center', justifyContent:'center', color:'var(--z-white)', fontWeight:600, fontSize:'13px', flexShrink:0 }">
                  {{ c.name.charAt(0) }}
                </div>
                <div>
                  <div style="font-weight:500">{{ c.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ c.code }}</div>
                </div>
              </div>
            </td>
            <td>{{ c.phone }}</td>
            <td style="color:var(--z-gray)">{{ c.email }}</td>
            <td style="font-weight:500">{{ c.orders }}</td>
            <td style="font-weight:600">{{ c.spent }}</td>
            <td style="color:var(--z-gray)">{{ c.date }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'

const avatarColors = ['var(--z-accent)', '#6366f1', 'var(--z-warm)', '#16a34a', '#2563eb', '#9333ea']
const search = ref('')
const customers = ref([])

onMounted(async () => {
  try {
    const data = await api().getKhachHang()
    customers.value = data.map((c, i) => ({
      id: c.id, code: c.maKhachHang || '', name: c.hoVaTen || '', phone: c.soDienThoai || '',
      email: c.email || '', orders: c.tongDon || 0, spent: fmtPrice(c.tongChiTieu),
      date: c.ngayTao ? new Date(c.ngayTao).toLocaleDateString('vi-VN') : '',
      color: avatarColors[i % avatarColors.length]
    }))
  } catch (e) { console.error('Không thể tải khách hàng:', e) }
})

const filteredCustomers = computed(() => {
  if (!search.value) return customers.value
  const q = search.value.toLowerCase()
  return customers.value.filter(c =>
    c.name.toLowerCase().includes(q) ||
    c.email.toLowerCase().includes(q) ||
    c.phone.includes(q) ||
    c.code.toLowerCase().includes(q)
  )
})
</script>
