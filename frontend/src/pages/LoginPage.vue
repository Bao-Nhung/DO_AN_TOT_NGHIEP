<template>
  <div class="lm-login-page">
    <div class="lm-login-visual p-5">
      <div class="position-relative" style="z-index:1">
        <p class="lm-eyebrow mb-4">Chào mừng trở lại</p>
        <h2 class="z-display mb-4" style="font-size:48px;font-weight:400;line-height:1.1;color:var(--z-white)">
          Không gian<br>thời trang <em style="font-style:italic;color:var(--z-accent)">của bạn</em>
        </h2>
        <p class="mb-5" style="font-size:14px;font-weight:400;line-height:1.7;color:rgba(255,255,255,0.45);max-width:320px">
          Đăng nhập để khám phá những đặc quyền dành riêng cho thành viên Zestia — từ ưu đãi đến trải nghiệm cá nhân hoá.
        </p>
        <ul class="list-unstyled d-flex flex-column gap-3">
          <li v-for="f in features" :key="f" class="d-flex align-items-center gap-3"
              style="font-size:13px;font-weight:400;color:rgba(255,255,255,0.6)">
            <span style="width:6px;height:6px;border-radius:50%;background:var(--z-accent);flex-shrink:0"></span>
            {{ f }}
          </li>
        </ul>
      </div>
    </div>

    <div class="d-flex align-items-center justify-content-center p-5" style="background:var(--z-bg)">
      <div style="width:100%;max-width:400px">
        <RouterLink class="z-display d-inline-flex align-items-center gap-2 mb-5 text-decoration-none" to="/" aria-label="Zestia - Trang chủ"
              style="font-size:26px;font-weight:600;letter-spacing:0;cursor:pointer;color:inherit">
          <img class="z-brand-mark" src="/images/brand/zestia-mark.png" alt="" aria-hidden="true">
          <span>Zest<span class="lm-gold-text">ia</span></span>
        </RouterLink>

        <!-- Role Tabs -->
        <div class="d-flex mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden">
          <button v-for="r in roles" :key="r.key" type="button" :aria-pressed="loginRole === r.key"
                  class="flex-fill text-center"
                  :style="{
                    padding: '10px',
                    fontSize: '13px',
                    fontWeight: loginRole === r.key ? '600' : '400',
                    background: loginRole === r.key ? 'var(--z-accent)' : 'transparent',
                    color: loginRole === r.key ? 'var(--z-white)' : 'var(--z-gray)',
                    border: 'none',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    fontFamily: 'var(--z-font-body)'
                  }"
                  @click="loginRole = r.key; error = ''">
            <i class="bi" :class="r.icon" style="margin-right:6px"></i>{{ r.label }}
          </button>
        </div>

        <!-- LOGIN VIEW -->
        <template v-if="view === 'login'">
          <h1 class="z-display mb-2" style="font-size:32px;font-weight:400;color:var(--z-dark)">Đăng nhập</h1>
          <p class="mb-4" style="font-size:14px;font-weight:400;color:var(--z-gray)">
            <template v-if="loginRole === 'customer'">
              Chưa có tài khoản?
              <button type="button" class="z-auth-link" @click="view = 'register'; error = ''">Tạo tài khoản ngay</button>
            </template>
            <template v-else>Dành cho nhân viên và quản trị viên</template>
          </p>

          <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
            {{ error }}
          </div>

          <div class="d-flex flex-column gap-3 mb-4">
            <div>
              <label class="lm-form-label d-block mb-2">Tài khoản</label>
              <input class="lm-input" v-model="username" type="text"
                     :placeholder="loginRole === 'customer' ? 'Email hoặc số điện thoại' : 'Tên đăng nhập hoặc email'"
                     @keydown.enter="doLogin">
            </div>
            <div class="position-relative">
              <label class="lm-form-label d-block mb-2">Mật khẩu</label>
              <input class="lm-input" v-model="password" :type="showPw ? 'text' : 'password'" placeholder="••••••••" aria-label="Mật khẩu"
                     @keydown.enter="doLogin">
              <button type="button" :aria-label="showPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPw = !showPw"
                      style="position:absolute;right:14px;bottom:12px;border:none;background:none;cursor:pointer;color:var(--z-gray-light)">
                <i class="bi" :class="showPw ? 'bi-eye-slash' : 'bi-eye'" style="font-size:16px"></i>
              </button>
            </div>
          </div>

          <div class="d-flex justify-content-between align-items-center mb-4" style="font-size:13px">
            <label class="d-flex align-items-center gap-2" style="color:var(--z-gray);cursor:pointer">
              <input type="checkbox" checked style="accent-color:var(--z-accent)"> Ghi nhớ đăng nhập
            </label>
            <button type="button" class="z-auth-link" @click="view = 'forgot'; error = ''; forgotMessage = ''">Quên mật khẩu?</button>
          </div>

          <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doLogin">
            <span v-if="loading">Đang xử lý...</span>
            <span v-else>Đăng nhập</span>
          </button>
        </template>

        <!-- REGISTER VIEW -->
        <template v-if="view === 'register'">
          <h1 class="z-display mb-2" style="font-size:32px;font-weight:400;color:var(--z-dark)">Tạo tài khoản</h1>
          <p class="mb-4" style="font-size:14px;font-weight:400;color:var(--z-gray)">
            Đã có tài khoản?
            <button type="button" class="z-auth-link" @click="view = 'login'; error = ''">Đăng nhập</button>
          </p>

          <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
            {{ error }}
          </div>
          <div v-if="regSuccess" class="mb-3" style="padding:12px 16px;background:#dcfce7;color:#16a34a;border-radius:var(--z-radius);font-size:13px">
            {{ regSuccess }}
          </div>

          <div class="d-flex flex-column gap-3 mb-4">
            <div>
              <label class="lm-form-label d-block mb-2">Họ và tên</label>
              <input class="lm-input" v-model="regForm.hoVaTen" type="text" placeholder="Nguyễn Văn A">
            </div>
            <div>
              <label class="lm-form-label d-block mb-2">Email</label>
              <input class="lm-input" v-model="regForm.email" type="email" placeholder="email@example.com">
            </div>
            <div>
              <label class="lm-form-label d-block mb-2">Số điện thoại</label>
              <input class="lm-input" v-model="regForm.soDienThoai" type="tel" inputmode="numeric"
                     autocomplete="tel" maxlength="10" placeholder="0912 345 678" @input="normalizeRegistrationPhone">
            </div>
            <div class="position-relative">
              <label class="lm-form-label d-block mb-2">Mật khẩu</label>
              <input class="lm-input" v-model="regForm.matKhau" :type="showPw ? 'text' : 'password'" placeholder="Tối thiểu 8 ký tự, gồm chữ và số" aria-label="Mật khẩu đăng ký"
                     @keydown.enter="doRegister">
              <button type="button" :aria-label="showPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPw = !showPw"
                      style="position:absolute;right:14px;bottom:12px;border:none;background:none;cursor:pointer;color:var(--z-gray-light)">
                <i class="bi" :class="showPw ? 'bi-eye-slash' : 'bi-eye'" style="font-size:16px"></i>
              </button>
            </div>
          </div>

          <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doRegister">
            <span v-if="loading">Đang xử lý...</span>
            <span v-else>Tạo tài khoản</span>
          </button>
        </template>

        <!-- FORGOT PASSWORD VIEW -->
        <template v-if="view === 'forgot'">
          <h1 class="z-display mb-2" style="font-size:32px;font-weight:400;color:var(--z-dark)">Quên mật khẩu</h1>
          <p class="mb-4" style="font-size:14px;font-weight:400;color:var(--z-gray)">
            Nhập email, tên đăng nhập hoặc số điện thoại để nhận liên kết đặt lại mật khẩu
          </p>

          <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
            {{ error }}
          </div>

          <div v-if="forgotMessage" class="mb-3" style="padding:16px;background:#dcfce7;color:#16a34a;border-radius:var(--z-radius);font-size:13px">
            {{ forgotMessage }}
          </div>

          <div class="d-flex flex-column gap-3 mb-4">
            <div>
              <label class="lm-form-label d-block mb-2">Email / Tên đăng nhập / SĐT</label>
              <input class="lm-input" v-model="forgotId" type="text" placeholder="Nhập thông tin tài khoản"
                     @keydown.enter="doForgot">
            </div>
          </div>

          <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doForgot">
            <span v-if="loading">Đang gửi yêu cầu...</span>
            <span v-else>Gửi liên kết đặt lại</span>
          </button>

          <div class="text-center">
            <button type="button" class="z-auth-link" style="font-size:13px" @click="view = 'login'; error = ''; forgotMessage = ''">
              <i class="bi bi-arrow-left me-1"></i> Quay lại đăng nhập
            </button>
          </div>
        </template>

        <!-- RESET PASSWORD VIEW -->
        <template v-if="view === 'reset'">
          <h1 class="z-display mb-2" style="font-size:32px;font-weight:400;color:var(--z-dark)">Đặt lại mật khẩu</h1>
          <p class="mb-4" style="font-size:14px;font-weight:400;color:var(--z-gray)">
            Nhập mật khẩu mới cho tài khoản của bạn.
          </p>

          <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
            {{ error }}
          </div>

          <div class="d-flex flex-column gap-3 mb-4">
            <div class="position-relative">
              <label class="lm-form-label d-block mb-2">Mật khẩu mới</label>
              <input class="lm-input" v-model="resetPassword" :type="showPw ? 'text' : 'password'" placeholder="Tối thiểu 8 ký tự, gồm chữ và số" aria-label="Mật khẩu mới"
                     @keydown.enter="doResetPassword">
              <button type="button" :aria-label="showPw ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'" @click="showPw = !showPw"
                      style="position:absolute;right:14px;bottom:12px;border:none;background:none;cursor:pointer;color:var(--z-gray-light)">
                <i class="bi" :class="showPw ? 'bi-eye-slash' : 'bi-eye'" style="font-size:16px"></i>
              </button>
            </div>
            <div>
              <label class="lm-form-label d-block mb-2">Xác nhận mật khẩu mới</label>
              <input class="lm-input" v-model="resetPasswordConfirm" :type="showPw ? 'text' : 'password'" placeholder="Nhập lại mật khẩu mới" aria-label="Xác nhận mật khẩu mới"
                     @keydown.enter="doResetPassword">
            </div>
          </div>

          <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doResetPassword">
            <span v-if="loading">Đang cập nhật...</span>
            <span v-else>Cập nhật mật khẩu</span>
          </button>

          <div class="text-center">
            <button type="button" class="z-auth-link" style="font-size:13px" @click="view = 'login'; error = ''">
              <i class="bi bi-arrow-left me-1"></i> Quay lại đăng nhập
            </button>
          </div>
        </template>

        <!-- Divider (login view only) -->
        <template v-if="view === 'login'">
          <div class="d-flex align-items-center gap-3 mb-3">
            <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
            <span style="font-size:12px;color:var(--z-gray-light)">hoặc</span>
            <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
          </div>

          <div v-if="loginRole === 'customer'" class="z-google-login">
            <div v-if="googleClientId" ref="googleButton"></div>
            <button v-else class="z-google-unconfigured" type="button" disabled
                    title="Cấu hình VITE_GOOGLE_CLIENT_ID và GOOGLE_CLIENT_ID để bật đăng nhập Google">
              <i class="bi bi-google"></i>
              Đăng nhập Google chưa được cấu hình
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'
import { detachGoogleCredentialHandler, renderGoogleButton } from '@/composables/googleIdentity'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()
const { saveLogin } = useAuth()

