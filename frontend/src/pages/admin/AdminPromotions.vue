<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500">Đợt khuyến mãi</h1>
        <p style="font-size:13px;color:var(--z-gray);margin:0">{{ campaigns.length }} đợt đã tạo</p>
      </div>
      <button class="lm-btn-primary" @click="openCreate"><i class="bi bi-plus-lg"></i><span>Tạo đợt</span></button>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <div class="table-responsive">
        <table class="z-table" style="min-width:920px">
          <thead>
            <tr>
              <th>Mã / Tên đợt</th>
              <th>Mức giảm</th>
              <th>Thời gian</th>
              <th>Phạm vi</th>
              <th>Trạng thái</th>
              <th style="width:96px"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="campaign in pagedCampaigns" :key="campaign.id">
              <td><strong>{{ campaign.tenDot }}</strong><div class="z-subtext">{{ campaign.maDot }}</div></td>
              <td>{{ discountLabel(campaign) }}</td>
              <td>
                <div>{{ formatDate(campaign.ngayBatDau) }}</div>
                <div class="z-subtext">đến {{ formatDate(campaign.ngayKetThuc) }}</div>
              </td>
              <td><span class="z-scope-summary">{{ scopeSummary(campaign.phamVis) }}</span></td>
              <td><span class="z-status" :class="statusInfo(campaign).cls">{{ statusInfo(campaign).label }}</span></td>
              <td>
                <div class="d-flex gap-1">
                  <button type="button" class="z-icon-btn" title="Sửa đợt khuyến mãi" aria-label="Sửa đợt khuyến mãi" @click="openEdit(campaign)"><i class="bi bi-pencil"></i></button>
                  <button type="button" class="z-icon-btn" title="Xoá đợt khuyến mãi" aria-label="Xóa đợt khuyến mãi" @click="removeCampaign(campaign)"><i class="bi bi-trash"></i></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="!campaigns.length" class="text-center py-5">
        <i class="bi bi-calendar2-event" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="font-size:13px;color:var(--z-gray);margin:8px 0 0">Chưa có đợt khuyến mãi.</p>
      </div>
    </div>
    <div v-if="campaigns.length" class="z-list-pagination">
      <span>
        Hiển thị {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, campaigns.length) }}
        / {{ campaigns.length }} đợt
      </span>
      <PageSizeSelect v-model="pageSize" :options="[5, 10, 20, 50]" />
      <div v-if="totalPages > 1" class="d-flex gap-2">
        <button class="lm-btn-secondary z-page-button" :disabled="currentPage === 1" @click="currentPage--">Trước</button>
        <span class="z-page-label">Trang {{ currentPage }} / {{ totalPages }}</span>
        <button class="lm-btn-secondary z-page-button" :disabled="currentPage === totalPages" @click="currentPage++">Sau</button>
      </div>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="closeModal">
      <div class="z-modal" style="max-width:900px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Sửa đợt khuyến mãi' : 'Tạo đợt khuyến mãi' }}</h3>
            <div style="font-size:12px;color:var(--z-gray)">Giá cuối cùng được chọn theo đợt giúp khách tiết kiệm nhiều nhất.</div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu đợt khuyến mãi" @click="closeModal"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="row g-3">
          <div class="col-md-4"><label class="z-label">Mã đợt</label><input v-model="form.maDot" class="lm-input" placeholder="Tự sinh nếu để trống"></div>
          <div class="col-md-8"><label class="z-label">Tên đợt *</label><input v-model="form.tenDot" class="lm-input" placeholder="Ưu đãi cuối tuần"></div>
          <div class="col-md-4">
            <label class="z-label">Loại giảm *</label>
            <select v-model="form.loaiGiam" class="lm-input"><option value="PERCENT">Phần trăm</option><option value="FIXED">Số tiền</option></select>
          </div>
          <div class="col-md-4">
            <label class="z-label">Giá trị giảm *</label>
            <input v-if="form.loaiGiam === 'PERCENT'" v-model.number="form.giaTriGiam" type="number" min="1" max="100" class="lm-input" placeholder="10">
            <input v-else :value="formatMoneyInput(form.giaTriGiam)" inputmode="numeric" class="lm-input" placeholder="200.000" @input="onMoneyInput">
          </div>
          <div class="col-md-2"><label class="z-label">Ưu tiên</label><input v-model.number="form.doUuTien" type="number" min="0" class="lm-input"></div>
          <div class="col-md-2 d-flex align-items-end pb-2">
            <label class="d-flex align-items-center gap-2" style="font-size:13px;cursor:pointer"><input v-model="form.active" type="checkbox"> Hoạt động</label>
          </div>
          <div class="col-md-6"><label class="z-label">Bắt đầu *</label><input v-model="form.ngayBatDau" type="datetime-local" class="lm-input"></div>
          <div class="col-md-6"><label class="z-label">Kết thúc *</label><input v-model="form.ngayKetThuc" type="datetime-local" class="lm-input"></div>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-4 mb-2">
          <h4 style="font-size:14px;font-weight:600;margin:0">Phạm vi áp dụng</h4>
          <button class="lm-btn-secondary" style="height:34px;padding:6px 12px" @click="addScope"><i class="bi bi-plus"></i> Thêm phạm vi</button>
        </div>
        <div class="z-promotion-scopes">
          <div v-for="(scope, index) in form.phamVis" :key="index" class="z-promotion-scope">
            <select v-model="scope.idSanPham" class="lm-input"><option :value="null">Tất cả sản phẩm</option><option v-for="product in products" :key="product.id" :value="product.id">{{ product.maSanPham }} · {{ product.tenSanPham }}</option></select>
            <select v-model="scope.idLoaiSanPham" class="lm-input"><option :value="null">Tất cả loại</option><option v-for="item in attributes.loaiSanPham" :key="item.id" :value="item.id">{{ item.tenLoaiSanPham }}</option></select>
            <select v-model="scope.idMauSac" class="lm-input"><option :value="null">Tất cả màu</option><option v-for="item in attributes.mauSac" :key="item.id" :value="item.id">{{ item.tenMauSac }}</option></select>
            <select v-model="scope.idKichThuoc" class="lm-input"><option :value="null">Tất cả size</option><option v-for="item in attributes.kichThuoc" :key="item.id" :value="item.id">{{ item.tenKichThuoc }}</option></select>
            <button type="button" class="z-icon-btn" title="Bỏ phạm vi" aria-label="Bỏ phạm vi áp dụng" :disabled="form.phamVis.length === 1" @click="form.phamVis.splice(index, 1)"><i class="bi bi-trash"></i></button>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4 pt-3" style="border-top:1px solid var(--z-gray-border)">
          <button class="lm-btn-secondary" @click="closeModal">Đóng</button>
          <button class="lm-btn-primary" :disabled="saving" @click="saveCampaign"><span>{{ saving ? 'Đang lưu...' : 'Lưu đợt khuyến mãi' }}</span></button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const campaigns = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const products = ref([])
