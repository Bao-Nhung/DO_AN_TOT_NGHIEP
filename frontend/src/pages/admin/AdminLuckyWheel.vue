<template>
  <AdminLayout>
    <div class="z-lucky-admin-header">
      <div>
        <h1 class="z-display">Vòng quay may mắn</h1>
        <p>Quản lý chiến dịch, kho quà hiện vật và lịch sử trao thưởng độc lập với đơn hàng.</p>
      </div>
      <button type="button" class="lm-btn-primary" @click="openCampaign()">
        <i class="bi bi-plus-lg"></i><span>Tạo chiến dịch</span>
      </button>
    </div>

    <div v-if="loading" class="z-admin-card z-admin-loading">
      <span class="spinner-border spinner-border-sm"></span><span>Đang tải dữ liệu vòng quay...</span>
    </div>

    <template v-else>
      <div class="z-campaign-strip">
        <button
          v-for="campaign in campaigns"
          :key="campaign.id"
          type="button"
          :class="{ active: selectedCampaignId === campaign.id }"
          @click="selectedCampaignId = campaign.id"
        >
          <span class="z-campaign-icon"><i class="bi bi-stars"></i></span>
          <span class="z-campaign-copy">
            <strong>{{ campaign.tenChienDich }}</strong>
            <small>{{ campaign.maChienDich }} · {{ statusInfo(campaign).label }}</small>
          </span>
          <span class="z-campaign-count">{{ campaign.tongLuotQuay }}</span>
        </button>
        <div v-if="!campaigns.length" class="z-no-campaign">
          <i class="bi bi-stars"></i><span>Chưa có chiến dịch vòng quay.</span>
        </div>
      </div>

      <template v-if="selectedCampaign">
        <section class="z-admin-card z-campaign-overview">
          <div class="z-campaign-title-row">
            <div>
              <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                <span class="z-status" :class="statusInfo(selectedCampaign).cls">{{ statusInfo(selectedCampaign).label }}</span>
                <span class="z-code-pill">{{ selectedCampaign.maChienDich }}</span>
              </div>
              <h2>{{ selectedCampaign.tenChienDich }}</h2>
              <p>{{ selectedCampaign.moTa || 'Chưa có mô tả chiến dịch.' }}</p>
            </div>
            <button type="button" class="z-admin-action" @click="openCampaign(selectedCampaign)">
              <i class="bi bi-pencil-square"></i><span>Chỉnh sửa</span>
            </button>
          </div>
          <div class="z-campaign-facts">
            <div><i class="bi bi-cash-stack"></i><span>Đơn tối thiểu</span><strong>{{ money(selectedCampaign.giaTriDonToiThieu) }}</strong></div>
            <div><i class="bi bi-calendar-range"></i><span>Thời gian</span><strong>{{ shortDate(selectedCampaign.ngayBatDau) }} - {{ shortDate(selectedCampaign.ngayKetThuc) }}</strong></div>
            <div><i class="bi bi-arrow-repeat"></i><span>Lượt quay</span><strong>{{ selectedCampaign.tongLuotQuay }}</strong></div>
            <div><i class="bi bi-gift"></i><span>Đã trúng quà</span><strong>{{ selectedCampaign.tongTrungThuong }}</strong></div>
            <div><i class="bi bi-hourglass-split"></i><span>Chờ trao</span><strong>{{ selectedCampaign.choTrao }}</strong></div>
          </div>
        </section>

        <section class="z-admin-section">
          <div class="z-section-heading">
            <div><h2>Phần thưởng trên vòng quay</h2><p>Trọng số chỉ dùng ở máy chủ; số lượng quà được khóa và giảm khi có kết quả trúng.</p></div>
            <button type="button" class="z-admin-action primary" @click="openPrize()"><i class="bi bi-plus-lg"></i><span>Thêm phần thưởng</span></button>
          </div>

          <div class="z-admin-card" style="padding:0;overflow:hidden">
            <div class="table-responsive">
              <table class="z-table" style="min-width:860px">
                <thead><tr><th>Phần thưởng</th><th>Loại</th><th>Trọng số</th><th>Kho quà</th><th>Đã trúng</th><th>Trạng thái</th><th style="width:64px"></th></tr></thead>
                <tbody>
                  <tr v-for="prize in selectedCampaign.phanThuongs" :key="prize.id">
                    <td>
                      <div class="z-prize-name"><span :style="{ background: prize.mauHienThi }"><img v-if="prize.anhBieuTuong" :src="prize.anhBieuTuong" alt="" @error="hideBrokenIcon(prize)"><i v-else class="bi" :class="prize.bieuTuong"></i></span><div><strong>{{ prize.tenPhanThuong }}</strong><small>Vị trí {{ prize.thuTu }}</small></div></div>
                    </td>
                    <td><span class="z-type-pill">{{ prize.loaiPhanThuong === 'VAT_PHAM' ? 'Quà hiện vật' : 'Không trúng' }}</span></td>
                    <td><strong>{{ prize.trongSo }}</strong></td>
                    <td>{{ prize.loaiPhanThuong === 'VAT_PHAM' ? `${prize.soLuongCon} / ${prize.soLuongBanDau}` : 'Không giới hạn' }}</td>
                    <td>{{ prize.soLuotTrung }}</td>
                    <td><span class="z-status" :class="Number(prize.trangThai) === 1 ? 'success' : 'pending'">{{ Number(prize.trangThai) === 1 ? 'Đang dùng' : 'Tạm ẩn' }}</span></td>
                    <td><button type="button" class="z-icon-btn" title="Sửa phần thưởng" aria-label="Sửa phần thưởng" @click="openPrize(prize)"><i class="bi bi-pencil-square"></i></button></td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="!selectedCampaign.phanThuongs?.length" class="z-table-empty"><i class="bi bi-gift"></i><span>Chiến dịch chưa có phần thưởng.</span></div>
          </div>
        </section>

        <section class="z-admin-section">
          <div class="z-section-heading">
            <div><h2>Lịch sử lượt quay và trao quà</h2><p>Thông tin là bản chụp tại thời điểm quay, không chỉnh sửa hồ sơ khách hay hóa đơn.</p></div>
            <select v-model="spinStatus" class="lm-input z-status-filter">
              <option value="">Tất cả trạng thái</option>
              <option value="CHO_NHAN">Chờ trao quà</option>
              <option value="DA_TRA">Đã trao quà</option>
              <option value="KHONG_TRUNG">Không trúng</option>
            </select>
          </div>

          <div class="z-admin-card" style="padding:0;overflow:hidden">
            <div class="table-responsive">
              <table class="z-table" style="min-width:1050px">
                <thead><tr><th>Mã đơn / Khách hàng</th><th>Kết quả</th><th>Mã nhận quà</th><th>Giá trị đơn</th><th>Thời gian quay</th><th>Trạng thái</th><th style="width:120px"></th></tr></thead>
                <tbody>
                  <tr v-for="spin in spins" :key="spin.id">
                    <td><strong>{{ spin.maHoaDon }}</strong><div class="z-subtext">{{ spin.tenKhachHang }} · {{ spin.soDienThoai }}</div></td>
                    <td><strong>{{ spin.tenKetQua }}</strong></td>
                    <td><code v-if="spin.trungThuong">{{ spin.maNhanThuong }}</code><span v-else class="z-subtext">Không có</span></td>
                    <td>{{ money(spin.giaTriDon) }}</td>
                    <td>{{ dateTime(spin.ngayQuay) }}</td>
                    <td><span class="z-status" :class="claimStatus(spin).cls">{{ claimStatus(spin).label }}</span><div v-if="spin.ngayTrao" class="z-subtext">{{ dateTime(spin.ngayTrao) }} · {{ spin.nguoiTrao }}</div></td>
                    <td>
                      <button v-if="spin.trangThaiNhan === 'CHO_NHAN'" type="button" class="z-deliver-btn" @click="deliverPrize(spin)"><i class="bi bi-check2"></i>Đã trao</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="!spins.length" class="z-table-empty"><i class="bi bi-clock-history"></i><span>Chưa có lượt quay phù hợp.</span></div>
          </div>
          <div v-if="spinTotalItems" class="z-list-pagination">
            <span>Hiển thị {{ (spinPage - 1) * spinPageSize + 1 }}-{{ Math.min(spinPage * spinPageSize, spinTotalItems) }} / {{ spinTotalItems }} lượt</span>
            <PageSizeSelect v-model="spinPageSize" :options="[5, 10, 20, 50]" />
            <div v-if="spinTotalPages > 1" class="d-flex gap-2 align-items-center">
              <button type="button" class="z-page-btn" :disabled="spinPage === 1" @click="goToSpinPage(spinPage - 1)"><i class="bi bi-chevron-left"></i></button>
              <span>Trang {{ spinPage }} / {{ spinTotalPages }}</span>
              <button type="button" class="z-page-btn" :disabled="spinPage === spinTotalPages" @click="goToSpinPage(spinPage + 1)"><i class="bi bi-chevron-right"></i></button>
            </div>
          </div>
        </section>
      </template>
    </template>

    <div v-if="campaignModal" class="z-modal-overlay" @click.self="campaignModal = false">
      <div class="z-modal z-lucky-modal">
        <div class="z-modal-header">
          <div><h3>{{ campaignForm.id ? 'Chỉnh sửa chiến dịch' : 'Tạo chiến dịch vòng quay' }}</h3><p>Điều kiện chỉ đọc từ đơn đã hoàn tất; không tạo voucher hay thay đổi tồn kho sản phẩm.</p></div>
          <button type="button" class="z-icon-btn" aria-label="Đóng" @click="campaignModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <form @submit.prevent="saveCampaign">
          <div class="row g-3">
            <div class="col-md-4"><label class="z-label">Mã chiến dịch</label><input v-model.trim="campaignForm.maChienDich" class="lm-input" maxlength="50" placeholder="Tự sinh nếu để trống"></div>
            <div class="col-md-8"><label class="z-label">Tên chiến dịch *</label><input v-model.trim="campaignForm.tenChienDich" class="lm-input" maxlength="150" required></div>
            <div class="col-12"><label class="z-label">Mô tả</label><textarea v-model.trim="campaignForm.moTa" class="lm-input" rows="3" maxlength="500"></textarea></div>
            <div class="col-md-4"><label class="z-label">Giá trị đơn tối thiểu *</label><input :value="moneyInput(campaignForm.giaTriDonToiThieu)" class="lm-input" inputmode="numeric" required @input="campaignMoneyInput"></div>
            <div class="col-md-4"><label class="z-label">Bắt đầu *</label><input v-model="campaignForm.ngayBatDau" type="datetime-local" class="lm-input" required></div>
            <div class="col-md-4"><label class="z-label">Kết thúc *</label><input v-model="campaignForm.ngayKetThuc" type="datetime-local" class="lm-input" required></div>
            <div class="col-12"><label class="z-check"><input v-model="campaignForm.active" type="checkbox">Cho phép chiến dịch hoạt động</label></div>
          </div>
          <div class="z-modal-actions"><button type="button" class="lm-btn-secondary" @click="campaignModal = false">Đóng</button><button class="lm-btn-primary" type="submit" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Lưu chiến dịch' }}</span></button></div>
        </form>
      </div>
    </div>

    <div v-if="prizeModal" class="z-modal-overlay" @click.self="prizeModal = false">
      <div class="z-modal z-lucky-modal" style="max-width:760px">
        <div class="z-modal-header">
          <div><h3>{{ prizeForm.id ? 'Chỉnh sửa phần thưởng' : 'Thêm phần thưởng' }}</h3><p>Quà hiện vật có số lượng; ô “không trúng” không sử dụng kho.</p></div>
          <button type="button" class="z-icon-btn" aria-label="Đóng" @click="prizeModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <form @submit.prevent="savePrize">
          <div class="row g-3">
            <div class="col-md-8"><label class="z-label">Tên hiển thị *</label><input v-model.trim="prizeForm.tenPhanThuong" class="lm-input" maxlength="150" required></div>
            <div class="col-md-4"><label class="z-label">Loại *</label><select v-model="prizeForm.loaiPhanThuong" class="lm-input"><option value="VAT_PHAM">Quà hiện vật</option><option value="KHONG_TRUNG">Không trúng</option></select></div>
            <div class="col-md-4"><label class="z-label">Trọng số *</label><input v-model.number="prizeForm.trongSo" type="number" min="1" max="100000" class="lm-input" required><small class="z-help">Tỷ lệ tương đối, không hiển thị cho khách.</small></div>
            <div class="col-md-4"><label class="z-label">Số lượng quà *</label><input v-model.number="prizeForm.soLuongBanDau" type="number" min="0" class="lm-input" :disabled="prizeForm.loaiPhanThuong === 'KHONG_TRUNG'" :required="prizeForm.loaiPhanThuong === 'VAT_PHAM'"></div>
            <div class="col-md-4"><label class="z-label">Vị trí</label><input v-model.number="prizeForm.thuTu" type="number" min="0" class="lm-input"></div>
            <div class="col-md-6"><label class="z-label">Màu lát vòng quay</label><div class="z-color-control"><input v-model="prizeForm.mauHienThi" type="color"><input v-model.trim="prizeForm.mauHienThi" class="lm-input" maxlength="7"></div></div>
            <div class="col-12">
              <div class="z-icon-field-heading">
                <div><label class="z-label mb-1">Biểu tượng phần thưởng</label><small>Chọn icon có sẵn hoặc tải ảnh riêng của phần quà.</small></div>
                <div class="z-icon-mode" role="group" aria-label="Nguồn biểu tượng">
                  <button type="button" :class="{ active: prizeIconMode === 'library' }" @click="prizeIconMode = 'library'"><i class="bi bi-grid"></i><span>Thư viện</span></button>
                  <button type="button" :class="{ active: prizeIconMode === 'upload' }" @click="prizeIconMode = 'upload'"><i class="bi bi-cloud-arrow-up"></i><span>Tải ảnh</span></button>
                </div>
              </div>

              <div v-if="prizeIconMode === 'library'" class="z-icon-library">
                <div class="z-icon-picker" role="radiogroup" aria-label="Chọn biểu tượng phần thưởng">
                  <button
                    v-for="icon in iconOptions"
                    :key="icon.value"
                    type="button"
                    :class="{ active: prizeForm.bieuTuong === icon.value }"
                    :title="icon.label"
                    :aria-label="icon.label"
                    :aria-pressed="prizeForm.bieuTuong === icon.value"
                    @click="prizeForm.bieuTuong = icon.value"
                  ><i class="bi" :class="icon.value"></i></button>
                </div>
                <div class="z-icon-code">
                  <label for="lucky-icon-code">Mã Bootstrap Icon khác</label>
                  <input id="lucky-icon-code" v-model.trim="prizeForm.bieuTuong" class="lm-input" maxlength="48" placeholder="Ví dụ: bi-handbag">
                  <span class="z-icon-code-preview"><i class="bi" :class="prizeForm.bieuTuong"></i></span>
                </div>
              </div>

              <div v-else class="z-icon-upload">
                <div class="z-upload-preview" :style="{ '--preview-color': prizeForm.mauHienThi }">
                  <img v-if="prizeIconPreview" :src="prizeIconPreview" alt="Xem trước biểu tượng" @error="clearCustomIcon">
                  <i v-else class="bi" :class="prizeForm.bieuTuong"></i>
                </div>
                <div class="z-upload-copy">
                  <strong>{{ prizeIconFile?.name || (prizeForm.anhBieuTuong ? 'Ảnh biểu tượng hiện tại' : 'Chọn ảnh biểu tượng riêng') }}</strong>
                  <small>JPG, PNG, WebP hoặc AVIF · tối đa 2 MB · nên dùng ảnh vuông nền trong suốt.</small>
                  <div>
                    <label class="z-upload-button"><i class="bi bi-image"></i><span>{{ prizeIconPreview ? 'Đổi ảnh' : 'Chọn ảnh' }}</span><input type="file" accept="image/png,image/jpeg,image/webp,image/avif" @change="selectIconFile"></label>
                    <button v-if="prizeIconPreview" type="button" class="z-remove-icon" @click="clearCustomIcon"><i class="bi bi-trash3"></i><span>Bỏ ảnh</span></button>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-12"><label class="z-check"><input v-model="prizeForm.active" type="checkbox">Hiển thị và có thể được chọn</label></div>
          </div>
          <div class="z-modal-actions"><button type="button" class="lm-btn-secondary" @click="prizeModal = false">Đóng</button><button class="lm-btn-primary" type="submit" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Lưu phần thưởng' }}</span></button></div>
        </form>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'
