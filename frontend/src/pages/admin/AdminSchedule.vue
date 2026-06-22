<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Lịch làm việc</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredSchedules.length }} ca trong tuần</p>
      </div>
      <button class="lm-btn-primary" @click="openAdd">
        <span><i class="bi bi-plus-lg me-2"></i>Thêm ca</span>
      </button>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-lg-4 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div>
            <div class="z-stat-label">Tổng ca</div>
            <div class="z-stat-value">{{ schedules.length }}</div>
          </div>
          <i class="bi bi-calendar2-week"></i>
        </div>
      </div>
      <div class="col-lg-4 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div>
            <div class="z-stat-label">Đã xác nhận</div>
            <div class="z-stat-value">{{ confirmedCount }}</div>
          </div>
          <i class="bi bi-check2-circle"></i>
        </div>
      </div>
      <div class="col-lg-4 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div>
            <div class="z-stat-label">Nhân viên có lịch</div>
            <div class="z-stat-value">{{ employeeCount }}</div>
          </div>
          <i class="bi bi-person-check"></i>
        </div>
      </div>
    </div>

    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-3 flex-wrap">
        <button class="z-icon-btn bordered" title="Tuần trước" @click="changeWeek(-1)"><i class="bi bi-chevron-left"></i></button>
        <div style="min-width:220px">
          <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Tuần làm việc</div>
          <div style="font-size:14px;font-weight:600">{{ weekLabel }}</div>
        </div>
        <button class="z-icon-btn bordered" title="Tuần sau" @click="changeWeek(1)"><i class="bi bi-chevron-right"></i></button>
        <button class="lm-btn-secondary" @click="goToday"><i class="bi bi-calendar-date"></i> Tuần này</button>

        <div class="d-flex align-items-center gap-2 ms-auto" style="min-width:260px">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="search" class="lm-input" placeholder="Tìm nhân viên, vị trí..." style="border:none;padding:8px 0;box-shadow:none">
        </div>

        <select v-model="activeStatus" class="lm-input" style="width:170px">
          <option value="all">Tất cả trạng thái</option>
          <option value="1">Đã xác nhận</option>
          <option value="0">Chờ xác nhận</option>
          <option value="2">Nghỉ phép</option>
        </select>
      </div>
    </div>

    <div class="z-schedule-grid mb-4">
      <div v-for="day in weekDays" :key="day.iso" class="z-admin-card z-day-card">
        <div class="z-day-head">
          <div>
            <div class="z-day-name">{{ day.name }}</div>
            <div class="z-day-date">{{ day.display }}</div>
          </div>
          <span class="z-day-count">{{ itemsByDay(day.iso).length }}</span>
        </div>

        <div v-if="itemsByDay(day.iso).length" class="d-flex flex-column gap-2">
          <div v-for="item in itemsByDay(day.iso)" :key="item.id" class="z-shift-card" @click="openEdit(item)">
            <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
              <div>
                <div class="z-shift-name">{{ item.tenNhanVien || 'Nhân viên' }}</div>
                <div class="z-shift-code">{{ item.maNhanVien || 'N/A' }} · {{ item.vaiTro || 'Nhân viên' }}</div>
              </div>
              <span class="z-status" :class="statusInfo(item.trangThai).cls">{{ statusInfo(item.trangThai).text }}</span>
            </div>
            <div class="z-shift-meta"><i class="bi bi-clock"></i>{{ fmtTime(item.gioBatDau) }} - {{ fmtTime(item.gioKetThuc) }}</div>
            <div class="z-shift-meta"><i class="bi bi-shop-window"></i>{{ item.viTri || 'Cửa hàng' }}</div>
            <div v-if="item.ghiChu" class="z-shift-note">{{ item.ghiChu }}</div>
          </div>
        </div>

        <div v-else class="z-empty-day">
          <i class="bi bi-calendar-x"></i>
          <span>Chưa có ca</span>
        </div>
      </div>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Nhân viên</th>
            <th>Ngày</th>
            <th>Ca</th>
            <th>Thời gian</th>
            <th>Vị trí</th>
            <th>Trạng thái</th>
            <th style="width:90px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredSchedules" :key="item.id">
            <td>
              <div style="font-weight:500">{{ item.tenNhanVien }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ item.maNhanVien }} · {{ item.vaiTro || 'Nhân viên' }}</div>
            </td>
            <td>{{ fmtDate(item.ngayLamViec) }}</td>
            <td>{{ item.caLamViec || 'Ca làm' }}</td>
            <td>{{ fmtTime(item.gioBatDau) }} - {{ fmtTime(item.gioKetThuc) }}</td>
            <td>{{ item.viTri || 'Cửa hàng' }}</td>
            <td><span class="z-status" :class="statusInfo(item.trangThai).cls">{{ statusInfo(item.trangThai).text }}</span></td>
            <td>
              <div class="d-flex gap-1">
                <button class="z-icon-btn" title="Sửa" @click="openEdit(item)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn danger" title="Xóa" @click="removeSchedule(item)"><i class="bi bi-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredSchedules.length === 0" class="text-center py-5">
        <i class="bi bi-calendar-x" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có lịch làm việc phù hợp</p>
      </div>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Cập nhật ca làm' : 'Thêm ca làm' }}</h3>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="d-flex flex-column gap-3">
          <div>
            <label class="z-label">Nhân viên *</label>
            <select v-model.number="form.nhanVienId" class="lm-input">
              <option :value="null">Chọn nhân viên</option>
              <option v-for="nv in activeEmployees" :key="nv.id" :value="nv.id">
                {{ nv.maNhanVien }} - {{ nv.hoVaTen }}
              </option>
            </select>
          </div>

          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Ngày làm việc *</label>
              <input v-model="form.ngayLamViec" type="date" class="lm-input">
            </div>
            <div class="col-6">
              <label class="z-label">Ca làm</label>
              <select v-model="form.caLamViec" class="lm-input" @change="applyShiftPreset">
                <option value="Ca sáng">Ca sáng</option>
                <option value="Ca chiều">Ca chiều</option>
                <option value="Ca tối">Ca tối</option>
                <option value="Ca cả ngày">Ca cả ngày</option>
                <option value="Tùy chỉnh">Tùy chỉnh</option>
              </select>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Giờ bắt đầu</label>
              <input v-model="form.gioBatDau" type="time" class="lm-input">
            </div>
            <div class="col-6">
              <label class="z-label">Giờ kết thúc</label>
              <input v-model="form.gioKetThuc" type="time" class="lm-input">
            </div>
          </div>

          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Vị trí</label>
              <input v-model="form.viTri" class="lm-input" placeholder="Quầy bán hàng, kho...">
            </div>
            <div class="col-6">
              <label class="z-label">Trạng thái</label>
              <select v-model.number="form.trangThai" class="lm-input">
                <option :value="1">Đã xác nhận</option>
                <option :value="0">Chờ xác nhận</option>
                <option :value="2">Nghỉ phép</option>
              </select>
            </div>
          </div>

          <div>
            <label class="z-label">Ghi chú</label>
            <textarea v-model="form.ghiChu" class="lm-input" rows="3" placeholder="Ghi chú thêm cho ca làm..."></textarea>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showModal = false">Hủy</button>
          <button class="lm-btn-primary" @click="saveSchedule" :disabled="saving">
            <span>{{ saving ? 'Đang lưu...' : 'Lưu lịch' }}</span>
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
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const dayNames = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7']
const shiftPresets = {
  'Ca sáng': ['08:00', '12:00'],
  'Ca chiều': ['13:00', '17:00'],
  'Ca tối': ['18:00', '22:00'],
  'Ca cả ngày': ['08:00', '17:00'],
}

