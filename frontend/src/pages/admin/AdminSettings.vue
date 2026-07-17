<template>
  <AdminLayout>
    <h1 class="z-display mb-1 z-settings-title">Cài đặt</h1>
    <p class="z-settings-subtitle">Quản lý tài khoản đang đăng nhập và xem thông tin hệ thống</p>

    <div class="z-settings-tabs" role="tablist" aria-label="Nhóm cài đặt">
      <button v-for="tab in tabs" :key="tab.key" type="button" role="tab"
              class="z-tab" :class="{ active: activeTab === tab.key }"
              :aria-selected="activeTab === tab.key" @click="activeTab = tab.key">
        <i class="bi" :class="tab.icon"></i>
        {{ tab.label }}
      </button>
    </div>

    <div v-if="activeTab === 'account'" class="row g-3">
      <div class="col-lg-6">
        <section class="z-admin-card h-100">
          <h2 class="z-admin-card-title mb-3"><i class="bi bi-person-circle me-2"></i>Thông tin tài khoản</h2>
          <div class="d-flex flex-column gap-3">
            <div>
              <label class="z-label" for="settings-name">Tên hiển thị</label>
              <input id="settings-name" class="lm-input" :value="profile.hoVaTen || profile.username || 'Chưa cập nhật'" disabled>
            </div>
            <div>
              <label class="z-label" for="settings-email">Email</label>
              <input id="settings-email" class="lm-input" :value="profile.email || 'Chưa cập nhật'" disabled>
            </div>
            <div>
              <label class="z-label" for="settings-role">Vai trò</label>
              <input id="settings-role" class="lm-input" :value="roleLabel" disabled>
            </div>
          </div>
        </section>
      </div>

      <div class="col-lg-6">
        <form class="z-admin-card h-100" @submit.prevent="submitPasswordChange">
          <h2 class="z-admin-card-title mb-3"><i class="bi bi-shield-lock me-2"></i>Đổi mật khẩu</h2>
          <div class="d-flex flex-column gap-3">
            <div>
              <label class="z-label" for="current-password">Mật khẩu hiện tại</label>
              <input id="current-password" v-model="passwordForm.current" class="lm-input" type="password"
                     autocomplete="current-password" required placeholder="Nhập mật khẩu hiện tại">
            </div>
            <div>
              <label class="z-label" for="new-password">Mật khẩu mới</label>
              <input id="new-password" v-model="passwordForm.next" class="lm-input" type="password"
                     autocomplete="new-password" required placeholder="Tối thiểu 8 ký tự, gồm chữ và số">
            </div>
            <div>
              <label class="z-label" for="confirm-password">Xác nhận mật khẩu mới</label>
              <input id="confirm-password" v-model="passwordForm.confirm" class="lm-input" type="password"
                     autocomplete="new-password" required placeholder="Nhập lại mật khẩu mới">
            </div>
            <button class="lm-btn-primary z-password-submit" type="submit" :disabled="savingPassword">
              <span v-if="savingPassword">Đang cập nhật...</span>
              <span v-else><i class="bi bi-key me-1"></i>Đổi mật khẩu</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-else class="row g-3">
      <div class="col-lg-7">
        <section class="z-admin-card">
          <h2 class="z-admin-card-title mb-3"><i class="bi bi-info-circle me-2"></i>Thông tin hệ thống</h2>
          <dl class="z-system-list mb-0">
            <div><dt>Phiên bản ứng dụng</dt><dd>Zestia 1.0.0</dd></div>
            <div><dt>Frontend</dt><dd>Vue 3 + Vite</dd></div>
            <div><dt>Backend</dt><dd>Spring Boot 3.5</dd></div>
            <div><dt>Cơ sở dữ liệu</dt><dd>Microsoft SQL Server</dd></div>
            <div><dt>Môi trường hiện tại</dt><dd>Local development</dd></div>
          </dl>
        </section>
      </div>
      <div class="col-lg-5">
        <section class="z-admin-card h-100">
          <h2 class="z-admin-card-title mb-3"><i class="bi bi-check2-circle me-2"></i>Trạng thái</h2>
          <div class="z-system-status">
            <span class="z-status success">Đã xác thực</span>
            <p>Tài khoản <strong>{{ profile.username }}</strong> đang hoạt động với quyền {{ roleLabel.toLowerCase() }}.</p>
          </div>
        </section>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api, useAuth } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'

