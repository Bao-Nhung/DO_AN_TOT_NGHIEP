<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="footer bg-dark text-white pt-5">
    <div class="container">
      <div class="row">
        <!-- About -->
        <div class="col-lg-4 mb-4">
          <h5 class="fw-bold mb-3">ZESTIA<span class="text-danger">.</span></h5>
          <p class="text-white-50">
            Zestia là thương hiệu thời trang nữ hàng đầu Việt Nam, mang đến những thiết kế váy đầm
            tinh tế, sang trọng và hiện đại cho phái đẹp.
          </p>
          <div class="social-links mt-3">
            <a href="#" class="text-white me-2"><i class="bi bi-facebook fs-5"></i></a>
            <a href="#" class="text-white me-2"><i class="bi bi-instagram fs-5"></i></a>
            <a href="#" class="text-white me-2"><i class="bi bi-tiktok fs-5"></i></a>
            <a href="#" class="text-white me-2"><i class="bi bi-youtube fs-5"></i></a>
            <a href="#" class="text-white"><i class="bi bi-pinterest fs-5"></i></a>
          </div>
        </div>

        <!-- Quick Links -->
        <div class="col-lg-2 col-md-4 mb-4">
          <h6 class="fw-bold mb-3 text-uppercase">Danh mục</h6>
          <ul class="list-unstyled footer-links">
            <li><a href="#" class="text-white-50 text-decoration-none">Váy dạ hội</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Váy công sở</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Váy dạo phố</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Đầm suông</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Chân váy</a></li>
          </ul>
        </div>

        <!-- Customer Service -->
        <div class="col-lg-2 col-md-4 mb-4">
          <h6 class="fw-bold mb-3 text-uppercase">Hỗ trợ</h6>
          <ul class="list-unstyled footer-links">
            <li><a href="#" class="text-white-50 text-decoration-none">Trung tâm trợ giúp</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Hướng dẫn mua hàng</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Chính sách đổi trả</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Chính sách bảo mật</a></li>
            <li><a href="#" class="text-white-50 text-decoration-none">Điều khoản sử dụng</a></li>
          </ul>
        </div>

        <!-- Newsletter -->
        <div class="col-lg-4 col-md-4 mb-4">
          <h6 class="fw-bold mb-3 text-uppercase">Đăng ký nhận tin</h6>
          <p class="text-white-50 small">
            Nhận ưu đãi 10% cho đơn hàng đầu tiên và cập nhật xu hướng mới nhất.
          </p>
          <div class="newsletter-form d-flex">
            <input
              type="email"
              class="form-control rounded-start-pill border-0"
              placeholder="Email của bạn"
            />
            <button class="btn btn-danger rounded-end-pill px-3">
              <i class="bi bi-send"></i>
            </button>
          </div>
          <div class="mt-3">
            <span class="text-white-50 small me-2">Chấp nhận thanh toán:</span>
            <i class="bi bi-credit-card text-white-50 me-1"></i>
            <span class="text-white-50 small">Visa, Mastercard, MoMo, ZaloPay</span>
          </div>
        </div>
      </div>

      <hr class="border-secondary" />

      <div class="row py-3">
        <div class="col-md-6 text-center text-md-start">
          <p class="text-white-50 small mb-0">
            &copy; {{ currentYear }} Zestia. Tất cả quyền được bảo lưu.
          </p>
        </div>
        <div class="col-md-6 text-center text-md-end">
          <img
            src="https://placehold.co/200x30/ffffff/999999?text=Payment+Methods"
            alt="Payment methods"
            class="img-fluid"
            style="max-height: 25px"
          />
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-links li {
  margin-bottom: 8px;
}
.footer-links a {
  transition: color 0.3s, padding-left 0.3s;
}
.footer-links a:hover {
  color: #e91e63 !important;
  padding-left: 5px;
}
.newsletter-form input:focus {
  box-shadow: none;
}
.social-links a {
  transition: color 0.3s;
}
.social-links a:hover {
  color: #e91e63 !important;
}
</style>
