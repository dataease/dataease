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
    loading: true
  })
}

// export function addDept(data) {
//   return request({
//     url: '/api/dept/create',
//     method: 'post',
//     data
//   })
// }

// export function delDept(ids) {
//   return request({
//     url: '/api/dept/delete',
//     method: 'post',
//     data: ids
//   })
// }

// export function editDept(data) {
//   return request({
//     url: '/api/dept/update',
//     method: 'post',
//     data
//   })
// }

export function treeByDeptId(deptId) {
  return request({
    url: '/api/dept/nodesByDeptId/' + deptId,
    method: 'post'
  })
}

// export default { addDept, delDept, editDept, getDeptTree, loadTable, treeByDeptId }
