<template>
  <div class="lm-marquee-strip">
    <div class="lm-marquee-track">
      <template v-for="_ in 2" :key="_">
        <template v-for="item in items" :key="item">
          <span>{{ item }}</span><span class="sep">&#10022;</span>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
const items = [
  'Bo Suu Tap Moi', 'Chat Lieu Cao Cap', 'Thiet Ke Viet Nam',
  'Giao Hang Toan Quoc', 'Mien Phi Doi Tra 30 Ngay', 'Zestia Fashion'
]
</script>
