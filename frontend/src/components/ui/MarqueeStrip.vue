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
  'Bộ Sưu Tập Mới', 'Chất Liệu Cao Cấp', 'Thiết Kế Việt Nam',
  'Giao Hàng Toàn Quốc', 'Miễn Phí Đổi Trả 30 Ngày', 'Zestia Fashion'
]
</script>
