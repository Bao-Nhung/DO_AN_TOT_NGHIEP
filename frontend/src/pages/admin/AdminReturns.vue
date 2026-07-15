<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center gap-3 mb-4 flex-wrap">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500">Đổi và trả hàng</h1>
        <p class="z-page-subtitle">Theo dõi từng yêu cầu từ lúc tiếp nhận đến khi đổi hàng hoặc hoàn tiền.</p>
      </div>
      <button class="lm-btn-primary" @click="openOffline"><i class="bi bi-shop"></i><span>Xử lý tại quầy</span></button>
    </div>

    <div class="z-return-toolbar">
      <div class="z-segmented">
        <button :class="{ active: activeType === 'DOI' }" @click="setType('DOI')">Đổi hàng</button>
        <button :class="{ active: activeType === 'TRA' }" @click="setType('TRA')">Trả hàng</button>
      </div>
      <select v-model="statusFilter" class="lm-input z-status-filter" @change="loadRequests">
        <option value="">Tất cả trạng thái</option>
        <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <span class="z-result-count">{{ requests.length }} yêu cầu</span>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <div v-if="loading" class="text-center py-5"><div class="spinner-border spinner-border-sm"></div></div>
      <div v-else-if="!requests.length" class="z-empty-return">
        <i class="bi bi-arrow-left-right"></i>
        <strong>Chưa có yêu cầu {{ activeType === 'DOI' ? 'đổi' : 'trả' }} hàng</strong>
        <span>Các yêu cầu mới sẽ xuất hiện tại đây.</span>
      </div>
      <div v-else class="table-responsive">
        <table class="z-table" style="min-width:1040px">
          <thead><tr><th>Yêu cầu / Đơn</th><th>Khách hàng</th><th>Sản phẩm</th><th>Nguồn</th><th>Trạng thái</th><th>Ngày tạo</th><th style="width:132px"></th></tr></thead>
          <tbody>
            <tr v-for="item in requests" :key="item.id">
              <td><strong>#{{ item.id }}</strong><div class="z-subtext">{{ item.orderCode }}</div></td>
              <td><strong>{{ item.customerName }}</strong><div class="z-subtext">{{ item.customerPhone }}</div></td>
              <td>
                <div class="z-product-cell">
                  <img v-if="item.productImage" :src="item.productImage" alt="">
                  <span v-else class="z-product-placeholder"><i class="bi bi-image"></i></span>
                  <div><strong>{{ item.productName }}</strong><div class="z-subtext">{{ item.productCode }} · {{ item.color }} · {{ item.size }} · SL {{ item.quantity }}</div></div>
                </div>
              </td>
              <td><span class="z-source" :class="item.source.toLowerCase()">{{ item.source === 'ONLINE' ? 'Online' : 'Tại quầy' }}</span></td>
              <td><span class="z-status" :class="statusInfo(item.status).cls">{{ statusInfo(item.status).label }}</span></td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td><button class="lm-btn-secondary z-small-btn" @click="openDetail(item)"><i class="bi bi-eye"></i><span>Xem xử lý</span></button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="detail" class="z-modal-overlay" @click.self="detail = null">
      <div class="z-modal z-return-detail-modal">
        <div class="z-modal-head">
          <div><h3>Yêu cầu {{ detail.type === 'DOI' ? 'đổi' : 'trả' }} #{{ detail.id }}</h3><span>{{ detail.orderCode }} · {{ detail.source === 'ONLINE' ? 'Đơn online' : 'Đơn tại quầy' }}</span></div>
          <button class="z-icon-btn" @click="detail = null"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="z-return-detail-grid">
          <section>
            <h4>Khách hàng và sản phẩm</h4>
            <dl><dt>Khách hàng</dt><dd>{{ detail.customerName }} · {{ detail.customerPhone }}</dd><dt>Sản phẩm</dt><dd>{{ detail.productCode }} · {{ detail.productName }}</dd><dt>Biến thể gửi lại</dt><dd>{{ detail.color }} · Size {{ detail.size }} · {{ detail.quantity }} sản phẩm</dd><template v-if="detail.replacement"><dt>Biến thể đổi mới</dt><dd>{{ detail.replacement.color }} · Size {{ detail.replacement.size }}</dd></template></dl>
          </section>
          <section>
            <h4>Nội dung yêu cầu</h4>
            <dl><dt>Lý do</dt><dd>{{ detail.reason }}</dd><dt v-if="detail.condition">Tình trạng hàng</dt><dd v-if="detail.condition">{{ detail.condition }}</dd><dt v-if="detail.refundInfo">Nhận tiền hoàn</dt><dd v-if="detail.refundInfo">{{ detail.refundInfo }}</dd><dt v-if="detail.rejectionReason">Lý do từ chối/trả lại</dt><dd v-if="detail.rejectionReason" class="text-danger">{{ detail.rejectionReason }}</dd><dt v-if="detail.employeeName">Nhân viên xử lý</dt><dd v-if="detail.employeeName">{{ detail.employeeName }}</dd></dl>
          </section>
        </div>

        <div v-if="detail.images?.length" class="z-evidence-section">
          <h4>Ảnh tình trạng hàng</h4>
          <div class="z-evidence-list"><a v-for="url in detail.images" :key="url" :href="url" target="_blank" rel="noopener"><img :src="url" alt="Ảnh tình trạng hàng"></a></div>
        </div>

        <div class="z-process-state"><span class="z-status" :class="statusInfo(detail.status).cls">{{ statusInfo(detail.status).label }}</span><span>{{ processHint(detail.status) }}</span></div>
        <div class="z-modal-actions">
          <button class="lm-btn-secondary" @click="detail = null">Đóng</button>
          <template v-if="detail.status === 'CHO_DUYET'">
            <button class="lm-btn-secondary z-danger-text" @click="openAction('reject')"><i class="bi bi-x-circle"></i><span>Từ chối</span></button>
            <button class="lm-btn-primary" @click="openAction('approve')"><i class="bi bi-check2"></i><span>Duyệt yêu cầu</span></button>
          </template>
          <template v-if="detail.status === 'CHO_NHAN_HANG'">
            <button class="lm-btn-secondary z-danger-text" @click="openAction('sendBack')"><i class="bi bi-arrow-return-left"></i><span>Không đạt, trả khách</span></button>
            <button class="lm-btn-primary" @click="openAction('receive')"><i class="bi bi-box-seam"></i><span>Đã nhận hàng</span></button>
          </template>
          <button v-if="detail.status === 'CHO_HOAN_TAT'" class="lm-btn-primary" @click="openAction('complete')"><i class="bi bi-check-circle"></i><span>{{ detail.type === 'DOI' ? 'Hoàn tất đổi hàng' : 'Xác nhận đã hoàn tiền' }}</span></button>
        </div>
      </div>
    </div>

    <div v-if="actionModal" class="z-modal-overlay" style="z-index:1100" @click.self="actionModal = null">
      <div class="z-modal" style="max-width:500px">
        <div class="z-modal-head"><div><h3>{{ actionTitle }}</h3><span>Thao tác được ghi vào lịch sử hóa đơn.</span></div><button class="z-icon-btn" @click="actionModal = null"><i class="bi bi-x-lg"></i></button></div>
        <label class="z-label">{{ actionNeedsReason ? 'Lý do *' : 'Ghi chú' }}</label>
        <textarea v-model="actionReason" class="lm-input" rows="4" :placeholder="actionNeedsReason ? 'Nhập lý do cụ thể để khách hàng hiểu rõ...' : 'Ghi chú kiểm tra hàng (không bắt buộc)...'"></textarea>
        <div class="z-modal-actions"><button class="lm-btn-secondary" @click="actionModal = null">Đóng</button><button class="lm-btn-primary" :disabled="saving" @click="submitAction">Xác nhận</button></div>
      </div>
    </div>

    <div v-if="offlineModal" class="z-modal-overlay" @click.self="closeOffline">
      <div class="z-modal" style="max-width:760px">
        <div class="z-modal-head"><div><h3>{{ offlineForm.type === 'DOI' ? 'Đổi hàng' : 'Trả hàng' }} tại quầy</h3><span>Chỉ áp dụng với hóa đơn đã thanh toán tại cửa hàng.</span></div><button class="z-icon-btn" @click="closeOffline"><i class="bi bi-x-lg"></i></button></div>
        <div class="row g-3">
          <div class="col-md-7"><label class="z-label">Hóa đơn *</label><select v-model="offlineForm.orderId" class="lm-input" @change="loadOfflineOrder"><option :value="null">Chọn hóa đơn tại quầy</option><option v-for="order in offlineOrders" :key="order.id" :value="order.id">{{ order.maHoaDon }} · {{ order.khachHang }} · {{ order.soDienThoai }}</option></select></div>
          <div class="col-md-5"><label class="z-label">Loại xử lý *</label><div class="z-segmented z-form-segment"><button :class="{ active: offlineForm.type === 'DOI' }" @click="offlineForm.type = 'DOI'; loadReplacementOptions()">Đổi</button><button :class="{ active: offlineForm.type === 'TRA' }" @click="offlineForm.type = 'TRA'">Trả</button></div></div>
          <div class="col-md-8"><label class="z-label">Sản phẩm *</label><select v-model="offlineForm.orderDetailId" class="lm-input" @change="loadReplacementOptions"><option :value="null">Chọn sản phẩm</option><option v-for="line in offlineDetail?.chiTiets || []" :key="line.id" :value="line.id">{{ line.maSanPham }} · {{ line.tenVay }} · {{ line.mauSac }} · {{ line.kichThuoc }} · SL {{ line.soLuong }}</option></select></div>
          <div class="col-md-4"><label class="z-label">Số lượng *</label><input v-model.number="offlineForm.quantity" type="number" min="1" :max="selectedOfflineLine?.soLuong || 1" class="lm-input"></div>
          <div v-if="offlineForm.type === 'DOI'" class="col-12"><label class="z-label">Màu / kích cỡ đổi mới *</label><select v-model="offlineForm.replacementVariantId" class="lm-input"><option :value="null">Chọn biến thể còn hàng</option><option v-for="variant in replacementOptions" :key="variant.id" :value="variant.id">{{ variant.mauSac }} · {{ variant.kichThuoc }} · còn {{ variant.soLuong }}</option></select></div>
          <div class="col-12"><label class="z-label">Lý do *</label><textarea v-model="offlineForm.reason" class="lm-input" rows="3" placeholder="Tình trạng và lý do khách đổi/trả..."></textarea></div>
          <div v-if="offlineForm.type === 'TRA'" class="col-12"><label class="z-label">Phương thức / thông tin hoàn tiền *</label><textarea v-model="offlineForm.refundInfo" class="lm-input" rows="2" placeholder="Ví dụ: hoàn tiền mặt tại quầy, đã trả đủ cho khách..."></textarea></div>
        </div>
        <div class="z-modal-actions"><button class="lm-btn-secondary" @click="closeOffline">Đóng</button><button class="lm-btn-primary" :disabled="saving" @click="submitOffline"><i class="bi bi-check2-circle"></i><span>Xác nhận xử lý</span></button></div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const activeType = ref('DOI')
