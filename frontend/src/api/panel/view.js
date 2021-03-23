import request from '@/utils/request'

export function tree(data) {
  return request({
    url: '/api/panelView/tree',
    method: 'post',
    loading: true,
    data
  })
}
