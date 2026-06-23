<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Lịch làm việc</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Sắp ca và theo dõi lịch làm việc của nhân viên</p>
      </div>
      <button class="lm-btn-primary" @click="openAdd"><span>Thêm ca làm</span></button>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-calendar2-week"></i></div>
          <div>
            <div class="z-stat-value">{{ schedules.length }}</div>
            <div class="z-stat-label">Ca trong kỳ</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-people"></i></div>
          <div>
            <div class="z-stat-value">{{ activeStaffCount }}</div>
            <div class="z-stat-label">Nhân viên có lịch</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-clock-history"></i></div>
          <div>
            <div class="z-stat-value">{{ totalHours }}h</div>
            <div class="z-stat-label">Tổng giờ dự kiến</div>
          </div>
        </div>
      </div>
      <div class="col-lg-3 col-md-6">
        <div class="z-admin-card z-stat-card">
          <div class="z-stat-icon"><i class="bi bi-check2-circle"></i></div>
          <div>
            <div class="z-stat-value">{{ confirmedCount }}</div>
            <div class="z-stat-label">Ca đã xác nhận</div>
          </div>
        </div>
      </div>
    </div>

    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="row g-3 align-items-end">
        <div class="col-lg-3 col-md-6">
          <label class="z-label">Từ ngày</label>
          <input v-model="filters.startDate" type="date" class="lm-input" @change="loadSchedules">
        </div>
        <div class="col-lg-3 col-md-6">
          <label class="z-label">Đến ngày</label>
          <input v-model="filters.endDate" type="date" class="lm-input" @change="loadSchedules">
        </div>
        <div class="col-lg-3 col-md-6">
          <label class="z-label">Nhân viên</label>
          <select v-model="filters.nhanVienId" class="lm-input" @change="loadSchedules">
            <option value="">Tất cả nhân viên</option>
            <option v-for="nv in staff" :key="nv.id" :value="nv.id">{{ nv.hoVaTen }}</option>
          </select>
        </div>
        <div class="col-lg-3 col-md-6">
          <label class="z-label">Tìm kiếm</label>
          <div class="d-flex align-items-center gap-2" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);padding:0 12px;background:var(--z-white)">
            <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
            <input v-model="search" class="lm-input" placeholder="Tên, mã, ca..." style="border:none;padding:10px 0;box-shadow:none">
          </div>
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center mt-3 flex-wrap gap-2" style="border-top:1px solid var(--z-gray-border);padding-top:12px">
        <div style="font-size:13px;color:var(--z-gray)">
          Tuần {{ formatDate(filters.startDate) }} - {{ formatDate(filters.endDate) }}
        </div>
        <div class="d-flex gap-2">
          <button class="z-week-btn" @click="moveWeek(-1)"><i class="bi bi-chevron-left"></i> Tuần trước</button>
          <button class="z-week-btn" @click="goCurrentWeek"><i class="bi bi-calendar2-check"></i> Tuần này</button>
          <button class="z-week-btn" @click="moveWeek(1)">Tuần sau <i class="bi bi-chevron-right"></i></button>
        </div>
      </div>
    </div>

    <div class="z-admin-card mb-4" style="padding:0;overflow:hidden">
      <div class="z-schedule-header">
        <div>
          <h3 class="z-admin-card-title mb-1" style="font-size:18px">Biểu lịch tuần</h3>
          <div style="font-size:13px;color:var(--z-gray)">Xem nhanh ca nào có ai làm và note công việc theo từng ngày</div>
        </div>
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <div class="z-legend"><span class="z-dot confirmed"></span>Đã xác nhận</div>
          <div class="z-legend"><span class="z-dot pending"></span>Chờ xác nhận</div>
          <div class="z-legend"><span class="z-dot off"></span>Nghỉ phép</div>
        </div>
      </div>

      <div class="z-week-grid-wrap">
        <div class="z-week-grid">
          <div class="z-week-corner">Ca làm</div>
          <div v-for="day in weekDays" :key="day.value" class="z-week-day" :class="{ today: day.isToday }">
            <div style="font-weight:600">{{ day.label }}</div>
            <div style="font-size:12px;color:var(--z-gray)">{{ day.shortDate }}</div>
          </div>

          <template v-for="shift in shiftRows" :key="shift">
            <div class="z-shift-cell">
              <div style="font-weight:600">{{ shift }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ shiftRangeText(shift) }}</div>
            </div>
            <div v-for="day in weekDays" :key="shift + day.value" class="z-day-cell">
              <button class="z-add-mini" title="Thêm ca vào ngày này" @click="openAddFor(day.value, shift)">
                <i class="bi bi-plus-lg"></i>
              </button>
              <div v-if="cellSchedules(day.value, shift).length" class="d-flex flex-column gap-2">
                <div v-for="item in cellSchedules(day.value, shift)" :key="item.id"
                     class="z-schedule-chip" :class="statusClass(item.trangThai)"
                     @click="openEdit(item)">
                  <div class="d-flex justify-content-between align-items-start gap-2">
                    <div style="font-weight:600;line-height:1.25">{{ item.tenNhanVien || 'Chưa rõ' }}</div>
                    <span style="font-size:11px;white-space:nowrap">{{ shortTime(item.gioBatDau) }}-{{ shortTime(item.gioKetThuc) }}</span>
                  </div>
                  <div style="font-size:11px;color:var(--z-gray);margin-top:3px">{{ item.maNhanVien || item.emailNhanVien || '' }}</div>
                  <div v-if="item.ghiChu" class="z-note-line"><i class="bi bi-journal-text"></i>{{ item.ghiChu }}</div>
                </div>
              </div>
              <div v-else class="z-empty-shift">Trống</div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Ngày làm</th>
            <th>Nhân viên</th>
            <th>Ca làm</th>
            <th>Thời gian</th>
            <th>Ghi chú</th>
            <th>Trạng thái</th>
            <th style="width:90px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Đang tải lịch làm việc...</td>
          </tr>
          <tr v-else-if="filteredSchedules.length === 0">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Chưa có ca làm phù hợp</td>
          </tr>
          <tr v-for="item in filteredSchedules" v-else :key="item.id">
            <td>
              <div style="font-weight:600;color:var(--z-dark)">{{ formatDate(item.ngayLam) }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ weekdayLabel(item.ngayLam) }}</div>
            </td>
            <td>
              <div class="d-flex align-items-center gap-3">
                <div class="z-avatar">{{ (item.tenNhanVien || 'N').charAt(0) }}</div>
                <div>
                  <div style="font-weight:500">{{ item.tenNhanVien || 'Chưa rõ' }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ item.maNhanVien || '' }}</div>
                </div>
              </div>
            </td>
            <td><span class="z-shift-pill">{{ item.caLam || shiftName(item.gioBatDau) }}</span></td>
            <td style="font-weight:500">{{ shortTime(item.gioBatDau) }} - {{ shortTime(item.gioKetThuc) }}</td>
            <td style="color:var(--z-gray);max-width:240px">{{ item.ghiChu || '—' }}</td>
            <td><span class="z-status" :class="statusClass(item.trangThai)">{{ statusText(item.trangThai) }}</span></td>
            <td>
              <div class="d-flex gap-1">
                <button class="z-icon-btn" title="Sửa" @click="openEdit(item)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Xóa" style="color:var(--z-accent)" @click="deleteSchedule(item)"><i class="bi bi-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Cập nhật ca làm' : 'Thêm ca làm mới' }}</h3>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="d-flex flex-column gap-3">
          <div class="row g-3">
            <div class="col-7">
              <label class="z-label">Nhân viên *</label>
              <select v-model="form.nhanVienId" class="lm-input">
                <option value="">Chọn nhân viên</option>
                <option v-for="nv in staff" :key="nv.id" :value="nv.id">{{ nv.hoVaTen }} - {{ nv.maNhanVien }}</option>
              </select>
            </div>
            <div class="col-5">
              <label class="z-label">Ngày làm *</label>
              <input v-model="form.ngayLam" type="date" class="lm-input">
            </div>
          </div>

          <div class="row g-3">
            <div class="col-4">
              <label class="z-label">Ca làm</label>
              <select v-model="form.caLam" class="lm-input" @change="applyShiftPreset">
                <option value="Ca sáng">Ca sáng</option>
                <option value="Ca chiều">Ca chiều</option>
                <option value="Ca tối">Ca tối</option>
                <option value="Cả ngày">Cả ngày</option>
                <option value="Tuỳ chỉnh">Tuỳ chỉnh</option>
              </select>
            </div>
            <div class="col-4">
              <label class="z-label">Giờ bắt đầu *</label>
              <input v-model="form.gioBatDau" type="time" class="lm-input">
            </div>
            <div class="col-4">
              <label class="z-label">Giờ kết thúc *</label>
              <input v-model="form.gioKetThuc" type="time" class="lm-input">
            </div>
          </div>

          <div class="row g-3">
            <div class="col-5">
              <label class="z-label">Trạng thái</label>
              <select v-model.number="form.trangThai" class="lm-input">
                <option :value="1">Đã xác nhận</option>
                <option :value="0">Chờ xác nhận</option>
                <option :value="2">Nghỉ phép</option>
              </select>
            </div>
            <div class="col-7">
              <label class="z-label">Ghi chú</label>
              <input v-model="form.ghiChu" class="lm-input" placeholder="VD: hỗ trợ kiểm kho, đổi ca...">
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
          <button class="lm-btn-primary" :disabled="saving" @click="saveSchedule">
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
const editingId = ref(null)
const search = ref('')
const staff = ref([])
const schedules = ref([])

