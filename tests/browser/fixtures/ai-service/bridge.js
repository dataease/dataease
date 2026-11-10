import { reactive } from 'vue'
import language from '@/locales/zh-CN'
export const useI18n = () => ({
  t: (key, args = {}) => {
    const text = key.split('.').reduce((value, part) => value?.[part], language) ?? key
    return text.replace(/\{(\w+)\}/g, (_, name) => String(args[name] ?? `{${name}}`))
  }
})
const user = reactive({ uid: '1', oid: null, proxyInfo: { proxyOid: null } })
window.aiSwitchUser = () => {
  user.uid = '2'
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
  if (!response.ok) throw new Error('Synthetic API error')
  return { data: await response.json() }
}
export default Object.fromEntries(
  ['get', 'post', 'put', 'delete'].map((method) => [
    method,
    (option) => send(method.toUpperCase(), option)
  ])
)
