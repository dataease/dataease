import request from '@/utils/request'
import store from '@/store'
import { queryPanelComponents } from '@/api/panel/panel'

export function post(url, data, loading = false) {
  return request({
    url: url,
    method: 'post',
    loading: loading,
    data
  })
}

export function getChartTree(data) {
  return request({
    url: 'api',
    method: 'post',
    loading: false,
    data
  })
}

export function chartCopy(id, panelId) {
  return request({
    url: '/chart/view/chartCopy/' + id + '/' + panelId,
    method: 'post',
    loading: false
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
    loading: false
  })
}

export function checkSameDataSet(viewIdSource, viewIdTarget) {
  return request({
    url: '/chart/view/checkSameDataSet/' + viewIdSource + '/' + viewIdTarget,
    method: 'get',
    loading: false
  })
}

export function ajaxGetDataOnly(id, panelId, data) {
  return request({
    url: '/chart/view/getData/' + id + '/' + panelId,
    method: 'post',
    loading: false,
    hideMsg: true,
    data
  })
}

export function pluginTypes() {
  return request({
    url: '/plugin/view/types',
    method: 'post'
  })
}

export function deleteCircle(id) {
  return request({
    url: '/chart/group/deleteCircle/' + id,
    method: 'post',
    loading: true
  })
}

export function getChartDetails(id, panelId, data) {
  return request({
    url: '/chart/view/get/' + id + '/' + panelId,
    method: 'post',
    loading: false,
    data
  })
}

export function save2Cache(panelId, data) {
  return request({
    url: '/chart/view/save2Cache/' + panelId,
    method: 'post',
    loading: false,
    data
  })
}

export function resetViewCacheCallBack(viewId, panelId, callback) {
  // 加载仪表板组件视图数据
  resetViewCache(viewId, panelId).then(rep => {
    callback(rep)
  })
}
export function resetViewCache(viewId, panelId) {
  return request({
    url: '/chart/view/resetViewCache/' + viewId + '/' + panelId,
    method: 'post',
    loading: false
  })
}

export function checkTitle(data) {
  return request({
    url: '/chart/view/checkTitle',
    method: 'post',
    data: data,
    loading: false
  })
}
