<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý sản phẩm</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredProducts.length }} sản phẩm</p>
      </div>
      <button class="lm-btn-primary" @click="openAdd">
        <i class="bi bi-plus-lg" style="position:relative;z-index:1"></i>
        <span>Thêm sản phẩm</span>
      </button>
    </div>

    <!-- Filters -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-3 flex-wrap">
        <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:320px">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="search" class="lm-input" placeholder="Tìm kiếm sản phẩm..." style="border:none;padding:8px 0;box-shadow:none">
        </div>
        <select v-model="filterCategory" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả loại</option>
          <option v-for="cat in categories" :key="cat">{{ cat }}</option>
        </select>
        <select v-model="filterStatus" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả trạng thái</option>
          <option value="1">Đang bán</option>
          <option value="0">Ngừng bán</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th style="width:50px"><input type="checkbox"></th>
            <th>Sản phẩm</th>
            <th>Loại</th>
            <th>Giá</th>
            <th>Tồn kho</th>
            <th>Trạng thái</th>
            <th style="width:100px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in filteredProducts" :key="p.id">
            <td><input type="checkbox"></td>
            <td>
              <div class="d-flex align-items-center gap-3">
                <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0">
                  <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                       :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
                    {{ p.letter }}
                  </div>
                </div>
                <div>
                  <div style="font-weight:500">{{ p.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ p.code }}</div>
                </div>
              </div>
            </td>
            <td>{{ p.category }}</td>
            <td style="font-weight:500">{{ p.priceDisplay }}</td>
            <td>
              <span :style="{ color: p.stock < 10 ? 'var(--z-accent)' : 'var(--z-dark)', fontWeight: p.stock < 10 ? 600 : 400 }">
                {{ p.stock }}
              </span>
            </td>
            <td><span class="z-status" :class="p.active ? 'success' : 'pending'">{{ p.active ? 'Đang bán' : 'Ngừng' }}</span></td>
            <td>
              <div class="d-flex gap-1">
                <button class="z-icon-btn" title="Sửa" @click="openEdit(p)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Xóa" style="color:var(--z-accent)" @click="doDelete(p)"><i class="bi bi-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredProducts.length === 0" class="text-center py-5">
        <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có sản phẩm nào</p>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h3>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <div class="d-flex flex-column gap-3">
          <div>
            <label class="z-label">Tên sản phẩm *</label>
            <input v-model="form.tenVay" class="lm-input" placeholder="Nhập tên sản phẩm">
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Mã sản phẩm</label>
              <input v-model="form.maVay" class="lm-input" placeholder="VD: VAY-001">
            </div>
            <div class="col-6">
              <label class="z-label">Trạng thái</label>
              <select v-model="form.trangThai" class="lm-input">
                <option :value="1">Đang bán</option>
                <option :value="0">Ngừng bán</option>
              </select>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Giá bán *</label>
              <input v-model.number="form.giaBan" type="number" class="lm-input" placeholder="0">
            </div>
            <div class="col-6">
              <label class="z-label">Giá gốc</label>
              <input v-model.number="form.giaBanGoc" type="number" class="lm-input" placeholder="0">
            </div>
          </div>
          <div class="row g-3">
            <div class="col-4">
              <label class="z-label">Loại váy</label>
              <select v-model="form.idLoaiVay" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="lv in loaiVayList" :key="lv.id" :value="lv.id">{{ lv.tenLoaiVay }}</option>
              </select>
            </div>
            <div class="col-4">
              <label class="z-label">Chất liệu</label>
              <select v-model="form.idChatLieu" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="cl in chatLieuList" :key="cl.id" :value="cl.id">{{ cl.tenChatLieu }}</option>
              </select>
            </div>
            <div class="col-4">
              <label class="z-label">Số lượng</label>
              <input v-model.number="form.soLuong" type="number" class="lm-input" placeholder="0">
            </div>
          </div>
          <div>
            <label class="z-label">Mô tả</label>
            <textarea v-model="form.moTa" class="lm-input" rows="3" placeholder="Mô tả sản phẩm..."></textarea>
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
          <button class="lm-btn-primary" @click="doSave" :disabled="saving">
            <span>{{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật' : 'Thêm mới') }}</span>
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { mapProduct, fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const search = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const showModal = ref(false)
const saving = ref(false)
const editingId = ref(null)
const allProducts = ref([])
const rawProducts = ref([])
const categories = ref([])
const loaiVayList = ref([])
const chatLieuList = ref([])

const defaultForm = { tenVay: '', maVay: '', moTa: '', trangThai: 1, giaBan: null, giaBanGoc: null, soLuong: 0, idLoaiVay: null, idChatLieu: null }
const form = ref({ ...defaultForm })

onMounted(async () => {
  await loadProducts()
  try {
    const attrs = await api().getThuocTinh()
    loaiVayList.value = attrs.loaiVay || []
    chatLieuList.value = attrs.chatLieu || []
  } catch (e) { console.error(e) }
})

async function loadProducts() {
  try {
    const data = await api().getVay()
    rawProducts.value = data
    allProducts.value = data.map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, priceDisplay: fmtPrice(m.salePrice || m.price), rawId: p.id, raw: p }
    })
    categories.value = [...new Set(allProducts.value.map(p => p.category).filter(Boolean))]
  } catch (e) { console.error('Không thể tải sản phẩm:', e) }
}

const filteredProducts = computed(() => {
  return allProducts.value.filter(p => {
    const matchSearch = !search.value || p.name.toLowerCase().includes(search.value.toLowerCase()) || (p.code || '').toLowerCase().includes(search.value.toLowerCase())
    const matchCat = !filterCategory.value || p.category === filterCategory.value
    const matchStatus = !filterStatus.value || (filterStatus.value === '1' ? p.active : !p.active)
    return matchSearch && matchCat && matchStatus
  })
})

function openAdd() {
  editingId.value = null
  form.value = { ...defaultForm }
  showModal.value = true
}

function openEdit(p) {
  editingId.value = p.rawId || p.id
  form.value = {
    tenVay: p.name || '',
    maVay: p.code || '',
    moTa: p.raw?.moTa || '',
    trangThai: p.active ? 1 : 0,
    giaBan: p.salePrice || p.price || 0,
    giaBanGoc: p.price || 0,
    soLuong: p.stock || 0,
    idLoaiVay: p.raw?.idLoaiVay || null,
    idChatLieu: p.raw?.idChatLieu || null,
  }
  showModal.value = true
}

async function doSave() {
  if (!form.value.tenVay) { showToast('Vui lòng nhập tên sản phẩm'); return }
  saving.value = true
  try {
    if (editingId.value) {
      await api().updateVay(editingId.value, form.value)
      showToast('Cập nhật sản phẩm thành công!')
    } else {
      await api().createVay(form.value)
      showToast('Thêm sản phẩm thành công!')
    }
    showModal.value = false
    await loadProducts()
  } catch (e) { showToast('Lỗi: ' + (e.message || 'Không thể lưu')) }
  finally { saving.value = false }
}

async function doDelete(p) {
  if (!confirm(`Bạn có chắc muốn xóa "${p.name}"?`)) return
  try {
    await api().deleteVay(p.rawId || p.id)
    showToast('Đã xóa sản phẩm!')
    await loadProducts()
  } catch (e) { showToast('Lỗi khi xóa: ' + (e.message || '')) }
}
</script>

<style scoped>
.z-icon-btn {
  width: 32px; height: 32px;
  border: none; background: transparent;
  border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray);
  transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
</style>
