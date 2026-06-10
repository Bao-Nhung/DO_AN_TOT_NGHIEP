<template>
  <div class="lm-product-card" @click="$router.push('/product/' + product.id)">
    <div class="lm-product-image">
      <!-- Product image placeholder -->
      <div class="lm-product-img-inner" :style="{ background: product.bg }">
        {{ product.letter }}
      </div>

      <!-- Badge -->
      <div v-if="product.badge" class="lm-product-badge" :class="{ sale: product.badge === 'Sale' }">
        {{ product.badge }}
      </div>

      <!-- Wishlist button -->
      <button class="lm-product-wish" :class="{ liked: isLiked }"
              @click.stop="toggleWish">
        <i class="bi" :class="isLiked ? 'bi-heart-fill' : 'bi-heart'"
           :style="{ color: isLiked ? 'var(--lm-gold)' : 'var(--lm-black)', fontSize:'14px' }"></i>
      </button>

      <!-- Quick add -->
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
import { ref } from 'vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  product: { type: Object, required: true }
})

const { addItem, formatPrice } = useCart()
const { showToast } = useToast()
const isLiked = ref(false)

function toggleWish() {
  isLiked.value = !isLiked.value
  showToast(isLiked.value ? 'Đã thêm vào yêu thích ♥' : 'Đã xóa khỏi yêu thích')
}

function addCart() {
  addItem(props.product)
  showToast('Đã thêm vào giỏ hàng')
}
</script>
