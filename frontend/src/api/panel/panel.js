import request from '@/utils/request'

export function loadTable(data) {
  return request({
    url: '/dataset/table/list',
    method: 'post',
    data
  })
}

export function getScene(sceneId) {
  return request({
    url: '/dataset/group/getScene/' + sceneId,
    method: 'post'
  })
}

export function addGroup(data) {
  return request({
    url: '/dataset/group/save',
    method: 'post',
    data
  })
}

export function delGroup(groupId) {
  return request({
    url: '/dataset/group/delete/' + groupId,
    method: 'post'
  })
}

export function addTable(data) {
  return request({
    url: '/dataset/table/update',
    method: 'post',
    data
  })
}

export function delTable(tableId) {
  return request({
    url: '/dataset/table/delete/' + tableId,
    method: 'post'
  })
}

export function groupTree(data) {
  return request({
    url: '/dataset/group/tree',
    method: 'post',
    data
  })
}

export function listDatasource() {
  return request({
    url: '/datasource/list',
    method: 'get'
  })
}

export function getTable(id) {
  return request({
    url: '/dataset/table/get/' + id,
    method: 'post'
  })
}

export function getPreviewData(data) {
  return request({
    url: '/dataset/table/getPreviewData',
    method: 'post',
    data
  })
}

export function fieldList(id) {
  return request({
    url: '/dataset/field/list/' + id,
    method: 'post'
  })
}

export function batchEdit(data) {
  return request({
    url: '/dataset/field/batchEdit',
    method: 'post',
    data
  })
}

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    data
  })
}

export default { loadTable, getScene, addGroup, delGroup, addTable, delTable, groupTree }
