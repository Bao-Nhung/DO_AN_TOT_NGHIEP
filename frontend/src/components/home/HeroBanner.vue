<script setup lang="ts">
const banners = [
  {
    id: 1,
    title: 'Bộ Sưu Tập Hè 2026',
    subtitle: 'Tinh tế - Sang trọng - Quyến rũ',
    description: 'Khám phá những thiết kế mới nhất với ưu đãi lên đến 30%',
    cta: 'Mua ngay',
    ctaLink: '#',
    secondaryCta: 'Khám phá BST',
    secondaryLink: '#',
    bgColor: 'linear-gradient(135deg, #fce4ec 0%, #f8bbd0 100%)',
    image: 'https://placehold.co/500x600/f8bbd0/333333?text=Summer+2026',
  },
  {
    id: 2,
    title: 'Váy Dạ Hội Cao Cấp',
    subtitle: 'Tỏa sáng trong mọi sự kiện',
    description: 'Thiết kế độc quyền từ các NTK hàng đầu',
    cta: 'Xem ngay',
    ctaLink: '#',
    secondaryCta: '',
    secondaryLink: '',
    bgColor: 'linear-gradient(135deg, #e8eaf6 0%, #c5cae9 100%)',
    image: 'https://placehold.co/500x600/c5cae9/333333?text=Evening+Gown',
  },
]
</script>

<template>
  <div id="heroCarousel" class="carousel slide" data-bs-ride="carousel" data-bs-interval="5000">
    <div class="carousel-indicators">
      <button
        v-for="(banner, index) in banners"
        :key="banner.id"
        type="button"
        data-bs-target="#heroCarousel"
        :data-bs-slide-to="index"
        :class="{ active: index === 0 }"
      ></button>
    </div>
    <div class="carousel-inner">
      <div
        v-for="(banner, index) in banners"
        :key="banner.id"
        class="carousel-item"
        :class="{ active: index === 0 }"
        :style="{ background: banner.bgColor }"
      >
        <div class="container">
          <div class="row align-items-center hero-slide" style="min-height: 550px">
            <div class="col-md-6">
              <span class="badge bg-danger mb-2 px-3 py-2">{{ banner.subtitle }}</span>
              <h1 class="display-4 fw-bold mt-3">{{ banner.title }}</h1>
              <p class="lead text-muted mt-3 mb-4">{{ banner.description }}</p>
              <div class="hero-buttons">
                <a :href="banner.ctaLink" class="btn btn-danger btn-lg rounded-pill px-4 me-2">
                  {{ banner.cta }}
                </a>
                <a
                  v-if="banner.secondaryCta"
                  :href="banner.secondaryLink"
                  class="btn btn-outline-dark btn-lg rounded-pill px-4"
                >
                  {{ banner.secondaryCta }}
                </a>
              </div>
            </div>
            <div class="col-md-6 text-center mt-4 mt-md-0">
              <img
                :src="banner.image"
                :alt="banner.title"
                class="img-fluid hero-image"
                style="max-height: 450px; object-fit: contain"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    <button
      class="carousel-control-prev"
      type="button"
      data-bs-target="#heroCarousel"
      data-bs-slide="prev"
    >
      <span class="carousel-control-prev-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
    </button>
    <button
      class="carousel-control-next"
      type="button"
      data-bs-target="#heroCarousel"
      data-bs-slide="next"
    >
      <span class="carousel-control-next-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
    </button>
  </div>
</template>

<style scoped>
.hero-slide {
  padding: 60px 0;
}
.hero-image {
  animation: float 3s ease-in-out infinite;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}
.carousel-control-prev,
.carousel-control-next {
  width: 5%;
}
</style>
