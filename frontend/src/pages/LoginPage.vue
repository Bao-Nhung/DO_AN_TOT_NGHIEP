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
        <span class="z-display d-block mb-5" style="font-size:26px;font-weight:600;letter-spacing:0.08em;cursor:pointer"
              @click="$router.push('/')">
          Zest<span class="lm-gold-text">ia</span>
        </span>

        <!-- Role Tabs -->
        <div class="d-flex mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden">
          <button v-for="r in roles" :key="r.key"
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
              <a @click="view = 'register'; error = ''" style="color:var(--z-accent);cursor:pointer;font-weight:500">Tạo tài khoản ngay</a>
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
              <input class="lm-input" v-model="password" :type="showPw ? 'text' : 'password'" placeholder="••••••••"
                     @keydown.enter="doLogin">
              <button @click="showPw = !showPw"
                      style="position:absolute;right:14px;bottom:12px;border:none;background:none;cursor:pointer;color:var(--z-gray-light)">
                <i class="bi" :class="showPw ? 'bi-eye-slash' : 'bi-eye'" style="font-size:16px"></i>
              </button>
            </div>
          </div>

          <div class="d-flex justify-content-between align-items-center mb-4" style="font-size:13px">
            <label class="d-flex align-items-center gap-2" style="color:var(--z-gray);cursor:pointer">
              <input type="checkbox" checked style="accent-color:var(--z-accent)"> Ghi nhớ đăng nhập
            </label>
            <a @click="view = 'forgot'; error = ''" style="color:var(--z-accent);cursor:pointer;font-weight:500">Quên mật khẩu?</a>
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
            <a @click="view = 'login'; error = ''" style="color:var(--z-accent);cursor:pointer;font-weight:500">Đăng nhập</a>
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
              <input class="lm-input" v-model="regForm.soDienThoai" type="tel" placeholder="0912 345 678">
            </div>
            <div class="position-relative">
              <label class="lm-form-label d-block mb-2">Mật khẩu</label>
              <input class="lm-input" v-model="regForm.matKhau" :type="showPw ? 'text' : 'password'" placeholder="Tối thiểu 6 ký tự"
                     @keydown.enter="doRegister">
              <button @click="showPw = !showPw"
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
            Nhập email hoặc tên đăng nhập để xem gợi ý mật khẩu
          </p>

          <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
            {{ error }}
          </div>

          <div v-if="forgotResult" class="mb-3" style="padding:16px;background:#dcfce7;color:#16a34a;border-radius:var(--z-radius);font-size:13px">
            <div class="mb-2"><strong>Email:</strong> {{ forgotResult.maskedEmail }}</div>
            <div><strong>Gợi ý:</strong> {{ forgotResult.hint }}</div>
          </div>

          <div class="d-flex flex-column gap-3 mb-4">
            <div>
              <label class="lm-form-label d-block mb-2">Email / Tên đăng nhập / SĐT</label>
              <input class="lm-input" v-model="forgotId" type="text" placeholder="Nhập thông tin tài khoản"
                     @keydown.enter="doForgot">
            </div>
          </div>

          <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doForgot">
            <span v-if="loading">Đang tìm kiếm...</span>
            <span v-else>Tìm tài khoản</span>
          </button>

          <div class="text-center">
            <a @click="view = 'login'; error = ''; forgotResult = null" style="color:var(--z-accent);cursor:pointer;font-size:13px;font-weight:500">
              <i class="bi bi-arrow-left me-1"></i> Quay lại đăng nhập
            </a>
          </div>
        </template>

        <!-- Divider (login view only) -->
        <template v-if="view === 'login'">
          <div class="d-flex align-items-center gap-3 mb-3">
            <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
            <span style="font-size:12px;color:var(--z-gray-light)">hoặc</span>
            <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
          </div>

          <button class="w-100 d-flex align-items-center justify-content-center gap-3"
                  style="padding:12px;border:1px solid var(--z-gray-border);background:transparent;font-size:13px;font-family:var(--z-font-body);cursor:pointer;transition:all 0.3s;border-radius:var(--z-radius)"
                  @mouseover="e => e.currentTarget.style.borderColor = 'var(--z-accent)'"
                  @mouseleave="e => e.currentTarget.style.borderColor = 'var(--z-gray-border)'">
            <svg width="16" height="16" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
            Tiếp tục với Google
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'

const router = useRouter()
const { showToast } = useToast()
const { saveLogin } = useAuth()

const view = ref('login')
const loginRole = ref('customer')
const showPw = ref(false)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const roles = [
  { key: 'customer', label: 'Khách hàng', icon: 'bi-person' },
  { key: 'employee', label: 'Nhân viên', icon: 'bi-shield-lock' },
]

const features = [
  'Truy cập sớm bộ sưu tập mới',
  'Ưu đãi độc quyền cho thành viên',
  'Tích điểm và đổi quà',
  'Tư vấn phong cách 1-1',
]

const regForm = ref({ hoVaTen: '', email: '', soDienThoai: '', matKhau: '' })
const regSuccess = ref('')
const forgotId = ref('')
const forgotResult = ref(null)

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
    if (data.role && data.role !== 'KhachHang') {
      router.push('/admin')
    } else {
      router.push('/profile')
    }
  } catch (e) {
    error.value = e.error || e.message || 'Tài khoản hoặc mật khẩu không chính xác'
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  const { hoVaTen, email, matKhau } = regForm.value
  if (!hoVaTen || !email || !matKhau) {
    error.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }
  if (matKhau.length < 6) {
    error.value = 'Mật khẩu phải có tối thiểu 6 ký tự'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const data = await api().register(regForm.value)
    saveLogin(data)
    showToast('Đăng ký thành công — Chào mừng ' + (data.hoVaTen || data.username) + '!')
    router.push('/profile')
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
  forgotResult.value = null
  loading.value = true
  try {
    const data = await api().forgotPassword(forgotId.value)
    forgotResult.value = data
  } catch (e) {
    error.value = e.error || e.message || 'Không tìm thấy tài khoản'
  } finally {
    loading.value = false
  }
}
</script>
