import request from '@/utils/request'

export function save(data) {
  return request({
    url: '/appTemplate/save',
    data: data,
    method: 'post',
    loading: true
  })
}

export function update(data) {
  return request({
    url: '/appTemplate/update',
    data: data,
    method: 'post',
    loading: true
  })
}

export function templateDelete(id) {
  return request({
    url: '/appTemplate/delete/' + id,
    method: 'delete'
  })
}

export function showappTemplateList(data) {
  return request({
    url: '/appTemplate/appTemplateList',
    data: data,
    method: 'post'
  })
}

export function findOne(id) {
  return request({
    url: '/appTemplate/findOne/' + id,
    method: 'get'
  })
}

export function find(data) {
  return request({
    url: '/appTemplate/find',
    data: data,
    loading: true,
    method: 'post'
  })
}

export function nameCheck(data) {
  return request({
    url: '/appTemplate/nameCheck',
    data: data,
    method: 'post'
  })
}
