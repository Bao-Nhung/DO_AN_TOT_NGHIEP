<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý nhân viên</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ staffList.length }} nhân viên</p>
      </div>
      <button class="btn btn-primary btn-sm" @click="openModal('add')">
        <i class="bi bi-plus-lg me-1"></i> Thêm nhân viên
      </button>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Họ và tên</th>
            <th>Tên đăng nhập</th>
            <th>Email</th>
            <th>Số điện thoại</th>
            <th>Trạng thái</th>
            <th class="text-end">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(staff, index) in staffList" :key="staff.id">
            <td>{{ index + 1 }}</td>
            <td>{{ staff.hoVaTen }}</td>
            <td>{{ staff.username }}</td>
            <td>{{ staff.email }}</td>
            <td>{{ staff.phone }}</td>
            <td>
              <span :class="staff.status === 1 ? 'badge bg-success' : 'badge bg-danger'">
                {{ staff.status === 1 ? 'Đang hoạt động' : 'Đang khóa' }}
              </span>
            </td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-secondary me-2" @click="openModal('edit', staff)">Sửa</button>
              <button
                :class="staff.status === 1 ? 'btn btn-sm btn-outline-danger' : 'btn btn-sm btn-outline-success'"
                @click="toggleStatus(staff)">
                {{ staff.status === 1 ? 'Khóa' : 'Mở khóa' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="custom-modal-overlay" @click.self="showModal = false">
      <div class="custom-modal-content">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h4 class="mb-0">{{ modalType === 'add' ? 'Thêm nhân viên mới' : 'Cập nhật nhân viên' }}</h4>
          <button class="btn btn-sm btn-outline-secondary" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="mb-3">
          <label class="form-label">Họ và tên</label>
          <input type="text" class="form-control" v-model="currentStaff.hoVaTen" />
        </div>
        <div class="mb-3">
          <label class="form-label">Email</label>
          <input type="email" class="form-control" v-model="currentStaff.email" />
        </div>
        <div class="mb-3">
          <label class="form-label">Tên đăng nhập</label>
          <input type="text" class="form-control" v-model="currentStaff.username" />
        </div>
        <div class="mb-3">
          <label class="form-label">Mật khẩu</label>
          <input type="password" class="form-control" v-model="currentStaff.password"
                 :placeholder="modalType === 'add' ? 'Tạo mật khẩu cho nhân viên' : 'Để trống nếu giữ nguyên mật khẩu'" />
        </div>
        <div class="mb-3">
          <label class="form-label">Số điện thoại</label>
          <input type="text" class="form-control" v-model="currentStaff.phone" />
        </div>
        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-secondary btn-sm" @click="showModal = false">Hủy</button>
          <button class="btn btn-primary btn-sm" @click="saveStaff">Lưu</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const staffList = ref([])
const loading = ref(false)
const showModal = ref(false)
const modalType = ref('add')
const currentStaff = ref({ id: null, hoVaTen: '', email: '', username: '', password: '', phone: '', status: 1 })

const fetchStaff = async () => {
  loading.value = true
  try {
    const data = await api().getNhanVien()
    staffList.value = data.map(item => ({
      ...item,
      hoVaTen: item.hoVaTen || item.fullName || '',
      username: item.username || '',
      phone: item.phone || item.soDienThoai || '',
      status: item.status != null ? item.status : 1
    }))
  } catch (e) {
    console.error('Fetch staff failed', e)
    showToast('Không tải được danh sách nhân viên')
  } finally {
    loading.value = false
  }
}

const openModal = (type, staff = null) => {
  modalType.value = type
  if (type === 'edit' && staff) {
    currentStaff.value = {
      id: staff.id,
      hoVaTen: staff.hoVaTen,
      email: staff.email,
      username: staff.username,
      password: '',
      phone: staff.phone,
      status: staff.status
    }
  } else {
    currentStaff.value = { id: null, hoVaTen: '', email: '', username: '', password: '', phone: '', status: 1 }
  }
  showModal.value = true
}

        password: currentStaff.value.password,
        phone: currentStaff.value.phone,
        status: currentStaff.value.status
      })
      showToast('Thêm nhân viên thành công')
    } else {
      await api().updateNhanVien(currentStaff.value.id, {
        hoVaTen: currentStaff.value.hoVaTen,
        email: currentStaff.value.email,
        username: currentStaff.value.username,
        password: currentStaff.value.password || undefined,
        phone: currentStaff.value.phone,
        status: currentStaff.value.status
      })
      showToast('Cập nhật nhân viên thành công')
    }
    await fetchStaff()
    showModal.value = false
  } catch (e) {
    alert(e.error || e.message || 'Lỗi khi lưu nhân viên')
  }
}

const toggleStatus = async (staff) => {
  const nextStatus = staff.status === 1 ? 0 : 1
  if (!confirm(`Bạn có chắc chắn muốn ${staff.status === 1 ? 'khóa' : 'mở khóa'} nhân viên ${staff.hoVaTen}?`)) {
    return
  }

  try {
    await api().updateNhanVienStatus(staff.id, nextStatus)
    staff.status = nextStatus
    showToast('Thay đổi trạng thái thành công')
  } catch (e) {
    alert(e.error || e.message || 'Lỗi khi thay đổi trạng thái')
  }
}

onMounted(fetchStaff)
</script>

<style scoped>
.custom-modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}
.custom-modal-content {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.14);
}
</style>
