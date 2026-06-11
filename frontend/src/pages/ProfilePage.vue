<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <!-- Sidebar -->
        <div class="col-lg-3">
          <div style="position:sticky;top:100px">
            <div class="position-relative mb-3" style="width:80px;height:80px">
              <div style="width:100%;height:100%;border-radius:50%;background:linear-gradient(135deg,var(--z-accent-soft),var(--z-accent));display:flex;align-items:center;justify-content:center;font-family:var(--z-font-display);font-size:28px;font-weight:500;color:var(--z-white)">N</div>
            </div>
            <div class="z-display mb-1" style="font-size:24px;font-weight:500;color:var(--z-dark)">Nguyen Lan Anh</div>
            <div class="d-inline-flex align-items-center gap-2 mb-4"
                 style="padding:4px 12px;background:var(--z-accent-soft);font-size:11px;font-weight:600;color:var(--z-accent);border-radius:20px">
              <i class="bi bi-star-fill" style="font-size:10px"></i> VIP Gold
            </div>

            <nav style="border-top:1px solid var(--z-gray-border)">
              <div v-for="item in navItems" :key="item.tab"
                   class="lm-profile-nav-item" :class="{ active: activeTab === item.tab }"
                   @click="activeTab = item.tab">
                <i class="bi" :class="item.icon"></i>
                {{ item.label }}
                <span v-if="item.count" class="lm-nav-count">{{ item.count }}</span>
              </div>
              <div class="lm-profile-nav-item" @click="$router.push('/login')">
                <i class="bi bi-box-arrow-right"></i>
                Dang xuat
              </div>
            </nav>
          </div>
        </div>

        <!-- Content -->
        <div class="col-lg-9 pt-2">

          <!-- Orders Tab -->
          <div v-if="activeTab === 'orders'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Don hang <em style="font-style:italic;color:var(--z-gray)">cua toi</em></h2>
            <div class="row g-3 mb-5">
              <div v-for="(stat, i) in stats" :key="stat.label" class="col-6 col-md-3 lm-reveal" :style="{ transitionDelay: i*0.1+'s' }">
                <div style="padding:20px;background:var(--z-white);border:1px solid var(--z-gray-border);text-align:center;border-radius:var(--z-radius-lg)">
                  <div class="z-display" style="font-size:28px;font-weight:500;color:var(--z-dark)">{{ stat.num }}</div>
                  <div style="font-size:12px;font-weight:500;color:var(--z-gray);margin-top:4px">{{ stat.label }}</div>
                </div>
              </div>
            </div>
            <div class="d-flex flex-column gap-3">
              <div v-for="order in orders" :key="order.id" class="lm-reveal"
                   style="border:1px solid var(--z-gray-border);padding:20px;background:var(--z-white);border-radius:var(--z-radius-lg)">
                <div class="d-flex justify-content-between align-items-start mb-3 pb-3" style="border-bottom:1px solid var(--z-gray-border)">
                  <div>
                    <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ order.id }}</div>
                    <div style="font-size:13px;color:var(--z-gray)">{{ order.date }}</div>
                  </div>
                  <span :class="'lm-status-' + order.status.key">{{ order.status.label }}</span>
                </div>
                <div class="d-flex gap-2 mb-3">
                  <div v-for="item in order.items" :key="item.letter" style="width:56px;height:70px;overflow:hidden;border-radius:var(--z-radius)">
                    <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                         :style="{ background: item.bg, fontFamily:'var(--z-font-display)', fontSize:'16px', color:'rgba(255,255,255,0.3)', fontStyle:'italic' }">
                      {{ item.letter }}
                    </div>
                  </div>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                  <div class="z-display" style="font-size:20px;font-weight:500;color:var(--z-dark)">{{ order.total }}</div>
                  <button class="lm-btn-primary" style="padding:10px 20px;font-size:12px"><span>Mua lai</span></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Settings Tab -->
          <div v-if="activeTab === 'settings'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Thong tin <em style="font-style:italic;color:var(--z-gray)">ca nhan</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-6"><label class="lm-form-label mb-2">Ho</label><input class="lm-input" value="Nguyen"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Ten</label><input class="lm-input" value="Lan Anh"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Email</label><input class="lm-input" type="email" value="lananh@email.com"></div>
              <div class="col-6"><label class="lm-form-label mb-2">So dien thoai</label><input class="lm-input" type="tel" value="0912 345 678"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Ngay sinh</label><input class="lm-input" type="date" value="1995-03-15"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Gioi tinh</label><select class="lm-input"><option>Nu</option><option>Nam</option></select></div>
            </div>
            <button class="lm-btn-primary" @click="showToast('Da luu thong tin!')"><span>Luu thay doi</span></button>
          </div>

          <!-- Address Tab -->
          <div v-if="activeTab === 'address'">
            <h2 class="z-display mb-4" style="font-size:28px;font-weight:400">Dia chi <em style="font-style:italic;color:var(--z-gray)">giao hang</em></h2>
            <div class="row g-4 mb-4" style="background:var(--z-white);padding:24px;border-radius:var(--z-radius-lg);border:1px solid var(--z-gray-border)">
              <div class="col-12"><label class="lm-form-label mb-2">Dia chi</label><input class="lm-input" value="128 Tran Hung Dao"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Quan / Huyen</label><input class="lm-input" value="Hoan Kiem"></div>
              <div class="col-6"><label class="lm-form-label mb-2">Tinh / Thanh pho</label><select class="lm-input"><option>Ha Noi</option><option>TP. Ho Chi Minh</option><option>Da Nang</option></select></div>
            </div>
            <button class="lm-btn-primary" @click="showToast('Da luu dia chi!')"><span>Luu dia chi</span></button>
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
  { tab: 'orders',   icon: 'bi-file-text',   label: 'Don hang cua toi', count: 5 },
  { tab: 'settings', icon: 'bi-person',       label: 'Thong tin ca nhan', count: null },
  { tab: 'address',  icon: 'bi-geo-alt',      label: 'Dia chi giao hang', count: null },
]
const stats = [
  { num: '5',    label: 'Tong don' },
  { num: '23M',  label: 'Da chi tieu' },
  { num: '1.200', label: 'Diem tich luy' },
  { num: 'Gold', label: 'Hang thanh vien' },
]
const orders = [
  {
    id: '#ZT-2025-0001', date: '15 thang 1, 2025',
    status: { key: 'delivered', label: 'Da giao' }, total: '6.070.000d',
    items: [
      { letter: 'Z', bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)' },
      { letter: 'e', bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)' },
    ]
  },
  {
    id: '#ZT-2025-0002', date: '18 thang 1, 2025',
    status: { key: 'shipping', label: 'Dang giao' }, total: '8.580.000d',
    items: [{ letter: 's', bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)' }]
  },
  {
    id: '#ZT-2025-0003', date: '20 thang 1, 2025',
    status: { key: 'processing', label: 'Dang xu ly' }, total: '4.290.000d',
    items: [
      { letter: 't', bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)' },
      { letter: 'i', bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)' },
    ]
  },
]
</script>