const statusFilter = ref('')
const requests = ref([])
const loading = ref(false)
const saving = ref(false)
const detail = ref(null)
const actionModal = ref(null)
const actionReason = ref('')
const offlineModal = ref(false)
const offlineOrders = ref([])
const offlineDetail = ref(null)
const replacementOptions = ref([])
const offlineForm = ref(defaultOfflineForm())

const statusOptions = [
  { value: 'CHO_DUYET', label: 'Chờ duyệt' }, { value: 'CHO_NHAN_HANG', label: 'Chờ khách gửi hàng' },
  { value: 'CHO_HOAN_TAT', label: 'Chờ hoàn tất' }, { value: 'TU_CHOI', label: 'Đã từ chối' },
  { value: 'TRA_LAI_KHACH', label: 'Trả lại khách' }, { value: 'DA_DOI', label: 'Đã đổi hàng' },
  { value: 'DA_HOAN_TIEN', label: 'Đã hoàn tiền' }
]

const selectedOfflineLine = computed(() => (offlineDetail.value?.chiTiets || []).find(line => Number(line.id) === Number(offlineForm.value.orderDetailId)))
const actionNeedsReason = computed(() => ['reject', 'sendBack'].includes(actionModal.value))
const actionTitle = computed(() => ({ approve: 'Duyệt yêu cầu', reject: 'Từ chối yêu cầu', receive: 'Xác nhận đã nhận hàng', sendBack: 'Trả lại hàng cho khách', complete: detail.value?.type === 'DOI' ? 'Hoàn tất đổi hàng' : 'Xác nhận hoàn tiền' }[actionModal.value] || 'Xác nhận'))

