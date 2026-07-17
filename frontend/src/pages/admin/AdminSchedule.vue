<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Lịch làm việc</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Sắp ca và theo dõi lịch làm việc của nhân viên</p>
      </div>
      <div class="d-flex gap-2 flex-wrap">
        <button class="lm-btn-secondary" :disabled="exporting" @click="exportHistory">
          <i class="bi bi-file-earmark-excel me-1"></i>{{ exporting ? 'Đang xuất...' : 'Xuất Excel' }}
        </button>
        <button v-if="isAdmin" class="lm-btn-primary" @click="openAdd"><span>Thêm ca làm</span></button>
      </div>
    </div>

    <div v-if="!isAdmin" class="z-admin-card z-my-shift-card mb-4">
      <div>
        <div class="z-my-shift-eyebrow">TRẠNG THÁI LÀM VIỆC</div>
        <h2>{{ workStatus.canOperate ? 'Bạn đang trong ca làm việc' : (workStatus.reason || 'Đang kiểm tra ca làm') }}</h2>
        <p v-if="currentShift">
          {{ formatDate(currentShift.ngayLam) }} · {{ currentShift.caLam || shiftName(currentShift.gioBatDau) }} ·
          {{ shortTime(currentShift.gioBatDau) }} - {{ shortTime(currentShift.gioKetThuc) }}
        </p>
        <p v-else>Hãy theo dõi lịch để xác nhận ca trước khi đến cửa hàng.</p>
      </div>
      <div v-if="currentShift" class="d-flex gap-2 flex-wrap justify-content-end">
        <button v-if="Number(currentShift.trangThai) === 0" class="lm-btn-primary" @click="confirmOwnShift(currentShift)">
          <span>Xác nhận ca</span>
        </button>
        <button v-if="[0, 1].includes(Number(currentShift.trangThai)) && !currentShift.gioCheckIn" class="lm-btn-secondary" @click="openUnavailable(currentShift)">
          Báo bận
        </button>
        <button v-if="currentShift.canCheckIn" class="lm-btn-primary" @click="checkIn(currentShift)"><span>Check-in</span></button>
        <button v-if="currentShift.canCheckOut" class="lm-btn-secondary" @click="checkOut(currentShift)">Check-out</button>
      </div>
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
        <div v-if="isAdmin" class="col-lg-3 col-md-6">
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
              <button v-if="isAdmin" type="button" class="z-add-mini" title="Thêm ca vào ngày này" aria-label="Thêm ca vào ngày này" @click="openAddFor(day.value, shift)">
                <i class="bi bi-plus-lg"></i>
              </button>
              <div v-if="cellSchedules(day.value, shift).length" class="d-flex flex-column gap-2">
                <div v-for="item in cellSchedules(day.value, shift)" :key="item.id"
                     class="z-schedule-chip" :class="statusClass(item.trangThai)"
                     @click="isAdmin && openEdit(item)">
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
            <th style="min-width:150px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Đang tải lịch làm việc...</td>
          </tr>
          <tr v-else-if="filteredSchedules.length === 0">
            <td colspan="7" class="text-center py-4" style="color:var(--z-gray)">Chưa có ca làm phù hợp</td>
          </tr>
          <tr v-for="item in paginatedSchedules" v-else :key="item.id">
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
            <td>
              <span class="z-status" :class="statusClass(item.trangThai)">{{ statusText(item.trangThai) }}</span>
              <div v-if="item.gioCheckIn" class="z-attendance-line">In {{ formatClock(item.gioCheckIn) }}<span v-if="item.gioCheckOut"> · Out {{ formatClock(item.gioCheckOut) }}</span></div>
              <div v-if="item.lyDoBaoBan" class="z-busy-reason" :title="item.lyDoBaoBan">{{ item.lyDoBaoBan }}</div>
            </td>
            <td>
              <div v-if="isAdmin" class="d-flex gap-1 flex-wrap">
                <button v-if="Number(item.trangThai) === 3" type="button" class="z-icon-btn z-approve-btn" title="Duyệt báo bận" aria-label="Duyệt báo bận" @click="approveUnavailable(item)"><i class="bi bi-check-lg"></i></button>
                <button v-if="Number(item.trangThai) === 3" type="button" class="z-icon-btn z-reject-btn" title="Từ chối báo bận" aria-label="Từ chối báo bận" @click="openRejectUnavailable(item)"><i class="bi bi-x-lg"></i></button>
                <button type="button" class="z-icon-btn" title="Sửa" aria-label="Sửa ca làm" @click="openEdit(item)"><i class="bi bi-pencil"></i></button>
                <button type="button" class="z-icon-btn" title="Xóa" aria-label="Xóa ca làm" style="color:var(--z-accent)" @click="deleteSchedule(item)"><i class="bi bi-trash"></i></button>
              </div>
              <div v-else class="d-flex gap-1 flex-wrap">
                <button v-if="Number(item.trangThai) === 0" class="z-row-action" @click="confirmOwnShift(item)">Xác nhận</button>
                <button v-if="[0, 1].includes(Number(item.trangThai)) && !item.gioCheckIn" class="z-row-action secondary" @click="openUnavailable(item)">Báo bận</button>
                <button v-if="item.canCheckIn" class="z-row-action" @click="checkIn(item)">Check-in</button>
                <button v-if="item.canCheckOut" class="z-row-action secondary" @click="checkOut(item)">Check-out</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span style="font-size: 13px; color: var(--z-gray)">
          Hiển thị từ {{ (currentPage - 1) * itemsPerPage + 1 }} đến {{ Math.min(currentPage * itemsPerPage, filteredSchedules.length) }} trong tổng số {{ filteredSchedules.length }} ca làm
        </span>
        <div class="d-flex gap-2">
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

    <div class="z-admin-card mt-4" style="padding:0;overflow:hidden">
      <div class="z-history-header">
        <div>
          <h3 class="z-admin-card-title mb-1">Lịch sử hoạt động và bàn giao ca</h3>
          <p>Doanh thu được lấy từ đơn POS của đúng nhân viên trong thời gian ca làm.</p>
        </div>
        <div class="z-cash-total">
          <span>Tổng tiền mặt cần bàn giao</span>
          <strong>{{ formatMoney(totalCashHandover) }}</strong>
        </div>
      </div>
      <div class="table-responsive">
        <table class="z-table">
          <thead>
            <tr>
              <th>Ca làm</th>
              <th>Nhân viên</th>
              <th>Chấm công</th>
              <th>Đơn POS</th>
              <th>Doanh thu</th>
              <th>Chuyển khoản</th>
              <th>Tiền mặt bàn giao</th>
              <th style="width:70px">Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="historyLoading"><td colspan="8" class="text-center py-4" style="color:var(--z-gray)">Đang tổng hợp hoạt động ca...</td></tr>
            <tr v-else-if="visibleShiftHistory.length === 0"><td colspan="8" class="text-center py-4" style="color:var(--z-gray)">Chưa có ca đã check-in trong khoảng ngày này</td></tr>
            <tr v-for="history in visibleShiftHistory" v-else :key="history.id">
              <td>
                <strong>{{ formatDate(history.ngayLam) }}</strong>
                <div class="z-history-sub">{{ history.caLam || shiftName(history.gioBatDau) }} · {{ shortTime(history.gioBatDau) }}-{{ shortTime(history.gioKetThuc) }}</div>
              </td>
              <td>{{ history.tenNhanVien }}<div class="z-history-sub">{{ history.maNhanVien }}</div></td>
              <td>
                <span>{{ history.gioCheckIn ? formatClock(history.gioCheckIn) : 'Chưa check-in' }}</span>
                <div class="z-history-sub">{{ history.gioCheckOut ? `Out ${formatClock(history.gioCheckOut)}` : formatWorkedTime(history.soPhutLamViec) }}</div>
              </td>
              <td><strong>{{ history.soDon || 0 }}</strong></td>
              <td>{{ formatMoney(history.doanhThu) }}</td>
              <td>{{ formatMoney(history.tienChuyenKhoan) }}</td>
              <td><strong class="z-cash-value">{{ formatMoney(history.tienMatBanGiao) }}</strong></td>
              <td>
                <button type="button" class="z-icon-btn" title="Xem lịch sử hoạt động" aria-label="Xem lịch sử hoạt động" @click="openActivityHistory(history)">
                  <i class="bi bi-clock-history"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="activityShift" class="z-modal-overlay" @click.self="activityShift = null">
      <div class="z-modal" style="max-width:560px">
        <div class="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Hoạt động trong ca</h3>
            <p class="z-activity-caption">{{ activityShift.tenNhanVien }} · {{ formatDate(activityShift.ngayLam) }} · {{ activityShift.caLam }}</p>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng lịch sử hoạt động" @click="activityShift = null"><i class="bi bi-x-lg"></i></button>
        </div>
        <div class="z-activity-summary">
          <div><span>Doanh thu</span><strong>{{ formatMoney(activityShift.doanhThu) }}</strong></div>
          <div><span>Tiền mặt bàn giao</span><strong>{{ formatMoney(activityShift.tienMatBanGiao) }}</strong></div>
        </div>
        <div v-if="activityShift.hoatDong?.length" class="z-activity-timeline">
          <div v-for="(activity, index) in activityShift.hoatDong" :key="`${activity.thoiGian}-${index}`" class="z-activity-row">
            <span class="z-activity-dot"></span>
            <div>
              <strong>{{ activityTitle(activity.loai) }} · {{ formatClock(activity.thoiGian) }}</strong>
              <p>{{ activity.noiDung }}</p>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-4" style="color:var(--z-gray);font-size:13px">Ca chưa phát sinh hoạt động.</div>
      </div>
    </div>

    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Cập nhật ca làm' : 'Thêm ca làm mới' }}</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu ca làm" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
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
                <option :value="0">Chờ xác nhận</option>
                <option :value="1">Đã xác nhận</option>
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

    <div v-if="showReasonModal" class="z-modal-overlay" @click.self="closeReasonModal">
      <div class="z-modal" style="max-width:500px">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ reasonModalMode === 'reject' ? 'Từ chối yêu cầu báo bận' : 'Báo bận ca làm' }}</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu lý do" @click="closeReasonModal"><i class="bi bi-x-lg"></i></button>
        </div>
        <p style="font-size:13px;color:var(--z-gray)">
          {{ reasonModalMode === 'reject' ? 'Nhập lý do để nhân viên biết vì sao yêu cầu chưa được chấp nhận.' : 'Trình bày rõ lý do không thể tham gia ca. Ca đã xác nhận sẽ chờ admin duyệt.' }}
        </p>
        <textarea v-model="actionReason" class="lm-input" rows="4" maxlength="500" :placeholder="reasonModalMode === 'reject' ? 'Lý do từ chối...' : 'Lý do báo bận...'"></textarea>
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="closeReasonModal">Đóng</button>
          <button class="lm-btn-primary" :disabled="actionSaving" @click="submitReasonAction"><span>{{ actionSaving ? 'Đang gửi...' : 'Xác nhận' }}</span></button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api, useAuth } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const { getUser } = useAuth()
