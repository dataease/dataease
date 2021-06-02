import request from '@/utils/request'

export function save(data) {
  return request({
    url: '/template/save',
    data: data,
    method: 'post'
  })
}
export function templateDelete(id) {
  return request({
    url: '/template/delete/' + id,
    method: 'delete'
  })
}

export function showTemplateList(data) {
  return request({
    url: '/template/templateList',
    data: data,
    loading: true,
    method: 'post'
  })
}

export function findOne(id) {
  return request({
    url: '/template/findOne/' + id,
    loading: true,
    method: 'get'
  })
}

export function find(data) {
  return request({
    url: '/template/find',
    data: data,
    method: 'post'
  })
}

export function nameCheck(data) {
  return request({
    url: '/template/nameCheck',
    data: data,
    method: 'post'
  })
}
