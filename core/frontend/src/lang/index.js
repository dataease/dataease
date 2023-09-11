import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import elementEnLocale from 'element-ui/lib/locale/lang/en' // element-ui lang
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'// element-ui lang
import elementTWLocale from 'element-ui/lib/locale/lang/zh-TW'// element-ui lang

import enLocale from './en'
import zhLocale from './zh'
import twLocale from './tw'

import fuZh from 'fit2cloud-ui/src/locale/lang/zh-CN' // 加载fit2cloud的内容

import fuEn from 'fit2cloud-ui/src/locale/lang/en_US' // 加载fit2cloud的内容

import fuTW from 'fit2cloud-ui/src/locale/lang/zh-TW' // 加载fit2cloud的内容

Vue.use(VueI18n)

const messages = {
  en_US: {
    ...enLocale,
    ...elementEnLocale,
    ...fuEn
  },
  zh_CN: {
    ...zhLocale,
    ...elementZhLocale,
    ...fuZh
  },
  zh_TW: {
    ...twLocale,
    ...elementTWLocale,
    ...fuTW
  }
}
export function getLanguage() {
  const chooseLanguage = Cookies.get('language')
  if (chooseLanguage) return chooseLanguage

  // if has not choose language
  const language = (navigator.language || navigator.browserLanguage).toLowerCase()
  const locales = Object.keys(messages)
  for (const locale of locales) {
    if (language.indexOf(locale) > -1) {
      return locale
    }
  }
  return 'zh_CN'
}
const i18n = new VueI18n({
  // set locale
  // options: en | zh | es
  locale: getLanguage(),
  // set locale messages
  messages
})

export default i18n
