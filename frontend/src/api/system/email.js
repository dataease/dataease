import request from '@/utils/request'

export function validate(data) {
  return request({
    url: '/system/testConnection',
    method: 'post',
    timeout: 30000,
    loading: true,
    data

  })
}

export function emailInfo() {
  return request({
    url: '/system/mail/info',
    method: 'get',
    loading: true
  })
}

export function updateInfo(data) {
  return request({
    url: '/system/edit/email',
    method: 'post',
    loading: true,
    data

  })
}