import { api } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const { confirmDialog } = useConfirm()
const campaigns = ref([])
const spins = ref([])
const selectedCampaignId = ref(null)
const loading = ref(true)
const saving = ref(false)
const campaignModal = ref(false)
const prizeModal = ref(false)
const spinStatus = ref('')
const spinPage = ref(1)
const spinPageSize = ref(10)
const spinTotalItems = ref(0)
const spinTotalPages = ref(1)
const prizeIconMode = ref('library')
const prizeIconFile = ref(null)
const prizeIconPreview = ref('')
let prizeIconObjectUrl = null

const iconOptions = [
  { value: 'bi-gift', label: 'Hộp quà' },
  { value: 'bi-phone', label: 'Điện thoại' },
  { value: 'bi-droplet', label: 'Nước hoa' },
  { value: 'bi-backpack', label: 'Balo' },
  { value: 'bi-umbrella', label: 'Ô gấp' },
  { value: 'bi-stars', label: 'May mắn' },
  { value: 'bi-bag-heart', label: 'Túi quà' },
  { value: 'bi-gem', label: 'Trang sức hoặc khăn lụa' },
  { value: 'bi-circle-half', label: 'Gương trang điểm' },
  { value: 'bi-watch', label: 'Đồng hồ' },
  { value: 'bi-headphones', label: 'Tai nghe' },
  { value: 'bi-handbag', label: 'Túi xách' }
]

