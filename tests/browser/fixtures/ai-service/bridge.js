import { reactive } from 'vue'
import zh from '@/locales/zh-CN'
import { requestLanguageTag } from '@/utils/locale'
import tw from '@/locales/tw'
import en from '@/locales/en'
const uiLocale = new URLSearchParams(location.search).get('language') || 'zh-CN'
const language = { 'zh-CN': zh, tw, en }[uiLocale] ?? zh
export const useI18n = (namespace) => ({
  t: (key, args = {}) => {
    const path = namespace ? namespace + '.' + key : key
    const text = path.split('.').reduce((value, part) => value?.[part], language) ?? key
    return text.replace(/\{(\w+)\}/g, (_, name) => String(args[name] ?? `{${name}}`))
  }
})
const user = reactive({ uid: '1', oid: null, proxyInfo: { proxyOid: null } })
window.aiSwitchUser = (context = 'user') => {
  if (context === 'workspace') user.oid = 'workspace-2'
  else if (context === 'proxy') user.proxyInfo.proxyOid = 'proxy-2'
  else user.uid = '2'
}
export const useUserStoreWithOut = () => user
const send = async (method, option) => {
  const query = new URLSearchParams(
    Object.entries(option.params ?? {}).filter(([, value]) => value != null)
  )
  const response = await fetch('/fixture-api' + option.url + (query.size ? '?' + query : ''), {
    method,
    body: option.data === undefined ? undefined : JSON.stringify(option.data),
    headers: { 'Content-Type': 'application/json', 'Accept-Language': requestLanguageTag(uiLocale) },
    signal: option.signal
  }).catch(error => {
    if (error.name === 'AbortError') throw error
    throw Object.assign(error, { transportCode: 'ERR_NETWORK' })
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw Object.assign(new Error(body.msg || 'Synthetic API error'), {
      messageKey: body.messageKey,
      category: body.category,
      requestId: body.requestId,
      httpStatus: response.status,
      response: { status: response.status, data: body }
    })
  }
  const body = await response.text()
  return { data: body ? JSON.parse(body) : undefined }
}
export default Object.fromEntries(
  ['get', 'post', 'put', 'delete'].map((method) => [
    method,
    (option) => send(method.toUpperCase(), option)
  ])
)

export const isRequestError = (error) => Boolean(error?.response || error?.transportCode)

// Metadata tests only list tables. Writes to the datasource configuration are outside the fixture.
export const nameTrim = () => { throw new Error('Unexpected datasource configuration write') }
