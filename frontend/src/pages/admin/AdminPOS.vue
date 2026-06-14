<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Bán hàng tại quầy</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">Tạo đơn hàng trực tiếp cho khách tại cửa hàng</p>
      </div>
    </div>

    <div class="row g-3">
      <!-- Product list -->
      <div class="col-lg-7">
        <div class="z-admin-card mb-3" style="padding:14px 20px">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
            <input v-model="search" class="lm-input" placeholder="Tìm sản phẩm theo tên hoặc mã..." style="border:none;padding:8px 0;box-shadow:none">
          </div>
        </div>

        <div class="z-admin-card" style="padding:0;overflow:hidden;max-height:calc(100vh - 280px);overflow-y:auto">
          <div v-for="p in filteredProducts" :key="p.id"
               class="d-flex align-items-center gap-3 z-pos-item" @click="addToCart(p)">
            <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
              <img v-if="p.image" :src="p.image" style="width:100%;height:100%;object-fit:cover">
              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                   :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'12px', color:'rgba(255,255,255,0.3)' }">
                {{ p.letter }}
              </div>
            </div>
            <div class="flex-grow-1">
              <div style="font-size:13px;font-weight:500">{{ p.name }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ p.code }} · Tồn: {{ p.stock }}</div>
            </div>
            <div style="font-size:14px;font-weight:600;color:var(--z-accent)">{{ p.priceDisplay }}</div>
            <button class="z-add-btn"><i class="bi bi-plus"></i></button>
          </div>
          <div v-if="filteredProducts.length === 0" class="text-center py-5">
            <i class="bi bi-search" style="font-size:32px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Không tìm thấy sản phẩm</p>
          </div>
        </div>
      </div>

      <!-- Cart -->
      <div class="col-lg-5">
        <div class="z-admin-card" style="position:sticky;top:100px">
          <h3 class="z-admin-card-title mb-3">
            <i class="bi bi-cart3 me-2"></i>Giỏ hàng
            <span v-if="cart.length" style="font-size:12px;color:var(--z-gray);font-weight:400"> ({{ cart.length }} sản phẩm)</span>
          </h3>

          <div v-if="cart.length === 0" class="text-center py-4">
            <i class="bi bi-cart-x" style="font-size:36px;color:var(--z-gray-border)"></i>
            <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Chưa có sản phẩm nào</p>
          </div>

          <div v-else>
            <div class="d-flex flex-column gap-2 mb-3" style="max-height:300px;overflow-y:auto">
              <div v-for="(item, i) in cart" :key="i"
                   class="d-flex align-items-center gap-3 p-2" style="background:var(--z-bg-alt);border-radius:var(--z-radius)">
                <div class="flex-grow-1">
                  <div style="font-size:13px;font-weight:500">{{ item.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ fmtPrice(item.price) }}</div>
                </div>
                <div class="d-flex align-items-center gap-1">
                  <button class="z-qty-btn" @click="item.qty > 1 ? item.qty-- : removeFromCart(i)">
                    <i class="bi" :class="item.qty > 1 ? 'bi-dash' : 'bi-trash'"></i>
                  </button>
                  <span style="width:28px;text-align:center;font-size:13px;font-weight:600">{{ item.qty }}</span>
                  <button class="z-qty-btn" @click="item.qty++"><i class="bi bi-plus"></i></button>
                </div>
                <div style="font-size:13px;font-weight:600;min-width:80px;text-align:right">{{ fmtPrice(item.price * item.qty) }}</div>
              </div>
            </div>

            <!-- Customer info -->
            <div class="mb-3 pt-3" style="border-top:1px solid var(--z-gray-border)">
              <label class="z-label">Khách hàng (tuỳ chọn)</label>
              <input v-model="customerName" class="lm-input" placeholder="Tên khách hàng" style="font-size:13px;padding:8px 12px">
            </div>

            <!-- Payment method -->
            <div class="mb-3">
              <label class="z-label">Hình thức thanh toán</label>
              <div class="d-flex gap-2">
                <button v-for="pm in paymentMethods" :key="pm.value"
                        class="z-pm-btn" :class="{ active: paymentMethod === pm.value }"
                        @click="paymentMethod = pm.value">
                  <i class="bi" :class="pm.icon"></i> {{ pm.label }}
                </button>
              </div>
            </div>

            <!-- Note -->
            <div class="mb-3">
              <label class="z-label">Ghi chú</label>
              <input v-model="note" class="lm-input" placeholder="Ghi chú đơn hàng..." style="font-size:13px;padding:8px 12px">
            </div>

            <!-- Total -->
            <div class="d-flex justify-content-between align-items-center py-3 mb-3" style="border-top:2px solid var(--z-dark)">
              <div style="font-size:16px;font-weight:600">Tổng cộng</div>
              <div style="font-size:24px;font-weight:700;color:var(--z-accent)">{{ fmtPrice(cartTotal) }}</div>
            </div>

            <button class="lm-btn-primary w-100 justify-content-center" @click="createOrder" :disabled="creating">
              <i class="bi bi-check2-circle" style="position:relative;z-index:1"></i>
              <span>{{ creating ? 'Đang tạo...' : 'Tạo đơn hàng' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { mapProduct, fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const search = ref('')
const allProducts = ref([])
const cart = ref([])
const customerName = ref('')
const paymentMethod = ref('cash')
const note = ref('')
const creating = ref(false)

const paymentMethods = [
  { value: 'cash', label: 'Tiền mặt', icon: 'bi-cash-stack' },
  { value: 'transfer', label: 'Chuyển khoản', icon: 'bi-bank' },
  { value: 'card', label: 'Thẻ', icon: 'bi-credit-card' },
]

onMounted(async () => {
  try {
    const data = await api().getVay()
    allProducts.value = data.filter(p => p.trangThai === 1).map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, priceDisplay: fmtPrice(m.salePrice || m.price), rawId: p.id }
    })
  } catch (e) { console.error(e) }
})

