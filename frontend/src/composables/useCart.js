import { reactive, computed, nextTick, watch } from 'vue'
import { api, useAuth } from '@/composables/useApi'

const STORAGE_KEY = 'zestia_cart'
const OWNER_KEY = 'zestia_cart_owner'

function loadSaved() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.map(normalizeCartItem).filter(item => item.variantId > 0) : []
  } catch { return [] }
}

function normalizeCartItem(item) {
  const variantId = Number(item?.variantId || 0)
  const numericId = Number(item?.id)
  const color = cleanAttribute(item?.color ?? item?.mauSac)
  const size = cleanAttribute(item?.size ?? item?.kichThuoc)
  const resolvedVariant = buildVariantLabel({ color, size }) || cleanAttribute(item?.variant) || ''
  return {
    ...item,
    id: variantId > 0 ? `variant-${variantId}` : String(item?.id || ''),
    productId: Number(item?.productId || (Number.isFinite(numericId) ? numericId : 0)),
    variantId,
    color,
    size,
    variant: resolvedVariant,
    qty: Math.max(1, Number(item?.qty || 1))
  }
}

function cleanAttribute(value) {
  const normalized = String(value ?? '').trim()
  return normalized || null
}

function buildVariantLabel(item) {
  return [item?.color, item?.size ? `Size ${item.size}` : ''].filter(Boolean).join(' · ')
}

const state = reactive({
  items: loadSaved(),
  isOpen: false,
})
let refreshPromise = null
let syncReady = false
let syncTimer = null
let applyingServerState = false
let cartHydrationPending = false
let cartHydrationSucceeded = true
let resolveCartHydration = null
let cartHydrationPromise = Promise.resolve(true)

watch(() => state.items, (items) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  if (syncReady && !applyingServerState) scheduleServerSync()
}, { deep: true })

const totalCount = computed(() => state.items.reduce((s, i) => s + i.qty, 0))
const subtotal   = computed(() => state.items.reduce((s, i) => i.unavailable ? s : s + i.price * i.qty, 0))

function openCart()  { state.isOpen = true;  document.body.style.overflow = 'hidden' }
function closeCart() { state.isOpen = false; document.body.style.overflow = '' }

function addItem(product) {
  const normalized = normalizeCartItem(product)
  const variantId = Number(normalized.variantId || 0)
  if (variantId > 0) {
    normalized.productId = Number(normalized.productId || normalized.id)
    normalized.id = `variant-${variantId}`
  }
  const existing = state.items.find(i => itemKey(i) === itemKey(normalized))
  const maxQty = Number(product.maxQty || existing?.maxQty || 0)
  if (existing) {
    const currentQty = existing.qty
    Object.assign(existing, normalized)
    existing.qty = currentQty
    if (maxQty > 0) existing.qty = Math.min(maxQty, existing.qty + 1)
    else existing.qty++
  }
  else { state.items.push({ ...normalized, qty: 1 }) }
}

function itemKey(item) {
  const variantId = Number(item?.variantId || 0)
  return variantId > 0 ? `variant-${variantId}` : String(item?.id || '')
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
        const availableStock = Number(variant?.soLuong || 0)
        if (!variant || availableStock <= 0 || Number(product?.trangThai) !== 1) {
          item.unavailable = true
          item.unavailableReason = variant ? 'Biến thể hiện đã hết hàng' : 'Biến thể hiện không còn được bán'
          item.maxQty = 0
          continue
        }
        const currentPrice = Number(variant?.giaBan ?? product?.giaBan ?? item.price) || 0

        item.unavailable = false
        delete item.unavailableReason
        item.name = product?.tenSanPham || item.name
        item.image = variant?.anhUrl || product?.anhUrl || item.image || null
        item.price = currentPrice
        item.color = cleanAttribute(variant?.mauSac)
        item.size = cleanAttribute(variant?.kichThuoc)
        item.variant = buildVariantLabel(item)
        delete item.originalPrice
        item.maxQty = availableStock
        item.qty = Math.min(Number(item.qty || 1), availableStock)
      }
    } catch (error) {
      items.forEach(item => {
        item.unavailable = true
        item.unavailableReason = 'Sản phẩm hiện không còn được bán'
        item.maxQty = 0
      })
      console.warn(`Không thể làm mới sản phẩm ${productId} trong giỏ hàng`, error)
    }
  })).finally(() => {
    refreshPromise = null
  })

  return refreshPromise
}

