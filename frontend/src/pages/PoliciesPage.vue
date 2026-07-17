<template>
  <div>
    <div class="lm-page-hero" data-title="POLICY">
      <div class="container">
        <p class="lm-eyebrow mb-3">Thông tin mua hàng</p>
        <h1>Chính sách <em>rõ ràng</em></h1>
        <p>Mốc thời gian và điều kiện đang áp dụng được lấy trực tiếp từ cấu hình cửa hàng.</p>
      </div>
    </div>

    <main class="container z-policy-page">
      <nav class="z-policy-nav" aria-label="Danh sách chính sách">
        <a href="#shopping-guide">Hướng dẫn mua hàng</a>
        <a v-for="policy in policies" :key="policy.code" :href="`#policy-${policy.code}`">{{ policy.title }}</a>
      </nav>
      <article id="shopping-guide" class="z-policy-row">
        <div><p class="lm-eyebrow">4 bước</p></div>
        <div>
          <h2>Hướng dẫn mua hàng</h2>
          <strong>Chọn đúng biến thể, kiểm tra thông tin rồi mới xác nhận thanh toán.</strong>
          <p>1. Mở sản phẩm và chọn đầy đủ màu sắc, kích thước.\n2. Kiểm tra số lượng trong giỏ hàng và voucher phù hợp.\n3. Nhập email, số điện thoại và chọn địa chỉ nhận hàng.\n4. Kiểm tra lại bảng xác nhận; với MoMo hoặc ZaloPay, đơn chỉ được xác nhận sau khi cổng báo thanh toán thành công.</p>
        </div>
      </article>
      <section v-if="loading" class="z-policy-empty">Đang tải chính sách...</section>
      <section v-else-if="!policies.length" class="z-policy-empty">Chưa có chính sách đang áp dụng.</section>
      <article v-for="policy in policies" :id="`policy-${policy.code}`" :key="policy.code" class="z-policy-row">
        <div>
          <span v-if="policy.numericValue" class="z-policy-number">{{ policy.numericValue }} {{ policy.unit }}</span>
          <p class="lm-eyebrow">{{ policy.code }}</p>
        </div>
        <div>
          <h2>{{ policy.title }}</h2>
          <strong>{{ policy.summary }}</strong>
          <p>{{ policy.content }}</p>
          <small v-if="policy.updatedAt">Cập nhật: {{ formatDate(policy.updatedAt) }}</small>
        </div>
      </article>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '@/composables/useApi'
import AppFooter from '@/components/layout/AppFooter.vue'

const policies = ref([])
const loading = ref(true)
onMounted(async () => {
  try { policies.value = await api().getStorePolicies() || [] }
  catch (error) { console.warn('Không tải được chính sách', error) }
  finally { loading.value = false }
})
function formatDate(value) { return new Date(value).toLocaleDateString('vi-VN') }
</script>

<style scoped>
.z-policy-page { padding-top: 48px; padding-bottom: 72px; }
.z-policy-nav { display: flex; flex-wrap: wrap; gap: 10px; padding-bottom: 32px; border-bottom: 1px solid var(--z-gray-border); }
.z-policy-nav a { padding: 9px 14px; border: 1px solid var(--z-gray-border); color: var(--z-dark); font-size: 13px; text-decoration: none; }
.z-policy-nav a:hover { border-color: var(--z-accent); color: var(--z-accent); }
.z-policy-row { display: grid; grid-template-columns: minmax(150px,.35fr) 1fr; gap: 52px; padding: 56px 0; border-bottom: 1px solid var(--z-gray-border); scroll-margin-top: 96px; }
.z-policy-number { display: block; margin-bottom: 16px; color: var(--z-accent); font-family: var(--z-font-display); font-size: 36px; }
.z-policy-row h2 { margin-bottom: 14px; font-family: var(--z-font-display); font-size: 34px; font-weight: 400; }
.z-policy-row strong { display: block; margin-bottom: 16px; font-size: 15px; }
.z-policy-row p { max-width: 760px; color: var(--z-gray); line-height: 1.8; white-space: pre-line; }
.z-policy-row small { color: var(--z-gray-light); }
.z-policy-empty { padding: 80px 0; color: var(--z-gray); text-align: center; }
@media (max-width: 700px) { .z-policy-row { grid-template-columns: 1fr; gap: 16px; padding: 40px 0; } }
</style>
