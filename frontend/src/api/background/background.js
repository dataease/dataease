import request from '@/utils/request'

export function queryBackground() {
  return request({
    url: 'background/findAll',
    method: 'get'
  })
}
