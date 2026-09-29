import request from '@/config/axios'

export const loginApi = data => request.post({ url: '/login/localLogin', data })

export const queryDekey = () => request.get({ url: 'dekey' })

export const modelApi = () => request.get({ url: 'model' })

export const platformLoginApi = origin => request.post({ url: '/login/platformLogin/' + origin })

export const logoutApi = () => request.get({ url: '/logout' })

export const refreshApi = (time?: any) => request.get({ url: '/login/refresh', params: { time } })

export const uiLoadApi = () => request.get({ url: '/sysParameter/ui' })

export const loginCategoryApi = () => request.get({ url: '/sysParameter/defaultLogin' })

export interface MfaData {
  enabled: boolean
  ready: boolean
  uid: string
  origin?: number
  challenge?: string
}

export const mfaQrApi = (mfa: MfaData) =>
  request.post({
    url: `/mfa/qr/${mfa.uid}`,
    headers: { 'X-DE-MFA-Challenge': mfa.challenge }
  })

export const mfaLoginApi = (mfa: MfaData, code: string) =>
  request.post({ url: '/mfa/login', data: { id: mfa.uid, code, challenge: mfa.challenge } })

export const mfaBindQrApi = () => request.get({ url: '/user/mfaQr' })

export const mfaBindApi = (code: string) => request.post({ url: '/user/mfaBind', data: { code } })