function changeQty(id, delta) {
  const item = state.items.find(i => String(i.id) === String(id) || itemKey(i) === String(id))
  if (!item || item.unavailable) return
  const maxQty = Number(item.maxQty || 0)
  const next = Math.max(1, item.qty + delta)
  item.qty = maxQty > 0 ? Math.min(maxQty, next) : next
}

function removeItem(id) {
  const idx = state.items.findIndex(i => String(i.id) === String(id) || itemKey(i) === String(id))
  if (idx !== -1) state.items.splice(idx, 1)
}

function clearCart() {
  state.items.splice(0, state.items.length)
  closeCart()
}

async function removePurchasedItems(purchasedItems = []) {
  const purchasedByVariant = new Map()
  for (const item of Array.isArray(purchasedItems) ? purchasedItems : []) {
    const variantId = Number(item?.variantId || 0)
    const qty = Number(item?.qty || 0)
    if (variantId > 0 && qty > 0) {
      purchasedByVariant.set(variantId, (purchasedByVariant.get(variantId) || 0) + qty)
    }
  }

  const nextItems = []
  for (const item of state.items) {
    const purchasedQty = purchasedByVariant.get(Number(item.variantId)) || 0
    const remainingQty = Number(item.qty || 0) - purchasedQty
    if (remainingQty > 0) nextItems.push({ ...item, qty: remainingQty })
  }

  applyingServerState = true
  try {
    state.items.splice(0, state.items.length, ...nextItems)
    await nextTick()
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
  } finally {
    applyingServerState = false
  }
  if (!nextItems.length) closeCart()
}

function beginCartHydration() {
  if (cartHydrationPending) return cartHydrationPromise
  cartHydrationPending = true
  cartHydrationSucceeded = false
  cartHydrationPromise = new Promise(resolve => {
    resolveCartHydration = resolve
  })
  return cartHydrationPromise
}

function completeCartHydration(succeeded = true) {
  cartHydrationPending = false
  cartHydrationSucceeded = Boolean(succeeded)
  resolveCartHydration?.(cartHydrationSucceeded)
  resolveCartHydration = null
  return cartHydrationSucceeded
}

async function awaitCartHydration() {
  if (cartHydrationPending) return cartHydrationPromise
  const user = useAuth().getUser()
  if (!user || user.role !== 'KhachHang') return true
  return syncReady && cartHydrationSucceeded
}

function formatPrice(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

async function hydrateCart(remoteItems = [], userId) {
  const previousOwner = localStorage.getItem(OWNER_KEY)
  const sameOwner = previousOwner && String(previousOwner) === String(userId)
  const incoming = Array.isArray(remoteItems) ? remoteItems : []
  const guestItems = previousOwner ? [] : state.items
  const merged = sameOwner ? incoming : mergeGuestAndServer(guestItems, incoming)

  applyingServerState = true
  try {
    state.items.splice(0, state.items.length, ...merged.map(normalizeCartItem))
    await nextTick()
  } finally {
    applyingServerState = false
  }
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
  const user = useAuth().getUser()
  if (!user || user.role !== 'KhachHang') return true
  if (!syncReady) return false
  if (state.items.some(item => item.unavailable)) return false
  if (syncTimer) clearTimeout(syncTimer)
  syncTimer = null
  try {
    const serverItems = await api().replaceCustomerCart(state.items.map(item => ({
      variantId: Number(item.variantId),
      qty: Number(item.qty || 1)
    })).filter(item => item.variantId > 0))
    applyingServerState = true
    try {
      state.items.splice(0, state.items.length, ...(serverItems || []).map(normalizeCartItem))
      await nextTick()
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
    } finally {
      applyingServerState = false
    }
    return true
  } catch (error) {
    console.warn('Không thể đồng bộ giỏ hàng với tài khoản', error)
    return false
  }
}

export function useCart() {
  return {
    state, totalCount, subtotal, openCart, closeCart, addItem, refreshItems,
    changeQty, removeItem, clearCart, removePurchasedItems, formatPrice, hydrateCart,
    resetCartForGuest, syncCartNow, beginCartHydration, completeCartHydration,
    awaitCartHydration
  }
}
