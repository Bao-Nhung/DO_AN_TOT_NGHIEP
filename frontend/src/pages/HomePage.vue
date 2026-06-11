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
        <p class="lm-eyebrow mb-3" style="animation:z-fade-up 0.8s ease 0.3s both">Bo Suu Tap Moi 2025</p>
        <h1 class="z-display mb-4" style="
          font-size:clamp(52px,7vw,96px);font-weight:400;line-height:1;
          color:var(--z-dark);animation:z-fade-up 0.9s ease 0.5s both">
          Phong cach<br>
          <em style="font-style:italic;color:var(--z-gray)">cua rieng</em><br>
          <span style="color:var(--z-accent)">ban</span>
        </h1>
        <p class="mb-5" style="font-size:15px;font-weight:400;line-height:1.7;color:var(--z-gray);max-width:420px;animation:z-fade-up 0.8s ease 0.7s both">
          Zestia mang den nhung thiet ke vay doc dao, pha tron giua net truyen thong va xu huong hien dai — giup ban toa sang moi ngay.
        </p>
        <div class="d-flex gap-3 align-items-center flex-wrap" style="animation:z-fade-up 0.8s ease 0.9s both">
          <RouterLink to="/collections" class="lm-btn-primary"><span>Kham pha ngay</span></RouterLink>
          <RouterLink to="/collections" class="lm-btn-secondary">
            Xem bo suu tap <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>
      </div>

      <div class="position-absolute top-0 end-0 h-100 overflow-hidden d-none d-lg-block" style="width:46%">
        <div class="w-100 h-100 d-flex align-items-center justify-content-center"
             style="background:linear-gradient(135deg,#F3E8E6,#E8CFC9 40%,#D4A99E 100%);
                    font-family:var(--z-font-display);font-size:140px;color:rgba(255,255,255,0.15);font-weight:300"
             id="hero-parallax">
          Z
        </div>
      </div>

      <div class="position-absolute d-none d-lg-flex align-items-center gap-3"
           style="bottom:40px;left:60px;font-size:12px;font-weight:500;color:var(--z-gray);animation:z-fade-up 0.8s ease 1.2s both">
        <div style="width:48px;height:2px;background:var(--z-accent);border-radius:1px"></div>
        Cuon xuong
      </div>
    </section>

    <MarqueeStrip />

    <!-- Collections -->
    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal">
          <div>
            <p class="lm-eyebrow mb-2">Danh Muc</p>
            <h2 class="lm-section-title">Kham pha <em>bo suu tap</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Tat ca san pham <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>

        <div class="row g-3">
          <div class="col-lg-5 lm-reveal">
            <CollectionCard label="Moi nhat" name="Vay Lua Truyen Thong" letter="Z"
                            bg="linear-gradient(160deg,#F3E8E6,#D4A99E 40%,#C08B7E)" tall />
          </div>
          <div class="col-lg-7">
            <div class="row g-3">
              <div class="col-6 lm-reveal"><CollectionCard label="Best seller" name="Vay Cach Tan" letter="e" bg="linear-gradient(160deg,#E8DDD6,#C4A98E)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Sale" name="Vay Da Hoi" letter="s" bg="linear-gradient(160deg,#E6E0DA,#A8A49E)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Gioi han" name="Vay Cuoi" letter="t" bg="linear-gradient(160deg,#F0E8E0,#D4C0A8)" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Moi" name="Vay Cong So" letter="ia" bg="linear-gradient(160deg,#E4DDD2,#C0B49E)" /></div>
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
        <p class="lm-eyebrow mb-3">Ve Zestia</p>
        <h2 class="z-display mb-3" style="font-size:48px;font-weight:400;line-height:1.1;color:var(--z-white)">
          Toa sang theo <em style="font-style:italic;color:var(--z-accent)">cach cua ban</em>
        </h2>
        <p style="font-size:14px;font-weight:400;line-height:1.8;color:rgba(255,255,255,0.5);max-width:400px;margin-bottom:36px">
          Moi thiet ke cua Zestia la mot cau chuyen ve phu nu Viet — tu tin, nang dong va day suc sang tao.
        </p>
        <a class="lm-btn-outline-light">Xem them ve chung toi</a>
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
            <p class="lm-eyebrow mb-2">Vua Ra Mat</p>
            <h2 class="lm-section-title">Hang moi <em>thang nay</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Xem them <i class="bi bi-arrow-right ms-1"></i>
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
        <div style="aspect-ratio:4/5;background:linear-gradient(160deg,#E8CFC9,#D4A99E);
                    display:flex;align-items:center;justify-content:center;
                    font-family:var(--z-font-display);font-size:72px;color:rgba(255,255,255,0.12);font-style:italic">
          Lookbook
        </div>
      </div>
      <div class="col-lg-6 lm-reveal-right d-flex flex-column justify-content-center"
           style="background:var(--z-bg-alt);padding:80px">
        <p class="lm-eyebrow mb-3">Phong Cach Song</p>
        <div class="lm-divider-gold"></div>
        <h2 class="z-display mb-4" style="font-size:38px;font-weight:400;line-height:1.15;color:var(--z-dark)">
          Thoi trang la <em style="font-style:italic;color:var(--z-gray)">ngon ngu</em> cua ban
        </h2>
        <p style="font-size:14px;font-weight:400;line-height:1.8;color:var(--z-gray);margin-bottom:32px">
          Khong chi la trang phuc — do la cach ban ke cau chuyen cua minh. Zestia tin rang phong cach la su phan chieu cua tam hon.
        </p>
        <RouterLink to="/collections" class="lm-btn-primary align-self-start"><span>Kham pha lookbook</span></RouterLink>
      </div>
    </div>

    <!-- Newsletter -->
    <div style="background:var(--z-dark);padding:80px 40px;text-align:center;border-radius:0">
      <p class="lm-eyebrow mb-3">Cong Dong Zestia</p>
      <h2 class="z-display mb-3" style="font-size:44px;font-weight:400;color:var(--z-white)">
        Nhan uu dai <em style="font-style:italic;color:var(--z-accent)">doc quyen</em>
      </h2>
      <p style="font-size:14px;font-weight:400;color:rgba(255,255,255,0.5);margin-bottom:32px">
        Dang ky de nhan thong tin bo suu tap moi, uu dai thanh vien va su kien rieng tu.
      </p>
      <div class="d-flex mx-auto" style="max-width:480px;border:1px solid rgba(255,255,255,0.15);border-radius:var(--z-radius);overflow:hidden">
        <input class="flex-grow-1 lm-input" type="email" placeholder="Email cua ban..."
               style="background:transparent;color:var(--z-white);border:none;border-radius:0">
        <button @click="showToast('Dang ky thanh cong!')"
                style="padding:16px 28px;background:var(--z-accent);color:var(--z-white);border:none;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--z-font-body);white-space:nowrap">
          Dang ky
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
  { num: '6',   label: 'Loai vay' },
  { num: '200', label: 'Thiet ke' },
  { num: '10K', label: 'Khach hang' },
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
