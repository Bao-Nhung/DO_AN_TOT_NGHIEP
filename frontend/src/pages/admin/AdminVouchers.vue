<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Khuyen mai & Voucher</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Quan ly chuong trinh khuyen mai va ma giam gia</p>
      </div>
      <button class="lm-btn-primary"><span>Them voucher</span></button>
    </div>

    <div class="row g-3 mb-4">
      <div v-for="v in vouchers" :key="v.code" class="col-lg-4">
        <div class="z-admin-card h-100">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <div style="font-size:18px;font-weight:700;font-family:monospace;color:var(--z-dark);letter-spacing:0.05em">{{ v.code }}</div>
              <div style="font-size:13px;color:var(--z-gray);margin-top:2px">{{ v.name }}</div>
            </div>
            <span class="z-status" :class="v.active ? 'success' : 'pending'">{{ v.active ? 'Hoat dong' : 'Het han' }}</span>
          </div>
          <div class="d-flex flex-column gap-2 mb-3" style="font-size:13px">
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Giam</span>
              <span style="font-weight:600;color:var(--z-accent)">{{ v.discount }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Don toi thieu</span>
              <span style="font-weight:500">{{ v.minOrder }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">So luong</span>
              <span style="font-weight:500">{{ v.used }}/{{ v.total }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Thoi gian</span>
              <span style="font-weight:500">{{ v.period }}</span>
            </div>
          </div>
          <div style="height:4px;background:var(--z-bg-alt);border-radius:2px;overflow:hidden">
            <div :style="{ width: (v.used / v.total * 100) + '%', height:'100%', background:'var(--z-accent)', borderRadius:'2px' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Promotions -->
    <h3 class="z-admin-card-title mb-3" style="font-size:18px">Chuong trinh khuyen mai</h3>
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Ma</th>
            <th>Ten chuong trinh</th>
            <th>Giam</th>
            <th>Bat dau</th>
            <th>Ket thuc</th>
            <th>Trang thai</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in promotions" :key="p.code">
            <td style="font-weight:600;font-family:monospace">{{ p.code }}</td>
            <td style="font-weight:500">{{ p.name }}</td>
            <td style="color:var(--z-accent);font-weight:600">{{ p.discount }}</td>
            <td>{{ p.start }}</td>
            <td>{{ p.end }}</td>
            <td><span class="z-status" :class="p.active ? 'success' : 'pending'">{{ p.active ? 'Dang chay' : 'Ket thuc' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'

function fmtDate(d) { return d ? new Date(d).toLocaleDateString('vi-VN') : '' }

const vouchers = ref([])
const promotions = ref([])

onMounted(async () => {
  try {
    const data = await api().getAllKhuyenMai()
    if (data.giamGia) {
      vouchers.value = data.giamGia.map(g => ({
        code: g.maGiamGia || '', name: g.tenGiamGia || '',
        discount: g.phanTramGiam ? g.phanTramGiam + '%' : (g.giaTriGiam ? Number(g.giaTriGiam).toLocaleString('vi-VN') + 'd' : ''),
        minOrder: '0d', used: 0, total: g.soLuong || 1,
        period: fmtDate(g.ngayBatDau) + ' - ' + fmtDate(g.ngayKetThuc),
        active: g.trangThai === 1 || g.trangThai === true
      }))
    }
    if (data.khuyenMai) {
      promotions.value = data.khuyenMai.map(k => ({
        code: k.maKhuyenMai || '', name: k.tenKhuyenMai || '',
        discount: k.phanTramGiam ? k.phanTramGiam + '%' : '',
        start: fmtDate(k.ngayBatDau), end: fmtDate(k.ngayKetThuc),
        active: k.trangThai === 1 || k.trangThai === true
      }))
    }
  } catch (e) { console.error('Failed to load promotions:', e) }
})
</script>
