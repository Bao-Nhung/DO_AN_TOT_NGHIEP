// composables/useReveal.js
import { onMounted, onUnmounted } from 'vue'

export function useReveal() {
  function initReveal() {
    const els = document.querySelectorAll('.lm-reveal, .lm-reveal-left, .lm-reveal-right')
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry, i) => {
        if (entry.isIntersecting) {
          setTimeout(() => entry.target.classList.add('visible'), i * 80)
          observer.unobserve(entry.target)
        }
      })
    }, { threshold: 0.1 })
    els.forEach(el => observer.observe(el))
    return observer
  }

  let observer = null
  onMounted(() => {
    // Small delay to let DOM settle after route transition
    setTimeout(() => { observer = initReveal() }, 600)
  })
  onUnmounted(() => { if (observer) observer.disconnect() })
}