const attributes = ref({ loaiSanPham: [], mauSac: [], kichThuoc: [] })
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)
const totalPages = computed(() => Math.max(1, Math.ceil(campaigns.value.length / pageSize.value)))
const pagedCampaigns = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return campaigns.value.slice(start, start + pageSize.value)
})

watch(pageSize, () => { currentPage.value = 1 })
watch(totalPages, total => {
  if (currentPage.value > total) currentPage.value = total
})

function emptyScope() { return { idSanPham: null, idLoaiSanPham: null, idMauSac: null, idKichThuoc: null } }
function defaultForm() {
  const start = new Date(Date.now() + 60 * 60 * 1000)
  const end = new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000)
  return {
    maDot: '', tenDot: '', loaiGiam: 'PERCENT', giaTriGiam: 10,
    ngayBatDau: toLocalInput(start), ngayKetThuc: toLocalInput(end),
    doUuTien: 0, active: true, phamVis: [emptyScope()]
  }
}
const form = ref(defaultForm())

onMounted(async () => {
  await Promise.all([loadCampaigns(), loadOptions()])
})

async function loadCampaigns() {
  try { campaigns.value = await api().getPromotions() || [] }
  catch (e) { showToast(e.error || 'Không thể tải đợt khuyến mãi') }
}

async function loadOptions() {
  try {
    const [productData, attrData] = await Promise.all([api().getSanPham(), api().getThuocTinh()])
    products.value = productData || []
    attributes.value = attrData || attributes.value
  } catch (e) { showToast(e.error || 'Không thể tải phạm vi áp dụng') }
}

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openEdit(campaign) {
  editingId.value = campaign.id
  form.value = {
    maDot: campaign.maDot || '', tenDot: campaign.tenDot || '', loaiGiam: campaign.loaiGiam || 'PERCENT',
    giaTriGiam: Number(campaign.giaTriGiam || 0), ngayBatDau: toLocalInput(campaign.ngayBatDau),
    ngayKetThuc: toLocalInput(campaign.ngayKetThuc), doUuTien: Number(campaign.doUuTien || 0),
    active: Number(campaign.trangThai) === 1,
    phamVis: campaign.phamVis?.length ? campaign.phamVis.map(scope => ({
      idSanPham: scope.idSanPham || null, idLoaiSanPham: scope.idLoaiSanPham || null,
      idMauSac: scope.idMauSac || null, idKichThuoc: scope.idKichThuoc || null
    })) : [emptyScope()]
  }
  showModal.value = true
}

function closeModal() { showModal.value = false }
function addScope() { form.value.phamVis.push(emptyScope()) }

