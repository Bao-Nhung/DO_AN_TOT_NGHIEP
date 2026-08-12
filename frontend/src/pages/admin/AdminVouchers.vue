<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý Voucher</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Tạo, cập nhật và theo dõi các mã giảm giá của cửa hàng</p>
      </div>
      <button class="lm-btn-primary" @click="openAddVoucher"><span>Thêm voucher</span></button>
    </div>

    <!-- Active Vouchers Grid -->
    <div class="row g-3 mb-4">
      <div v-for="v in paginatedVouchers" :key="v.id" class="col-lg-4">
        <div class="z-admin-card h-100 d-flex flex-column justify-content-between">
          <div>
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div>
                <div style="font-size:17px;font-weight:700;font-family:var(--z-font-body);color:var(--z-dark);letter-spacing:0">{{ v.code }}</div>
                <div style="font-size:13px;color:var(--z-gray);margin-top:2px">{{ v.name }}</div>
              </div>
              <div class="d-flex align-items-center gap-1">
                <span class="z-status me-2" :class="v.statusClass">{{ v.statusLabel }}</span>
                <button type="button" class="z-icon-btn" title="Xem chi tiết" aria-label="Xem chi tiết voucher" @click="openViewVoucher(v)"><i class="bi bi-eye"></i></button>
                <button type="button" class="z-icon-btn" title="Chỉnh sửa" aria-label="Chỉnh sửa voucher" @click="openEditVoucher(v)"><i class="bi bi-pencil"></i></button>
                <button v-if="v.raw.trangThai === 1" type="button" class="z-icon-btn" title="Ngừng hoạt động" aria-label="Ngừng hoạt động voucher" style="color:var(--z-accent)" @click="deactivateVoucher(v)"><i class="bi bi-slash-circle"></i></button>
              </div>
            </div>
            <div class="d-flex flex-column gap-2 mb-3" style="font-size:13px">
              <div class="d-flex justify-content-between">
                <span style="color:var(--z-gray)">Mức giảm</span>
                <span style="font-weight:600;color:var(--z-accent)">{{ v.discount }}</span>
              </div>
              <div class="d-flex justify-content-between">
                <span style="color:var(--z-gray)">Đơn tối thiểu</span>
                <span style="font-weight:500">{{ v.minOrder }}</span>
              </div>
              <div class="d-flex justify-content-between">
                <span style="color:var(--z-gray)">Số lượng còn lại</span>
                <span style="font-weight:500">{{ v.total }}</span>
              </div>
              <div class="d-flex justify-content-between">
                <span style="color:var(--z-gray)">Hạn sử dụng</span>
                <span style="font-weight:500">{{ v.period }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="filteredVouchers.length === 0" class="col-12 text-center py-5">
        <i class="bi bi-ticket-perforated" style="font-size:48px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có mã giảm giá nào</p>
      </div>
    </div>

    <div v-if="filteredVouchers.length" class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <span style="font-size:13px;color:var(--z-gray)">
        Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }}-{{ Math.min(currentPage * itemsPerPage, filteredVouchers.length) }} / {{ filteredVouchers.length }} voucher
      </span>
      <PageSizeSelect v-model="itemsPerPage" :options="[6, 9, 18, 36]" />
      <div v-if="totalPages > 1" class="d-flex gap-2">
        <button class="lm-btn-secondary z-page-btn" :disabled="currentPage === 1" @click="currentPage--">Trước</button>
        <button
          v-for="page in totalPages"
          :key="page"
          class="lm-btn-secondary z-page-btn"
          :class="{ active: currentPage === page }"
          @click="currentPage = page"
        >
          {{ page }}
        </button>
        <button class="lm-btn-secondary z-page-btn" :disabled="currentPage === totalPages" @click="currentPage++">Sau</button>
      </div>
    </div>

    <!-- Voucher Add/Edit Modal -->
    <div v-if="showVoucherModal" class="z-modal-overlay" @click.self="showVoucherModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ vForm.id ? 'Chỉnh sửa Voucher' : 'Thêm voucher mới' }}</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu voucher" @click="showVoucherModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Mã giảm giá *</label>
              <input v-model="vForm.maGiamGia" class="lm-input" placeholder="VD: SALE20" :disabled="!!vForm.id">
            </div>
            <div class="col-6">
              <label class="z-label">Tên voucher</label>
              <input v-model="vForm.tenGiamGia" class="lm-input" placeholder="Tên mô tả">
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Loại giảm *</label>
              <select v-model="vForm.discountType" class="lm-input">
                <option value="PERCENT">Theo phần trăm</option>
                <option value="FIXED">Theo số tiền</option>
              </select>
            </div>
            <div class="col-6">
              <label class="z-label">{{ vForm.discountType === 'PERCENT' ? 'Phần trăm giảm (%) *' : 'Số tiền giảm (đ) *' }}</label>
              <input
                v-if="vForm.discountType === 'PERCENT'"
                v-model.number="vForm.phanTramGiam"
                type="number"
                min="0.01"
                max="100"
                step="0.01"
                class="lm-input"
                placeholder="Ví dụ: 10"
              >
              <input
                v-else
                v-model.number="vForm.gioTriGiam"
                type="number"
                min="1"
                step="1000"
                class="lm-input"
                placeholder="Ví dụ: 50000"
              >
            </div>
          </div>
          <div class="row g-3">
            <div v-if="vForm.discountType === 'PERCENT'" class="col-6">
              <label class="z-label">Giảm tối đa (đ)</label>
              <input v-model.number="vForm.giamToiDa" type="number" min="1" step="1000" class="lm-input" placeholder="Ví dụ: 100000">
            </div>
            <div :class="vForm.discountType === 'PERCENT' ? 'col-6' : 'col-12'">
              <label class="z-label">Đơn tối thiểu (đ)</label>
              <input v-model.number="vForm.giaTriDonToiThieu" type="number" min="0" step="1000" class="lm-input" placeholder="Ví dụ: 200000">
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Số lượng</label>
              <input v-model.number="vForm.soLuong" type="number" min="0" max="1000000" step="1" class="lm-input" placeholder="100">
            </div>
            <div class="col-6" v-if="vForm.id">
              <label class="z-label">Trạng thái</label>
              <select v-model.number="vForm.trangThai" class="lm-input">
                <option :value="1">Hoạt động</option>
                <option :value="0">Ngừng hoạt động</option>
              </select>
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
          <button class="lm-btn-primary" @click="saveVoucher" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : (vForm.id ? 'Cập nhật' : 'Thêm mới') }}</span></button>
        </div>
      </div>
    </div>

    <!-- View Voucher Modal -->
    <div v-if="showViewModal" class="z-modal-overlay" @click.self="showViewModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết Voucher</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng chi tiết voucher" @click="showViewModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <div v-if="viewData" class="d-flex flex-column gap-3" style="font-size: 14px;">
          <div class="row g-3">
            <div class="col-6">
              <span style="color:var(--z-gray)">Mã giảm giá:</span>
              <strong style="font-family:var(--z-font-body); font-size:16px" class="d-block text-dark mt-1">{{ viewData.maGiamGia }}</strong>
            </div>
            <div class="col-6">
              <span style="color:var(--z-gray)">Tên voucher:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.tenGiamGia || 'N/A' }}</strong>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-4">
              <span style="color:var(--z-gray)">Phần trăm giảm:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.phanTramGiam ? viewData.phanTramGiam + '%' : 'N/A' }}</strong>
            </div>
            <div class="col-4">
              <span style="color:var(--z-gray)">Số tiền giảm thẳng:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.gioTriGiam ? fmtPrice(viewData.gioTriGiam) : 'N/A' }}</strong>
            </div>
            <div class="col-4">
              <span style="color:var(--z-gray)">Giảm tối đa:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.giamToiDa ? fmtPrice(viewData.giamToiDa) : 'N/A' }}</strong>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <span style="color:var(--z-gray)">Giá trị đơn tối thiểu:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.giaTriDonToiThieu ? fmtPrice(viewData.giaTriDonToiThieu) : '0đ' }}</strong>
            </div>
            <div class="col-6">
              <span style="color:var(--z-gray)">Số lượng:</span>
              <strong class="d-block text-dark mt-1">{{ viewData.soLuong }}</strong>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-6">
              <span style="color:var(--z-gray)">Ngày bắt đầu:</span>
              <strong class="d-block text-dark mt-1">{{ fmtDate(viewData.ngayBatDau) }}</strong>
            </div>
            <div class="col-6">
              <span style="color:var(--z-gray)">Ngày kết thúc:</span>
              <strong class="d-block text-dark mt-1">{{ fmtDate(viewData.ngayKetThuc) }}</strong>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-12">
              <span style="color:var(--z-gray)">Trạng thái:</span>
              <span class="z-status mt-1 d-inline-block" :class="viewData.trangThai === 1 ? 'success' : 'pending'">
                {{ viewData.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
              </span>
            </div>
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showViewModal = false">Đóng</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
function fmtDate(d) { return d ? new Date(d).toLocaleDateString('vi-VN') : '' }
function fmtPrice(v) { return v ? Number(v).toLocaleString('vi-VN') + 'đ' : '0đ' }

const vouchers = ref([])
const currentPage = ref(1)
const itemsPerPage = ref(9)
const showVoucherModal = ref(false)
const showViewModal = ref(false)
const viewData = ref(null)
const saving = ref(false)

const vForm = ref({
  id: null,
  maGiamGia: '',
  tenGiamGia: '',
  discountType: 'PERCENT',
  phanTramGiam: null,
  gioTriGiam: null,
  giaTriDonToiThieu: null,
  giamToiDa: null,
  soLuong: null,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1
})

onMounted(() => loadData())

const filteredVouchers = computed(() => vouchers.value)
const totalPages = computed(() => Math.max(1, Math.ceil(filteredVouchers.value.length / itemsPerPage.value)))
const paginatedVouchers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  return filteredVouchers.value.slice(start, start + itemsPerPage.value)
})

