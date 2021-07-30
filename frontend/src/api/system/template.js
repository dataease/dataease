import request from '@/utils/request'

export function save(data) {
  return request({
    url: '/template/save',
    data: data,
    timeout: 60000,
    method: 'post'
  })
}
export function templateDelete(id) {
  return request({
    url: '/template/delete/' + id,
    timeout: 60000,
    method: 'delete'
  })
}

export function showTemplateList(data) {
  return request({
    url: '/template/templateList',
    timeout: 60000,
    data: data,
    method: 'post'
  })
}

export function findOne(id) {
  return request({
    url: '/template/findOne/' + id,
    timeout: 60000,
    method: 'get'
  })
}

export function find(data) {
  return request({
    url: '/template/find',
    timeout: 60000,
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
