import request from '@/common/js/request'

export function login(data) {
    return request({
        url: '/api/auth/mobileLogin',
        method: 'post',
        data
    })
}

export function getInfo() {
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

export function getPublicKey() {
    return request({
      url: '/api/auth/getPublicKey',
      method: 'get'
    })
}

export function buildVersion() {
    return request({
      url: '/about/build/version',
      method: 'get'
    })
}

export function validate(data) {
    return request({
        url: '/about/license/validate',
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
