import { ref } from 'vue'

const message = ref('')
const visible = ref(false)
const type = ref('default') // success, error, info, warning, default
let timer = null

export function useToast() {
  function showToast(msg, toastType = 'default', duration = 3000) {
    message.value = msg
    type.value = toastType
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => { visible.value = false }, duration)
  }

  function showSuccess(msg, duration = 3000) { showToast(msg, 'success', duration) }
  function showError(msg, duration = 3000) { showToast(msg, 'error', duration) }
  function showInfo(msg, duration = 3000) { showToast(msg, 'info', duration) }
  function showWarning(msg, duration = 3000) { showToast(msg, 'warning', duration) }
  
  function hideToast() {
    clearTimeout(timer)
    visible.value = false
  }

  return {
    message, visible, type,
    showToast, showSuccess, showError, showInfo, showWarning, hideToast
  }
}