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
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Đang tải nhân viên...</td>
          </tr>
          <tr v-else-if="filteredEmployees.length === 0">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Không có nhân viên phù hợp</td>
          </tr>
          <tr v-for="nv in filteredEmployees" v-else :key="nv.id">
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
                <button class="z-icon-btn" title="Sửa" @click="openEdit(nv)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Khoá / mở khoá" @click="toggleStatus(nv)">
                  <i class="bi" :class="Number(nv.tinhTrangLamViec) === 1 ? 'bi-lock' : 'bi-unlock'"></i>
                </button>
                <button class="z-icon-btn" title="Tạm khoá" style="color:var(--z-accent)" @click="deleteEmployee(nv)"><i class="bi bi-person-x"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Cập nhật nhân viên' : 'Thêm nhân viên mới' }}</h3>
            <div style="font-size:13px;color:var(--z-gray)">Tài khoản này có thể đăng nhập ở trang Đăng nhập bằng username hoặc email</div>
          </div>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Họ và tên *</label>
              <input v-model="form.hoVaTen" class="lm-input" placeholder="VD: Nguyễn Minh Anh">
            </div>
            <div class="col-md-6">
              <label class="z-label">Vai trò</label>
              <select v-model="form.vaiTroId" class="lm-input">
                <option v-for="role in roles" :key="role.id" :value="role.id">{{ role.tenVaiTro }}</option>
              </select>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Tên đăng nhập *</label>
              <input v-model="form.tenNguoiDung" class="lm-input" placeholder="VD: minhanh">
            </div>
            <div class="col-md-6">
              <label class="z-label">Mật khẩu {{ editingId ? '(để trống nếu không đổi)' : '*' }}</label>
              <div class="position-relative">
                <input v-model="form.matKhau" class="lm-input" :type="showPassword ? 'text' : 'password'" placeholder="Tối thiểu 6 ký tự">
                <button class="z-password-toggle" @click="showPassword = !showPassword">
                  <i class="bi" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Email *</label>
              <input v-model="form.email" type="email" class="lm-input" placeholder="email@zestia.vn">
            </div>
            <div class="col-md-6">
              <label class="z-label">Số điện thoại</label>
              <input v-model="form.soDienThoai" class="lm-input" placeholder="0901234567">
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-4">
              <label class="z-label">Giới tính</label>
              <select v-model="form.gioiTinh" class="lm-input">
                <option value="">Chưa chọn</option>
                <option value="1">Nam</option>
                <option value="0">Nữ</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="z-label">Ngày sinh</label>
              <input v-model="form.ngaySinh" type="date" class="lm-input">
            </div>
            <div class="col-md-4">
              <label class="z-label">Trạng thái</label>
              <select v-model="form.tinhTrangLamViec" class="lm-input">
                <option value="1">Đang làm</option>
                <option value="0">Tạm khoá</option>
              </select>
            </div>
          </div>

          <div>
            <label class="z-label">Địa chỉ</label>
            <input v-model="form.diaChi" class="lm-input" placeholder="Địa chỉ liên hệ">
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
          <button class="lm-btn-primary" :disabled="saving" @click="saveEmployee">
            <span>{{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật' : 'Thêm mới') }}</span>
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const showPassword = ref(false)
const editingId = ref(null)
const search = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const employees = ref([])
const roles = ref([])
const form = ref(defaultForm())

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

function openAdd() {
  editingId.value = null
  showPassword.value = false
  form.value = defaultForm()
  if (roles.value.length) {
    const staffRole = roles.value.find(r => r.tenVaiTro === 'Nhân viên') || roles.value[0]
    form.value.vaiTroId = staffRole.id
  }
  showModal.value = true
}

function openEdit(nv) {
  editingId.value = nv.id
  showPassword.value = false
  form.value = {
    hoVaTen: nv.hoVaTen || '',
    vaiTroId: nv.vaiTroId || '',
    tenNguoiDung: nv.tenNguoiDung || '',
    matKhau: '',
    email: nv.email || '',
    soDienThoai: nv.soDienThoai || '',
    gioiTinh: nv.gioiTinh ?? '',
    ngaySinh: nv.ngaySinh || '',
    tinhTrangLamViec: nv.tinhTrangLamViec ?? 1,
    diaChi: nv.diaChi || ''
  }
  showModal.value = true
}

async function saveEmployee() {
  if (!form.value.hoVaTen || !form.value.tenNguoiDung || !form.value.email) {
    showToast('Vui lòng nhập họ tên, tên đăng nhập và email')
    return
  }
  if (!editingId.value && !form.value.matKhau) {
    showToast('Vui lòng nhập mật khẩu cho nhân viên mới')
    return
  }
  if (form.value.matKhau && form.value.matKhau.length < 6) {
    showToast('Mật khẩu phải có tối thiểu 6 ký tự')
    return
  }

  saving.value = true
  try {
    const payload = { ...form.value }
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
  try {
    await api().updateNhanVienStatus(nv.id, next)
    showToast(next === 1 ? 'Đã mở khoá nhân viên' : 'Đã tạm khoá nhân viên')
    await loadEmployees()
  } catch (e) {
    showToast('Không thể cập nhật trạng thái')
  }
}

async function deleteEmployee(nv) {
  if (!confirm(`Tạm khoá tài khoản nhân viên "${nv.hoVaTen}"?`)) return
  try {
    await api().deleteNhanVien(nv.id)
    showToast('Đã tạm khoá nhân viên')
    await loadEmployees()
  } catch (e) {
    showToast('Không thể tạm khoá nhân viên')
  }
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
    tinhTrangLamViec: 1,
    diaChi: ''
  }
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : ''
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
</style>
