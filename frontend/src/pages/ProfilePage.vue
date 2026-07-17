<template>
  <div>
    <div class="container z-profile-page">
      <div class="row g-4 g-xl-5">
        <aside class="col-lg-3">
          <div class="z-profile-sidebar">
            <div class="z-profile-avatar" aria-hidden="true">{{ userInitial }}</div>
            <h1 class="z-display z-profile-name">{{ user.hoVaTen || 'Khách hàng' }}</h1>
            <span class="z-profile-member"><i class="bi bi-star-fill"></i> Thành viên Zestia</span>

            <nav class="z-profile-nav" aria-label="Tài khoản">
              <button v-for="item in navItems" :key="item.tab" type="button"
                      class="lm-profile-nav-item" :class="{ active: activeTab === item.tab }"
                      @click="activeTab = item.tab">
                <i class="bi" :class="item.icon"></i>{{ item.label }}
              </button>
              <button type="button" class="lm-profile-nav-item" @click="doLogout">
                <i class="bi bi-box-arrow-right"></i>Đăng xuất
              </button>
            </nav>
          </div>
        </aside>

        <main class="col-lg-9">
          <section v-if="activeTab === 'overview'">
            <div class="z-profile-heading">
              <div>
                <p class="lm-eyebrow mb-2">Tài khoản của tôi</p>
                <h2 class="z-display">Tổng quan <em>mua sắm</em></h2>
              </div>
              <RouterLink to="/my-orders" class="lm-btn-secondary">Xem tất cả đơn</RouterLink>
            </div>

            <div class="z-profile-stats">
              <div v-for="stat in stats" :key="stat.label">
                <strong>{{ stat.value }}</strong><span>{{ stat.label }}</span>
              </div>
            </div>

            <div class="z-section-header">
              <h3>Đơn gần nhất</h3>
              <span>{{ orders.length }} đơn trong tài khoản</span>
            </div>
            <div v-if="loadingOrders" class="z-profile-loading"><span class="spinner-border spinner-border-sm"></span> Đang tải đơn hàng...</div>
            <div v-else-if="!recentOrders.length" class="z-profile-empty">
              <i class="bi bi-bag"></i>
              <strong>Bạn chưa có đơn hàng</strong>
              <p>Khám phá bộ sưu tập để bắt đầu đơn đầu tiên.</p>
              <RouterLink to="/collections" class="lm-btn-primary"><span>Mua sắm ngay</span></RouterLink>
            </div>
            <div v-else class="z-recent-orders">
              <RouterLink v-for="order in recentOrders" :key="order.id" to="/my-orders" class="z-order-row">
                <div>
                  <strong>{{ order.maHoaDon }}</strong>
                  <span>{{ fmtDate(order.ngayTao) }} · {{ order.soSanPham || 0 }} sản phẩm</span>
                </div>
                <div class="z-order-row-total">
                  <strong>{{ fmtMoney(order.tongTien) }}</strong>
                  <span :class="['z-status', statusMap[order.trangThai]?.className || 'pending']">
                    {{ statusMap[order.trangThai]?.label || 'Chờ xử lý' }}
                  </span>
                </div>
                <i class="bi bi-chevron-right"></i>
              </RouterLink>
            </div>
          </section>

          <section v-else-if="activeTab === 'personal'">
            <div class="z-profile-heading">
              <div><p class="lm-eyebrow mb-2">Hồ sơ</p><h2 class="z-display">Thông tin <em>cá nhân</em></h2></div>
            </div>
            <form class="z-profile-panel" @submit.prevent="saveProfile">
              <div class="row g-4">
                <div class="col-md-6">
                  <label class="lm-form-label mb-2" for="profile-name">Họ và tên</label>
                  <input id="profile-name" v-model.trim="profile.hoVaTen" class="lm-input" autocomplete="name" required maxlength="150">
                </div>
                <div class="col-md-6">
                  <label class="lm-form-label mb-2" for="profile-email">Email</label>
                  <input id="profile-email" v-model="profile.email" class="lm-input" type="email" disabled>
                </div>
                <div class="col-md-6">
                  <label class="lm-form-label mb-2" for="profile-phone">Số điện thoại</label>
                  <input id="profile-phone" v-model="profile.soDienThoai" class="lm-input" type="tel" inputmode="numeric"
                         autocomplete="tel" maxlength="10" required @input="normalizeProfilePhone">
                </div>
                <div class="col-md-6">
                  <label class="lm-form-label mb-2" for="profile-gender">Giới tính</label>
                  <select id="profile-gender" v-model="profile.gioiTinh" class="lm-input">
                    <option value="">Chưa chọn</option><option value="0">Nữ</option><option value="1">Nam</option>
                  </select>
                </div>
              </div>
              <button class="lm-btn-primary z-profile-submit" type="submit" :disabled="savingProfile">
                <span>{{ savingProfile ? 'Đang lưu...' : 'Lưu thay đổi' }}</span>
              </button>
            </form>
          </section>

          <section v-else-if="activeTab === 'address'">
            <div class="z-profile-heading">
              <div><p class="lm-eyebrow mb-2">Nhận hàng</p><h2 class="z-display">Địa chỉ <em>giao hàng</em></h2></div>
              <button v-if="!showAddressForm" type="button" class="lm-btn-primary" @click="resetAddressForm">
                <i class="bi bi-plus-lg"></i><span>Thêm địa chỉ</span>
              </button>
            </div>

            <div v-if="addresses.length" class="z-profile-address-list">
              <article v-for="address in addresses" :key="address.id" class="z-profile-address-row">
                <i class="bi bi-geo-alt"></i>
                <div class="flex-grow-1">
                  <strong>{{ formatAddress(address) }}</strong>
                  <span v-if="address.macDinh" class="z-profile-default-badge">Mặc định</span>
                </div>
                <div class="z-address-actions">
                  <button v-if="!address.macDinh" type="button" class="z-icon-btn" title="Đặt làm mặc định" aria-label="Đặt địa chỉ làm mặc định" @click="setDefaultAddress(address)"><i class="bi bi-star"></i></button>
                  <button type="button" class="z-icon-btn" title="Sửa địa chỉ" aria-label="Sửa địa chỉ" @click="startAddressEdit(address)"><i class="bi bi-pencil"></i></button>
                  <button type="button" class="z-icon-btn" title="Xóa địa chỉ" aria-label="Xóa địa chỉ" @click="removeAddress(address)"><i class="bi bi-trash"></i></button>
                </div>
              </article>
            </div>
            <div v-else-if="!showAddressForm" class="z-profile-empty"><i class="bi bi-geo-alt"></i><strong>Chưa có địa chỉ giao hàng</strong></div>

            <form v-if="showAddressForm" class="z-profile-panel z-address-form" @submit.prevent="saveAddress">
              <h3>{{ editingAddressId ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="lm-form-label mb-2">Tỉnh / Thành phố</label>
                  <select v-model="selectedCity" class="lm-input" required @change="onCityChange">
                    <option value="">Chọn Tỉnh/Thành</option><option v-for="city in addressData" :key="city.code" :value="city.code">{{ city.name }}</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="lm-form-label mb-2">Quận / Huyện</label>
                  <select v-model="selectedDistrict" class="lm-input" required :disabled="!selectedCity" @change="onDistrictChange">
                    <option value="">Chọn Quận/Huyện</option><option v-for="district in availableDistricts" :key="district.code" :value="district.code">{{ district.name }}</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="lm-form-label mb-2">Phường / Xã</label>
                  <select v-model="selectedWard" class="lm-input" required :disabled="!selectedDistrict">
                    <option value="">Chọn Phường/Xã</option><option v-for="ward in availableWards" :key="ward.code" :value="ward.code">{{ ward.name }}</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="lm-form-label mb-2">Địa chỉ cụ thể</label>
                  <input v-model.trim="specificAddress" class="lm-input" required maxlength="255" placeholder="Số nhà, tên đường, ngõ...">
                </div>
                <div class="col-12"><label class="z-checkbox"><input v-model="addressIsDefault" type="checkbox">Dùng làm địa chỉ mặc định</label></div>
              </div>
              <div class="z-form-actions">
                <button class="lm-btn-primary" type="submit" :disabled="savingAddress"><span>{{ savingAddress ? 'Đang lưu...' : 'Lưu địa chỉ' }}</span></button>
                <button class="lm-btn-secondary" type="button" @click="cancelAddressForm">Đóng</button>
              </div>
            </form>
          </section>

          <section v-else>
            <div class="z-profile-heading"><div><p class="lm-eyebrow mb-2">Tài khoản</p><h2 class="z-display">Bảo mật <em>đăng nhập</em></h2></div></div>
            <form class="z-profile-panel z-security-form" @submit.prevent="changePassword">
              <div>
                <label class="lm-form-label mb-2" for="customer-current-password">Mật khẩu hiện tại</label>
                <input id="customer-current-password" v-model="passwordForm.current" class="lm-input" type="password" autocomplete="current-password" placeholder="Để trống nếu tài khoản Google chưa có mật khẩu">
              </div>
              <div>
                <label class="lm-form-label mb-2" for="customer-new-password">Mật khẩu mới</label>
                <input id="customer-new-password" v-model="passwordForm.next" class="lm-input" type="password" autocomplete="new-password" required placeholder="Tối thiểu 8 ký tự, gồm chữ và số">
              </div>
              <div>
                <label class="lm-form-label mb-2" for="customer-confirm-password">Xác nhận mật khẩu mới</label>
                <input id="customer-confirm-password" v-model="passwordForm.confirm" class="lm-input" type="password" autocomplete="new-password" required>
              </div>
              <button class="lm-btn-primary z-profile-submit" type="submit" :disabled="savingPassword"><span>{{ savingPassword ? 'Đang cập nhật...' : 'Đổi mật khẩu' }}</span></button>
            </form>
          </section>
        </main>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api, useAuth } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { getUser, isLoggedIn, logout } = useAuth()
const { confirmDialog } = useConfirm()
const { showToast } = useToast()

const activeTab = ref('overview')
const user = ref(getUser() || {})
const orders = ref([])
const loadingOrders = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const savingPassword = ref(false)
const profile = reactive({ hoVaTen: user.value.hoVaTen || '', email: user.value.email || '', soDienThoai: user.value.soDienThoai || '', gioiTinh: user.value.gioiTinh == null ? '' : String(user.value.gioiTinh) })
const passwordForm = reactive({ current: '', next: '', confirm: '' })

const addressData = ref([])
const addresses = ref([])
const editingAddressId = ref(null)
const showAddressForm = ref(false)
const addressIsDefault = ref(false)
const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedWard = ref('')
const specificAddress = ref('')

const navItems = [
  { tab: 'overview', icon: 'bi-grid', label: 'Tổng quan' },
  { tab: 'personal', icon: 'bi-person', label: 'Thông tin cá nhân' },
  { tab: 'address', icon: 'bi-geo-alt', label: 'Địa chỉ giao hàng' },
  { tab: 'security', icon: 'bi-shield-lock', label: 'Bảo mật' }
]
const statusMap = {
  0: { label: 'Chờ xử lý', className: 'pending' }, 1: { label: 'Đã xác nhận', className: 'warning' },
  2: { label: 'Đang chuẩn bị', className: 'info' }, 3: { label: 'Đang giao', className: 'info' },
  4: { label: 'Hoàn thành', className: 'success' }, 5: { label: 'Đã hủy', className: 'danger' },
  6: { label: 'Giao thất bại', className: 'danger' }, 7: { label: 'Thanh toán thất bại', className: 'danger' },
  8: { label: 'Đang đổi / trả', className: 'warning' }, 9: { label: 'Đã hoàn tiền', className: 'danger' }
}

const userInitial = computed(() => (user.value.hoVaTen || user.value.username || 'Z').trim().charAt(0).toUpperCase())
const sortedOrders = computed(() => [...orders.value].sort((a, b) => new Date(b.ngayTao || 0) - new Date(a.ngayTao || 0)))
const recentOrders = computed(() => sortedOrders.value.slice(0, 5))
const stats = computed(() => {
  const completed = orders.value.filter(order => Number(order.trangThai) === 4)
  const inProgress = orders.value.filter(order => [0, 1, 2, 3, 8].includes(Number(order.trangThai)))
  const spent = completed.reduce((sum, order) => sum + Number(order.tongTien || 0) - Number(order.returnRefundAmount || 0), 0)
  return [
    { value: String(orders.value.length), label: 'Tổng đơn' },
    { value: String(inProgress.length), label: 'Đang xử lý' },
    { value: String(completed.length), label: 'Hoàn thành' },
    { value: fmtMoney(spent), label: 'Đã chi tiêu' }
  ]
})
const availableDistricts = computed(() => addressData.value.find(city => city.code === selectedCity.value)?.districts || [])
const availableWards = computed(() => availableDistricts.value.find(district => district.code === selectedDistrict.value)?.wards || [])

onMounted(async () => {
  if (!isLoggedIn()) return router.replace('/login?redirect=/profile')
  await Promise.all([loadCurrentUser(), loadOrders(), loadAddressData()])
  await loadProfileAddresses()
})

async function loadCurrentUser() {
  try {
    const current = await api().me()
    user.value = { ...user.value, ...current }
    profile.hoVaTen = current.hoVaTen || profile.hoVaTen
    profile.email = current.email || profile.email
    profile.soDienThoai = current.soDienThoai || profile.soDienThoai
    if (current.gioiTinh != null) profile.gioiTinh = String(current.gioiTinh)
  } catch (error) {
    showToast(error.error || error.message || 'Không thể tải hồ sơ', 'error')
  }
}

async function loadOrders() {
  loadingOrders.value = true
  try {
    const result = await api().getMyOrders()
    orders.value = Array.isArray(result?.data) ? result.data : Array.isArray(result) ? result : []
  } catch { orders.value = [] }
  finally { loadingOrders.value = false }
}

async function loadAddressData() {
  try {
    const response = await fetch('/data/vietnam-provinces.json')
    if (!response.ok) throw new Error()
    addressData.value = await response.json()
  } catch { showToast('Không thể tải dữ liệu tỉnh thành', 'error') }
}

async function loadProfileAddresses() {
  try { addresses.value = await api().getProfileAddresses() || [] }
  catch { addresses.value = [] }
}

async function saveProfile() {
  if (profile.hoVaTen.trim().length < 2) return showToast('Họ tên cần ít nhất 2 ký tự', 'error')
  if (!/^0[35789]\d{8}$/.test(profile.soDienThoai)) return showToast('Số điện thoại Việt Nam không hợp lệ', 'error')
  savingProfile.value = true
  try {
    const result = await api().updateProfile({ hoVaTen: profile.hoVaTen.trim(), soDienThoai: profile.soDienThoai, gioiTinh: profile.gioiTinh })
    const stored = { ...(getUser() || {}), ...result, gioiTinh: result.gioiTinh === '' ? null : Number(result.gioiTinh) }
    localStorage.setItem('zestia_user', JSON.stringify(stored))
    user.value = stored
    window.dispatchEvent(new Event('zestia-auth-changed'))
    showToast('Đã lưu thông tin cá nhân', 'success')
  } catch (error) { showToast(error.error || error.message || 'Không thể lưu hồ sơ', 'error') }
  finally { savingProfile.value = false }
}

function normalizeProfilePhone(event) { profile.soDienThoai = String(event.target.value || '').replace(/\D/g, '').slice(0, 10) }
function onCityChange() { selectedDistrict.value = ''; selectedWard.value = '' }
function onDistrictChange() { selectedWard.value = '' }
function formatAddress(address) { return [address.duong, address.xaPhuong, address.quanHuyen, address.tinhThanhPho].filter(Boolean).join(', ') }

function resetAddressForm() {
  editingAddressId.value = null; selectedCity.value = ''; selectedDistrict.value = ''; selectedWard.value = ''
  specificAddress.value = ''; addressIsDefault.value = addresses.value.length === 0; showAddressForm.value = true
}

function startAddressEdit(address) {
  const city = addressData.value.find(item => item.name === address.tinhThanhPho)
  const district = city?.districts?.find(item => item.name === address.quanHuyen)
  const ward = district?.wards?.find(item => item.name === address.xaPhuong)
  editingAddressId.value = address.id; selectedCity.value = city?.code || ''; selectedDistrict.value = district?.code || ''
  selectedWard.value = ward?.code || ''; specificAddress.value = address.duong || ''; addressIsDefault.value = Boolean(address.macDinh)
  showAddressForm.value = true
}

function cancelAddressForm() { showAddressForm.value = false; editingAddressId.value = null }

async function saveAddress() {
  if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value || !specificAddress.value.trim()) return showToast('Vui lòng nhập đầy đủ địa chỉ', 'error')
  const payload = {
    tinhThanhPho: addressData.value.find(item => item.code === selectedCity.value)?.name || '',
    quanHuyen: availableDistricts.value.find(item => item.code === selectedDistrict.value)?.name || '',
    xaPhuong: availableWards.value.find(item => item.code === selectedWard.value)?.name || '',
    duong: specificAddress.value.trim(), macDinh: addressIsDefault.value
  }
  savingAddress.value = true
  try {
    if (editingAddressId.value) await api().updateProfileAddressById(editingAddressId.value, payload)
    else await api().createProfileAddress(payload)
    await loadProfileAddresses(); cancelAddressForm(); showToast('Đã lưu địa chỉ', 'success')
  } catch (error) { showToast(error.error || error.message || 'Không thể lưu địa chỉ', 'error') }
  finally { savingAddress.value = false }
}