const { getUser } = useAuth()
const { confirmDialog } = useConfirm()
const { showToast } = useToast()

const activeTab = ref('account')
const savingPassword = ref(false)
const profile = ref(getUser() || {})
const passwordForm = reactive({ current: '', next: '', confirm: '' })
const tabs = [
  { key: 'account', label: 'Tài khoản', icon: 'bi-person-gear' },
  { key: 'system', label: 'Hệ thống', icon: 'bi-gear' }
]

const roleLabel = computed(() => profile.value.role === 'Admin' ? 'Quản trị viên' : 'Nhân viên')

onMounted(async () => {
  try {
    profile.value = { ...profile.value, ...await api().me() }
  } catch (error) {
    showToast(error.error || error.message || 'Không thể tải thông tin tài khoản', 'error')
  }
})

async function submitPasswordChange() {
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,100}$/.test(passwordForm.next)) {
    showToast('Mật khẩu mới cần 8-100 ký tự, gồm ít nhất một chữ và một số', 'error')
    return
  }
  if (passwordForm.next !== passwordForm.confirm) {
    showToast('Mật khẩu xác nhận không khớp', 'error')
    return
  }
  const accepted = await confirmDialog({
    title: 'Đổi mật khẩu',
    message: 'Mật khẩu tài khoản đang đăng nhập sẽ được thay đổi. Bạn có muốn tiếp tục?',
    confirmText: 'Đổi mật khẩu',
    variant: 'danger'
  })
  if (!accepted) return

  savingPassword.value = true
  try {
    const response = await api().changePassword(passwordForm.current, passwordForm.next)
    passwordForm.current = ''
    passwordForm.next = ''
    passwordForm.confirm = ''
    showToast(response.message || 'Đổi mật khẩu thành công', 'success')
  } catch (error) {
    showToast(error.error || error.message || 'Không thể đổi mật khẩu', 'error')
  } finally {
    savingPassword.value = false
  }
}
</script>

<style scoped>
.z-settings-title { font-size: 26px; font-weight: 500; color: var(--z-dark); }
.z-settings-subtitle { margin-bottom: 24px; color: var(--z-gray); font-size: 14px; }
.z-settings-tabs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 24px; }
.z-tab {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 15px; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius);
  background: var(--z-white); color: var(--z-gray); font: 500 13px var(--z-font-body);
  cursor: pointer; transition: border-color .18s ease, background-color .18s ease, color .18s ease;
}
.z-tab:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-tab.active { border-color: var(--z-dark); background: var(--z-dark); color: var(--z-white); }
.z-admin-card-title i { color: var(--z-accent); }
.z-label { display: block; margin-bottom: 6px; color: var(--z-dark); font-size: 13px; font-weight: 500; }
.z-password-submit { align-self: flex-start; min-width: 158px; justify-content: center; }
.z-system-list > div { display: flex; justify-content: space-between; gap: 24px; padding: 11px 0; border-bottom: 1px solid var(--z-gray-border); }
.z-system-list > div:last-child { border-bottom: 0; }
.z-system-list dt { color: var(--z-gray); font-size: 13px; font-weight: 400; }
.z-system-list dd { margin: 0; color: var(--z-dark); font-size: 13px; font-weight: 600; text-align: right; }
.z-system-status p { margin: 18px 0 0; color: var(--z-gray); font-size: 13px; line-height: 1.7; }
@media (max-width: 575px) {
  .z-password-submit { width: 100%; }
  .z-system-list > div { align-items: flex-start; }
}
</style>
