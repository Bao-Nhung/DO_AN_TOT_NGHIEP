<template>
  <div>
    <!-- Hero -->
    <section class="position-relative d-flex align-items-center overflow-hidden"
             style="min-height:100vh;background:var(--z-bg)">
      <div class="position-absolute w-100 h-100" style="
        background:
          radial-gradient(ellipse 60% 80% at 75% 50%, var(--z-accent-soft) 0%, transparent 50%),
          radial-gradient(ellipse 50% 60% at 20% 70%, var(--z-warm-light) 0%, transparent 50%);
        pointer-events:none"></div>

      <div class="container-fluid position-relative" style="z-index:2;padding:0 60px;max-width:55%">
        <p class="lm-eyebrow mb-3" style="animation:z-fade-up 0.8s ease 0.3s both">Bộ Sưu Tập Mới 2025</p>
        <h1 class="z-display mb-4" style="
          font-size:clamp(52px,7vw,96px);font-weight:400;line-height:1;
          color:var(--z-dark);animation:z-fade-up 0.9s ease 0.5s both">
          Phong cách<br>
          <em style="font-style:italic;color:var(--z-gray)">của riêng</em><br>
          <span style="color:var(--z-accent)">bạn</span>
        </h1>
        <p class="mb-5" style="font-size:15px;font-weight:400;line-height:1.7;color:var(--z-gray);max-width:420px;animation:z-fade-up 0.8s ease 0.7s both">
          Zestia mang đến những thiết kế váy độc đáo, pha trộn giữa nét truyền thống và xu hướng hiện đại — giúp bạn toả sáng mỗi ngày.
        </p>
        <div class="d-flex gap-3 align-items-center flex-wrap" style="animation:z-fade-up 0.8s ease 0.9s both">
          <RouterLink to="/collections" class="lm-btn-primary"><span>Khám phá ngay</span></RouterLink>
          <RouterLink to="/collections" class="lm-btn-secondary">
            Xem bộ sưu tập <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>
      </div>

      <div class="position-absolute top-0 end-0 h-100 overflow-hidden d-none d-lg-block" style="width:46%">
        <img src="/images/banners/banner1.png" alt="Zestia Fashion"
             style="width:100%;height:100%;object-fit:cover" id="hero-parallax" />
      </div>

      <div class="position-absolute d-none d-lg-flex align-items-center gap-3"
           style="bottom:40px;left:60px;font-size:12px;font-weight:500;color:var(--z-gray);animation:z-fade-up 0.8s ease 1.2s both">
        <div style="width:48px;height:2px;background:var(--z-accent);border-radius:1px"></div>
        Cuộn xuống
      </div>
    </section>

    <MarqueeStrip />

    <!-- Collections -->
    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal">
          <div>
            <p class="lm-eyebrow mb-2">Danh Mục</p>
            <h2 class="lm-section-title">Khám phá <em>bộ sưu tập</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Tất cả sản phẩm <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>

        <div class="row g-3">
          <div class="col-lg-5 lm-reveal">
            <CollectionCard label="Mới nhất" name="Váy Lụa Truyền Thống" letter="Z"
                            image="/images/banners/banner6.png"
                            bg="linear-gradient(160deg,#F3E8E6,#D4A99E 40%,#C08B7E)" tall />
          </div>
          <div class="col-lg-7">
            <div class="row g-3">
              <div class="col-6 lm-reveal"><CollectionCard label="Bán chạy" name="Váy Cách Tân" letter="e" image="/images/banners/banner7.png" bg="linear-gradient(160deg,#E8DDD6,#C4A98E)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Giảm giá" name="Váy Dạ Hội" letter="s" image="/images/banners/banner8.png" bg="linear-gradient(160deg,#E6E0DA,#A8A49E)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Giới hạn" name="Váy Cưới" letter="t" image="/images/banners/banner9.png" bg="linear-gradient(160deg,#F0E8E0,#D4C0A8)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Mới" name="Váy Công Sở" letter="ia" bg="linear-gradient(160deg,#E4DDD2,#C0B49E)" /></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Brand Banner -->
    <div class="lm-reveal mx-3 mx-lg-5 p-5 position-relative overflow-hidden d-flex align-items-center justify-content-between gap-5"
         style="background:var(--z-dark);border-radius:var(--z-radius-lg)">
      <div class="position-absolute" style="right:-20px;top:50%;transform:translateY(-50%);
        font-family:var(--z-font-display);font-size:200px;font-weight:300;color:rgba(255,255,255,0.03);
        pointer-events:none;white-space:nowrap;line-height:1">ZESTIA</div>

      <div style="position:relative;z-index:1">
        <p class="lm-eyebrow mb-3">Về Zestia</p>
        <h2 class="z-display mb-3" style="font-size:48px;font-weight:400;line-height:1.1;color:var(--z-white)">
          Toả sáng theo <em style="font-style:italic;color:var(--z-accent)">cách của bạn</em>
        </h2>
        <p style="font-size:14px;font-weight:400;line-height:1.8;color:rgba(255,255,255,0.5);max-width:400px;margin-bottom:36px">
          Mỗi thiết kế của Zestia là một câu chuyện về phụ nữ Việt — tự tin, năng động và đầy sức sáng tạo.
        </p>
        <RouterLink to="/about" class="lm-btn-outline-light">Xem thêm về chúng tôi</RouterLink>
      </div>

      <div class="d-none d-lg-flex flex-column gap-4 flex-shrink-0 text-end" style="position:relative;z-index:1">
        <div v-for="stat in stats" :key="stat.label">
          <div class="z-display" style="font-size:44px;font-weight:400;color:var(--z-white);line-height:1">
            {{ stat.num }}<span style="color:var(--z-accent);font-size:24px;vertical-align:super">+</span>
          </div>
          <div style="font-size:11px;font-weight:500;letter-spacing:0.1em;text-transform:uppercase;color:rgba(255,255,255,0.35)">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- New Arrivals -->
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

    <!-- Editorial -->
    <div class="row g-0">
      <div class="col-lg-6 lm-reveal-left">
        <div style="aspect-ratio:4/5;overflow:hidden">
          <img src="/images/banners/banner5.png" alt="Zestia Lookbook"
               style="width:100%;height:100%;object-fit:cover" />
        </div>
      </div>
      <div class="col-lg-6 lm-reveal-right d-flex flex-column justify-content-center"
           style="background:var(--z-bg-alt);padding:80px">
        <p class="lm-eyebrow mb-3">Phong Cách Sống</p>
        <div class="lm-divider-gold"></div>
        <h2 class="z-display mb-4" style="font-size:38px;font-weight:400;line-height:1.15;color:var(--z-dark)">
          Thời trang là <em style="font-style:italic;color:var(--z-gray)">ngôn ngữ</em> của bạn
        </h2>
        <p style="font-size:14px;font-weight:400;line-height:1.8;color:var(--z-gray);margin-bottom:32px">
          Không chỉ là trang phục — đó là cách bạn kể câu chuyện của mình. Zestia tin rằng phong cách là sự phản chiếu của tâm hồn.
        </p>
        <RouterLink to="/collections" class="lm-btn-primary align-self-start"><span>Khám phá lookbook</span></RouterLink>
      </div>
    </div>

    <!-- Newsletter -->
    <div style="background:var(--z-dark);padding:80px 40px;text-align:center;border-radius:0">
      <p class="lm-eyebrow mb-3">Cộng Đồng Zestia</p>
      <h2 class="z-display mb-3" style="font-size:44px;font-weight:400;color:var(--z-white)">
        Nhận ưu đãi <em style="font-style:italic;color:var(--z-accent)">độc quyền</em>
      </h2>
      <p style="font-size:14px;font-weight:400;color:rgba(255,255,255,0.5);margin-bottom:32px">
        Đăng ký để nhận thông tin bộ sưu tập mới, ưu đãi thành viên và sự kiện riêng tư.
      </p>
      <div class="d-flex mx-auto" style="max-width:480px;border:1px solid rgba(255,255,255,0.15);border-radius:var(--z-radius);overflow:hidden">
        <input class="flex-grow-1 lm-input" type="email" placeholder="Email của bạn..."
               style="background:transparent;color:var(--z-white);border:none;border-radius:0">
        <button @click="showToast('Đăng ký thành công!')"
                style="padding:16px 28px;background:var(--z-accent);color:var(--z-white);border:none;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--z-font-body);white-space:nowrap">
          Đăng ký
        </button>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import MarqueeStrip    from '@/components/ui/MarqueeStrip.vue'
import ProductCard     from '@/components/ui/ProductCard.vue'
import CollectionCard  from '@/components/ui/CollectionCard.vue'
import AppFooter       from '@/components/layout/AppFooter.vue'
import { useToast }    from '@/composables/useToast'
import { useReveal }   from '@/composables/useReveal'
import { products, loadProducts } from '@/composables/useProducts'

useReveal()
const { showToast } = useToast()

onMounted(() => loadProducts())

const newArrivals = computed(() => products.value.slice(0, 4))
const stats = [
  { num: '6',   label: 'Loại váy' },
  { num: '200', label: 'Thiết kế' },
  { num: '10K', label: 'Khách hàng' },
]

function onScroll() {
  const hero = document.getElementById('hero-parallax')
  if (hero) hero.style.transform = `translateY(${window.scrollY * 0.15}px)`
}
onMounted  (() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
@keyframes z-fade-up {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