async function setDefaultAddress(address) {
  try { await api().setDefaultProfileAddress(address.id); await loadProfileAddresses(); showToast('Đã đổi địa chỉ mặc định', 'success') }
  catch (error) { showToast(error.error || error.message || 'Không thể đổi địa chỉ mặc định', 'error') }
}

async function removeAddress(address) {
  if (!await confirmDialog({ title: 'Xóa địa chỉ', message: `Xóa địa chỉ ${formatAddress(address)}?`, confirmText: 'Xóa địa chỉ', variant: 'danger' })) return
  try { await api().deleteProfileAddress(address.id); await loadProfileAddresses(); if (editingAddressId.value === address.id) cancelAddressForm(); showToast('Đã xóa địa chỉ', 'success') }
  catch (error) { showToast(error.error || error.message || 'Không thể xóa địa chỉ', 'error') }
}

async function changePassword() {
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,100}$/.test(passwordForm.next)) return showToast('Mật khẩu mới cần 8-100 ký tự, gồm chữ và số', 'error')
  if (passwordForm.next !== passwordForm.confirm) return showToast('Mật khẩu xác nhận không khớp', 'error')
  if (!await confirmDialog({ title: 'Đổi mật khẩu', message: 'Bạn có chắc muốn đổi mật khẩu đăng nhập?', confirmText: 'Đổi mật khẩu', variant: 'danger' })) return
  savingPassword.value = true
  try {
    const result = await api().changePassword(passwordForm.current, passwordForm.next)
    passwordForm.current = ''; passwordForm.next = ''; passwordForm.confirm = ''
    showToast(result.message || 'Đổi mật khẩu thành công', 'success')
  } catch (error) { showToast(error.error || error.message || 'Không thể đổi mật khẩu', 'error') }
  finally { savingPassword.value = false }
}

