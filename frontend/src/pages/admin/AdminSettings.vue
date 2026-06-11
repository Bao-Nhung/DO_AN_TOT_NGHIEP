<template>
  <AdminLayout>
    <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Cai dat</h1>
    <p style="font-size:14px;color:var(--z-gray);margin-bottom:24px">Quan ly thuoc tinh san pham va cai dat he thong</p>

    <div class="row g-3">
      <!-- Colors -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-palette me-2" style="color:var(--z-accent)"></i>Mau sac</h3>
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px"><span>Them</span></button>
          </div>
          <div class="d-flex flex-column gap-2">
            <div v-for="c in colors" :key="c.name" class="d-flex align-items-center gap-3 py-2"
                 style="border-bottom:1px solid var(--z-gray-border)">
              <div :style="{ width:'24px', height:'24px', borderRadius:'50%', background: c.hex, border:'1px solid var(--z-gray-border)' }"></div>
              <span style="font-size:14px;flex:1">{{ c.name }}</span>
              <code style="font-size:12px;color:var(--z-gray)">{{ c.hex }}</code>
            </div>
          </div>
        </div>
      </div>

      <!-- Sizes -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-rulers me-2" style="color:var(--z-accent)"></i>Kich thuoc</h3>
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px"><span>Them</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="s in sizes" :key="s" style="padding:8px 16px;border:1px solid var(--z-gray-border);border-radius:var(--z-radius);font-size:14px;font-weight:500">
              {{ s }}
            </div>
          </div>
        </div>
      </div>

      <!-- Materials -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-scissors me-2" style="color:var(--z-accent)"></i>Chat lieu</h3>
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px"><span>Them</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="m in materials" :key="m" style="padding:8px 16px;background:var(--z-bg-alt);border-radius:20px;font-size:13px">
              {{ m }}
            </div>
          </div>
        </div>
      </div>

      <!-- Categories -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-tags me-2" style="color:var(--z-accent)"></i>Danh muc</h3>
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px"><span>Them</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="cat in categories" :key="cat" style="padding:8px 16px;background:var(--z-bg-alt);border-radius:20px;font-size:13px">
              {{ cat }}
            </div>
          </div>
        </div>
      </div>

      <!-- Suppliers -->
      <div class="col-12">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-building me-2" style="color:var(--z-accent)"></i>Nha cung cap</h3>
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px"><span>Them</span></button>
          </div>
          <table class="z-table">
            <thead>
              <tr><th>Ten</th><th>Dia chi</th><th>Dien thoai</th><th>Email</th></tr>
            </thead>
            <tbody>
              <tr v-for="s in suppliers" :key="s.name">
                <td style="font-weight:500">{{ s.name }}</td>
                <td style="color:var(--z-gray)">{{ s.address }}</td>
                <td>{{ s.phone }}</td>
                <td style="color:var(--z-gray)">{{ s.email }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'

const colors = ref([])
const sizes = ref([])
const materials = ref([])
const categories = ref([])
const suppliers = ref([])

onMounted(async () => {
  try {
    const data = await api().getThuocTinh()
    colors.value = (data.mauSac || []).map(c => ({ name: c.tenMauSac, hex: c.maHex || '#ccc' }))
    sizes.value = (data.kichThuoc || []).map(s => s.tenKichThuoc)
    materials.value = (data.chatLieu || []).map(m => m.tenChatLieu)
    categories.value = (data.loaiVay || []).map(l => l.tenLoaiVay)
    suppliers.value = (data.nhaCungCap || []).map(s => ({
      name: s.tenNhaCungCap || '', address: s.diaChi || '', phone: s.soDienThoai || '', email: s.email || ''
    }))
  } catch (e) { console.error('Failed to load attributes:', e) }
})
</script>
