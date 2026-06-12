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
            <li><RouterLink class="lm-footer-link" to="/collections">Khuyến mãi</RouterLink></li>
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
          <span class="z-payment-badge">
            <svg width="36" height="12" viewBox="0 0 36 12"><path d="M13.3 0.5L8.7 11.5H5.7L3.4 2.9C3.3 2.4 3.1 2.2 2.7 2C2 1.6 0.9 1.3 0 1.1L0.1 0.5H5C5.5 0.5 5.9 0.9 6 1.4L7.2 7.7L10.1 0.5H13.3ZM25.4 7.8C25.4 4.9 21.3 4.7 21.3 3.4C21.3 3 21.7 2.5 22.5 2.4C22.9 2.4 24 2.3 25.2 2.9L25.7 0.8C25.1 0.5 24.3 0.3 23.3 0.3C20.3 0.3 18.2 1.9 18.2 4.1C18.2 5.8 19.6 6.7 20.7 7.2C21.8 7.8 22.2 8.1 22.2 8.6C22.2 9.3 21.4 9.6 20.6 9.6C19.3 9.6 18.5 9.3 17.9 9L17.4 11.2C18.1 11.5 19.2 11.7 20.4 11.7C23.6 11.7 25.4 10.1 25.4 7.8ZM32.8 11.5H35.5L33.1 0.5H30.7C30.2 0.5 29.8 0.8 29.6 1.2L25.1 11.5H28.3L28.9 9.8H32.8L33.1 11.5H32.8ZM29.8 7.4L31.4 3L32.3 7.4H29.8ZM17.2 0.5L14.7 11.5H11.7L14.2 0.5H17.2Z" fill="#1A1F71"/></svg>
          </span>
          <span class="z-payment-badge">
            <svg width="30" height="18" viewBox="0 0 30 18"><circle cx="11" cy="9" r="9" fill="#EB001B" opacity="0.8"/><circle cx="19" cy="9" r="9" fill="#F79E1B" opacity="0.8"/><path d="M15 2.3A8.96 8.96 0 0 0 11.5 9 8.96 8.96 0 0 0 15 15.7 8.96 8.96 0 0 0 18.5 9 8.96 8.96 0 0 0 15 2.3Z" fill="#FF5F00"/></svg>
          </span>
          <span class="z-payment-badge" style="font-weight:700;color:#0066CC;font-size:11px;letter-spacing:-0.02em">
            VNPay
          </span>
          <span class="z-payment-badge" style="font-weight:700;color:#a50064;font-size:12px;letter-spacing:-0.02em">
            MoMo
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