watch(filteredVouchers, () => {
  if (currentPage.value > totalPages.value) currentPage.value = totalPages.value
})
watch(itemsPerPage, () => {
  currentPage.value = 1
})

async function loadData() {
  currentPage.value = 1
  try {
    const data = await api().getVouchers()
    if (Array.isArray(data)) {
      vouchers.value = data.map(g => ({
        id: g.id, code: g.maGiamGia || '', name: g.tenGiamGia || '',
        discount: g.phanTramGiam ? g.phanTramGiam + '%' : (g.gioTriGiam ? Number(g.gioTriGiam).toLocaleString('vi-VN') + 'đ' : ''),
        minOrder: g.giaTriDonToiThieu ? Number(g.giaTriDonToiThieu).toLocaleString('vi-VN') + 'đ' : '0đ',
        total: g.soLuong || 0,
        period: fmtDate(g.ngayBatDau) + ' - ' + fmtDate(g.ngayKetThuc),
        ...voucherStatus(g),
        raw: g
      }))
    }
  } catch (e) { console.error('Không thể tải voucher:', e) }
}

function openAddVoucher() {
  vForm.value = {
    id: null,
    maGiamGia: '',
    tenGiamGia: '',
    discountType: 'PERCENT',
    phanTramGiam: null,
    gioTriGiam: null,
    giaTriDonToiThieu: null,
    giamToiDa: null,
    soLuong: null,
    ngayBatDau: '',
    ngayKetThuc: '',
    trangThai: 1
  }
  showVoucherModal.value = true
}

