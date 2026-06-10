<template>
  <div>
    <div class="container" style="padding-top:120px;padding-bottom:100px">
      <div class="row g-5">

        <!-- Sidebar -->
        <div class="col-lg-3">
          <div style="position:sticky;top:120px">
            <!-- Avatar -->
            <div class="position-relative mb-3" style="width:100px;height:100px">
              <div style="width:100%;height:100%;border-radius:50%;background:linear-gradient(135deg,var(--lm-beige),var(--lm-beige-dark));display:flex;align-items:center;justify-content:center;font-family:var(--lm-font-display);font-size:36px;font-weight:400;color:var(--lm-gray);border:2px solid var(--lm-beige-dark)">N</div>
              <div style="position:absolute;inset:-4px;border-radius:50%;border:1px solid var(--lm-gold);opacity:0.5;pointer-events:none"></div>
            </div>
            <div class="lm-display mb-1" style="font-size:28px;font-weight:400;color:var(--lm-black)">Nguyễn Lan Anh</div>
            <div class="d-inline-flex align-items-center gap-2 mb-4"
                 style="padding:4px 12px;background:var(--lm-gold-light);font-size:8px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;color:var(--lm-gold)">
              <i class="bi bi-star-fill" style="font-size:8px"></i> VIP Gold
            </div>

            <nav style="border-top:1px solid var(--lm-beige)">
              <div v-for="item in navItems" :key="item.tab"
                   class="lm-profile-nav-item" :class="{ active: activeTab === item.tab }"
                   @click="activeTab = item.tab">
                <i class="bi" :class="item.icon"></i>
                {{ item.label }}
                <span v-if="item.count" class="lm-nav-count">{{ item.count }}</span>
              </div>
              <div class="lm-profile-nav-item" @click="$router.push('/login')">
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
            <h2 class="lm-display mb-4" style="font-size:32px;font-weight:300;letter-spacing:-0.01em">Đơn hàng <em style="font-style:italic;color:var(--lm-gray)">của tôi</em></h2>
            <div class="row g-3 mb-5">
              <div v-for="(stat, i) in stats" :key="stat.label" class="col-6 col-md-3 lm-reveal" :style="{ transitionDelay: i*0.1+'s' }">
                <div style="padding:24px 20px;background:var(--lm-white);border:1px solid var(--lm-beige);text-align:center">
                  <div class="lm-display" style="font-size:36px;font-weight:300;color:var(--lm-black)">{{ stat.num }}</div>
                  <div style="font-size:9px;font-weight:500;letter-spacing:0.2em;text-transform:uppercase;color:var(--lm-gray);margin-top:6px">{{ stat.label }}</div>
                </div>
              </div>
            </div>
            <div class="d-flex flex-column gap-3">
              <div v-for="order in orders" :key="order.id" class="lm-reveal"
                   style="border:1px solid var(--lm-beige);padding:24px;background:var(--lm-white)">
                <div class="d-flex justify-content-between align-items-start mb-3 pb-3" style="border-bottom:1px solid var(--lm-beige)">
                  <div>
                    <div style="font-size:10px;font-weight:500;letter-spacing:0.2em;text-transform:uppercase;color:var(--lm-black)">{{ order.id }}</div>
                    <div style="font-size:11px;color:var(--lm-gray)">{{ order.date }}</div>
                  </div>
                  <span :class="'lm-status-' + order.status.key">{{ order.status.label }}</span>
                </div>
                <div class="d-flex gap-2 mb-3">
                  <div v-for="item in order.items" :key="item" style="width:60px;height:75px;overflow:hidden">
                    <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                         :style="{ background: item.bg, fontFamily:'var(--lm-font-display)', fontSize:'18px', color:'rgba(255,255,255,0.3)', fontStyle:'italic' }">
                      {{ item.letter }}
                    </div>
                  </div>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                  <div class="lm-display" style="font-size:22px;font-weight:400;color:var(--lm-black)">{{ order.total }}</div>
                  <button class="lm-btn-primary" style="padding:10px 24px;font-size:9px"><span>Mua lại</span></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-if="activeTab === 'settings'">
            <h2 class="lm-display mb-4" style="font-size:32px;font-weight:300">Thông tin <em style="font-style:italic;color:var(--lm-gray)">cá nhân</em></h2>
            <div class="row g-4 mb-4">
              <div class="col-6"><label class="lm-form-label mb-2">Họ</label><input class="lm-input" value="Nguyễn"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Tên</label><input class="lm-input" value="Lan Anh"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Email</label><input class="lm-input" type="email" value="lananh@email.com"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Số điện thoại</label><input class="lm-input" type="tel" value="0901 234 567"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Ngày sinh</label><input class="lm-input" type="date" value="1995-03-15"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Giới tính</label><select class="lm-input"><option>Nữ</option><option>Nam</option></select></div>
              <div class="col-12"><label class="lm-form-label mb-2">Tiểu sử</label><textarea class="lm-input" rows="3">Yêu thích thời trang tối giản và các chất liệu tự nhiên...</textarea></div>
            </div>
            <button class="lm-btn-primary" @click="showToast('Đã lưu thông tin!')"><span>Lưu thay đổi</span></button>
          </div>

          <!-- Address Tab -->
          <div v-if="activeTab === 'address'">
            <h2 class="lm-display mb-4" style="font-size:32px;font-weight:300">Địa chỉ <em style="font-style:italic;color:var(--lm-gray)">giao hàng</em></h2>
            <div class="row g-4 mb-4">
              <div class="col-12"><label class="lm-form-label mb-2">Địa chỉ</label><input class="lm-input" value="128 Trần Hưng Đạo"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Quận / Huyện</label><input class="lm-input" value="Hoàn Kiếm"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Tỉnh / Thành phố</label><select class="lm-input"><option>Hà Nội</option><option>TP. Hồ Chí Minh</option><option>Đà Nẵng</option></select></div>
              <div class="col-6"><label class="lm-form-label mb-2">Mã bưu chính</label><input class="lm-input" value="100000"></div>
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
import { ref } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useToast } from '@/composables/useToast'
import { useReveal } from '@/composables/useReveal'

