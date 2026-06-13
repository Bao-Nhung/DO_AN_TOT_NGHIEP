<template>
  <AdminLayout>
    <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Cài đặt</h1>
    <p style="font-size:14px;color:var(--z-gray);margin-bottom:24px">Quản lý thuộc tính sản phẩm và cài đặt hệ thống</p>

    <div class="row g-3">
      <!-- Colors -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-palette me-2" style="color:var(--z-accent)"></i>Màu sắc</h3>
          </div>
          <div class="d-flex gap-2 mb-3">
            <input v-model="newColor.tenMauSac" class="lm-input" placeholder="Tên màu" style="flex:1">
            <input v-model="newColor.maHex" type="color" style="width:40px;height:40px;border:none;padding:0;cursor:pointer;border-radius:var(--z-radius)">
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="addColor"><span>Thêm</span></button>
          </div>
          <div class="d-flex flex-column gap-2">
            <div v-for="c in colors" :key="c.id" class="d-flex align-items-center gap-3 py-2"
                 style="border-bottom:1px solid var(--z-gray-border)">
              <div :style="{ width:'24px', height:'24px', borderRadius:'50%', background: c.hex, border:'1px solid var(--z-gray-border)' }"></div>
              <span style="font-size:14px;flex:1">{{ c.name }}</span>
              <code style="font-size:12px;color:var(--z-gray)">{{ c.hex }}</code>
              <button class="z-icon-btn-sm" @click="deleteColor(c)"><i class="bi bi-x"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- Sizes -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-rulers me-2" style="color:var(--z-accent)"></i>Kích thước</h3>
          </div>
          <div class="d-flex gap-2 mb-3">
            <input v-model="newSize" class="lm-input" placeholder="VD: S, M, L, XL" style="flex:1" @keyup.enter="addSize">
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="addSize"><span>Thêm</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="s in sizes" :key="s.id" class="z-chip">
              {{ s.name }}
              <button class="z-chip-x" @click="deleteSize(s)"><i class="bi bi-x"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- Materials -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-scissors me-2" style="color:var(--z-accent)"></i>Chất liệu</h3>
          </div>
          <div class="d-flex gap-2 mb-3">
            <input v-model="newMaterial" class="lm-input" placeholder="Tên chất liệu" style="flex:1" @keyup.enter="addMaterial">
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="addMaterial"><span>Thêm</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="m in materials" :key="m.id" class="z-chip">
              {{ m.name }}
              <button class="z-chip-x" @click="deleteMaterial(m)"><i class="bi bi-x"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- Categories -->
      <div class="col-lg-6">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-tags me-2" style="color:var(--z-accent)"></i>Danh mục</h3>
          </div>
          <div class="d-flex gap-2 mb-3">
            <input v-model="newCategory" class="lm-input" placeholder="Tên danh mục" style="flex:1" @keyup.enter="addCategory">
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="addCategory"><span>Thêm</span></button>
          </div>
          <div class="d-flex flex-wrap gap-2">
            <div v-for="cat in categories" :key="cat.id" class="z-chip">
              {{ cat.name }}
              <button class="z-chip-x" @click="deleteCategory(cat)"><i class="bi bi-x"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- Suppliers -->
      <div class="col-12">
        <div class="z-admin-card">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="z-admin-card-title"><i class="bi bi-building me-2" style="color:var(--z-accent)"></i>Nhà cung cấp</h3>
          </div>
          <div class="d-flex gap-2 mb-3 flex-wrap">
            <input v-model="newSupplier.tenNhaCungCap" class="lm-input" placeholder="Tên nhà cung cấp" style="flex:2;min-width:150px">
            <input v-model="newSupplier.diaChi" class="lm-input" placeholder="Địa chỉ" style="flex:2;min-width:150px">
            <input v-model="newSupplier.soDienThoai" class="lm-input" placeholder="Số điện thoại" style="flex:1;min-width:120px">
            <input v-model="newSupplier.email" class="lm-input" placeholder="Email" style="flex:1;min-width:120px">
            <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="addSupplier"><span>Thêm</span></button>
          </div>
          <table class="z-table">
            <thead>
              <tr><th>Tên</th><th>Địa chỉ</th><th>Điện thoại</th><th>Email</th><th style="width:60px"></th></tr>
            </thead>
            <tbody>
              <tr v-for="s in suppliers" :key="s.id">
                <td style="font-weight:500">{{ s.name }}</td>
                <td style="color:var(--z-gray)">{{ s.address }}</td>
                <td>{{ s.phone }}</td>
                <td style="color:var(--z-gray)">{{ s.email }}</td>
                <td><button class="z-icon-btn-sm" @click="deleteSupplier(s)"><i class="bi bi-x"></i></button></td>
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
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const colors = ref([])
const sizes = ref([])
const materials = ref([])
const categories = ref([])
const suppliers = ref([])

