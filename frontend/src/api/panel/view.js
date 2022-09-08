import request from '@/utils/request'

export function tree(data) {
  return request({
    url: '/api/panelView/tree',
    method: 'post',
    data
  })
}

export function viewsWithIds(data) {
  return request({
    url: '/api/panelView/viewsWithIds',
    method: 'post',
    loading: true,
    data
  })
}

export function paramsWithIds(type, data) {
  return request({
    url: '/dataset/table/paramsWithIds/' + type,
    method: 'post',
    loading: true,
    data
  })
}

export function findOne(id) {
  return request({
    url: '/api/panelView/findOne/' + id,
    method: 'get',
    loading: true
  })
}

