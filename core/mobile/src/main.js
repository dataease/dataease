import Vue from 'vue'
import App from './App'
import './common/style/index.css'

import en from './locale/en.json'
import zh from './locale/zh-Hans.json'
import tw from './locale/zh-Hant.json'
import VueI18n from 'vue-i18n'
const messages = {
    en,
    'zh-Hans': zh,
    'zh-Hant': tw
}
import {parseLanguage} from '@/common/utils'
let i18nConfig = {
  locale: parseLanguage() || uni.getLocale(),// 获取已设置的语言
  messages
}

Vue.use(VueI18n)
const i18n = new VueI18n(i18nConfig)

Vue.config.productionTip = false

App.mpType = 'app'

const app = new Vue({
    i18n,
  ...App
})
app.$mount()
