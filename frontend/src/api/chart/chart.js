import request from '@/utils/request'

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    data
  })
}