function openEditVoucher(v) {
  const raw = v.raw || {}
  vForm.value = {
    id: v.id,
    maGiamGia: raw.maGiamGia || '',
    tenGiamGia: raw.tenGiamGia || '',
    discountType: Number(raw.phanTramGiam) > 0 ? 'PERCENT' : 'FIXED',
    phanTramGiam: raw.phanTramGiam,
    gioTriGiam: raw.gioTriGiam,
    giaTriDonToiThieu: raw.giaTriDonToiThieu,
    giamToiDa: raw.giamToiDa,
    soLuong: raw.soLuong,
    ngayBatDau: raw.ngayBatDau ? raw.ngayBatDau.split('T')[0] : '',
    ngayKetThuc: raw.ngayKetThuc ? raw.ngayKetThuc.split('T')[0] : '',
    trangThai: raw.trangThai ?? 1
  }
  showVoucherModal.value = true
}

function openViewVoucher(v) {
  viewData.value = v.raw
  showViewModal.value = true
}

async function saveVoucher() {
  const form = vForm.value
  if (!form.maGiamGia?.trim()) { showToast('Vui lòng nhập mã giảm giá', 'warning'); return }
  if (!form.tenGiamGia?.trim()) { showToast('Vui lòng nhập tên voucher', 'warning'); return }
  if (form.discountType === 'PERCENT' && !(Number(form.phanTramGiam) > 0 && Number(form.phanTramGiam) <= 100)) {
    showToast('Phần trăm giảm phải lớn hơn 0 và không vượt quá 100', 'warning'); return
  }
  if (form.discountType === 'FIXED' && !(Number(form.gioTriGiam) > 0)) {
    showToast('Số tiền giảm phải lớn hơn 0', 'warning'); return
  }
  if (!form.ngayBatDau || !form.ngayKetThuc || form.ngayKetThuc < form.ngayBatDau) {
    showToast('Khoảng thời gian áp dụng voucher không hợp lệ', 'warning'); return
  }
  if (!Number.isInteger(Number(form.soLuong)) || Number(form.soLuong) < 0) {
    showToast('Số lượng voucher phải là số nguyên không âm', 'warning'); return
  }
  const payload = {
    ...form,
    maGiamGia: form.maGiamGia.trim().toUpperCase(),
    tenGiamGia: form.tenGiamGia.trim(),
    phanTramGiam: form.discountType === 'PERCENT' ? Number(form.phanTramGiam) : null,
    gioTriGiam: form.discountType === 'FIXED' ? Number(form.gioTriGiam) : null,
    giamToiDa: form.discountType === 'PERCENT' && Number(form.giamToiDa) > 0 ? Number(form.giamToiDa) : null,
    giaTriDonToiThieu: Number(form.giaTriDonToiThieu) || 0,
    soLuong: Number(form.soLuong)
  }
  delete payload.discountType
  saving.value = true
  try {
    if (form.id) {
      await api().updateVoucher(form.id, payload)
      showToast('Cập nhật voucher thành công!')
    } else {
      await api().addVoucher(payload)
      showToast('Thêm voucher thành công!')
    }
    showVoucherModal.value = false
    await loadData()
  } catch (e) { showToast('Lỗi: ' + (e.message || '')) }
  finally { saving.value = false }
}

