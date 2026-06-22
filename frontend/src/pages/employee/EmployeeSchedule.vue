<template>
  <div class="z-page-container">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Lịch làm việc của tôi</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ schedules.length }} ca làm việc</p>
      </div>
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
            <div class="z-stat-label">Ca sắp tới</div>
            <div class="z-stat-value">{{ upcomingCount }}</div>
          </div>
          <i class="bi bi-clock-history"></i>
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
          <input v-model="search" class="lm-input" placeholder="Tìm ca, vị trí..." style="border:none;padding:8px 0;box-shadow:none">
        </div>

        <select v-model="activeStatus" class="lm-input" style="width:170px">
          <option value="all">Tất cả trạng thái</option>
          <option value="1">Đã xác nhận</option>
          <option value="0">Chưa xác nhận</option>
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
          <div v-for="item in itemsByDay(day.iso)" :key="item.id" class="z-shift-card" style="cursor:default">
            <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
              <div>
                <div class="z-shift-name">{{ item.caLamViec || 'Ca làm' }}</div>
                <div class="z-shift-code">Vị trí: {{ item.viTri || 'Cửa hàng' }}</div>
              </div>
              <span class="z-status" :class="statusInfo(item.trangThai).cls">{{ statusInfo(item.trangThai).text }}</span>
            </div>
            <div class="z-shift-meta"><i class="bi bi-clock"></i>{{ fmtTime(item.gioBatDau) }} - {{ fmtTime(item.gioKetThuc) }}</div>
            <div v-if="item.ghiChu" class="z-shift-note">{{ item.ghiChu }}</div>
          </div>
        </div>

        <div v-else class="z-empty-day">
          <i class="bi bi-calendar-x"></i>
          <span>Không có ca</span>
        </div>
      </div>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Ngày</th>
            <th>Ca</th>
            <th>Thời gian</th>
            <th>Vị trí</th>
            <th>Trạng thái</th>
            <th>Ghi chú</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredSchedules" :key="item.id">
            <td>{{ fmtDate(item.ngayLamViec) }}</td>
            <td>{{ item.caLamViec || 'Ca làm' }}</td>
            <td>{{ fmtTime(item.gioBatDau) }} - {{ fmtTime(item.gioKetThuc) }}</td>
            <td>{{ item.viTri || 'Cửa hàng' }}</td>
            <td><span class="z-status" :class="statusInfo(item.trangThai).cls">{{ statusInfo(item.trangThai).text }}</span></td>
            <td>{{ item.ghiChu || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredSchedules.length === 0" class="text-center py-5">
        <i class="bi bi-calendar-x" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có lịch làm việc phù hợp</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const dayNames = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7']

const schedules = ref([])
const search = ref('')
const activeStatus = ref('all')
const currentWeekStart = ref(getWeekStart(new Date()))

onMounted(async () => {
  await loadSchedules()
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

const confirmedCount = computed(() => schedules.value.filter(x => Number(x.trangThai) === 1).length)
const upcomingCount = computed(() => schedules.value.filter(x => new Date(x.ngayLamViec) >= new Date()).length)

const filteredSchedules = computed(() => {
  const q = search.value.trim().toLowerCase()
  return schedules.value.filter(item => {
    const matchStatus = activeStatus.value === 'all' || String(item.trangThai ?? 1) === activeStatus.value
    const matchSearch = !q ||
      (item.caLamViec || '').toLowerCase().includes(q) ||
      (item.viTri || '').toLowerCase().includes(q)
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

function itemsByDay(iso) {
  return filteredSchedules.value.filter(item => item.ngayLamViec === iso)
}

function changeWeek(offset) {
  currentWeekStart.value = addDays(currentWeekStart.value, offset * 7)
  loadSchedules()
}

function goToday() {
  currentWeekStart.value = getWeekStart(new Date())
  loadSchedules()
}

function statusInfo(status) {
  const st = Number(status ?? 1)
  const map = {
    1: { text: 'Đã xác nhận', cls: 'confirmed' },
    0: { text: 'Chưa xác nhận', cls: 'pending' },
    2: { text: 'Nghỉ phép', cls: 'leave' }
  }
  return map[st] || map[1]
}

function fmtDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function fmtTime(timeStr) {
  if (!timeStr) return ''
  if (timeStr.length === 5) return timeStr
  return timeStr.substring(0, 5)
}

function getWeekStart(date) {
  const d = new Date(date)
  d.setDate(d.getDate() - d.getDay())
  return d
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
  const dt = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dt}`
}
</script>

<style scoped>
.z-page-container {
  padding: 20px;
}

.z-admin-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.z-stat-card {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.z-stat-label {
  font-size: 12px;
  color: var(--z-gray);
  font-weight: 500;
  margin-bottom: 8px;
}

.z-stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--z-dark);
}

.z-stat-card i {
  font-size: 32px;
  color: var(--z-gray-light);
  opacity: 0.6;
}

.z-schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.z-day-card {
  padding: 16px;
}

.z-day-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--z-gray-border);
}

.z-day-name {
  font-weight: 600;
  color: var(--z-dark);
  font-size: 14px;
}

.z-day-date {
  font-size: 12px;
  color: var(--z-gray);
  margin-top: 2px;
}

.z-day-count {
  background: var(--z-primary);
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.z-shift-card {
  padding: 12px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: #fafafa;
}

.z-shift-name {
  font-weight: 600;
  font-size: 13px;
  color: var(--z-dark);
}

.z-shift-code {
  font-size: 11px;
  color: var(--z-gray);
  margin-top: 2px;
}

.z-shift-meta {
  font-size: 12px;
  color: var(--z-gray);
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.z-shift-note {
  font-size: 11px;
  color: var(--z-gray);
  margin-top: 6px;
  font-style: italic;
  padding: 6px 8px;
  background: white;
  border-radius: 3px;
}

.z-empty-day {
  text-align: center;
  padding: 20px;
  color: var(--z-gray);
}

.z-empty-day i {
  font-size: 24px;
  margin-bottom: 6px;
  display: block;
  color: var(--z-gray-border);
}

.z-status {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

.z-status.confirmed {
  background: #d4edda;
  color: #155724;
}

.z-status.pending {
  background: #fff3cd;
  color: #856404;
}

.z-status.leave {
  background: #f8d7da;
  color: #721c24;
}

.z-table {
  width: 100%;
  border-collapse: collapse;
}

.z-table thead {
  background: #f8f9fa;
  border-bottom: 2px solid var(--z-gray-border);
}

.z-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: var(--z-dark);
  font-size: 13px;
}

.z-table tbody tr {
  border-bottom: 1px solid var(--z-gray-border);
}

.z-table td {
  padding: 12px 16px;
  font-size: 13px;
}

.z-table tbody tr:hover {
  background: #f8f9fa;
}

.lm-input {
  border: 1px solid var(--z-gray-border);
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 13px;
}

.lm-btn-secondary {
  background: white;
  border: 1px solid var(--z-gray-border);
  color: var(--z-dark);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
}

.lm-btn-secondary:hover {
  background: #f8f9fa;
}

.z-icon-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--z-gray-border);
  border-radius: 4px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.z-icon-btn:hover {
  background: #f8f9fa;
}
</style>