const isAdmin = computed(() => getUser()?.role === 'Admin')

const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const search = ref('')
const staff = ref([])
const schedules = ref([])
const shiftHistory = ref([])
const historyLoading = ref(false)
const exporting = ref(false)
const activityShift = ref(null)
const workStatus = ref({ canOperate: false, reason: '', relevantShift: null, activeShift: null })
const showReasonModal = ref(false)
const reasonModalMode = ref('unavailable')
const actionShift = ref(null)
const actionReason = ref('')
const actionSaving = ref(false)
const currentShift = computed(() => workStatus.value.activeShift || workStatus.value.relevantShift || null)
const visibleShiftHistory = computed(() => shiftHistory.value.filter(item => item.gioCheckIn || item.gioCheckOut || Number(item.soDon || 0) > 0))
const totalCashHandover = computed(() => visibleShiftHistory.value.reduce((sum, item) => sum + Number(item.tienMatBanGiao || 0), 0))

const filters = ref({
  startDate: toInputDate(startOfWeek(new Date())),
  endDate: toInputDate(endOfWeek(new Date())),
  nhanVienId: ''
})

const form = ref(defaultForm())

onMounted(async () => {
  const user = getUser()
  if (!isAdmin.value && user?.userId) {
    filters.value.nhanVienId = user.userId
  }
  if (isAdmin.value) {
    await Promise.all([loadStaff(), loadSchedules()])
  } else {
    await Promise.all([loadSchedules(), loadWorkStatus()])
  }
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

const currentPage = ref(1)
const itemsPerPage = 10

const totalPages = computed(() => Math.ceil(filteredSchedules.value.length / itemsPerPage))

const paginatedSchedules = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredSchedules.value.slice(start, start + itemsPerPage)
})

