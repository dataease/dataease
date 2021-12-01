import request from '@/utils/request'

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
    timeout: 30000,
    data
  })
}

export function querySubjectWithGroup(data) {
  return request({
    url: '/panel/subject/querySubjectWithGroup',
    method: 'post',
    timeout: 30000,
    data
  })
}

export function defaultTree(data, loading = true, timeout = 30000) {
  return request({
    url: '/panel/group/defaultTree',
    method: 'post',
    loading: loading,
    timeout: timeout,
    data
  })
}

export function groupTree(data, loading = true, timeout = 30000) {
  return request({
    url: '/panel/group/tree',
    method: 'post',
    loading: loading,
    timeout: timeout,
    data
  })
}

export function viewData(id, data) {
  return request({
    url: '/chart/view/getData/' + id,
    method: 'post',
    timeout: 30000,
    hideMsg: true,
    data
  })
}
export function panelSave(data) {
  return request({
    url: 'panel/group/save',
    method: 'post',
    timeout: 30000,
    loading: true,
    data
  })
}
export function findOne(id) {
  return request({
    url: 'panel/group/findOne/' + id,
    method: 'get',
    loading: true,
    timeout: 30000
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
    timeout: 30000,
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
    method: 'post',
    timeout: 30000
  })
}