const view = ref('login')
const loginRole = ref('customer')
const showPw = ref(false)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const googleButton = ref(null)
const defaultGoogleClientId = '906678560911-vv9vu3jsqkvgu7og8mjg8to7lhh88odt.apps.googleusercontent.com'
const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || defaultGoogleClientId
const googleCredentialHandler = response => handleGoogleCredential(response)

const roles = [
  { key: 'customer', label: 'Khách hàng', icon: 'bi-person' },
  { key: 'employee', label: 'Nhân viên', icon: 'bi-shield-lock' },
]

const features = [
  'Truy cập sớm bộ sưu tập mới',
  'Ưu đãi độc quyền cho thành viên',
  'Đồng bộ giỏ hàng và sản phẩm yêu thích',
  'Theo dõi đơn và đánh giá sản phẩm đã mua',
]

const regForm = ref({ hoVaTen: '', email: '', soDienThoai: '', matKhau: '' })
const regSuccess = ref('')
const forgotId = ref('')
const forgotMessage = ref('')
const resetToken = ref('')
const resetPassword = ref('')
const resetPasswordConfirm = ref('')

onMounted(() => {
  if (route.query.resetToken) {
    resetToken.value = String(route.query.resetToken)
    view.value = 'reset'
  }
  if (googleClientId) initializeGoogleButton()
})

