<template>
  <footer class="lm-footer py-5">
    <div class="container">
      <div class="row g-5 mb-5">

        <!-- Brand -->
        <div class="col-lg-4">
          <div class="lm-footer-logo mb-3">LUM<span class="lm-gold-text">I</span>ÈRE</div>
          <p class="text-secondary" style="font-size:12px;font-weight:300;line-height:1.8;max-width:260px">
            Thương hiệu thời trang & lifestyle cao cấp Việt Nam. Được truyền cảm hứng từ vẻ đẹp tinh tế và sự sang trọng vượt thời gian.
          </p>
          <div class="d-flex gap-2 mt-3">
            <a class="lm-social-link"><i class="bi bi-instagram"></i></a>
            <a class="lm-social-link"><i class="bi bi-facebook"></i></a>
            <a class="lm-social-link"><i class="bi bi-youtube"></i></a>
          </div>
        </div>

        <!-- Products -->
        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">Sản Phẩm</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/collections">Bộ sưu tập mới</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Đầm & Váy</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Áo khoác</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Phụ kiện</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Sale</RouterLink></li>
          </ul>
        </div>

        <!-- Services -->
        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">Dịch Vụ</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link">Tư vấn phong cách</a></li>
            <li><a class="lm-footer-link">Đặt hàng riêng</a></li>
            <li><a class="lm-footer-link">Đổi trả hàng</a></li>
            <li><a class="lm-footer-link">Bảo hành</a></li>
            <li><a class="lm-footer-link">Theo dõi đơn hàng</a></li>
          </ul>
        </div>

        <!-- Contact -->
        <div class="col-lg-3">
          <h4 class="lm-footer-heading mb-4">Liên Hệ</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link">hello@lumiere.vn</a></li>
            <li><a class="lm-footer-link">1800 1234 (miễn phí)</a></li>
            <li><a class="lm-footer-link">36 Tràng Tiền, Hoàn Kiếm</a></li>
            <li><a class="lm-footer-link">T2–T7: 9:00–21:00</a></li>
          </ul>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center pt-4"
           style="border-top:1px solid var(--lm-beige)">
        <p class="mb-0" style="font-size:11px;color:var(--lm-gray-light)">© 2025 LUMIÈRE. Tất cả quyền được bảo lưu.</p>
        <p class="mb-0" style="font-size:11px;color:var(--lm-gray-light)">Thiết kế tại Việt Nam với ♥</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
// No logic needed — purely presentational
</script>
