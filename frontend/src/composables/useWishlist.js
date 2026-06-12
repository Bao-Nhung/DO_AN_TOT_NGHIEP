import { ref } from 'vue'

const STORAGE_KEY = 'zestia_wishlist'

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

const wishlistIds = ref(load())

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(wishlistIds.value))
}

export function useWishlist() {
  function addToWishlist(id) {
    if (!wishlistIds.value.includes(id)) {
      wishlistIds.value.push(id)
      save()
    }
  }

  function removeFromWishlist(id) {
    wishlistIds.value = wishlistIds.value.filter(i => i !== id)
    save()
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
    return wishlistIds.value.includes(id)
  }

  return { wishlistIds, addToWishlist, removeFromWishlist, toggleWishlist, isInWishlist }
}
