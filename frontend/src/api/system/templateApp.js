import request from '@/utils/request'

export function save(data) {
  return request({
    url: '/templateApp/save',
    data: data,
    method: 'post',
    loading: true
  })
}
export function templateDelete(id) {
  return request({
    url: '/templateApp/delete/' + id,
    method: 'delete'
  })
}

export function showtemplateAppList(data) {
  return request({
    url: '/templateApp/templateAppList',
    data: data,
    method: 'post'
  })
}

export function findOne(id) {
  return request({
    url: '/templateApp/findOne/' + id,
    method: 'get'
  })
}

export function find(data) {
  return request({
    url: '/templateApp/find',
    data: data,
    loading: true,
    method: 'post'
  })
}

export function nameCheck(data) {
  return request({
    url: '/templateApp/nameCheck',
    data: data,
    method: 'post'
  })
}
