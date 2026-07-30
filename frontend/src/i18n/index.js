import { createI18n } from 'vue-i18n'
import { messages } from './messages'

export const I18N_STORAGE_KEY = 'zestia_lang'
export const SUPPORTED_LOCALES = ['vi', 'en']

export function initialLocale() {
  if (typeof window === 'undefined') return 'vi'
  const saved = window.localStorage.getItem(I18N_STORAGE_KEY)
  return SUPPORTED_LOCALES.includes(saved) ? saved : 'vi'
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: initialLocale(),
  fallbackLocale: 'vi',
  missingWarn: false,
  fallbackWarn: false,
  messages,
})

export function currentLocale() {
  return i18n.global.locale.value
}
