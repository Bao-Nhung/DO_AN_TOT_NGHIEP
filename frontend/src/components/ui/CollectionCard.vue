<template>
  <div class="lm-collection-card position-relative overflow-hidden cursor-pointer"
       :style="{ height: tall ? '100%' : 'auto' }"
       @click="$router.push(to || '/collections')">
    <div class="lm-card-image w-100 position-relative overflow-hidden"
         :style="{ aspectRatio: tall ? 'unset' : '3/4', height: tall ? '100%' : 'auto' }">
      <div v-if="image" class="lm-card-image-inner w-100 h-100">
        <img :src="image" :alt="name" style="width:100%;height:100%;object-fit:cover" />
      </div>
      <div v-else class="lm-card-image-inner w-100 h-100 d-flex align-items-center justify-content-center"
           :style="{ background: bg, fontFamily: 'var(--z-font-display)', fontSize: '60px', color: 'rgba(255,255,255,0.15)', fontWeight: '300', fontStyle: 'italic' }">
        {{ letter }}
      </div>
      <div class="lm-card-overlay position-absolute inset-0 w-100 h-100"></div>
      <div class="lm-card-info position-absolute bottom-0 start-0 end-0 p-3">
        <div class="lm-card-label lm-eyebrow mb-1">{{ label }}</div>
        <div class="lm-card-name lm-display" style="font-size:24px;font-weight:400;color:var(--z-white)">{{ name }}</div>
      </div>
    </div>
  </div>
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
.lm-collection-card { cursor: pointer; background: var(--z-bg-alt); }
.lm-card-image-inner { transition: transform 0.8s cubic-bezier(0.25,0.46,0.45,0.94); }
.lm-collection-card:hover .lm-card-image-inner { transform: scale(1.06); }
.lm-card-overlay {
  background: linear-gradient(to top, rgba(26,26,24,0.7) 0%, transparent 60%);
  opacity: 0; transition: opacity 0.4s ease;
}
.lm-collection-card:hover .lm-card-overlay { opacity: 1; }
.lm-card-info { transform: translateY(10px); transition: transform 0.4s ease; }
.lm-collection-card:hover .lm-card-info { transform: translateY(0); }
.lm-card-label { opacity: 0; transition: opacity 0.4s ease 0.1s; }
.lm-card-name  { opacity: 0; transition: opacity 0.4s ease 0.15s; }
.lm-collection-card:hover .lm-card-label,
.lm-collection-card:hover .lm-card-name { opacity: 1; }
@media (hover: none), (max-width: 768px) {
  .lm-card-overlay { opacity: 1; }
  .lm-card-info { transform: translateY(0); }
  .lm-card-label, .lm-card-name { opacity: 1; }
}
</style>
