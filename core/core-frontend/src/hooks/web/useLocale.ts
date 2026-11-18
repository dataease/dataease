import { i18n } from '@/plugins/vue-i18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { setHtmlPageLang } from '@/plugins/vue-i18n/helper'
import { loadLocaleMessages } from '@/plugins/vue-i18n/loader'
const setI18nLanguage = (locale: string) => {
  const localeStore = useLocaleStoreWithOut()

  if (i18n.mode === 'legacy') {
    i18n.global.locale = locale
  } else {
    ;(i18n.global.locale as any).value = locale
  }
  localeStore.setCurrentLocale({
    lang: locale
  })
  setHtmlPageLang(locale)
}

export const useLocale = () => {
  const changeLocale = async (language: string) => {
    const localeStore = useLocaleStoreWithOut()
    const loaded = await loadLocaleMessages(language, await localeStore.getLocaleMap)
    i18n.global.setLocaleMessage(loaded.locale, loaded.messages)
    setI18nLanguage(loaded.locale)
    return loaded.locale
  }

  return { changeLocale }
}
