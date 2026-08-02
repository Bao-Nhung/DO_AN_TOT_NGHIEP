<template>
  <div>
    <section class="z-home-hero">
      <img id="hero-parallax" class="z-home-hero-img" src="/images/banners/banner1.png" alt="Zestia Fashion">
      <div class="z-home-hero-shade"></div>

      <div class="container z-home-hero-content">
        <p class="lm-eyebrow mb-3">Zestia Collection 2026</p>
        <h1 class="z-display mb-4">
          Váy dành cho những khoảnh khắc bạn muốn được nhớ đến
        </h1>
        <p class="z-home-hero-copy mb-5">
          Từ dáng công sở thanh lịch đến váy dự tiệc nổi bật, Zestia chọn màu sắc, chất liệu và phom dáng để bạn bước vào ngày của mình với sự tự tin rất riêng.
        </p>
        <div class="d-flex gap-3 align-items-center flex-wrap">
          <RouterLink to="/collections" class="lm-btn-primary"><span>Khám phá bộ sưu tập</span></RouterLink>
          <RouterLink to="/about" class="lm-btn-outline-light">Câu chuyện Zestia</RouterLink>
        </div>
      </div>

      <div class="z-hero-proof">
        <div><strong>{{ storefront.activeProductCount || 0 }}</strong><span>Sản phẩm đang bán</span></div>
        <div><strong>{{ ratingLabel }}</strong><span>{{ storefront.reviewCount || 0 }} đánh giá đã mua</span></div>
        <div><strong>{{ sizeExchangeLabel }}</strong><span>Hỗ trợ đổi size</span></div>
      </div>
    </section>

    <MarqueeStrip :items="marqueeItems" />

    <section class="z-service-strip">
      <div v-for="item in serviceHighlights" :key="item.title" class="z-service-item lm-reveal">
        <i class="bi" :class="item.icon"></i>
        <div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.text }}</p>
        </div>
      </div>
    </section>

    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal flex-wrap gap-3">
          <div>
            <p class="lm-eyebrow mb-2">Danh mục</p>
            <h2 class="lm-section-title">Chọn váy theo <em>khoảnh khắc</em></h2>
          </div>
          <RouterLink to="/collections" class="lm-btn-secondary flex-shrink-0">
            Tất cả sản phẩm <i class="bi bi-arrow-right ms-1"></i>
          </RouterLink>
        </div>

        <div class="row g-3">
          <div class="col-lg-5 lm-reveal">
            <CollectionCard label="Lễ kỷ niệm" name="Váy Dự Lễ" letter="Z"
                            image="/images/products/dress6.jpg"
                            bg="linear-gradient(160deg,#F3E8E6,#D4A99E 40%,#C08B7E)" to="/lookbook" tall />
          </div>
          <div class="col-lg-7">
            <div class="row g-3">
              <div class="col-6 lm-reveal"><CollectionCard label="Hẹn hò" name="Váy Hẹn Hò" letter="e" image="/images/products/dress17.jpg" bg="linear-gradient(160deg,#E8DDD6,#C4A98E)" to="/lookbook" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Dự tiệc" name="Váy Dự Tiệc" letter="s" image="/images/products/dress9.jpg" bg="linear-gradient(160deg,#E6E0DA,#A8A49E)" to="/collections?occasion=party" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Cưới hỏi" name="Váy Cưới" letter="t" image="/images/products/dress11.jpg" bg="linear-gradient(160deg,#F0E8E0,#D4C0A8)" to="/collections?occasion=wedding" /></div>
              <div class="col-6 lm-reveal"><CollectionCard label="Đi làm" name="Váy Đi Làm" letter="ia" image="/images/products/dress19.jpg" bg="linear-gradient(160deg,#E4DDD2,#C0B49E)" to="/collections?occasion=work" /></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="z-style-edit lm-reveal">
      <div class="z-style-media">
        <img src="/images/banners/banner5.png" alt="Zestia Style Edit">
      </div>
      <div class="z-style-content">
        <p class="lm-eyebrow mb-3">Style Edit</p>
        <h2 class="z-display">Một chiếc váy đúng có thể đổi cả tinh thần của ngày hôm đó</h2>
        <p>
          Zestia gợi ý sản phẩm theo hoàn cảnh sử dụng: gặp khách hàng, dự tiệc tối, chụp ảnh kỷ niệm hay những ngày bạn chỉ muốn thật nhẹ nhàng.
        </p>
        <div class="z-style-tags">
          <span>Công sở</span>
          <span>Dự tiệc</span>
          <span>Hẹn hò</span>
          <span>Lookbook</span>
        </div>
        <RouterLink to="/lookbook" class="lm-btn-primary align-self-start"><span>Tìm phong cách của tôi</span></RouterLink>
      </div>
    </section>

    <section v-if="bestsellers.length" class="lm-section z-bestseller-band">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal flex-wrap gap-3">
          <div>
            <p class="lm-eyebrow mb-2">Từ đơn hàng đã hoàn thành</p>
            <h2 class="lm-section-title">Được chọn <em>nhiều nhất</em></h2>
          </div>
          <RouterLink to="/collections?sort=bestseller" class="lm-btn-secondary">Xem bộ lọc bán chạy</RouterLink>
        </div>
        <div class="row g-4">
          <div v-for="item in bestsellers" :key="item.product.id" class="col-6 col-lg-3 lm-reveal">
            <ProductCard :product="item.product" />
            <p class="z-sales-note">{{ item.soldQuantity }} sản phẩm đã giao</p>
          </div>
        </div>
      </div>
    </section>

    <section class="lm-section">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 lm-reveal flex-wrap gap-3">
          <div>
            <p class="lm-eyebrow mb-2">Vừa ra mắt</p>
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

    <section v-if="recentlyViewed.length" class="lm-section z-recent-band">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 flex-wrap gap-3">
          <div>
            <p class="lm-eyebrow mb-2">Theo tài khoản của bạn</p>
            <h2 class="lm-section-title">Sản phẩm <em>đã xem</em></h2>
          </div>
          <RouterLink to="/profile" class="lm-btn-secondary">Quản lý tài khoản</RouterLink>
        </div>
        <div class="row g-4">
          <div v-for="product in recentlyViewed" :key="product.id" class="col-6 col-lg-3">
            <ProductCard :product="product" />
          </div>
        </div>
      </div>
    </section>

    <section v-if="storefront.reviewHighlights?.length" class="z-review-band">
      <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5 flex-wrap gap-3">
          <div>
            <p class="lm-eyebrow mb-2">Người mua nói gì</p>
            <h2 class="lm-section-title">Trải nghiệm <em>đã xác minh</em></h2>
          </div>
          <RouterLink to="/reviews" class="lm-btn-secondary">Xem tất cả đánh giá <i class="bi bi-arrow-right ms-1"></i></RouterLink>
        </div>
        <div class="row g-3">
          <div v-for="review in storefront.reviewHighlights.slice(0, 3)" :key="review.id" class="col-md-4">
            <article class="z-review-quote">
              <div class="z-review-stars" :aria-label="`${review.stars} trên 5 sao`">
                <i v-for="star in 5" :key="star" class="bi" :class="star <= review.stars ? 'bi-star-fill' : 'bi-star'"></i>
              </div>
              <p>“{{ review.content }}”</p>
              <strong>{{ review.customerName }}</strong>
              <RouterLink :to="`/product/${review.productId}`">{{ review.productName }}</RouterLink>
            </article>
          </div>
        </div>
      </div>
    </section>

    <section v-if="luckyCampaign?.active" class="z-lucky-home-band">
      <div class="z-lucky-home-mark"><img src="/images/brand/zestia-mark.png" alt="" aria-hidden="true"></div>
      <div>
        <p class="lm-eyebrow mb-2">Quà tặng tháng 8</p>
        <h2 class="z-display">{{ luckyCampaign.tenChienDich }}</h2>
        <p>Đơn đã giao thành công từ {{ formatMoney(luckyCampaign.giaTriDonToiThieu) }} nhận một lượt quay với quà hiện vật tại showroom.</p>
      </div>
      <RouterLink to="/lucky-wheel" class="lm-btn-primary"><i class="bi bi-stars"></i><span>Kiểm tra lượt quay</span></RouterLink>
    </section>

    <section class="z-brand-band">
      <div>
        <p class="lm-eyebrow mb-3">Về Zestia</p>
        <h2 class="z-display">Tỏa sáng theo <em>cách của bạn</em></h2>
        <p>
          Mỗi thiết kế là một câu chuyện về phụ nữ Việt: tự tin, tinh tế, linh hoạt và có gu thẩm mỹ riêng.
        </p>
        <RouterLink to="/about" class="lm-btn-outline-light">Xem thêm về chúng tôi</RouterLink>
      </div>
      <div class="z-brand-stats">
        <div v-for="stat in stats" :key="stat.label">
          <strong>{{ stat.num }}<span>+</span></strong>
          <p>{{ stat.label }}</p>
        </div>
      </div>
    </section>

    <section class="z-newsletter">
      <p class="lm-eyebrow mb-3">Cộng đồng Zestia</p>
      <h2 class="z-display">Nhận ưu đãi <em>độc quyền</em></h2>
      <p>Đăng ký để nhận bộ sưu tập mới, voucher thành viên và gợi ý phối đồ theo mùa.</p>
      <form class="z-newsletter-form" @submit.prevent="subscribeNewsletter">
        <input v-model="newsletterEmail" class="lm-input" type="email" placeholder="Email của bạn..." :disabled="newsletterLoading">
        <button type="submit" :disabled="newsletterLoading">
          {{ newsletterLoading ? 'Đang gửi...' : 'Đăng ký' }}
        </button>
      </form>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import MarqueeStrip from '@/components/ui/MarqueeStrip.vue'
import ProductCard from '@/components/ui/ProductCard.vue'
import CollectionCard from '@/components/ui/CollectionCard.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useToast } from '@/composables/useToast'
import { useReveal } from '@/composables/useReveal'
import { products, loadProducts } from '@/composables/useProducts'
import { api, useAuth } from '@/composables/useApi'

useReveal()
const { showToast } = useToast()
const newsletterEmail = ref('')
const newsletterLoading = ref(false)
const recentProductIds = ref([])
const luckyCampaign = ref(null)
const { isLoggedIn, getUser } = useAuth()
const storefront = reactive({
  activeProductCount: 0,
  categoryCount: 0,
  customerCount: 0,
  completedOrderCount: 0,
  averageRating: 0,
  reviewCount: 0,
  bestsellers: [],
  reviewHighlights: [],
  policies: []
})

const newArrivals = computed(() => products.value.slice(0, 4))
const bestsellers = computed(() => (storefront.bestsellers || []).map(sale => ({
  soldQuantity: Number(sale.soldQuantity || 0),
  product: products.value.find(product => Number(product.id) === Number(sale.productId))
})).filter(item => item.product).slice(0, 4))
const recentlyViewed = computed(() => recentProductIds.value
  .map(id => products.value.find(product => Number(product.id) === Number(id)))
  .filter(Boolean)
  .slice(0, 4))
const ratingLabel = computed(() => storefront.reviewCount
  ? `${Number(storefront.averageRating || 0).toFixed(1)}/5`
  : 'Chưa có')
const sizeExchangeLabel = computed(() => {
  const policy = storefront.policies?.find(item => item.code === 'SIZE_EXCHANGE')
  return policy?.numericValue ? `${policy.numericValue}${policy.unit === 'giờ' ? 'h' : ' ' + policy.unit}` : 'Theo chính sách'
})
const marqueeItems = computed(() => [
  'Bộ Sưu Tập Mới',
  ...(storefront.policies || []).slice(0, 4).map(policy => policy.title),
  'Zestia Fashion'
])
const serviceHighlights = computed(() => [
  { icon: 'bi-rulers', title: 'Tư vấn chọn size', text: 'Giảm rủi ro đổi trả khi mua váy online.' },
  { icon: 'bi-stars', title: 'Chất liệu chọn lọc', text: 'Ưu tiên bề mặt vải đẹp, dễ mặc và lên dáng.' },
  { icon: 'bi-arrow-repeat', title: 'Đổi size linh hoạt', text: `${sizeExchangeLabel.value} cho sản phẩm đủ điều kiện.` },
  { icon: 'bi-gift', title: 'Voucher thành viên', text: 'Hiển thị mã đang hoạt động ngay khi thanh toán.' },
])
const stats = computed(() => [
  { num: storefront.categoryCount || 0, label: 'Dòng sản phẩm' },
  { num: storefront.activeProductCount || 0, label: 'Sản phẩm đang bán' },
  { num: storefront.customerCount || 0, label: 'Tài khoản khách hàng' },
])

function formatMoney(value) { return Number(value || 0).toLocaleString('vi-VN') + 'đ' }

onMounted(async () => {
  await Promise.all([
    loadProducts(),
    api().getStorefrontSummary().then(data => Object.assign(storefront, data || {})).catch(error => {
      console.warn('Không tải được số liệu trang chủ', error)
    }),
    api().getLuckyWheelCampaign().then(data => { luckyCampaign.value = data }).catch(() => {})
  ])
  if (isLoggedIn() && getUser()?.role === 'KhachHang') {
    try {
      const data = await api().getCustomerData()
      recentProductIds.value = data?.recentProductIds || []
    } catch (error) {
      console.warn('Không tải được sản phẩm đã xem', error)
    }
  }
  window.addEventListener('scroll', onScroll)
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

function onScroll() {
  const hero = document.getElementById('hero-parallax')
  if (hero) hero.style.transform = `translateY(${window.scrollY * 0.08}px) scale(1.04)`
}

async function subscribeNewsletter() {
  const email = newsletterEmail.value.trim()
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showToast('Vui lòng nhập email hợp lệ')
    return
  }
  newsletterLoading.value = true
  try {
    const res = await api().subscribeNewsletter(email)
    showToast(res?.message || 'Đăng ký nhận tin thành công!')
    newsletterEmail.value = ''
  } catch (err) {
    showToast(err.error || 'Không thể đăng ký nhận tin')
  } finally {
    newsletterLoading.value = false
  }
}
</script>

<style scoped>
@keyframes z-fade-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.z-home-hero {
  position: relative;
  min-height: max(620px, calc(100svh - 118px));
  overflow: hidden;
  display: flex;
  align-items: center;
  background: var(--z-dark);
}
.z-home-hero-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 110%;
  object-fit: cover;
  transform: scale(1.04);
  transform-origin: center;
  filter: saturate(0.95) contrast(1.05);
}
.z-home-hero-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(10, 10, 12, 0.82), rgba(10, 10, 12, 0.46) 48%, rgba(10, 10, 12, 0.18));
}
.z-home-hero-content {
  position: relative;
  z-index: 2;
  color: var(--z-white);
  padding: 64px 12px 138px;
}
.z-home-hero-content h1 {
  max-width: 780px;
  font-size: 72px;
  font-weight: 400;
  line-height: 1.02;
  color: var(--z-white);
  animation: z-fade-up 0.8s ease 0.25s both;
}
.z-home-hero-copy {
  max-width: 560px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 16px;
  line-height: 1.8;
  animation: z-fade-up 0.8s ease 0.45s both;
}
.z-hero-proof {
  position: absolute;
  z-index: 2;
  left: 50%;
  bottom: 26px;
  transform: translateX(-50%);
  width: min(1120px, calc(100% - 32px));
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-top: 1px solid rgba(255, 255, 255, 0.22);
  border-bottom: 1px solid rgba(255, 255, 255, 0.22);
  color: var(--z-white);
}
.z-hero-proof div {
  padding: 16px 22px;
  border-right: 1px solid rgba(255, 255, 255, 0.16);
}
.z-hero-proof div:last-child {
  border-right: 0;
}
.z-hero-proof strong {
  display: block;
  font-size: 24px;
  line-height: 1;
}
.z-hero-proof span {
  display: block;
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.66);
  font-size: 12px;
  text-transform: uppercase;
}

