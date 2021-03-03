import request from '@/utils/request'

export function dsGrid(pageIndex, pageSize, data) {
  return request({
    url: 'datasource/list/' + pageIndex + '/' + pageSize,
    method: 'post',
    data
  })
}

export function addDs(data) {
  return request({
    url: 'datasource/add/',
    method: 'post',
    data
  })
}

export function editDs(data) {
  return request({
    url: 'datasource/update/',
    method: 'post',
    data
  })
}

export function delDs(id) {
  return request({
    url: 'datasource/delete/' + id,
    method: 'post'
  })
}

export function validateDs(data) {
  return request({
    url: 'datasource/validate/',
    method: 'post',
    data
  })
}

export default { dsGrid, addDs, editDs, delDs, validateDs }