const schedules = ref([])
const employees = ref([])
const search = ref('')
const activeStatus = ref('all')
const currentWeekStart = ref(getWeekStart(new Date()))
const showModal = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref(defaultForm())

onMounted(async () => {
  await Promise.all([loadEmployees(), loadSchedules()])
})

const weekDays = computed(() => Array.from({ length: 7 }, (_, i) => {
  const date = addDays(currentWeekStart.value, i)
  return {
    iso: toIsoDate(date),
    name: dayNames[date.getDay()],
    display: date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })
  }
}))

const weekLabel = computed(() => {
  const start = currentWeekStart.value
  const end = addDays(start, 6)
  return `${fmtDate(toIsoDate(start))} - ${fmtDate(toIsoDate(end))}`
})

const activeEmployees = computed(() => employees.value.filter(nv => nv.tinhTrangLamViec === 1 || nv.tinhTrangLamViec === true))
const confirmedCount = computed(() => schedules.value.filter(x => Number(x.trangThai) === 1).length)
const employeeCount = computed(() => new Set(schedules.value.map(x => x.nhanVienId).filter(Boolean)).size)

const filteredSchedules = computed(() => {
  const q = search.value.trim().toLowerCase()
  return schedules.value.filter(item => {
    const matchStatus = activeStatus.value === 'all' || String(item.trangThai ?? 1) === activeStatus.value
    const matchSearch = !q ||
      (item.tenNhanVien || '').toLowerCase().includes(q) ||
      (item.maNhanVien || '').toLowerCase().includes(q) ||
      (item.viTri || '').toLowerCase().includes(q) ||
      (item.caLamViec || '').toLowerCase().includes(q)
    return matchStatus && matchSearch
  })
})

