<template>
  <div id="lm-toast" :class="['lm-toast', { show: visible }, `lm-toast-${type}`]">
    <div class="lm-toast-content">
      <i :class="['bi', iconClass]" class="lm-toast-icon"></i>
      <span class="lm-toast-message">{{ message }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useToast } from '@/composables/useToast'

const { message, visible, type } = useToast()

const iconClass = computed(() => {
  const icons = {
    success: 'bi-check-circle-fill',
    error: 'bi-exclamation-circle-fill',
    info: 'bi-info-circle-fill',
    warning: 'bi-exclamation-triangle-fill',
    default: 'bi-info-circle'
  }
  return icons[type.value] || icons.default
})
</script>

<style scoped>
#lm-toast {
  position: fixed;
  bottom: -100px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--z-white);
  padding: 14px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  font-size: 14px;
  font-weight: 500;
  color: var(--z-dark);
  z-index: 9999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  max-width: 400px;
  border-left: 4px solid var(--z-gray-border);
}

#lm-toast.show { bottom: 24px; }

/* Success */
#lm-toast.lm-toast-success { border-left-color: #10b981; background: linear-gradient(135deg, #f0fdf4 0%, #f9fdf7 100%); }
#lm-toast.lm-toast-success .lm-toast-icon { color: #10b981; }

/* Error */
#lm-toast.lm-toast-error { border-left-color: #ef4444; background: linear-gradient(135deg, #fef2f2 0%, #fdf7f7 100%); }
#lm-toast.lm-toast-error .lm-toast-icon { color: #ef4444; }

/* Warning */
#lm-toast.lm-toast-warning { border-left-color: #f59e0b; background: linear-gradient(135deg, #fffbeb 0%, #fdf9f3 100%); }
#lm-toast.lm-toast-warning .lm-toast-icon { color: #f59e0b; }

.lm-toast-content { display: flex; align-items: center; gap: 10px; }
.lm-toast-icon { font-size: 18px; flex-shrink: 0; }
.lm-toast-message { flex: 1; line-height: 1.4; }
</style>