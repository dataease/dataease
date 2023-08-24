import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import elementEnLocale from 'element-ui/lib/locale/lang/en' // element-ui lang
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'// element-ui lang
import elementTWLocale from 'element-ui/lib/locale/lang/zh-TW'// element-ui lang

import localMessages from './messages'


Vue.use(VueI18n)

const messages = {
  en_US: {
    ...localMessages['en_US'],
    ...elementEnLocale
  },
  zh_CN: {
    ...localMessages['zh_CN'],
    ...elementZhLocale
  },
  zh_TW: {
    ...localMessages['zh_TW'],
    ...elementTWLocale
  }
}
export function getLanguage () {
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
