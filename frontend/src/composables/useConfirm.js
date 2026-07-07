import { reactive } from 'vue'

const state = reactive({
  open: false,
  title: '',
  message: '',
  confirmText: 'Xác nhận',
  cancelText: 'Hủy',
  variant: 'primary',
  resolving: null
})

export function useConfirm() {
  function confirmDialog(options = {}) {
    state.title = options.title || 'Xác nhận thao tác'
    state.message = options.message || 'Bạn có chắc muốn tiếp tục?'
    state.confirmText = options.confirmText || 'Xác nhận'
    state.cancelText = options.cancelText || 'Hủy'
    state.variant = options.variant || 'primary'
    state.open = true

    return new Promise(resolve => {
      state.resolving = resolve
    })
  }

  function resolveConfirm(value) {
    state.open = false
    const resolve = state.resolving
    state.resolving = null
    if (resolve) resolve(value)
  }

  return { confirmState: state, confirmDialog, resolveConfirm }
}