.z-service-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  background: var(--z-gray-border);
}
.z-service-item {
  display: flex;
  gap: 14px;
  padding: 28px;
  background: var(--z-white);
}
.z-service-item i {
  color: var(--z-accent);
  font-size: 24px;
  flex-shrink: 0;
}
.z-service-item h3 {
  margin: 0 0 5px;
  color: var(--z-dark);
  font-size: 15px;
  font-weight: 700;
}
.z-service-item p {
  margin: 0;
  color: var(--z-gray);
  font-size: 13px;
  line-height: 1.6;
}
.z-bestseller-band { background: var(--z-bg-alt); }
.z-recent-band { border-top: 1px solid var(--z-gray-border); }
.z-sales-note {
  margin: 8px 0 0;
  color: var(--z-gray);
  font-size: 12px;
}
.z-review-band {
  padding: 76px 0;
  background: var(--z-white);
}
.z-review-quote {
  height: 100%;
  padding: 24px 0;
  border-top: 1px solid var(--z-dark);
}
.z-review-stars { color: var(--z-accent); font-size: 12px; }
.z-review-quote p { min-height: 76px; margin: 18px 0; color: var(--z-dark); line-height: 1.7; }
.z-review-quote strong { display: block; font-size: 13px; }
.z-review-quote a { color: var(--z-gray); font-size: 12px; text-decoration: none; }

