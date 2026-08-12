<template>
  <div class="z-reviews-page">
    <header class="z-reviews-hero">
      <div class="container">
        <p class="lm-eyebrow mb-3">Cộng đồng Zestia</p>
        <h1 class="z-display">Trải nghiệm từ <em>người mua thật</em></h1>
        <p>Mỗi đánh giá hiển thị tại đây đều gắn với một đơn hàng đã giao thành công.</p>
      </div>
    </header>

    <section class="z-review-overview">
      <div class="container">
        <div class="z-review-summary">
          <div class="z-review-average">
            <strong>{{ summary.count ? Number(summary.average).toFixed(1) : '–' }}</strong>
            <div class="z-review-stars" :aria-label="`${summary.average || 0} trên 5 sao`">
              <i v-for="star in 5" :key="star" class="bi" :class="star <= Math.round(summary.average || 0) ? 'bi-star-fill' : 'bi-star'"></i>
            </div>
            <span>{{ summary.count || 0 }} đánh giá đã xác minh</span>
          </div>

          <div class="z-rating-bars">
            <button v-for="star in [5, 4, 3, 2, 1]" :key="star" type="button" :class="{ active: selectedStars === star }" @click="selectStars(star)">
              <span>{{ star }} sao</span>
              <span class="z-rating-track"><span :style="{ width: ratingPercent(star) + '%' }"></span></span>
              <strong>{{ summary.distribution?.[star] || 0 }}</strong>
            </button>
          </div>

          <div class="z-review-action">
            <template v-if="!isLoggedIn()">
              <h2>Bạn đã mua hàng tại Zestia?</h2>
              <p>Đăng nhập, mở sản phẩm đã nhận và chia sẻ đánh giá của bạn.</p>
              <RouterLink :to="{ name: 'login', query: { redirect: '/reviews' } }" class="lm-btn-primary"><span>Đăng nhập để đánh giá</span></RouterLink>
            </template>
            <template v-else>
              <h2>Chia sẻ trải nghiệm của bạn</h2>
              <p>Chọn sản phẩm trong đơn đã giao để viết đánh giá kèm ảnh thực tế.</p>
              <RouterLink to="/my-orders" class="lm-btn-primary"><span>Xem đơn đã giao</span></RouterLink>
            </template>
          </div>
        </div>
      </div>
    </section>

    <section class="lm-section z-review-feed">
      <div class="container">
        <div class="z-review-feed-heading">
          <div>
            <p class="lm-eyebrow mb-2">Chia sẻ mới nhất</p>
            <h2 class="lm-section-title">Khách hàng <em>nói gì</em></h2>
          </div>
          <button v-if="selectedStars" class="z-clear-filter" type="button" @click="selectStars(null)">
            <i class="bi bi-x-lg"></i> Bỏ lọc {{ selectedStars }} sao
          </button>
        </div>

        <div v-if="loading" class="z-review-loading" aria-live="polite">
          <span class="spinner-border spinner-border-sm"></span> Đang tải đánh giá...
        </div>

        <div v-else-if="reviews.length" class="z-review-grid">
          <article v-for="review in reviews" :key="review.id" class="z-store-review">
            <div class="z-store-review-head">
              <span class="z-review-avatar">{{ customerInitial(review.customerName) }}</span>
              <div>
                <strong>{{ review.customerName }}</strong>
                <span><i class="bi bi-patch-check-fill"></i> Đã mua hàng · {{ formatDate(review.createdAt) }}</span>
              </div>
            </div>
            <div class="z-review-stars">
              <i v-for="star in 5" :key="star" class="bi" :class="star <= review.stars ? 'bi-star-fill' : 'bi-star'"></i>
            </div>
            <p class="z-review-content">“{{ review.content }}”</p>
            <div v-if="review.images?.length" class="z-review-photos">
              <img v-for="image in review.images" :key="image" :src="image" alt="Ảnh thực tế từ khách hàng" loading="lazy" />
            </div>
            <RouterLink :to="`/product/${review.productId}`" class="z-reviewed-product">
              <img v-if="review.productImage" :src="review.productImage" :alt="review.productName" loading="lazy" />
              <span v-else class="z-product-placeholder">Z</span>
              <span><small>Đánh giá cho</small><strong>{{ review.productName }}</strong></span>
              <i class="bi bi-arrow-right"></i>
            </RouterLink>
          </article>
        </div>

        <div v-else class="z-review-empty-state">
          <i class="bi bi-chat-square-text"></i>
          <h3>Chưa có đánh giá phù hợp</h3>
          <p>Hãy chọn mức sao khác hoặc quay lại xem toàn bộ chia sẻ.</p>
          <button v-if="selectedStars" class="lm-btn-secondary" type="button" @click="selectStars(null)">Xem tất cả đánh giá</button>
        </div>

        <div v-if="reviews.length" class="d-flex justify-content-end mt-4">
          <PageSizeSelect v-model="pageSize" :options="[6, 9, 18, 36]" />
        </div>
        <nav v-if="totalPages > 1" class="z-review-pagination" aria-label="Phân trang đánh giá">
          <button type="button" class="z-page-button" :disabled="page === 0" title="Trang trước" aria-label="Trang đánh giá trước" @click="changePage(page - 1)"><i class="bi bi-chevron-left"></i></button>
          <button v-for="number in visiblePages" :key="number" type="button" :class="{ active: page === number }" @click="changePage(number)">{{ number + 1 }}</button>
          <button type="button" class="z-page-button" :disabled="page + 1 >= totalPages" title="Trang sau" aria-label="Trang đánh giá sau" @click="changePage(page + 1)"><i class="bi bi-chevron-right"></i></button>
        </nav>
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'
import { api, useAuth } from '@/composables/useApi'

