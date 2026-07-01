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
            <th style="width:60px"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in paginatedCustomers" :key="c.id" class="z-clickable-row" @click="openDetail(c)">
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
            <td @click.stop>
              <button class="z-icon-btn" title="Chi tiết" @click="openDetail(c)"><i class="bi bi-eye"></i></button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span style="font-size: 13px; color: var(--z-gray)">
          Hiển thị từ {{ (currentPage - 1) * itemsPerPage + 1 }} đến {{ Math.min(currentPage * itemsPerPage, filteredCustomers.length) }} trong tổng số {{ filteredCustomers.length }} khách hàng
        </span>
        <div class="d-flex gap-2">
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === 1" @click="currentPage--">
            Trước
          </button>
          <button v-for="page in totalPages" :key="page" 
                  class="lm-btn-secondary" 
                  :style="{
                    padding:'6px 12px', fontSize:'12px', height:'auto', borderRadius:'6px',
                    background: currentPage === page ? 'var(--z-dark)' : '',
                    color: currentPage === page ? '#fff' : '',
                    borderColor: currentPage === page ? 'var(--z-dark)' : ''
                  }"
                  @click="currentPage = page">
            {{ page }}
          </button>
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === totalPages" @click="currentPage++">
            Sau
          </button>
        </div>
      </div>
    </div>

    <!-- Customer Detail Modal -->
    <div v-if="showDetail" class="z-modal-overlay" @click.self="showDetail = false">
      <div class="z-modal" style="max-width:600px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết khách hàng</h3>
          <button class="z-icon-btn" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="selectedCustomer" class="text-center mb-4">
          <div :style="{ width:'64px', height:'64px', borderRadius:'50%', background: selectedCustomer.color, display:'flex', alignItems:'center', justifyContent:'center', color:'var(--z-white)', fontWeight:600, fontSize:'24px', margin:'0 auto 12px' }">
            {{ selectedCustomer.name.charAt(0) }}
          </div>
          <h4 style="font-size:20px;font-weight:600;margin-bottom:4px">{{ selectedCustomer.name }}</h4>
          <div style="font-size:13px;color:var(--z-gray)">{{ selectedCustomer.code }}</div>
        </div>

        <div v-if="selectedCustomer" class="row g-3 mb-4">
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Email</div>
              <div class="z-info-value">{{ selectedCustomer.email || 'N/A' }}</div>
            </div>
          </div>
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Số điện thoại</div>
              <div class="z-info-value">{{ selectedCustomer.phone || 'N/A' }}</div>
            </div>
          </div>
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Tổng đơn hàng</div>
              <div class="z-info-value" style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ selectedCustomer.orders }}</div>
            </div>
          </div>
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Tổng chi tiêu</div>
              <div class="z-info-value" style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ selectedCustomer.spent }}</div>
            </div>
          </div>
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Ngày tham gia</div>
              <div class="z-info-value">{{ selectedCustomer.date }}</div>
            </div>
          </div>
          <div class="col-6">
            <div class="z-info-card">
              <div class="z-info-label">Hạng thành viên</div>
              <div class="z-info-value">
                <span class="z-status" :class="selectedCustomer.orders >= 10 ? 'success' : selectedCustomer.orders >= 5 ? 'warning' : 'pending'">
                  {{ selectedCustomer.orders >= 10 ? 'Vàng' : selectedCustomer.orders >= 5 ? 'Bạc' : 'Mới' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'

const avatarColors = ['var(--z-accent)', '#6366f1', 'var(--z-warm)', '#16a34a', '#2563eb', '#9333ea']
const search = ref('')
const customers = ref([])
const showDetail = ref(false)
const selectedCustomer = ref(null)

onMounted(async () => {
  try {
    const data = await api().getKhachHang()
    customers.value = data.map((c, i) => ({
      id: c.id, code: c.maKhachHang || '', name: c.hoVaTen || '', phone: c.soDienThoai || '',
      email: c.email || '', orders: c.tongDon || 0, spent: fmtPrice(c.tongChiTieu),
      date: c.ngayTao ? new Date(c.ngayTao).toLocaleDateString('vi-VN') : '',
      color: avatarColors[i % avatarColors.length],
      gioiTinh: c.gioiTinh,
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

const currentPage = ref(1)
const itemsPerPage = 10

const totalPages = computed(() => Math.ceil(filteredCustomers.value.length / itemsPerPage))

const paginatedCustomers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredCustomers.value.slice(start, start + itemsPerPage)
})

watch(search, () => {
  currentPage.value = 1
})

function openDetail(c) {
  selectedCustomer.value = c
  showDetail.value = true
}
</script>

<style scoped>
.z-clickable-row { cursor: pointer; }
.z-clickable-row:hover td { background: var(--z-accent-soft) !important; }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
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
.z-info-card {
  padding: 14px; background: var(--z-bg-alt); border-radius: var(--z-radius); text-align: center;
}
.z-info-label { font-size: 11px; font-weight: 500; color: var(--z-gray); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.05em; }
.z-info-value { font-size: 14px; font-weight: 500; color: var(--z-dark); }
</style>
