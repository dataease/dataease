import request from '@/utils/request'

export function basicInfo() {
  return request({
    url: '/system/basic/info',
    method: 'get',
    loading: true
  })
}

export function updateInfo(data) {
  return request({
    url: '/system/edit/basic',
    method: 'post',
    loading: true,
    data

  })
}
