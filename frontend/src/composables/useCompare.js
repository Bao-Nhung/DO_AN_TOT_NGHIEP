import { computed, ref } from 'vue'

const STORAGE_KEY = 'zestia_compare_products'
const MAX_PRODUCTS = 4

function readIds() {
  if (typeof window === 'undefined') return []
  try {
    const parsed = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '[]')
    return [...new Set(parsed.map(Number).filter(Number.isInteger))].slice(0, MAX_PRODUCTS)
  } catch {
    return []
  }
}

const comparedIds = ref(readIds())

function persist() {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(comparedIds.value))
  window.dispatchEvent(new CustomEvent('zestia-compare-changed', {
    detail: { ids: [...comparedIds.value] }
  }))
}

export function useCompare() {
  function hasProduct(productId) {
    return comparedIds.value.includes(Number(productId))
  }

  function addProduct(productId) {
    const id = Number(productId)
    if (!Number.isInteger(id) || hasProduct(id)) return { added: false, reason: 'exists' }
    if (comparedIds.value.length >= MAX_PRODUCTS) return { added: false, reason: 'limit' }
    comparedIds.value = [...comparedIds.value, id]
    persist()
    return { added: true }
  }

  function removeProduct(productId) {
    const id = Number(productId)
    const next = comparedIds.value.filter(item => item !== id)
    if (next.length === comparedIds.value.length) return false
    comparedIds.value = next
    persist()
    return true
  }

  function toggleProduct(productId) {
    if (hasProduct(productId)) {
      removeProduct(productId)
      return { added: false, removed: true }
    }
    return addProduct(productId)
  }

  function clear() {
    comparedIds.value = []
    persist()
  }

  return {
    ids: computed(() => comparedIds.value),
    count: computed(() => comparedIds.value.length),
    maxProducts: MAX_PRODUCTS,
    hasProduct,
    addProduct,
    removeProduct,
    toggleProduct,
    clear,
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('storage', event => {
    if (event.key === STORAGE_KEY) comparedIds.value = readIds()
  })
}
