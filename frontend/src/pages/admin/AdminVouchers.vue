<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Khuyến mãi & Voucher</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Quản lý chương trình khuyến mãi và mã giảm giá</p>
      </div>
      <button class="lm-btn-primary" @click="openAddVoucher"><span>Thêm voucher</span></button>
    </div>

    <div class="row g-3 mb-4">
      <div v-for="v in vouchers" :key="v.id" class="col-lg-4">
        <div class="z-admin-card h-100">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <div style="font-size:18px;font-weight:700;font-family:monospace;color:var(--z-dark);letter-spacing:0.05em">{{ v.code }}</div>
              <div style="font-size:13px;color:var(--z-gray);margin-top:2px">{{ v.name }}</div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="z-status" :class="v.active ? 'success' : 'pending'">{{ v.active ? 'Hoạt động' : 'Hết hạn' }}</span>
              <button class="z-icon-btn" title="Xóa" style="color:var(--z-accent)" @click="deleteVoucher(v)"><i class="bi bi-trash"></i></button>
            </div>
          </div>
          <div class="d-flex flex-column gap-2 mb-3" style="font-size:13px">
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Giảm</span>
              <span style="font-weight:600;color:var(--z-accent)">{{ v.discount }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Đơn tối thiểu</span>
              <span style="font-weight:500">{{ v.minOrder }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Số lượng</span>
              <span style="font-weight:500">{{ v.total }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span style="color:var(--z-gray)">Thời gian</span>
              <span style="font-weight:500">{{ v.period }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Promotions -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="z-admin-card-title" style="font-size:18px">Chương trình khuyến mãi</h3>
      <button class="lm-btn-primary" style="padding:8px 16px;font-size:12px" @click="openAddPromo"><span>Thêm khuyến mãi</span></button>
    </div>
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Mã</th>
            <th>Tên chương trình</th>
            <th>Giảm</th>
            <th>Bắt đầu</th>
            <th>Kết thúc</th>
            <th>Trạng thái</th>
            <th style="width:80px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in promotions" :key="p.id">
            <td style="font-weight:600;font-family:monospace">{{ p.code }}</td>
            <td style="font-weight:500">{{ p.name }}</td>
            <td style="color:var(--z-accent);font-weight:600">{{ p.discount }}</td>
            <td>{{ p.start }}</td>
            <td>{{ p.end }}</td>
            <td><span class="z-status" :class="p.active ? 'success' : 'pending'">{{ p.active ? 'Đang chạy' : 'Kết thúc' }}</span></td>
            <td>
              <button class="z-icon-btn" title="Xóa" style="color:var(--z-accent)" @click="deletePromo(p)"><i class="bi bi-trash"></i></button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Voucher Modal -->
    <div v-if="showVoucherModal" class="z-modal-overlay" @click.self="showVoucherModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Thêm voucher mới</h3>
          <button class="z-icon-btn" @click="showVoucherModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Mã giảm giá *</label>
              <input v-model="vForm.maGiamGia" class="lm-input" placeholder="VD: SALE20">
            </div>
            <div class="col-6">
              <label class="z-label">Tên voucher</label>
              <input v-model="vForm.tenGiamGia" class="lm-input" placeholder="Tên mô tả">
            </div>
          </div>
          <div class="row g-3">
            <div class="col-4">
              <label class="z-label">Phần trăm giảm (%)</label>
              <input v-model.number="vForm.phanTramGiam" type="number" class="lm-input" placeholder="20">
            </div>
            <div class="col-4">
              <label class="z-label">Giảm tối đa</label>
              <input v-model.number="vForm.giamToiDa" type="number" class="lm-input" placeholder="100000">
            </div>
            <div class="col-4">
              <label class="z-label">Số lượng</label>
              <input v-model.number="vForm.soLuong" type="number" class="lm-input" placeholder="100">
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Ngày bắt đầu</label>
              <input v-model="vForm.ngayBatDau" type="date" class="lm-input">
            </div>
            <div class="col-6">
              <label class="z-label">Ngày kết thúc</label>
              <input v-model="vForm.ngayKetThuc" type="date" class="lm-input">
            </div>
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showVoucherModal = false">Huỷ</button>
          <button class="lm-btn-primary" @click="saveVoucher" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Thêm mới' }}</span></button>
        </div>
      </div>
    </div>

    <!-- Promo Modal -->
    <div v-if="showPromoModal" class="z-modal-overlay" @click.self="showPromoModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Thêm khuyến mãi mới</h3>
          <button class="z-icon-btn" @click="showPromoModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Mã khuyến mãi *</label>
              <input v-model="pForm.maKhuyenMai" class="lm-input" placeholder="VD: KM2025">
            </div>
            <div class="col-6">
              <label class="z-label">Tên chương trình</label>
              <input v-model="pForm.tenKhuyenMai" class="lm-input" placeholder="Tên mô tả">
            </div>
          </div>
          <div>
            <label class="z-label">Phần trăm giảm (%)</label>
            <input v-model.number="pForm.phanTramGiam" type="number" class="lm-input" placeholder="10">
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Ngày bắt đầu</label>
              <input v-model="pForm.ngayBatDau" type="date" class="lm-input">
            </div>
            <div class="col-6">
              <label class="z-label">Ngày kết thúc</label>
              <input v-model="pForm.ngayKetThuc" type="date" class="lm-input">
            </div>
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showPromoModal = false">Huỷ</button>
          <button class="lm-btn-primary" @click="savePromo" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Thêm mới' }}</span></button>
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
function fmtDate(d) { return d ? new Date(d).toLocaleDateString('vi-VN') : '' }

const vouchers = ref([])
const promotions = ref([])
const showVoucherModal = ref(false)
const showPromoModal = ref(false)
const saving = ref(false)

const vForm = ref({ maGiamGia: '', tenGiamGia: '', phanTramGiam: null, giamToiDa: null, soLuong: null, ngayBatDau: '', ngayKetThuc: '' })
const pForm = ref({ maKhuyenMai: '', tenKhuyenMai: '', phanTramGiam: null, ngayBatDau: '', ngayKetThuc: '' })

onMounted(() => loadData())

async function loadData() {
  try {
    const data = await api().getAllKhuyenMai()
    if (data.giamGia) {
      vouchers.value = data.giamGia.map(g => ({
        id: g.id, code: g.maGiamGia || '', name: g.tenGiamGia || '',
        discount: g.phanTramGiam ? g.phanTramGiam + '%' : (g.gioTriGiam ? Number(g.gioTriGiam).toLocaleString('vi-VN') + 'đ' : ''),
        minOrder: g.giaTriDonToiThieu ? Number(g.giaTriDonToiThieu).toLocaleString('vi-VN') + 'đ' : '0đ',
        total: g.soLuong || 0,
        period: fmtDate(g.ngayBatDau) + ' - ' + fmtDate(g.ngayKetThuc),
        active: g.trangThai === 1 || g.trangThai === true
      }))
    }
    if (data.khuyenMai) {
      promotions.value = data.khuyenMai.map(k => ({
        id: k.id, code: k.maKhuyenMai || '', name: k.tenKhuyenMai || '',
        discount: k.phanTramGiam ? k.phanTramGiam + '%' : '',
        start: fmtDate(k.ngayBatDau), end: fmtDate(k.ngayKetThuc),
        active: k.trangThai === 1 || k.trangThai === true
      }))
    }
  } catch (e) { console.error('Không thể tải khuyến mãi:', e) }
}

function openAddVoucher() {
  vForm.value = { maGiamGia: '', tenGiamGia: '', phanTramGiam: null, giamToiDa: null, soLuong: null, ngayBatDau: '', ngayKetThuc: '' }
  showVoucherModal.value = true
}

function openAddPromo() {
  pForm.value = { maKhuyenMai: '', tenKhuyenMai: '', phanTramGiam: null, ngayBatDau: '', ngayKetThuc: '' }
  showPromoModal.value = true
}

async function saveVoucher() {
  if (!vForm.value.maGiamGia) { showToast('Vui lòng nhập mã giảm giá'); return }
  saving.value = true
  try {
    await api().addGiamGia(vForm.value)
    showToast('Thêm voucher thành công!')
    showVoucherModal.value = false
    await loadData()
  } catch (e) { showToast('Lỗi: ' + (e.message || '')) }
  finally { saving.value = false }
}

async function savePromo() {
  if (!pForm.value.maKhuyenMai) { showToast('Vui lòng nhập mã khuyến mãi'); return }
  saving.value = true
  try {
    await api().addKhuyenMai(pForm.value)
    showToast('Thêm khuyến mãi thành công!')
    showPromoModal.value = false
    await loadData()
  } catch (e) { showToast('Lỗi: ' + (e.message || '')) }
  finally { saving.value = false }
}

async function deleteVoucher(v) {
  if (!confirm(`Bạn có chắc muốn xóa voucher "${v.code}"?`)) return
  try {
    await api().deleteGiamGia(v.id)
    showToast('Đã xóa voucher!')
    await loadData()
  } catch (e) { showToast('Lỗi khi xóa') }
}

async function deletePromo(p) {
  if (!confirm(`Bạn có chắc muốn xóa khuyến mãi "${p.code}"?`)) return
  try {
    await api().deleteKhuyenMai(p.id)
    showToast('Đã xóa khuyến mãi!')
    await loadData()
  } catch (e) { showToast('Lỗi khi xóa') }
}
</script>

<style scoped>
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.z-modal { background: var(--z-white); border-radius: var(--z-radius-lg); padding: 28px; width: 100%; max-width: 560px; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
</style>
