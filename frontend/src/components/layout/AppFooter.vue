<template>
  <footer class="lm-footer py-5">
    <div class="container">
      <div class="row g-5 mb-5">

        <div class="col-lg-4">
          <div class="lm-footer-logo mb-3">Zest<span class="lm-gold-text">ia</span></div>
          <p style="font-size:14px;font-weight:400;color:var(--z-gray);line-height:1.7;max-width:280px">
            Thuong hieu thoi trang Viet Nam mang phong cach tre trung, hien dai va thanh lich cho phu nu hien dai.
          </p>
          <div class="d-flex gap-2 mt-3">
            <a class="lm-social-link"><i class="bi bi-instagram"></i></a>
            <a class="lm-social-link"><i class="bi bi-facebook"></i></a>
            <a class="lm-social-link"><i class="bi bi-tiktok"></i></a>
          </div>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">San Pham</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/collections">Vay truyen thong</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Vay cach tan</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Vay da hoi</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Vay cong so</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">Khuyen mai</RouterLink></li>
          </ul>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">Ho Tro</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link">Huong dan mua hang</a></li>
            <li><a class="lm-footer-link">Chinh sach doi tra</a></li>
            <li><a class="lm-footer-link">Van chuyen</a></li>
            <li><a class="lm-footer-link">Bao hanh</a></li>
            <li><a class="lm-footer-link">Theo doi don hang</a></li>
          </ul>
        </div>

        <div class="col-lg-3">
          <h4 class="lm-footer-heading mb-4">Lien He</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link">hello@zestia.vn</a></li>
            <li><a class="lm-footer-link">1800 9999 (mien phi)</a></li>
            <li><a class="lm-footer-link">FPT Polytechnic, Ha Noi</a></li>
            <li><a class="lm-footer-link">T2-T7: 9:00-21:00</a></li>
          </ul>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center pt-4"
           style="border-top:1px solid var(--z-gray-border)">
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">© 2025 Zestia. All rights reserved.</p>
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">Do an tot nghiep — FPT Polytechnic</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>
