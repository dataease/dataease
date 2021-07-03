import request from '@/utils/request'

export function query(pageIndex, pageSize, data) {
  return request({
    url: '/api/sys_msg/list/' + pageIndex + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}

export function updateStatus(msgId) {
  return request({
    url: '/api/sys_msg/setReaded/' + msgId,
    method: 'post',
    loading: true
  })
}

