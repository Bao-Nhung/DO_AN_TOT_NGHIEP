<template>
  <Transition name="z-confirm-fade">
    <div v-if="confirmState.open" class="z-confirm-overlay" @click.self="resolveConfirm(false)">
      <div class="z-confirm-modal" role="dialog" aria-modal="true" :aria-label="confirmState.title">
        <div class="z-confirm-icon" :class="confirmState.variant">
          <i class="bi" :class="iconClass"></i>
        </div>
        <div class="z-confirm-content">
          <h3>{{ confirmState.title }}</h3>
          <p>{{ confirmState.message }}</p>
        </div>
        <div class="z-confirm-actions">
          <button class="lm-btn-secondary z-confirm-cancel" @click="resolveConfirm(false)">
            {{ confirmState.cancelText }}
          </button>
          <button class="lm-btn-primary z-confirm-accept" :class="confirmState.variant" @click="resolveConfirm(true)">
            <span>{{ confirmState.confirmText }}</span>
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useConfirm } from '@/composables/useConfirm'

const { confirmState, resolveConfirm } = useConfirm()

const iconClass = computed(() => {
  if (confirmState.variant === 'danger') return 'bi-exclamation-triangle'
  if (confirmState.variant === 'success') return 'bi-check2-circle'
  return 'bi-question-circle'
})

function onKeydown(event) {
  if (!confirmState.open) return
  if (event.key === 'Escape') resolveConfirm(false)
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.z-confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 12000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(17, 17, 20, 0.56);
  backdrop-filter: blur(3px);
}
.z-confirm-modal {
  width: min(420px, 100%);
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.22);
  padding: 28px;
  text-align: center;
}
.z-confirm-icon {
  width: 54px;
  height: 54px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--z-accent-soft);
  color: var(--z-accent);
  font-size: 24px;
}
.z-confirm-icon.danger {
  background: #fee2e2;
  color: #dc2626;
}
.z-confirm-icon.success {
  background: #dcfce7;
  color: #15803d;
}
.z-confirm-content h3 {
  margin: 0 0 8px;
  font-size: 19px;
  font-weight: 700;
  color: var(--z-dark);
}
.z-confirm-content p {
  margin: 0;
  color: var(--z-gray);
  font-size: 14px;
  line-height: 1.65;
}
.z-confirm-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 24px;
}
.z-confirm-cancel,
.z-confirm-accept {
  justify-content: center;
  min-height: 42px;
}
.z-confirm-accept.danger {
  background: #dc2626;
  border-color: #dc2626;
}
.z-confirm-accept.danger:hover:not(:disabled) {
  background: #b91c1c;
  border-color: #b91c1c;
  color: #ffffff;
}
.z-confirm-fade-enter-active,
.z-confirm-fade-leave-active {
  transition: opacity 0.18s ease;
}
.z-confirm-fade-enter-from,
.z-confirm-fade-leave-to {
  opacity: 0;
}
@media (max-width: 480px) {
  .z-confirm-modal { padding: 24px 20px; }
  .z-confirm-actions { grid-template-columns: 1fr; }
}
</style>
