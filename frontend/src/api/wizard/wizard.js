import request from '@/utils/request'

export function blogLastActive() {
  return request({
    url: 'Reptile/lastActive',
    method: 'get'
  })
}
