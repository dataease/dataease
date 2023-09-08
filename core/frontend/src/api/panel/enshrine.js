import request from '@/utils/request'

export function saveEnshrine(panelGroupId, loading = true) {
  return request({
    url: '/api/store/' + panelGroupId,
    method: 'post',
    loading: loading
  })
}

export function deleteEnshrine(id, loading = true) {
  return request({
    url: '/api/store/remove/' + id,
    method: 'post',
    loading: loading
  })
}

export function enshrineList(data, loading = true) {
  return request({
    url: '/api/store/list',
    method: 'post',
    loading: loading,
    data
  })
}

export function starStatus(panelId) {
  return request({
    url: '/api/store/status/' + panelId,
    method: 'post',
    loading: false
  })
}