.z-style-edit {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  min-height: 560px;
  background: var(--z-bg-alt);
}
.z-style-media {
  overflow: hidden;
}
.z-style-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.z-style-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 72px;
}
.z-style-content h2 {
  max-width: 620px;
  margin-bottom: 22px;
  color: var(--z-dark);
  font-size: 44px;
  font-weight: 400;
  line-height: 1.12;
}
.z-style-content p {
  max-width: 560px;
  color: var(--z-gray);
  font-size: 15px;
  line-height: 1.8;
}
.z-style-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0 30px;
}
.z-style-tags span {
  padding: 8px 12px;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 12px;
  font-weight: 600;
}

.z-lucky-home-band {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) auto;
  align-items: center;
  gap: 28px;
  padding: 38px 64px;
  border-block: 1px solid var(--z-gray-border);
  background: var(--z-white);
}
.z-lucky-home-mark {
  width: 84px;
  height: 84px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--z-accent-soft);
}
.z-lucky-home-mark img { width: 68px; height: 68px; object-fit: contain; }
.z-lucky-home-band h2 { margin: 0 0 7px; color: var(--z-dark); font-size: 30px; font-weight: 500; }
.z-lucky-home-band > div:nth-child(2) > p:last-child { margin: 0; color: var(--z-gray); font-size: 13px; line-height: 1.6; }