function fmtMoney(value) { return Number(value || 0).toLocaleString('vi-VN') + 'đ' }
function fmtDate(value) { return value ? new Date(value).toLocaleDateString('vi-VN') : '' }
function doLogout() { logout(); showToast('Đã đăng xuất', 'info'); router.replace('/login') }
</script>

<style scoped>
.z-profile-page { padding-top: 44px; padding-bottom: 80px; }
.z-profile-sidebar { position: sticky; top: 96px; }
.z-profile-avatar { display: grid; place-items: center; width: 72px; height: 72px; border-radius: 50%; background: var(--z-dark); color: var(--z-white); font: 500 28px var(--z-font-display); }
.z-profile-name { margin: 16px 0 7px; color: var(--z-dark); font-size: 24px; font-weight: 500; }
.z-profile-member { display: inline-flex; align-items: center; gap: 6px; padding: 4px 9px; border-radius: var(--z-radius); background: var(--z-accent-soft); color: var(--z-accent); font-size: 11px; font-weight: 600; }
.z-profile-nav { display: grid; margin-top: 24px; padding-top: 10px; border-top: 1px solid var(--z-gray-border); }
.z-profile-nav .lm-profile-nav-item { width: 100%; border: 0; background: transparent; text-align: left; }
.z-profile-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 28px; }
.z-profile-heading h2 { margin: 0; color: var(--z-dark); font-size: 30px; font-weight: 400; }
.z-profile-heading h2 em { color: var(--z-gray); font-style: italic; }
.z-profile-stats { display: grid; grid-template-columns: repeat(4, 1fr); border-block: 1px solid var(--z-gray-border); margin-bottom: 36px; }
.z-profile-stats > div { display: grid; gap: 4px; padding: 22px 16px; border-right: 1px solid var(--z-gray-border); }
.z-profile-stats > div:last-child { border-right: 0; }
.z-profile-stats strong { color: var(--z-dark); font: 500 24px var(--z-font-display); overflow-wrap: anywhere; }
.z-profile-stats span, .z-section-header span { color: var(--z-gray); font-size: 12px; }
.z-section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.z-section-header h3 { margin: 0; color: var(--z-dark); font-size: 15px; font-weight: 650; }
.z-recent-orders { border-top: 1px solid var(--z-gray-border); }
.z-order-row { display: grid; grid-template-columns: minmax(0, 1fr) auto 18px; align-items: center; gap: 20px; padding: 17px 4px; border-bottom: 1px solid var(--z-gray-border); color: var(--z-dark); text-decoration: none; transition: background-color .18s ease, padding .18s ease; }
.z-order-row:hover { padding-inline: 10px; background: var(--z-bg-alt); }
.z-order-row > div { display: grid; gap: 4px; min-width: 0; }
.z-order-row span { color: var(--z-gray); font-size: 12px; }
.z-order-row-total { justify-items: end; }
.z-order-row-total > strong { font: 500 17px var(--z-font-display); }
.z-profile-loading, .z-profile-empty { min-height: 210px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--z-gray); text-align: center; }
.z-profile-empty > i { color: var(--z-gray-border); font-size: 38px; }
.z-profile-empty p { margin: 0 0 10px; font-size: 13px; }
.z-profile-panel { padding: 24px; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius-lg); background: var(--z-white); }
.z-profile-submit { min-width: 150px; justify-content: center; margin-top: 24px; }
.z-profile-address-list { display: grid; gap: 10px; margin-bottom: 18px; }
.z-profile-address-row { display: flex; align-items: flex-start; gap: 12px; padding: 16px; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius); background: var(--z-white); }
.z-profile-address-row > i { margin-top: 2px; color: var(--z-accent); }
.z-profile-address-row strong { display: block; color: var(--z-dark); font-size: 13px; line-height: 1.6; }
.z-profile-default-badge { display: inline-flex; margin-top: 6px; padding: 3px 7px; border-radius: var(--z-radius); background: #eaf8ee; color: #217a3d; font-size: 10px; font-weight: 650; }
.z-address-actions { display: flex; gap: 3px; }
.z-address-form { margin-top: 18px; }
.z-address-form h3 { margin: 0 0 18px; font-size: 15px; font-weight: 650; }
.z-checkbox { display: inline-flex; align-items: center; gap: 8px; color: var(--z-dark); font-size: 13px; cursor: pointer; }
.z-form-actions { display: flex; gap: 8px; margin-top: 20px; }
.z-security-form { display: grid; gap: 18px; max-width: 620px; }
.z-security-form .z-profile-submit { justify-self: start; }
@media (max-width: 991px) {
  .z-profile-page { padding-top: 28px; }
  .z-profile-sidebar { position: static; }
  .z-profile-nav { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 640px) {
  .z-profile-heading { align-items: flex-start; flex-direction: column; }
  .z-profile-stats { grid-template-columns: repeat(2, 1fr); }
  .z-profile-stats > div:nth-child(2) { border-right: 0; }
  .z-profile-stats > div:nth-child(-n+2) { border-bottom: 1px solid var(--z-gray-border); }
  .z-order-row { grid-template-columns: minmax(0, 1fr) 16px; gap: 10px; }
  .z-order-row-total { grid-column: 1; justify-items: start; }
  .z-order-row > i { grid-column: 2; grid-row: 1 / span 2; }
  .z-profile-panel { padding: 18px; }
  .z-profile-address-row { flex-wrap: wrap; }
  .z-address-actions { width: 100%; justify-content: flex-end; }
  .z-form-actions { flex-direction: column; }
  .z-form-actions > * { width: 100%; justify-content: center; }
}
</style>