async function saveCampaign() {
  if (form.value.tenDot.trim().length < 3) return showToast('Vui lòng nhập tên đợt khuyến mãi')
  if (!form.value.ngayBatDau || !form.value.ngayKetThuc) return showToast('Vui lòng chọn đủ thời gian')
  if (new Date(form.value.ngayKetThuc) <= new Date(form.value.ngayBatDau)) return showToast('Thời gian kết thúc phải sau thời gian bắt đầu')
  if (Number(form.value.giaTriGiam) <= 0 || (form.value.loaiGiam === 'PERCENT' && Number(form.value.giaTriGiam) > 100)) return showToast('Giá trị giảm không hợp lệ')
  const payload = {
    ...form.value,
    maDot: form.value.maDot.trim() || null,
    tenDot: form.value.tenDot.trim(),
    trangThai: form.value.active ? 1 : 0,
    ngayBatDau: form.value.ngayBatDau.length === 16 ? `${form.value.ngayBatDau}:00` : form.value.ngayBatDau,
    ngayKetThuc: form.value.ngayKetThuc.length === 16 ? `${form.value.ngayKetThuc}:00` : form.value.ngayKetThuc
  }
  delete payload.active
  saving.value = true
  try {
    if (editingId.value) await api().updatePromotion(editingId.value, payload)
    else await api().createPromotion(payload)
    showToast(editingId.value ? 'Đã cập nhật đợt khuyến mãi' : 'Đã tạo đợt khuyến mãi')
    closeModal()
    await loadCampaigns()
  } catch (e) { showToast(e.error || e.detail || 'Không thể lưu đợt khuyến mãi') }
  finally { saving.value = false }
}

async function removeCampaign(campaign) {
  const accepted = await confirmDialog({ title: 'Xoá đợt khuyến mãi', message: `Xoá đợt “${campaign.tenDot}”?`, confirmText: 'Xoá', variant: 'danger' })
  if (!accepted) return
  try { await api().deletePromotion(campaign.id); await loadCampaigns(); showToast('Đã xoá đợt khuyến mãi') }
  catch (e) { showToast(e.error || 'Không thể xoá đợt khuyến mãi') }
}

function statusInfo(campaign) {
  return {
    ACTIVE: { label: 'Đang diễn ra', cls: 'success' }, UPCOMING: { label: 'Sắp diễn ra', cls: 'warning' },
    ENDED: { label: 'Đã kết thúc', cls: 'pending' }, INACTIVE: { label: 'Tạm dừng', cls: 'danger' }
  }[campaign.trangThaiHienTai] || { label: 'Tạm dừng', cls: 'pending' }
}
function discountLabel(campaign) { return campaign.loaiGiam === 'PERCENT' ? `${Number(campaign.giaTriGiam)}%` : formatMoney(campaign.giaTriGiam) }
function formatMoney(value) { return Number(value || 0).toLocaleString('vi-VN') + 'đ' }
function formatMoneyInput(value) { return value === '' || value == null ? '' : Number(value).toLocaleString('vi-VN') }
function onMoneyInput(event) { const digits = event.target.value.replace(/\D/g, '').slice(0, 15); form.value.giaTriGiam = digits ? Number(digits) : null; event.target.value = formatMoneyInput(form.value.giaTriGiam) }
function formatDate(value) { return value ? new Date(value).toLocaleString('vi-VN') : '' }
function toLocalInput(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 16)
}
function scopeSummary(scopes = []) {
  if (!scopes.length || scopes.some(scope => !scope.idSanPham && !scope.idLoaiSanPham && !scope.idMauSac && !scope.idKichThuoc)) return 'Toàn bộ sản phẩm'
  return scopes.map(scope => [scope.tenSanPham, scope.tenLoaiSanPham, scope.tenMauSac, scope.tenKichThuoc].filter(Boolean).join(' · ')).join('; ')
}
</script>

<style scoped>
.z-subtext { color: var(--z-gray); font-size: 11px; margin-top: 2px; }
.z-scope-summary { display: block; max-width: 260px; font-size: 12px; color: var(--z-gray); line-height: 1.5; }
.z-icon-btn { width:32px;height:32px;border:0;background:transparent;display:grid;place-items:center;color:var(--z-gray); }
.z-icon-btn:hover { background:var(--z-bg-alt);color:var(--z-dark); }
.z-modal-overlay { position:fixed;inset:0;z-index:1200;display:flex;align-items:center;justify-content:center;padding:20px;background:rgba(0,0,0,.5);backdrop-filter:blur(2px); }
.z-modal { width:100%;max-height:90vh;overflow-y:auto;background:var(--z-white);padding:28px;box-shadow:0 20px 60px rgba(0,0,0,.18); }
.z-label { display:block;font-size:12px;font-weight:500;color:var(--z-gray);margin-bottom:6px; }
.z-promotion-scopes { display:grid;gap:8px; }
.z-promotion-scope { display:grid;grid-template-columns:1.35fr 1fr 1fr 1fr 36px;gap:8px;padding:10px;background:var(--z-bg-alt); }
.z-promotion-scope .lm-input { padding:8px;font-size:12px; }
.z-list-pagination { display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-top:16px;color:var(--z-gray);font-size:12px; }
.z-page-button { min-width:68px;height:34px;padding:6px 12px; }
.z-page-label { display:inline-flex;align-items:center;padding:0 6px;color:var(--z-dark); }
@media (max-width:800px) { .z-promotion-scope { grid-template-columns:1fr; } }
</style>