watch([search, filters], () => {
  currentPage.value = 1
}, { deep: true })

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
  historyLoading.value = true
  try {
    const params = {
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      nhanVienId: filters.value.nhanVienId
    }
    const [scheduleData, historyData] = await Promise.all([
      api().getLichLamViec(params),
      api().getShiftHistory(params)
    ])
    schedules.value = scheduleData
    shiftHistory.value = historyData
  } catch (e) {
    showToast('Không thể tải lịch làm việc')
  } finally {
    loading.value = false
    historyLoading.value = false
  }
}

async function exportHistory() {
  if (exporting.value) return
  exporting.value = true
  try {
    await api().exportShiftHistory({
      startDate: filters.value.startDate,
      endDate: filters.value.endDate,
      nhanVienId: filters.value.nhanVienId
    })
    showToast('Đã xuất file Excel ca làm')
  } catch (error) {
    showToast(error.error || 'Không thể xuất file Excel')
  } finally {
    exporting.value = false
  }
}

async function loadWorkStatus() {
  if (isAdmin.value) return
  try {
    workStatus.value = await api().getWorkShiftStatus()
  } catch (e) {
    workStatus.value = { canOperate: false, reason: e.error || 'Không thể kiểm tra trạng thái ca làm' }
  }
}

async function refreshShiftData() {
  await Promise.all([loadSchedules(), loadWorkStatus()])
  window.dispatchEvent(new Event('zestia-shift-changed'))
}

