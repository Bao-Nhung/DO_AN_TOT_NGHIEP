<template>
  <div>
    <div class="lm-page-hero" data-title="VỀ CHÚNG TÔI">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thông Tin</p>
        <h1 class="mb-3">Về <em>Zestia</em></h1>
        <p style="max-width:520px">Thương hiệu thời trang Việt Nam — nơi truyền thống gặp gỡ hiện đại, tôn vinh vẻ đẹp phụ nữ Việt.</p>
      </div>
    </div>

    <section class="py-5">
      <div class="container">
        <div class="row g-5 align-items-center mb-5">
          <div class="col-lg-6">
            <div style="aspect-ratio:4/3;background:linear-gradient(160deg,#F3E8E6,#D4A99E);border-radius:var(--z-radius-lg);display:flex;align-items:center;justify-content:center;font-family:var(--z-font-display);font-size:80px;color:rgba(255,255,255,0.15)">
              Z
            </div>
          </div>
          <div class="col-lg-6">
            <p class="lm-eyebrow mb-3">Câu Chuyện Của Chúng Tôi</p>
            <h2 class="lm-section-title mb-4">Toả sáng theo <em>cách của bạn</em></h2>
            <p style="font-size:14px;line-height:1.8;color:var(--z-gray);margin-bottom:20px">
              Zestia được thành lập với sứ mệnh mang đến những thiết kế váy độc đáo cho phụ nữ Việt Nam.
              Chúng tôi tin rằng thời trang không chỉ là trang phục — đó là cách bạn kể câu chuyện của chính mình.
            </p>
            <p style="font-size:14px;line-height:1.8;color:var(--z-gray);margin-bottom:20px">
              Mỗi sản phẩm của Zestia được thiết kế tỉ mỉ, pha trộn giữa nét truyền thống và xu hướng hiện đại,
              sử dụng chất liệu cao cấp từ lụa tơ tằm đến vải nhập khẩu — giúp bạn tự tin toả sáng mỗi ngày.
            </p>
            <div class="d-flex gap-4 flex-wrap">
              <div v-for="stat in stats" :key="stat.label" class="text-center">
                <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-accent)">{{ stat.num }}</div>
                <div style="font-size:11px;font-weight:500;letter-spacing:0.08em;text-transform:uppercase;color:var(--z-gray)">{{ stat.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section style="background:var(--z-bg-alt);padding:80px 0">
      <div class="container">
        <div class="text-center mb-5">
          <p class="lm-eyebrow mb-2">Giá Trị Cốt Lõi</p>
          <h2 class="lm-section-title">Tại sao chọn <em>Zestia</em></h2>
        </div>
        <div class="row g-4">
          <div v-for="v in values" :key="v.title" class="col-md-6 col-lg-3">
            <div style="padding:32px;background:var(--z-white);border-radius:var(--z-radius-lg);height:100%;border:1px solid var(--z-gray-border)">
              <div style="width:48px;height:48px;border-radius:12px;background:var(--z-accent-soft);display:flex;align-items:center;justify-content:center;margin-bottom:20px">
                <i class="bi" :class="v.icon" style="font-size:20px;color:var(--z-accent)"></i>
              </div>
              <h4 style="font-size:16px;font-weight:600;color:var(--z-dark);margin-bottom:8px">{{ v.title }}</h4>
              <p style="font-size:13px;line-height:1.7;color:var(--z-gray);margin:0">{{ v.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="py-5">
      <div class="container">
        <div class="text-center mb-5">
          <p class="lm-eyebrow mb-2">Thông Tin Cửa Hàng</p>
          <h2 class="lm-section-title">Liên hệ <em>với chúng tôi</em></h2>
        </div>
        <div class="row g-4">
          <div v-for="info in contactInfo" :key="info.label" class="col-md-6 col-lg-3">
            <div class="text-center" style="padding:24px">
              <i class="bi" :class="info.icon" style="font-size:28px;color:var(--z-accent);margin-bottom:12px;display:block"></i>
              <h5 style="font-size:14px;font-weight:600;color:var(--z-dark);margin-bottom:6px">{{ info.label }}</h5>
              <p style="font-size:13px;color:var(--z-gray);margin:0">{{ info.value }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div style="background:var(--z-dark);padding:60px 40px;text-align:center">
      <h2 class="z-display mb-3" style="font-size:36px;font-weight:400;color:var(--z-white)">
        Sẵn sàng khám phá <em style="font-style:italic;color:var(--z-accent)">Zestia</em>?
      </h2>
      <p style="font-size:14px;color:rgba(255,255,255,0.5);margin-bottom:24px">
        Trải nghiệm bộ sưu tập mới nhất và tìm cho mình phong cách riêng.
      </p>
      <RouterLink to="/collections" class="lm-btn-primary"><span>Xem bộ sưu tập</span></RouterLink>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '@/components/layout/AppFooter.vue'

const stats = [
  { num: '6+', label: 'Loại váy' },
  { num: '200+', label: 'Thiết kế' },
  { num: '10K+', label: 'Khách hàng' },
  { num: '5', label: 'Năm kinh nghiệm' },
]

const values = [
  { icon: 'bi-gem', title: 'Chất liệu cao cấp', desc: 'Sử dụng lụa tơ tằm, vải gấm nhập khẩu và các loại vải cao cấp được tuyển chọn kỹ lưỡng.' },
  { icon: 'bi-palette', title: 'Thiết kế độc quyền', desc: 'Mỗi mẫu váy đều được thiết kế riêng bởi đội ngũ nhà thiết kế tài năng của Zestia.' },
  { icon: 'bi-truck', title: 'Giao hàng toàn quốc', desc: 'Miễn phí vận chuyển cho đơn hàng từ 500K. Giao hàng nhanh trong 2-5 ngày.' },
  { icon: 'bi-arrow-repeat', title: 'Đổi trả 30 ngày', desc: 'Cam kết đổi trả miễn phí trong 30 ngày nếu sản phẩm không vừa ý.' },
]

const contactInfo = [
  { icon: 'bi-geo-alt-fill', label: 'Địa chỉ', value: 'FPT Polytechnic, Hà Nội' },
  { icon: 'bi-telephone-fill', label: 'Hotline', value: '1800 9999 (miễn phí)' },
  { icon: 'bi-envelope-fill', label: 'Email', value: 'hello@zestia.vn' },
  { icon: 'bi-clock-fill', label: 'Giờ làm việc', value: 'T2-T7: 9:00 - 21:00' },
]
</script>
