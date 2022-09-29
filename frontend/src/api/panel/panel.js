import request from '@/utils/request'
import { panelInit } from '@/components/canvas/utils/utils'
import { getPanelAllLinkageInfo } from '@/api/panel/linkage'
import { queryPanelJumpInfo } from '@/api/panel/linkJump'
import store from '@/store'

export function deleteSubject(id) {
  return request({
    url: '/panel/subject/delete/' + id,
    method: 'delete',
    loading: true
  })
}

export function saveOrUpdateSubject(data) {
  return request({
    url: '/panel/subject/update',
    method: 'post',
    loading: true,
    data
  })
}

export function querySubject(data) {
  return request({
    url: '/panel/subject/query',
    method: 'post',
    loading: true,
    data
  })
}

export function querySubjectWithGroup(data) {
  return request({
    url: '/panel/subject/querySubjectWithGroup',
    method: 'post',
    data
  })
}

export function defaultTree(data, loading = true, timeout = 60000) {
  return request({
    url: '/panel/group/defaultTree',
    method: 'post',
    loading: loading,
    data
  })
}

export function groupTree(data, loading = true, timeout = 60000) {
  return request({
    url: '/panel/group/tree',
    method: 'post',
    loading: loading,
    data
  })
}

export function viewData(id, panelId, data) {
  return request({
    url: '/chart/view/getData/' + id + '/' + panelId,
    method: 'post',
    hideMsg: true,
    data
  })
}
export function panelSave(data) {
  return request({
    url: 'panel/group/save',
    method: 'post',
    loading: true,
    data
  })
}

export function panelUpdate(data) {
  return request({
    url: 'panel/group/update',
    method: 'post',
    loading: true,
    data
  })
}

export function findOne(id) {
  return request({
    url: 'panel/group/findOne/' + id,
    method: 'get',
    loading: true
  })
}

export function viewPanelLog(data) {
  return request({
    url: 'panel/group/viewLog',
    method: 'post',
    loading: true,
    data
  })
}

export function getTable(id) {
  return request({
    url: '/panel/table/get/' + id,
    method: 'post'
  })
}

export function getPreviewData(data) {
  return request({
    url: '/panel/table/getPreviewData',
    method: 'post',
    data
  })
}

export function fieldList(id) {
  return request({
    url: '/panel/field/list/' + id,
    method: 'post'
  })
}

export function batchEdit(data) {
  return request({
    url: '/panel/field/batchEdit',
    method: 'post',
    data
  })
}

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    loading: true,
    data
  })
}

export function get(url) {
  return request({
    url: url,
    method: 'get'
  })
}

export function delGroup(groupId) {
  return request({
    url: '/panel/group/deleteCircle/' + groupId,
    loading: true,
    method: 'post'
  })
}

export function initPanelData(panelId, useCache = false, callback) {
  const queryMethod = useCache ? findUserCacheRequest : findOne
  // 加载视图数据
  queryMethod(panelId).then(response => {
    // 初始化视图data和style 数据
    panelInit(JSON.parse(response.data.panelData), JSON.parse(response.data.panelStyle))
    // 设置当前仪表板全局信息
    store.dispatch('panel/setPanelInfo', {
      id: response.data.id,
      name: response.data.name,
      privileges: response.data.privileges,
      sourcePanelName: response.data.sourcePanelName,
      status: response.data.status,
      createBy: response.data.createBy,
      createTime: response.data.createTime,
      creatorName: response.data.creatorName,
      updateBy: response.data.updateBy,
      updateName: response.data.updateName,
      updateTime: response.data.updateTime
    })
    // 刷新联动信息
    getPanelAllLinkageInfo(panelId).then(rsp => {
      store.commit('setNowPanelTrackInfo', rsp.data)
    })
    // 刷新跳转信息
    queryPanelJumpInfo(panelId).then(rsp => {
      store.commit('setNowPanelJumpInfo', rsp.data)
    })
    callback(response)
  })
}

export function queryPanelViewTree() {
  return request({
    url: '/panel/group/queryPanelViewTree',
    method: 'post'
  })
}

export function queryPanelMultiplexingViewTree() {
  return request({
    url: '/panel/group/queryPanelMultiplexingViewTree',
    method: 'post',
    loading: false
  })
}

export function initPanelComponentsData(panelId, callback) {
  // 加载仪表板组件视图数据
  queryPanelComponents(panelId).then(rep => {
    store.commit('initPanelComponents', rep.data)
    callback(rep)
  })
}

export function queryPanelComponents(id) {
  return request({
    url: 'panel/group/queryPanelComponents/' + id,
    method: 'get',
    loading: false
  })
}

export function initViewCache(panelId) {
  // 初始化仪表板视图缓存
  return request({
    url: 'chart/view/initViewCache/' + panelId,
    method: 'post',
    loading: false
  })
}
export function exportDetails(data) {
  // 初始化仪表板视图缓存
  return request({
    url: 'panel/group/exportDetails',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

export function innerExportDetails(data) {
  return request({
    url: 'panel/group/innerExportDetails',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

export function updatePanelStatus(panelId, param) {
  return request({
    url: '/panel/group/updatePanelStatus/' + panelId,
    method: 'post',
    loading: false,
    data: param
  })
}

export function saveCache(data) {
  return request({
    url: 'panel/group/autoCache',
    method: 'post',
    loading: false,
    data
  })
}
export function findUserCacheRequest(panelId) {
  return request({
    url: 'panel/group/findUserCache/' + panelId,
    method: 'get',
    loading: false
  })
}

export function checkUserCacheRequest(panelId) {
  return request({
    url: 'panel/group/checkUserCache/' + panelId,
    method: 'get',
    loading: false
  })
}

export function checkUserCache(panelId, callback) {
  // 加载视图数据
  checkUserCacheRequest(panelId).then(response => {
    callback(response)
  })
}

export function removePanelCache(panelId) {
  return request({
    url: 'panel/group/removePanelCache/' + panelId,
    method: 'delete',
    loading: false
  })
}


export function findPanelElementInfo(viewId) {
  return request({
    url: 'panel/group/findPanelElementInfo/'+viewId,
    method: 'get',
    loading: false
  })
}

export function export2AppCheck(panelId){
  return request({
    url: 'panel/group/export2AppCheck/'+panelId,
    method: 'get',
    loading: false
  })
}

export function appApply(data) {
  return request({
    url: 'panel/group/appApply',
    method: 'post',
    loading: true,
    data
  })
}
