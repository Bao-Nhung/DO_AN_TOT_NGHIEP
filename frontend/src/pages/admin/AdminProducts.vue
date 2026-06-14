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
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in filteredProducts" :key="p.id" class="z-clickable-row" @click="openProductDetail(p)">
            <td @click.stop><input type="checkbox"></td>
            <td>
              <div class="d-flex align-items-center gap-3">
                <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                  <img v-if="p.image" :src="p.image" style="width:100%;height:100%;object-fit:cover">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
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
            <td @click.stop>
              <div class="d-flex gap-1">
                <button class="z-icon-btn" title="Sửa" @click="openEdit(p)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Chi tiết" @click="openProductDetail(p)"><i class="bi bi-eye"></i></button>
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

    <!-- Product Detail Modal -->
    <div v-if="showProductDetail" class="z-modal-overlay" @click.self="showProductDetail = false">
      <div class="z-modal" style="max-width:800px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết sản phẩm</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ productDetail?.maVay }}</div>
          </div>
          <button class="z-icon-btn" @click="showProductDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingProductDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>

        <div v-else-if="productDetail">
          <div class="row g-4 mb-4">
            <div class="col-md-4">
              <div style="aspect-ratio:3/4;border-radius:var(--z-radius-lg);overflow:hidden;background:var(--z-bg-alt)">
                <img v-if="productDetail.anhUrl" :src="productDetail.anhUrl" style="width:100%;height:100%;object-fit:cover">
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:48px;color:var(--z-gray-light)">
                  <i class="bi bi-image"></i>
                </div>
              </div>
            </div>
            <div class="col-md-8">
              <h2 style="font-size:22px;font-weight:600;margin-bottom:8px">{{ productDetail.tenVay }}</h2>
              <div class="d-flex gap-2 align-items-center mb-3">
                <span class="z-status" :class="productDetail.trangThai === 1 ? 'success' : 'pending'">
                  {{ productDetail.trangThai === 1 ? 'Đang bán' : 'Ngừng bán' }}
                </span>
                <span v-if="productDetail.loaiVay" style="font-size:12px;color:var(--z-gray);background:var(--z-bg-alt);padding:4px 10px;border-radius:20px">{{ productDetail.loaiVay }}</span>
              </div>
              <div class="row g-2 mb-3">
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Chất liệu:</span> <strong style="font-size:13px">{{ productDetail.chatLieu || 'N/A' }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Tồn kho:</span> <strong style="font-size:13px">{{ productDetail.tonKho }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Giá bán:</span> <strong style="font-size:13px;color:var(--z-accent)">{{ fmtPrice(productDetail.giaBan) }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Giá gốc:</span> <strong style="font-size:13px">{{ fmtPrice(productDetail.giaBanGoc) }}</strong></div>
              </div>
              <div v-if="productDetail.moTa" style="font-size:13px;color:var(--z-gray);line-height:1.6">{{ productDetail.moTa }}</div>
            </div>
          </div>

          <!-- Variants -->
          <h4 style="font-size:14px;font-weight:600;margin-bottom:12px">Biến thể ({{ productDetail.bienThe?.length || 0 }})</h4>
          <div class="z-admin-card" style="padding:0;overflow:hidden">
            <table class="z-table" style="font-size:12px">
              <thead>
                <tr>
                  <th>Mã</th>
                  <th>Màu sắc</th>
                  <th>Kích thước</th>
                  <th>Giá bán</th>
                  <th>Giá gốc</th>
                  <th>Số lượng</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="bt in productDetail.bienThe" :key="bt.id">
                  <td style="font-weight:500">{{ bt.maVayChiTiet }}</td>
                  <td>
                    <span class="d-inline-flex align-items-center gap-1">
                      <span v-if="bt.maHex" :style="{ width:'12px', height:'12px', borderRadius:'50%', background: bt.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                      {{ bt.mauSac || 'N/A' }}
                    </span>
                  </td>
                  <td>{{ bt.kichThuoc || 'N/A' }}</td>
                  <td style="font-weight:500">{{ fmtPrice(bt.giaBan) }}</td>
                  <td style="color:var(--z-gray)">{{ fmtPrice(bt.giaBanGoc) }}</td>
                  <td :style="{ color: bt.soLuong < 5 ? 'var(--z-accent)' : '', fontWeight: bt.soLuong < 5 ? 600 : 400 }">{{ bt.soLuong }}</td>
                  <td><span class="z-status" :class="bt.trangThai === 1 ? 'success' : 'pending'" style="font-size:10px;padding:2px 8px">{{ bt.trangThai === 1 ? 'Bán' : 'Ngừng' }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal" style="max-width:700px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h3>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <!-- Product info -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">THÔNG TIN SẢN PHẨM</h4>
        <div class="d-flex flex-column gap-3 mb-4">
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
              <label class="z-label">Nhà cung cấp</label>
              <select v-model="form.idNhaCungCap" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="ncc in nhaCungCapList" :key="ncc.id" :value="ncc.id">{{ ncc.tenNhaCungCap }}</option>
              </select>
            </div>
          </div>
          <div>
            <label class="z-label">Mô tả</label>
            <textarea v-model="form.moTa" class="lm-input" rows="2" placeholder="Mô tả sản phẩm..."></textarea>
          </div>
        </div>

        <!-- Variants -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">BIẾN THỂ (Size, Màu, Giá, Số lượng)</h4>
        <div class="d-flex flex-column gap-2 mb-3">
          <div v-for="(v, i) in form.variants" :key="i"
               class="d-flex align-items-center gap-2 p-2" style="background:var(--z-bg-alt);border-radius:var(--z-radius)">
            <select v-model="v.idMauSac" class="lm-input" style="padding:6px 10px;font-size:12px;flex:1">
              <option :value="null">Màu sắc</option>
              <option v-for="ms in mauSacList" :key="ms.id" :value="ms.id">{{ ms.tenMauSac }}</option>
            </select>
            <select v-model="v.idKichThuoc" class="lm-input" style="padding:6px 10px;font-size:12px;flex:1">
              <option :value="null">Kích thước</option>
              <option v-for="kt in kichThuocList" :key="kt.id" :value="kt.id">{{ kt.tenKichThuoc }}</option>
            </select>
            <input v-model.number="v.giaBan" type="number" class="lm-input" placeholder="Giá bán" style="padding:6px 10px;font-size:12px;flex:1">
            <input v-model.number="v.giaBanGoc" type="number" class="lm-input" placeholder="Giá gốc" style="padding:6px 10px;font-size:12px;flex:1">
            <input v-model.number="v.soLuong" type="number" class="lm-input" placeholder="SL" style="padding:6px 10px;font-size:12px;width:70px">
            <button class="z-icon-btn" style="color:var(--z-accent);flex-shrink:0" @click="form.variants.splice(i, 1)">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
        <button class="lm-btn-secondary mb-4" @click="addVariant" style="font-size:13px">
          <i class="bi bi-plus-circle me-1"></i> Thêm biến thể
        </button>

        <div class="d-flex justify-content-end gap-2">
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
const mauSacList = ref([])
const kichThuocList = ref([])
const nhaCungCapList = ref([])

const showProductDetail = ref(false)
const productDetail = ref(null)
const loadingProductDetail = ref(false)

const defaultForm = { tenVay: '', maVay: '', moTa: '', trangThai: 1, idLoaiVay: null, idChatLieu: null, idNhaCungCap: null, variants: [] }
const form = ref({ ...defaultForm, variants: [] })

onMounted(async () => {
  await loadProducts()
  try {
    const attrs = await api().getThuocTinh()
    loaiVayList.value = attrs.loaiVay || []
    chatLieuList.value = attrs.chatLieu || []
    mauSacList.value = attrs.mauSac || []
    kichThuocList.value = attrs.kichThuoc || []
    nhaCungCapList.value = attrs.nhaCungCap || []
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

function addVariant() {
  form.value.variants.push({ idMauSac: null, idKichThuoc: null, giaBan: null, giaBanGoc: null, soLuong: 0 })
}

function openAdd() {
  editingId.value = null
  form.value = { ...defaultForm, variants: [{ idMauSac: null, idKichThuoc: null, giaBan: null, giaBanGoc: null, soLuong: 0 }] }
  showModal.value = true
}

async function openEdit(p) {
  editingId.value = p.rawId || p.id
  form.value = {
    tenVay: p.name || '',
    maVay: p.code || '',
    moTa: p.raw?.moTa || '',
    trangThai: p.active ? 1 : 0,
    idLoaiVay: p.raw?.idLoaiVay || null,
    idChatLieu: p.raw?.idChatLieu || null,
    idNhaCungCap: p.raw?.idNhaCungCap || null,
    variants: [],
  }
  try {
    const detail = await api().getVayById(p.rawId || p.id)
    if (detail.bienThe && detail.bienThe.length > 0) {
      form.value.variants = detail.bienThe.map(bt => ({
        idMauSac: mauSacList.value.find(m => m.tenMauSac === bt.mauSac)?.id || null,
        idKichThuoc: kichThuocList.value.find(k => k.tenKichThuoc === bt.kichThuoc)?.id || null,
        giaBan: bt.giaBan ? Number(bt.giaBan) : null,
        giaBanGoc: bt.giaBanGoc ? Number(bt.giaBanGoc) : null,
        soLuong: bt.soLuong || 0,
      }))
    } else {
      form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.salePrice || p.price || 0, giaBanGoc: p.price || 0, soLuong: p.stock || 0 }]
    }
  } catch (e) {
    form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.salePrice || p.price || 0, giaBanGoc: p.price || 0, soLuong: p.stock || 0 }]
  }
  showModal.value = true
}

async function openProductDetail(p) {
  showProductDetail.value = true
  loadingProductDetail.value = true
  try {
    productDetail.value = await api().getVayById(p.rawId || p.id)
  } catch (e) {
    productDetail.value = p.raw
  } finally {
    loadingProductDetail.value = false
  }
}

async function doSave() {
  if (!form.value.tenVay) { showToast('Vui lòng nhập tên sản phẩm'); return }
  saving.value = true
  try {
    const firstVariant = form.value.variants[0] || {}
    const payload = {
      tenVay: form.value.tenVay,
      maVay: form.value.maVay,
      moTa: form.value.moTa,
      trangThai: form.value.trangThai,
      idLoaiVay: form.value.idLoaiVay,
      idChatLieu: form.value.idChatLieu,
      idNhaCungCap: form.value.idNhaCungCap,
      giaBan: firstVariant.giaBan,
      giaBanGoc: firstVariant.giaBanGoc,
      soLuong: firstVariant.soLuong || 0,
    }
    if (editingId.value) {
      await api().updateVay(editingId.value, payload)
      showToast('Cập nhật sản phẩm thành công!')
    } else {
      await api().createVay(payload)
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
.z-clickable-row { cursor: pointer; }
.z-clickable-row:hover td { background: var(--z-accent-soft) !important; }
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
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
</style>
