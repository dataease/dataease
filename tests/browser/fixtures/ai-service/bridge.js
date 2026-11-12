import { reactive } from 'vue'
import zh from '@/locales/zh-CN'
import tw from '@/locales/tw'
import en from '@/locales/en'
const language = { 'zh-CN': zh, tw, en }[new URLSearchParams(location.search).get('language')] ?? zh
export const useI18n = () => ({
  t: (key, args = {}) => {
    const text = key.split('.').reduce((value, part) => value?.[part], language) ?? key
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
    headers: { 'Content-Type': 'application/json' },
    signal: option.signal
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw Object.assign(new Error(body.msg || 'Synthetic API error'), {
      messageKey: body.messageKey,
      httpStatus: response.status
    })
  }
  return { data: await response.json() }
}
export default Object.fromEntries(
  ['get', 'post', 'put', 'delete'].map((method) => [
    method,
    (option) => send(method.toUpperCase(), option)
  ])
)
