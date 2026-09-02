<template>
  <div class="z-product-price" :class="[`z-product-price--${size}`, { 'is-discounted': hasDiscount }]">
    <span v-if="hasDiscount" class="z-product-price__original">
      {{ formatPrice(originalTotal) }}
    </span>
    <span class="z-product-price__current">
      {{ formatPrice(currentTotal) }}
    </span>
    <span v-if="showDiscount && hasDiscount" class="z-product-price__discount">
      -{{ discountPercent }}%
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  price: { type: [Number, String], required: true },
  originalPrice: { type: [Number, String], default: null },
  quantity: { type: [Number, String], default: 1 },
  size: {
    type: String,
    default: 'card',
    validator: value => ['card', 'detail', 'compact'].includes(value)
  },
  showDiscount: { type: Boolean, default: false }
})

const unitPrice = computed(() => Math.max(0, Number(props.price) || 0))
const originalUnitPrice = computed(() => Math.max(0, Number(props.originalPrice) || 0))
const normalizedQuantity = computed(() => Math.max(1, Number(props.quantity) || 1))
const hasDiscount = computed(() => originalUnitPrice.value > unitPrice.value)
const currentTotal = computed(() => unitPrice.value * normalizedQuantity.value)
const originalTotal = computed(() => originalUnitPrice.value * normalizedQuantity.value)
const discountPercent = computed(() => {
  if (!hasDiscount.value || originalUnitPrice.value <= 0) return 0
  return Math.max(1, Math.round((1 - unitPrice.value / originalUnitPrice.value) * 100))
})

function formatPrice(value) {
  return Number(value || 0).toLocaleString('vi-VN') + 'đ'
}
</script>

<style scoped>
.z-product-price {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 5px 9px;
  color: var(--z-dark);
  font-family: var(--z-font-body);
  font-variant-numeric: tabular-nums;
  line-height: 1.25;
}

.z-product-price__original {
  color: var(--z-gray-light);
  font-size: .82em;
  font-weight: 500;
  text-decoration: line-through;
  text-decoration-thickness: 1px;
}

.z-product-price__current {
  font-weight: 700;
  white-space: nowrap;
}

.is-discounted .z-product-price__current {
  color: var(--z-accent);
}

.z-product-price__discount {
  padding: 3px 7px;
  border-radius: 999px;
  background: var(--z-accent-soft);
  color: var(--z-accent);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.z-product-price--card {
  font-size: 15px;
}

.z-product-price--detail {
  gap: 7px 12px;
  font-size: 32px;
}

.z-product-price--detail .z-product-price__original {
  font-size: 17px;
}

.z-product-price--compact {
  justify-content: flex-end;
  font-size: 13px;
}

.z-product-price--compact .z-product-price__original {
  font-size: 11px;
}

@media (max-width: 575.98px) {
  .z-product-price--card { font-size: 13px; }
  .z-product-price--detail { font-size: 27px; }
  .z-product-price--detail .z-product-price__original { font-size: 15px; }
  .z-product-price--compact { gap: 3px 6px; font-size: 12px; }
}
</style>
