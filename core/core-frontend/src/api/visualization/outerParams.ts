import request from '@/config/axios'

export function queryWithVisualizationId(dvId) {
  return request.get({
    url: '/outerParams/queryWithVisualizationId/' + encodeURIComponent(dvId),
    headers: {
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
}

export function updateOuterParamsSet(requestInfo) {
  return request.post({
    url: '/outerParams/updateOuterParamsSet',
    data: requestInfo,
    loading: true
  })
}

export function getOuterParamsInfo(dvId) {
  return request.get({
    url: '/outerParams/getOuterParamsInfo/' + encodeURIComponent(dvId),
    method: 'get',
    loading: false,
    headers: {
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
}
