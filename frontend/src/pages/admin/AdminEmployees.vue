<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý nhân viên</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredEmployees.length }} nhân viên</p>
      </div>
      <button class="lm-btn-primary" @click="openAdd">
        <i class="bi bi-plus-lg" style="position:relative;z-index:1"></i>
        <span>Thêm nhân viên</span>
      </button>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-person-check"></i></div>
          <div>
            <div class="z-stat-value">{{ activeCount }}</div>
            <div class="z-stat-label">Đang làm việc</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-shield-lock"></i></div>
          <div>
            <div class="z-stat-value">{{ adminCount }}</div>
            <div class="z-stat-label">Quyền quản trị</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-calendar-plus"></i></div>
          <div>
            <div class="z-stat-value">{{ thisMonthCount }}</div>
            <div class="z-stat-label">Tạo trong tháng</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-person-x"></i></div>
          <div>
            <div class="z-stat-value">{{ inactiveCount }}</div>
            <div class="z-stat-label">Đã nghỉ / tạm khoá</div>
          </div>
        </div>
      </div>
    </div>

    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-3 flex-wrap">
        <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:360px">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="search" class="lm-input" placeholder="Tìm theo tên, mã, email, username..." style="border:none;padding:8px 0;box-shadow:none">
        </div>
        <select v-model="filterRole" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả vai trò</option>
          <option v-for="role in roles" :key="role.id" :value="role.tenVaiTro">{{ role.tenVaiTro }}</option>
        </select>
        <select v-model="filterStatus" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả trạng thái</option>
          <option value="1">Đang làm</option>
          <option value="0">Tạm khoá</option>
        </select>
      </div>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Nhân viên</th>
            <th>Tài khoản</th>
            <th>Liên hệ</th>
            <th>Vai trò</th>
            <th>Ngày tạo</th>
            <th>Trạng thái</th>
            <th style="width:150px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Đang tải nhân viên...</td>
          </tr>
          <tr v-else-if="filteredEmployees.length === 0">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Không có nhân viên phù hợp</td>
          </tr>
          <tr v-for="nv in paginatedEmployees" v-else :key="nv.id">
            <td>
              <div class="d-flex align-items-center gap-3">
                <div class="z-avatar">{{ (nv.hoVaTen || 'N').charAt(0) }}</div>
                <div>
                  <div style="font-weight:500">{{ nv.hoVaTen }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ nv.maNhanVien }}</div>
                </div>
              </div>
            </td>
            <td>
              <div style="font-weight:500">{{ nv.tenNguoiDung }}</div>
              <div style="font-size:12px;color:var(--z-gray)">Đăng nhập bằng username hoặc email</div>
            </td>
            <td>
              <div>{{ nv.email }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ nv.soDienThoai || 'Chưa có SĐT' }}</div>
            </td>
            <td><span class="z-role-pill">{{ nv.tenVaiTro || 'Nhân viên' }}</span></td>
            <td style="color:var(--z-gray)">{{ formatDate(nv.ngayTao) }}</td>
            <td>
              <span class="z-status" :class="Number(nv.tinhTrangLamViec) === 1 ? 'success' : 'pending'">
                {{ Number(nv.tinhTrangLamViec) === 1 ? 'Đang làm' : 'Tạm khoá' }}
              </span>
            </td>
            <td>
              <div class="d-flex gap-1">
                <button type="button" class="z-icon-btn" title="Xem chi tiết" aria-label="Xem chi tiết nhân viên" @click="openDetails(nv)"><i class="bi bi-eye"></i></button>
                <button type="button" class="z-icon-btn" title="Sửa" aria-label="Sửa nhân viên" @click="openEdit(nv)"><i class="bi bi-pencil"></i></button>
                <button type="button" class="z-icon-btn" title="Khoá / mở khoá" :aria-label="Number(nv.tinhTrangLamViec) === 1 ? 'Khóa nhân viên' : 'Mở khóa nhân viên'" @click="toggleStatus(nv)">
                  <i class="bi" :class="Number(nv.tinhTrangLamViec) === 1 ? 'bi-lock' : 'bi-unlock'"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination Controls -->
      <div v-if="filteredEmployees.length" class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span style="font-size: 13px; color: var(--z-gray)">
          Hiển thị từ {{ (currentPage - 1) * itemsPerPage + 1 }} đến {{ Math.min(currentPage * itemsPerPage, filteredEmployees.length) }} trong tổng số {{ filteredEmployees.length }} nhân viên
        </span>
        <PageSizeSelect v-model="itemsPerPage" />
        <div v-if="totalPages > 1" class="d-flex gap-2">
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === 1" @click="currentPage--">
            Trước
          </button>
          <button v-for="page in totalPages" :key="page" 
                  class="lm-btn-secondary" 
                  :style="{
                    padding:'6px 12px', fontSize:'12px', height:'auto', borderRadius:'6px',
                    background: currentPage === page ? 'var(--z-dark)' : '',
                    color: currentPage === page ? '#fff' : '',
                    borderColor: currentPage === page ? 'var(--z-dark)' : ''
                  }"
                  @click="currentPage = page">
            {{ page }}
          </button>
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === totalPages" @click="currentPage++">
            Sau
          </button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal" style="max-width:600px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Cập nhật nhân viên' : 'Thêm nhân viên mới' }}</h3>
            <div style="font-size:13px;color:var(--z-gray)">Tài khoản này có thể đăng nhập ở trang Đăng nhập bằng username hoặc email</div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu nhân viên" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="row">
          <div class="col-12">
            <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Họ và tên *</label>
              <input v-model="form.hoVaTen" class="lm-input" :class="{ 'is-invalid': formErrors.hoVaTen }" maxlength="150" placeholder="VD: Nguyễn Minh Anh" @input="clearFieldError('hoVaTen')">
              <div v-if="formErrors.hoVaTen" class="z-field-error">{{ formErrors.hoVaTen }}</div>
            </div>
            <div class="col-md-6">
              <label class="z-label">Vai trò</label>
              <select v-model="form.vaiTroId" class="lm-input" :class="{ 'is-invalid': formErrors.vaiTroId }" @change="clearFieldError('vaiTroId')">
                <option v-for="role in roles" :key="role.id" :value="role.id">{{ role.tenVaiTro }}</option>
              </select>
              <div v-if="formErrors.vaiTroId" class="z-field-error">{{ formErrors.vaiTroId }}</div>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Tên đăng nhập *</label>
              <input v-model="form.tenNguoiDung" class="lm-input" :class="{ 'is-invalid': formErrors.tenNguoiDung }" maxlength="50" autocomplete="off" placeholder="VD: minhanh" @input="normalizeUsername">
              <div v-if="formErrors.tenNguoiDung" class="z-field-error">{{ formErrors.tenNguoiDung }}</div>
            </div>
            <div class="col-md-6">
              <label class="z-label">Mật khẩu {{ editingId ? '(để trống nếu không đổi)' : '*' }}</label>
              <div class="position-relative">
                <input v-model="form.matKhau" class="lm-input" :class="{ 'is-invalid': formErrors.matKhau }" :type="showPassword ? 'text' : 'password'" maxlength="72" autocomplete="new-password" placeholder="8-72 ký tự, gồm chữ và số" @input="clearFieldError('matKhau')">
                <button type="button" class="z-password-toggle" :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPassword = !showPassword">
                  <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
                </button>
              </div>
              <div v-if="formErrors.matKhau" class="z-field-error">{{ formErrors.matKhau }}</div>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Email *</label>
              <input v-model="form.email" type="email" class="lm-input" :class="{ 'is-invalid': formErrors.email }" maxlength="150" placeholder="email@zestia.vn" @input="clearFieldError('email')">
              <div v-if="formErrors.email" class="z-field-error">{{ formErrors.email }}</div>
            </div>
            <div class="col-md-6">
              <label class="z-label">Số điện thoại</label>
              <input v-model="form.soDienThoai" inputmode="tel" class="lm-input" :class="{ 'is-invalid': formErrors.soDienThoai }" maxlength="16" placeholder="0901234567 hoặc +84901234567" @input="normalizeEmployeePhone">
              <div v-if="formErrors.soDienThoai" class="z-field-error">{{ formErrors.soDienThoai }}</div>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Giới tính</label>
              <select v-model="form.gioiTinh" class="lm-input">
                <option value="">Chưa chọn</option>
                <option value="1">Nam</option>
                <option value="0">Nữ</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="z-label">Ngày sinh</label>
              <input v-model="form.ngaySinh" type="date" class="lm-input" :class="{ 'is-invalid': formErrors.ngaySinh }" :max="adultMaximumDate" @change="clearFieldError('ngaySinh')">
              <div v-if="formErrors.ngaySinh" class="z-field-error">{{ formErrors.ngaySinh }}</div>
            </div>
          </div>

          <div>
            <label class="z-label">Địa chỉ</label>
            <input v-model="form.diaChi" class="lm-input" :class="{ 'is-invalid': formErrors.diaChi }" maxlength="255" placeholder="Địa chỉ liên hệ" @input="clearFieldError('diaChi')">
            <div v-if="formErrors.diaChi" class="z-field-error">{{ formErrors.diaChi }}</div>
            </div>
            
            <div class="d-flex justify-content-end gap-2 mt-4">
              <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
              <button class="lm-btn-primary" :disabled="saving" @click="saveEmployee">
                <span>{{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật' : 'Thêm mới') }}</span>
              </button>
            </div>
          </div>
        </div>

        </div>
      </div>
    </div>

    <div v-if="showDetailModal && selectedEmployee" class="z-modal-overlay" @click.self="closeDetails">
      <div class="z-modal z-employee-detail-modal" role="dialog" aria-modal="true" aria-labelledby="employee-detail-title">
        <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
          <div class="d-flex align-items-center gap-3">
            <div class="z-detail-avatar">{{ (selectedEmployee.hoVaTen || 'N').charAt(0) }}</div>
            <div>
              <div class="z-detail-eyebrow">HỒ SƠ NHÂN VIÊN</div>
              <h3 id="employee-detail-title" class="z-detail-title">{{ selectedEmployee.hoVaTen }}</h3>
              <p class="z-detail-caption">{{ selectedEmployee.maNhanVien }} · {{ selectedEmployee.tenVaiTro || 'Nhân viên' }}</p>
            </div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng chi tiết nhân viên" @click="closeDetails"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="z-employee-info-grid">
          <div><span>Tên đăng nhập</span><strong>{{ selectedEmployee.tenNguoiDung }}</strong></div>
          <div><span>Trạng thái</span><strong>{{ Number(selectedEmployee.tinhTrangLamViec) === 1 ? 'Đang làm việc' : 'Tạm khóa' }}</strong></div>
          <div><span>Email</span><strong>{{ selectedEmployee.email || 'Chưa cập nhật' }}</strong></div>
          <div><span>Số điện thoại</span><strong>{{ selectedEmployee.soDienThoai || 'Chưa cập nhật' }}</strong></div>
          <div><span>Ngày sinh</span><strong>{{ formatDate(selectedEmployee.ngaySinh) || 'Chưa cập nhật' }}</strong></div>
          <div><span>Ngày tạo tài khoản</span><strong>{{ formatDate(selectedEmployee.ngayTao) || 'Chưa cập nhật' }}</strong></div>
          <div class="z-info-full"><span>Địa chỉ</span><strong>{{ selectedEmployee.diaChi || 'Chưa cập nhật' }}</strong></div>
        </div>

        <div class="z-performance-header">
          <div>
            <h4>Hiệu suất và ca làm</h4>
            <p>Dữ liệu được tổng hợp từ lịch phân ca và đơn POS của nhân viên.</p>
          </div>
          <i class="bi bi-bar-chart-line"></i>
        </div>

        <div v-if="loadingPerf" class="text-center py-5">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
          <p style="color:var(--z-gray);font-size:12px;margin-top:8px">Đang tính toán hiệu suất...</p>
        </div>
        <div v-else-if="perfStats" class="z-performance-grid">
          <div class="z-performance-metric featured">
            <span>Doanh số POS thành công</span>
            <strong>{{ fmtPrice(perfStats.totalSales) }}</strong>
            <i class="bi bi-cash-stack"></i>
          </div>
          <div class="z-performance-metric">
            <span>Đơn hàng thành công</span>
            <strong>{{ perfStats.completedOrders }} / {{ perfStats.totalOrders }} đơn</strong>
            <i class="bi bi-receipt"></i>
          </div>
          <div class="z-performance-metric">
            <span>Tổng số ca được xếp</span>
            <strong>{{ perfStats.shiftCount }} ca</strong>
            <i class="bi bi-calendar2-week"></i>
          </div>
          <div class="z-performance-metric">
            <span>Tổng giờ làm dự kiến</span>
            <strong>{{ formatHours(perfStats.totalHours) }}</strong>
            <i class="bi bi-clock-history"></i>
          </div>
        </div>
        <div v-else class="z-performance-empty">Không thể tải dữ liệu hiệu suất của nhân viên này.</div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="closeDetails">Đóng</button>
          <button class="lm-btn-primary" @click="editFromDetails"><i class="bi bi-pencil me-1"></i><span>Chỉnh sửa thông tin</span></button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { fmtPrice } from '@/composables/useProducts'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()

const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const showDetailModal = ref(false)
const selectedEmployee = ref(null)
const showPassword = ref(false)
const editingId = ref(null)
const loadingPerf = ref(false)
const perfStats = ref(null)
const search = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const employees = ref([])
const roles = ref([])
const form = ref(defaultForm())
const formErrors = ref({})
const adultMaximumDate = computed(() => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 18)
  return date.toISOString().slice(0, 10)
})

