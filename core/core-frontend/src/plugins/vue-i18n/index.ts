import type { App } from 'vue'
import { createI18n } from 'vue-i18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type { I18n, I18nOptions } from 'vue-i18n'
import { setHtmlPageLang } from './helper'
export let i18n: ReturnType<typeof createI18n>
import { PATH_URL } from '@/config/axios/service'
const createI18nOptions = async (): Promise<I18nOptions> => {
  const localeStore = useLocaleStoreWithOut()
  let locale = localeStore.getCurrentLocale
  const localeMap = await localeStore.getLocaleMap
  const cMap: any = localeMap.find(item => {
    return item.lang === locale.lang
  })
  let defaultLocal = null
  if (cMap) {
    if (cMap['custom']) {
      defaultLocal = await loadRemoteI18n(cMap)
    } else {
      defaultLocal = await import(`../../locales/${locale.lang}.ts`)
    }
  } else {
    const item = localeMap[0]
    localeStore.setLang(item.lang)
    locale = localeStore.getCurrentLocale
    defaultLocal = await import(`../../locales/${locale.lang}.ts`)
  }
  const message = defaultLocal.default ?? {}

  setHtmlPageLang(locale.lang)

  localeStore.setCurrentLocale({
    lang: locale.lang
  })

  return {
    legacy: false,
    locale: locale.lang,
    fallbackLocale: locale.lang,
    messages: {
      [locale.lang]: message
    },
    availableLocales: localeMap.map(v => v.lang),
    sync: true,
    silentTranslationWarn: true,
    missingWarn: false,
    silentFallbackWarn: true
  }
}

const loadRemoteI18n = async (option: any) => {
  const name = option.lang.replace('-', '_')
  const path =
    PATH_URL.startsWith('./') && PATH_URL.length > 2
      ? window.location.pathname + PATH_URL.substring(2)
      : PATH_URL
  const url = `${path}/i18n/custom_${name}_front_${option.name}.js`
  return await import(url)
}

export const setupI18n = async (app: App<Element>) => {
  const options = await createI18nOptions()
  i18n = createI18n(options) as I18n
  app.use(i18n)
}