const filteredProducts = computed(() => {
  if (!search.value) return allProducts.value
  const q = search.value.toLowerCase()
  return allProducts.value.filter(p =>
    p.name.toLowerCase().includes(q) || (p.code || '').toLowerCase().includes(q)
  )
})

const cartTotal = computed(() => cart.value.reduce((s, item) => s + item.price * item.qty, 0))

function addToCart(p) {
  const existing = cart.value.find(c => c.id === p.id)
  if (existing) {
    existing.qty++
  } else {
    cart.value.push({ id: p.id, name: p.name, price: p.salePrice || p.price, qty: 1 })
  }
  showToast(`Đã thêm "${p.name}"`)
}

function removeFromCart(index) {
  cart.value.splice(index, 1)
}

async function createOrder() {
  if (cart.value.length === 0) return
  creating.value = true
  try {
    const pmLabels = { cash: 'Tiền mặt', transfer: 'Chuyển khoản', card: 'Thẻ' }
    const orderData = {
      items: cart.value.map(item => ({ productId: item.id, quantity: item.qty, price: item.price })),
      tongTien: cartTotal.value,
      hinhThucThanhToan: pmLabels[paymentMethod.value],
      hinhThucNhanHang: 1,
      ghiChu: note.value ? `[Tại quầy] ${note.value}` : '[Tại quầy]',
      tenKhachHang: customerName.value || 'Khách lẻ',
    }
    await api().createOrder(orderData)
    showToast('Tạo đơn hàng thành công!')
    cart.value = []
    customerName.value = ''
    note.value = ''
  } catch (e) {
    showToast('Lỗi: ' + (e.message || 'Không thể tạo đơn'))
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.z-pos-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--z-gray-border);
  cursor: pointer; transition: all 0.15s;
}
.z-pos-item:hover { background: var(--z-accent-soft); }
.z-pos-item:last-child { border-bottom: none; }
.z-add-btn {
  width: 32px; height: 32px; border: 1px solid var(--z-accent);
  background: var(--z-white); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-accent); font-size: 14px;
  transition: all 0.2s; flex-shrink: 0;
}
.z-add-btn:hover { background: var(--z-accent); color: var(--z-white); }
.z-qty-btn {
  width: 26px; height: 26px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 12px; color: var(--z-gray);
  transition: all 0.2s;
}
.z-qty-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-pm-btn {
  flex: 1; padding: 8px 12px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: var(--z-radius);
  font-size: 12px; font-weight: 500; color: var(--z-gray);
  cursor: pointer; transition: all 0.2s; text-align: center;
  font-family: var(--z-font-body);
  display: flex; align-items: center; justify-content: center; gap: 4px;
}
.z-pm-btn:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-pm-btn.active { background: var(--z-dark); color: var(--z-white); border-color: var(--z-dark); }
.z-label { display: block; font-size: 12px; font-weight: 500; color: var(--z-gray); margin-bottom: 6px; }
</style>