const filters = ref({
  startDate: toInputDate(startOfWeek(new Date())),
  endDate: toInputDate(endOfWeek(new Date())),
  nhanVienId: ''
})

const form = ref(defaultForm())

onMounted(async () => {
  await Promise.all([loadStaff(), loadSchedules()])
})

const filteredSchedules = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return schedules.value
  return schedules.value.filter(item =>
    (item.tenNhanVien || '').toLowerCase().includes(q) ||
    (item.maNhanVien || '').toLowerCase().includes(q) ||
    (item.caLam || '').toLowerCase().includes(q) ||
    (item.ghiChu || '').toLowerCase().includes(q)
  )
})

const activeStaffCount = computed(() => new Set(schedules.value.map(item => item.nhanVienId).filter(Boolean)).size)
const confirmedCount = computed(() => schedules.value.filter(item => Number(item.trangThai) === 1).length)
const totalHours = computed(() => {
  const total = schedules.value.reduce((sum, item) => sum + diffHours(item.gioBatDau, item.gioKetThuc), 0)
  return Number.isInteger(total) ? total : total.toFixed(1)
})
const weekDays = computed(() => Array.from({ length: 7 }, (_, index) => {
  const d = addDays(parseInputDate(filters.value.startDate), index)
  const value = toInputDate(d)
  return {
    value,
    label: d.toLocaleDateString('vi-VN', { weekday: 'short' }),
    shortDate: d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
    isToday: value === toInputDate(new Date())
  }
}))
const shiftRows = computed(() => {
  const base = ['Ca sáng', 'Ca chiều', 'Ca tối', 'Cả ngày']
  const extra = filteredSchedules.value
    .map(item => item.caLam || shiftName(item.gioBatDau))
    .filter(Boolean)
    .filter(name => !base.includes(name))
  return [...base, ...new Set(extra)]
})

