<template>
  <div class="lm-marquee-strip">
    <div class="lm-marquee-track">
      <template v-for="_ in 2" :key="_">
        <template v-for="item in displayItems" :key="item">
          <span>{{ item }}</span><span class="sep">&#10022;</span>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const displayItems = computed(() => props.items.length
  ? props.items
  : ['Bộ Sưu Tập Mới', 'Thiết Kế Việt Nam', 'Zestia Fashion'])
</script>
