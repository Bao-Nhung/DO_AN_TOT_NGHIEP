<script setup lang="ts">
defineProps<{
  id: number
  name: string
  price: number
  originalPrice?: number
  image: string
  discount?: number
  isNew?: boolean
}>()

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}
</script>

<template>
  <div class="product-card card border-0 h-100">
    <div class="product-img-wrapper position-relative overflow-hidden">
      <img :src="image" :alt="name" class="card-img-top product-img" />
      <span v-if="discount" class="badge bg-danger position-absolute top-0 start-0 m-2">
        -{{ discount }}%
      </span>
      <span v-if="isNew" class="badge bg-success position-absolute top-0 end-0 m-2">
        Mới
      </span>
      <div class="product-actions">
        <button class="btn btn-light btn-sm rounded-circle me-1" title="Yêu thích">
          <i class="bi bi-heart"></i>
        </button>
        <button class="btn btn-light btn-sm rounded-circle me-1" title="Xem nhanh">
          <i class="bi bi-eye"></i>
        </button>
        <button class="btn btn-dark btn-sm rounded-circle" title="Thêm vào giỏ">
          <i class="bi bi-cart-plus"></i>
        </button>
      </div>
    </div>
    <div class="card-body text-center">
      <h6 class="card-title product-name">{{ name }}</h6>
      <div class="product-price">
        <span class="current-price fw-bold text-danger">{{ formatPrice(price) }}</span>
        <span v-if="originalPrice" class="original-price text-muted text-decoration-line-through ms-2">
          {{ formatPrice(originalPrice) }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
.product-img-wrapper {
  aspect-ratio: 3 / 4;
  background-color: #f8f9fa;
}
.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.product-card:hover .product-img {
  transform: scale(1.08);
}
.product-actions {
  position: absolute;
  bottom: -50px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding: 10px;
  transition: bottom 0.3s ease;
}
.product-card:hover .product-actions {
  bottom: 10px;
}
.product-name {
  font-size: 0.9rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.7rem;
}
.product-price {
  font-size: 0.95rem;
}
.original-price {
  font-size: 0.8rem;
}
</style>
