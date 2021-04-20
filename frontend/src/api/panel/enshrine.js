import request from '@/utils/request'

export function saveEnshrine(panelGroupId) {
  return request({
    url: '/api/store/' + panelGroupId,
    method: 'post',
    loading: true
  })
}

export function deleteEnshrine(id) {
  return request({
    url: '/api/store/remove/' + id,
    method: 'post',
    loading: true
  })
}

export function enshrineList(data) {
  return request({
    url: '/api/store/list',
    method: 'post',
    loading: true,
    data
  })
}

