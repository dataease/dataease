import request from '@/utils/request'

export function userLoginInfo() {
  return request({
    url: '/systemInfo/userLoginInfo',
    method: 'get',
    loading: false
  })
}

export function proxyUserLoginInfo() {
  return request({
    url: '/systemInfo/proxyUserLoginInfo',
    method: 'get',
    loading: false
  })
}

export default {
  userLoginInfo,
  proxyUserLoginInfo
}
