<template>
  <div>
    <header class="z-lookbook-header">
      <img src="/images/banners/banner5.png" alt="Lookbook Zestia theo dịp" />
      <div class="z-lookbook-intro container">
        <p class="lm-eyebrow mb-3">Zestia Lookbook</p>
        <h1>Chọn trang phục theo <em>khoảnh khắc</em></h1>
        <p>Bốn gợi ý bắt đầu nhanh, dẫn thẳng tới những sản phẩm đang bán phù hợp với từng dịp.</p>
      </div>
    </header>

    <main>
      <RouterLink v-for="(occasion, index) in occasions" :key="occasion.key"
                  :to="`/collections?occasion=${occasion.key}`"
                  class="z-occasion" :class="{ reverse: index % 2 }">
        <div class="z-occasion-media"><img :src="occasion.image" :alt="occasion.title" /></div>
        <div class="z-occasion-copy">
          <span>0{{ index + 1 }}</span>
          <p class="lm-eyebrow">{{ occasion.eyebrow }}</p>
          <h2>{{ occasion.title }}</h2>
          <p>{{ occasion.description }}</p>
          <strong>Xem sản phẩm <i class="bi bi-arrow-right"></i></strong>
        </div>
      </RouterLink>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '@/components/layout/AppFooter.vue'

const occasions = [
  { key: 'work', eyebrow: 'Ngày làm việc', title: 'Thanh lịch và linh hoạt', image: '/images/banners/banner2.jpg', description: 'Phom gọn, màu dễ phối và kiểu dáng phù hợp cho lịch họp, gặp khách hàng hoặc một ngày làm việc dài.' },
  { key: 'party', eyebrow: 'Tiệc tối', title: 'Điểm nhấn vừa đủ', image: '/images/banners/banner8.png', description: 'Chất liệu có độ rủ, chi tiết bắt sáng và phom tôn dáng dành cho những buổi tối cần một ấn tượng rõ ràng.' },
  { key: 'wedding', eyebrow: 'Cưới hỏi & Lễ hội', title: 'Trang trọng mà vẫn là bạn', image: '/images/banners/banner9.png', description: 'Những lựa chọn trang phục cao cấp cân bằng giữa nét trang trọng và khả năng vận động tự nhiên.' },
  { key: 'date', eyebrow: 'Hẹn hò & Dạo phố', title: 'Mềm mại, không gắng gượng', image: '/images/banners/banner7.png', description: 'Màu sắc gần gũi, đường nét nhẹ và những bộ trang phục có thể đi cùng bạn từ buổi cà phê đến bữa tối.' }
]
</script>

<style scoped>
.z-lookbook-header { position: relative; height: min(72vh, 680px); overflow: hidden; color: white; }
.z-lookbook-header > img { width: 100%; height: 100%; object-fit: cover; }
.z-lookbook-header::after { content: ''; position: absolute; inset: 0; background: linear-gradient(90deg, rgba(12,12,14,.78), rgba(12,12,14,.2)); }
.z-lookbook-intro { position: absolute; z-index: 1; left: 50%; top: 50%; transform: translate(-50%, -45%); }
.z-lookbook-intro h1 { max-width: 760px; margin: 0 0 20px; font-family: var(--z-font-display); font-size: 64px; font-weight: 400; line-height: 1.04; }
.z-lookbook-intro em { color: var(--z-accent); }
.z-lookbook-intro > p:last-child { max-width: 520px; color: rgba(255,255,255,.75); line-height: 1.7; }
.z-occasion { display: grid; grid-template-columns: 1.1fr .9fr; min-height: 520px; color: inherit; text-decoration: none; background: var(--z-white); }
.z-occasion.reverse { grid-template-columns: .9fr 1.1fr; }
.z-occasion.reverse .z-occasion-media { order: 2; }
.z-occasion-media { overflow: hidden; }
.z-occasion-media img { width: 100%; height: 100%; object-fit: cover; transition: transform .7s ease; }
.z-occasion:hover .z-occasion-media img { transform: scale(1.025); }
.z-occasion-copy { display: flex; flex-direction: column; justify-content: center; padding: 64px; }
.z-occasion-copy > span { color: var(--z-gray-light); font-family: var(--z-font-display); font-size: 42px; }
.z-occasion-copy h2 { margin: 6px 0 20px; font-family: var(--z-font-display); font-size: 42px; font-weight: 400; }
.z-occasion-copy > p:not(.lm-eyebrow) { max-width: 480px; color: var(--z-gray); line-height: 1.8; }
.z-occasion-copy strong { margin-top: 24px; color: var(--z-accent); font-size: 13px; }
@media (max-width: 768px) {
  .z-lookbook-header { height: 76vh; }
  .z-lookbook-intro h1 { font-size: 40px; }
  .z-occasion, .z-occasion.reverse { grid-template-columns: 1fr; }
  .z-occasion.reverse .z-occasion-media { order: 0; }
  .z-occasion-media { min-height: 360px; }
  .z-occasion-copy { padding: 42px 24px; }
  .z-occasion-copy h2 { font-size: 34px; }
}
</style>
