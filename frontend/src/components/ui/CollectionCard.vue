<template>
  <RouterLink class="lm-collection-card position-relative overflow-hidden"
              :to="to || '/collections'"
              :style="{ height: tall ? '100%' : 'auto' }">
    <div class="lm-card-image w-100 position-relative overflow-hidden"
         :style="{ aspectRatio: tall ? 'unset' : '3/4', height: tall ? '100%' : 'auto' }">
      <div v-if="image" class="lm-card-image-inner w-100 h-100">
        <img :src="image" :alt="name" />
      </div>
      <div v-else class="lm-card-image-inner lm-card-placeholder w-100 h-100 d-flex align-items-center justify-content-center"
           :style="{ background: bg }">
        {{ letter }}
      </div>
      <div class="lm-card-overlay position-absolute inset-0 w-100 h-100"></div>
      <div class="lm-card-info position-absolute bottom-0 start-0 end-0 p-3">
        <div class="lm-card-label lm-eyebrow mb-1">{{ label }}</div>
        <div class="lm-card-name lm-display">{{ name }}</div>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
defineProps({
  label:  String,
  name:   String,
  letter: String,
  bg:     String,
  image:  String,
  tall:   Boolean,
  to:     [String, Object],
})
</script>

<style scoped>
.lm-collection-card {
  display: block;
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  color: inherit;
  cursor: pointer;
}
.lm-card-image-inner { transition: transform 0.8s cubic-bezier(0.25,0.46,0.45,0.94); }
.lm-card-image-inner img { width: 100%; height: 100%; display: block; object-fit: cover; }
.lm-card-placeholder {
  color: rgba(255,255,255,0.2);
  font-family: var(--z-font-display);
  font-size: 60px;
  font-style: italic;
  font-weight: 300;
}
.lm-collection-card:hover .lm-card-image-inner { transform: scale(1.06); }
.lm-card-overlay {
  background: rgba(27, 27, 31, 0.14);
  opacity: 1;
  transition: background-color 0.3s ease;
}
.lm-collection-card:hover .lm-card-overlay { background: rgba(27, 27, 31, 0.24); }
.lm-card-info {
  padding: 14px 16px !important;
  border-top: 1px solid rgba(255,255,255,0.16);
  background: rgba(27, 27, 31, 0.78);
  transition: background-color 0.3s ease;
}
.lm-collection-card:hover .lm-card-info { background: rgba(27, 27, 31, 0.9); }
.lm-card-label { color: var(--z-accent-light); }
.lm-card-name { color: var(--z-white); font-size: 22px; font-weight: 400; }
@media (max-width: 576px) { .lm-card-name { font-size: 18px; } }
</style>
