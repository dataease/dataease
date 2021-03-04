import request from '@/utils/request'
const pathMap = {
  queryPath: '/api/user/userGrid/',
  deletePath: '/api/user/delete/',
  createPath: '/api/user/create',
  updatePath: '/api/user/update',
  editPasswordPath: '/api/user/password',
  editStatusPath: '/api/user/updateStatus'
}
export function userLists(page, size, data) {
  return request({
    url: pathMap.queryPath + page + '/' + size,
    method: 'post',
    data,
    loading: true
  })
}

export const addUser = (data) => {
  return request({
    url: pathMap.createPath,
    method: 'post',
    data
  })
}

export const editUser = (data) => {
  return request({
    url: pathMap.updatePath,
    method: 'post',
    data
  })
}

export const delUser = (userId) => {
  return request({
    url: pathMap.deletePath + userId,
    method: 'post'
  })
}

export const editPassword = (data) => {
  return request({
    url: pathMap.editPasswordPath,
    method: 'post',
    data
  })
}

export const editStatus = (data) => {
  return request({
    url: pathMap.editStatusPath,
    method: 'post',
    data
  })
}

export default { editPassword, delUser, editUser, addUser, userLists, editStatus }