onMounted(loadRequests)

async function loadRequests() {
  loading.value = true
  try { requests.value = await api().getReturnRequests({ type: activeType.value, status: statusFilter.value }) || [] }
  catch (error) { showToast(error.error || error.message || 'Không thể tải danh sách đổi trả', 'error') }
  finally { loading.value = false }
}

function setType(type) { activeType.value = type; loadRequests() }
function openDetail(item) { detail.value = item }
function openAction(type) { actionModal.value = type; actionReason.value = '' }

async function submitAction() {
  if (actionNeedsReason.value && actionReason.value.trim().length < 5) return showToast('Vui lòng nhập lý do ít nhất 5 ký tự', 'warning')
  saving.value = true
  try {
    let updated
    if (actionModal.value === 'approve') updated = await api().reviewReturnRequest(detail.value.id, true, actionReason.value.trim())
    if (actionModal.value === 'reject') updated = await api().reviewReturnRequest(detail.value.id, false, actionReason.value.trim())
    if (actionModal.value === 'receive') updated = await api().receiveReturnRequest(detail.value.id, true, actionReason.value.trim())
    if (actionModal.value === 'sendBack') updated = await api().receiveReturnRequest(detail.value.id, false, actionReason.value.trim())
    if (actionModal.value === 'complete') updated = await api().completeReturnRequest(detail.value.id, actionReason.value.trim())
    detail.value = updated
    actionModal.value = null
    showToast('Đã cập nhật yêu cầu đổi trả', 'success')
    await loadRequests()
  } catch (error) { showToast(error.error || error.message || 'Không thể cập nhật yêu cầu', 'error') }
  finally { saving.value = false }
}

