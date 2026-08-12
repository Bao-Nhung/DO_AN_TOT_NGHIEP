import { ref } from 'vue'

export function useNotification() {
  const notifications = ref([])
  const unreadCount = ref(0)

  function addNotification(notification) {
    const id = Date.now() + Math.random()
    const newNotif = { id, ...notification, timestamp: new Date() }
    notifications.value.unshift(newNotif)
    return id
  }

  function removeNotification(id) {
    notifications.value = notifications.value.filter(n => n.id !== id)
  }

  function clearNotifications() { notifications.value = [] }
  function setUnreadCount(count) { unreadCount.value = count }
  function incrementUnreadCount() { unreadCount.value++ }
  function decrementUnreadCount() { if (unreadCount.value > 0) unreadCount.value-- }

  function notifyOrderCreated(orderCode, amount) {
    return addNotification({
      type: 'order_created',
      title: `Đơn hàng ${orderCode} được tạo`,
      message: `Đơn hàng trị giá ${formatMoney(amount)} đã được tạo thành công. Cảm ơn bạn!`,
      action: { label: 'Xem chi tiết', callback: () => {} }
    })
  }

  function notifyOrderConfirmed(orderCode) {
    return addNotification({
      type: 'order_confirmed',
      title: `Đơn hàng ${orderCode} đã xác nhận`,
      message: 'Đơn hàng của bạn đã được xác nhận. Chúng tôi sẽ chuẩn bị giao ngay.',
      action: { label: 'Theo dõi' }
    })
  }

  function notifyOrderShipping(orderCode) {
    return addNotification({
      type: 'order_shipping',
      title: `Đơn hàng ${orderCode} đang giao`,
      message: 'Đơn hàng của bạn đang trên đường tới với bạn.',
      action: { label: 'Xem tracking' }
    })
  }

  function notifyOrderDelivered(orderCode) {
    return addNotification({
      type: 'order_delivered',
      title: `Đơn hàng ${orderCode} đã giao`,
      message: 'Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã tin tưởng ZESTIA!',
      action: { label: 'Xem đánh giá' }
    })
  }

  function notifyAddToCart(productName, quantity) {
    return addNotification({
      type: 'add_to_cart',
      title: 'Thêm vào giỏ thành công',
      message: `${quantity}x ${productName} đã được thêm vào giỏ hàng.`,
      action: { label: 'Xem giỏ' }
    })
  }

  function notifyError(title, message) { return addNotification({ type: 'error', title, message }) }
  function notifySuccess(title, message) { return addNotification({ type: 'success', title, message }) }

  return {
    notifications, unreadCount, addNotification, removeNotification, clearNotifications,
    setUnreadCount, incrementUnreadCount, decrementUnreadCount,
    notifyOrderCreated, notifyOrderConfirmed, notifyOrderShipping, notifyOrderDelivered,
    notifyAddToCart, notifyError, notifySuccess
  }
}

function formatMoney(amount) {
  return Number(amount || 0).toLocaleString('vi-VN') + 'đ'
}
