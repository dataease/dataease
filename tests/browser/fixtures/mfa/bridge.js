import { ref } from 'vue'
export const events = { tokens: [], routes: [], success: 0, bindings: 0 }
window.mfaEvents = events
const words = { 'common.sure': '确认', 'common.next': '下一步', 'common.prev': '上一步', 'setting_mfa.code_input_msg': '请输入六位验证码' }
export const useI18n = () => ({ t: key => words[key] || key })
export const useUserStoreWithOut = () => ({ setToken: token => events.tokens.push(token), setExp() {}, setTime() {} })
export const isMobile = () => new URLSearchParams(location.search).has('mobile')
export const logoutHandler = () => {}
export default { currentRoute: ref({ query: {} }), push: route => { events.routes.push(route); return Promise.resolve() } }
async function post(url, data) {
  const res = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) })
  const result = await res.json()
  if (result.code !== 0) throw result.msg
  return result
}
export const mfaLoginApi = (mfa, code) => post('/mfa/login', { id: mfa.uid, challenge: mfa.challenge, code })
export const mfaBindApi = code => post('/user/mfaBind', { code })
export const mfaQrApi = () => Promise.resolve({ data: { img: '' } })
export const mfaBindQrApi = mfaQrApi
