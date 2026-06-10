<template>
  <div>
    <div class="container" style="padding-top:120px;padding-bottom:100px">
      <div class="row g-5">

        <!-- Images -->
        <div class="col-lg-6">
          <div class="d-grid gap-3" style="grid-template-columns:80px 1fr">
            <!-- Thumbnails -->
            <div class="d-flex flex-column gap-2">
              <div v-for="(t, i) in 4" :key="i"
                   class="lm-pd-thumb" :class="{ active: activeThumb === i }"
                   @click="activeThumb = i" style="cursor:pointer;aspect-ratio:3/4;border:2px solid transparent;transition:border-color 0.3s;overflow:hidden"
                   :style="activeThumb === i ? 'border-color:var(--lm-gold)' : 'border-color:transparent'">
                <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                     style="background:linear-gradient(160deg,#EDE6D8,#C5B89A);font-family:var(--lm-font-display);font-size:18px;color:rgba(255,255,255,0.3);font-style:italic">
                  {{ i + 1 }}
                </div>
              </div>
            </div>
            <!-- Main image -->
            <div style="aspect-ratio:3/4;background:linear-gradient(160deg,#E8E0D0,#C5B89A);position:relative;overflow:hidden">
              <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                   style="background:linear-gradient(160deg,#EDE6D8,#C5B89A 60%,#B8A88A);font-family:var(--lm-font-display);font-size:90px;color:rgba(255,255,255,0.15);font-style:italic;font-weight:300">
                LUMIÈRE
              </div>
              <div style="position:absolute;bottom:16px;right:16px;background:rgba(255,255,255,0.85);padding:6px 12px;font-size:8px;font-weight:500;letter-spacing:0.15em;text-transform:uppercase;color:var(--lm-black)">
                Zoom ↗
              </div>
            </div>
          </div>
        </div>

        <!-- Info -->
        <div class="col-lg-6 pt-lg-2">
          <div style="font-size:9px;font-weight:500;letter-spacing:0.35em;text-transform:uppercase;color:var(--lm-gold);margin-bottom:14px">
            LUMIÈRE — Thu Đông 2025
          </div>
          <h1 class="lm-display mb-3" style="font-size:44px;font-weight:300;line-height:1.05;color:var(--lm-black);letter-spacing:-0.01em">
            Silk Wrap<br><em style="font-style:italic;color:var(--lm-gray)">Dress</em>
          </h1>

          <!-- Rating -->
          <div class="d-flex align-items-center gap-2 mb-4">
            <div class="d-flex gap-1">
              <i v-for="s in 5" :key="s" class="bi bi-star-fill"
                 :style="{ color: s <= 4 ? 'var(--lm-gold)' : 'var(--lm-beige-dark)', fontSize:'14px' }"></i>
            </div>
            <span style="font-size:11px;color:var(--lm-gray)">4.2 · 87 đánh giá</span>
          </div>

          <!-- Price -->
          <div class="d-flex align-items-baseline gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--lm-beige)">
            <div class="lm-display" style="font-size:36px;font-weight:300;color:var(--lm-black)">4.290.000₫</div>
            <div style="font-size:18px;color:var(--lm-gray-light);text-decoration:line-through;font-weight:300">5.490.000₫</div>
            <div style="font-size:10px;font-weight:500;letter-spacing:0.1em;color:var(--lm-gold);background:var(--lm-gold-light);padding:4px 10px">Tiết kiệm 22%</div>
          </div>

          <p style="font-size:13px;font-weight:300;line-height:1.9;color:var(--lm-gray);margin-bottom:36px">
            Thiết kế wrap dress từ lụa tự nhiên 100%, nhẹ như sương mù và ôm dịu dàng theo từng đường cong. Phần dây buộc điều chỉnh tạo nên sự linh hoạt — có thể mặc về ngày hay dịp đặc biệt.
          </p>

          <!-- Colors -->
          <div style="font-size:9px;font-weight:600;letter-spacing:0.3em;text-transform:uppercase;color:var(--lm-black);margin-bottom:14px">Màu sắc</div>
          <div class="d-flex gap-2 mb-4">
            <div v-for="(c, i) in colors" :key="i"
                 class="lm-color-swatch"
                 :class="{ active: activeColor === i }"
                 :style="{ background: c.bg, border: c.border || '1px solid transparent', width:'28px', height:'28px', borderRadius:'50%', cursor:'pointer', boxSizing:'border-box' }"
                 :title="c.name"
                 @click="activeColor = i">
            </div>
          </div>

          <!-- Sizes -->
          <div style="font-size:9px;font-weight:600;letter-spacing:0.3em;text-transform:uppercase;color:var(--lm-black);margin-bottom:14px">Kích thước</div>
          <div class="d-flex gap-2 mb-4">
            <button v-for="s in sizes" :key="s.label"
                    class="lm-size-btn"
                    :class="{ active: activeSize === s.label, 'sold-out': s.soldOut }"
                    :disabled="s.soldOut"
                    @click="activeSize = s.label"
                    style="width:44px;height:44px;border:1px solid var(--lm-beige-dark);display:flex;align-items:center;justify-content:center;font-size:11px;cursor:pointer;background:transparent;font-family:var(--lm-font-body);transition:all 0.3s">
              {{ s.label }}
            </button>
          </div>

          <!-- Actions -->
          <div class="d-grid gap-2 mb-4" style="grid-template-columns:1fr 52px">
            <button class="lm-btn-primary justify-content-center" @click="addToCart()">
              <span>Thêm vào giỏ hàng</span>
            </button>
            <button @click="toggleWish"
                    style="border:1px solid var(--lm-beige-dark);background:transparent;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:border-color 0.3s"
                    :style="isLiked ? 'border-color:var(--lm-gold)' : ''">
              <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
                 :style="{ color: isLiked ? 'var(--lm-gold)' : 'var(--lm-black)', fontSize:'20px' }"></i>
            </button>
          </div>

          <!-- Meta -->
          <div class="d-flex flex-column gap-2" style="font-size:11px;color:var(--lm-gray)">
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-truck" style="color:var(--lm-gold)"></i>
              Giao hàng miễn phí cho đơn từ 1.000.000₫
            </div>
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-arrow-repeat" style="color:var(--lm-gold)"></i>
              Đổi trả trong 30 ngày
            </div>
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-shield-check" style="color:var(--lm-gold)"></i>
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
import { ref } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'