const campaignForm = reactive(emptyCampaign())
const prizeForm = reactive(emptyPrize())
const selectedCampaign = computed(() => campaigns.value.find(item => item.id === selectedCampaignId.value) || null)
watch(selectedCampaignId, async id => { spinStatus.value = ''; spinPage.value = 1; await loadSpins(id) })
watch([spinStatus, spinPageSize], () => { spinPage.value = 1; loadSpins(selectedCampaignId.value) })
onMounted(loadData)
onBeforeUnmount(releaseIconPreview)

async function loadData() {
  loading.value = true
  try {
    campaigns.value = await api().getLuckyWheelCampaignsAdmin() || []
    if (!selectedCampaignId.value || !campaigns.value.some(item => item.id === selectedCampaignId.value)) {
      selectedCampaignId.value = campaigns.value[0]?.id || null
    } else await loadSpins(selectedCampaignId.value)
  } catch (error) { toast.showToast(error.error || 'Không thể tải quản lý vòng quay', 'error') }
  finally { loading.value = false }
}

async function loadSpins(campaignId) {
  if (!campaignId) { spins.value = []; spinTotalItems.value = 0; spinTotalPages.value = 1; return }
  try {
    const data = await api().getLuckyWheelSpinsAdmin({
      campaignId,
      status: spinStatus.value || null,
      page: spinPage.value - 1,
      size: spinPageSize.value
    }) || {}
    spins.value = data.content || []
    spinTotalItems.value = Number(data.totalElements || 0)
    spinTotalPages.value = Math.max(1, Number(data.totalPages || 0))
    if (spinPage.value > spinTotalPages.value) {
      spinPage.value = spinTotalPages.value
      await loadSpins(campaignId)
    }
  }
  catch (error) { toast.showToast(error.error || 'Không thể tải lịch sử lượt quay', 'error') }
}

