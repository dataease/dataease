import request from '@/utils/request'

export function queryWithPanelId(panelId) {
  return request({
    url: '/outerParams/queryWithPanelId/' + panelId,
    method: 'get'
  })
}

export function updateOuterParamsSet(requestInfo) {
  return request({
    url: '/outerParams/updateOuterParamsSet',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}

export function getOuterParamsInfo(panelId) {
  return request({
    url: '/outerParams/getOuterParamsInfo/' + panelId,
    method: 'get',
    loading: true
  })
}