useReveal()
const { showToast } = useToast()
const activeTab = ref('orders')

const navItems = [
  { tab: 'orders',   icon: 'bi-file-text',   label: 'Đơn hàng của tôi', count: 12 },
  { tab: 'settings', icon: 'bi-person',       label: 'Thông tin cá nhân', count: null },
  { tab: 'address',  icon: 'bi-geo-alt',      label: 'Địa chỉ giao hàng', count: null },
]
const stats = [
  { num: '12',   label: 'Tổng đơn' },
  { num: '38.4M', label: 'Đã chi tiêu' },
  { num: '2.400', label: 'Điểm tích lũy' },
  { num: 'Gold', label: 'Hạng thành viên' },
]
const orders = [
  {
    id: '#LM-2025-0847', date: '12 tháng 11, 2025',
    status: { key: 'delivered', label: 'Đã giao' }, total: '14.180.000₫',
    items: [
      { letter: 'A', bg: 'linear-gradient(160deg,#EDE6D8,#C5B89A)' },
      { letter: 'B', bg: 'linear-gradient(160deg,#E0D4C4,#B8A88A)' },
    ]
  },
  {
    id: '#LM-2025-0791', date: '3 tháng 11, 2025',
    status: { key: 'shipping', label: 'Đang giao' }, total: '2.190.000₫',
    items: [{ letter: 'C', bg: 'linear-gradient(160deg,#DEDCD8,#A8A49E)' }]
  },
  {
    id: '#LM-2025-0756', date: '25 tháng 10, 2025',
    status: { key: 'processing', label: 'Đang xử lý' }, total: '9.970.000₫',
    items: [
      { letter: 'D', bg: 'linear-gradient(160deg,#E8E2D8,#CBBEA8)' },
      { letter: 'E', bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)' },
      { letter: 'F', bg: 'linear-gradient(160deg,#DEDAD4,#ABAAA6)' },
    ]
  },
]
</script>