onUnmounted(() => detachGoogleCredentialHandler(googleCredentialHandler))

function initializeGoogleButton(attempt = 0) {
  if (!googleButton.value) return
  if (!window.google?.accounts?.id) {
    if (attempt < 20) setTimeout(() => initializeGoogleButton(attempt + 1), 200)
    else error.value = 'Không tải được dịch vụ đăng nhập Google'
    return
  }
  renderGoogleButton(googleButton.value, googleClientId, googleCredentialHandler)
}

async function handleGoogleCredential(response) {
  if (!response?.credential) return
  loading.value = true
  error.value = ''
  try {
    const data = await api().googleLogin(response.credential)
    saveLogin(data)
    showToast('Đăng nhập Google thành công')
    const redirectPath = route.query.redirect ? String(route.query.redirect) : ''
    await router.replace(redirectPath && !redirectPath.startsWith('/admin') ? redirectPath : '/profile')
  } catch (e) {
    error.value = e.error || e.message || 'Đăng nhập Google thất bại'
  } finally {
    loading.value = false
  }
}

async function doLogin() {
  if (!username.value || !password.value) {
    error.value = 'Vui lòng nhập tài khoản và mật khẩu'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const data = await api().login(username.value, password.value)
    saveLogin(data)
    showToast('Đăng nhập thành công — Chào mừng ' + (data.hoVaTen || data.username) + '!')
    
    const isStaff = ['Admin', 'NhanVien', 'Nhân viên'].includes(data.role)
    const redirectPath = route.query.redirect ? String(route.query.redirect) : ''
    if (isStaff) {
      await router.replace('/admin')
    } else if (redirectPath && !redirectPath.startsWith('/admin')) {
      await router.replace(redirectPath)
    } else {
      await router.replace('/profile')
    }
  } catch (e) {
    error.value = e.error || e.message || 'Tài khoản hoặc mật khẩu không chính xác'
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  const { hoVaTen, email, soDienThoai, matKhau } = regForm.value
  if (!hoVaTen || !email || !soDienThoai || !matKhau) {
    error.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
    error.value = 'Email không hợp lệ'
    return
  }
  if (!/^0[35789]\d{8}$/.test(soDienThoai)) {
    error.value = 'Số điện thoại Việt Nam không hợp lệ'
    return
  }
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,100}$/.test(matKhau)) {
    error.value = 'Mật khẩu cần 8-100 ký tự, gồm ít nhất một chữ và một số'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const data = await api().register(regForm.value)
    saveLogin(data)
    showToast('Đăng ký thành công — Chào mừng ' + (data.hoVaTen || data.username) + '!')
    await router.replace('/profile')
  } catch (e) {
    error.value = e.error || e.message || 'Đăng ký thất bại'
  } finally {
    loading.value = false
  }
}