function goToSpinPage(page) {
  const target = Math.max(1, Math.min(spinTotalPages.value, page))
  if (target === spinPage.value) return
  spinPage.value = target
  loadSpins(selectedCampaignId.value)
}

function emptyCampaign() {
  const start = new Date()
  start.setMinutes(start.getMinutes() + 30)
  const end = new Date(start.getTime() + 30 * 24 * 60 * 60 * 1000)
  return { id: null, maChienDich: '', tenChienDich: '', moTa: '', giaTriDonToiThieu: 1000000, ngayBatDau: localInput(start), ngayKetThuc: localInput(end), active: true }
}

function emptyPrize() {
  return { id: null, tenPhanThuong: '', loaiPhanThuong: 'VAT_PHAM', soLuongBanDau: 1, trongSo: 10, mauHienThi: '#D4564E', bieuTuong: 'bi-gift', anhBieuTuong: null, thuTu: 1, active: true }
}

function openCampaign(campaign = null) {
  Object.assign(campaignForm, campaign ? {
    id: campaign.id, maChienDich: campaign.maChienDich || '', tenChienDich: campaign.tenChienDich || '', moTa: campaign.moTa || '',
    giaTriDonToiThieu: Number(campaign.giaTriDonToiThieu || 0), ngayBatDau: localInput(campaign.ngayBatDau),
    ngayKetThuc: localInput(campaign.ngayKetThuc), active: Number(campaign.trangThai) === 1
  } : emptyCampaign())
  campaignModal.value = true
}

