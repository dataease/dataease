import { createI18n } from 'vue-i18n'
import zhCN from '@/locales/zh-CN'
export const i18n = createI18n({ legacy: false, locale: 'zh-CN', messages: { 'zh-CN': zhCN } })
