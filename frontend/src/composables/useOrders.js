import { ref, computed } from 'vue'
import { useApi } from './useApi'

const orders = ref([])
const currentOrder = ref(null)
const trackingInfo = ref(null)
const loading = ref(false)

export function useOrders() {
  const api = useApi()

  /**
   * Lấy danh sách đơn hàng của khách hàng hiện tại (yêu cầu đăng nhập)
   */
  async function fetchMyOrders() {
    loading.value = true
    try {
      const data = await api.getMyOrders()
      orders.value = data || []
      return data
    } catch (err) {
      console.error('Error fetching orders:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Tìm kiếm đơn hàng không cần đăng nhập
   * @param {string} maHoaDon - Mã hóa đơn
   * @param {string} soDienThoai - Số điện thoại (tùy chọn)
   */
  async function searchOrder(maHoaDon, soDienThoai = null) {
    loading.value = true
    try {
      const params = {
        maHoaDon,
        soDienThoai: soDienThoai || undefined
      }
      const data = await api.searchOrder(params)
      currentOrder.value = data
      return data
    } catch (err) {
      console.error('Error searching order:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Lấy danh sách đơn hàng theo số điện thoại
   * @param {string} soDienThoai - Số điện thoại
   */
  async function searchOrderByPhone(soDienThoai) {
    loading.value = true
    try {
      const data = await api.searchOrderByPhone(soDienThoai)
      orders.value = data || []
      return data
    } catch (err) {
      console.error('Error searching orders by phone:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Lấy chi tiết tracking của một đơn hàng
   * @param {number} orderId - ID của hóa đơn
   */
  async function getOrderTracking(orderId) {
    loading.value = true
    try {
      const data = await api.getOrderTracking(orderId)
      trackingInfo.value = data
      return data
    } catch (err) {
      console.error('Error fetching tracking info:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Lấy chi tiết đơn hàng
   * @param {number} orderId - ID của hóa đơn
   */
  async function getOrderDetail(orderId) {
    loading.value = true
    try {
      const data = await api.getHoaDonById(orderId)
      currentOrder.value = data
      return data
    } catch (err) {
      console.error('Error fetching order detail:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Lấy danh sách đơn hàng theo trạng thái
   * @param {string} status - Trạng thái (pending, processing, shipped, delivered, cancelled)
   */
  async function getOrdersByStatus(status) {
    loading.value = true
    try {
      const data = await api.getOrdersByStatus(status)
      orders.value = data || []
      return data
    } catch (err) {
      console.error('Error fetching orders by status:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Computed - Đơn hàng được sắp xếp theo ngày mới nhất
   */
  const sortedOrders = computed(() => {
    return [...orders.value].sort((a, b) => {
      const dateA = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const dateB = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return dateB - dateA
    })
  })

  /**
   * Computed - Số lượng đơn hàng
   */
  const totalOrders = computed(() => orders.value.length)

  /**
   * Computed - Tổng giá trị các đơn hàng đã thanh toán
   */
  const totalSpent = computed(() => {
    return orders.value
      .filter(o => o.trangThai === 1 || o.trangThai === 3) // Đã xác nhận hoặc đã giao
      .reduce((sum, o) => sum + Number(o.tongTien || 0), 0)
  })

  /**
   * Computed - Đơn hàng đang chờ xử lý
   */
  const pendingOrders = computed(() => {
    return orders.value.filter(o => o.trangThai === 0)
  })

  /**
   * Reset state
   */
  function resetState() {
    orders.value = []
    currentOrder.value = null
    trackingInfo.value = null
    loading.value = false
  }

  return {
    // State
    orders,
    currentOrder,
    trackingInfo,
    loading,
    
    // Computed
    sortedOrders,
    totalOrders,
    totalSpent,
    pendingOrders,
    
    // Methods
    fetchMyOrders,
    searchOrder,
    searchOrderByPhone,
    getOrderTracking,
    getOrderDetail,
    getOrdersByStatus,
    resetState
  }
}