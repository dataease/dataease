import request from '@/utils/request'

export function tree(data) {
  return request({
    url: '/api/panelView/tree',
    method: 'post',
    timeout: 30000,
    data
  })
}

export function viewsWithIds(data) {
  return request({
    url: '/api/panelView/viewsWithIds',
    method: 'post',
    timeout: 30000,
    loading: true,
    data
  })
}

export function findOne(id) {
  return request({
    url: '/api/panelView/findOne/' + id,
    method: 'get',
    timeout: 30000,
    loading: true
  })
}

