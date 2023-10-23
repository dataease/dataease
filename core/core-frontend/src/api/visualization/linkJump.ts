import request from '@/config/axios'

export function getTableFieldWithViewId(viewId) {
  return request.get({
    url: '/linkJump/getTableFieldWithViewId/' + viewId
  })
}
export function queryWithViewId(dvId, viewId) {
  return request.get({
    url: '/linkJump/queryWithViewId/' + dvId + '/' + viewId
  })
}

export function updateJumpSet(requestInfo) {
  return request.post({
    url: '/linkJump/updateJumpSet',
    data: requestInfo,
    loading: true
  })
}
export function queryTargetVisualizationJumpInfo(requestInfo) {
  return request.post({
    url: '/linkJump/queryTargetVisualizationJumpInfo',
    data: requestInfo,
    loading: true
  })
}

export function queryVisualizationJumpInfo(dvId) {
  return request.get({
    url: '/linkJump/queryVisualizationJumpInfo/' + dvId,
    loading: false
  })
}

export function viewTableDetailList(dvId) {
  return request.get({
    url: '/linkJump/viewTableDetailList/' + dvId,
    loading: false
  })
}

export function updateJumpSetActive(requestInfo) {
  return request.post({
    url: '/linkJump/updateJumpSetActive',
    data: requestInfo,
    loading: true
  })
}
