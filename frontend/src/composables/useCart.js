import { reactive, computed } from 'vue'

const state = reactive({
  items: [
    { id: 1, name: 'Vay Lua To Tam Co Dien',     variant: 'Trang · Size S',   price: 2890000, qty: 1, letter: 'Z', bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)' },
    { id: 3, name: 'Vay Da Hoi Gam Hoang Gia',    variant: 'Do · Size M',      price: 4290000, qty: 1, letter: 's', bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)' },
    { id: 5, name: 'Vay Dui Cach Tan Mua He',      variant: 'Xanh La · Size M', price: 1390000, qty: 2, letter: 'i', bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)' },
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
  return n.toLocaleString('vi-VN') + 'd'
}

export function useCart() {
  return { state, totalCount, subtotal, openCart, closeCart, addItem, changeQty, removeItem, formatPrice }
}
