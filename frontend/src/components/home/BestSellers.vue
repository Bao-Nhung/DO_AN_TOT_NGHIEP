<script setup lang="ts">
import SectionTitle from '@/components/common/SectionTitle.vue'
import ProductCard from '@/components/common/ProductCard.vue'

const bestSellers = [
  {
    id: 5,
    name: 'Váy công sở dáng ôm thanh lịch',
    price: 750000,
    originalPrice: 950000,
    image: 'https://placehold.co/400x500/e8eaf6/3f51b5?text=Váy+Công+Sở',
    discount: 21,
  },
  {
    id: 6,
    name: 'Đầm dạo phố họa tiết hoa',
    price: 550000,
    image: 'https://placehold.co/400x500/fff3e0/ff9800?text=Đầm+Dạo+Phố',
  },
  {
    id: 7,
    name: 'Váy bodycon dự tiệc sang trọng',
    price: 1200000,
    originalPrice: 1600000,
    image: 'https://placehold.co/400x500/fce4ec/e91e63?text=Váy+Bodycon',
    discount: 25,
  },
  {
    id: 8,
    name: 'Chân váy midi xẻ tà quyến rũ',
    price: 620000,
    image: 'https://placehold.co/400x500/f3e5f5/9c27b0?text=Chân+Váy+Midi',
  },
]

const tabs = ['Tất cả', 'Váy dạ hội', 'Váy công sở', 'Đầm suông', 'Chân váy']
</script>

<template>
  <section class="best-sellers-section py-5">
    <div class="container">
      <SectionTitle title="Sản Phẩm Bán Chạy" subtitle="Được yêu thích nhất trong tháng này" />

      <!-- Filter Tabs -->
      <div class="d-flex justify-content-center flex-wrap gap-2 mb-4">
        <button
          v-for="(tab, index) in tabs"
          :key="tab"
          class="btn rounded-pill px-4"
          :class="index === 0 ? 'btn-danger' : 'btn-outline-secondary'"
        >
          {{ tab }}
        </button>
      </div>

      <div class="row g-4">
        <div v-for="product in bestSellers" :key="product.id" class="col-6 col-md-4 col-lg-3">
          <ProductCard
            :id="product.id"
            :name="product.name"
            :price="product.price"
            :original-price="product.originalPrice"
            :image="product.image"
            :discount="product.discount"
          />
        </div>
      </div>

      <div class="text-center mt-4">
        <a href="#" class="btn btn-outline-dark rounded-pill px-5 py-2">
          Xem tất cả sản phẩm bán chạy <i class="bi bi-arrow-right ms-1"></i>
        </a>
      </div>
    </div>
  </section>
</template>
