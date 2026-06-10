<script setup lang="ts">
import SectionTitle from '@/components/common/SectionTitle.vue'

const categories = [
  { id: 1, name: 'Váy dạ hội', image: 'https://placehold.co/400x500/fce4ec/e91e63?text=Dạ+Hội', count: 42 },
  { id: 2, name: 'Váy công sở', image: 'https://placehold.co/400x500/e8eaf6/3f51b5?text=Công+Sở', count: 68 },
  { id: 3, name: 'Váy dạo phố', image: 'https://placehold.co/400x500/fff3e0/ff9800?text=Dạo+Phố', count: 55 },
  { id: 4, name: 'Đầm suông', image: 'https://placehold.co/400x500/e0f2f1/009688?text=Đầm+Suông', count: 37 },
  { id: 5, name: 'Chân váy', image: 'https://placehold.co/400x500/f3e5f5/9c27b0?text=Chân+Váy', count: 29 },
  { id: 6, name: 'Váy maxi', image: 'https://placehold.co/400x500/fce4ec/d81b60?text=Maxi', count: 23 },
]
</script>

<template>
  <section class="category-section py-5">
    <div class="container">
      <SectionTitle title="Danh Mục Nổi Bật" subtitle="Khám phá bộ sưu tập theo phong cách của bạn" />

      <div class="row g-3">
        <div v-for="cat in categories" :key="cat.id" class="col-6 col-md-4 col-lg-2">
          <a href="#" class="category-card text-decoration-none text-dark">
            <div class="card border-0 overflow-hidden rounded-4">
              <div class="category-img-wrapper">
                <img :src="cat.image" :alt="cat.name" class="card-img category-img" />
                <div class="category-overlay"></div>
              </div>
              <div class="card-body text-center py-3">
                <h6 class="fw-bold mb-1">{{ cat.name }}</h6>
                <small class="text-muted">{{ cat.count }} sản phẩm</small>
              </div>
            </div>
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.category-card .card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}
.category-card:hover .card {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.category-img-wrapper {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}
.category-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.category-card:hover .category-img {
  transform: scale(1.1);
}
.category-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.2), transparent);
}
</style>