onMounted(async () => {
  await Promise.all([loadEmployees(), loadRoles()])
})

const filteredEmployees = computed(() => {
  const q = search.value.trim().toLowerCase()
  return employees.value.filter(nv => {
    const matchSearch = !q ||
      (nv.hoVaTen || '').toLowerCase().includes(q) ||
      (nv.maNhanVien || '').toLowerCase().includes(q) ||
      (nv.tenNguoiDung || '').toLowerCase().includes(q) ||
      (nv.email || '').toLowerCase().includes(q) ||
      (nv.soDienThoai || '').includes(q)
    const matchRole = !filterRole.value || nv.tenVaiTro === filterRole.value
    const matchStatus = filterStatus.value === '' || String(nv.tinhTrangLamViec ?? 1) === filterStatus.value
    return matchSearch && matchRole && matchStatus
  })
})

const currentPage = ref(1)
const itemsPerPage = ref(10)

const totalPages = computed(() => Math.ceil(filteredEmployees.value.length / itemsPerPage.value))

const paginatedEmployees = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  return filteredEmployees.value.slice(start, start + itemsPerPage.value)
})

watch([search, filterRole, filterStatus, itemsPerPage], () => {
  currentPage.value = 1
})

const activeCount = computed(() => employees.value.filter(nv => Number(nv.tinhTrangLamViec) === 1).length)
const inactiveCount = computed(() => employees.value.filter(nv => Number(nv.tinhTrangLamViec) !== 1).length)
const adminCount = computed(() => employees.value.filter(nv => (nv.tenVaiTro || '').toLowerCase().includes('admin')).length)
const thisMonthCount = computed(() => {
  const now = new Date()
  return employees.value.filter(nv => {
    if (!nv.ngayTao) return false
    const d = new Date(nv.ngayTao)
    return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
  }).length
})

