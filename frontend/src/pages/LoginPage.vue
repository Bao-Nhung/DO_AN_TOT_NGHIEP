<template>
  <div class="lm-login-page">
    <!-- Visual side -->
    <div class="lm-login-visual p-5">
      <div class="position-relative" style="z-index:1">
        <p class="lm-eyebrow mb-4">Chào mừng trở lại</p>
        <h2 class="lm-display mb-4" style="font-size:56px;font-weight:300;line-height:1;color:var(--lm-white);letter-spacing:-0.02em">
          Không gian<br>của sự <em style="font-style:italic;color:var(--lm-gold)">tinh tế</em>
        </h2>
        <p class="mb-5" style="font-size:13px;font-weight:300;line-height:1.8;color:rgba(255,255,255,0.45);max-width:320px">
          Đăng nhập để khám phá những đặc quyền dành riêng cho thành viên LUMIÈRE — từ bộ sưu tập giới hạn đến trải nghiệm mua sắm cá nhân hóa.
        </p>
        <ul class="list-unstyled d-flex flex-column gap-3">
          <li v-for="f in features" :key="f" class="d-flex align-items-center gap-3"
              style="font-size:12px;font-weight:300;color:rgba(255,255,255,0.6)">
            <span style="width:6px;height:6px;border-radius:50%;background:var(--lm-gold);flex-shrink:0"></span>
            {{ f }}
          </li>
        </ul>
      </div>
    </div>

    <!-- Form side -->
    <div class="d-flex align-items-center justify-content-center p-5" style="background:var(--lm-cream)">
      <div style="width:100%;max-width:400px">
        <span class="lm-display d-block mb-5" style="font-size:24px;font-weight:500;letter-spacing:0.3em;text-transform:uppercase;cursor:pointer"
              @click="$router.push('/')">
          LUM<span class="lm-gold-text">I</span>ÈRE
        </span>
        <h1 class="lm-display mb-2" style="font-size:38px;font-weight:300;color:var(--lm-black);letter-spacing:-0.01em">Đăng nhập</h1>
        <p class="mb-4" style="font-size:12px;font-weight:300;color:var(--lm-gray);line-height:1.6">
          Chưa có tài khoản?
          <a @click="showToast('Đăng ký thành công — Chào mừng!')" style="color:var(--lm-gold);cursor:pointer">Tạo tài khoản ngay</a>
        </p>

        <div class="d-flex flex-column gap-3 mb-4">
          <div>
            <label class="lm-form-label d-block mb-2">Email</label>
            <input class="lm-input" type="email" placeholder="email@example.com">
          </div>
          <div class="position-relative">
            <label class="lm-form-label d-block mb-2">Mật khẩu</label>
            <input class="lm-input" :type="showPw ? 'text' : 'password'" placeholder="••••••••">
            <button @click="showPw = !showPw"
                    style="position:absolute;right:14px;bottom:14px;border:none;background:none;cursor:pointer;color:var(--lm-gray-light)">
              <i class="bi" :class="showPw ? 'bi-eye-slash' : 'bi-eye'" style="font-size:16px"></i>
            </button>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-4" style="font-size:11px">
          <label class="d-flex align-items-center gap-2 cursor-pointer" style="color:var(--lm-gray)">
            <input type="checkbox" checked style="accent-color:var(--lm-gold)"> Ghi nhớ đăng nhập
          </label>
          <a style="color:var(--lm-gold);cursor:pointer">Quên mật khẩu?</a>
        </div>

        <button class="lm-btn-primary w-100 justify-content-center mb-3" @click="$router.push('/profile')">
          <span>Đăng nhập</span>
        </button>

        <div class="d-flex align-items-center gap-3 mb-3">
          <div style="flex:1;height:1px;background:var(--lm-beige-dark)"></div>
          <span style="font-size:10px;color:var(--lm-gray-light);letter-spacing:0.1em;text-transform:uppercase">hoặc</span>
          <div style="flex:1;height:1px;background:var(--lm-beige-dark)"></div>
        </div>

        <button class="w-100 d-flex align-items-center justify-content-center gap-3"
                style="padding:14px;border:1px solid var(--lm-beige-dark);background:transparent;font-size:11px;font-family:var(--lm-font-body);cursor:pointer;transition:all 0.3s;letter-spacing:0.05em"
                @click="$router.push('/profile')"
                @mouseover="e => e.target.style.borderColor = 'var(--lm-gold)'"
                @mouseleave="e => e.target.style.borderColor = 'var(--lm-beige-dark)'">
          <svg width="16" height="16" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
          Tiếp tục với Google
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useToast } from '@/composables/useToast'
const { showToast } = useToast()
const showPw = ref(false)
const features = [
  'Truy cập sớm bộ sưu tập mới',
  'Ưu đãi độc quyền cho thành viên',
  'Tích điểm và đổi quà',
  'Tư vấn phong cách 1-1',
]
</script>
