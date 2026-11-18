import { createI18n } from 'vue-i18n'
import zh from '../../src/locales/zh-CN'
export const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  messages: { 'zh-CN': zh },
  missingWarn: false,
  fallbackWarn: false
})
export const useI18n = () => ({ t: i18n.global.t })
export default {
  get: async ({ url }) => (await fetch(url)).json(),
  post: async ({ url, data = undefined }: { url: string; data?: unknown }) => {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    if (!response.ok) throw new Error('Request failed')
    return response.json()
  }
}
