// composables/useReveal.js
import { nextTick, onMounted, onUnmounted } from 'vue'

export function useReveal() {
  const selector = '.lm-reveal, .lm-reveal-left, .lm-reveal-right'
  let observer = null
  let mutationObserver = null

  function observeElement(element) {
    if (!(element instanceof HTMLElement) || element.dataset.revealObserved === 'true') return
    element.dataset.revealObserved = 'true'
    observer?.observe(element)
  }

  function observeTree(root) {
    if (!(root instanceof Element)) return
    if (root.matches(selector)) observeElement(root)
    root.querySelectorAll(selector).forEach(observeElement)
  }

  function initReveal() {
    if (!('IntersectionObserver' in window) || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return
    }

    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          if (entry.target.dataset.revealAnimated !== 'true') {
            entry.target.dataset.revealAnimated = 'true'
            const startTransform = entry.target.classList.contains('lm-reveal-left')
              ? 'translateX(-18px)'
              : entry.target.classList.contains('lm-reveal-right')
                ? 'translateX(18px)'
                : 'translateY(18px)'
            entry.target.animate([
              { opacity: 0.72, transform: startTransform },
              { opacity: 1, transform: 'translate(0)' }
            ], { duration: 460, easing: 'cubic-bezier(.2,.7,.2,1)' })
          }
          observer.unobserve(entry.target)
        }
      })
    }, { threshold: 0.05, rootMargin: '0px 0px -24px 0px' })

    document.querySelectorAll(selector).forEach(observeElement)
    mutationObserver = new MutationObserver(records => {
      records.forEach(record => record.addedNodes.forEach(observeTree))
    })
    mutationObserver.observe(document.body, { childList: true, subtree: true })
  }

  onMounted(async () => {
    await nextTick()
    window.requestAnimationFrame(initReveal)
  })
  onUnmounted(() => {
    observer?.disconnect()
    mutationObserver?.disconnect()
  })
}
