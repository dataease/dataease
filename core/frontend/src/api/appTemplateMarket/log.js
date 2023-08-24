import request from '@/utils/request'

export function logGrid(page, size, data) {
  return request({
    url: '/app/log/logGrid/' + page + '/' + size,
    method: 'post',
    data,
    loading: true
  })
}

export function opTypes() {
  return request({
    url: '/app/log/opTypes',
    method: 'post',
    loading: true
  })
}

export function exportExcel(data) {
  return request({
    url: '/app/log/export',
    method: 'post',
    loading: true,
    responseType: 'blob',
    data
  })
}

export function deleteLogAndResource(data) {
  return request({
    url: '/app/log/deleteLog',
    method: 'post',
    data,
    loading: true
  })
}
