// import request from '@/utils/request'

// export function allRoles() {
//   return request({
//     url: '/api/user/all',
//     method: 'post',
//     loading: true
//   })
// }

// export function roleGrid(pageIndex, pageSize, data) {
//   return request({
//     url: '/api/role/roleGrid/' + pageIndex + '/' + pageSize,
//     method: 'post',
//     data,
//     loading: true
//   })
// }

// export function delRole(pid) {
//   return request({
//     url: '/api/role/delete/' + pid,
//     method: 'post'
//   })
// }

// export function addRole(data) {
//   return request({
//     url: '/api/role/create',
//     method: 'post',
//     data
//   })
// }

// export function editRole(data) {
//   return request({
//     url: '/api/role/update',
//     method: 'post',
//     data
//   })
// }

// export function addRoleMenus(data) {
//   return request({
//     url: '/api/role/saveRolesMenus',
//     method: 'post',
//     data
//   })
// }

// export function menuIds(roleId) {
//   return request({
//     url: '/api/role/menuIds/' + roleId,
//     method: 'post'
//   })
// }
// export default { addRole, editRole, delRole, roleGrid, allRoles, addRoleMenus, menuIds }