const { isLoggedIn } = useAuth()
const reviews = ref([])
const loading = ref(true)
const page = ref(0)
const pageSize = ref(9)
const totalPages = ref(0)
const selectedStars = ref(null)
const summary = reactive({ average: 0, count: 0, distribution: {} })

const visiblePages = computed(() => {
  const start = Math.max(0, Math.min(page.value - 2, totalPages.value - 5))
  const count = Math.min(5, totalPages.value)
  return Array.from({ length: count }, (_, index) => start + index)
})

onMounted(loadReviews)
watch(pageSize, () => {
  page.value = 0
  loadReviews()
})

async function loadReviews() {
  loading.value = true
  try {
    const data = await api().getStoreReviews(page.value, pageSize.value, selectedStars.value || '')
    reviews.value = data?.content || []
    totalPages.value = Number(data?.totalPages || 0)
    Object.assign(summary, data?.summary || {})
  } catch (error) {
    console.warn('Không tải được đánh giá', error)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

function selectStars(stars) {
  selectedStars.value = selectedStars.value === stars ? null : stars
  page.value = 0
  loadReviews()
}

function changePage(nextPage) {
  if (nextPage < 0 || nextPage >= totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadReviews().then(() => {
    const feed = document.querySelector('.z-review-feed')
    if (feed) window.scrollTo({ top: feed.offsetTop - 80, behavior: 'smooth' })
  })
}

function ratingPercent(star) {
  const count = Number(summary.count || 0)
  return count ? Math.round((Number(summary.distribution?.[star] || 0) / count) * 100) : 0
}

function customerInitial(name) {
  return String(name || 'Z').trim().charAt(0).toUpperCase()
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : ''
}
</script>

<style scoped>
.z-reviews-hero { padding: 86px 0 72px; color: var(--z-white); background: var(--z-dark); }
.z-reviews-hero .container { max-width: 920px; margin-left: max(calc((100vw - 1320px) / 2), 24px); }
.z-reviews-hero h1 { max-width: 780px; margin: 0 0 20px; font-size: 54px; font-weight: 400; }
.z-reviews-hero h1 em { color: var(--z-accent); font-weight: 400; }
.z-reviews-hero > .container > p:last-child { max-width: 620px; margin: 0; color: rgba(255,255,255,.66); line-height: 1.7; }
.z-review-overview { border-bottom: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-review-summary { display: grid; grid-template-columns: .72fr 1fr 1.35fr; align-items: center; min-height: 230px; }
.z-review-summary > * { min-width: 0; padding: 34px 40px; }
.z-review-summary > * + * { border-left: 1px solid var(--z-gray-border); }
.z-review-average strong { display: block; font-family: var(--z-font-body); font-size: 58px; font-weight: 600; line-height: 1; font-variant-numeric: tabular-nums; }
.z-review-average > span { display: block; margin-top: 8px; color: var(--z-gray); font-size: 12px; }
.z-review-stars { color: var(--z-accent); letter-spacing: 0; }
.z-rating-bars { display: flex; flex-direction: column; gap: 7px; }
.z-rating-bars button { display: grid; grid-template-columns: 48px 1fr 28px; align-items: center; gap: 10px; padding: 3px 6px; border: 0; background: transparent; color: var(--z-gray); font-size: 11px; text-align: left; }
.z-rating-bars button:hover, .z-rating-bars button.active { color: var(--z-dark); background: var(--z-bg-alt); }
.z-rating-track { height: 4px; overflow: hidden; background: var(--z-gray-border); }
.z-rating-track span { display: block; height: 100%; background: var(--z-accent); }
.z-rating-bars strong { text-align: right; }
.z-review-action h2 { margin: 0 0 8px; font-family: var(--z-font-display); font-size: 24px; font-weight: 500; }
.z-review-action p { max-width: 390px; margin: 0 0 18px; color: var(--z-gray); font-size: 13px; line-height: 1.6; }
.z-review-feed { background: var(--z-bg-alt); }
.z-review-feed-heading { display: flex; justify-content: space-between; align-items: end; gap: 20px; margin-bottom: 36px; }
.z-clear-filter { padding: 10px 14px; border: 1px solid var(--z-gray-border); background: var(--z-white); color: var(--z-dark); font-size: 12px; }
.z-review-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.z-store-review { display: flex; min-height: 390px; flex-direction: column; padding: 24px; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-store-review-head { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; }
.z-review-avatar { display: grid; width: 42px; height: 42px; flex: 0 0 42px; place-items: center; border-radius: 50%; background: var(--z-dark); color: var(--z-white); font-family: var(--z-font-display); font-size: 18px; }
.z-store-review-head strong, .z-store-review-head span { display: block; }
.z-store-review-head strong { font-size: 13px; }
.z-store-review-head div > span { margin-top: 3px; color: var(--z-gray); font-size: 10px; }
.z-store-review-head i { color: var(--z-accent); }
.z-review-content { flex: 1; margin: 16px 0; color: var(--z-dark); font-size: 14px; line-height: 1.75; }
.z-review-photos { display: flex; gap: 8px; margin-bottom: 18px; }
.z-review-photos img { width: 70px; aspect-ratio: 1; object-fit: cover; border: 1px solid var(--z-gray-border); }
.z-reviewed-product { display: grid; grid-template-columns: 48px 1fr 18px; align-items: center; gap: 12px; padding-top: 16px; border-top: 1px solid var(--z-gray-border); color: var(--z-dark); text-decoration: none; }
.z-reviewed-product > img, .z-product-placeholder { width: 48px; height: 58px; object-fit: cover; background: var(--z-bg-alt); }
.z-product-placeholder { display: grid; place-items: center; color: var(--z-accent); font-family: var(--z-font-display); }
.z-reviewed-product small, .z-reviewed-product strong { display: block; }
.z-reviewed-product small { color: var(--z-gray); font-size: 10px; }
.z-reviewed-product strong { margin-top: 2px; overflow: hidden; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.z-reviewed-product > i { transition: transform .2s ease; }
.z-reviewed-product:hover > i { transform: translateX(3px); }
.z-review-loading, .z-review-empty-state { padding: 80px 20px; color: var(--z-gray); text-align: center; }
.z-review-loading span { margin-right: 8px; }
.z-review-empty-state > i { color: var(--z-accent); font-size: 36px; }
.z-review-empty-state h3 { margin: 14px 0 6px; color: var(--z-dark); font-size: 18px; }
.z-review-pagination { display: flex; justify-content: center; gap: 6px; margin-top: 38px; }
.z-review-pagination button { width: 38px; height: 38px; border: 1px solid var(--z-gray-border); background: var(--z-white); color: var(--z-dark); }
.z-review-pagination button:hover:not(:disabled), .z-review-pagination button.active { border-color: var(--z-dark); background: var(--z-dark); color: var(--z-white); }
.z-review-pagination button:disabled { cursor: not-allowed; opacity: .4; }
@media (max-width: 991px) {
  .z-review-summary { grid-template-columns: .8fr 1.2fr; }
  .z-review-action { grid-column: 1 / -1; border-top: 1px solid var(--z-gray-border); border-left: 0 !important; }
  .z-review-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 767px) {
  .z-reviews-hero { padding: 58px 0 52px; }
  .z-reviews-hero .container { margin-left: auto; }
  .z-reviews-hero h1 { font-size: 36px; }
  .z-review-summary { grid-template-columns: 1fr; }
  .z-review-summary > * { padding: 28px 20px; }
  .z-review-summary > * + * { border-top: 1px solid var(--z-gray-border); border-left: 0; }
  .z-review-action { grid-column: auto; }
  .z-review-grid { grid-template-columns: 1fr; }
  .z-review-feed-heading { align-items: start; flex-direction: column; }
  .z-store-review { min-height: 0; }
}
</style>
