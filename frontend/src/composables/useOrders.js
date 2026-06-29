import { ref, computed } from 'vue'
import { api } from './useApi' // Đã sửa lại tên import chuẩn

const orders = ref([])
const currentOrder = ref(null)
const trackingInfo = ref(null)
const loading = ref(false)

export function useOrders() {
  const apiClient = api() // Đã sửa lại cách gọi khởi tạo

  async function fetchMyOrders() {
    loading.value = true
    try {
      const res = await apiClient.getMyOrders()
      orders.value = res?.data || res || []
      return orders.value
    } catch (err) {
      console.error('Error fetching orders:', err)
      throw err
    } finally { loading.value = false }
  }

  async function searchOrder(maHoaDon, soDienThoai = null) {
    loading.value = true
    try {
      const params = { maHoaDon, soDienThoai: soDienThoai || undefined }
      const res = await apiClient.searchOrder(params)
      currentOrder.value = res?.data || res
      return currentOrder.value
    } catch (err) {
      console.error('Error searching order:', err)
      throw err
    } finally { loading.value = false }
  }

  async function searchOrderByPhone(soDienThoai) {
    loading.value = true
    try {
      const res = await apiClient.searchOrderByPhone(soDienThoai)
      orders.value = res?.data || res || []
      return orders.value
    } catch (err) {
      console.error('Error searching orders by phone:', err)
      throw err
    } finally { loading.value = false }
  }

  async function getOrderTracking(orderId) {
    loading.value = true
    try {
      const res = await apiClient.getOrderTracking(orderId)
      trackingInfo.value = res?.data || res
      return trackingInfo.value
    } catch (err) {
      console.error('Error fetching tracking info:', err)
      throw err
    } finally { loading.value = false }
  }

  const sortedOrders = computed(() => {
    if (!Array.isArray(orders.value)) return [] // Tránh lỗi trắng trang nếu data không phải là mảng
    return [...orders.value].sort((a, b) => {
      const dateA = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const dateB = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return dateB - dateA
    })
  })

  function resetState() {
    orders.value = []
    currentOrder.value = null
    trackingInfo.value = null
    loading.value = false
  }

  return {
    orders, currentOrder, trackingInfo, loading, sortedOrders,
    fetchMyOrders, searchOrder, searchOrderByPhone, getOrderTracking, resetState
  }
}