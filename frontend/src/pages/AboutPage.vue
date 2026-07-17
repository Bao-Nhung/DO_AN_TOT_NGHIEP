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
            <img src="/images/banners/banner6.png" alt="Thiết kế Zestia" style="width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:var(--z-radius-lg)" />
          </div>
          <div class="col-lg-6">
            <p class="lm-eyebrow mb-3">Câu Chuyện Của Chúng Tôi</p>
            <h2 class="lm-section-title mb-4">Toả sáng theo <em>cách của bạn</em></h2>
            <p style="font-size:14px;line-height:1.8;color:var(--z-gray);margin-bottom:20px">
              Zestia được thành lập với sứ mệnh mang đến những thiết kế váy độc đáo cho phụ nữ Việt Nam.
              Chúng tôi tin rằng thời trang không chỉ là trang phục — đó là cách bạn kể câu chuyện của chính mình.
            </p>
            <p style="font-size:14px;line-height:1.8;color:var(--z-gray);margin-bottom:20px">
              Danh mục hiện có nhiều phom, chất liệu, màu và kích cỡ. Thông tin tồn kho, bảng size,
              đánh giá và chính sách được hiển thị từ dữ liệu đang áp dụng để bạn dễ đối chiếu trước khi mua.
            </p>
            <div class="d-flex gap-4 flex-wrap">
              <div v-for="stat in stats" :key="stat.label" class="text-center">
                <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-accent)">{{ stat.num }}</div>
                <div style="font-size:11px;font-weight:500;letter-spacing:0;text-transform:uppercase;color:var(--z-gray)">{{ stat.label }}</div>
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
import { computed, onMounted, reactive } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api } from '@/composables/useApi'

const summary = reactive({ activeProductCount: 0, categoryCount: 0, customerCount: 0, completedOrderCount: 0, reviewCount: 0, policies: [] })
const stats = computed(() => [
  { num: summary.categoryCount || 0, label: 'Dòng sản phẩm' },
  { num: summary.activeProductCount || 0, label: 'Sản phẩm đang bán' },
  { num: summary.customerCount || 0, label: 'Tài khoản khách hàng' },
  { num: summary.reviewCount || 0, label: 'Đánh giá đã mua' },
])

const values = computed(() => {
  const shipping = summary.policies?.find(policy => policy.code === 'SHIPPING')
  const returns = summary.policies?.find(policy => policy.code === 'RETURN')
  return [
    { icon: 'bi-rulers', title: 'Bảng size theo sản phẩm', desc: 'Đối chiếu chiều cao, cân nặng và số đo theo từng phom trước khi chọn biến thể.' },
    { icon: 'bi-patch-check', title: 'Đánh giá đã xác minh', desc: 'Chỉ tài khoản có đơn đã giao chứa đúng sản phẩm mới có thể gửi đánh giá.' },
    { icon: 'bi-truck', title: shipping?.title || 'Giao hàng', desc: shipping?.summary || 'Phí giao hàng được hệ thống tính từ địa chỉ và giá trị đơn.' },
    { icon: 'bi-arrow-repeat', title: returns?.title || 'Đổi trả', desc: returns?.summary || 'Điều kiện và thời hạn đổi trả được công bố trong trang chính sách.' },
  ]
})

onMounted(async () => {
  try { Object.assign(summary, await api().getStorefrontSummary()) }
  catch (error) { console.warn('Không tải được thông tin cửa hàng', error) }
})

const contactInfo = [
  { icon: 'bi-geo-alt-fill', label: 'Địa chỉ', value: 'FPT Polytechnic, Hà Nội' },
  { icon: 'bi-telephone-fill', label: 'Hotline', value: '1800 9999 (miễn phí)' },
  { icon: 'bi-envelope-fill', label: 'Email', value: 'hello@zestia.vn' },
  { icon: 'bi-clock-fill', label: 'Giờ làm việc', value: 'T2-T7: 9:00 - 21:00' },
]
</script>
