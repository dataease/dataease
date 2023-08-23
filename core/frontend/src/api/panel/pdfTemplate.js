import request from '@/utils/request'

export function queryAll() {
  return request({
    url: '/pdf-template/queryAll',
    method: 'get',
    loading: false
  })
}
