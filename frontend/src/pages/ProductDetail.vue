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
              <i v-for="s in 5" :key="s" class="bi bi-star-fill"
                 :style="{ color: s <= 4 ? 'var(--z-accent)' : 'var(--z-gray-border)', fontSize:'14px' }"></i>
            </div>
            <span style="font-size:13px;color:var(--z-gray)">4.2 · {{ product.tonKho || 0 }} tồn kho</span>
          </div>

          <div class="d-flex align-items-baseline gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
            <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-dark)">{{ fmtPrice(product.giaBan) }}</div>
            <div v-if="product.giaBanGoc && Number(product.giaBanGoc) > Number(product.giaBan)"
                 style="font-size:16px;color:var(--z-gray-light);text-decoration:line-through;font-weight:400">{{ fmtPrice(product.giaBanGoc) }}</div>
            <div v-if="discountPct"
                 style="font-size:12px;font-weight:600;color:var(--z-accent);background:var(--z-accent-soft);padding:4px 12px;border-radius:20px">-{{ discountPct }}%</div>
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
            <div class="d-grid gap-2" style="grid-template-columns:repeat(4,1fr)">
              <div v-for="row in sizeGuideRows" :key="row.size" class="z-size-guide-cell" :class="{ active: activeSize === row.size }">
                <strong>{{ row.size }}</strong>
                <span>{{ row.fit }}</span>
              </div>
            </div>
          </div>

          <div class="d-grid gap-2 mb-4" style="grid-template-columns:1fr 52px">
            <button class="lm-btn-primary justify-content-center" @click="addToCart()">
              <span>Thêm vào giỏ hàng</span>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'
import { useWishlist } from '@/composables/useWishlist'

const route = useRoute()
const { addItem } = useCart()
const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()

const product = ref({})
const activeThumb = ref(0)
// Thay đổi: Để mặc định là null để bắt buộc người dùng click chọn
const activeColor = ref(null) 
const activeSize  = ref(null) 
const isLiked = computed(() => isInWishlist(product.value.id))
const sizeGuideRows = [
  { size: 'S', fit: '40-48kg' },
  { size: 'M', fit: '49-56kg' },
  { size: 'L', fit: '57-64kg' },
  { size: 'XL', fit: '65-72kg' },
]

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

const discountPct = computed(() => {
  const orig = Number(product.value.giaBanGoc)
  const cur = Number(product.value.giaBan)
  if (!orig || orig <= cur) return 0
  return Math.round((1 - cur / orig) * 100)
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
  if (activeSize.value && !sizes.value.some(s => s.label === activeSize.value && !s.soldOut)) {
    activeSize.value = null
  }
})

onMounted(async () => {
  try {
    const id = route.params.id
    product.value = await api().getVayById(id)
    // Đã xóa phần auto-select size để ép người dùng phải chọn
  } catch (e) { console.error('Failed to load product:', e) }
})

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
  
  // TẠO ID DUY NHẤT ĐỂ GIỎ HÀNG KHÔNG GỘP CHUNG SẢN PHẨM KHÁC SIZE/MÀU
  const uniqueCartId = `variant-${variantMatch.id}`

  addItem({
    id: uniqueCartId,       // ID ảo để tách giỏ hàng
    productId: p.id,        // ID gốc bắt buộc phải có cho Backend
    name: p.tenVay || 'Sản phẩm',
    size: activeSize.value, // Lưu size vào giỏ
    color: colorName,       // Lưu màu vào giỏ
    variant: [colorName, `Size ${activeSize.value}`].filter(Boolean).join(' · '),
    price: Number(variantMatch.giaBan || p.giaBan) || 0,
    maxQty: Number(variantMatch.soLuong || 0),
    variantId: variantMatch.id,
    image: p.anhUrl || null,
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
.z-size-guide-cell.active { border-color: var(--z-accent); background: var(--z-accent-soft); }
</style>
