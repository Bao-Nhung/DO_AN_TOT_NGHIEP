<template>
  <article class="lm-product-card" role="link" tabindex="0"
           :aria-label="`Xem ${product.name}`"
           @click="$router.push('/product/' + product.id)"
           @keydown.enter="$router.push('/product/' + product.id)"
           @keydown.space.prevent="$router.push('/product/' + product.id)">
    <div class="lm-product-image">
      <div v-if="imgSrc" class="lm-product-img-inner">
        <img :src="imgSrc" :alt="product.name" loading="lazy" @error="onImgError" />
      </div>
      <div v-else class="lm-product-img-inner" :style="{ background: product.bg }">
        {{ product.letter }}
      </div>

      <div v-if="product.badge" class="lm-product-badge" :class="{ sale: product.badge === 'Sale' }">
        {{ product.badge }}
      </div>

      <button class="lm-product-wish" :class="{ liked: isLiked }"
              :aria-label="isLiked ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'"
              @click.stop="toggleWish">
        <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
           :style="{ color: isLiked ? 'var(--z-accent)' : 'var(--z-dark)', fontSize:'14px' }"></i>
      </button>
      <button
        class="lm-product-compare"
        :class="{ active: isCompared }"
        :aria-label="isCompared ? 'Bỏ khỏi so sánh' : 'Thêm vào so sánh'"
        :title="isCompared ? 'Bỏ khỏi so sánh' : 'Thêm vào so sánh'"
        @click.stop="toggleComparison"
      >
        <i class="bi bi-columns-gap"></i>
      </button>

      <div class="lm-product-quick" aria-hidden="true">Tùy chọn</div>
    </div>

    <div class="lm-product-cat">{{ product.category }}</div>
    <div class="lm-product-name">{{ product.name }}</div>
    <ProductPrice
      class="lm-product-price"
      :price="product.price"
      :original-price="product.basePrice"
      size="card"
    />
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import { useWishlist } from '@/composables/useWishlist'
import { useCompare } from '@/composables/useCompare'
import ProductPrice from '@/components/ui/ProductPrice.vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const imgSrc = ref(props.product.image)
watch(() => props.product.image, newImg => { imgSrc.value = newImg })

function onImgError() {
  const cat = String(props.product.category || '').toLowerCase()
  if (cat.includes('quần') || cat.includes('jeans')) imgSrc.value = '/images/products/catalog-v2/sp002_main.webp'
  else if (cat.includes('áo khoác') || cat.includes('blazer')) imgSrc.value = '/images/products/catalog-v2/sp011_main.webp'
  else if (cat.includes('áo') || cat.includes('sơ mi')) imgSrc.value = '/images/products/catalog-v2/sp001_main.webp'
  else if (cat.includes('phụ kiện')) imgSrc.value = '/images/products/catalog-v2/sp048_main.webp'
  else if (cat.includes('công sở')) imgSrc.value = '/images/products/catalog-v2/sp021_main.webp'
  else imgSrc.value = '/images/products/catalog-v2/sp003_main.webp'
}

const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()
const { hasProduct, toggleProduct, maxProducts } = useCompare()

const isLiked = computed(() => isInWishlist(props.product.id))
const isCompared = computed(() => hasProduct(props.product.id))

function toggleWish() {
  const added = toggleWishlist(props.product.id)
  showToast(added ? 'Đã thêm vào yêu thích' : 'Đã xoá khỏi yêu thích')
}

function toggleComparison() {
  const result = toggleProduct(props.product.id)
  if (result.reason === 'limit') {
    showToast(`Chỉ có thể so sánh tối đa ${maxProducts} sản phẩm`)
    return
  }
  showToast(result.added ? 'Đã thêm vào so sánh' : 'Đã bỏ khỏi so sánh')
}
</script>

<style scoped>
@media (hover: none) {
  .lm-product-wish { opacity: 1; }
  .lm-product-compare { opacity: 1; }
  .lm-product-quick { bottom: 0; }
}

.lm-product-compare {
  position: absolute; top: 56px; right: 12px;
  width: 36px; height: 36px; border: 0; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: var(--z-white); color: var(--z-dark);
  box-shadow: 0 2px 8px rgba(0,0,0,.08);
  opacity: 0; cursor: pointer; transition: var(--z-ease);
}
.lm-product-card:hover .lm-product-compare,
.lm-product-compare:focus-visible { opacity: 1; }
.lm-product-compare:hover { background: var(--z-accent-soft); color: var(--z-accent-dark); transform: translateY(-1px); }
.lm-product-compare.active { background: var(--z-dark); color: var(--z-white); opacity: 1; }
</style>