function defaultOfflineForm() { return { orderId: null, orderDetailId: null, type: activeType.value || 'DOI', quantity: 1, reason: '', refundInfo: '', replacementVariantId: null } }

async function openOffline() {
  offlineForm.value = defaultOfflineForm()
  offlineModal.value = true
  try {
    const data = await api().getHoaDonPage({ page: 0, size: 200, orderType: 0 })
    offlineOrders.value = (data.content || []).filter(order => order.daThanhToan)
  } catch (error) { showToast(error.error || 'Không thể tải hóa đơn tại quầy', 'error') }
}

function closeOffline() { offlineModal.value = false; offlineDetail.value = null; replacementOptions.value = [] }

async function loadOfflineOrder() {
  offlineDetail.value = null
  offlineForm.value.orderDetailId = null
  replacementOptions.value = []
  if (!offlineForm.value.orderId) return
  try { offlineDetail.value = await api().getHoaDonById(offlineForm.value.orderId) }
  catch (error) { showToast(error.error || 'Không thể tải chi tiết hóa đơn', 'error') }
}

async function loadReplacementOptions() {
  offlineForm.value.replacementVariantId = null
  replacementOptions.value = []
  if (offlineForm.value.type !== 'DOI' || !selectedOfflineLine.value?.productId) return
  try {
    const product = await api().getVayById(selectedOfflineLine.value.productId)
    replacementOptions.value = (product.bienThe || []).filter(variant => Number(variant.id) !== Number(selectedOfflineLine.value.variantId) && Number(variant.trangThai) === 1 && Number(variant.soLuong) > 0)
  } catch (error) { showToast(error.error || 'Không thể tải biến thể đổi', 'error') }
}

