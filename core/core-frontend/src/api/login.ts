import request from '@/config/axios'
import { beginPlatformLogin, clearPlatformLogin, platformLoginHeaders } from '@/utils/platformLogin'

export const loginApi = data => request.post({ url: '/login/localLogin', data })

export const queryDekey = () => request.get({ url: 'dekey' })

export const modelApi = () => request.get({ url: 'model' })

export const platformLoginApi = (origin: number, token = '') => {
  const headers = platformLoginHeaders(token, origin)
  return request.post({ url: '/login/platformLogin/' + origin, headers }).finally(() => {
    clearPlatformLogin(headers)
  })
}

const platformPaths: Record<number, string> = {
  4: 'lark',
  5: 'dingtalk',
  6: 'wecom',
  7: 'larksuite',
  9: 'oauth2'
}

export const platformTokenApi = (origin: number, data: { code: string; state: string }) => {
  const headers = beginPlatformLogin(origin)
  return request.post({ url: `/${platformPaths[origin]}/token`, data, headers }).catch(error => {
    clearPlatformLogin(headers)
    throw error
  })
}

export const samlLoginApi = () => {
  const headers = beginPlatformLogin(10)
  return request.post({ url: '/saml/login', headers }).catch(error => {
    clearPlatformLogin(headers)
    throw error
  })
}

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
