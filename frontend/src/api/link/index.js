import request from '@/utils/request'

export function validate(param) {
  return request({
    url: 'api/link/validate',
    method: 'post',
    loading: true,
    param
  })
}

export function generate(param) {
  return request({
    url: 'api/link/generate',
    method: 'post',
    param
  })
}
