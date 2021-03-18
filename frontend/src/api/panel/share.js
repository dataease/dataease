import request from '@/utils/request'

export function saveShare(data) {
  return request({
    url: '/api/share/',
    method: 'post',
    loading: true,
    data
  })
}

export function loadShares(data) {
  return request({
    url: '/api/share/queryWithResourceId',
    method: 'post',
    loading: true,
    data
  })
}

export function loadTree(data) {
  return request({
    url: '/api/share/treeList',
    method: 'post',
    loading: true,
    data
  })
}

