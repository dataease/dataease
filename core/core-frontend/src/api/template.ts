import request from '@/config/axios'

export function save(data) {
  return request.post({
    url: '/template/save',
    data: data,
    loading: true
  })
}
export function templateDelete(id) {
  return request.post({
    url: '/template/delete/' + id
  })
}

export function showTemplateList(data) {
  return request.post({
    url: '/template/templateList',
    data: data
  })
}

export function findOne(id) {
  return request.get({
    url: '/template/findOne/' + id
  })
}

export function find(data) {
  return request.post({
    url: '/template/find',
    data: data,
    loading: true
  })
}

export function nameCheck(data) {
  return request.post({
    url: '/template/nameCheck',
    data: data
  })
}
