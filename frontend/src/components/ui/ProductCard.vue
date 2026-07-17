<template>
  <article class="lm-product-card" role="link" tabindex="0"
           :aria-label="`Xem ${product.name}`"
           @click="$router.push('/product/' + product.id)"
           @keydown.enter="$router.push('/product/' + product.id)"
           @keydown.space.prevent="$router.push('/product/' + product.id)">
    <div class="lm-product-image">
      <div v-if="product.image" class="lm-product-img-inner">
        <img :src="product.image" :alt="product.name" loading="lazy" />
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

      <div class="lm-product-quick" aria-hidden="true">Tùy chọn</div>
    </div>

    <div class="lm-product-cat">{{ product.category }}</div>
    <div class="lm-product-name">{{ product.name }}</div>
    <div class="lm-product-price">{{ formatPrice(product.price) }}</div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { useWishlist } from '@/composables/useWishlist'

const props = defineProps({
  product: { type: Object, required: true }
})

// Bỏ hàm addItem, chỉ giữ lại formatPrice
const { formatPrice } = useCart()
const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()

const isLiked = computed(() => isInWishlist(props.product.id))

function toggleWish() {
  const added = toggleWishlist(props.product.id)
  showToast(added ? 'Đã thêm vào yêu thích' : 'Đã xoá khỏi yêu thích')
}
</script>

<style scoped>
@media (hover: none) {
  .lm-product-wish { opacity: 1; }
  .lm-product-quick { bottom: 0; }
}
</style>
