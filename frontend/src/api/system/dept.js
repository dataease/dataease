import request from '@/utils/request'

export function getDeptTree(pid) {
  return request({
    url: 'api/dept/childNodes/' + pid,
    method: 'post'
  })
}

export function addDept(data) {
  return request({
    url: '/api/dept/create',
    method: 'post',
    data
  })
}

export function delDept(ids) {
  return request({
    url: '/api/dept/delete',
    method: 'post',
    data: ids
  })
}

export function editDept(data) {
  return request({
    url: '/api/dept/update',
    method: 'post',
    data
  })
}

export default { addDept, delDept, editDept, getDeptTree }
