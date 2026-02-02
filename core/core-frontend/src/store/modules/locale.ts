import { defineStore } from 'pinia'
import { store } from '../index'
import type { LocaleDropdownType } from 'types/localeDropdown'
import zhCnOriginal from 'element-plus-secondary/es/locale/lang/zh-cn'
import enOriginal from 'element-plus-secondary/es/locale/lang/en'
import twOriginal from 'element-plus-secondary/es/locale/lang/zh-tw'
import { getLocale } from '@/utils/utils'
import request from '@/config/axios'
import { setElementPlusLocale } from '@/plugins/element-plus'

// 合并DataEase的国际化配置到Element Plus的国际化结构中
const mergeLocaleData = (baseLocale: any, customData: any) => {
  const merged = JSON.parse(JSON.stringify(baseLocale || {}))

  const mergeRecursive = (obj: any, source: any) => {
    for (const key in source) {
      if (source.hasOwnProperty(key)) {
        if (
          typeof source[key] === 'object' &&
          source[key] !== null &&
          !Array.isArray(source[key])
        ) {
          if (!obj[key]) obj[key] = {}
          mergeRecursive(obj[key], source[key])
        } else {
          obj[key] = source[key]
        }
      }
    }
    return obj
  }

  return mergeRecursive(merged, customData)
}

// 加载DataEase的国际化配置
const loadCustomLocaleData = async (lang: string) => {
  try {
    const localeModule = await import(`../../locales/${lang}.ts`)
    const localeData = localeModule.default || localeModule
    return localeData.element_plus || {}
  } catch (error) {
    console.warn(`Failed to load custom locale data for ${lang}:`, error)
    return {}
  }
}

const elLocaleMap = {
  'zh-CN': zhCnOriginal,
  en: enOriginal,
  tw: twOriginal
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
        let match = false
        for (const key in customMap) {
          const item = {
            lang: key,
            name: customMap[key],
            custom: true
          }
          this.localeMap.push(item)
          if (this.currentLocale?.lang === key) {
            match = true
          }
        }
        if (this.currentLocale?.lang && !match) {
          const matchItem = this.localeMap.find(item =>
            item.lang.startsWith(this.currentLocale.lang)
          )
          if (matchItem) {
            this.currentLocale['lang'] = matchItem.lang
          }
        }
        return this.localeMap
      } catch (error) {
        this.customLoaded = true
        return this.localeMap
      }
    }
  },
  actions: {
    async setCurrentLocale(localeMap: LocaleDropdownType) {
      // this.locale = Object.assign(this.locale, localeMap)
      this.currentLocale.lang = localeMap?.lang
      const baseLocale = elLocaleMap[localeMap?.lang]
      const customData = await loadCustomLocaleData(localeMap?.lang)

      // 合并基础国际化配置和自定义配置
      this.currentLocale.elLocale = mergeLocaleData(baseLocale, customData)

      // 同时更新Element Plus的国际化配置
      if (this.currentLocale.elLocale) {
        setElementPlusLocale(this.currentLocale.elLocale)
      }
      // wsCache.set('lang', localeMap?.lang)
    },
    async setLang(language: string) {
      this.currentLocale.lang = language
      const baseLocale = elLocaleMap[language]
      const customData = await loadCustomLocaleData(language)

      // 合并基础国际化配置和自定义配置
      this.currentLocale.elLocale = mergeLocaleData(baseLocale, customData)

      // 同时更新Element Plus的国际化配置
      if (this.currentLocale.elLocale) {
        setElementPlusLocale(this.currentLocale.elLocale)
      }
    }
  }
})

export const useLocaleStoreWithOut = () => {
  return useLocaleStore(store)
}
