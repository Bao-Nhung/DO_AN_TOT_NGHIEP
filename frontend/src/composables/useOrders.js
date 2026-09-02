import { ref } from 'vue'
import { api } from './useApi'

const currentOrder = ref(null)
const loading = ref(false)

export function useOrders() {
  const apiClient = api()

  async function searchOrder(maHoaDon, soDienThoai = null, options = {}) {
    const silent = options.silent === true
    if (!silent) loading.value = true
    try {
      const params = { maHoaDon, soDienThoai: soDienThoai || undefined }
      const res = await apiClient.searchOrder(params)
      currentOrder.value = res?.data || res
      return currentOrder.value
    } catch (err) {
      if (!silent) console.error('Error searching order:', err)
      throw err
    } finally {
      if (!silent) loading.value = false
    }
  }

  function resetState() {
    currentOrder.value = null
    loading.value = false
  }

  return {
    currentOrder, loading, searchOrder, resetState
  }
}
