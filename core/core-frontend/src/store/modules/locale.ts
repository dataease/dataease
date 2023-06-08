import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
import { LocaleDropdownType } from 'types/localeDropdown'
import zhCn from 'element-plus-secondary/es/locale/lang/zh-cn'
import en from 'element-plus-secondary/es/locale/lang/en'
import tw from 'element-plus-secondary/es/locale/lang/zh-tw'

const { wsCache } = useCache()

const elLocaleMap = {
  'zh-CN': zhCn,
  en: en,
  tw: tw
}
interface LocaleState {
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const useLocaleStore = defineStore('locales', {
  state: (): LocaleState => {
    return {
      currentLocale: {
        lang: wsCache.get('user.language') || 'zh-CN',
        elLocale: elLocaleMap[wsCache.get('user.language') || 'zh-CN']
      },
      // 多语言
      localeMap: [
        {
          lang: 'zh-CN',
          name: '简体中文'
        },
        {
          lang: 'en',
          name: 'English'
        },
        {
          lang: 'tw',
          name: '繁體中文'
        }
      ]
    }
  },
  getters: {
    getCurrentLocale(): LocaleDropdownType {
      return this.currentLocale
    },
    getLocaleMap(): LocaleDropdownType[] {
      return this.localeMap
    }
  },
  actions: {
    setCurrentLocale(localeMap: LocaleDropdownType) {
      // this.locale = Object.assign(this.locale, localeMap)
      this.currentLocale.lang = localeMap?.lang
      this.currentLocale.elLocale = elLocaleMap[localeMap?.lang]
      // wsCache.set('lang', localeMap?.lang)
    },
    setLang(language: string) {
      this.currentLocale.lang = language
      this.currentLocale.elLocale = elLocaleMap[language]
    }
  }
})

export const useLocaleStoreWithOut = () => {
  return useLocaleStore(store)
}
