import request from '@/utils/request'

export function deleteSubject(id) {
  return request({
    url: '/panel/subject/delete/' + id,
    method: 'delete',
    loading: true
  })
}

export function saveSubject(data) {
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
    loading: true,
    data
  })
}

export function defaultTree(data) {
  return request({
    url: '/panel/group/defaultTree',
    method: 'post',
    loading: true,
    data
  })
}

export function groupTree(data) {
  return request({
    url: '/panel/group/tree',
    method: 'post',
    loading: true,
    data
  })
}

export function viewData(id, data) {
  return request({
    url: '/chart/view/getData/' + id,
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
    method: 'post'
  })
}
