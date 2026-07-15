import { reactive, computed, watch } from 'vue'
import { api, useAuth } from '@/composables/useApi'

const STORAGE_KEY = 'zestia_cart'
const OWNER_KEY = 'zestia_cart_owner'

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
let refreshPromise = null
let syncReady = false
let syncTimer = null
let applyingServerState = false

watch(() => state.items, (items) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  if (syncReady && !applyingServerState) scheduleServerSync()
}, { deep: true })

const totalCount = computed(() => state.items.reduce((s, i) => s + i.qty, 0))
const subtotal   = computed(() => state.items.reduce((s, i) => s + i.price * i.qty, 0))

function openCart()  { state.isOpen = true;  document.body.style.overflow = 'hidden' }
function closeCart() { state.isOpen = false; document.body.style.overflow = '' }

function addItem(product) {
  const existing = state.items.find(i => i.id === product.id)
  const maxQty = Number(product.maxQty || existing?.maxQty || 0)
  if (existing) {
    const currentQty = existing.qty
    Object.assign(existing, product)
    existing.qty = currentQty
    if (maxQty > 0) existing.qty = Math.min(maxQty, existing.qty + 1)
    else existing.qty++
  }
  else { state.items.push({ ...product, qty: 1 }) }
}

function refreshItems(loadProduct) {
  if (typeof loadProduct !== 'function' || !state.items.length) return
  if (refreshPromise) return refreshPromise

  const itemsByProduct = new Map()
  for (const item of state.items) {
    const productId = Number(item.productId)
    if (!productId) continue
    if (!itemsByProduct.has(productId)) itemsByProduct.set(productId, [])
    itemsByProduct.get(productId).push(item)
  }

  refreshPromise = Promise.all([...itemsByProduct.entries()].map(async ([productId, items]) => {
    try {
      const product = await loadProduct(productId)
      const variants = Array.isArray(product?.bienThe) ? product.bienThe : []

      for (const item of items) {
        const variant = variants.find(v => Number(v.id) === Number(item.variantId))
        const currentPrice = Number(variant?.giaBan ?? product?.giaBan ?? item.price) || 0

        item.name = product?.tenVay || item.name
        item.image = variant?.anhUrl || product?.anhUrl || item.image || null
        item.price = currentPrice
        delete item.originalPrice
        if (variant?.soLuong != null) item.maxQty = Number(variant.soLuong)
      }
    } catch (error) {
      console.warn(`Không thể làm mới sản phẩm ${productId} trong giỏ hàng`, error)
    }
  })).finally(() => {
    refreshPromise = null
  })

  return refreshPromise
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
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

async function hydrateCart(remoteItems = [], userId) {
  const previousOwner = localStorage.getItem(OWNER_KEY)
  const sameOwner = previousOwner && String(previousOwner) === String(userId)
  const incoming = Array.isArray(remoteItems) ? remoteItems : []
  const merged = sameOwner ? incoming : mergeGuestAndServer(state.items, incoming)

  applyingServerState = true
  state.items.splice(0, state.items.length, ...merged)
  applyingServerState = false
  localStorage.setItem(OWNER_KEY, String(userId))
  syncReady = true

  if (!sameOwner && state.items.length) await syncCartNow()
}

function resetCartForGuest() {
  syncReady = false
  if (syncTimer) clearTimeout(syncTimer)
  syncTimer = null
  applyingServerState = true
  state.items.splice(0, state.items.length)
  applyingServerState = false
  localStorage.removeItem(OWNER_KEY)
  localStorage.setItem(STORAGE_KEY, '[]')
}

function mergeGuestAndServer(localItems, remoteItems) {
  const merged = new Map()
  for (const item of remoteItems) merged.set(Number(item.variantId), { ...item })
  for (const item of localItems) {
    const variantId = Number(item.variantId)
    if (!variantId) continue
    const current = merged.get(variantId)
    if (!current) merged.set(variantId, { ...item })
    else current.qty = Math.max(Number(current.qty || 1), Number(item.qty || 1))
  }
  return [...merged.values()]
}

function scheduleServerSync() {
  if (syncTimer) clearTimeout(syncTimer)
  syncTimer = setTimeout(syncCartNow, 450)
}

async function syncCartNow() {
  if (!syncReady) return
  const user = useAuth().getUser()
  if (!user || user.role !== 'KhachHang') return
  if (syncTimer) clearTimeout(syncTimer)
  syncTimer = null
  try {
    const serverItems = await api().replaceCustomerCart(state.items.map(item => ({
      variantId: Number(item.variantId),
      qty: Number(item.qty || 1)
    })).filter(item => item.variantId > 0))
    applyingServerState = true
    state.items.splice(0, state.items.length, ...(serverItems || []))
    applyingServerState = false
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
  } catch (error) {
    console.warn('Không thể đồng bộ giỏ hàng với tài khoản', error)
  }
}

export function useCart() {
  return {
    state, totalCount, subtotal, openCart, closeCart, addItem, refreshItems,
    changeQty, removeItem, clearCart, formatPrice, hydrateCart,
    resetCartForGuest, syncCartNow
  }
}
