<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <!-- Images -->
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

        <!-- Info -->
        <div class="col-lg-6 pt-lg-2">
          <p class="lm-eyebrow mb-2">Zestia — {{ product.loaiVay || 'Bộ Sưu Tập' }}</p>
          <p style="font-size:12px;color:var(--z-gray);letter-spacing:1px;text-transform:uppercase;margin-bottom:12px">MÃ SP: {{ product.maVay }}</p>
          <h1 class="z-display mb-3" style="font-size:36px;font-weight:400;line-height:1.15;color:var(--z-dark)">
            {{ productName.main }}<br><em style="font-style:italic;color:var(--z-gray)">{{ productName.sub }}</em>
          </h1>

          <div class="d-flex align-items-center gap-2 mb-4">
            <div class="d-flex gap-1">
              <i v-for="s in 5" :key="s" class="bi bi-star-fill"
                 :style="{ color: s <= 4 ? 'var(--z-accent)' : 'var(--z-gray-border)', fontSize:'14px' }"></i>
            </div>
            <span style="font-size:13px;color:var(--z-gray)">4.2 · {{ selectedVariant ? selectedVariant.soLuong : (product.tonKho || 0) }} tồn kho</span>
          </div>

          <div class="d-flex align-items-baseline gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
            <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-dark)">{{ fmtPrice(selectedVariant ? selectedVariant.giaBan : product.giaBan) }}</div>
            <div v-if="(selectedVariant ? selectedVariant.giaBanGoc : product.giaBanGoc) && Number(selectedVariant ? selectedVariant.giaBanGoc : product.giaBanGoc) > Number(selectedVariant ? selectedVariant.giaBan : product.giaBan)"
                 style="font-size:16px;color:var(--z-gray-light);text-decoration:line-through;font-weight:400">{{ fmtPrice(selectedVariant ? selectedVariant.giaBanGoc : product.giaBanGoc) }}</div>
            <div v-if="discountPct"
                 style="font-size:12px;font-weight:600;color:var(--z-accent);background:var(--z-accent-soft);padding:4px 12px;border-radius:20px">-{{ discountPct }}%</div>
          </div>

          <p style="font-size:14px;font-weight:400;line-height:1.8;color:var(--z-gray);margin-bottom:32px">
            {{ product.moTa || 'Thiết kế sang trọng, chất liệu cao cấp từ Zestia. Phù hợp cho cả ngày thường và dịp đặc biệt.' }}
          </p>

          <!-- Colors -->
          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Màu sắc: <span style="font-weight:400;color:var(--z-gray)">{{ activeColor || 'Chưa chọn' }}</span></span>
          </div>
          <div class="d-flex gap-2 mb-4">
            <div v-for="c in colors" :key="c.name"
                 :style="{ background: c.bg, border: activeColor === c.name ? '2px solid var(--z-accent)' : (c.border || '2px solid transparent'), width:'32px', height:'32px', borderRadius:'50%', cursor:'pointer', boxSizing:'border-box', transition:'all 0.2s' }"
                 :title="c.name"
                 @click="activeColor = c.name">
            </div>
          </div>

          <!-- Sizes -->
          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Kích thước: <span style="font-weight:400;color:var(--z-gray)">{{ activeSize || 'Chưa chọn' }}</span></span>
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

          <!-- Actions -->
          <div class="d-grid gap-2 mb-4" style="grid-template-columns:1fr 52px">
            <button class="lm-btn-primary justify-content-center" @click="addToCart()" :disabled="isAdding">
              <span>{{ isAdding ? 'Đang thêm...' : 'Thêm vào giỏ hàng' }}</span>
            </button>
            <button @click="toggleWish"
                    style="border:1px solid var(--z-gray-border);background:transparent;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:all 0.3s;border-radius:var(--z-radius)"
                    :style="isLiked ? 'border-color:var(--z-accent);background:var(--z-accent-soft)' : ''">
              <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
                 :style="{ color: isLiked ? 'var(--z-accent)' : 'var(--z-dark)', fontSize:'20px' }"></i>
            </button>
          </div>

          <div class="d-flex flex-column gap-3" style="font-size:13px;color:var(--z-gray);padding:20px;background:var(--z-bg-alt);border-radius:var(--z-radius-lg)">
            <div class="d-flex align-items-center gap-3">
              <i class="bi bi-truck" style="color:var(--z-accent);font-size:16px"></i>
              Giao hàng miễn phí cho đơn từ 1.000.000đ
            </div>
            <div class="d-flex align-items-center gap-3">
              <i class="bi bi-arrow-repeat" style="color:var(--z-accent);font-size:16px"></i>
              Đổi trả trong 30 ngày
            </div>
            <div class="d-flex align-items-center gap-3">
              <i class="bi bi-shield-check" style="color:var(--z-accent);font-size:16px"></i>
              Chất liệu lụa tự nhiên 100% được chứng nhận
            </div>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'
