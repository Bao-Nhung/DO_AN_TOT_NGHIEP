<template>
  <div class="lm-product-card" @click="$router.push('/product/' + product.id)">
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
              @click.stop="toggleWish">
        <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
           :style="{ color: isLiked ? 'var(--z-accent)' : 'var(--z-dark)', fontSize:'14px' }"></i>
      </button>

      <div class="lm-product-quick" @click.stop="addCart">Thêm vào giỏ</div>
    </div>

    <div class="lm-product-cat">{{ product.category }}</div>
    <div class="lm-product-name">{{ product.name }}</div>
    <div class="lm-product-price d-flex align-items-center gap-2">
      <span :class="product.salePrice ? 'lm-price-sale' : ''">
        {{ formatPrice(product.salePrice || product.price) }}
      </span>
      <span v-if="product.salePrice" class="lm-price-old">{{ formatPrice(product.price) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { useWishlist } from '@/composables/useWishlist'

const props = defineProps({
  product: { type: Object, required: true }
})

const { addItem, formatPrice } = useCart()
const { showToast } = useToast()
const { isInWishlist, toggleWishlist } = useWishlist()

const isLiked = computed(() => isInWishlist(props.product.id))

function toggleWish() {
  const added = toggleWishlist(props.product.id)
  showToast(added ? 'Đã thêm vào yêu thích' : 'Đã xoá khỏi yêu thích')
}

function addCart() {
  const p = props.product
  addItem({
    id: p.id, name: p.name,
    variant: p.category || 'Mặc định',
    price: p.salePrice || p.price,
    image: p.image || null,
    letter: p.letter, bg: p.bg
  })
  showToast('Đã thêm vào giỏ hàng')
}
</script>