const newColor = ref({ tenMauSac: '', maHex: '#c08b7e' })
const newSize = ref('')
const newMaterial = ref('')
const newCategory = ref('')
const newSupplier = ref({ tenNhaCungCap: '', diaChi: '', soDienThoai: '', email: '' })

onMounted(() => loadData())

async function loadData() {
  try {
    const data = await api().getThuocTinh()
    colors.value = (data.mauSac || []).map(c => ({ id: c.id, name: c.tenMauSac, hex: c.maHex || '#ccc' }))
    sizes.value = (data.kichThuoc || []).map(s => ({ id: s.id, name: s.tenKichThuoc }))
    materials.value = (data.chatLieu || []).map(m => ({ id: m.id, name: m.tenChatLieu }))
    categories.value = (data.loaiVay || []).map(l => ({ id: l.id, name: l.tenLoaiVay }))
    suppliers.value = (data.nhaCungCap || []).map(s => ({
      id: s.id, name: s.tenNhaCungCap || '', address: s.diaChi || '', phone: s.soDienThoai || '', email: s.email || ''
    }))
  } catch (e) { console.error('Không thể tải thuộc tính:', e) }
}

async function addColor() {
  if (!newColor.value.tenMauSac) { showToast('Vui lòng nhập tên màu'); return }
  try {
    await api().addMauSac(newColor.value)
    showToast('Thêm màu sắc thành công!')
    newColor.value = { tenMauSac: '', maHex: '#c08b7e' }
    await loadData()
  } catch (e) { showToast('Lỗi khi thêm') }
}

async function deleteColor(c) {
  if (!confirm(`Xóa màu "${c.name}"?`)) return
  try { await api().deleteMauSac(c.id); showToast('Đã xóa!'); await loadData() } catch (e) { showToast('Lỗi khi xóa') }
}

async function addSize() {
  if (!newSize.value) { showToast('Vui lòng nhập kích thước'); return }
  try {
    await api().addKichThuoc({ tenKichThuoc: newSize.value })
    showToast('Thêm kích thước thành công!')
    newSize.value = ''
    await loadData()
  } catch (e) { showToast('Lỗi khi thêm') }
}

async function deleteSize(s) {
  if (!confirm(`Xóa kích thước "${s.name}"?`)) return
  try { await api().deleteKichThuoc(s.id); showToast('Đã xóa!'); await loadData() } catch (e) { showToast('Lỗi khi xóa') }
}

async function addMaterial() {
  if (!newMaterial.value) { showToast('Vui lòng nhập chất liệu'); return }
  try {
    await api().addChatLieu({ tenChatLieu: newMaterial.value })
    showToast('Thêm chất liệu thành công!')
    newMaterial.value = ''
    await loadData()
  } catch (e) { showToast('Lỗi khi thêm') }
}

async function deleteMaterial(m) {
  if (!confirm(`Xóa chất liệu "${m.name}"?`)) return
  try { await api().deleteChatLieu(m.id); showToast('Đã xóa!'); await loadData() } catch (e) { showToast('Lỗi khi xóa') }
}

async function addCategory() {
  if (!newCategory.value) { showToast('Vui lòng nhập tên danh mục'); return }
  try {
    await api().addLoaiVay({ tenLoaiVay: newCategory.value })
    showToast('Thêm danh mục thành công!')
    newCategory.value = ''
    await loadData()
  } catch (e) { showToast('Lỗi khi thêm') }
}

async function deleteCategory(cat) {
  if (!confirm(`Xóa danh mục "${cat.name}"?`)) return
  try { await api().deleteLoaiVay(cat.id); showToast('Đã xóa!'); await loadData() } catch (e) { showToast('Lỗi khi xóa') }
}

async function addSupplier() {
  if (!newSupplier.value.tenNhaCungCap) { showToast('Vui lòng nhập tên nhà cung cấp'); return }
  try {
    await api().addNhaCungCap(newSupplier.value)
    showToast('Thêm nhà cung cấp thành công!')
    newSupplier.value = { tenNhaCungCap: '', diaChi: '', soDienThoai: '', email: '' }
    await loadData()
  } catch (e) { showToast('Lỗi khi thêm') }
}

async function deleteSupplier(s) {
  if (!confirm(`Xóa nhà cung cấp "${s.name}"?`)) return
  try { await api().deleteNhaCungCap(s.id); showToast('Đã xóa!'); await loadData() } catch (e) { showToast('Lỗi khi xóa') }
}
</script>

<style scoped>
.z-chip {
  padding: 8px 12px; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius);
  font-size: 14px; font-weight: 500; display: inline-flex; align-items: center; gap: 6px;
}
.z-chip-x {
  width: 18px; height: 18px; border: none; background: transparent; cursor: pointer;
  color: var(--z-gray); font-size: 14px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; padding: 0;
}
.z-chip-x:hover { background: #fee2e2; color: #dc2626; }
.z-icon-btn-sm {
  width: 28px; height: 28px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); font-size: 16px;
}
.z-icon-btn-sm:hover { background: #fee2e2; color: #dc2626; }
</style>
