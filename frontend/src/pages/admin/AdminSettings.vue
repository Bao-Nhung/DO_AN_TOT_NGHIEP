<template>
  <AdminLayout>
    <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Cài đặt</h1>
    <p style="font-size:14px;color:var(--z-gray);margin-bottom:24px">Quản lý tài khoản và hệ thống</p>

    <!-- Navigation tabs -->
    <div class="d-flex gap-2 mb-4 flex-wrap">
      <button v-for="tab in tabs" :key="tab.key"
              class="z-tab" :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key">
        <i class="bi" :class="tab.icon" style="font-size:14px"></i>
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab: Tài khoản Admin -->
    <div v-if="activeTab === 'account'">
      <div class="row g-3">
        <div class="col-lg-6">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3"><i class="bi bi-person-circle me-2" style="color:var(--z-accent)"></i>Thông tin Admin</h3>
            <div class="d-flex flex-column gap-3">
              <div>
                <label class="z-label">Tên hiển thị</label>
                <input class="lm-input" value="Admin" disabled>
              </div>
              <div>
                <label class="z-label">Email</label>
                <input class="lm-input" value="admin@zestia.vn" disabled>
              </div>
              <div>
                <label class="z-label">Vai trò</label>
                <input class="lm-input" value="Quản trị viên" disabled>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3"><i class="bi bi-shield-lock me-2" style="color:var(--z-accent)"></i>Bảo mật</h3>
            <div class="d-flex flex-column gap-3">
              <div>
                <label class="z-label">Mật khẩu hiện tại</label>
                <input class="lm-input" type="password" placeholder="••••••••">
              </div>
              <div>
                <label class="z-label">Mật khẩu mới</label>
                <input class="lm-input" type="password" placeholder="Nhập mật khẩu mới">
              </div>
              <button class="lm-btn-primary" style="align-self:flex-start" @click="showToast('Tính năng đang phát triển')"><span>Đổi mật khẩu</span></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab: Hệ thống -->
    <div v-if="activeTab === 'system'">
      <div class="row g-3">
        <div class="col-lg-6">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3"><i class="bi bi-info-circle me-2" style="color:var(--z-accent)"></i>Thông tin hệ thống</h3>
            <div class="d-flex flex-column gap-2">
              <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid var(--z-gray-border)">
                <span style="font-size:13px;color:var(--z-gray)">Phiên bản</span>
                <span style="font-size:13px;font-weight:500">Zestia v1.0.0</span>
              </div>
              <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid var(--z-gray-border)">
                <span style="font-size:13px;color:var(--z-gray)">Frontend</span>
                <span style="font-size:13px;font-weight:500">Vue 3 + Vite</span>
              </div>
              <div class="d-flex justify-content-between py-2" style="border-bottom:1px solid var(--z-gray-border)">
                <span style="font-size:13px;color:var(--z-gray)">Backend</span>
                <span style="font-size:13px;font-weight:500">Spring Boot 3.5</span>
              </div>
              <div class="d-flex justify-content-between py-2">
                <span style="font-size:13px;color:var(--z-gray)">Database</span>
                <span style="font-size:13px;font-weight:500">SQL Server</span>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="z-admin-card">
            <h3 class="z-admin-card-title mb-3"><i class="bi bi-tools me-2" style="color:var(--z-accent)"></i>Công cụ nhanh</h3>
            <div class="d-flex flex-column gap-2">
              <button class="z-tool-btn" @click="seedProducts">
                <i class="bi bi-database-add"></i>
                <div>
                  <div style="font-weight:500">Tạo dữ liệu mẫu</div>
                  <div style="font-size:11px;color:var(--z-gray)">Thêm 24 sản phẩm váy mẫu vào hệ thống</div>
                </div>
              </button>
              <button class="z-tool-btn" @click="showToast('Tính năng đang phát triển')">
                <i class="bi bi-arrow-repeat"></i>
                <div>
                  <div style="font-weight:500">Đồng bộ dữ liệu</div>
                  <div style="font-size:11px;color:var(--z-gray)">Làm mới cache và đồng bộ với database</div>
                </div>
              </button>
              <button class="z-tool-btn" @click="showToast('Tính năng đang phát triển')">
                <i class="bi bi-download"></i>
                <div>
                  <div style="font-weight:500">Xuất dữ liệu</div>
                  <div style="font-size:11px;color:var(--z-gray)">Xuất danh sách sản phẩm, đơn hàng ra file</div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const activeTab = ref('account')
const tabs = [
  { key: 'account', label: 'Tài khoản', icon: 'bi-person-gear' },
  { key: 'system', label: 'Hệ thống', icon: 'bi-gear' },
]

async function seedProducts() {
  try {
    const res = await fetch('http://localhost:8080/api/vay/seed', { method: 'POST', headers: { 'Content-Type': 'application/json' } })
    const data = await res.json()
    showToast(data.message || 'Đã tạo dữ liệu mẫu!')
  } catch (e) { showToast('Lỗi khi tạo dữ liệu mẫu') }
}
</script>

<style scoped>
.z-tab {
  padding: 8px 16px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: 20px;
  font-size: 13px; font-weight: 500; color: var(--z-gray);
  cursor: pointer; transition: all 0.2s;
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--z-font-body);
}
.z-tab:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-tab.active { background: var(--z-dark); color: var(--z-white); border-color: var(--z-dark); }
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
.z-tool-btn {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 16px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  cursor: pointer; transition: all 0.2s; text-align: left;
  font-family: var(--z-font-body); font-size: 13px; color: var(--z-dark); width: 100%;
}
.z-tool-btn:hover { border-color: var(--z-accent); background: var(--z-accent-soft); }
.z-tool-btn i { font-size: 20px; color: var(--z-accent); flex-shrink: 0; }
</style>
