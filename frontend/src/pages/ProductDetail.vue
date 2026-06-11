<template>
  <div>
    <div class="container" style="padding-top:100px;padding-bottom:80px">
      <div class="row g-5">

        <!-- Images -->
        <div class="col-lg-6">
          <div class="d-grid gap-3" style="grid-template-columns:72px 1fr">
            <div class="d-flex flex-column gap-2">
              <div v-for="(t, i) in 4" :key="i"
                   @click="activeThumb = i" style="cursor:pointer;aspect-ratio:3/4;overflow:hidden;border-radius:var(--z-radius);transition:all 0.3s"
                   :style="activeThumb === i ? 'box-shadow:0 0 0 2px var(--z-accent)' : ''">
                <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                     style="background:linear-gradient(160deg,#F3E8E6,#D4A99E);font-family:var(--z-font-display);font-size:16px;color:rgba(255,255,255,0.3);font-style:italic">
                  {{ i + 1 }}
                </div>
              </div>
            </div>
            <div style="aspect-ratio:3/4;position:relative;overflow:hidden;border-radius:var(--z-radius-lg)">
              <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                   style="background:linear-gradient(160deg,#F3E8E6,#D4A99E 60%,#C08B7E);font-family:var(--z-font-display);font-size:80px;color:rgba(255,255,255,0.15);font-style:italic;font-weight:300">
                Zestia
              </div>
            </div>
          </div>
        </div>

        <!-- Info -->
        <div class="col-lg-6 pt-lg-2">
          <p class="lm-eyebrow mb-3">Zestia — {{ product.loaiVay || 'Bo Suu Tap' }}</p>
          <h1 class="z-display mb-3" style="font-size:36px;font-weight:400;line-height:1.15;color:var(--z-dark)">
            {{ productName.main }}<br><em style="font-style:italic;color:var(--z-gray)">{{ productName.sub }}</em>
          </h1>

          <div class="d-flex align-items-center gap-2 mb-4">
            <div class="d-flex gap-1">
              <i v-for="s in 5" :key="s" class="bi bi-star-fill"
                 :style="{ color: s <= 4 ? 'var(--z-accent)' : 'var(--z-gray-border)', fontSize:'14px' }"></i>
            </div>
            <span style="font-size:13px;color:var(--z-gray)">4.2 · {{ product.tonKho || 0 }} ton kho</span>
          </div>

          <div class="d-flex align-items-baseline gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
            <div class="z-display" style="font-size:32px;font-weight:500;color:var(--z-dark)">{{ fmtPrice(product.giaBan) }}</div>
            <div v-if="product.giaBanGoc && Number(product.giaBanGoc) > Number(product.giaBan)"
                 style="font-size:16px;color:var(--z-gray-light);text-decoration:line-through;font-weight:400">{{ fmtPrice(product.giaBanGoc) }}</div>
            <div v-if="discountPct"
                 style="font-size:12px;font-weight:600;color:var(--z-accent);background:var(--z-accent-soft);padding:4px 12px;border-radius:20px">-{{ discountPct }}%</div>
          </div>

          <p style="font-size:14px;font-weight:400;line-height:1.8;color:var(--z-gray);margin-bottom:32px">
            {{ product.moTa || 'Thiet ke sang trong, chat lieu cao cap tu Zestia. Phu hop cho ca ngay thuong va dip dac biet.' }}
          </p>

          <!-- Colors -->
          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Mau sac</span>
          </div>
          <div class="d-flex gap-2 mb-4">
            <div v-for="(c, i) in colors" :key="i"
                 :style="{ background: c.bg, border: activeColor === i ? '2px solid var(--z-accent)' : (c.border || '2px solid transparent'), width:'32px', height:'32px', borderRadius:'50%', cursor:'pointer', boxSizing:'border-box', transition:'all 0.2s' }"
                 :title="c.name"
                 @click="activeColor = i">
            </div>
          </div>

          <!-- Sizes -->
          <div class="mb-3">
            <span style="font-size:13px;font-weight:600;color:var(--z-dark)">Kich thuoc</span>
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
            <button class="lm-btn-primary justify-content-center" @click="addToCart()">
              <span>Them vao gio hang</span>
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
              Giao hang mien phi cho don tu 1.000.000d
            </div>
            <div class="d-flex align-items-center gap-3">
              <i class="bi bi-arrow-repeat" style="color:var(--z-accent);font-size:16px"></i>
              Doi tra trong 30 ngay
            </div>
            <div class="d-flex align-items-center gap-3">
              <i class="bi bi-shield-check" style="color:var(--z-accent);font-size:16px"></i>
              Chat lieu lua tu nhien 100% duoc chung nhan
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

const route = useRoute()
const { addItem } = useCart()
const { showToast } = useToast()

const product = ref({})
const activeThumb = ref(0)
const activeColor = ref(0)
const activeSize  = ref('')
const isLiked = ref(false)

const letters = ['Z', 'e', 's', 't', 'i', 'a']
const bgs = [
  'linear-gradient(160deg,#F3E8E6,#D4A99E)',
  'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  'linear-gradient(160deg,#E6E0DA,#A8A49E)',
]

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
  return unique.length ? unique : [{ name: 'Mac dinh', bg: '#D4A99E', border: '2px solid transparent' }]
})

const sizes = computed(() => {
  const bienThe = product.value.bienThe || []
  const unique = []
  const seen = new Set()
  for (const bt of bienThe) {
    if (bt.kichThuoc && !seen.has(bt.kichThuoc)) {
      seen.add(bt.kichThuoc)
      unique.push({ label: bt.kichThuoc, soldOut: bt.soLuong === 0 })
    }
  }
  return unique.length ? unique : [{ label: 'Free', soldOut: false }]
})

onMounted(async () => {
  try {
    const id = route.params.id
    product.value = await api().getVayById(id)
    if (sizes.value.length) activeSize.value = sizes.value.find(s => !s.soldOut)?.label || sizes.value[0].label
  } catch (e) { console.error('Failed to load product:', e) }
})

function addToCart() {
  const p = product.value
  const idx = (p.id || 0) % letters.length
  addItem({
    id: p.id, name: p.tenVay || 'San pham',
    variant: `Size ${activeSize.value}`,
    price: Number(p.giaBan) || 0,
    letter: letters[idx], bg: bgs[idx % bgs.length]
  })
  showToast('Da them vao gio hang')
}
function toggleWish() {
  isLiked.value = !isLiked.value
  showToast(isLiked.value ? 'Da them vao yeu thich' : 'Da xoa khoi yeu thich')
}
</script>

<style scoped>
.z-size-btn.active       { background: var(--z-dark) !important; color: var(--z-white); border-color: var(--z-dark) !important; }
.z-size-btn.sold-out     { opacity: 0.3; cursor: not-allowed !important; text-decoration: line-through; }
.z-size-btn:not(.sold-out):not(.active):hover { border-color: var(--z-dark); }
</style>
