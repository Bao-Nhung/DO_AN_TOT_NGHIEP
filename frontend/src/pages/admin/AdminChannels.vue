<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-3">
      <div>
        <p class="lm-eyebrow mb-2">Kênh bán ngoài</p>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Liên kết nền tảng bán hàng</h1>
        <p class="z-page-description">
          Zestia chưa kết nối API đồng bộ với các nền tảng bên ngoài. Các liên kết dưới đây chỉ mở trang quản trị chính thức và không thay đổi sản phẩm, tồn kho hay đơn hàng trong hệ thống.
        </p>
      </div>
    </div>

    <div class="z-channel-notice mb-4" role="note">
      <i class="bi bi-info-circle"></i>
      <div>
        <strong>Trạng thái: chưa tích hợp đồng bộ</strong>
        <span>Mọi dữ liệu bán hàng hiện tại chỉ được quản lý trong Zestia.</span>
      </div>
    </div>

    <div class="row g-3">
      <div v-for="channel in channels" :key="channel.name" class="col-12 col-md-6 col-xl-3">
        <article class="z-admin-card z-channel-card h-100">
          <div class="z-channel-icon" :style="{ color: channel.color, background: channel.background }">
            <i class="bi" :class="channel.icon"></i>
          </div>
          <div>
            <h2>{{ channel.name }}</h2>
            <p>{{ channel.description }}</p>
          </div>
          <a class="lm-btn-secondary justify-content-center mt-auto" :href="channel.url" target="_blank" rel="noopener noreferrer">
            <span>Mở trang chính thức</span>
            <i class="bi bi-box-arrow-up-right"></i>
          </a>
        </article>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '@/components/layout/AdminLayout.vue'

const channels = [
  {
    name: 'Facebook Shop',
    description: 'Trang chủ Facebook dành cho hoạt động giới thiệu và bán hàng.',
    url: 'https://www.facebook.com/',
    icon: 'bi-facebook',
    color: '#1877f2',
    background: '#eef5ff'
  },
  {
    name: 'TikTok Shop',
    description: 'Trang chủ TikTok dành cho nội dung và kênh bán hàng.',
    url: 'https://www.tiktok.com/',
    icon: 'bi-tiktok',
    color: '#161823',
    background: '#f3f3f4'
  },
  {
    name: 'Shopee',
    description: 'Trang chủ sàn thương mại điện tử Shopee Việt Nam.',
    url: 'https://shopee.vn/',
    icon: 'bi-bag',
    color: '#ee4d2d',
    background: '#fff3ef'
  },
  {
    name: 'Lazada',
    description: 'Trang chủ sàn thương mại điện tử Lazada Việt Nam.',
    url: 'https://www.lazada.vn/',
    icon: 'bi-shop-window',
    color: '#10146f',
    background: '#f1f1ff'
  }
]
</script>

<style scoped>
.z-page-description {
  max-width: 780px;
  margin: 0;
  color: var(--z-gray);
  font-size: 14px;
  line-height: 1.7;
}

.z-channel-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e6d7ad;
  border-radius: var(--z-radius);
  background: #fffaf0;
  color: #6f5313;
}

.z-channel-notice i { margin-top: 2px; }
.z-channel-notice strong,
.z-channel-notice span { display: block; }
.z-channel-notice span { margin-top: 3px; font-size: 13px; }

.z-channel-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 270px;
}

.z-channel-icon {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: var(--z-radius);
  font-size: 22px;
}

.z-channel-card h2 {
  margin: 0 0 8px;
  color: var(--z-dark);
  font-size: 17px;
  font-weight: 650;
}

.z-channel-card p {
  margin: 0;
  color: var(--z-gray);
  font-size: 13px;
  line-height: 1.6;
}
</style>
