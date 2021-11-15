import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
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