async function submitOffline() {
  const form = offlineForm.value
  if (!form.orderId || !form.orderDetailId) return showToast('Vui lòng chọn hóa đơn và sản phẩm', 'warning')
  if (!form.quantity || form.quantity < 1 || form.quantity > Number(selectedOfflineLine.value?.soLuong || 0)) return showToast('Số lượng không hợp lệ', 'warning')
  if (form.reason.trim().length < 5) return showToast('Vui lòng nhập lý do ít nhất 5 ký tự', 'warning')
  if (form.type === 'DOI' && !form.replacementVariantId) return showToast('Vui lòng chọn biến thể đổi mới', 'warning')
  if (form.type === 'TRA' && form.refundInfo.trim().length < 3) return showToast('Vui lòng ghi thông tin hoàn tiền', 'warning')
  const accepted = await confirmDialog({ title: `Xác nhận ${form.type === 'DOI' ? 'đổi' : 'trả'} tại quầy`, message: 'Tồn kho sẽ được cập nhật ngay và thao tác không thể thực hiện lại trên dòng hàng này.', confirmText: 'Xác nhận xử lý', variant: 'danger' })
  if (!accepted) return
  saving.value = true
  try {
    await api().createOfflineReturnRequest({ ...form, reason: form.reason.trim(), refundInfo: form.refundInfo.trim() || null })
    showToast('Đã xử lý đổi trả tại quầy', 'success')
    closeOffline()
    activeType.value = form.type
    await loadRequests()
  } catch (error) { showToast(error.error || error.message || 'Không thể xử lý tại quầy', 'error') }
  finally { saving.value = false }
}

function statusInfo(status) {
  return {
    CHO_DUYET: { label: 'Chờ duyệt', cls: 'pending' }, CHO_NHAN_HANG: { label: 'Chờ nhận hàng', cls: 'waiting' },
    CHO_HOAN_TAT: { label: 'Chờ hoàn tất', cls: 'processing' }, TU_CHOI: { label: 'Đã từ chối', cls: 'rejected' },
    TRA_LAI_KHACH: { label: 'Trả lại khách', cls: 'rejected' }, DA_DOI: { label: 'Đã đổi hàng', cls: 'done' },
    DA_HOAN_TIEN: { label: 'Đã hoàn tiền', cls: 'done' }
  }[status] || { label: status, cls: 'pending' }
}

function processHint(status) {
  return { CHO_DUYET: 'Nhân viên cần kiểm tra bằng chứng và nội dung yêu cầu.', CHO_NHAN_HANG: 'Đã duyệt, đang chờ sản phẩm được gửi về cửa hàng.', CHO_HOAN_TAT: 'Hàng đã được kiểm tra, sẵn sàng hoàn tiền hoặc đổi biến thể.', TU_CHOI: 'Yêu cầu không đủ điều kiện.', TRA_LAI_KHACH: 'Hàng gửi về không đạt điều kiện và sẽ được trả lại.', DA_DOI: 'Đã hoàn tất đổi hàng và cập nhật tồn kho.', DA_HOAN_TIEN: 'Đã hoàn tiền và cập nhật tồn kho.' }[status] || ''
}

function formatDate(value) { return value ? new Date(value).toLocaleString('vi-VN') : '' }
</script>