import { useWishlist } from '@/composables/useWishlist'

const route = useRoute()
const { addItem, openCart } = useCart()
const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()

const product = ref({})
const activeThumb = ref(0)
const activeColor = ref(null)
const activeSize  = ref(null)
const isAdding = ref(false)
const isLiked = computed(() => isInWishlist(product.value.id))

const letters = ['Z', 'e', 's', 't', 'i', 'a']
const bgs = [
  'linear-gradient(160deg,#F3E8E6,#D4A99E)',
  'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  'linear-gradient(160deg,#E6E0DA,#A8A49E)',
]

const galleryImages = computed(() => {
  const imgs = product.value.danhSachAnh || []
  if (imgs.length >= 4) return imgs.slice(0, 4)
  const main = product.value.anhUrl
  if (main) {
    const arr = [main, ...imgs.filter(u => u !== main)]
    while (arr.length < 4) arr.push(null)
    return arr.slice(0, 4)
  }
  return [null, null, null, null]
})

const productName = computed(() => {
  const name = product.value.tenVay || ''
  const words = name.split(' ')
  if (words.length <= 2) return { main: name, sub: '' }
  const mid = Math.ceil(words.length / 2)
  return { main: words.slice(0, mid).join(' '), sub: words.slice(mid).join(' ') }
})

const selectedVariant = computed(() => {
  if (!activeColor.value || !activeSize.value) return null
  const bienThe = product.value.bienThe || []
  return bienThe.find(bt => bt.mauSac === activeColor.value && bt.kichThuoc === activeSize.value) || null
})

const discountPct = computed(() => {
  const orig = Number(selectedVariant.value ? selectedVariant.value.giaBanGoc : product.value.giaBanGoc)
  const cur = Number(selectedVariant.value ? selectedVariant.value.giaBan : product.value.giaBan)
  if (!orig || orig <= cur) return 0
  return Math.round((1 - cur / orig) * 100)
})

const colors = computed(() => {
  const bienThe = product.value.bienThe || []
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
  const bienThe = product.value.bienThe || []
  const unique = []
  const seen = new Set()
  for (const bt of bienThe) {
    if (bt.kichThuoc && !seen.has(bt.kichThuoc)) {
      seen.add(bt.kichThuoc)
      // Nếu đã chọn màu, check xem size này với màu đó có còn hàng không
      let isSoldOut = false
      if (activeColor.value) {
        const variant = bienThe.find(v => v.mauSac === activeColor.value && v.kichThuoc === bt.kichThuoc)
        isSoldOut = !variant || variant.soLuong <= 0
      } else {
        isSoldOut = bt.soLuong <= 0
      }
      unique.push({ label: bt.kichThuoc, soldOut: isSoldOut })
    }
  }
  return unique.length ? unique : [{ label: 'Free', soldOut: false }]
})

onMounted(async () => {
  try {
    const id = route.params.id
    product.value = await api().getVayById(id)
  } catch (e) { console.error('Failed to load product:', e) }
})

async function addToCart() {
  if (!activeColor.value) {
    return showToast('Vui lòng chọn màu sắc!')
  }
  if (!activeSize.value) {
    return showToast('Vui lòng chọn kích thước!')
  }
  const variant = selectedVariant.value
  if (!variant) {
    return showToast('Biến thể này không tồn tại!')
  }
  if (variant.soLuong <= 0) {
    return showToast('Sản phẩm đã hết hàng!')
  }
  
  const p = product.value
  const idx = (p.id || 0) % letters.length
  
  isAdding.value = true
  try {
    await addItem({
      idVayChiTiet: variant.id,
      maVayChiTiet: variant.maVayChiTiet,
      name: p.tenVay || 'Sản phẩm',
      size: activeSize.value,
      color: activeColor.value,
      price: Number(variant.giaBan) || 0,
      image: p.anhUrl || null,
      letter: letters[idx], 
      bg: bgs[idx % bgs.length],
      qty: 1
    })
    showToast('Đã thêm vào giỏ hàng')
    openCart()
  } catch (e) {
    showToast(e.error || 'Lỗi thêm vào giỏ hàng')
  } finally {
    isAdding.value = false
  }
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
</style>
