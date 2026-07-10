import { reactive, computed, watch } from 'vue'

const STORAGE_KEY = 'zestia_cart'

function loadSaved() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

const state = reactive({
  items: loadSaved(),
  isOpen: false,
})

watch(() => state.items, (items) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
}, { deep: true })

const totalCount = computed(() => state.items.reduce((s, i) => s + i.qty, 0))
const subtotal   = computed(() => state.items.reduce((s, i) => s + i.price * i.qty, 0))

function openCart()  { state.isOpen = true;  document.body.style.overflow = 'hidden' }
function closeCart() { state.isOpen = false; document.body.style.overflow = '' }

function addItem(product) {
  const existing = state.items.find(i => i.id === product.id)
  const maxQty = Number(product.maxQty || existing?.maxQty || 0)
  if (existing) {
    if (maxQty > 0) existing.qty = Math.min(maxQty, existing.qty + 1)
    else existing.qty++
  }
  else { state.items.push({ ...product, qty: 1 }) }
}

function changeQty(id, delta) {
  const item = state.items.find(i => i.id === id)
  if (!item) return
  const maxQty = Number(item.maxQty || 0)
  const next = Math.max(1, item.qty + delta)
  item.qty = maxQty > 0 ? Math.min(maxQty, next) : next
}

function removeItem(id) {
  const idx = state.items.findIndex(i => i.id === id)
  if (idx !== -1) state.items.splice(idx, 1)
}

function clearCart() {
  state.items.splice(0, state.items.length)
  closeCart()
}

function formatPrice(n) {
  return n.toLocaleString('vi-VN') + 'đ'
}

export function useCart() {
  return { state, totalCount, subtotal, openCart, closeCart, addItem, changeQty, removeItem, clearCart, formatPrice }
}