async function loadSchedules() {
  try {
    const from = toIsoDate(currentWeekStart.value)
    const to = toIsoDate(addDays(currentWeekStart.value, 6))
    schedules.value = await api().getLichLamViec(from, to)
  } catch (e) {
    showToast('Không thể tải lịch làm việc')
  }
}

async function loadEmployees() {
  try {
    employees.value = await api().getNhanVienLich()
  } catch (e) {
    showToast('Không thể tải danh sách nhân viên')
  }
}

function itemsByDay(iso) {
  return filteredSchedules.value.filter(item => item.ngayLamViec === iso)
}

function openAdd() {
  editingId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openEdit(item) {
  editingId.value = item.id
  form.value = {
    nhanVienId: item.nhanVienId,
    ngayLamViec: item.ngayLamViec,
    caLamViec: item.caLamViec || 'Ca sáng',
    gioBatDau: normalizeTime(item.gioBatDau) || '08:00',
    gioKetThuc: normalizeTime(item.gioKetThuc) || '12:00',
    viTri: item.viTri || '',
    ghiChu: item.ghiChu || '',
    trangThai: Number(item.trangThai ?? 1),
  }
  showModal.value = true
}

async function saveSchedule() {
  if (!form.value.nhanVienId) { showToast('Vui lòng chọn nhân viên'); return }
  if (!form.value.ngayLamViec) { showToast('Vui lòng chọn ngày làm việc'); return }
  if (form.value.gioBatDau && form.value.gioKetThuc && form.value.gioBatDau >= form.value.gioKetThuc) {
    showToast('Giờ kết thúc phải sau giờ bắt đầu')
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await api().updateLichLamViec(editingId.value, form.value)
      showToast('Cập nhật lịch làm việc thành công!')
    } else {
      await api().addLichLamViec(form.value)
      showToast('Thêm lịch làm việc thành công!')
    }
    showModal.value = false
    await loadSchedules()
  } catch (e) {
    showToast('Lỗi: ' + (e.error || e.message || 'Không thể lưu lịch'))
  } finally {
    saving.value = false
  }
}

