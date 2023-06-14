import request from '@/config/axios'

export function getTableFieldWithViewId(viewId) {
  return request.get({
    url: '/linkJump/getTableFieldWithViewId/' + viewId
  })
}
export function queryWithViewId(panelId, viewId) {
  return request.get({
    url: '/linkJump/queryWithViewId/' + panelId + '/' + viewId
  })
}

export function updateJumpSet(requestInfo) {
  return request.post({
    url: '/linkJump/updateJumpSet',
    data: requestInfo,
    loading: true
  })
}
export function queryTargetPanelJumpInfo(requestInfo) {
  return request.post({
    url: '/linkJump/queryTargetPanelJumpInfo',
    data: requestInfo,
    loading: true
  })
}

export function queryPanelJumpInfo(panelId) {
  return request.get({
    url: '/linkJump/queryPanelJumpInfo/' + panelId,
    loading: false
  })
}
