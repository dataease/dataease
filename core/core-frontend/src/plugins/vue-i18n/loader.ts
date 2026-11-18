import type { LocaleMessageObject, VueMessageType } from 'vue-i18n'
import type { LocaleDropdownType } from 'types/localeDropdown'
import { PATH_URL } from '@/config/axios/service'
import { DEFAULT_LOCALE, resolveLocale } from '@/utils/locale'

export const loadLocaleMessages = async (language: string, options: LocaleDropdownType[]) => {
  let locale = resolveLocale(
    [language],
    options.map(item => item.lang)
  )
  const option = options.find(item => item.lang === locale)
  let messages: { default?: LocaleMessageObject<VueMessageType> }
  if (option?.custom) {
    const name = option.lang.replace('-', '_')
    const path =
      PATH_URL.startsWith('./') && PATH_URL.length > 2
        ? window.location.pathname + PATH_URL.substring(2)
        : PATH_URL
    const url = `${path}/i18n/custom_${name}_front_${option.name}.js`
    try {
      messages = await import(/* @vite-ignore */ url)
    } catch {
      // A removed or unavailable custom pack must not prevent login or cause reload loops.
      locale = DEFAULT_LOCALE
      messages = await import('../../locales/zh-CN')
    }
  } else {
    messages = await import(`../../locales/${locale}.ts`)
  }
  return { locale, messages: messages.default ?? {} }
}
