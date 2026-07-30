import { computed } from 'vue'
import { i18n, I18N_STORAGE_KEY, SUPPORTED_LOCALES } from '@/i18n'
import { refreshDomTranslations, translateUiText } from '@/i18n/domTranslator'

function syncDocumentLanguage(locale) {
  if (typeof document !== 'undefined') {
    document.documentElement.lang = locale
  }
}

syncDocumentLanguage(i18n.global.locale.value)

export function useI18n() {
  function t(key, fallback = '') {
    const translated = i18n.global.t(key)
    return translated === key ? (fallback || key) : translated
  }

  function setLocale(locale) {
    if (!SUPPORTED_LOCALES.includes(locale)) return
    i18n.global.locale.value = locale
    syncDocumentLanguage(locale)
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(I18N_STORAGE_KEY, locale)
      window.dispatchEvent(new CustomEvent('zestia-locale-changed', { detail: { locale } }))
      queueMicrotask(() => refreshDomTranslations())
    }
  }

  function toggleLocale() {
    setLocale(i18n.global.locale.value === 'vi' ? 'en' : 'vi')
  }

  return {
    locale: computed(() => i18n.global.locale.value),
    isEn: computed(() => i18n.global.locale.value === 'en'),
    t,
    translateUiText,
    setLocale,
    toggleLocale,
  }
}
