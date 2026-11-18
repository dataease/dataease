import locale from '../../../../core/core-frontend/src/locales/zh-CN.ts'

const state = () => window.hmacErrorFixtureState ||= { messages: [], redirects: [], cleared: 0, dialogs: 0 }
export const ElMessage = Object.assign(options => {
  state().messages.push(options.message)
  ;(state().options ||= []).push({ duration: options.duration, grouping: options.grouping })
}, {
  error: message => state().messages.push(message)
})
export const ElMessageBox = { confirm() { state().dialogs++; return Promise.reject(new Error('fixture dismissed')) } }
export const useI18n = () => ({ t: key => window.hmacFixtureI18nReady === false
  ? key : key.split('.').reduce((value, part) => value?.[part], locale) || key })
export const usePermissionStoreWithOut = () => ({ getCurrentPath: '/fixture' })
export const useEmbedded = () => ({ baseUrl: '', token: '' })
export const useLinkStoreWithOut = () => ({ getLinkToken: '', setLinkToken() {} })
export const useRequestStoreWithOut = () => ({ resetLoadingMap() {} })
const cache = { get() {}, set() {}, delete() {} }
export const useCache = () => ({ wsCache: cache })
export const configHandler = config => config
export const isMobile = () => false
export const isLink = () => false
export const getLocale = () => 'zh-CN'
export const clearCache = () => state().cleared++
export const tryShowLoading = () => {}
export const tryHideLoading = () => {}
export default {
  currentRoute: { value: { fullPath: '/fixture' } },
  push: path => state().redirects.push(path)
}
