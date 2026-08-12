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
  bottom: 24px;
  right: 24px;
  background: var(--z-white);
  padding: 16px 22px;
  border-radius: var(--z-radius-lg);
  box-shadow: 0 10px 35px rgba(27,27,31,0.08);
  font-size: 13px;
  font-weight: 600;
  color: var(--z-dark);
  z-index: 99999;
  max-width: 360px;
  border-left: 5px solid var(--z-gray-border);
  transition: transform 0.24s ease, opacity 0.24s ease;
  transform: translateY(24px);
  opacity: 0;
}

#lm-toast.show {
  transform: translateY(0);
  opacity: 1;
}

/* Success */
#lm-toast.lm-toast-success { border-left-color: #10b981; background: #f0fdf4; }
#lm-toast.lm-toast-success .lm-toast-icon { color: #10b981; }

/* Error */
#lm-toast.lm-toast-error { border-left-color: #ef4444; background: #fef2f2; }
#lm-toast.lm-toast-error .lm-toast-icon { color: #ef4444; }

/* Warning */
#lm-toast.lm-toast-warning { border-left-color: #f59e0b; background: #fffbeb; }
#lm-toast.lm-toast-warning .lm-toast-icon { color: #f59e0b; }

.lm-toast-content { display: flex; align-items: center; gap: 10px; }
.lm-toast-icon { font-size: 18px; flex-shrink: 0; }
.lm-toast-message { flex: 1; line-height: 1.45; }
</style>
