import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function seizeLogin(data) {
  return request({
    url: '/api/auth/seizeLogin',
    method: 'post',
    data,
    loading: true
  })
}

export function getInfo(token) {
  return request({
    url: '/api/auth/userInfo',
    method: 'post'
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    hideMsg: true
  })
}

export function deLogout() {
  return request({
    url: '/api/auth/deLogout',
    method: 'post',
    hideMsg: true
  })
}

export function needModifyPwd() {
  return request({
    url: '/api/auth/useInitPwd',
    method: 'post'
  })
}

export function removePwdTips() {
  return request({
    url: '/api/auth/removeNoti',
    method: 'post'
  })
}

export function defaultPwd() {
  return request({
    url: '/api/auth/defaultPwd',
    method: 'post'
  })
}

export function validateUserName(data) {
  return request({
    url: '/api/auth/validateName',
    method: 'post',
    data
  })
}

export function getUIinfo() {
  return request({
    url: '/system/ui/info',
    method: 'get'
  })
}

export function uiImage(imageId) {
  return request({
    url: '/display/file/' + imageId,
    method: 'get'
  })
}

export function languageApi(language) {
  return request({
    url: '/api/user/setLanguage/' + language,
    method: 'post'
  })
}

export function ldapStatus() {
  return request({
    url: '/api/auth/isOpenLdap',
    method: 'post'
  })
}

export function oidcStatus() {
  return request({
    url: '/api/auth/isOpenOidc',
    method: 'post'
  })
}

export function casStatus() {
  return request({
    url: '/api/auth/isOpenCas',
    method: 'post'
  })
}

export function casLoginPage() {
  return request({
    url: '/cas/loginPage',
    method: 'get'
  })
}

export function wecomStatus() {
  return request({
    url: '/api/auth/isOpenWecom',
    method: 'post'
  })
}

export function wecomQrParams() {
  return request({
    url: '/plugin/wecom/getQrParam',
    method: 'post'
  })
}

export function dingtalkStatus() {
  return request({
    url: '/api/auth/isOpenDingtalk',
    method: 'post'
  })
}

export function larkStatus() {
  return request({
    url: '/api/auth/isOpenLark',
    method: 'post'
  })
}

export function larkAppId() {
  return request({
    url: '/plugin/lark/appId',
    method: 'get'
  })
}

export function larksuiteStatus() {
  return request({
    url: '/api/auth/isOpenLarksuite',
    method: 'post'
  })
}

export function pluginLoaded() {
  return request({
    url: '/api/auth/isPluginLoaded',
    method: 'post'
  })
}

export function getPublicKey() {
  return request({
    url: '/api/auth/getPublicKey',
    method: 'get'
  })
}

export function defaultLoginType() {
  return request({
    url: '/system/defaultLoginType',
    method: 'post'
  })
}
