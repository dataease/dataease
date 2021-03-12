import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import elementEnLocale from 'element-ui/lib/locale/lang/en' // element-ui lang
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'// element-ui lang
import elementTWLocale from 'element-ui/lib/locale/lang/zh-TW'// element-ui lang
import elementEsLocale from 'element-ui/lib/locale/lang/es'// element-ui lang
import elementJaLocale from 'element-ui/lib/locale/lang/ja'// element-ui lang
import enLocale from './en'
import zhLocale from './zh'
import twLocale from './tw'
import esLocale from './es'
import jaLocale from './ja'
import fuZh from 'fit2cloud-ui/src/locale/lang/zh-CN' // 加载fit2cloud的内容

import fuEn from 'fit2cloud-ui/src/locale/lang/en_US' // 加载fit2cloud的内容

Vue.use(VueI18n)

const messages = {
  en: {
    ...enLocale,
    ...elementEnLocale,
    ...fuEn
  },
  zh: {
    ...zhLocale,
    ...elementZhLocale,
    ...fuZh
  },
  tw: {
    ...twLocale,
    ...elementTWLocale
  },
  es: {
    ...esLocale,
    ...elementEsLocale
  },
  ja: {
    ...jaLocale,
    ...elementJaLocale
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
  return 'zh'
}
const i18n = new VueI18n({
  // set locale
  // options: en | zh | es
  locale: getLanguage(),
  // set locale messages
  messages
})

export default i18n
