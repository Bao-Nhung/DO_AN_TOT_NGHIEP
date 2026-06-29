<template>
  <div>
    <div class="lm-page-hero" data-title="TRA CỨU ĐƠN">
      <div class="container text-center">
        <p class="lm-eyebrow mb-3">Hỗ trợ khách hàng</p>
        <h1 class="mb-3">Tra cứu <em>đơn hàng</em></h1>
        <p style="font-size:16px;color:var(--z-gray);max-width:600px;margin: 0 auto;">
          Nhập mã đơn hàng hoặc số điện thoại để xem trạng thái giao hàng của bạn.
        </p>
      </div>
    </div>

    <div class="container py-5" style="max-width: 800px; min-height: 50vh;">
      <div class="z-search-box mb-5">
        <h4 class="mb-4 text-center z-display">Thông tin đơn hàng</h4>
        <form @submit.prevent="handleSearch">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label" style="font-size: 13px; font-weight: 500;">Mã đơn hàng *</label>
              <input v-model="form.maHoaDon" class="lm-input" placeholder="VD: HD2606..." required />
            </div>
            <div class="col-md-6">
              <label class="form-label" style="font-size: 13px; font-weight: 500;">Số điện thoại (tùy chọn)</label>
              <input v-model="form.soDienThoai" class="lm-input" placeholder="Nhập SĐT đặt hàng" />
            </div>
            <div class="col-12 mt-4">
              <button type="submit" class="lm-btn-primary w-100" :disabled="loading">
                <span v-if="loading"><i class="bi bi-arrow-repeat z-spin"></i> Đang tra cứu...</span>
                <span v-else><i class="bi bi-search me-2"></i>Tra cứu ngay</span>
              </button>
            </div>
          </div>
        </form>
      </div>

      <!-- Kết quả tra cứu -->
      <div v-if="currentOrder">
        <OrderTrackingCard :order="currentOrder" />
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useOrders } from '@/composables/useOrders'
import { useToast } from '@/composables/useToast'
import AppFooter from '@/components/layout/AppFooter.vue'

// Import trực tiếp thẻ Card lộ trình
import OrderTrackingCard from '@/components/OrderTrackingCard.vue'

const { searchOrder, currentOrder, loading } = useOrders()
const { showToast } = useToast()

const form = ref({
  maHoaDon: '',
  soDienThoai: ''
})

async function handleSearch() {
  if (!form.value.maHoaDon.trim()) {
    showToast('Vui lòng nhập mã đơn hàng', 'warning')
    return
  }
  try {
    await searchOrder(form.value.maHoaDon.trim(), form.value.soDienThoai.trim())
    showToast('Tra cứu thành công', 'success')
  } catch (e) {
    currentOrder.value = null
    showToast(e.message || 'Không tìm thấy đơn hàng. Vui lòng kiểm tra lại mã.', 'error')
  }
}
</script>

<style scoped>
.z-search-box {
  background: var(--z-white);
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
}
@keyframes spin { to { transform: rotate(360deg); } }
.z-spin { display: inline-block; animation: spin 1s linear infinite; }
</style>