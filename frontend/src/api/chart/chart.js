import request from '@/utils/request'

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    loading: true,
    data
  })
}


export function getChartTree(data) {
  return request({
    url: 'api',
    method: 'post',
    loading: true,
    data
  })
}
