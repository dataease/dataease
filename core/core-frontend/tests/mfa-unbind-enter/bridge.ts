const words = {
  'common.personal_info': '个人信息',
  'commons.unbind': '取消绑定',
  'dataset.cancel': '取消',
  'setting_mfa.mfa_code': '验证码',
  'setting_mfa.code_input_msg': '请输入六位验证码',
  'setting_mfa.unbind_confirm': '确认取消绑定 MFA？'
}
export const useI18n = () => ({ t: key => words[key] || key })
export const useUserStoreWithOut = () => ({ getUid: '100', setUser() {} })
export const roleOptionForUserApi = async () => ({ data: [] })
export const personInfoApi = async () => ({
  data: {
    id: '100',
    account: 'synthetic-user',
    name: 'Synthetic user',
    email: '',
    phone: '',
    phonePrefix: '+86',
    roleIds: [],
    enable: true,
    mfaEnable: true
  }
})
export const userCreateApi = async () => ({})
export const personEditApi = async () => ({})
export default {
  async post({ url }) {
    const response = await fetch(url, { method: 'POST' })
    return response.json()
  }
}
