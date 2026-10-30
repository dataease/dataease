import type { App } from 'vue'
import { createI18n } from 'vue-i18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type { I18n, I18nOptions } from 'vue-i18n'
import { setHtmlPageLang } from './helper'
export let i18n: ReturnType<typeof createI18n>
import { getLocale } from '@/utils/utils'
import { useCache } from '@/hooks/web/useCache'
import { loadLocaleMessages } from './loader'
const createI18nOptions = async (): Promise<I18nOptions> => {
  const localeStore = useLocaleStoreWithOut()
  const localeMap = await localeStore.getLocaleMap
  const loaded = await loadLocaleMessages(getLocale(), localeMap)
  const locale = loaded.locale
  useCache().wsCache.set('user.language', locale)
  const message = loaded.messages

  setHtmlPageLang(locale)

  localeStore.setCurrentLocale({
    lang: locale
  })

  return {
    legacy: false,
    locale: locale,
    fallbackLocale: locale,
    messages: {
      [locale]: message
    },
    availableLocales: localeMap.map(v => v.lang),
    sync: true,
    silentTranslationWarn: true,
    missingWarn: false,
    silentFallbackWarn: true
  }
}

export const setupI18n = async (app: App<Element>) => {
  const options = await createI18nOptions()
  i18n = createI18n(options) as I18n
  app.use(i18n)
}
