// composables/useCart.js
// Lightweight reactive cart state — no Pinia/Vuex needed for this scope
import { reactive, computed } from 'vue'

const state = reactive({
  items: [
    { id: 1, name: 'Silk Wrap Dress',   variant: 'Màu Kem · Size S',     price: 4290000, qty: 1, letter: 'A', bg: 'linear-gradient(160deg,#EDE6D8,#C5B89A)' },
    { id: 2, name: 'Cashmere Coat',     variant: 'Màu Be · Size M',      price: 9890000, qty: 1, letter: 'B', bg: 'linear-gradient(160deg,#E0D4C4,#B8A88A)' },
    { id: 3, name: 'Linen Trousers',    variant: 'Màu Trắng · Size S',   price: 2190000, qty: 2, letter: 'C', bg: 'linear-gradient(160deg,#DEDCD8,#A8A49E)' },
  ],
  isOpen: false,
})

const totalCount = computed(() => state.items.reduce((s, i) => s + i.qty, 0))
const subtotal   = computed(() => state.items.reduce((s, i) => s + i.price * i.qty, 0))

function openCart()  { state.isOpen = true;  document.body.style.overflow = 'hidden' }
function closeCart() { state.isOpen = false; document.body.style.overflow = '' }

function addItem(product) {
  const existing = state.items.find(i => i.id === product.id)
  if (existing) { existing.qty++ }
  else { state.items.push({ ...product, qty: 1 }) }
}

function changeQty(id, delta) {
  const item = state.items.find(i => i.id === id)
  if (!item) return
  item.qty = Math.max(1, item.qty + delta)
}

function removeItem(id) {
  const idx = state.items.findIndex(i => i.id === id)
  if (idx !== -1) state.items.splice(idx, 1)
}

function formatPrice(n) {
  return n.toLocaleString('vi-VN') + '₫'
}

export function useCart() {
  return { state, totalCount, subtotal, openCart, closeCart, addItem, changeQty, removeItem, formatPrice }
}
