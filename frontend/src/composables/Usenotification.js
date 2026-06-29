import { ref } from 'vue'

// State quản lý toast
const message = ref('')
const visible = ref(false)
const type = ref('default') // success, error, info, warning, default
let hideTimeout = null

/**
 * Composable cho Toast Notification
 * Hỗ trợ các loại: success, error, info, warning, default
 */
export function useToast() {
  /**
   * Hiển thị toast message
   * @param {string} msg - Nội dung tin nhắn
   * @param {string} notificationType - Loại thông báo (success, error, info, warning, default)
   * @param {number} duration - Thời gian hiển thị (ms), default 3000
   */
  function showToast(msg, notificationType = 'default', duration = 3000) {
    // Xóa timeout cũ nếu có
    if (hideTimeout) {
      clearTimeout(hideTimeout)
    }

    message.value = msg
    type.value = notificationType
    visible.value = true

    // Tự động ẩn sau duration
    hideTimeout = setTimeout(() => {
      visible.value = false
    }, duration)
  }

  /**
   * Hiển thị toast success
   */
  function showSuccess(msg, duration = 3000) {
    showToast(msg, 'success', duration)
  }

  /**
   * Hiển thị toast error
   */
  function showError(msg, duration = 3000) {
    showToast(msg, 'error', duration)
  }

  /**
   * Hiển thị toast info
   */
  function showInfo(msg, duration = 3000) {
    showToast(msg, 'info', duration)
  }

  /**
   * Hiển thị toast warning
   */
  function showWarning(msg, duration = 3000) {
    showToast(msg, 'warning', duration)
  }

  /**
   * Ẩn toast ngay lập tức
   */
  function hideToast() {
    if (hideTimeout) {
      clearTimeout(hideTimeout)
    }
    visible.value = false
  }

  return {
    message,
    visible,
    type,
    showToast,
    showSuccess,
    showError,
    showInfo,
    showWarning,
    hideToast
  }
}

/**
 * Composable cho Notification System
 * Quản lý thông báo trong app
 */
export function useNotification() {
  const notifications = ref([])
  const unreadCount = ref(0)

  /**
   * Thêm notification vào danh sách
   */
  function addNotification(notification) {
    const id = Date.now() + Math.random()
    const newNotif = {
      id,
      ...notification,
      timestamp: new Date()
    }
    notifications.value.unshift(newNotif)
    return id
  }

  /**
   * Xóa notification
   */
  function removeNotification(id) {
    notifications.value = notifications.value.filter(n => n.id !== id)
  }

  /**
   * Xóa tất cả notification
   */
  function clearNotifications() {
    notifications.value = []
  }

  /**
   * Cập nhật unread count
   */
  function setUnreadCount(count) {
    unreadCount.value = count
  }

  /**
   * Tăng unread count
   */
  function incrementUnreadCount() {
    unreadCount.value++
  }

  /**
   * Giảm unread count
   */
  function decrementUnreadCount() {
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  /**
   * Notification factory - Đơn hàng tạo thành công
   */
  function notifyOrderCreated(orderCode, amount) {
    return addNotification({
      type: 'order_created',
      title: `✓ Đơn hàng ${orderCode} được tạo`,
      message: `Đơn hàng trị giá ${formatMoney(amount)} đã được tạo thành công. Cảm ơn bạn!`,
      action: {
        label: 'Xem chi tiết',
        callback: () => {
          // Navigate to order detail
        }
      }
    })
  }

  /**
   * Notification factory - Đơn hàng được xác nhận
   */
  function notifyOrderConfirmed(orderCode) {
    return addNotification({
      type: 'order_confirmed',
      title: `Đơn hàng ${orderCode} đã xác nhận`,
      message: 'Đơn hàng của bạn đã được xác nhận. Chúng tôi sẽ chuẩn bị giao ngay.',
      action: {
        label: 'Theo dõi'
      }
    })
  }

  /**
   * Notification factory - Đơn hàng đang giao
   */
  function notifyOrderShipping(orderCode) {
    return addNotification({
      type: 'order_shipping',
      title: `🚚 Đơn hàng ${orderCode} đang giao`,
      message: 'Đơn hàng của bạn đang trên đường tới với bạn.',
      action: {
        label: 'Xem tracking'
      }
    })
  }

  /**
   * Notification factory - Đơn hàng đã giao
   */
  function notifyOrderDelivered(orderCode) {
    return addNotification({
      type: 'order_delivered',
      title: `✅ Đơn hàng ${orderCode} đã giao`,
      message: 'Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã tin tưởng ZESTIA!',
      action: {
        label: 'Xem đánh giá'
      }
    })
  }

  /**
   * Notification factory - Thêm vào giỏ thành công
   */
  function notifyAddToCart(productName, quantity) {
    return addNotification({
      type: 'add_to_cart',
      title: 'Thêm vào giỏ thành công',
      message: `${quantity}x ${productName} đã được thêm vào giỏ hàng.`,
      action: {
        label: 'Xem giỏ'
      }
    })
  }

  /**
   * Notification factory - Lỗi
   */
  function notifyError(title, message) {
    return addNotification({
      type: 'error',
      title,
      message
    })
  }

  /**
   * Notification factory - Thành công
   */
  function notifySuccess(title, message) {
    return addNotification({
      type: 'success',
      title,
      message
    })
  }

  return {
    notifications,
    unreadCount,
    addNotification,
    removeNotification,
    clearNotifications,
    setUnreadCount,
    incrementUnreadCount,
    decrementUnreadCount,
    notifyOrderCreated,
    notifyOrderConfirmed,
    notifyOrderShipping,
    notifyOrderDelivered,
    notifyAddToCart,
    notifyError,
    notifySuccess
  }
}

/**
 * Helper function - Format tiền tệ
 */
function formatMoney(amount) {
  return Number(amount || 0).toLocaleString('vi-VN') + 'đ'
}