async function confirmOwnShift(item) {
  if (!await confirmDialog({ title: 'Xác nhận ca làm', message: `Xác nhận tham gia ca ${item.caLam || ''} ngày ${formatDate(item.ngayLam)}?`, confirmText: 'Xác nhận' })) return
  try {
    await api().confirmShift(item.id)
    showToast('Đã xác nhận ca làm')
    await refreshShiftData()
  } catch (e) { showToast(e.error || 'Không thể xác nhận ca làm') }
}

function openUnavailable(item) {
  reasonModalMode.value = 'unavailable'
  actionShift.value = item
  actionReason.value = ''
  showReasonModal.value = true
}

function openRejectUnavailable(item) {
  reasonModalMode.value = 'reject'
  actionShift.value = item
  actionReason.value = ''
  showReasonModal.value = true
}

function closeReasonModal() {
  if (actionSaving.value) return
  showReasonModal.value = false
  actionShift.value = null
  actionReason.value = ''
}

async function submitReasonAction() {
  const reason = actionReason.value.trim()
  const minimum = reasonModalMode.value === 'reject' ? 5 : 10
  if (reason.length < minimum) return showToast(`Vui lòng nhập lý do ít nhất ${minimum} ký tự`)
  actionSaving.value = true
  let succeeded = false
  try {
    if (reasonModalMode.value === 'reject') {
      await api().reviewShiftUnavailable(actionShift.value.id, false, reason)
      showToast('Đã từ chối yêu cầu báo bận')
    } else {
      const result = await api().reportShiftUnavailable(actionShift.value.id, reason)
      showToast(Number(result.trangThai) === 3 ? 'Yêu cầu đã gửi và đang chờ admin duyệt' : 'Đã ghi nhận báo bận')
    }
    succeeded = true
    await refreshShiftData()
  } catch (e) { showToast(e.error || 'Không thể cập nhật ca làm') }
  finally {
    actionSaving.value = false
    if (succeeded) closeReasonModal()
  }
}

async function approveUnavailable(item) {
  if (!await confirmDialog({ title: 'Duyệt báo bận', message: `Chấp nhận báo bận của ${item.tenNhanVien || 'nhân viên'}?`, confirmText: 'Duyệt' })) return
  try {
    await api().reviewShiftUnavailable(item.id, true, 'Admin đã chấp nhận yêu cầu')
    showToast('Đã duyệt yêu cầu báo bận')
    await loadSchedules()
  } catch (e) { showToast(e.error || 'Không thể duyệt yêu cầu') }
}

async function checkIn(item) {
  if (!await confirmDialog({ title: 'Check-in', message: `Bắt đầu ca ${item.caLam || ''} ngay bây giờ?`, confirmText: 'Check-in' })) return
  try {
    await api().checkInShift(item.id)
    showToast('Check-in thành công')
    await refreshShiftData()
  } catch (e) { showToast(e.error || 'Không thể check-in') }
}

async function checkOut(item) {
  if (!await confirmDialog({ title: 'Check-out', message: 'Kết thúc ca làm hiện tại?', confirmText: 'Check-out' })) return
  try {
    await api().checkOutShift(item.id)
    showToast('Check-out thành công')
    await refreshShiftData()
  } catch (e) { showToast(e.error || 'Không thể check-out') }
}

