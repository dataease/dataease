import request from '@/utils/request'

export function post(url, data) {
  return request({
    url: url,
    data: data,
    loading: true,
    method: 'post'
  })
}
export function authChange(data) {
  return request({
    url: '/api/sys_auth/authChange',
    data: data,
    method: 'post'
  })
}
export function authDetails(data) {
  return request({
    url: '/api/sys_auth/authDetails',
    data: data,
    method: 'post'
  })
}

export function authDetailsModel(authType) {
  return request({
    url: '/api/sys_auth/authDetailsModel/' + authType,
    method: 'get'
  })
}

export function authModel(data) {
  return request({
    url: '/plugin/auth/authModels',
    data: data,
    method: 'post'
  })
}
