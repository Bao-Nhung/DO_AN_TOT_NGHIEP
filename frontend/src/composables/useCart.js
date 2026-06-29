import { reactive, computed, watch } from 'vue'
import { api, useAuth } from './useApi'

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

// Load cart from server if logged in
async function loadCartFromServer() {
  const { isLoggedIn } = useAuth()
  if (isLoggedIn()) {
    try {
      const serverItems = await api().getCart()
      state.items = serverItems.map(item => ({
        id: item.idVayChiTiet, // Dùng idVayChiTiet làm key cục bộ
        cartItemId: item.id, // ID thực trong bảng GioHangChiTiet
        idVayChiTiet: item.idVayChiTiet,
        maVayChiTiet: item.maVayChiTiet,
        name: item.tenVay,
        size: item.kichThuoc,
        color: item.mauSac,
        price: item.giaBan,
        image: item.anhUrl,
        qty: item.soLuong,
        letter: item.tenVay?.charAt(0)?.toUpperCase() || 'Z',
        bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)'
      }))
    } catch (e) {
      console.error('Failed to load cart from server', e)
    }
  }
}

async function addItem(product) {
  // product = { idVayChiTiet, maVayChiTiet, name, size, color, price, image, letter, bg, qty }
  const existing = state.items.find(i => i.idVayChiTiet === product.idVayChiTiet)
  let newQty = product.qty || 1

  if (existing) { 
    newQty = existing.qty + (product.qty || 1)
  }

  const { isLoggedIn } = useAuth()
  if (isLoggedIn()) {
    try {
      await api().addToCart(product.idVayChiTiet, product.qty || 1)
      await loadCartFromServer() // Tải lại để lấy cartItemId
    } catch (e) {
      console.error('Add to cart failed', e)
      throw e // Ném lỗi để UI xử lý (vd: vượt tồn kho)
    }
  } else {
    if (existing) {
      existing.qty = newQty
    } else {
      state.items.push({ 
        ...product, 
        id: product.idVayChiTiet, // Dùng idVayChiTiet làm ID cục bộ
        qty: newQty 
      })
    }
  }
}

async function changeQty(id, delta) {
  const item = state.items.find(i => i.id === id)
  if (!item) return
  const newQty = Math.max(1, item.qty + delta)
  
  const { isLoggedIn } = useAuth()
  if (isLoggedIn() && item.cartItemId) {
    try {
      await api().updateCartItem(item.cartItemId, newQty)
      item.qty = newQty
    } catch (e) {
      console.error('Update qty failed', e)
      throw e
    }
  } else {
    item.qty = newQty
  }
}

async function removeItem(id) {
  const idx = state.items.findIndex(i => i.id === id)
  if (idx === -1) return
  const item = state.items[idx]
  
  const { isLoggedIn } = useAuth()
  if (isLoggedIn() && item.cartItemId) {
    try {
      await api().deleteCartItem(item.cartItemId)
      state.items.splice(idx, 1)
    } catch (e) {
      console.error('Delete item failed', e)
    }
  } else {
    state.items.splice(idx, 1)
  }
}

function clearCart() {
  state.items.splice(0, state.items.length)
  closeCart()
}

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

// Khi gọi useCart lần đầu, thử tải giỏ hàng từ server nếu đã đăng nhập
let isInitialized = false;

export function useCart() {
  if (!isInitialized) {
    loadCartFromServer();
    isInitialized = true;
  }
  return { 
    state, totalCount, subtotal, 
    openCart, closeCart, addItem, changeQty, removeItem, clearCart, formatPrice,
    loadCartFromServer
  }
}
