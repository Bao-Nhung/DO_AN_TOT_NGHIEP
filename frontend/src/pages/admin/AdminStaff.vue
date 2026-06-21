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
            <th>Email</th>
            <th>Số điện thoại</th>
            <th>Trạng thái</th>
            <th class="text-end">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(staff, index) in staffList" :key="staff.id">
            <td>{{ index + 1 }}</td>
            <td>{{ staff.fullName }}</td>
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
          <input type="text" class="form-control" v-model="currentStaff.fullName" />
        </div>
        <div class="mb-3">
          <label class="form-label">Email</label>
          <input type="email" class="form-control" v-model="currentStaff.email" />
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
import { ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const staffList = ref([
  { id: 1, fullName: 'Nguyễn Văn A', email: 'vana@gmail.com', phone: '0912345678', status: 1 },
  { id: 2, fullName: 'Trần Thị B', email: 'thib@gmail.com', phone: '0987654321', status: 1 },
  { id: 3, fullName: 'Lê Văn C', email: 'vanc@gmail.com', phone: '0933445566', status: 0 }
])

const showModal = ref(false)
const modalType = ref('add')
const currentStaff = ref({ id: null, fullName: '', email: '', phone: '', status: 1 })

const openModal = (type, staff = null) => {
  modalType.value = type
  if (type === 'edit' && staff) {
    currentStaff.value = { ...staff }
  } else {
    currentStaff.value = { id: null, fullName: '', email: '', phone: '', status: 1 }
  }
  showModal.value = true
}

const saveStaff = () => {
  if (!currentStaff.value.fullName || !currentStaff.value.email) {
    alert('Vui lòng nhập đầy đủ họ tên và email')
    return
  }

  if (modalType.value === 'add') {
    const newId = staffList.value.length + 1
    staffList.value.push({ ...currentStaff.value, id: newId })
  } else {
    const index = staffList.value.findIndex(item => item.id === currentStaff.value.id)
    if (index !== -1) {
      staffList.value[index] = { ...currentStaff.value }
    }
  }
  showModal.value = false
}

const toggleStatus = (staff) => {
  if (!confirm(`Bạn có chắc chắn muốn ${staff.status === 1 ? 'khóa' : 'mở khóa'} nhân viên ${staff.fullName}?`)) {
    return
  }
  staff.status = staff.status === 1 ? 0 : 1
}
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
