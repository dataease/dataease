import request from '@/utils/request'

export function userLoginInfo() {
  return request({
    url: '/systemInfo/userLoginInfo',
    method: 'get',
    loading: false
  })
}

export function proxyUserLoginInfo(userId) {
  return request({
    url: '/systemInfo/proxyUserLoginInfo/' + userId,
    method: 'get',
    loading: false
  })
}

export default {
  userLoginInfo,
  proxyUserLoginInfo
}