async function loadStaff() {
  try {
    staff.value = await api().getNhanVienLamViec()
  } catch (e) {
    showToast('Không thể tải danh sách nhân viên')
  }
}

async function loadSchedules() {
  loading.value = true
  try {
    schedules.value = await api().getLichLamViec({
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      nhanVienId: filters.value.nhanVienId
    })
  } catch (e) {
    showToast('Không thể tải lịch làm việc')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openAddFor(day, shift) {
  editingId.value = null
  form.value = { ...defaultForm(), ngayLam: day, caLam: shift }
  applyShiftPreset()
  showModal.value = true
}

function openEdit(item) {
  editingId.value = item.id
  form.value = {
    nhanVienId: item.nhanVienId || '',
    ngayLam: item.ngayLam || '',
    caLam: item.caLam || 'Ca sáng',
    gioBatDau: shortTime(item.gioBatDau),
    gioKetThuc: shortTime(item.gioKetThuc),
    trangThai: item.trangThai ?? 1,
    ghiChu: item.ghiChu || ''
  }
  showModal.value = true
}

async function saveSchedule() {
  if (!form.value.nhanVienId || !form.value.ngayLam || !form.value.gioBatDau || !form.value.gioKetThuc) {
    showToast('Vui lòng nhập đầy đủ nhân viên, ngày và giờ làm')
    return
  }
  if (form.value.gioBatDau >= form.value.gioKetThuc) {
    showToast('Giờ kết thúc phải sau giờ bắt đầu')
    return
  }

  const payload = {
    nhanVien: { id: Number(form.value.nhanVienId) },
    ngayLam: form.value.ngayLam,
    caLam: form.value.caLam,
    gioBatDau: form.value.gioBatDau,
    gioKetThuc: form.value.gioKetThuc,
    trangThai: form.value.trangThai,
    ghiChu: form.value.ghiChu
  }

  saving.value = true
  try {
    if (editingId.value) {
      await api().updateLichLamViec(editingId.value, payload)
      showToast('Cập nhật ca làm thành công!')
    } else {
      await api().addLichLamViec(payload)
      showToast('Thêm ca làm thành công!')
    }
    showModal.value = false
    await loadSchedules()
  } catch (e) {
    showToast(e.message || 'Lưu lịch làm việc thất bại')
  } finally {
    saving.value = false
  }
}

async function deleteSchedule(item) {
  if (!confirm(`Xóa ca làm của ${item.tenNhanVien || 'nhân viên'} ngày ${formatDate(item.ngayLam)}?`)) return
  try {
    await api().deleteLichLamViec(item.id)
    showToast('Đã xóa ca làm!')
    await loadSchedules()
  } catch (e) {
    showToast('Lỗi khi xóa ca làm')
  }
}

async function moveWeek(direction) {
  const start = addDays(parseInputDate(filters.value.startDate), direction * 7)
  const end = addDays(start, 6)
  filters.value.startDate = toInputDate(start)
  filters.value.endDate = toInputDate(end)
  await loadSchedules()
}

async function goCurrentWeek() {
  filters.value.startDate = toInputDate(startOfWeek(new Date()))
  filters.value.endDate = toInputDate(endOfWeek(new Date()))
  await loadSchedules()
}

function applyShiftPreset() {
  const presets = {
    'Ca sáng': ['08:00', '12:00'],
    'Ca chiều': ['13:00', '17:00'],
    'Ca tối': ['18:00', '22:00'],
    'Cả ngày': ['08:00', '17:00']
  }
  const preset = presets[form.value.caLam]
  if (!preset) return
  form.value.gioBatDau = preset[0]
  form.value.gioKetThuc = preset[1]
}

function defaultForm() {
  return {
    nhanVienId: '',
    ngayLam: toInputDate(new Date()),
    caLam: 'Ca sáng',
    gioBatDau: '08:00',
    gioKetThuc: '12:00',
    trangThai: 1,
    ghiChu: ''
  }
}

function startOfWeek(date) {
  const d = new Date(date)
  const day = d.getDay() || 7
  d.setDate(d.getDate() - day + 1)
  return d
}

function endOfWeek(date) {
  const d = startOfWeek(date)
  d.setDate(d.getDate() + 6)
  return d
}

function toInputDate(date) {
  const d = new Date(date)
  const offset = d.getTimezoneOffset()
  d.setMinutes(d.getMinutes() - offset)
  return d.toISOString().slice(0, 10)
}

function parseInputDate(value) {
  if (!value) return new Date()
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function addDays(date, days) {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : ''
}

function weekdayLabel(value) {
  if (!value) return ''
  return new Date(value).toLocaleDateString('vi-VN', { weekday: 'long' })
}

function shortTime(value) {
  return value ? String(value).slice(0, 5) : ''
}

function shiftName(time) {
  const hour = Number(shortTime(time).slice(0, 2))
  if (hour >= 18) return 'Ca tối'
  if (hour >= 13) return 'Ca chiều'
  return 'Ca sáng'
}

function shiftRangeText(shift) {
  const ranges = {
    'Ca sáng': '08:00 - 12:00',
    'Ca chiều': '13:00 - 17:00',
    'Ca tối': '18:00 - 22:00',
    'Cả ngày': '08:00 - 17:00'
  }
  return ranges[shift] || 'Tuỳ chỉnh'
}

function cellSchedules(day, shift) {
  return filteredSchedules.value.filter(item =>
    item.ngayLam === day &&
    (item.caLam || shiftName(item.gioBatDau)) === shift
  )
}

function statusText(status) {
  const value = Number(status)
  if (value === 1) return 'Đã xác nhận'
  if (value === 2) return 'Nghỉ phép'
  return 'Chờ xác nhận'
}

function statusClass(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'danger'
  return 'pending'
}

function diffHours(start, end) {
  const [sh, sm] = shortTime(start).split(':').map(Number)
  const [eh, em] = shortTime(end).split(':').map(Number)
  if ([sh, sm, eh, em].some(Number.isNaN)) return 0
  return Math.max(0, ((eh * 60 + em) - (sh * 60 + sm)) / 60)
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
.z-week-btn {
  min-height: 34px; padding: 7px 12px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px; font-weight: 500; color: var(--z-dark);
  cursor: pointer; transition: all 0.2s;
}
.z-week-btn:hover { border-color: var(--z-accent); color: var(--z-accent); background: var(--z-accent-soft); }
.z-schedule-header {
  padding: 18px 20px; border-bottom: 1px solid var(--z-gray-border);
  display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
}
.z-legend { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; color: var(--z-gray); }
.z-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.z-dot.confirmed { background: #16a34a; }
.z-dot.pending { background: #f59e0b; }
.z-dot.off { background: var(--z-accent); }
.z-week-grid-wrap { overflow-x: auto; }
.z-week-grid {
  min-width: 1080px;
  display: grid;
  grid-template-columns: 128px repeat(7, minmax(132px, 1fr));
  background: var(--z-gray-border);
  gap: 1px;
}
.z-week-corner,
.z-week-day,
.z-shift-cell,
.z-day-cell {
  background: var(--z-white);
}
.z-week-corner {
  padding: 14px 16px;
  font-size: 12px; font-weight: 700; color: var(--z-gray);
  text-transform: uppercase; letter-spacing: 0.04em;
}
.z-week-day {
  padding: 12px 14px;
  min-height: 64px;
  border-top: 3px solid transparent;
}
.z-week-day.today { border-top-color: var(--z-accent); background: var(--z-accent-soft); }
.z-shift-cell {
  padding: 14px 16px;
  min-height: 126px;
}
.z-day-cell {
  position: relative;
  padding: 10px;
  min-height: 126px;
}
.z-add-mini {
  position: absolute; right: 8px; top: 8px;
  width: 24px; height: 24px; border: none; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: var(--z-gray); background: var(--z-bg-alt);
  opacity: 0; transition: all 0.2s; cursor: pointer; font-size: 11px;
}
.z-day-cell:hover .z-add-mini { opacity: 1; }
.z-add-mini:hover { background: var(--z-accent); color: var(--z-white); }
.z-schedule-chip {
  padding: 10px 10px 9px;
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  border-left: 3px solid var(--z-gray-light);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  color: var(--z-dark);
}
.z-schedule-chip:hover { transform: translateY(-1px); box-shadow: 0 8px 18px rgba(0,0,0,0.08); }
.z-schedule-chip.success { border-left-color: #16a34a; background: #f0fdf4; }
.z-schedule-chip.pending { border-left-color: #f59e0b; background: #fffbeb; }
.z-schedule-chip.danger { border-left-color: var(--z-accent); background: var(--z-accent-soft); }
.z-note-line {
  margin-top: 8px; padding-top: 7px; border-top: 1px solid rgba(0,0,0,0.06);
  display: flex; align-items: flex-start; gap: 5px;
  color: var(--z-dark); line-height: 1.35;
}
.z-empty-shift {
  height: 100%;
  min-height: 76px;
  border: 1px dashed var(--z-gray-border);
  border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  color: var(--z-gray-light);
  font-size: 12px;
}
.z-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--z-dark); color: var(--z-white);
  display: flex; align-items: center; justify-content: center;
  font-weight: 600; font-size: 13px; flex-shrink: 0;
}
.z-shift-pill {
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
  padding: 28px; width: 100%; max-width: 680px; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
</style>