function openPrize(prize = null) {
  releaseIconPreview()
  prizeIconFile.value = null
  Object.assign(prizeForm, prize ? {
    id: prize.id, tenPhanThuong: prize.tenPhanThuong || '', loaiPhanThuong: prize.loaiPhanThuong || 'VAT_PHAM',
    soLuongBanDau: prize.soLuongBanDau ?? 0, trongSo: Number(prize.trongSo || 1), mauHienThi: prize.mauHienThi || '#D4564E',
    bieuTuong: prize.bieuTuong || 'bi-gift', anhBieuTuong: prize.anhBieuTuong || null,
    thuTu: Number(prize.thuTu || 0), active: Number(prize.trangThai) === 1
  } : { ...emptyPrize(), thuTu: (selectedCampaign.value?.phanThuongs?.length || 0) + 1 })
  prizeIconMode.value = prizeForm.anhBieuTuong ? 'upload' : 'library'
  prizeIconPreview.value = prizeForm.anhBieuTuong || ''
  prizeModal.value = true
}

async function saveCampaign() {
  if (campaignForm.tenChienDich.trim().length < 3) return toast.showToast('Tên chiến dịch cần ít nhất 3 ký tự', 'warning')
  if (!campaignForm.giaTriDonToiThieu || campaignForm.giaTriDonToiThieu <= 0) return toast.showToast('Giá trị đơn tối thiểu chưa hợp lệ', 'warning')
  if (new Date(campaignForm.ngayKetThuc) <= new Date(campaignForm.ngayBatDau)) return toast.showToast('Ngày kết thúc phải sau ngày bắt đầu', 'warning')
  const payload = {
    maChienDich: campaignForm.maChienDich || null, tenChienDich: campaignForm.tenChienDich, moTa: campaignForm.moTa,
    giaTriDonToiThieu: campaignForm.giaTriDonToiThieu, ngayBatDau: withSeconds(campaignForm.ngayBatDau),
    ngayKetThuc: withSeconds(campaignForm.ngayKetThuc), trangThai: campaignForm.active ? 1 : 0
  }
  saving.value = true
  try {
    const saved = campaignForm.id ? await api().updateLuckyWheelCampaign(campaignForm.id, payload) : await api().createLuckyWheelCampaign(payload)
    campaignModal.value = false
    selectedCampaignId.value = saved.id
    await loadData()
    toast.showToast('Đã lưu chiến dịch vòng quay', 'success')
  } catch (error) { toast.showToast(error.error || 'Không thể lưu chiến dịch', 'error') }
  finally { saving.value = false }
}

async function savePrize() {
  if (prizeForm.tenPhanThuong.trim().length < 2) return toast.showToast('Vui lòng nhập tên phần thưởng', 'warning')
  if (prizeForm.trongSo < 1) return toast.showToast('Trọng số phải lớn hơn 0', 'warning')
  if (prizeForm.loaiPhanThuong === 'VAT_PHAM' && (prizeForm.soLuongBanDau == null || prizeForm.soLuongBanDau < 0)) return toast.showToast('Số lượng quà chưa hợp lệ', 'warning')
  if (!/^bi-[a-z0-9-]{2,45}$/.test(prizeForm.bieuTuong)) return toast.showToast('Mã Bootstrap Icon chưa hợp lệ', 'warning')
  if (prizeIconMode.value === 'upload' && !prizeIconFile.value && !prizeForm.anhBieuTuong) return toast.showToast('Vui lòng chọn ảnh biểu tượng hoặc dùng thư viện icon', 'warning')
  saving.value = true
  try {
    let customIcon = prizeIconMode.value === 'upload' ? prizeForm.anhBieuTuong : null
    if (prizeIconMode.value === 'upload' && prizeIconFile.value) {
      const uploaded = await api().uploadLuckyWheelIcon(prizeIconFile.value)
      customIcon = uploaded.url
    }
    const payload = { ...prizeForm, anhBieuTuong: customIcon, trangThai: prizeForm.active ? 1 : 0 }
    delete payload.id
    delete payload.active
    if (prizeForm.id) await api().updateLuckyWheelPrize(selectedCampaignId.value, prizeForm.id, payload)
    else await api().createLuckyWheelPrize(selectedCampaignId.value, payload)
    prizeModal.value = false
    releaseIconPreview()
    prizeIconFile.value = null
    await loadData()
    toast.showToast('Đã lưu phần thưởng', 'success')
  } catch (error) { toast.showToast(error.error || 'Không thể lưu phần thưởng', 'error') }
  finally { saving.value = false }
}

function selectIconFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (file.size > 2 * 1024 * 1024) return toast.showToast('Ảnh biểu tượng không được vượt quá 2 MB', 'warning')
  if (file.type && !['image/jpeg', 'image/png', 'image/webp', 'image/avif'].includes(file.type)) {
    return toast.showToast('Ảnh phải có định dạng JPG, PNG, WebP hoặc AVIF', 'warning')
  }
  releaseIconPreview()
  prizeIconFile.value = file
  prizeIconObjectUrl = URL.createObjectURL(file)
  prizeIconPreview.value = prizeIconObjectUrl
}

function clearCustomIcon() {
  releaseIconPreview()
  prizeIconFile.value = null
  prizeForm.anhBieuTuong = null
  prizeIconPreview.value = ''
}

function releaseIconPreview() {
  if (prizeIconObjectUrl) URL.revokeObjectURL(prizeIconObjectUrl)
  prizeIconObjectUrl = null
}

function hideBrokenIcon(prize) {
  if (prize) prize.anhBieuTuong = null
}

async function deliverPrize(spin) {
  const accepted = await confirmDialog({ title: 'Xác nhận đã trao quà', message: `Xác nhận đã trao “${spin.tenKetQua}” cho ${spin.tenKhachHang}?`, confirmText: 'Đã trao quà' })
  if (!accepted) return
  try {
    const updated = await api().deliverLuckyWheelPrize(spin.id)
    spins.value = spins.value.map(item => Number(item.id) === Number(spin.id) ? { ...item, ...updated } : item)
    const refreshedCampaigns = await api().getLuckyWheelCampaignsAdmin().catch(() => null)
    if (refreshedCampaigns) campaigns.value = refreshedCampaigns
    toast.showToast('Đã ghi nhận trao quà', 'success')
  } catch (error) { toast.showToast(error.error || 'Không thể cập nhật trao quà', 'error') }
}

function campaignMoneyInput(event) {
  const digits = event.target.value.replace(/\D/g, '').slice(0, 15)
  campaignForm.giaTriDonToiThieu = digits ? Number(digits) : null
  event.target.value = moneyInput(campaignForm.giaTriDonToiThieu)
}

function statusInfo(campaign) {
  return {
    ACTIVE: { label: 'Đang diễn ra', cls: 'success' }, UPCOMING: { label: 'Sắp diễn ra', cls: 'warning' },
    ENDED: { label: 'Đã kết thúc', cls: 'pending' }, INACTIVE: { label: 'Tạm dừng', cls: 'danger' }
  }[campaign.trangThaiHienTai] || { label: 'Tạm dừng', cls: 'pending' }
}
function claimStatus(spin) {
  return {
    CHO_NHAN: { label: 'Chờ trao quà', cls: 'warning' }, DA_TRA: { label: 'Đã trao quà', cls: 'success' },
    KHONG_TRUNG: { label: 'Không trúng', cls: 'pending' }
  }[spin.trangThaiNhan] || { label: 'Chưa xác định', cls: 'pending' }
}
function money(value) { return Number(value || 0).toLocaleString('vi-VN') + 'đ' }
function moneyInput(value) { return value == null || value === '' ? '' : Number(value).toLocaleString('vi-VN') }
function dateTime(value) { return value ? new Date(value).toLocaleString('vi-VN') : '' }
function shortDate(value) { return value ? new Date(value).toLocaleDateString('vi-VN') : '' }
function withSeconds(value) { return value?.length === 16 ? `${value}:00` : value }
function localInput(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
}
</script>

