import request from '@/utils/request'

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    loading: true,
    data
  })
}

export function ajaxGetData(id, data) {
  return request({
    url: '/chart/view/getOneWithPermission/' + id,
    method: 'post',
    loading: true,
    hideMsg: true,
    timeout: 30000,
    data
  })
}

export function getChartTree(data) {
  return request({
    url: 'api',
    method: 'post',
    loading: true,
    data
  })
}

export function chartCopy(id) {
  return request({
    url: '/chart/view/chartCopy/' + id,
    method: 'post',
    loading: true
  })
}
export function chartGroupTree(data) {
  return request({
    url: '/chart/group/tree',
    method: 'post',
    loading: false,
    data
  })
}

export function searchAdviceSceneId(panelId) {
  return request({
    url: '/chart/view/searchAdviceSceneId/' + panelId,
    method: 'get',
    loading: true
  })
}

export function checkSameDataSet(viewIdSource, viewIdTarget) {
  return request({
    url: '/chart/view/checkSameDataSet/' + viewIdSource + '/' + viewIdTarget,
    method: 'get',
    loading: false
  })
}

export function ajaxGetDataOnly(id, data) {
  return request({
    url: '/chart/view/getData/' + id,
    method: 'post',
    loading: true,
    hideMsg: true,
    timeout: 30000,
    data
  })
}
