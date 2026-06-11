<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quan ly san pham</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredProducts.length }} san pham</p>
      </div>
      <button class="lm-btn-primary" @click="showAddModal = true">
        <i class="bi bi-plus-lg" style="position:relative;z-index:1"></i>
        <span>Them san pham</span>
      </button>
    </div>

    <!-- Filters -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-3 flex-wrap">
        <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:320px">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="search" class="lm-input" placeholder="Tim kiem san pham..." style="border:none;padding:8px 0;box-shadow:none">
        </div>
        <select v-model="filterCategory" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tat ca loai</option>
          <option v-for="cat in categories" :key="cat">{{ cat }}</option>
        </select>
        <select v-model="filterStatus" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tat ca trang thai</option>
          <option value="1">Dang ban</option>
          <option value="0">Ngung ban</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th style="width:50px"><input type="checkbox"></th>
            <th>San pham</th>
            <th>Loai</th>
            <th>Gia</th>
            <th>Ton kho</th>
            <th>Trang thai</th>
            <th style="width:100px">Thao tac</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in filteredProducts" :key="p.id">
            <td><input type="checkbox"></td>
            <td>
              <div class="d-flex align-items-center gap-3">
                <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0">
                  <div class="w-100 h-100 d-flex align-items-center justify-content-center"
                       :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
                    {{ p.letter }}
                  </div>
                </div>
                <div>
                  <div style="font-weight:500">{{ p.name }}</div>
                  <div style="font-size:12px;color:var(--z-gray)">{{ p.code }}</div>
                </div>
              </div>
            </td>
            <td>{{ p.category }}</td>
            <td style="font-weight:500">{{ p.price }}</td>
            <td>
              <span :style="{ color: p.stock < 10 ? 'var(--z-accent)' : 'var(--z-dark)', fontWeight: p.stock < 10 ? 600 : 400 }">
                {{ p.stock }}
              </span>
            </td>
            <td><span class="z-status" :class="p.active ? 'success' : 'pending'">{{ p.active ? 'Dang ban' : 'Ngung' }}</span></td>
            <td>
              <div class="d-flex gap-1">
                <button class="z-icon-btn" title="Sua"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Xoa" style="color:var(--z-accent)"><i class="bi bi-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { mapProduct, fmtPrice } from '@/composables/useProducts'

const search = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const showAddModal = ref(false)
const allProducts = ref([])
const categories = ref([])

onMounted(async () => {
  try {
    const data = await api().getVay()
    allProducts.value = data.map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, price: fmtPrice(m.salePrice || m.price) }
    })
    const cats = [...new Set(allProducts.value.map(p => p.category).filter(Boolean))]
    categories.value = cats
  } catch (e) { console.error('Failed to load products:', e) }
})

const filteredProducts = computed(() => {
  return allProducts.value.filter(p => {
    const matchSearch = !search.value || p.name.toLowerCase().includes(search.value.toLowerCase()) || (p.code || '').toLowerCase().includes(search.value.toLowerCase())
    const matchCat = !filterCategory.value || p.category === filterCategory.value
    const matchStatus = !filterStatus.value || (filterStatus.value === '1' ? p.active : !p.active)
    return matchSearch && matchCat && matchStatus
  })
})
</script>

<style scoped>
.z-icon-btn {
  width: 32px; height: 32px;
  border: none; background: transparent;
  border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray);
  transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
</style>