async function loadEmployees() {
  loading.value = true
  try {
    employees.value = await api().getNhanVien()
  } catch (e) {
    showToast('Không thể tải danh sách nhân viên')
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    roles.value = await api().getVaiTroNhanVien()
  } catch (e) {
    roles.value = []
  }
}

async function loadPerformance(employeeId) {
  loadingPerf.value = true
  perfStats.value = null
  try {
    perfStats.value = await api().getNhanVienHieuSuat(employeeId)
  } catch (e) {
    console.error('Không thể tải hiệu suất nhân viên', e)
  } finally {
    loadingPerf.value = false
  }
}

function openAdd() {
  editingId.value = null
  perfStats.value = null
  showPassword.value = false
  form.value = defaultForm()
  formErrors.value = {}
  if (roles.value.length) {
    const staffRole = roles.value.find(r => r.tenVaiTro === 'Nhân viên') || roles.value[0]
    form.value.vaiTroId = staffRole.id
  }
  showModal.value = true
}

function openEdit(nv) {
  editingId.value = nv.id
  showPassword.value = false
  formErrors.value = {}
  form.value = {
    hoVaTen: nv.hoVaTen || '',
    vaiTroId: nv.vaiTroId || '',
    tenNguoiDung: nv.tenNguoiDung || '',
    matKhau: '',
    email: nv.email || '',
    soDienThoai: nv.soDienThoai || '',
    gioiTinh: nv.gioiTinh ?? '',
    ngaySinh: nv.ngaySinh || '',
    diaChi: nv.diaChi || ''
  }
  showModal.value = true
}

