<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">
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

        <div class="col-lg-9 pt-2">
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
            <div v-if="sortedOrders.length === 0" class="text-center py-5">
              <i class="bi bi-bag mb-3" style="font-size:48px;color:var(--z-gray-border)"></i>
              <h3 class="z-display" style="font-weight:400;color:var(--z-gray)">Chưa có đơn hàng</h3>
              <p style="color:var(--z-gray);font-size:14px">Hãy khám phá bộ sưu tập và đặt đơn hàng đầu tiên.</p>
              <RouterLink to="/collections" class="lm-btn-primary mt-3"><span>Mua sắm ngay</span></RouterLink>
            </div>
            <div v-else class="d-flex flex-column gap-3">
              <div v-for="order in sortedOrders" :key="order.id"
                   class="z-order-card"
                   @click="openOrderDetail(order)">
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
                  <div>
                    <div class="z-display" style="font-size:20px;font-weight:500;color:var(--z-dark)">{{ fmtMoney(order.tongTien) }}</div>
                    <div style="font-size:12px;color:var(--z-gray-light);margin-top:2px">{{ order.soSanPham || 0 }} sản phẩm · Nhấn xem chi tiết</div>
                  </div>
                  <i class="bi bi-chevron-right" style="color:var(--z-gray-light);font-size:18px"></i>
                </div>
              </div>
            </div>
          </div>

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

          <div v-if="activeTab === 'address'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Địa chỉ <em style="font-style:italic;color:var(--z-gray)">giao hàng</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-md-4">
                <label class="lm-form-label mb-2">Tỉnh / Thành phố *</label>
                <select v-model="selectedCity" class="lm-input" @change="onCityChange">
                  <option value="">Chọn Tỉnh/Thành</option>
                  <option v-for="c in addressData" :key="c.code" :value="c.code">{{ c.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="lm-form-label mb-2">Quận / Huyện *</label>
                <select v-model="selectedDistrict" class="lm-input" :disabled="!selectedCity" @change="onDistrictChange">
                  <option value="">Chọn Quận/Huyện</option>
                  <option v-for="d in availableDistricts" :key="d.code" :value="d.code">{{ d.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="lm-form-label mb-2">Phường / Xã *</label>
                <select v-model="selectedWard" class="lm-input" :disabled="!selectedDistrict">
                  <option value="">Chọn Phường/Xã</option>
                  <option v-for="w in availableWards" :key="w.code" :value="w.code">{{ w.name }}</option>
                </select>
              </div>
              <div class="col-12">
                <label class="lm-form-label mb-2">Địa chỉ cụ thể *</label>
                <input class="lm-input" v-model="specificAddress" placeholder="Số nhà, tên đường, ngõ ngách...">
              </div>
            </div>
            <button class="lm-btn-primary" @click="saveAddress" :disabled="savingAddress">
              <span>{{ savingAddress ? 'Đang lưu...' : 'Lưu địa chỉ' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="z-modal-overlay" @click.self="showDetail = false" style="z-index: 1050; backdrop-filter: blur(2px);">
      <div class="z-modal" style="max-width:800px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết đơn hàng</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ detailOrder?.maHoaDon }}</div>
          </div>
          <button class="z-icon-btn" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
          <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Đang tải...</p>
        </div>

        <div v-else-if="detailOrder">
          
          <div class="mb-4">
            <OrderTrackingCard :order="detailOrder" />
          </div>

          <div class="d-flex align-items-center gap-2 mb-4 pb-3" style="border-bottom:1px solid var(--z-gray-border);overflow-x:auto">
            <template v-if="detailOrder.trangThai === 5 || detailOrder.trangThai === 7">
               <div class="z-step active">
                  <div class="z-step-dot" style="background: var(--z-danger);"></div>
                  <div class="z-step-label" style="color: var(--z-danger); font-weight: 600;">Đã huỷ</div>
               </div>
            </template>
            <template v-else>
                <div v-for="(step, i) in statusSteps" :key="i"
                     class="z-step" :class="{ active: detailOrder.trangThai >= i && detailOrder.trangThai !== 6, current: detailOrder.trangThai === i, failed: i === 4 && detailOrder.trangThai === 6 }">
                  <div class="z-step-dot" :style="i === 4 && detailOrder.trangThai === 6 ? 'background: var(--z-danger)' : ''"></div>
                  <div class="z-step-label" :style="i === 4 && detailOrder.trangThai === 6 ? 'color: var(--z-danger); font-weight: 600;' : ''">
                      {{ i === 4 && detailOrder.trangThai === 6 ? 'Giao thất bại' : step }}
                  </div>
                  <div v-if="i < statusSteps.length - 1" class="z-step-line" :class="{ filled: detailOrder.trangThai > i && detailOrder.trangThai !== 6 }"></div>
                </div>
            </template>
          </div>

          <div class="row mb-4 mt-2">
            <div class="col-md-6">
              <h5 style="font-size:14px;font-weight:600;color:var(--z-dark);margin-bottom:12px;">Thông tin nhận hàng</h5>
              <div style="font-size:13px;color:var(--z-gray); margin-bottom: 4px;"><strong>Người nhận:</strong> {{ detailOrder.khachHang || 'Khách hàng' }} <span v-if="detailOrder.soDienThoai">- {{ detailOrder.soDienThoai }}</span></div>
              <div style="font-size:13px;color:var(--z-gray); margin-bottom: 4px;"><strong>Hình thức:</strong> {{ detailOrder.hinhThucNhanHang === 0 ? 'Nhận tại cửa hàng' : 'Giao hàng tận nơi' }}</div>
              <div v-if="detailOrder.diaChiGiaoHang && detailOrder.hinhThucNhanHang !== 0" style="font-size:13px;color:var(--z-gray);"><strong>Địa chỉ:</strong> {{ detailOrder.diaChiGiaoHang }}</div>
            </div>
            <div class="col-md-6 mt-3 mt-md-0">
               <h5 style="font-size:14px;font-weight:600;color:var(--z-dark);margin-bottom:12px;">Thông tin thanh toán</h5>
               <div style="font-size:13px;color:var(--z-gray); margin-bottom: 4px;"><strong>Ngày đặt:</strong> {{ fmtDate(detailOrder.ngayTao) }}</div>
               <div style="font-size:13px;color:var(--z-gray); margin-bottom: 4px;"><strong>Phương thức:</strong> {{ detailOrder.hinhThucThanhToan }}</div>
               <div style="font-size:13px;color:var(--z-gray);">
                 <strong>Trạng thái: </strong> 
                 <span v-if="detailOrder.phuongThucThanhToanOnline === 'FAILED' || (detailOrder.trangThai === 5 && (detailOrder.hinhThucThanhToan === 'MOMO' || detailOrder.hinhThucThanhToan === 'ZALOPAY') && !detailOrder.daThanhToan)"
                       class="text-danger" style="font-weight: 600;">
                    Thanh toán thất bại
                 </span>
                 <span v-else :class="detailOrder.daThanhToan ? 'text-success' : 'text-warning'" style="font-weight: 500;">
                    {{ detailOrder.daThanhToan ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                 </span>
               </div>
            </div>
          </div>

          <h5 style="font-size:14px;font-weight:600;color:var(--z-dark);margin-bottom:12px;">Danh sách sản phẩm ({{ detailOrder.chiTiets?.length || 0 }})</h5>
          <div class="d-flex flex-column gap-0 mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden; max-height: 250px; overflow-y: auto;">
            <div v-for="item in detailOrder.chiTiets" :key="item.id"
                 class="d-flex align-items-center gap-3" style="padding:12px 14px;border-bottom:1px solid var(--z-gray-border)">
              <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:18px;color:var(--z-gray-light)">
                  <i class="bi bi-image"></i>
                </div>
              </div>
              <div class="flex-grow-1">
                <div style="font-size:13px;font-weight:500;color:var(--z-dark)">{{ item.tenSanPham || item.tenVay || 'Sản phẩm' }}</div>
                <div style="font-size:12px;color:var(--z-gray)">
                  <span v-if="item.maSanPham">Mã SP: {{ item.maSanPham }}<br></span>
                  <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                    <span v-if="item.maHex" :style="{ width:'8px', height:'8px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                    {{ item.mauSac }}
                  </span>
                  <span v-if="item.mauSac && item.kichThuoc"> · </span>
                  <span v-if="item.kichThuoc"> Size: {{ item.kichThuoc }}</span>
                  <span v-if="item.phanTramGiam > 0" style="color:var(--z-danger); font-weight: 500;"> | Giảm: {{ item.phanTramGiam }}%</span>
                  <span> · SL: {{ item.soLuong }}</span>
                </div>
              </div>
              <div style="font-size:13px;font-weight:600;color:var(--z-dark);white-space:nowrap">{{ fmtMoney(item.donGia) }}</div>
            </div>
          </div>

          <div v-if="detailOrder.ghiChu" class="mb-3 p-3" style="background:var(--z-bg-alt);border-radius:var(--z-radius);font-size:13px;color:var(--z-gray)">
            <strong>Ghi chú đơn hàng:</strong> {{ detailOrder.ghiChu }}
          </div>

          <div class="mb-3 p-3 bg-light rounded" style="font-size:13px; color:var(--z-gray)">
             <div class="d-flex justify-content-between mb-2">
                <span>Tổng tiền hàng:</span>
                <span style="font-weight: 500; color: var(--z-dark);">{{ fmtMoney(detailOrder.tongTien + (detailOrder.giamGiaKhuyenMai || 0) - (detailOrder.phiVanChuyen || 0)) }}</span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Phí vận chuyển:</span>
                <span style="font-weight: 500; color: var(--z-dark);">
                  {{ detailOrder.phiVanChuyen > 0 ? '+ ' + fmtMoney(detailOrder.phiVanChuyen) : '0đ (Miễn phí)' }}
                </span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Giảm giá / Khuyến mãi:</span>
                <span class="text-danger font-weight-bold">
                  {{ detailOrder.giamGiaKhuyenMai > 0 ? '- ' + fmtMoney(detailOrder.giamGiaKhuyenMai) : '0đ' }}
                </span>
             </div>

             <div class="d-flex justify-content-between align-items-center mb-2" v-if="detailOrder.khuyenMai">
                <div style="font-size:13px;color:var(--z-gray)">Chương trình Khuyến mãi</div>
                <div style="font-size:13px;font-weight:500;color:var(--z-success)">{{ detailOrder.khuyenMai }}</div>
             </div>
             <div class="d-flex justify-content-between align-items-center mb-2" v-if="detailOrder.giamGia">
                <div style="font-size:13px;color:var(--z-gray)">Voucher</div>
                <div style="font-size:13px;font-weight:500;color:var(--z-success)">{{ detailOrder.giamGia }}</div>
             </div>
             
             <div class="d-flex justify-content-between align-items-center pt-2 mt-2" style="border-top:1px dashed var(--z-gray-border)">
                <div style="font-size:14px;font-weight:600; color: var(--z-dark);">Tổng cộng</div>
                <div class="z-display" style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ fmtMoney(detailOrder.tongTien) }}</div>
             </div>
          </div>

          <div class="d-flex justify-content-end mt-3" v-if="detailOrder.trangThai === 0">
            <button class="lm-btn-secondary" style="color:var(--z-danger);border-color:var(--z-danger)" @click="showCancelModal = true">
              <i class="bi bi-x-circle me-1"></i> Xin huỷ đơn
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCancelModal" class="z-modal-overlay" @click.self="showCancelModal = false" style="z-index: 1060; background: rgba(0,0,0,0.6);">
      <div class="z-modal" style="max-width:500px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Lý do huỷ đơn hàng</h3>
          <button class="z-icon-btn" @click="showCancelModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <p style="font-size:14px;color:var(--z-gray);margin-bottom:20px">Vui lòng cho Zestia biết lý do bạn muốn huỷ đơn hàng này nhé:</p>
        
        <div class="d-flex flex-column gap-3 mb-4">
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Thay đổi ý định mua">
            <span style="font-size:14px;color:var(--z-dark)">Thay đổi ý định mua</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Tìm thấy giá rẻ hơn ở nơi khác">
            <span style="font-size:14px;color:var(--z-dark)">Tìm thấy giá rẻ hơn ở nơi khác</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Quên áp dụng mã giảm giá">
            <span style="font-size:14px;color:var(--z-dark)">Quên áp dụng mã giảm giá</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Khác">
            <span style="font-size:14px;color:var(--z-dark)">Khác...</span>
          </label>
          
          <textarea v-if="cancelReason === 'Khác'" v-model="otherCancelReason" 
                    class="lm-input mt-2" rows="3" placeholder="Vui lòng nhập lý do cụ thể..."></textarea>
        </div>

        <div class="d-flex gap-3">
          <button class="lm-btn-secondary flex-fill" style="height:44px;" @click="showCancelModal = false">Đóng</button>
          <button class="lm-btn-primary flex-fill" style="height:44px; background:var(--z-danger); border-color:var(--z-danger)" @click="submitCancelOrder">Xác nhận huỷ</button>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import OrderTrackingCard from '@/components/OrderTrackingCard.vue'
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

const addressData = ref([])
const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedWard = ref('')
const specificAddress = ref('')
const savingAddress = ref(false)

const availableDistricts = computed(() => {
  const city = addressData.value.find(c => c.code === selectedCity.value)
  return city ? city.districts : []
})

const availableWards = computed(() => {
  const district = availableDistricts.value.find(d => d.code === selectedDistrict.value)
  return district ? district.wards : []
})

function onCityChange() {
  selectedDistrict.value = ''
  selectedWard.value = ''
}

function onDistrictChange() {
  selectedWard.value = ''
}

async function loadProfileAddress() {
  try {
    const addr = await api().getProfileAddress()
    if (addr && addr.tinhThanhPho) {
      const city = addressData.value.find(c => c.name === addr.tinhThanhPho)
      if (city) {
        selectedCity.value = city.code
        const dist = city.districts.find(d => d.name === addr.quanHuyen)
        if (dist) {
          selectedDistrict.value = dist.code
          const ward = dist.wards.find(w => w.name === addr.xaPhuong)
          if (ward) {
            selectedWard.value = ward.code
          }
        }
      }
      specificAddress.value = addr.duong || ''
    }
  } catch (e) {
    console.error("Lỗi khi tải địa chỉ:", e)
  }
}

async function saveAddress() {
  if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value || !specificAddress.value.trim()) {
    showToast('Vui lòng nhập đầy đủ thông tin địa chỉ giao hàng!', 'warning')
    return
  }

  savingAddress.value = true
  try {
    const cityName = addressData.value.find(c => c.code === selectedCity.value)?.name || ''
    const districtName = availableDistricts.value.find(d => d.code === selectedDistrict.value)?.name || ''
    const wardName = availableWards.value.find(w => w.code === selectedWard.value)?.name || ''
    
    await api().updateProfileAddress({
      tinhThanhPho: cityName,
      quanHuyen: districtName,
      xaPhuong: wardName,
      duong: specificAddress.value.trim()
    })
    showToast('Đã lưu địa chỉ thành công!', 'success')
  } catch (e) {
    showToast(e.error || 'Lỗi khi lưu địa chỉ', 'error')
  } finally {
    savingAddress.value = false
  }
}

const orders = ref([])
const showDetail = ref(false)
const detailOrder = ref(null)
const loadingDetail = ref(false)

// Các biến phục vụ Hủy Đơn
const showCancelModal = ref(false)
const cancelReason = ref('')
const otherCancelReason = ref('')

let pollingInterval = null

// Status Map nâng cấp (Thêm Chuẩn bị (2), dời Hủy (5) và Thất bại (6))
const statusMap = {
  0: { key: 'pending', label: 'Chờ xử lý' },
  1: { key: 'warning', label: 'Đã xác nhận' },
  2: { key: 'info', label: 'Đang chuẩn bị' },
  3: { key: 'primary', label: 'Đang giao' },
  4: { key: 'success', label: 'Hoàn thành' },
  5: { key: 'danger', label: 'Đã huỷ' },
  6: { key: 'danger', label: 'Giao thất bại' },
}
const statusSteps = ['Chờ xử lý', 'Xác nhận', 'Chuẩn bị', 'Đang giao', 'Hoàn thành']

statusMap[7] = { key: 'danger', label: 'Thanh toán thất bại' }

const sortedOrders = computed(() => {
  return [...orders.value].sort((a, b) => {
    const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    return db - da
  })
})

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
    .filter(o => o.trangThai === 4 || o.trangThai === 3)
    .reduce((s, o) => s + Number(o.tongTien || 0), 0)
  return [
    { num: String(total), label: 'Tổng đơn' },
    { num: fmtMoney(spent), label: 'Đã chi tiêu' },
    { num: String(Math.floor(spent / 10000)), label: 'Điểm tích luỹ' },
    { num: total >= 10 ? 'Vàng' : total >= 5 ? 'Bạc' : 'Mới', label: 'Hạng thành viên' },
  ]
})

async function openOrderDetail(order) {
  showDetail.value = true
  loadingDetail.value = true
  try {
    const [orderRes, trackingRes] = await Promise.all([
      api().getHoaDonById(order.id).catch(() => null),
      api().getOrderTracking(order.id).catch(() => null)
    ])
    const orderData = orderRes?.data || orderRes || order
    const trackingData = trackingRes?.data || trackingRes
    
    detailOrder.value = { 
      ...orderData, 
      trackingHistory: trackingData?.trackingHistory || [] 
    }
  } catch (e) {
    detailOrder.value = order
  } finally {
    loadingDetail.value = false
  }
}

// Logic gửi Hủy đơn có lý do
async function submitCancelOrder() {
  if (!cancelReason.value) {
    showToast('Vui lòng chọn lý do hủy!', 'warning')
    return
  }
  
  const finalReason = cancelReason.value === 'Khác' ? otherCancelReason.value : cancelReason.value
  
  if (cancelReason.value === 'Khác' && !finalReason.trim()) {
    showToast('Vui lòng nhập lý do cụ thể!', 'warning')
    return
  }

  try {
    await api().cancelMyOrder(detailOrder.value.id, finalReason)
    showToast('Đã huỷ đơn hàng thành công!', 'success')
    detailOrder.value.trangThai = 5 // 5 = Đã Hủy
    
    const idx = orders.value.findIndex(o => o.id === detailOrder.value.id)
    if (idx !== -1) orders.value[idx].trangThai = 5

    // Tắt modal sau khi thành công
    showCancelModal.value = false
    cancelReason.value = ''
    otherCancelReason.value = ''
  } catch (e) {
    showToast(e.error || 'Lỗi khi huỷ đơn hàng', 'error')
  }
}

async function loadOrdersSilent() {
  try {
    const res = await api().getMyOrders()
    const data = res?.data || res
    if (Array.isArray(data)) orders.value = data
  } catch (e) { }
}

async function loadOrders() {
  try {
    const res = await api().getMyOrders()
    const data = res?.data || res
    orders.value = Array.isArray(data) ? data : []
  } catch (e) {
    orders.value = []
  }
}

onMounted(async () => {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  await loadOrders()
  
  try {
    const res = await fetch('https://provinces.open-api.vn/api/?depth=3')
    addressData.value = await res.json()
    await loadProfileAddress()
  } catch(e) {
    console.error("Lỗi khi tải danh sách tỉnh thành/địa chỉ mặc định", e)
  }

  pollingInterval = setInterval(async () => {
      if (!showDetail.value && !showCancelModal.value) {
          await loadOrdersSilent()
      }
  }, 10000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
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
      stored.gioiTinh = gioiTinhReverse[profile.value.gioiTinh] != null ? Number(gioiTinhReverse[profile.value.gioiTinh]) : null
      localStorage.setItem('zestia_user', JSON.stringify(stored))
      user.value = stored
    }
    showToast('Đã lưu thông tin!', 'success')
  } catch (e) {
    showToast(e.error || 'Lỗi khi lưu thông tin', 'error')
  } finally {
    saving.value = false
  }
}

function doLogout() {
  logout()
  showToast('Đã đăng xuất', 'info')
  router.push('/login')
}

const navItems = [
  { tab: 'orders',   icon: 'bi-file-text',   label: 'Đơn hàng của tôi' },
  { tab: 'settings', icon: 'bi-person',       label: 'Thông tin cá nhân' },
  { tab: 'address',  icon: 'bi-geo-alt',      label: 'Địa chỉ giao hàng' },
]
</script>

<style scoped>
.z-order-card {
  border: 1px solid var(--z-gray-border);
  padding: 20px;
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  cursor: pointer;
  transition: all 0.2s;
}
.z-order-card:hover {
  border-color: var(--z-accent);
  box-shadow: 0 4px 16px rgba(212,86,78,0.08);
}
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }

/* Bảng màu Trạng thái */
.lm-status-pending { color: #b45309; background: #fef3cd; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.lm-status-warning { color: #d97706; background: #fef08a; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.lm-status-info { color: #0369a1; background: #e0f2fe; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.lm-status-primary { color: #4338ca; background: #e0e7ff; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.lm-status-success { color: #15803d; background: #dcfce7; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.lm-status-danger { color: #b91c1c; background: #fee2e2; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

.lm-form-label { font-size: 13px; font-weight: 500; color: var(--z-dark); }

.z-step { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.z-step-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--z-gray-border); transition: all 0.2s; }
.z-step.active .z-step-dot { background: var(--z-accent); }
.z-step.current .z-step-dot { box-shadow: 0 0 0 3px var(--z-accent-soft); }
.z-step-label { font-size: 12px; font-weight: 500; color: var(--z-gray-light); white-space: nowrap; }
.z-step.active .z-step-label { color: var(--z-dark); }
.z-step-line { width: 32px; height: 2px; background: var(--z-gray-border); margin: 0 4px; }
.z-step-line.filled { background: var(--z-accent); }
</style>
