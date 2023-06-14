import request from '@/config/axios'

export function queryAll() {
  return request.get({
    url: '/pdf-template/queryAll',
    loading: false
  })
}
