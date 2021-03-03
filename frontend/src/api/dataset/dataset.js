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
export default { loadTable, getScene, addGroup, delGroup, addTable, delTable, groupTree }
