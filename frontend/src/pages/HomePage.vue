<template>
  <div>
    <!-- ── Hero ── -->
    <section class="hero position-relative d-flex align-items-center overflow-hidden"
             style="min-height:100vh;background:var(--lm-cream)">
      <!-- bg gradients -->
      <div class="position-absolute inset-0 w-100 h-100" style="
        background:
          radial-gradient(ellipse 60% 80% at 75% 50%,#EDE6D8 0%,transparent 60%),
          radial-gradient(ellipse 40% 60% at 20% 70%,#F2EDE4 0%,transparent 60%);
        pointer-events:none"></div>

      <!-- Content -->
      <div class="container-fluid position-relative" style="z-index:2;padding:0 60px;max-width:55%">
        <p class="lm-eyebrow mb-4" style="animation:lm-fade-up 0.8s ease 0.3s both">Bộ Sưu Tập Thu Đông 2025</p>
        <h1 class="lm-display mb-4" style="
          font-size:clamp(64px,8vw,110px);font-weight:300;line-height:0.95;
          letter-spacing:-0.02em;color:var(--lm-black);
          animation:lm-fade-up 0.9s ease 0.5s both">
          Vẻ đẹp<br>
          <em style="font-style:italic;color:var(--lm-gray)">tối giản</em><br>
          <span style="color:var(--lm-gold)">vượt thời gian</span>
        </h1>
        <p class="mb-5" style="font-size:13px;font-weight:300;line-height:1.8;color:var(--lm-gray);max-width:400px;animation:lm-fade-up 0.8s ease 0.7s both">
          Những thiết kế mang tinh thần tự do và sự thanh lịch vĩnh cửu — được tạo nên từ những chất liệu quý hiếm nhất thế giới.
        </p>
        <div class="d-flex gap-3 align-items-center" style="animation:lm-fade-up 0.8s ease 0.9s both">
          <RouterLink to="/collections" class="lm-btn-primary"><span>Khám phá ngay</span></RouterLink>
          <RouterLink to="/collections" class="lm-btn-secondary">
            Xem lookbook <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>
      </div>

      <!-- Hero visual -->
      <div class="position-absolute top-0 end-0 h-100 overflow-hidden" style="width:48%">
        <div class="w-100 h-100 d-flex align-items-center justify-content-center"
             style="background:linear-gradient(135deg,#E8E0D0,#D4C9B5 40%,#C5B89A 100%);
                    font-family:var(--lm-font-display);font-size:120px;color:rgba(255,255,255,0.15);letter-spacing:-0.05em"
             id="hero-parallax">
          L
        </div>
      </div>

      <!-- Scroll hint -->
      <div class="position-absolute d-flex align-items-center gap-3"
           style="bottom:40px;left:60px;font-size:9px;font-weight:500;letter-spacing:0.25em;text-transform:uppercase;color:var(--lm-gray);animation:lm-fade-up 0.8s ease 1.2s both">
        <div style="width:60px;height:1px;background:var(--lm-beige-dark)"></div>
        Cuộn xuống
      </div>
    </section>

    <!-- ── Marquee ── -->
    <MarqueeStrip />

    <!-- ── Collections ── -->
    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal">
          <div>
            <p class="lm-eyebrow mb-2">Danh Mục Nổi Bật</p>
            <h2 class="lm-section-title">Khám phá <em>bộ sưu tập</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Tất cả sản phẩm <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>

        <div class="row g-3">
          <!-- Large card -->
          <div class="col-lg-5 lm-reveal">
            <CollectionCard label="Mới nhất" name="Đầm Lụa Sang Trọng" letter="L"
                            bg="linear-gradient(160deg,#E8E0D0,#C5B89A 40%,#B8A88A)" tall />
          </div>
          <div class="col-lg-7">
            <div class="row g-3">
              <div class="col-6 lm-reveal"><CollectionCard label="Best seller" name="Áo Khoác Cashmere" letter="U" bg="linear-gradient(160deg,#E0D4C4,#B8A88A)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Mới" name="Phụ Kiện" letter="M" bg="linear-gradient(160deg,#DEDCD8,#A8A49E)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Giới hạn" name="Đặt Riêng" letter="I" bg="linear-gradient(160deg,#E8E2D8,#CBBEA8)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Mới" name="Lifestyle" letter="È" bg="linear-gradient(160deg,#E4DDD2,#C0B49E)" /></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── Featured Banner ── -->
    <div class="lm-reveal mx-4 mx-lg-5 mb-0 p-5 position-relative overflow-hidden d-flex align-items-center justify-content-between gap-5"
         style="background:var(--lm-black)">
      <!-- Watermark -->
      <div class="position-absolute" style="right:-20px;top:50%;transform:translateY(-50%);
        font-family:var(--lm-font-display);font-size:220px;font-weight:300;color:rgba(255,255,255,0.03);
        pointer-events:none;letter-spacing:-0.05em;white-space:nowrap;line-height:1">LUMIÈRE</div>

      <div style="position:relative;z-index:1">
        <p class="lm-eyebrow mb-3">Câu Chuyện Thương Hiệu</p>
        <h2 class="lm-display mb-3" style="font-size:60px;font-weight:300;line-height:1;color:var(--lm-white);letter-spacing:-0.02em">
          Tạo nên từng <em style="font-style:italic;color:var(--lm-gold)">khoảnh khắc</em>
        </h2>
        <p style="font-size:13px;font-weight:300;line-height:1.9;color:rgba(255,255,255,0.55);max-width:380px;margin-bottom:40px">
          Mỗi thiết kế là một chương trong câu chuyện về vẻ đẹp người phụ nữ Việt — hiện đại, tự tin, và đầy sức sống.
        </p>
        <a class="lm-btn-outline-light">Đọc câu chuyện của chúng tôi</a>
      </div>

      <div class="d-flex flex-column gap-4 flex-shrink-0 text-end" style="position:relative;z-index:1">
        <div v-for="stat in stats" :key="stat.label">
          <div class="lm-display" style="font-size:52px;font-weight:300;color:var(--lm-white);line-height:1;letter-spacing:-0.02em">
            {{ stat.num }}<span style="color:var(--lm-gold);font-size:28px;vertical-align:super">+</span>
          </div>
          <div style="font-size:9px;font-weight:500;letter-spacing:0.25em;text-transform:uppercase;color:rgba(255,255,255,0.35)">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- ── New Arrivals ── -->
    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal">
          <div>
            <p class="lm-eyebrow mb-2">Vừa Ra Mắt</p>
            <h2 class="lm-section-title">Hàng mới <em>tháng này</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Xem thêm <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>
        <div class="row g-4">
          <div v-for="(p, i) in newArrivals" :key="p.id"
               class="col-6 col-lg-3 lm-reveal" :style="{ transitionDelay: i * 0.1 + 's' }">
            <ProductCard :product="p" />
          </div>
        </div>
      </div>
    </section>

    <!-- ── Editorial ── -->
    <div class="row g-0">
      <div class="col-lg-6 lm-reveal-left">
        <div style="aspect-ratio:4/5;background:linear-gradient(160deg,#D4C9B5,#B8A88A);
                    display:flex;align-items:center;justify-content:center;
                    font-family:var(--lm-font-display);font-size:80px;color:rgba(255,255,255,0.12);font-style:italic">
          Éditorial
        </div>
      </div>
      <div class="col-lg-6 lm-reveal-right d-flex flex-column justify-content-center"
           style="background:var(--lm-beige);padding:80px">
        <p class="lm-eyebrow mb-3">Phong Cách Sống</p>
        <div class="lm-divider-gold"></div>
        <h2 class="lm-display mb-4" style="font-size:44px;font-weight:300;line-height:1.1;color:var(--lm-black);letter-spacing:-0.01em">
          Thời trang như một <em style="font-style:italic;color:var(--lm-gray)">ngôn ngữ</em>
        </h2>
        <p style="font-size:13px;font-weight:300;line-height:1.9;color:var(--lm-gray);margin-bottom:36px">
          Không chỉ là quần áo — đó là cách bạn kể câu chuyện của mình. LUMIÈRE tin rằng phong cách là sự phản chiếu của tâm hồn.
        </p>
        <RouterLink to="/collections" class="lm-btn-primary align-self-start"><span>Khám phá lifestyle</span></RouterLink>
      </div>
    </div>

    <!-- ── Newsletter ── -->
    <div style="background:var(--lm-black);padding:100px 60px;text-align:center">
      <p class="lm-eyebrow mb-3">Cộng đồng LUMIÈRE</p>
      <h2 class="lm-display mb-3" style="font-size:56px;font-weight:300;color:var(--lm-white);letter-spacing:-0.02em">
        Nhận ưu đãi <em style="font-style:italic;color:var(--lm-gold)">độc quyền</em>
      </h2>
      <p style="font-size:13px;font-weight:300;color:rgba(255,255,255,0.5);margin-bottom:40px">
        Đăng ký để nhận thông tin về bộ sưu tập mới, ưu đãi thành viên và lời mời sự kiện riêng tư.
      </p>
      <div class="d-flex mx-auto" style="max-width:480px;border:1px solid rgba(255,255,255,0.15)">
        <input class="flex-grow-1 lm-input" type="email" placeholder="Email của bạn..."
               style="background:transparent;color:var(--lm-white);border:none">
        <button @click="showToast('Đăng ký thành công!')"
                style="padding:18px 32px;background:var(--lm-gold);color:var(--lm-black);border:none;font-size:9px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;cursor:pointer;font-family:var(--lm-font-body)">
          Đăng ký
        </button>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import MarqueeStrip    from '@/components/ui/MarqueeStrip.vue'
import ProductCard     from '@/components/ui/ProductCard.vue'
import CollectionCard  from '@/components/ui/CollectionCard.vue'
import AppFooter       from '@/components/layout/AppFooter.vue'
import { useToast }    from '@/composables/useToast'
import { useReveal }   from '@/composables/useReveal'
import { products }    from '@/composables/useProducts'

useReveal()
const { showToast } = useToast()

const newArrivals = products.slice(0, 4)
const stats = [
  { num: '12',  label: 'Năm kinh nghiệm' },
  { num: '850', label: 'Thiết kế độc quyền' },
  { num: '50K', label: 'Khách hàng tin tưởng' },
]

// Hero parallax on scroll
function onScroll() {
  const hero = document.getElementById('hero-parallax')
  if (hero) hero.style.transform = `translateY(${window.scrollY * 0.2}px)`
}
onMounted  (() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
@keyframes lm-fade-up {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
