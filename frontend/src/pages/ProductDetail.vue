<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <div class="col-lg-6">
          <div class="d-grid gap-3" style="grid-template-columns:72px 1fr">
            <div class="d-flex flex-column gap-2">
              <div v-for="(img, i) in galleryImages" :key="i"
                   @click="activeThumb = i" style="cursor:pointer;aspect-ratio:3/4;overflow:hidden;border-radius:var(--z-radius);transition:all 0.3s"
                   :style="activeThumb === i ? 'box-shadow:0 0 0 2px var(--z-accent)' : ''">
                <img v-if="img" :src="img" :alt="'Ảnh ' + (i+1)" style="width:100%;height:100%;object-fit:cover" />
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                     style="background:linear-gradient(160deg,#F3E8E6,#D4A99E);font-family:var(--z-font-display);font-size:16px;color:rgba(255,255,255,0.3);font-style:italic">
                  {{ i + 1 }}
                </div>
              </div>
            </div>
            <div style="aspect-ratio:3/4;position:relative;overflow:hidden;border-radius:var(--z-radius-lg)">
              <img v-if="galleryImages[activeThumb]" :src="galleryImages[activeThumb]" :alt="product.tenVay"
                   style="width:100%;height:100%;object-fit:cover" />
              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                   style="background:linear-gradient(160deg,#F3E8E6,#D4A99E 60%,#C08B7E);font-family:var(--z-font-display);font-size:80px;color:rgba(255,255,255,0.15);font-style:italic;font-weight:300">
                Zestia
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-6 pt-lg-2">
          <p class="lm-eyebrow mb-3">Zestia — {{ product.loaiVay || 'Bộ Sưu Tập' }}</p>
          <h1 class="z-display mb-3" style="font-size:36px;font-weight:400;line-height:1.15;color:var(--z-dark)">
            {{ productName.main }}<br><em style="font-style:italic;color:var(--z-gray)">{{ productName.sub }}</em>
          </h1>

          <div class="d-flex align-items-center gap-2 mb-4">
            <div class="d-flex gap-1">
              <i v-for="s in 5" :key="s" class="bi"
                 :class="s <= Math.round(reviewSummary.average || 0) ? 'bi-star-fill' : 'bi-star'"
                 :style="{ color: s <= Math.round(reviewSummary.average || 0) ? 'var(--z-accent)' : 'var(--z-gray-border)', fontSize:'14px' }"></i>
            </div>
            <span data-no-i18n style="font-size:13px;color:var(--z-gray)">{{ ratingStockLabel }}</span>
          </div>

          <div class="d-flex align-items-baseline gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
            <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-dark)">{{ fmtPrice(product.giaBan) }}</div>
            <div v-if="product.dotKhuyenMai"
                 style="font-size:12px;font-weight:600;color:var(--z-accent);background:var(--z-accent-soft);padding:4px 12px;border-radius:20px">
              {{ product.dotKhuyenMai }}
            </div>
          </div>

          <p style="font-size:14px;font-weight:400;line-height:1.8;color:var(--z-gray);margin-bottom:32px">
            {{ product.moTa || 'Thiết kế sang trọng, chất liệu cao cấp từ Zestia. Phù hợp cho cả ngày thường và dịp đặc biệt.' }}
          </p>

          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Màu sắc <span class="text-danger">*</span></span>
          </div>
          <div class="d-flex gap-2 mb-4">
            <div v-for="(c, i) in colors" :key="i"
                 :style="{ background: c.bg, border: activeColor === i ? '2px solid var(--z-accent)' : (c.border || '2px solid transparent'), width:'32px', height:'32px', borderRadius:'50%', cursor:'pointer', boxSizing:'border-box', transition:'all 0.2s' }"
                 :title="c.name"
                 @click="activeColor = i">
            </div>
          </div>

          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Kích thước <span class="text-danger">*</span></span>
          </div>
          <div class="d-flex gap-2 mb-4">
            <button v-for="s in sizes" :key="s.label"
                    class="z-size-btn"
                    :class="{ active: activeSize === s.label, 'sold-out': s.soldOut }"
                    :disabled="s.soldOut"
                    @click="activeSize = s.label"
                    style="width:48px;height:48px;border:1px solid var(--z-gray-border);display:flex;align-items:center;justify-content:center;font-size:13px;cursor:pointer;background:transparent;font-family:var(--z-font-body);transition:all 0.2s;border-radius:var(--z-radius);font-weight:500">
              {{ s.label }}
            </button>
          </div>

          <div class="z-size-guide mb-4">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Gợi ý chọn size</span>
              <span v-if="activeSize" style="font-size:12px;color:var(--z-accent)">Đang chọn {{ activeSize }}</span>
            </div>
            <div class="z-size-guide-grid">
              <div v-for="row in sizeGuideRows" :key="row.size" class="z-size-guide-cell" :class="{ active: activeSize === row.size }">
                <strong>{{ row.size }}</strong>
                <span>{{ row.fit }}</span>
                <small v-if="row.measurements">{{ row.measurements }}</small>
              </div>
            </div>
            <p v-if="product.moTaPhom" class="z-fit-note">{{ product.moTaPhom }}</p>
            <p v-if="product.chieuCaoNguoiMau || product.canNangNguoiMau"
               class="z-model-note" data-no-i18n>{{ modelNoteLabel }}</p>
          </div>

          <div class="d-grid gap-2 mb-4" style="grid-template-columns:1fr 52px">
            <button class="lm-btn-primary justify-content-center" @click="addToCart()">
              <span>Thêm vào giỏ hàng</span>
            </button>
            <button type="button" :aria-label="isLiked ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'" @click="toggleWish"
                    style="border:1px solid var(--z-gray-border);background:transparent;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:all 0.3s;border-radius:var(--z-radius)"
                    :style="isLiked ? 'border-color:var(--z-accent);background:var(--z-accent-soft)' : ''">
              <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
                 :style="{ color: isLiked ? 'var(--z-accent)' : 'var(--z-dark)', fontSize:'20px' }"></i>
            </button>
          </div>

          <div class="d-flex flex-column gap-3" style="font-size:13px;color:var(--z-gray);padding:20px;background:var(--z-bg-alt);border-radius:var(--z-radius-lg)">
            <div v-for="policy in productPolicies" :key="policy.code" class="d-flex align-items-center gap-3">
              <i class="bi" :class="policyIcon(policy.code)" style="color:var(--z-accent);font-size:16px"></i>
              {{ policy.summary }}
            </div>
            <div v-if="product.chatLieu" class="d-flex align-items-center gap-3">
              <i class="bi bi-patch-check" style="color:var(--z-accent);font-size:16px"></i>
              <span data-no-i18n>{{ isEn ? 'Material:' : 'Chất liệu:' }} {{ product.chatLieu }}</span>
            </div>
            <RouterLink to="/policies" class="z-policy-link">Xem đầy đủ điều kiện áp dụng <i class="bi bi-arrow-right"></i></RouterLink>
            <!-- Icons and copy above are sourced from product/policy data. -->
            <div v-if="!productPolicies.length" class="d-flex align-items-center gap-3">
              <i class="bi bi-truck" style="color:var(--z-accent);font-size:16px"></i>
              Chính sách mua hàng đang được cập nhật
            </div>
          </div>
        </div>
      </div>
    </div>

    <section id="reviews" class="z-reviews-section">
      <div class="container">
        <div class="z-review-heading">
          <div>
            <p class="lm-eyebrow mb-2">Đánh giá từ đơn đã giao</p>
            <h2 class="lm-section-title">Người mua <em>chia sẻ</em></h2>
          </div>
          <div class="z-review-score">
            <strong>{{ reviewSummary.count ? Number(reviewSummary.average).toFixed(1) : '–' }}</strong>
            <span>{{ reviewSummary.count || 0 }} đánh giá đã xác minh</span>
          </div>
        </div>

        <div v-if="!isLoggedIn()" class="z-review-login-prompt">
          <div>
            <i class="bi bi-chat-square-heart"></i>
            <div>
              <h3>Đăng nhập để chia sẻ trải nghiệm</h3>
              <p>Chỉ khách hàng đã đăng nhập và đã nhận sản phẩm mới có thể gửi đánh giá.</p>
            </div>
          </div>
          <button class="lm-btn-primary" type="button" @click="goToReviewLogin"><span>Đăng nhập để đánh giá</span></button>
        </div>

        <div v-else-if="reviewEligibility.canReview" class="z-review-form">
          <h3>Đánh giá sản phẩm đã nhận</h3>
          <div class="row g-3">
            <div class="col-md-4">
              <label class="z-review-label">Đơn hàng</label>
              <select v-model="reviewForm.orderId" class="lm-input">
                <option value="">Chọn đơn đã giao</option>
                <option v-for="order in reviewEligibility.orders" :key="order.id" :value="order.id">{{ order.maHoaDon }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="z-review-label">Số sao</label>
              <div class="z-review-star-input">
                <button v-for="star in 5" :key="star" type="button" :title="`${star} sao`" :aria-label="`Đánh giá ${star} sao`" @click="reviewForm.stars = star">
                  <i class="bi" :class="star <= reviewForm.stars ? 'bi-star-fill' : 'bi-star'"></i>
                </button>
              </div>
            </div>
            <div class="col-md-4">
              <label class="z-review-label">Ảnh thực tế (tối đa 3)</label>
              <input class="lm-input" type="file" accept="image/jpeg,image/png" multiple @change="onReviewImages" />
            </div>
            <div class="col-12">
              <label class="z-review-label">Nội dung</label>
              <textarea v-model="reviewForm.content" class="lm-input" rows="4" maxlength="2000" placeholder="Chia sẻ về phom, size, chất liệu và trải nghiệm nhận hàng..."></textarea>
            </div>
          </div>
          <button class="lm-btn-primary mt-3" type="button" :disabled="reviewSubmitting" @click="submitReview">
            <span>{{ reviewSubmitting ? 'Đang gửi...' : 'Gửi đánh giá' }}</span>
          </button>
        </div>

        <div v-else class="z-review-eligibility-note">
          <i class="bi bi-patch-check"></i>
          <span>Bạn có thể đánh giá tại đây sau khi đơn chứa sản phẩm đã được giao thành công.</span>
          <RouterLink to="/my-orders">Xem đơn hàng</RouterLink>
        </div>

        <div v-if="reviews.length" class="d-flex justify-content-end mb-3">
          <PageSizeSelect v-model="reviewPageSize" :options="[3, 6, 12, 24]" />
        </div>
        <div v-if="reviews.length" class="z-review-list">
          <article v-for="review in reviews" :key="review.id" class="z-review-item">
            <div class="z-review-author">
              <strong>{{ review.customerName }}</strong>
              <span><i class="bi bi-patch-check-fill"></i> Đã mua hàng · {{ formatReviewDate(review.createdAt) }}</span>
            </div>
            <div>
              <div class="z-review-stars"><i v-for="star in 5" :key="star" class="bi" :class="star <= review.stars ? 'bi-star-fill' : 'bi-star'"></i></div>
              <p>{{ review.content }}</p>
              <div v-if="review.images?.length" class="z-review-images">
                <img v-for="image in review.images" :key="image" :src="image" alt="Ảnh đánh giá thực tế" loading="lazy" />
              </div>
            </div>
          </article>
        </div>
        <div v-else class="z-review-empty">Chưa có đánh giá cho sản phẩm này.</div>
        <button v-if="reviewPage + 1 < reviewTotalPages" class="lm-btn-secondary z-load-reviews" type="button" @click="loadMoreReviews">Xem thêm đánh giá</button>
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { api, useAuth } from '@/composables/useApi'
import { fmtPrice, products, loadProducts, MOCK_PRODUCTS } from '@/composables/useProducts'
import { useWishlist } from '@/composables/useWishlist'
import { useI18n } from '@/composables/useI18n'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const route = useRoute()
const router = useRouter()
const { addItem } = useCart()
const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()
const { isLoggedIn } = useAuth()
const { isEn } = useI18n()

const product = ref({})
const activeThumb = ref(0)
// Thay đổi: Để mặc định là null để bắt buộc người dùng click chọn
const activeColor = ref(null) 
const activeSize  = ref(null) 
const isLiked = computed(() => isInWishlist(product.value.id))
const policies = ref([])
const reviewSummary = ref({ average: 0, count: 0, distribution: {} })
const reviews = ref([])
const reviewPage = ref(0)
const reviewPageSize = ref(6)
const reviewTotalPages = ref(0)
const reviewEligibility = ref({ canReview: false, orders: [] })
const reviewSubmitting = ref(false)
const reviewForm = ref({ orderId: '', stars: 5, content: '', images: [] })
const ratingStockLabel = computed(() => {
  const stock = Number(product.value.tonKho || 0)
  if (isEn.value) {
    return reviewSummary.value.count
      ? `${Number(reviewSummary.value.average).toFixed(1)} · ${reviewSummary.value.count} reviews · ${stock} in stock`
      : `No reviews yet · ${stock} in stock`
  }
  return reviewSummary.value.count
    ? `${Number(reviewSummary.value.average).toFixed(1)} · ${reviewSummary.value.count} đánh giá · ${stock} tồn kho`
    : `Chưa có đánh giá · ${stock} tồn kho`
})
const modelNoteLabel = computed(() => {
  const parts = [
    product.value.chieuCaoNguoiMau ? `${product.value.chieuCaoNguoiMau}cm` : '',
    product.value.canNangNguoiMau ? `${product.value.canNangNguoiMau}kg` : '',
    product.value.sizeNguoiMau
      ? `${isEn.value ? 'wears size' : 'mặc size'} ${product.value.sizeNguoiMau}`
      : '',
  ].filter(Boolean)
  return `${isEn.value ? 'Model' : 'Người mẫu'}: ${parts.join(' · ')}`
})
const sizeGuideRows = computed(() => (product.value.huongDanSize || []).map(row => ({
  size: row.kichThuoc || row.size || '',
  fit: [row.canNangTu, row.canNangDen].every(value => value != null)
    ? `${row.canNangTu}-${row.canNangDen}kg`
    : (isEn.value ? 'By measurements' : 'Theo số đo'),
  measurements: [
    row.vongNgucTu != null ? `${isEn.value ? 'Bust' : 'Ngực'} ${row.vongNgucTu}-${row.vongNgucDen}` : '',
    row.vongEoTu != null ? `${isEn.value ? 'Waist' : 'Eo'} ${row.vongEoTu}-${row.vongEoDen}` : '',
    row.vongMongTu != null ? `${isEn.value ? 'Hips' : 'Mông'} ${row.vongMongTu}-${row.vongMongDen}` : ''
  ].filter(Boolean).join(' · ')
})))
const productPolicies = computed(() => policies.value.filter(policy => ['SHIPPING', 'SIZE_EXCHANGE', 'RETURN'].includes(policy.code)))

const letters = ['Z', 'e', 's', 't', 'i', 'a']
const bgs = [
  'linear-gradient(160deg,#F3E8E6,#D4A99E)',
  'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  'linear-gradient(160deg,#E6E0DA,#A8A49E)',
]

const galleryImages = computed(() => {
  const variants = (product.value.bienThe || []).filter(variant => Number(variant.trangThai) === 1)
  const colorNames = [...new Set(variants.map(variant => variant.mauSac).filter(Boolean))]
  const colorName = activeColor.value !== null ? colorNames[activeColor.value] : null
  const colorImage = colorName
    ? variants.find(variant => variant.mauSac === colorName && variant.anhUrl)?.anhUrl
    : null
  const productImages = [product.value.anhUrl, ...(product.value.danhSachAnh || [])].filter(Boolean)
  const uniqueImages = [...new Set([colorImage, ...productImages].filter(Boolean))]
  while (uniqueImages.length < 4) uniqueImages.push(null)
  return uniqueImages.slice(0, 4)
})

const productName = computed(() => {
  const name = product.value.tenVay || ''
  const words = name.split(' ')
  if (words.length <= 2) return { main: name, sub: '' }
  const mid = Math.ceil(words.length / 2)
  return { main: words.slice(0, mid).join(' '), sub: words.slice(mid).join(' ') }
})

const activeVariants = computed(() => {
  return (product.value.bienThe || []).filter(v => Number(v.trangThai) === 1 && Number(v.soLuong || 0) > 0)
})

const colors = computed(() => {
  const bienThe = activeVariants.value
  const unique = []
  const seen = new Set()
  for (const bt of bienThe) {
    if (bt.mauSac && !seen.has(bt.mauSac)) {
      seen.add(bt.mauSac)
      const hex = bt.maHex || '#ccc'
      unique.push({
        name: bt.mauSac, bg: hex,
        border: hex.toUpperCase() === '#FFFFFF' || hex.toUpperCase() === '#FFF' ? '2px solid var(--z-gray-border)' : '2px solid transparent'
      })
    }
  }
  return unique.length ? unique : [{ name: 'Mặc định', bg: '#D4A99E', border: '2px solid transparent' }]
})

const sizes = computed(() => {
  const colorName = activeColor.value !== null ? colors.value[activeColor.value]?.name : null
  const bienThe = colorName
    ? activeVariants.value.filter(bt => bt.mauSac === colorName)
    : activeVariants.value
  const unique = []
  const seen = new Set()
  for (const bt of bienThe) {
    if (bt.kichThuoc && !seen.has(bt.kichThuoc)) {
      seen.add(bt.kichThuoc)
      unique.push({ label: bt.kichThuoc, soldOut: Number(bt.soLuong || 0) <= 0 })
    }
  }
  return unique
})

const selectedVariant = computed(() => {
  if (activeColor.value === null || !activeSize.value) return null
  const colorName = colors.value[activeColor.value]?.name
  return activeVariants.value.find(bt => bt.mauSac === colorName && bt.kichThuoc === activeSize.value) || null
})

watch(activeColor, () => {
  activeThumb.value = 0
  if (activeSize.value && !sizes.value.some(s => s.label === activeSize.value && !s.soldOut)) {
    activeSize.value = null
  }
})
watch(reviewPageSize, async () => {
  const data = await api().getProductReviews(route.params.id, 0, reviewPageSize.value)
  applyReviewData(data)
})

onMounted(async () => {
  try {
    const id = route.params.id
    const [productData, reviewData, policyData] = await Promise.all([
      api().getVayById(id).catch(() => null),
      api().getProductReviews(id, 0, reviewPageSize.value).catch(() => null),
      api().getStorePolicies().catch(() => null)
    ])
    if (productData && productData.id) {
      product.value = productData
    } else {
      await loadProducts()
      const found = products.value.find(p => String(p.id) === String(id)) || MOCK_PRODUCTS[0]
      product.value = {
        id: found.id,
        tenVay: found.name || found.tenVay,
        loaiVay: found.category || found.loaiVay,
        chatLieu: found.material || found.chatLieu,
        moTaPhom: found.fit || found.moTaPhom,
        giaBan: found.price || found.giaBan,
        coKhuyenMai: found.promotionActive,
        dotKhuyenMai: found.campaign,
        tonKho: found.stock || found.tonKho || 10,
        trangThai: 1,
        anhUrl: found.image || found.anhUrl,
        danhSachAnh: found.images || found.danhSachAnh || [found.image || found.anhUrl],
        diemDanhGia: found.rating || 5.0,
        soDanhGia: found.reviewCount || 10,
        bienThe: found.bienThe || [
          { id: found.id * 100 + 1, mauSac: 'Trắng Ngà', maHex: '#FFF8F0', kichThuoc: 'S', soLuong: 10, giaBan: found.price || found.giaBan, trangThai: 1, anhUrl: found.image || found.anhUrl },
          { id: found.id * 100 + 2, mauSac: 'Trắng Ngà', maHex: '#FFF8F0', kichThuoc: 'M', soLuong: 15, giaBan: found.price || found.giaBan, trangThai: 1, anhUrl: found.image || found.anhUrl },
          { id: found.id * 100 + 3, mauSac: 'Đen Tuyền', maHex: '#1A1A1A', kichThuoc: 'M', soLuong: 12, giaBan: found.price || found.giaBan, trangThai: 1, anhUrl: (found.images || found.danhSachAnh)?.[1] || found.image }
        ]
      }
    }
    applyReviewData(reviewData)
    policies.value = policyData || []
    if (isLoggedIn()) {
      api().recordCustomerView(id).catch(() => {})
      reviewEligibility.value = await api().getReviewEligibility(id).catch(() => ({ canReview: false, orders: [] }))
    }
  } catch (e) { console.error('Failed to load product:', e) }
})

function applyReviewData(data, append = false) {
  if (!data) return
  reviews.value = append ? [...reviews.value, ...(data.content || [])] : (data.content || [])
  reviewSummary.value = data.summary || reviewSummary.value
  reviewPage.value = Number(data.page || 0)
  reviewTotalPages.value = Number(data.totalPages || 0)
}

async function loadMoreReviews() {
  const data = await api().getProductReviews(route.params.id, reviewPage.value + 1, reviewPageSize.value)
  applyReviewData(data, true)
}

function onReviewImages(event) {
  const files = [...(event.target.files || [])]
  if (files.length > 3) showToast('Mỗi đánh giá được chọn tối đa 3 ảnh')
  reviewForm.value.images = files.slice(0, 3)
}

async function submitReview() {
  if (!isLoggedIn()) return goToReviewLogin()
  if (!reviewForm.value.orderId) return showToast('Vui lòng chọn đơn hàng đã giao')
  if (reviewForm.value.content.trim().length < 10) return showToast('Nội dung đánh giá cần ít nhất 10 ký tự')
  reviewSubmitting.value = true
  try {
    await api().createProductReview(route.params.id, reviewForm.value)
    showToast('Cảm ơn bạn đã gửi đánh giá')
    reviewForm.value = { orderId: '', stars: 5, content: '', images: [] }
    reviewEligibility.value = await api().getReviewEligibility(route.params.id)
    applyReviewData(await api().getProductReviews(route.params.id, 0, reviewPageSize.value))
  } catch (error) {
    showToast(error.error || 'Không thể gửi đánh giá')
  } finally {
    reviewSubmitting.value = false
  }
}

function goToReviewLogin() {
  router.push({ name: 'login', query: { redirect: `${route.path}#reviews` } })
}

function policyIcon(code) {
  return { SHIPPING: 'bi-truck', SIZE_EXCHANGE: 'bi-rulers', RETURN: 'bi-arrow-repeat' }[code] || 'bi-info-circle'
}

function formatReviewDate(value) {
  return value ? new Date(value).toLocaleDateString('vi-VN') : ''
}

function addToCart() {
  // --- BẮT BUỘC CHỌN MÀU VÀ SIZE ---
  if (activeColor.value === null) {
    showToast('Vui lòng chọn Màu sắc trước khi mua!', 'warning')
    return
  }
  if (!activeSize.value) {
    showToast('Vui lòng chọn Kích thước trước khi mua!', 'warning')
    return
  }

  const variantMatch = selectedVariant.value
  if (!variantMatch) {
    showToast('Biến thể này không khả dụng hoặc đã hết hàng!', 'warning')
    return
  }

  const p = product.value
  const idx = (p.id || 0) % letters.length
  const colorName = colors.value[activeColor.value]?.name || ''
  const currentPrice = Number(variantMatch.giaBan ?? p.giaBan) || 0
  
  // TẠO ID DUY NHẤT ĐỂ GIỎ HÀNG KHÔNG GỘP CHUNG SẢN PHẨM KHÁC SIZE/MÀU
  const uniqueCartId = `variant-${variantMatch.id}`

  addItem({
    id: uniqueCartId,       // ID ảo để tách giỏ hàng
    productId: p.id,        // ID gốc bắt buộc phải có cho Backend
    name: p.tenVay || 'Sản phẩm',
    size: activeSize.value, // Lưu size vào giỏ
    color: colorName,       // Lưu màu vào giỏ
    variant: [colorName, `Size ${activeSize.value}`].filter(Boolean).join(' · '),
    price: currentPrice,
    maxQty: Number(variantMatch.soLuong || 0),
    variantId: variantMatch.id,
    image: variantMatch.anhUrl || p.anhUrl || p.danhSachAnh?.[0] || null,
    letter: letters[idx], 
    bg: bgs[idx % bgs.length]
  })
  showToast('Đã thêm vào giỏ hàng', 'success')
}

function toggleWish() {
  const added = toggleWishlist(product.value.id)
  showToast(added ? 'Đã thêm vào yêu thích' : 'Đã xoá khỏi yêu thích')
}
</script>

<style scoped>
.z-size-btn.active       { background: var(--z-dark) !important; color: var(--z-white); border-color: var(--z-dark) !important; }
.z-size-btn.sold-out     { opacity: 0.3; cursor: not-allowed !important; text-decoration: line-through; }
.z-size-btn:not(.sold-out):not(.active):hover { border-color: var(--z-dark); }
.z-size-guide {
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  padding: 12px;
  background: var(--z-bg-alt);
}
.z-size-guide-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(108px, 1fr)); gap: 8px; }
.z-size-guide-cell {
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  padding: 8px 6px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.z-size-guide-cell strong { font-size: 13px; color: var(--z-dark); }
.z-size-guide-cell span { font-size: 11px; color: var(--z-gray); white-space: nowrap; }
.z-size-guide-cell small { color: var(--z-gray); font-size: 9px; line-height: 1.45; }
.z-size-guide-cell.active { border-color: var(--z-accent); background: var(--z-accent-soft); }
.z-fit-note, .z-model-note { margin: 10px 0 0; color: var(--z-gray); font-size: 11px; line-height: 1.6; }
.z-policy-link { color: var(--z-accent); font-size: 12px; font-weight: 600; text-decoration: none; }
.z-reviews-section { padding: 72px 0; background: var(--z-bg-alt); }
.z-review-heading { display: flex; justify-content: space-between; align-items: end; gap: 24px; margin-bottom: 38px; }
.z-review-score { text-align: right; }
.z-review-score strong { display: block; color: var(--z-dark); font-family: var(--z-font-body); font-size: 40px; font-variant-numeric: tabular-nums; }
.z-review-score span { color: var(--z-gray); font-size: 12px; }
.z-review-form { margin-bottom: 32px; padding: 24px; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-review-login-prompt, .z-review-eligibility-note { display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 32px; padding: 22px 24px; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-review-login-prompt > div { display: flex; align-items: center; gap: 16px; }
.z-review-login-prompt > div > i { color: var(--z-accent); font-size: 26px; }
.z-review-login-prompt h3 { margin: 0 0 4px; font-size: 16px; }
.z-review-login-prompt p { margin: 0; color: var(--z-gray); font-size: 13px; }
.z-review-eligibility-note { justify-content: flex-start; color: var(--z-gray); font-size: 13px; }
.z-review-eligibility-note i { color: var(--z-accent); font-size: 18px; }
.z-review-eligibility-note a { margin-left: auto; color: var(--z-dark); font-weight: 600; }
.z-review-form h3 { margin-bottom: 18px; font-size: 16px; }
.z-review-label { display: block; margin-bottom: 7px; font-size: 12px; font-weight: 600; }
.z-review-star-input { display: flex; min-height: 43px; align-items: center; }
.z-review-star-input button { border: 0; background: transparent; color: var(--z-accent); font-size: 20px; }
.z-review-list { border-top: 1px solid var(--z-gray-border); }
.z-review-item { display: grid; grid-template-columns: minmax(180px, .35fr) 1fr; gap: 32px; padding: 30px 0; border-bottom: 1px solid var(--z-gray-border); }
.z-review-author strong { display: block; font-size: 13px; }
.z-review-author span { color: var(--z-gray); font-size: 11px; }
.z-review-author i, .z-review-stars { color: var(--z-accent); }
.z-review-item p { margin: 12px 0; color: var(--z-dark); line-height: 1.7; }
.z-review-images { display: flex; gap: 10px; flex-wrap: wrap; }
.z-review-images img { width: 96px; aspect-ratio: 1; object-fit: cover; border: 1px solid var(--z-gray-border); }
.z-review-empty { padding: 40px 0; border-top: 1px solid var(--z-gray-border); color: var(--z-gray); text-align: center; }
.z-load-reviews { display: block; margin: 24px auto 0; }
@media (max-width: 700px) {
  .z-review-heading { align-items: start; flex-direction: column; }
  .z-review-score { text-align: left; }
  .z-review-item { grid-template-columns: 1fr; gap: 12px; }
  .z-review-login-prompt, .z-review-eligibility-note { align-items: stretch; flex-direction: column; }
  .z-review-eligibility-note a { margin-left: 0; }
}
</style>
