<template>
  <label class="z-page-size">
    <span>{{ t('common.pageSize') }}</span>
    <select
      :value="modelValue"
      :aria-label="t('common.itemsPerPage')"
      @change="$emit('update:modelValue', Number($event.target.value))"
    >
      <option v-for="option in options" :key="option" :value="option">{{ option }}</option>
    </select>
    <span>{{ t('common.itemsPerPage') }}</span>
  </label>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'

defineProps({
  modelValue: { type: Number, required: true },
  options: { type: Array, default: () => [5, 10, 20, 50] },
})
defineEmits(['update:modelValue'])
const { t } = useI18n()
</script>

<style scoped>
.z-page-size {
  display: inline-flex; align-items: center; gap: 7px;
  color: var(--z-gray); font-size: 11px; white-space: nowrap;
}
.z-page-size select {
  min-width: 58px; height: 32px; padding: 4px 24px 4px 8px;
  border: 1px solid var(--z-gray-border); border-radius: 4px;
  background: var(--z-white); color: var(--z-dark); font-size: 12px;
}
.z-page-size select:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--z-accent) 35%, transparent);
  outline-offset: 1px;
}
@media (max-width: 575px) {
  .z-page-size span:last-child { display: none; }
}
</style>