<style scoped>
.z-lucky-admin-header, .z-section-heading, .z-campaign-title-row { display:flex;align-items:flex-start;justify-content:space-between;gap:20px; }
.z-lucky-admin-header { margin-bottom:24px; }
.z-lucky-admin-header h1 { margin:0 0 5px;color:var(--z-dark);font-size:28px;font-weight:500; }
.z-lucky-admin-header p, .z-section-heading p, .z-campaign-title-row p { margin:0;color:var(--z-gray);font-size:12px;line-height:1.6; }
.z-admin-loading { min-height:180px;display:flex;align-items:center;justify-content:center;gap:10px;color:var(--z-gray); }
.z-campaign-strip { display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:10px;margin-bottom:18px; }
.z-campaign-strip > button { display:grid;grid-template-columns:40px 1fr auto;align-items:center;gap:10px;padding:13px;border:1px solid var(--z-gray-border);background:var(--z-white);text-align:left;cursor:pointer;transition:var(--z-ease); }
.z-campaign-strip > button:hover, .z-campaign-strip > button.active { border-color:var(--z-accent);background:var(--z-accent-soft); }
.z-campaign-icon { width:40px;height:40px;display:grid;place-items:center;background:var(--z-dark);color:var(--z-white);font-size:17px; }
.z-campaign-copy { display:grid;gap:3px;min-width:0; }
.z-campaign-copy strong { overflow:hidden;color:var(--z-dark);font-size:12px;text-overflow:ellipsis;white-space:nowrap; }
.z-campaign-copy small { color:var(--z-gray);font-size:10px; }
.z-campaign-count { min-width:28px;height:28px;display:grid;place-items:center;border-radius:50%;background:var(--z-white);color:var(--z-accent);font-size:11px;font-weight:700; }
.z-no-campaign { grid-column:1/-1;min-height:100px;display:flex;align-items:center;justify-content:center;gap:8px;border:1px dashed var(--z-gray-border);color:var(--z-gray);font-size:13px; }
.z-campaign-overview { padding:24px; }
.z-campaign-title-row h2 { margin:0 0 7px;color:var(--z-dark);font-size:20px;font-weight:650; }
.z-code-pill, .z-type-pill { padding:4px 8px;background:var(--z-bg-alt);color:var(--z-gray);font-size:10px;font-weight:600; }
.z-admin-action, .z-deliver-btn { display:inline-flex;align-items:center;justify-content:center;gap:7px;height:36px;padding:0 13px;border:1px solid var(--z-gray-border);background:var(--z-white);color:var(--z-dark);font-size:11px;font-weight:600;cursor:pointer;transition:var(--z-ease); }
.z-admin-action:hover, .z-admin-action.primary { border-color:var(--z-dark);background:var(--z-dark);color:var(--z-white); }
.z-admin-action.primary:hover { border-color:var(--z-accent);background:var(--z-accent); }
.z-campaign-facts { display:grid;grid-template-columns:repeat(5,1fr);margin-top:22px;border-top:1px solid var(--z-gray-border); }
.z-campaign-facts > div { display:grid;grid-template-columns:24px 1fr;gap:2px 7px;padding:18px 12px 0;border-right:1px solid var(--z-gray-border); }
.z-campaign-facts > div:last-child { border-right:0; }
.z-campaign-facts i { grid-row:1/3;color:var(--z-accent);font-size:16px; }
.z-campaign-facts span { color:var(--z-gray);font-size:10px; }
.z-campaign-facts strong { color:var(--z-dark);font-size:12px;overflow-wrap:anywhere; }
.z-admin-section { margin-top:26px; }
.z-section-heading { align-items:flex-end;margin-bottom:12px; }
.z-section-heading h2 { margin:0 0 4px;color:var(--z-dark);font-size:15px;font-weight:650; }
.z-status-filter { width:190px;padding:9px 12px;font-size:11px; }
.z-prize-name { display:flex;align-items:center;gap:10px; }
.z-prize-name > span { width:38px;height:38px;display:grid;place-items:center;flex:none;color:white;font-size:17px; }
.z-prize-name > span img { width:100%;height:100%;padding:4px;object-fit:contain;background:rgba(255,255,255,.94); }
.z-prize-name > div { display:grid;gap:3px; }
.z-prize-name strong { font-size:12px; }
.z-prize-name small, .z-subtext { color:var(--z-gray);font-size:10px; }
.z-deliver-btn { height:32px;border-color:#b9dfc4;color:#217a3d;background:#f0faf3; }
.z-deliver-btn:hover { border-color:#217a3d;background:#217a3d;color:white; }
.z-table code { color:var(--z-accent-dark);font-size:11px;font-weight:700; }
.z-table-empty { min-height:120px;display:flex;align-items:center;justify-content:center;gap:8px;color:var(--z-gray);font-size:12px; }
.z-table-empty i { font-size:22px;color:var(--z-gray-light); }
.z-list-pagination { display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-top:14px;color:var(--z-gray);font-size:11px; }
.z-page-btn { width:34px;height:34px;border:1px solid var(--z-gray-border);background:var(--z-white);color:var(--z-dark); }
.z-page-btn:disabled { opacity:.4; }
.z-icon-btn { width:34px;height:34px;border:1px solid var(--z-gray-border);background:var(--z-white);display:grid;place-items:center;color:var(--z-dark); }
.z-icon-btn:hover { border-color:var(--z-accent);background:var(--z-accent-soft);color:var(--z-accent); }
.z-modal-overlay { position:fixed;inset:0;z-index:1300;display:flex;align-items:center;justify-content:center;padding:20px;background:rgba(27,27,31,.55);backdrop-filter:blur(3px); }
.z-lucky-modal { width:100%;max-width:820px;max-height:90vh;overflow-y:auto;padding:26px;background:var(--z-white);box-shadow:0 24px 70px rgba(0,0,0,.2); }
.z-modal-header { display:flex;align-items:flex-start;justify-content:space-between;gap:18px;margin-bottom:22px;padding-bottom:16px;border-bottom:1px solid var(--z-gray-border); }
.z-modal-header h3 { margin:0 0 4px;color:var(--z-dark);font-size:17px;font-weight:650; }
.z-modal-header p { margin:0;color:var(--z-gray);font-size:11px; }
.z-label { display:block;margin-bottom:6px;color:var(--z-dark);font-size:11px;font-weight:600; }
.z-help { display:block;margin-top:4px;color:var(--z-gray);font-size:9px; }
.z-check { display:inline-flex;align-items:center;gap:8px;color:var(--z-dark);font-size:12px;cursor:pointer; }
.z-color-control { display:grid;grid-template-columns:46px 1fr;gap:8px; }
.z-color-control input[type=color] { width:46px;height:44px;padding:3px;border:1px solid var(--z-gray-border);background:white; }
.z-icon-field-heading { display:flex;align-items:flex-end;justify-content:space-between;gap:18px;margin-top:4px; }
.z-icon-field-heading small { display:block;color:var(--z-gray);font-size:9px; }
.z-icon-mode { display:inline-grid;grid-template-columns:1fr 1fr;padding:3px;border:1px solid var(--z-gray-border);background:var(--z-bg-alt); }
.z-icon-mode button { min-width:98px;height:32px;display:flex;align-items:center;justify-content:center;gap:6px;border:0;background:transparent;color:var(--z-gray);font-size:10px;font-weight:650;cursor:pointer; }
.z-icon-mode button.active { background:var(--z-dark);color:var(--z-white); }
.z-icon-library, .z-icon-upload { margin-top:12px;padding:14px;border:1px solid var(--z-gray-border);background:var(--z-bg-alt); }
.z-icon-picker { display:grid;grid-template-columns:repeat(12,1fr);gap:5px; }
.z-icon-picker button { aspect-ratio:1;min-width:0;display:grid;place-items:center;border:1px solid var(--z-gray-border);background:var(--z-white);color:var(--z-dark);font-size:17px;cursor:pointer;transition:var(--z-ease); }
.z-icon-picker button:hover, .z-icon-picker button.active { border-color:var(--z-accent);background:var(--z-accent-soft);color:var(--z-accent); }
.z-icon-code { display:grid;grid-template-columns:145px 1fr 42px;gap:8px;align-items:center;margin-top:10px; }
.z-icon-code label { color:var(--z-gray);font-size:10px; }
.z-icon-code-preview { width:42px;height:42px;display:grid;place-items:center;border:1px solid var(--z-gray-border);background:var(--z-white);color:var(--z-accent);font-size:18px; }
.z-icon-upload { display:grid;grid-template-columns:84px 1fr;gap:16px;align-items:center; }
.z-upload-preview { width:84px;height:84px;display:grid;place-items:center;overflow:hidden;border:1px dashed color-mix(in srgb,var(--preview-color) 65%,white);background:var(--z-white);color:var(--preview-color);font-size:30px; }
.z-upload-preview img { width:100%;height:100%;padding:7px;object-fit:contain; }
.z-upload-copy { min-width:0;display:grid;gap:5px; }
.z-upload-copy strong { overflow:hidden;color:var(--z-dark);font-size:11px;text-overflow:ellipsis;white-space:nowrap; }
.z-upload-copy small { color:var(--z-gray);font-size:9px;line-height:1.5; }
.z-upload-copy > div { display:flex;gap:8px;margin-top:3px; }
.z-upload-button, .z-remove-icon { min-height:34px;display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:0 11px;border:1px solid var(--z-dark);background:var(--z-dark);color:var(--z-white);font-size:10px;font-weight:650;cursor:pointer; }
.z-upload-button:hover { border-color:var(--z-accent);background:var(--z-accent); }
.z-upload-button input { position:absolute;width:1px;height:1px;overflow:hidden;opacity:0; }
.z-remove-icon { border-color:var(--z-gray-border);background:var(--z-white);color:var(--z-gray); }
.z-remove-icon:hover { border-color:#c62828;color:#c62828; }
.z-modal-actions { display:flex;align-items:center;justify-content:flex-end;gap:16px;margin-top:24px;padding-top:18px;border-top:1px solid var(--z-gray-border); }
@media(max-width:1100px){.z-campaign-facts{grid-template-columns:repeat(3,1fr)}.z-campaign-facts>div:nth-child(3){border-right:0}}
@media(max-width:700px){.z-lucky-admin-header,.z-section-heading,.z-campaign-title-row,.z-icon-field-heading{flex-direction:column;align-items:stretch}.z-campaign-facts{grid-template-columns:1fr 1fr}.z-campaign-facts>div:nth-child(even){border-right:0}.z-status-filter{width:100%}.z-icon-mode{width:100%}.z-icon-picker{grid-template-columns:repeat(6,1fr)}.z-icon-code{grid-template-columns:1fr 42px}.z-icon-code label{grid-column:1/-1}.z-modal-actions{flex-direction:column-reverse}.z-modal-actions>*{width:100%;justify-content:center}}
@media(max-width:480px){.z-icon-upload{grid-template-columns:1fr}.z-upload-preview{width:72px;height:72px}.z-upload-copy>div{flex-direction:column}.z-upload-copy>div>*{width:100%}}
</style>
