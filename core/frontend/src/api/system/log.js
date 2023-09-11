import request from '@/utils/request'

export function logGrid(page, size, data) {
  return request({
    url: '/api/log/logGrid/' + page + '/' + size,
    method: 'post',
    data,
    loading: true
  })
}

export function opTypes() {
  return request({
    url: '/api/log/opTypes',
    method: 'post',
    loading: true
  })
}

export function exportExcel(data) {
  return request({
    url: '/api/log/export',
    method: 'post',
    loading: true,
    responseType: 'blob',
    data
  })
}
