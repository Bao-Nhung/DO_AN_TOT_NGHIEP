<template>
  <div class="lm-login-page">
    <div class="lm-login-visual p-5">
      <div class="position-relative" style="z-index:1">
        <p class="lm-eyebrow mb-4">Chao mung tro lai</p>
        <h2 class="z-display mb-4" style="font-size:48px;font-weight:400;line-height:1.1;color:var(--z-white)">
          Khong gian<br>thoi trang <em style="font-style:italic;color:var(--z-accent)">cua ban</em>
        </h2>
        <p class="mb-5" style="font-size:14px;font-weight:400;line-height:1.7;color:rgba(255,255,255,0.45);max-width:320px">
          Dang nhap de kham pha nhung dac quyen danh rieng cho thanh vien Zestia — tu uu dai den trai nghiem ca nhan hoa.
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
        <h1 class="z-display mb-2" style="font-size:32px;font-weight:400;color:var(--z-dark)">Dang nhap</h1>
        <p class="mb-4" style="font-size:14px;font-weight:400;color:var(--z-gray)">
          Chua co tai khoan?
          <a @click="showToast('Dang ky thanh cong — Chao mung!')" style="color:var(--z-accent);cursor:pointer;font-weight:500">Tao tai khoan ngay</a>
        </p>

        <div v-if="error" class="mb-3" style="padding:12px 16px;background:#fee2e2;color:#dc2626;border-radius:var(--z-radius);font-size:13px">
          {{ error }}
        </div>

        <div class="d-flex flex-column gap-3 mb-4">
          <div>
            <label class="lm-form-label d-block mb-2">Tai khoan</label>
            <input class="lm-input" v-model="username" type="text" placeholder="Email, ten dang nhap hoac SDT"
                   @keydown.enter="doLogin">
          </div>
          <div class="position-relative">
            <label class="lm-form-label d-block mb-2">Mat khau</label>
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
            <input type="checkbox" checked style="accent-color:var(--z-accent)"> Ghi nho dang nhap
          </label>
          <a style="color:var(--z-accent);cursor:pointer;font-weight:500">Quen mat khau?</a>
        </div>

        <button class="lm-btn-primary w-100 justify-content-center mb-3" :disabled="loading" @click="doLogin">
          <span v-if="loading">Dang xu ly...</span>
          <span v-else>Dang nhap</span>
        </button>

        <div class="d-flex align-items-center gap-3 mb-3">
          <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
          <span style="font-size:12px;color:var(--z-gray-light)">hoac</span>
          <div style="flex:1;height:1px;background:var(--z-gray-border)"></div>
        </div>

        <button class="w-100 d-flex align-items-center justify-content-center gap-3"
                style="padding:12px;border:1px solid var(--z-gray-border);background:transparent;font-size:13px;font-family:var(--z-font-body);cursor:pointer;transition:all 0.3s;border-radius:var(--z-radius)"
                @click="$router.push('/profile')"
                @mouseover="e => e.target.style.borderColor = 'var(--z-accent)'"
                @mouseleave="e => e.target.style.borderColor = 'var(--z-gray-border)'">
          <svg width="16" height="16" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
          Tiep tuc voi Google
        </button>
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

const showPw = ref(false)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const features = [
  'Truy cap som bo suu tap moi',
  'Uu dai doc quyen cho thanh vien',
  'Tich diem va doi qua',
  'Tu van phong cach 1-1',
]

async function doLogin() {
  if (!username.value || !password.value) {
    error.value = 'Vui long nhap tai khoan va mat khau'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const data = await api().login(username.value, password.value)
    saveLogin(data)
    showToast('Dang nhap thanh cong — Chao mung ' + (data.hoVaTen || data.username) + '!')
    if (data.role === 'Admin' || data.role === 'Nhan vien' || data.role === 'Nhân viên') {
      router.push('/admin')
    } else {
      router.push('/profile')
    }
  } catch (e) {
    error.value = e.message || e.error || 'Tai khoan hoac mat khau khong chinh xac'
  } finally {
    loading.value = false
  }
}
</script>
