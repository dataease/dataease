import request from '@/utils/request'

export function getDeptTree(pid) {
  return request({
    url: 'api/dept/childNodes/' + pid,
    method: 'post',
    loading: true
  })
}

export function loadTable(data) {
  return request({
    url: 'api/dept/search',
    method: 'post',
    data,
    loading: false
  })
}

export function treeByDeptId(deptId) {
  return request({
    url: '/api/dept/nodesByDeptId/' + deptId,
    method: 'post'
  })
}