async function removeSchedule(item) {
  if (!confirm(`Bạn có chắc muốn xóa ca làm của ${item.tenNhanVien}?`)) return
  try {
    await api().deleteLichLamViec(item.id)
    showToast('Đã xóa lịch làm việc!')
    await loadSchedules()
  } catch (e) {
    showToast('Không thể xóa lịch làm việc')
  }
}

function applyShiftPreset() {
  const preset = shiftPresets[form.value.caLamViec]
  if (preset) {
    form.value.gioBatDau = preset[0]
    form.value.gioKetThuc = preset[1]
  }
}

function changeWeek(offset) {
  currentWeekStart.value = addDays(currentWeekStart.value, offset * 7)
  loadSchedules()
}

function goToday() {
  currentWeekStart.value = getWeekStart(new Date())
  loadSchedules()
}

function defaultForm() {
  return {
    nhanVienId: null,
    ngayLamViec: toIsoDate(new Date()),
    caLamViec: 'Ca sáng',
    gioBatDau: '08:00',
    gioKetThuc: '12:00',
    viTri: 'Cửa hàng',
    ghiChu: '',
    trangThai: 1,
  }
}

function statusInfo(value) {
  const v = Number(value ?? 1)
  if (v === 0) return { text: 'Chờ xác nhận', cls: 'pending' }
  if (v === 2) return { text: 'Nghỉ phép', cls: 'danger' }
  return { text: 'Đã xác nhận', cls: 'success' }
}

function getWeekStart(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setHours(0, 0, 0, 0)
  return addDays(d, diff)
}

function addDays(date, days) {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

function toIsoDate(date) {
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function normalizeTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
}

function fmtTime(value) {
  return normalizeTime(value) || '--:--'
}

function fmtDate(value) {
  return value ? new Date(value + 'T00:00:00').toLocaleDateString('vi-VN') : ''
}
</script>

<style scoped>
.z-stat-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px;
}
.z-stat-card i { font-size: 24px; color: var(--z-accent); }
.z-stat-label { font-size: 12px; color: var(--z-gray); margin-bottom: 4px; }
.z-stat-value { font-size: 24px; font-weight: 700; color: var(--z-dark); }
.z-schedule-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(150px, 1fr));
  gap: 12px;
}
.z-day-card { padding: 14px; min-height: 220px; }
.z-day-head {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; margin-bottom: 12px; border-bottom: 1px solid var(--z-gray-border);
}
.z-day-name { font-size: 13px; font-weight: 700; color: var(--z-dark); }
.z-day-date { font-size: 12px; color: var(--z-gray); }
.z-day-count {
  min-width: 24px; height: 24px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  background: var(--z-accent-soft); color: var(--z-accent);
  font-size: 12px; font-weight: 700;
}
.z-shift-card {
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  padding: 10px;
  background: var(--z-white);
  cursor: pointer;
  transition: all 0.2s;
}
.z-shift-card:hover { border-color: var(--z-accent); box-shadow: 0 8px 20px rgba(0,0,0,0.06); }
.z-shift-name { font-size: 13px; font-weight: 600; color: var(--z-dark); }
.z-shift-code { font-size: 11px; color: var(--z-gray); }
.z-shift-meta {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: var(--z-gray); margin-top: 5px;
}
.z-shift-note {
  margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--z-gray-border);
  font-size: 12px; color: var(--z-dark);
}
.z-empty-day {
  min-height: 130px; display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 6px; color: var(--z-gray-light); font-size: 13px;
}
.z-empty-day i { font-size: 24px; }
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: inline-flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-icon-btn.bordered { border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-icon-btn.danger { color: #dc2626; }
.z-icon-btn.danger:hover { background: #fee2e2; }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-width: 620px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
@media (max-width: 1200px) {
  .z-schedule-grid { grid-template-columns: repeat(4, minmax(170px, 1fr)); }
}
@media (max-width: 768px) {
  .z-schedule-grid { grid-template-columns: 1fr; }
}
</style>