function openDetails(nv) {
  selectedEmployee.value = nv
  showDetailModal.value = true
  loadPerformance(nv.id)
}

function closeDetails() {
  showDetailModal.value = false
  selectedEmployee.value = null
  perfStats.value = null
}

function editFromDetails() {
  const employee = selectedEmployee.value
  closeDetails()
  if (employee) openEdit(employee)
}

async function saveEmployee() {
  if (!validateEmployeeForm()) return

  saving.value = true
  try {
    const payload = {
      ...form.value,
      hoVaTen: form.value.hoVaTen.trim().replace(/\s+/g, ' '),
      tenNguoiDung: form.value.tenNguoiDung.trim(),
      email: form.value.email.trim().toLowerCase(),
      soDienThoai: form.value.soDienThoai.trim(),
      diaChi: form.value.diaChi.trim()
    }
    if (editingId.value && !payload.matKhau) delete payload.matKhau

    if (editingId.value) {
      await api().updateNhanVien(editingId.value, payload)
      showToast('Cập nhật nhân viên thành công!')
    } else {
      await api().addNhanVien(payload)
      showToast('Thêm nhân viên thành công! Nhân viên có thể đăng nhập ngay.')
    }
    showModal.value = false
    await loadEmployees()
  } catch (e) {
    showToast(e.message || 'Lưu nhân viên thất bại')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(nv) {
  const next = Number(nv.tinhTrangLamViec) === 1 ? 0 : 1
  if (!await confirmDialog({
    title: next === 1 ? 'Mở khóa nhân viên' : 'Tạm khóa nhân viên',
    message: `${next === 1 ? 'Mở khóa' : 'Tạm khóa'} tài khoản nhân viên "${nv.hoVaTen}"?`,
    confirmText: next === 1 ? 'Mở khóa' : 'Tạm khóa',
    variant: next === 1 ? 'primary' : 'danger'
  })) return
  try {
    await api().updateNhanVienStatus(nv.id, next)
    showToast(next === 1 ? 'Đã mở khoá nhân viên' : 'Đã tạm khoá nhân viên')
    await loadEmployees()
  } catch (e) {
    showToast('Không thể cập nhật trạng thái')
  }
}

function validateEmployeeForm() {
  const errors = {}
  const name = form.value.hoVaTen.trim().replace(/\s+/g, ' ')
  const username = form.value.tenNguoiDung.trim()
  const email = form.value.email.trim()
  const password = form.value.matKhau || ''
  const phone = form.value.soDienThoai.trim()
  const namePattern = /^[\p{L}][\p{L} .'-]{1,149}$/u
  const emailPattern = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/

  if (!namePattern.test(name)) errors.hoVaTen = 'Họ tên từ 2-150 ký tự, không chứa số.'
  if (!/^[A-Za-z0-9._-]{4,50}$/.test(username)) errors.tenNguoiDung = 'Dùng 4-50 chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang.'
  if (!emailPattern.test(email) || email.length > 150) errors.email = 'Email không đúng định dạng.'
  if (!editingId.value || password) {
    if (password.length < 8 || password.length > 72 || !/\p{L}/u.test(password) || !/\d/.test(password)) {
      errors.matKhau = 'Mật khẩu 8-72 ký tự và phải có cả chữ lẫn số.'
    }
  }
  if (phone && !/^(?:0\d{9,10}|\+[1-9]\d{7,14})$/.test(phone)) {
    errors.soDienThoai = 'Nhập 10-11 số hoặc mã quốc gia, ví dụ +84901234567.'
  }
  if (!form.value.vaiTroId) errors.vaiTroId = 'Vui lòng chọn vai trò.'
  if (form.value.ngaySinh) {
    const birth = new Date(`${form.value.ngaySinh}T00:00:00`)
    const oldest = new Date('1900-01-01T00:00:00')
    const adultDate = new Date(`${adultMaximumDate.value}T23:59:59`)
    if (Number.isNaN(birth.getTime()) || birth < oldest || birth > adultDate) errors.ngaySinh = 'Nhân viên phải đủ 18 tuổi và ngày sinh hợp lệ.'
  }
  if (form.value.diaChi.length > 255) errors.diaChi = 'Địa chỉ tối đa 255 ký tự.'

  formErrors.value = errors
  const firstError = Object.values(errors)[0]
  if (firstError) showToast(firstError)
  return !firstError
}

function clearFieldError(field) {
  if (!formErrors.value[field]) return
  const next = { ...formErrors.value }
  delete next[field]
  formErrors.value = next
}

function normalizeUsername(event) {
  form.value.tenNguoiDung = String(event.target.value || '').replace(/[^A-Za-z0-9._-]/g, '').slice(0, 50)
  clearFieldError('tenNguoiDung')
}

function normalizeEmployeePhone(event) {
  let value = String(event.target.value || '').replace(/[^\d+]/g, '')
  value = (value.startsWith('+') ? '+' + value.slice(1).replace(/\+/g, '') : value.replace(/\+/g, '')).slice(0, 16)
  form.value.soDienThoai = value
  clearFieldError('soDienThoai')
}

function defaultForm() {
  return {
    hoVaTen: '',
    vaiTroId: '',
    tenNguoiDung: '',
    matKhau: '',
    email: '',
    soDienThoai: '',
    gioiTinh: '',
    ngaySinh: '',
    diaChi: ''
  }
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : ''
}

function formatHours(value) {
  const hours = Number(value || 0)
  return `${Number.isInteger(hours) ? hours : hours.toFixed(1)} giờ`
}
</script>

<style scoped>
.z-stat-card {
  display: flex; align-items: center; gap: 14px;
  min-height: 96px;
}
.z-stat-icon {
  width: 42px; height: 42px; border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  color: var(--z-accent); background: var(--z-accent-soft);
  font-size: 20px; flex-shrink: 0;
}
.z-stat-value { font-size: 24px; font-weight: 700; color: var(--z-dark); line-height: 1; }
.z-stat-label { font-size: 12px; color: var(--z-gray); margin-top: 6px; }
.z-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: var(--z-dark); color: var(--z-white);
  display: flex; align-items: center; justify-content: center;
  font-weight: 600; font-size: 14px; flex-shrink: 0;
}
.z-role-pill {
  display: inline-flex; align-items: center;
  padding: 5px 10px; border-radius: 20px;
  background: var(--z-bg-alt); color: var(--z-dark);
  font-size: 12px; font-weight: 600;
}
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-width: 760px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
.z-password-toggle {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  border: none; background: none; color: var(--z-gray-light);
  cursor: pointer; padding: 4px;
}
.lm-input.is-invalid { border-color: #dc2626; box-shadow: 0 0 0 2px rgba(220, 38, 38, 0.08); }
.z-field-error { margin-top: 5px; color: #b91c1c; font-size: 11px; line-height: 1.35; }
.z-employee-detail-modal { max-width: 720px; }
.z-detail-avatar {
  width: 48px; height: 48px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; background: var(--z-dark); color: var(--z-white);
  font-size: 18px; font-weight: 650;
}
.z-detail-eyebrow { margin-bottom: 4px; color: var(--z-accent); font-size: 10px; font-weight: 700; }
.z-detail-title { margin: 0; color: var(--z-dark); font-size: 20px; font-weight: 650; }
.z-detail-caption { margin: 4px 0 0; color: var(--z-gray); font-size: 12px; }
.z-employee-info-grid {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px; border: 1px solid var(--z-gray-border); background: var(--z-gray-border);
}
.z-employee-info-grid > div { min-width: 0; padding: 12px 14px; background: var(--z-white); }
.z-employee-info-grid .z-info-full { grid-column: 1 / -1; }
.z-employee-info-grid span,
.z-employee-info-grid strong { display: block; }
.z-employee-info-grid span { margin-bottom: 4px; color: var(--z-gray); font-size: 10px; text-transform: uppercase; }
.z-employee-info-grid strong { overflow-wrap: anywhere; color: var(--z-dark); font-size: 12px; }
.z-performance-header {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  margin-top: 22px; padding-bottom: 10px; border-bottom: 1px solid var(--z-gray-border);
}
.z-performance-header h4 { margin: 0; color: var(--z-dark); font-size: 15px; font-weight: 650; }
.z-performance-header p { margin: 3px 0 0; color: var(--z-gray); font-size: 11px; }
.z-performance-header > i { color: var(--z-accent); font-size: 20px; }
.z-performance-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-top: 14px; }
.z-performance-metric { position: relative; min-height: 92px; padding: 14px; overflow: hidden; border: 1px solid var(--z-gray-border); background: var(--z-bg-alt); }
.z-performance-metric.featured { border-left: 3px solid #15803d; background: #f0fdf4; }
.z-performance-metric span,
.z-performance-metric strong { display: block; max-width: calc(100% - 34px); }
.z-performance-metric span { color: var(--z-gray); font-size: 10px; }
.z-performance-metric strong { margin-top: 8px; color: var(--z-dark); font-size: 16px; }
.z-performance-metric > i { position: absolute; right: 14px; bottom: 14px; color: var(--z-accent); font-size: 20px; opacity: 0.8; }
.z-performance-empty { margin-top: 14px; padding: 20px; background: var(--z-bg-alt); color: var(--z-gray); text-align: center; font-size: 12px; }
@media (max-width: 640px) {
  .z-employee-info-grid,
  .z-performance-grid { grid-template-columns: 1fr; }
  .z-employee-info-grid .z-info-full { grid-column: auto; }
}
</style>
