import type { QueryRequest } from '@/config/axios/queryScope'
import request from '@/config/axios'
import { postCanvasRelation } from './canvasRevision'

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
  return postCanvasRelation('/linkJump/updateJumpSet', requestInfo, requestInfo?.sourceDvId, true)
}
export function queryTargetVisualizationJumpInfo(requestInfo, signal?: AbortSignal) {
  return request.post({
    url: '/linkJump/queryTargetVisualizationJumpInfo',
    data: requestInfo,
    signal,
    loading: true
  })
}

export function queryVisualizationJumpInfo(
  dvId,
  resourceTable = 'snapshot',
  queryRequest?: QueryRequest
) {
  return request.get({
    url: '/linkJump/queryVisualizationJumpInfo/' + dvId + '/' + resourceTable,
    queryRequest,
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
  return postCanvasRelation(
    '/linkJump/updateJumpSetActive',
    requestInfo,
    requestInfo?.sourceDvId,
    true
  )
}

export function removeJumpSet(requestInfo) {
  return postCanvasRelation('/linkJump/removeJumpSet', requestInfo, requestInfo?.sourceDvId, true)
}
