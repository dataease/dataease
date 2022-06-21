import request from '@/utils/request'

export function query(pageIndex, pageSize, data) {
  return request({
    url: '/api/sys_msg/list/' + pageIndex + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}

export function unReadCount() {
  return request({
    url: '/api/sys_msg/unReadCount',
    method: 'post',
    loading: false
    // data
  })
}

export function updateStatus(msgId) {
  return request({
    url: '/api/sys_msg/setReaded/' + msgId,
    method: 'post',
    loading: true
  })
}

export function batchRead(data) {
  return request({
    url: '/api/sys_msg/batchRead',
    method: 'post',
    loading: true,
    data
  })
}

export function allRead() {
  return request({
    url: '/api/sys_msg/allRead',
    method: 'post',
    loading: true
  })
}

export function batchDelete(data) {
  return request({
    url: '/api/sys_msg/batchDelete',
    method: 'post',
    loading: true,
    data
  })
}

export function treeList() {
  return request({
    url: '/api/sys_msg/treeNodes',
    method: 'post',
    loading: true
  })
}

export function channelList() {
  return request({
    url: '/api/sys_msg/channelList',
    method: 'post',
    loading: true
  })
}

export function settingList() {
  return request({
    url: '/api/sys_msg/settingList',
    method: 'post',
    loading: true
  })
}

export function updateSetting(data) {
  return request({
    url: '/api/sys_msg/updateSetting',
    method: 'post',
    loading: true,
    data
  })
}

export function batchUpdate(data) {
  return request({
    url: '/api/sys_msg/batchUpdate',
    method: 'post',
    loading: true,
    data
  })
}

export function allTypes(data) {
  return request({
    url: '/api/sys_msg/types',
    method: 'post',
    loading: true
  })
}

