<template></template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

const overlaySelector = '.z-modal-overlay, .z-confirm-overlay'
const modalSelector = '.z-modal, .z-confirm-modal, [role="dialog"]'
const focusableSelector = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled]):not([type="hidden"])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])'
].join(',')

const activeDialogs = new Map()
let observer
let headingSequence = 0
let previousOverflow = ''

function visibleOverlays() {
  return [...document.querySelectorAll(overlaySelector)]
    .filter(overlay => getComputedStyle(overlay).display !== 'none')
}

function setupOverlay(overlay) {
  if (!(overlay instanceof HTMLElement) || activeDialogs.has(overlay)) return
  const modal = overlay.matches(modalSelector) ? overlay : overlay.querySelector(modalSelector) || overlay.firstElementChild
  if (!(modal instanceof HTMLElement)) return

  const heading = modal.querySelector('h1, h2, h3')
  modal.setAttribute('role', 'dialog')
  modal.setAttribute('aria-modal', 'true')
  if (!modal.hasAttribute('tabindex')) modal.setAttribute('tabindex', '-1')
  if (heading) {
    if (!heading.id) heading.id = `z-dialog-title-${++headingSequence}`
    modal.setAttribute('aria-labelledby', heading.id)
  } else if (!modal.hasAttribute('aria-label')) {
    modal.setAttribute('aria-label', 'Hộp thoại')
  }

  activeDialogs.set(overlay, { modal, previousFocus: document.activeElement })
  if (activeDialogs.size === 1) {
    previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
  }
  requestAnimationFrame(() => {
    const first = modal.querySelector(focusableSelector)
    ;(first || modal).focus({ preventScroll: true })
  })
}

function teardownOverlay(overlay) {
  const state = activeDialogs.get(overlay)
  if (!state) return
  activeDialogs.delete(overlay)
  if (activeDialogs.size === 0) document.body.style.overflow = previousOverflow
  if (state.previousFocus instanceof HTMLElement && document.contains(state.previousFocus)) {
    state.previousFocus.focus({ preventScroll: true })
  }
}

function processAdded(node) {
  if (!(node instanceof Element)) return
  if (node.matches(overlaySelector)) setupOverlay(node)
  node.querySelectorAll(overlaySelector).forEach(setupOverlay)
}

function processRemoved(node) {
  if (!(node instanceof Element)) return
  if (activeDialogs.has(node)) teardownOverlay(node)
  node.querySelectorAll(overlaySelector).forEach(teardownOverlay)
}

function topDialog() {
  const overlays = visibleOverlays()
  return overlays.length ? overlays[overlays.length - 1] : null
}

function handleKeydown(event) {
  const overlay = topDialog()
  if (!overlay) return
  const state = activeDialogs.get(overlay)
  if (!state) return

  if (event.key === 'Escape') {
    event.preventDefault()
    overlay.click()
    return
  }
  if (event.key !== 'Tab') return

  const focusable = [...state.modal.querySelectorAll(focusableSelector)]
    .filter(element => element instanceof HTMLElement && element.offsetParent !== null)
  if (!focusable.length) {
    event.preventDefault()
    state.modal.focus()
    return
  }
  const first = focusable[0]
  const last = focusable[focusable.length - 1]
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

onMounted(() => {
  visibleOverlays().forEach(setupOverlay)
  observer = new MutationObserver(records => {
    records.forEach(record => {
      record.addedNodes.forEach(processAdded)
      record.removedNodes.forEach(processRemoved)
    })
  })
  observer.observe(document.body, { childList: true, subtree: true })
  document.addEventListener('keydown', handleKeydown, true)
})

onUnmounted(() => {
  observer?.disconnect()
  document.removeEventListener('keydown', handleKeydown, true)
  activeDialogs.forEach((_, overlay) => teardownOverlay(overlay))
})
</script>
