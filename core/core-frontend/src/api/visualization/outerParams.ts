import request from '@/config/axios'

export function queryWithPanelId(panelId) {
  return request.get({
    url: '/outerParams/queryWithPanelId/' + panelId
  })
}

export function updateOuterParamsSet(requestInfo) {
  return request.post({
    url: '/outerParams/updateOuterParamsSet',
    data: requestInfo,
    loading: true
  })
}

export function getOuterParamsInfo(panelId) {
  return request.get({
    url: '/outerParams/getOuterParamsInfo/' + panelId,
    loading: true
  })
}