.z-brand-band {
  display: flex;
  justify-content: space-between;
  gap: 48px;
  padding: 78px 64px;
  background: var(--z-dark);
  color: var(--z-white);
}
.z-brand-band h2 {
  max-width: 680px;
  margin-bottom: 18px;
  font-size: 48px;
  font-weight: 400;
  line-height: 1.08;
}
.z-brand-band h2 em,
.z-newsletter h2 em {
  color: var(--z-accent);
  font-style: italic;
}
.z-brand-band p {
  max-width: 520px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;
  margin-bottom: 30px;
}
.z-brand-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 24px;
  align-content: center;
}
.z-brand-stats strong {
  color: var(--z-white);
  font-size: 40px;
  line-height: 1;
}
.z-brand-stats strong span {
  color: var(--z-accent);
  font-size: 22px;
}
.z-brand-stats p {
  margin: 8px 0 0;
  font-size: 11px;
  text-transform: uppercase;
}

.z-newsletter {
  padding: 80px 32px;
  text-align: center;
  background: var(--z-bg);
}
.z-newsletter h2 {
  color: var(--z-dark);
  font-size: 44px;
  font-weight: 400;
}
.z-newsletter > p:not(.lm-eyebrow) {
  color: var(--z-gray);
  margin: 10px auto 30px;
  max-width: 560px;
}
.z-newsletter-form {
  display: flex;
  max-width: 520px;
  margin: 0 auto;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  overflow: hidden;
  background: var(--z-white);
}
.z-newsletter-form input {
  border: none;
  border-radius: 0;
}
.z-newsletter-form button {
  padding: 0 28px;
  border: 0;
  background: var(--z-dark);
  color: var(--z-white);
  font-weight: 700;
}
.z-newsletter-form button:hover {
  background: var(--z-accent);
  color: var(--z-white);
}
.z-newsletter-form button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .z-home-hero-content h1 { font-size: 48px; }
  .z-hero-proof strong { font-size: 20px; }
  .z-hero-proof div { padding: 14px; }
  .z-service-strip { grid-template-columns: repeat(2, 1fr); }
  .z-style-edit { grid-template-columns: 1fr; }
  .z-style-media { min-height: 420px; }
  .z-lucky-home-band { grid-template-columns: 72px 1fr; padding: 34px 28px; }
  .z-lucky-home-band > .lm-btn-primary { grid-column: 2; justify-self: start; }
  .z-lucky-home-mark { width: 68px; height: 68px; }
  .z-lucky-home-mark img { width: 54px; height: 54px; }
  .z-brand-band { flex-direction: column; padding: 56px 28px; }
}

@media (max-width: 576px) {
  .z-home-hero { min-height: calc(100svh - 100px); }
  .z-home-hero-content { padding: 38px 16px; }
  .z-home-hero-content h1 { font-size: 36px; }
  .z-home-hero-copy { font-size: 14px; }
  .z-hero-proof { display: none; }
  .z-service-strip { grid-template-columns: 1fr; }
  .z-style-content { padding: 44px 24px; }
  .z-lucky-home-band { grid-template-columns: 1fr; text-align: center; justify-items: center; padding: 38px 20px; }
  .z-lucky-home-band > .lm-btn-primary { grid-column: 1; justify-self: stretch; justify-content: center; }
  .z-style-content h2,
  .z-brand-band h2,
  .z-newsletter h2 { font-size: 32px; }
  .z-brand-stats { grid-template-columns: 1fr; }
  .z-newsletter-form { flex-direction: column; }
  .z-newsletter-form button { padding: 14px 24px; }
}
</style>
