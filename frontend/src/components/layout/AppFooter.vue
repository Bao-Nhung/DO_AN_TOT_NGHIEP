<template>
  <footer class="lm-footer py-5">
    <div class="container">
      <div class="row g-5 mb-5">

        <div class="col-lg-4">
          <div class="lm-footer-logo mb-3">Zest<span class="lm-gold-text">ia</span></div>
          <p style="font-size:14px;font-weight:400;color:var(--z-gray);line-height:1.7;max-width:280px">
            Thương hiệu thời trang Việt Nam mang phong cách trẻ trung, hiện đại và thanh lịch cho phụ nữ hiện đại.
          </p>
          <div class="d-flex gap-2 mt-3">
            <a class="lm-social-link"><i class="bi bi-instagram"></i></a>
            <a class="lm-social-link"><i class="bi bi-facebook"></i></a>
            <a class="lm-social-link"><i class="bi bi-tiktok"></i></a>
          </div>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">Sản Phẩm</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/collections">Váy truyền thống</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Váy cách tân</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Váy dạ hội</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Váy công sở</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Voucher</RouterLink></li>
          </ul>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">Hỗ Trợ</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/about">Về chúng tôi</RouterLink></li>
            <li><a class="lm-footer-link" href="#/checkout">Hướng dẫn mua hàng</a></li>
            <li><a class="lm-footer-link">Chính sách đổi trả</a></li>
            <li><a class="lm-footer-link">Vận chuyển</a></li>
            <li><RouterLink class="lm-footer-link" to="/profile">Theo dõi đơn hàng</RouterLink></li>
          </ul>
        </div>

        <div class="col-lg-3">
          <h4 class="lm-footer-heading mb-4">Liên Hệ</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link">hello@zestia.vn</a></li>
            <li><a class="lm-footer-link">1800 9999 (miễn phí)</a></li>
            <li><a class="lm-footer-link">FPT Polytechnic, Hà Nội</a></li>
            <li><a class="lm-footer-link">T2-T7: 9:00-21:00</a></li>
          </ul>
        </div>
      </div>

      <!-- Payment Badges -->
      <div class="d-flex flex-wrap align-items-center gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
        <span style="font-size:12px;font-weight:600;color:var(--z-gray);text-transform:uppercase;letter-spacing:0.08em">Thanh toán bảo mật:</span>
        <div class="d-flex gap-2 flex-wrap">
          <span class="z-payment-badge" style="font-weight:700;color:#a50064;font-size:12px;letter-spacing:-0.02em">
            MoMo
          </span>
          <span class="z-payment-badge" style="font-weight:700;color:#0068FF;font-size:12px;letter-spacing:-0.02em">
            ZaloPay
          </span>
          <span class="z-payment-badge" style="font-weight:600;color:var(--z-gray);font-size:11px">
            <i class="bi bi-cash-stack me-1"></i>COD
          </span>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">&copy; 2025 Zestia. All rights reserved.</p>
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">Đồ án tốt nghiệp — FPT Polytechnic</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.z-payment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  min-width: 56px;
  height: 32px;
}
</style>
