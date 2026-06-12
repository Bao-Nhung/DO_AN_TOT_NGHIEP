<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <!-- Sidebar -->
        <div class="col-lg-3">
          <div style="position:sticky;top:100px">
            <div class="position-relative mb-3" style="width:80px;height:80px">
              <div style="width:100%;height:100%;border-radius:50%;background:linear-gradient(135deg,var(--z-accent-soft),var(--z-accent));display:flex;align-items:center;justify-content:center;font-family:var(--z-font-display);font-size:28px;font-weight:500;color:var(--z-white)">
                {{ userInitial }}
              </div>
            </div>
            <div class="z-display mb-1" style="font-size:24px;font-weight:500;color:var(--z-dark)">{{ user.hoVaTen || 'Khách hàng' }}</div>
            <div class="d-inline-flex align-items-center gap-2 mb-4"
                 style="padding:4px 12px;background:var(--z-accent-soft);font-size:11px;font-weight:600;color:var(--z-accent);border-radius:20px">
              <i class="bi bi-star-fill" style="font-size:10px"></i> {{ user.role === 'Admin' ? 'Admin' : 'Thành viên' }}
            </div>

            <nav style="border-top:1px solid var(--z-gray-border)">
              <div v-for="item in navItems" :key="item.tab"
                   class="lm-profile-nav-item" :class="{ active: activeTab === item.tab }"
                   @click="activeTab = item.tab">
                <i class="bi" :class="item.icon"></i>
                {{ item.label }}
              </div>
              <div class="lm-profile-nav-item" @click="doLogout">
                <i class="bi bi-box-arrow-right"></i>
                Đăng xuất
              </div>
            </nav>
          </div>
        </div>

        <!-- Content -->
        <div class="col-lg-9 pt-2">

          <!-- Orders Tab -->
          <div v-if="activeTab === 'orders'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Đơn hàng <em style="font-style:italic;color:var(--z-gray)">của tôi</em></h2>
            <div class="row g-3 mb-5">
              <div v-for="stat in stats" :key="stat.label" class="col-6 col-md-3">
                <div style="padding:20px;background:var(--z-white);border:1px solid var(--z-gray-border);text-align:center;border-radius:var(--z-radius-lg)">
                  <div class="z-display" style="font-size:28px;font-weight:500;color:var(--z-dark)">{{ stat.num }}</div>
                  <div style="font-size:12px;font-weight:500;color:var(--z-gray);margin-top:4px">{{ stat.label }}</div>
                </div>
              </div>
            </div>
            <div v-if="orders.length === 0" class="text-center py-5">
              <i class="bi bi-bag mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
              <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Chưa có đơn hàng</h3>
              <p style="color:var(--z-gray);font-size:14px">Hãy khám phá bộ sưu tập và đặt đơn hàng đầu tiên.</p>
              <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Mua sắm ngay</span></RouterLink>
            </div>
            <div v-else class="d-flex flex-column gap-3">
              <div v-for="order in orders" :key="order.id"
                   style="border:1px solid var(--z-gray-border);padding:20px;background:var(--z-white);border-radius:var(--z-radius-lg)">
                <div class="d-flex justify-content-between align-items-start mb-3 pb-3" style="border-bottom:1px solid var(--z-gray-border)">
                  <div>
                    <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ order.maHoaDon }}</div>
                    <div style="font-size:13px;color:var(--z-gray)">{{ fmtDate(order.ngayTao) }} · {{ order.hinhThucThanhToan }}</div>
                  </div>
                  <span :class="'lm-status-' + (statusMap[order.trangThai]?.key || 'pending')">
                    {{ statusMap[order.trangThai]?.label || 'Chờ xử lý' }}
                  </span>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                  <div class="z-display" style="font-size:20px;font-weight:500;color:var(--z-dark)">{{ fmtMoney(order.tongTien) }}</div>
                  <RouterLink to="/collections" class="lm-btn-primary" style="padding:10px 20px;font-size:12px"><span>Mua lại</span></RouterLink>
                </div>
              </div>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-if="activeTab === 'settings'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Thông tin <em style="font-style:italic;color:var(--z-gray)">cá nhân</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-md-6"><label class="lm-form-label mb-2">Họ và tên</label><input class="lm-input" v-model="profile.hoVaTen"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Email</label><input class="lm-input" type="email" v-model="profile.email" disabled></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Số điện thoại</label><input class="lm-input" type="tel" v-model="profile.soDienThoai"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Giới tính</label>
                <select class="lm-input" v-model="profile.gioiTinh"><option value="">Chọn</option><option>Nữ</option><option>Nam</option></select>
              </div>
            </div>
            <button class="lm-btn-primary" @click="saveProfile" :disabled="saving"><span>{{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}</span></button>
          </div>

          <!-- Address Tab -->
          <div v-if="activeTab === 'address'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Địa chỉ <em style="font-style:italic;color:var(--z-gray)">giao hàng</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-12"><label class="lm-form-label mb-2">Địa chỉ</label><input class="lm-input" v-model="address.street"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Quận / Huyện</label><input class="lm-input" v-model="address.district"></div>
              <div class="col-md-6"><label class="lm-form-label mb-2">Tỉnh / Thành phố</label>
                <select class="lm-input" v-model="address.city"><option>Hà Nội</option><option>TP. Hồ Chí Minh</option><option>Đà Nẵng</option></select>
              </div>
            </div>
            <button class="lm-btn-primary" @click="showToast('Đã lưu địa chỉ!')"><span>Lưu địa chỉ</span></button>
          </div>

        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'

const router = useRouter()
const { showToast } = useToast()
const { getUser, isLoggedIn, logout } = useAuth()

const activeTab = ref('orders')

const user = ref(getUser() || {})
const userInitial = computed(() => {
  const name = user.value.hoVaTen || user.value.username || ''
  return name.charAt(0).toUpperCase() || 'U'
})

const gioiTinhMap = { 0: 'Nữ', 1: 'Nam' }
const gioiTinhReverse = { 'Nữ': '0', 'Nam': '1' }

const profile = ref({
  hoVaTen: user.value.hoVaTen || '',
  email: user.value.email || '',
  soDienThoai: user.value.soDienThoai || '',
  gioiTinh: gioiTinhMap[user.value.gioiTinh] || '',
})

const address = ref({ street: '', district: '', city: 'Hà Nội' })

const orders = ref([])

const statusMap = {
  0: { key: 'pending', label: 'Chờ xử lý' },
  1: { key: 'paid', label: 'Đã thanh toán' },
  2: { key: 'shipping', label: 'Đang giao' },
  3: { key: 'delivered', label: 'Đã giao' },
  4: { key: 'cancelled', label: 'Đã huỷ' },
  5: { key: 'failed', label: 'Thất bại' },
}

function fmtMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

function fmtDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  return dt.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const stats = computed(() => {
  const total = orders.value.length
  const spent = orders.value
    .filter(o => o.trangThai === 1 || o.trangThai === 3)
    .reduce((s, o) => s + Number(o.tongTien || 0), 0)
  return [
    { num: String(total), label: 'Tổng đơn' },
    { num: fmtMoney(spent), label: 'Đã chi tiêu' },
    { num: String(Math.floor(spent / 10000)), label: 'Điểm tích luỹ' },
    { num: total >= 10 ? 'Vàng' : total >= 5 ? 'Bạc' : 'Mới', label: 'Hạng thành viên' },
  ]
})

onMounted(async () => {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  try {
    const data = await api().getMyOrders()
    orders.value = data || []
  } catch (e) {
    console.error('Failed to load orders:', e)
  }
})

const saving = ref(false)

async function saveProfile() {
  saving.value = true
  try {
    const data = {
      hoVaTen: profile.value.hoVaTen,
      soDienThoai: profile.value.soDienThoai,
      gioiTinh: gioiTinhReverse[profile.value.gioiTinh] || '',
    }
    await api().updateProfile(data)
    const stored = getUser()
    if (stored) {
      stored.hoVaTen = profile.value.hoVaTen
      stored.soDienThoai = profile.value.soDienThoai
      stored.gioiTinh = gioiTinhReverse[profile.value.gioiTinh] != null
        ? Number(gioiTinhReverse[profile.value.gioiTinh]) : null
      localStorage.setItem('zestia_user', JSON.stringify(stored))
      user.value = stored
    }
    showToast('Đã lưu thông tin!')
  } catch (e) {
    showToast(e.error || 'Lỗi khi lưu thông tin')
  } finally {
    saving.value = false
  }
}

function doLogout() {
  logout()
  showToast('Đã đăng xuất')
  router.push('/login')
}

const navItems = [
  { tab: 'orders',   icon: 'bi-file-text',   label: 'Đơn hàng của tôi' },
  { tab: 'settings', icon: 'bi-person',       label: 'Thông tin cá nhân' },
  { tab: 'address',  icon: 'bi-geo-alt',      label: 'Địa chỉ giao hàng' },
]
</script>
