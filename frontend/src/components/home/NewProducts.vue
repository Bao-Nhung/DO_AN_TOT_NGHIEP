<script setup lang="ts">
// import SectionTitle from '@/components/common/SectionTitle.vue'
// import ProductCard from '@/components/common/ProductCard.vue'

const newProducts = [
  {
    id: 1,
    name: 'Váy dạ hội đuôi cá đính kết',
    price: 2500000,
    originalPrice: 3200000,
    image: 'https://placehold.co/400x500/fce4ec/e91e63?text=Váy+Dạ+Hội',
    discount: 22,
    isNew: true,
  },
  {
    id: 2,
    name: 'Đầm suông cổ chữ V thanh lịch',
    price: 890000,
    originalPrice: 1200000,
    image: 'https://placehold.co/400x500/e8eaf6/3f51b5?text=Đầm+Suông',
    discount: 26,
    isNew: true,
  },
  {
    id: 3,
    name: 'Chân váy chữ A xếp ly Hàn Quốc',
    price: 450000,
    originalPrice: 599000,
    image: 'https://placehold.co/400x500/fff3e0/ff9800?text=Chân+Váy',
    discount: 25,
    isNew: true,
  },
  {
    id: 4,
    name: 'Váy maxi hoa nhí vintage',
    price: 690000,
    image: 'https://placehold.co/400x500/e0f2f1/009688?text=Maxi+Vintage',
    isNew: true,
  },
]
</script>

<template>
  <section class="new-products-section py-5 bg-light">
    <div class="container">
      <SectionTitle title="Sản Phẩm Mới Nhất" subtitle="Những thiết kế vừa ra mắt đang chờ bạn" />

      <div class="row g-4">
        <div v-for="product in newProducts" :key="product.id" class="col-6 col-md-4 col-lg-3">
          <ProductCard
            :id="product.id"
            :name="product.name"
            :price="product.price"
            :original-price="product.originalPrice"
            :image="product.image"
            :discount="product.discount"
            :is-new="product.isNew"
          />
        </div>
      </div>

      <div class="text-center mt-4">
        <a href="#" class="btn btn-outline-dark rounded-pill px-5 py-2">
          Xem tất cả <i class="bi bi-arrow-right ms-1"></i>
        </a>
      </div>
    </div>
  </section>
</template>
