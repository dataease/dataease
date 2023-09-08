import request from '@/utils/request'

export function loadTable(data) {
  return request({
    url: '/dataset/table/list',
    method: 'post',
    loading: true,
    data
  })
}

export function getScene(sceneId) {
  return request({
    url: '/dataset/group/getScene/' + sceneId,
    loading: true,
    method: 'post'
  })
}

export function addGroup(data) {
  return request({
    url: '/dataset/group/save',
    method: 'post',
    loading: true,
    data
  })
}

export function delGroup(groupId) {
  return request({
    url: '/dataset/group/delete/' + groupId,
    loading: true,
    method: 'post'
  })
}

export function addTable(data) {
  return request({
    url: '/dataset/table/update',
    method: 'post',
    loading: true,
    data
  })
}

export function alter(data) {
  return request({
    url: '/dataset/table/alter',
    method: 'post',
    loading: true,
    data
  })
}

export function delTable(tableId) {
  return request({
    url: '/dataset/table/delete/' + tableId,
    loading: true,
    method: 'post'
  })
}

export function groupTree(data) {
  return request({
    url: '/dataset/group/tree',
    method: 'post',
    loading: true,
    data
  })
}

export function dsGroupTree(data) {
  return request({
    url: '/dataset/group/tree',
    method: 'post',
    loading: true,
    data
  })
}

export function listDatasource() {
  return request({
    url: '/datasource/list',
    loading: true,
    method: 'get'
  })
}

export function getDatasetList() {
  return request({
    url: 'dataset/table/list',
    loading: false,
    method: 'post',
    data: {}
  })
}

export function getPanelGroupList() {
  return request({
    url: '/panel/group/list',
    loading: false,
    method: 'get'
  })
}

export function listApiDatasource() {
  return request({
    url: '/datasource/list/api',
    loading: true,
    method: 'get'
  })
}

export function getTable(id, hideMsg = false) {
  return request({
    url: '/dataset/table/get/' + id,
    loading: false,
    method: 'post',
    hideMsg: hideMsg
  })
}

export function getPreviewData(data) {
  return request({
    url: '/dataset/table/getPreviewData',
    method: 'post',
    loading: true,
    data
  })
}

export function fieldList(id, showLoading = true) {
  return request({
    url: '/dataset/field/list/' + id,
    loading: showLoading,
    method: 'post'
  })
}

export function fieldListWithPermission(id, showLoading = true) {
  //初始模板中的过滤组件无需走后台
  if (id.indexOf('no_auth') > -1) {
    return new Promise(function(resolve) {
      resolve({
        data: []
      })
    })
  }
  return request({
    url: '/dataset/field/listWithPermission/' + id,
    loading: showLoading,
    method: 'post'
  })
}

export function datasetParams(id, type, showLoading = true) {
  return request({
    url: '/dataset/table/params/' + id + '/' + type,
    loading: showLoading,
    method: 'post'
  })
}

export function fieldListDQ(id, showLoading = true) {
  return request({
    url: '/dataset/field/listByDQ/' + id,
    loading: showLoading,
    method: 'post'
  })
}

export function dateformats(id, showLoading = true) {
  return request({
    url: '/dataset/field/dateformats/' + id,
    loading: showLoading,
    method: 'post'
  })
}

export function batchEdit(data) {
  return request({
    url: '/dataset/field/batchEdit',
    method: 'post',
    loading: true,
    data
  })
}

export function post(url, data, showLoading = true, timeout = 60000, hideMsg) {
  return request({
    url: url,
    method: 'post',
    loading: showLoading,
    hideMsg,
    data
  })
}

export function mappingFieldValues(data) {
  return request({
    url: '/dataset/field/mappingFieldValues',
    method: 'post',
    loading: false,
    data
  })
}

export function linkMappingFieldValues(data) {
  return request({
    url: '/dataset/field/linkMappingFieldValues',
    method: 'post',
    loading: true,
    data
  })
}

export function multFieldValues(data) {
  return request({
    url: '/dataset/field/multFieldValues',
    method: 'post',
    loading: false,
    data
  })
}

export function linkMultFieldValues(data) {
  return request({
    url: '/dataset/field/linkMultFieldValues',
    method: 'post',
    loading: true,
    data
  })
}

export function isKettleRunning(showLoading = true) {
  return request({
    url: '/dataset/group/isKettleRunning',
    method: 'post',
    loading: showLoading
  })
}

export function taskList(spage, size, data) {
  return request({
    url: '/dataset/group/isKettleRunning',
    method: 'post',
    // eslint-disable-next-line no-undef
    loading: showLoading
  })
}

export function datasetTaskList(page, size, data, loading) {
  return request({
    url: '/dataset/task/pageList/' + page + '/' + size,
    method: 'post',
    data,
    loading: loading
  })
}

export function datasetRowPermissionsList(datasetId, page, size, data, loading) {
  return request({
    url: 'plugin/dataset/rowPermissions/pageList/' + datasetId + '/' + page + '/' + size,
    method: 'post',
    data,
    loading: loading
  })
}

export function checkCustomDs() {
  return request({
    url: '/system/checkCustomDs',
    method: 'post',
    loading: true
  })
}

export function exportExcel(data) {
  return request({
    url: '/dataset/taskLog/export',
    method: 'post',
    loading: true,
    responseType: 'blob',
    data
  })
}

export function dsTable(page, size, id) {
  return request({
    url: '/datasource/getTables/' + id + '/' + page + '/' + size,
    method: 'post'
  })
}

export function exportDataset(data) {
  // 初始化仪表板视图缓存
  return request({
    url: 'dataset/table/exportDataset',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

export default { loadTable, getScene, addGroup, delGroup, addTable, delTable, groupTree, checkCustomDs, exportDataset }