const { addItem } = useCart()
const { showToast } = useToast()

const activeThumb = ref(0)
const activeColor = ref(0)
const activeSize  = ref('S')
const isLiked = ref(false)

const colors = [
  { name: 'Kem trắng', bg: '#FAF8F3', border: '1px solid var(--lm-beige-dark)' },
  { name: 'Be đậm',    bg: '#C5B89A' },
  { name: 'Đen',       bg: '#1A1A18' },
  { name: 'Xám',       bg: '#8C8680' },
]
const sizes = [
  { label: 'XS', soldOut: true },
  { label: 'S' }, { label: 'M' }, { label: 'L' }, { label: 'XL' },
]

function addToCart() {
  addItem({ id: 1, name: 'Silk Wrap Dress', variant: `Size ${activeSize.value}`, price: 4290000, letter: 'A', bg: 'linear-gradient(160deg,#EDE6D8,#C5B89A)' })
  showToast('Đã thêm vào giỏ hàng')
}
function toggleWish() {
  isLiked.value = !isLiked.value
  showToast(isLiked.value ? 'Đã thêm vào yêu thích ♥' : 'Đã xóa khỏi yêu thích')
}
</script>

<style scoped>
.lm-size-btn.active      { background: var(--lm-black); color: var(--lm-white); border-color: var(--lm-black); }
.lm-size-btn.sold-out    { opacity: 0.35; cursor: not-allowed; text-decoration: line-through; }
.lm-size-btn:not(.sold-out):not(.active):hover { border-color: var(--lm-black); }
</style>
