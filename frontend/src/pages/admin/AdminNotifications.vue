<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 style="font-size:24px;font-weight:700;margin:0" class="z-gradient-text">Quản lý Thông báo</h2>
        <div style="font-size:13px;color:var(--z-gray)">Tạo và gửi thông báo hệ thống, khuyến mãi đến toàn bộ khách hàng</div>
      </div>
      <button class="lm-btn-primary" @click="openAdd">
        <i class="bi bi-plus-lg"></i>
        <span>Thêm thông báo</span>
      </button>
    </div>

    <!-- Search & Filter -->
    <div class="z-admin-card mb-4">
      <div class="row g-3">
        <div class="col-md-6">
          <input v-model="search" type="text" class="lm-input" placeholder="Tìm kiếm theo tiêu đề hoặc nội dung...">
        </div>
        <div class="col-md-3">
          <select v-model="filterType" class="lm-input">
            <option value="">Tất cả loại thông báo</option>
            <option value="HeThong">Hệ thống</option>
            <option value="KhuyenMai">Khuyến mãi</option>
            <option value="DonHang">Đơn hàng</option>
          </select>
        </div>
        <div class="col-md-3">
          <select v-model="filterStatus" class="lm-input">
            <option value="">Tất cả trạng thái</option>
            <option value="1">Đang hiển thị (Active)</option>
            <option value="0">Bản nháp (Draft)</option>
          </select>
        </div>
      </div>
    </div>

    <!-- List table -->
    <div class="z-admin-card">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border spinner-border-sm text-secondary"></div>
        <p style="color:var(--z-gray);font-size:13px;margin-top:8px">Đang tải danh sách thông báo...</p>
      </div>

      <div v-else class="table-responsive">
        <table class="z-table">
          <thead>
            <tr>
              <th style="width: 80px">ID</th>
              <th style="width: 140px">Loại</th>
              <th>Tiêu đề</th>
              <th style="width: 180px">Ngày tạo</th>
              <th style="width: 150px">Trạng thái</th>
              <th style="width: 150px; text-align: right">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="paginatedAnnouncements.length === 0">
              <td colspan="6" class="text-center py-4" style="color:var(--z-gray)">Chưa có thông báo nào phù hợp</td>
            </tr>
            <tr v-for="item in paginatedAnnouncements" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>
                <span :class="['z-status', getLabelClass(item.loai)]">
                  {{ getTypeName(item.loai) }}
                </span>
              </td>
              <td>
                <div style="font-weight:600;color:var(--z-dark)">{{ item.tieuDe }}</div>
                <div style="font-size:12px;color:var(--z-gray);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:350px">
                  {{ item.noiDung }}
                </div>
              </td>
              <td>{{ formatDateTime(item.ngayTao) }}</td>
              <td>
                <span :class="['z-status', item.trangThai === 1 ? 'success' : 'pending']">
                  {{ item.trangThai === 1 ? 'Đang hiển thị' : 'Bản nháp' }}
                </span>
              </td>
              <td>
                <div class="d-flex justify-content-end gap-2">
                  <button class="z-icon-btn text-primary" title="Xem chi tiết" @click="viewDetail(item)">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button class="z-icon-btn text-warning" title="Chỉnh sửa" @click="openEdit(item)">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="z-icon-btn text-danger" title="Xóa" @click="deleteItem(item.id)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span style="font-size: 13px; color: var(--z-gray)">
          Hiển thị từ {{ (currentPage - 1) * itemsPerPage + 1 }} đến {{ Math.min(currentPage * itemsPerPage, filteredAnnouncements.length) }} trong tổng số {{ filteredAnnouncements.length }} thông báo
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

    <!-- Edit/Add Modal -->
    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal" style="max-width: 600px;">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">
            {{ editingId ? 'Cập nhật thông báo' : 'Tạo thông báo mới' }}
          </h3>
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="d-flex flex-column gap-3">
          <div>
            <label class="z-label">Tiêu đề thông báo *</label>
            <input v-model="form.tieuDe" type="text" class="lm-input" placeholder="VD: Khuyến mãi tết dương lịch 2026">
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="z-label">Loại thông báo</label>
              <select v-model="form.loai" class="lm-input">
                <option value="HeThong">Hệ thống</option>
                <option value="KhuyenMai">Khuyến mãi</option>
                <option value="DonHang">Đơn hàng</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="z-label">Trạng thái</label>
              <select v-model="form.trangThai" class="lm-input">
                <option :value="1">Đang hiển thị (Active)</option>
                <option :value="0">Bản nháp (Draft)</option>
              </select>
            </div>
          </div>

          <div>
            <label class="z-label">Nội dung chi tiết *</label>
            <textarea v-model="form.noiDung" class="lm-input" rows="5" placeholder="Nhập nội dung thông báo..."></textarea>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
          <button class="lm-btn-primary" :disabled="saving" @click="saveItem">
            <span>{{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật' : 'Tạo mới') }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- View Details Modal -->
    <div v-if="selectedItem" class="z-modal-overlay" @click.self="selectedItem = null">
      <div class="z-modal" style="max-width: 500px;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <span :class="['z-status', getLabelClass(selectedItem.loai)]">
            {{ getTypeName(selectedItem.loai) }}
          </span>
          <button class="z-icon-btn" @click="selectedItem = null"><i class="bi bi-x-lg"></i></button>
        </div>

        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 8px;">{{ selectedItem.tieuDe }}</h3>
        <p style="font-size: 12px; color: var(--z-gray); margin-bottom: 16px;">
          Ngày tạo: {{ formatDateTime(selectedItem.ngayTao) }} | Trạng thái: 
          <span :style="{ color: selectedItem.trangThai === 1 ? '#10b981' : '#f59e0b', fontWeight: '600' }">
            {{ selectedItem.trangThai === 1 ? 'Đang hiển thị' : 'Bản nháp' }}
          </span>
        </p>

        <div class="p-3 bg-light rounded" style="border: 1px solid var(--z-gray-border); line-height: 1.6; font-size: 13px; white-space: pre-wrap;">
          {{ selectedItem.noiDung }}
        </div>

        <div class="d-flex justify-content-end mt-4">
          <button class="lm-btn-primary" @click="selectedItem = null">
            <span>Đóng</span>
          </button>
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

const { showToast } = useToast()

const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editingId = ref(null)
const selectedItem = ref(null)

const search = ref('')
const filterType = ref('')
const filterStatus = ref('')

const announcements = ref([])

const form = ref({
  tieuDe: '',
  noiDung: '',
  loai: 'HeThong',
  trangThai: 1
})

onMounted(() => {
  loadAnnouncements()
})

async function loadAnnouncements() {
  loading.value = true
  try {
    announcements.value = await api().getThongBao()
  } catch (e) {
    showToast('Không thể tải danh sách thông báo')
  } finally {
    loading.value = false
  }
}

const filteredAnnouncements = computed(() => {
  return announcements.value.filter(item => {
    const matchesSearch = !search.value.trim() || 
      (item.tieuDe || '').toLowerCase().includes(search.value.toLowerCase()) || 
      (item.noiDung || '').toLowerCase().includes(search.value.toLowerCase())
    const matchesType = !filterType.value || item.loai === filterType.value
    const matchesStatus = !filterStatus.value || String(item.trangThai) === filterStatus.value
    return matchesSearch && matchesType && matchesStatus
  })
})

// Pagination
const currentPage = ref(1)
const itemsPerPage = 10
const totalPages = computed(() => Math.ceil(filteredAnnouncements.value.length / itemsPerPage))
const paginatedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredAnnouncements.value.slice(start, start + itemsPerPage)
})

