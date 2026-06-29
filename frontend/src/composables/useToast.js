// composables/useToast.js
import { ref } from 'vue'

const message = ref('')
const visible = ref(false)
const type = ref('default')   // 👈 thêm dòng này
let timer = null

export function useToast() {
  function showToast(msg, toastType = 'default') {   // 👈 cho phép truyền type khi gọi
    message.value = msg
    type.value = toastType                            // 👈 set type
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => { visible.value = false }, 3000)
  }
  return { message, visible, type, showToast }   // 👈 export thêm type
}