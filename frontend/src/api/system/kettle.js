import request from '@/utils/request'

export function validate(data) {
  return request({
    url: '/kettle/validate',
    method: 'post',
    loading: true,
    data
  })
}

export function validateById(id) {
  return request({
    url: '/kettle/validate/' + id,
    method: 'post',
    loading: true
  })
}

export function save(data) {
  return request({
    url: '/kettle/save',
    method: 'post',
    loading: true,
    data
  })
}

export function deleteKettle(id) {
  return request({
    url: '/kettle/delete/' + id,
    method: 'delete',
    loading: true
  })
}

export function pageList(url, data) {
  return request({
    url: url,
    method: 'post',
    loading: true,
    data
  })
}