<style scoped>
.z-page-subtitle,.z-subtext{font-size:12px;color:var(--z-gray);margin:0}.z-return-toolbar{display:flex;align-items:center;gap:12px;margin-bottom:16px}.z-segmented{display:inline-grid;grid-template-columns:1fr 1fr;border:1px solid var(--z-gray-border);background:var(--z-white);padding:3px;border-radius:var(--z-radius)}.z-segmented button{border:0;background:transparent;min-width:112px;height:34px;padding:0 14px;color:var(--z-gray);font-size:13px}.z-segmented button.active{background:var(--z-dark);color:var(--z-white)}.z-status-filter{width:210px;height:42px}.z-result-count{margin-left:auto;font-size:12px;color:var(--z-gray)}.z-empty-return{min-height:280px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;color:var(--z-gray)}.z-empty-return i{font-size:34px;color:var(--z-gray-light)}.z-empty-return strong{color:var(--z-dark)}.z-product-cell{display:flex;align-items:center;gap:10px;min-width:280px}.z-product-cell img,.z-product-placeholder{width:42px;height:50px;object-fit:cover;background:var(--z-bg-alt);display:grid;place-items:center;border-radius:var(--z-radius);flex:none}.z-source{font-size:11px;padding:4px 8px;border:1px solid var(--z-gray-border);border-radius:20px}.z-source.online{color:#1769aa;background:#eef7ff}.z-source.offline{color:#6b4d00;background:#fff8df}.z-status{display:inline-flex;padding:5px 9px;border-radius:20px;font-size:11px;font-weight:600}.z-status.pending{background:#fff4d6;color:#8a5b00}.z-status.waiting{background:#eaf5ff;color:#1769aa}.z-status.processing{background:#f2edff;color:#6440a4}.z-status.rejected{background:#ffeded;color:#b42318}.z-status.done{background:#eaf8ee;color:#217a3d}.z-small-btn{height:34px;padding:6px 10px;font-size:12px}.z-modal-head{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;margin-bottom:20px}.z-modal-head h3{font-size:18px;font-weight:600;margin:0}.z-modal-head span{font-size:12px;color:var(--z-gray)}.z-return-detail-modal{max-width:860px}.z-return-detail-grid{display:grid;grid-template-columns:1fr 1fr;gap:28px}.z-return-detail-grid section{min-width:0}.z-return-detail-grid h4,.z-evidence-section h4{font-size:13px;font-weight:700;margin:0 0 10px}.z-return-detail-grid dl{display:grid;grid-template-columns:125px 1fr;gap:7px 12px;font-size:13px}.z-return-detail-grid dt{font-weight:500;color:var(--z-gray)}.z-return-detail-grid dd{margin:0;overflow-wrap:anywhere}.z-evidence-section{border-top:1px solid var(--z-gray-border);padding-top:16px;margin-top:16px}.z-evidence-list{display:flex;gap:8px;overflow-x:auto}.z-evidence-list img{width:88px;height:104px;object-fit:cover;border:1px solid var(--z-gray-border);border-radius:var(--z-radius)}.z-process-state{display:flex;align-items:center;gap:10px;background:var(--z-bg-alt);padding:12px;margin-top:18px;font-size:12px;color:var(--z-gray)}.z-modal-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:22px;padding-top:16px;border-top:1px solid var(--z-gray-border)}.z-danger-text{color:var(--z-danger)}.z-label{display:block;font-size:12px;font-weight:600;margin-bottom:6px}.z-form-segment{display:grid;width:100%;height:42px}.z-form-segment button{height:34px;min-width:0}.z-form-segment button:not(.active){color:var(--z-gray)}
@media(max-width:767px){.z-return-toolbar{align-items:stretch;flex-direction:column}.z-status-filter{width:100%}.z-result-count{margin:0}.z-return-detail-grid{grid-template-columns:1fr}.z-return-detail-modal{max-height:92vh;overflow-y:auto}.z-modal-actions{flex-wrap:wrap}.z-modal-actions button{flex:1;min-width:130px}}
</style>
