<template>
  <footer class="lm-footer py-5">
    <div class="container">
      <div class="row gx-3 gy-5 mb-5">

        <div class="col-lg-4">
          <div class="lm-footer-logo mb-3">Zest<span class="lm-gold-text">ia</span></div>
          <p style="font-size:14px;font-weight:400;color:var(--z-gray);line-height:1.7;max-width:280px">
            {{ t('footerAboutText') }}
          </p>
          <div class="d-flex gap-2 mt-3">
            <a class="lm-social-link" href="https://www.instagram.com/" target="_blank" rel="noopener noreferrer" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
            <a class="lm-social-link" href="https://www.facebook.com/" target="_blank" rel="noopener noreferrer" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
            <a class="lm-social-link" href="https://www.tiktok.com/" target="_blank" rel="noopener noreferrer" aria-label="TikTok"><i class="bi bi-tiktok"></i></a>
          </div>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">{{ t('products') }}</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/collections?sort=newest">{{ t('products') }}</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections?sort=bestseller">{{ t('lookbook') }}</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/collections">{{ t('reviews') }}</RouterLink></li>
          </ul>
        </div>

        <div class="col-6 col-lg-2">
          <h4 class="lm-footer-heading mb-4">{{ t('footerPolicies') }}</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><RouterLink class="lm-footer-link" to="/about">{{ t('about') }}</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/policies#policy-RETURN">{{ t('footerPolicies') }}</RouterLink></li>
            <li><RouterLink class="lm-footer-link" to="/tracking">{{ t('tracking') }}</RouterLink></li>
          </ul>
        </div>

        <div class="col-lg-3">
          <h4 class="lm-footer-heading mb-4">{{ t('footerContact') }}</h4>
          <ul class="list-unstyled d-flex flex-column gap-2">
            <li><a class="lm-footer-link" href="mailto:nguyenthanh.hn090307@gmail.com">nguyenthanh.hn090307@gmail.com</a></li>
            <li><a class="lm-footer-link" href="tel:+84869167207">0869 167 207</a></li>
            <li><span class="lm-footer-link">FPT Polytechnic, Hà Nội</span></li>
            <li><span class="lm-footer-link">{{ t('showroomHours') }}</span></li>
          </ul>
        </div>
      </div>

      <!-- Payment Badges -->
      <div class="d-flex flex-wrap align-items-center gap-3 mb-4 pb-4" style="border-bottom:1px solid var(--z-gray-border)">
        <span style="font-size:12px;font-weight:600;color:var(--z-gray);text-transform:uppercase;letter-spacing:0">Phương thức thanh toán:</span>
        <div class="d-flex gap-2 flex-wrap">
          <span class="z-payment-badge" style="font-weight:700;color:#a50064;font-size:12px;letter-spacing:0">
            MoMo
          </span>
          <span class="z-payment-badge" style="font-weight:700;color:#0068FF;font-size:12px;letter-spacing:0">
            ZaloPay
          </span>
          <span class="z-payment-badge" style="font-weight:600;color:var(--z-gray);font-size:11px">
            <i class="bi bi-cash-stack me-1"></i>COD
          </span>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">&copy; 2026 Zestia. All rights reserved.</p>
        <p class="mb-0" style="font-size:13px;color:var(--z-gray-light)">Đồ án tốt nghiệp — FPT Polytechnic</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'
const { t } = useI18n()
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
