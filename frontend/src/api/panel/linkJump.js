import request from '@/utils/request'

export function getTableFieldWithViewId(viewId) {
  return request({
    url: '/linkJump/getTableFieldWithViewId/' + viewId,
    method: 'get'
  })
}
export function queryWithViewId(panelId, viewId) {
  return request({
    url: '/linkJump/queryWithViewId/' + panelId + '/' + viewId,
    method: 'get'
  })
}

export function updateJumpSet(requestInfo) {
  return request({
    url: '/linkJump/updateJumpSet',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}
export function queryTargetPanelJumpInfo(requestInfo) {
  return request({
    url: '/linkJump/queryTargetPanelJumpInfo',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}

export function queryPanelJumpInfo(panelId) {
  return request({
    url: '/linkJump/queryPanelJumpInfo/' + panelId,
    method: 'get',
    loading: false
  })
}