watch([search, filterType, filterStatus], () => {
  currentPage.value = 1
}, { deep: true })

function openAdd() {
  editingId.value = null
  form.value = {
    tieuDe: '',
    noiDung: '',
    loai: 'HeThong',
    trangThai: 1
  }
  showModal.value = true
}

function openEdit(item) {
  editingId.value = item.id
  form.value = {
    tieuDe: item.tieuDe,
    noiDung: item.noiDung,
    loai: item.loai || 'HeThong',
    trangThai: item.trangThai ?? 1
  }
  showModal.value = true
}

function viewDetail(item) {
  selectedItem.value = item
}

async function saveItem() {
  if (!form.value.tieuDe || !form.value.noiDung) {
    showToast('Vui lòng nhập đầy đủ tiêu đề và nội dung')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await api().updateThongBao(editingId.value, form.value)
      showToast('Cập nhật thông báo thành công', 'success')
    } else {
      await api().addThongBao(form.value)
      showToast('Tạo thông báo thành công', 'success')
    }
    showModal.value = false
    await loadAnnouncements()
  } catch (e) {
    showToast('Lỗi khi lưu thông báo')
  } finally {
    saving.value = false
  }
}

async function deleteItem(id) {
  if (!confirm('Bạn có chắc chắn muốn xoá thông báo này?')) return
  try {
    await api().deleteThongBao(id)
    showToast('Xoá thông báo thành công', 'success')
    await loadAnnouncements()
  } catch (e) {
    showToast('Lỗi khi xoá thông báo')
  }
}

function getLabelClass(type) {
  const classes = {
    HeThong: 'info',
    KhuyenMai: 'success',
    DonHang: 'warning'
  }
  return classes[type] || 'info'
}

function getTypeName(type) {
  const names = {
    HeThong: 'Hệ thống',
    KhuyenMai: 'Khuyến mãi',
    DonHang: 'Đơn hàng'
  }
  return names[type] || type
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>