function voucherStatus(voucher) {
  const today = new Date().toISOString().slice(0, 10)
  if (!(voucher.trangThai === 1 || voucher.trangThai === true)) return { statusLabel: 'Ngừng hoạt động', statusClass: 'pending' }
  if (Number(voucher.soLuong) <= 0) return { statusLabel: 'Hết lượt', statusClass: 'pending' }
  if (voucher.ngayBatDau && voucher.ngayBatDau > today) return { statusLabel: 'Sắp diễn ra', statusClass: 'info' }
  if (voucher.ngayKetThuc && voucher.ngayKetThuc < today) return { statusLabel: 'Hết hạn', statusClass: 'pending' }
  return { statusLabel: 'Đang áp dụng', statusClass: 'success' }
}

async function deactivateVoucher(v) {
  if (!await confirmDialog({
    title: 'Ngừng voucher',
    message: `Ngừng hoạt động voucher "${v.code}"? Các đơn đã dùng voucher vẫn được giữ nguyên.`,
    confirmText: 'Ngừng hoạt động',
    variant: 'danger'
  })) return
  try {
    await api().deleteVoucher(v.id)
    showToast('Đã ngừng hoạt động voucher')
    await loadData()
  } catch (e) { showToast('Không thể ngừng voucher: ' + (e.message || 'Lỗi không xác định'), 'error') }
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
.z-page-btn { padding: 6px 12px; height: auto; border-radius: 6px; font-size: 12px; }
.z-page-btn.active { background: var(--z-dark); color: #fff; border-color: var(--z-dark); }
</style>
