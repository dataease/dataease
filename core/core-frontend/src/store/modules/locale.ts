import { defineStore } from 'pinia'
import { store } from '../index'
import type { LocaleDropdownType } from 'types/localeDropdown'
import zhCn from 'element-plus-secondary/es/locale/lang/zh-cn'
import en from 'element-plus-secondary/es/locale/lang/en'
import tw from 'element-plus-secondary/es/locale/lang/zh-tw'
import { getLocale } from '@/utils/utils'
import request from '@/config/axios'
const elLocaleMap = {
  'zh-CN': zhCn,
  en: en,
  tw: tw
}
interface LocaleState {
  customLoaded: boolean
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const useLocaleStore = defineStore('locales', {
  state: (): LocaleState => {
    return {
      customLoaded: false,
      currentLocale: {
        lang: getLocale(),
        elLocale: elLocaleMap[getLocale()]
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
    async getLocaleMap(): Promise<LocaleDropdownType[]> {
      if (this.customLoaded) {
        return this.localeMap
      }
      try {
        const res = await request.get({ url: '/sysParameter/i18nOptions' })
        this.customLoaded = true
        const customMap = res.data
        for (const key in customMap) {
          const item = {
            lang: key,
            name: customMap[key],
            custom: true
          }
          this.localeMap.push(item)
        }
        return this.localeMap
      } catch (error) {
        this.customLoaded = true
        return this.localeMap
      }
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