async function doForgot() {
  if (!forgotId.value) {
    error.value = 'Vui lòng nhập email hoặc tên đăng nhập'
    return
  }
  error.value = ''
  forgotMessage.value = ''
  loading.value = true
  try {
    const data = await api().forgotPassword(forgotId.value)
    forgotMessage.value = data?.message || 'Nếu tài khoản tồn tại, hệ thống đã gửi email hướng dẫn đặt lại mật khẩu.'
  } catch (e) {
    error.value = e.error || e.message || 'Không thể gửi yêu cầu đặt lại mật khẩu'
  } finally {
    loading.value = false
  }
}

async function doResetPassword() {
  if (!resetToken.value) {
    error.value = 'Liên kết đặt lại mật khẩu không hợp lệ'
    return
  }
  if (!resetPassword.value || !resetPasswordConfirm.value) {
    error.value = 'Vui lòng nhập đầy đủ mật khẩu mới'
    return
  }
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,100}$/.test(resetPassword.value)) {
    error.value = 'Mật khẩu mới cần 8-100 ký tự, gồm ít nhất một chữ và một số'
    return
  }
  if (resetPassword.value !== resetPasswordConfirm.value) {
    error.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  error.value = ''
  loading.value = true
  try {
    await api().resetPassword(resetToken.value, resetPassword.value)
    showToast('Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.')
    resetToken.value = ''
    resetPassword.value = ''
    resetPasswordConfirm.value = ''
    view.value = 'login'
    router.replace('/login')
  } catch (e) {
    error.value = e.error || e.message || 'Đặt lại mật khẩu thất bại'
  } finally {
    loading.value = false
  }
}

function normalizeRegistrationPhone(event) {
  regForm.value.soDienThoai = String(event.target.value || '').replace(/\D/g, '').slice(0, 10)
}
</script>

<style scoped>
.z-google-login {
  width: 100%;
  min-height: 44px;
  overflow: hidden;
}
.z-google-login :deep(iframe) {
  max-width: 100%;
}
.z-google-unconfigured {
  width: 100%;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  color: var(--z-gray);
  font-family: var(--z-font-body);
  font-size: 13px;
}
.z-auth-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--z-accent);
  cursor: pointer;
  font: inherit;
  font-weight: 500;
}
.z-auth-link:hover,
.z-auth-link:focus-visible { text-decoration: underline; }
</style>
