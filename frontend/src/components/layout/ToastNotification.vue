<template>
  <div id="lm-toast" :class="{ show: visible }">
    <span class="lm-toast-gold">✦</span> {{ message }}
  </div>
</template>

<script setup>
import { useToast } from '@/composables/useToast'
const { message, visible } = useToast()
</script>
