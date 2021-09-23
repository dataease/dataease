import request from '@/utils/request'
const pathMap = {
  userUpdatePwdPath: '/api/user/updatePwd/',
  personInfoPath: '/api/user/personInfo/',
  piupdatePath: '/api/user/updatePersonInfo/',
  queryPath: '/api/user/userGrid/',
  deletePath: '/api/user/delete/',
  createPath: '/api/user/create',
  updatePath: '/api/user/update',
  editPasswordPath: '/api/user/adminUpdatePwd',
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

export const persionInfo = () => {
  return request({
    url: pathMap.personInfoPath,
    method: 'post'
  })
}

export const updatePerson = (data) => {
  return request({
    url: pathMap.piupdatePath,
    method: 'post',
    data
  })
}

export const updatePersonPwd = (data) => {
  return request({
    url: pathMap.userUpdatePwdPath,
    method: 'post',
    data
  })
}

export const allRoles = () => {
  return request({
    url: '/api/user/all',
    method: 'post',
    loading: true
  })
}

export function roleGrid(pageIndex, pageSize, data) {
  return request({
    url: '/api/user/roleGrid/' + pageIndex + '/' + pageSize,
    method: 'post',
    data,
    loading: true
  })
}

export function ldapUsers(data) {
  return request({
    url: '/plugin/ldap/users',
    method: 'post',
    loading: true
  })
}

export function saveLdapUser(data) {
  return request({
    url: '/api/user/sync',
    method: 'post',
    loading: true,
    data
  })
}

export function existLdapUsers() {
  return request({
    url: '/api/user/existLdapUsers',
    method: 'post',
    loading: false
  })
}

export default { editPassword, delUser, editUser, addUser, userLists, editStatus, persionInfo, updatePerson, updatePersonPwd, allRoles, roleGrid, ldapUsers, saveLdapUser, existLdapUsers }
