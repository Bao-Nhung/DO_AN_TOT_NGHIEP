import { ref } from 'vue'
import { api, useAuth } from '@/composables/useApi'

const STORAGE_KEY = 'zestia_wishlist'
const OWNER_KEY = 'zestia_wishlist_owner'

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

const wishlistIds = ref(load())
let syncReady = false

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(wishlistIds.value))
}

export function useWishlist() {
  function addToWishlist(id) {
    const numericId = Number(id)
    if (!wishlistIds.value.includes(numericId)) {
      wishlistIds.value.push(numericId)
      save()
      syncWishlist('add', numericId)
    }
  }

  function removeFromWishlist(id) {
    const numericId = Number(id)
    wishlistIds.value = wishlistIds.value.filter(i => i !== numericId)
    save()
    syncWishlist('remove', numericId)
  }

  function toggleWishlist(id) {
    if (wishlistIds.value.includes(id)) {
      removeFromWishlist(id)
      return false
    } else {
      addToWishlist(id)
      return true
    }
  }

  function isInWishlist(id) {
    return wishlistIds.value.includes(Number(id))
  }

  function hydrateWishlist(remoteIds = [], userId) {
    const previousOwner = localStorage.getItem(OWNER_KEY)
    const sameOwner = previousOwner && String(previousOwner) === String(userId)
    const remote = (remoteIds || []).map(Number).filter(Boolean)
    const guestIds = previousOwner ? [] : wishlistIds.value.map(Number)
    const merged = sameOwner ? remote : [...new Set([...remote, ...guestIds])]
    wishlistIds.value = merged
    localStorage.setItem(OWNER_KEY, String(userId))
    save()
    syncReady = true
    if (!sameOwner) {
      for (const productId of merged) api().addCustomerWishlist(productId).catch(() => {})
    }
  }

  function resetWishlistForGuest() {
    syncReady = false
    wishlistIds.value = []
    localStorage.removeItem(OWNER_KEY)
    save()
  }

  return {
    wishlistIds, addToWishlist, removeFromWishlist, toggleWishlist,
    isInWishlist, hydrateWishlist, resetWishlistForGuest
  }
}

function syncWishlist(action, productId) {
  if (!syncReady) return
  const user = useAuth().getUser()
  if (!user || user.role !== 'KhachHang') return
  const call = action === 'add'
    ? api().addCustomerWishlist(productId)
    : api().removeCustomerWishlist(productId)
  call.catch(error => console.warn('Không thể đồng bộ danh sách yêu thích', error))
}