function openAdd() {
  if (!isAdmin.value) return
  editingId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openAddFor(day, shift) {
  if (!isAdmin.value) return
  editingId.value = null
  form.value = { ...defaultForm(), ngayLam: day, caLam: shift }
  applyShiftPreset()
  showModal.value = true
}

function openEdit(item) {
  if (!isAdmin.value) return
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
  if (!isAdmin.value) return
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
  if (!isAdmin.value) return
  if (!await confirmDialog({
    title: 'Xóa ca làm',
    message: `Xóa ca làm của ${item.tenNhanVien || 'nhân viên'} ngày ${formatDate(item.ngayLam)}?`,
    confirmText: 'Xóa',
    variant: 'danger'
  })) return
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
    trangThai: 0,
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
  if (value === 2) return 'Đã báo bận'
  if (value === 3) return 'Chờ duyệt báo bận'
  return 'Chờ xác nhận'
}

function statusClass(status) {
  const value = Number(status)
  if (value === 1) return 'success'
  if (value === 2) return 'danger'
  if (value === 3) return 'warning'
  return 'pending'
}

function formatClock(value) {
  return value ? new Date(value).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : ''
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('vi-VN') + 'đ'
}

function formatWorkedTime(minutes) {
  const total = Number(minutes || 0)
  if (!total) return 'Chưa có giờ thực tế'
  const hours = Math.floor(total / 60)
  const remain = total % 60
  return `${hours}h${remain ? ` ${remain}p` : ''}`
}

function openActivityHistory(history) {
  activityShift.value = history
}

function activityTitle(type) {
  if (type === 'CHECK_IN') return 'Check-in'
  if (type === 'CHECK_OUT') return 'Check-out'
  if (type === 'POS_SALE') return 'Bán hàng POS'
  return 'Hoạt động'
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
  text-transform: uppercase; letter-spacing: 0;
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
.z-my-shift-card { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 20px; border-left: 4px solid var(--z-accent); }
.z-my-shift-eyebrow { color: var(--z-accent); font-size: 10px; font-weight: 700; margin-bottom: 5px; }
.z-my-shift-card h2 { margin: 0 0 5px; color: var(--z-dark); font-size: 18px; font-weight: 650; }
.z-my-shift-card p { margin: 0; color: var(--z-gray); font-size: 13px; }
.z-attendance-line { margin-top: 5px; color: #166534; font-size: 10px; font-weight: 600; }
.z-busy-reason { max-width: 220px; margin-top: 5px; overflow: hidden; color: var(--z-gray); font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.z-row-action { border: none; border-radius: var(--z-radius); background: var(--z-dark); color: var(--z-white); padding: 5px 8px; font-size: 10px; font-weight: 600; }
.z-row-action.secondary { border: 1px solid var(--z-gray-border); background: var(--z-white); color: var(--z-dark); }
.z-approve-btn { color: #166534; background: #dcfce7; }
.z-reject-btn { color: #991b1b; background: #fee2e2; }
.z-history-header { padding: 18px 20px; display: flex; align-items: center; justify-content: space-between; gap: 18px; border-bottom: 1px solid var(--z-gray-border); }
.z-history-header p { margin: 3px 0 0; color: var(--z-gray); font-size: 12px; }
.z-cash-total { min-width: 220px; padding-left: 20px; border-left: 1px solid var(--z-gray-border); text-align: right; }
.z-cash-total span { display: block; color: var(--z-gray); font-size: 10px; text-transform: uppercase; }
.z-cash-total strong { display: block; margin-top: 4px; color: var(--z-dark); font-size: 20px; }
.z-history-sub { margin-top: 3px; color: var(--z-gray); font-size: 10px; }
.z-cash-value { color: #166534; }
.z-activity-caption { margin: 5px 0 0; color: var(--z-gray); font-size: 12px; }
.z-activity-summary { margin-bottom: 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.z-activity-summary div { padding: 12px; border: 1px solid var(--z-gray-border); background: var(--z-bg-alt); }
.z-activity-summary span, .z-activity-summary strong { display: block; }
.z-activity-summary span { color: var(--z-gray); font-size: 10px; }
.z-activity-summary strong { margin-top: 4px; color: var(--z-dark); font-size: 14px; }
.z-activity-timeline { max-height: 430px; overflow-y: auto; }
.z-activity-row { position: relative; min-height: 58px; padding: 0 0 18px 28px; border-left: 1px solid var(--z-gray-border); }
.z-activity-row:last-child { border-left-color: transparent; }
.z-activity-dot { position: absolute; top: 2px; left: -5px; width: 9px; height: 9px; border-radius: 50%; background: var(--z-accent); }
.z-activity-row strong { color: var(--z-dark); font-size: 12px; }
.z-activity-row p { margin: 4px 0 0; color: var(--z-gray); font-size: 12px; }
@media (max-width: 760px) { .z-my-shift-card { align-items: flex-start; flex-direction: column; } }
@media (max-width: 760px) {
  .z-history-header { align-items: flex-start; flex-direction: column; }
  .z-cash-total { width: 100%; padding: 12px 0 0; border-top: 1px solid var(--z-gray-border); border-left: 0; text-align: left; }
}
</style>
