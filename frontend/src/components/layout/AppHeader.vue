<script setup lang="ts">
import { ref } from 'vue'

const searchQuery = ref('')
const cartCount = ref(0)
</script>

<template>
  <!-- Top Bar -->
  <div class="top-bar bg-dark text-white py-2 d-none d-md-block">
    <div class="container d-flex justify-content-between align-items-center">
      <div class="top-bar-left">
        <span class="me-3"><i class="bi bi-telephone me-1"></i> 1900 1234</span>
        <span><i class="bi bi-envelope me-1"></i> support@zestia.vn</span>
      </div>
      <div class="top-bar-right">
        <a href="#" class="text-white text-decoration-none me-3">Giới thiệu</a>
        <a href="#" class="text-white text-decoration-none me-3">Liên hệ</a>
        <a href="#" class="text-white text-decoration-none">Chính sách</a>
      </div>
    </div>
  </div>

  <!-- Main Header -->
  <header class="main-header py-3 sticky-top bg-white border-bottom">
    <div class="container">
      <div class="row align-items-center">
        <!-- Logo -->
        <div class="col-4 col-md-2">
          <router-link to="/" class="navbar-brand fw-bold fs-3 text-dark text-decoration-none">
            ZESTIA<span class="text-danger">.</span>
          </router-link>
        </div>

        <!-- Search Bar -->
        <div class="col-8 col-md-5 order-last order-md-0 mt-3 mt-md-0">
          <div class="search-box position-relative">
            <input
              v-model="searchQuery"
              type="text"
              class="form-control rounded-pill"
              placeholder="Tìm kiếm váy, đầm..."
            />
            <button class="btn btn-danger rounded-pill position-absolute end-0 top-0 bottom-0 px-3">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>

        <!-- Icons -->
        <div class="col-2 col-md-5 text-end">
          <div class="header-icons d-flex justify-content-end align-items-center gap-3">
            <a href="#" class="text-dark position-relative" title="Yêu thích">
              <i class="bi bi-heart fs-5"></i>
              <span class="badge bg-danger rounded-pill badge-icon">0</span>
            </a>
            <a href="#" class="text-dark position-relative" title="Giỏ hàng">
              <i class="bi bi-bag fs-5"></i>
              <span class="badge bg-danger rounded-pill badge-icon">{{ cartCount }}</span>
            </a>
            <a href="#" class="text-dark" title="Tài khoản">
              <i class="bi bi-person fs-5"></i>
            </a>
          </div>
        </div>
      </div>
    </div>
  </header>

  <!-- Navigation Bar -->
  <nav class="navbar navbar-expand-lg bg-white border-bottom py-0">
    <div class="container">
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#mainNav"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="mainNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link to="/" class="nav-link active fw-semibold">Trang chủ</router-link>
          </li>
          <li class="nav-item dropdown">
            <a
              class="nav-link dropdown-toggle fw-semibold"
              href="#"
              data-bs-toggle="dropdown"
            >
              Danh mục
            </a>
            <ul class="dropdown-menu">
              <li><a class="dropdown-item" href="#">Váy dạ hội</a></li>
              <li><a class="dropdown-item" href="#">Váy công sở</a></li>
              <li><a class="dropdown-item" href="#">Váy dạo phố</a></li>
              <li><a class="dropdown-item" href="#">Váy đầm suông</a></li>
              <li><a class="dropdown-item" href="#">Chân váy</a></li>
            </ul>
          </li>
          <li class="nav-item">
            <a class="nav-link fw-semibold" href="#">Sản phẩm mới</a>
          </li>
          <li class="nav-item">
            <a class="nav-link fw-semibold" href="#">Bán chạy</a>
          </li>
          <li class="nav-item">
            <a class="nav-link fw-semibold" href="#">Khuyến mãi</a>
          </li>
          <li class="nav-item">
            <a class="nav-link fw-semibold" href="#">Bộ sưu tập</a>
          </li>
          <li class="nav-item">
            <a class="nav-link fw-semibold" href="#">Blog</a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.top-bar {
  font-size: 0.85rem;
}
.main-header {
  z-index: 1030;
}
.search-box input {
  padding-right: 120px;
  border: 2px solid #e9ecef;
  transition: border-color 0.3s;
}
.search-box input:focus {
  border-color: #e91e63;
  box-shadow: none;
}
.search-box button {
  border-top-left-radius: 0 !important;
  border-bottom-left-radius: 0 !important;
}
.badge-icon {
  position: absolute;
  top: -8px;
  right: -12px;
  font-size: 0.6rem;
  padding: 2px 5px;
}
.navbar-nav .nav-link {
  padding: 12px 16px;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
  color: #333;
  transition: color 0.3s;
}
.navbar-nav .nav-link:hover,
.navbar-nav .nav-link.active {
  color: #e91e63;
}
</style>
